package com.rafaellima.hojeafestaenossa.infra.storage.oci;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.rafaellima.hojeafestaenossa.infra.storage.StorageService;
import com.rafaellima.hojeafestaenossa.shared.config.OciProperties;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@ConditionalOnProperty(name = "oci.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class OciRestStorageService implements StorageService {

    private final OciProperties props;
    private PrivateKey privateKey;
    private RestClient restClient;

    @PostConstruct
    public void init() throws Exception {
        String keyPath = props.getPrivateKeyPath();
        String privateKeyContent = Files.readString(Paths.get(keyPath));
        
        String base64Content = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        
        byte[] decodedKey = Base64.getDecoder().decode(base64Content);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));

        String region = props.getRegion();
        String baseUrl = String.format("https://objectstorage.%s.oraclecloud.com", region);
        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        System.out.println("OCI REST Storage initialized for bucket: " + props.getBucketName());
    }

    @Override
    public String upload(String objectName, InputStream inputStream, long contentLength, String contentType) {
        try {
            byte[] content = inputStream.readAllBytes();
            String date = Instant.now().toString();
            String region = props.getRegion();

            String path = String.format("/n/%s/b/%s/o/%s", props.getNamespace(), props.getBucketName(), objectName);

            String signature = signRequest("PUT", path, contentLength, contentType, date, new String(content, StandardCharsets.UTF_8));

            String url = String.format("https://objectstorage.%s.oraclecloud.com%s", region, path);

            restClient.put()
                    .uri(url)
                    .header("Date", date)
                    .header("Content-Type", contentType)
                    .header("Content-Length", String.valueOf(contentLength))
                    .header("Authorization", signature)
                    .header("Host", "objectstorage." + region + ".oraclecloud.com")
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(new ByteArrayInputStream(content))
                    .retrieve()
                    .toBodilessEntity();

            return buildObjectUrl(objectName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to OCI: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String objectName) {
        try {
            String date = Instant.now().toString();
            String region = props.getRegion();

            String path = String.format("/n/%s/b/%s/o/%s", props.getNamespace(), props.getBucketName(), objectName);

            String signature = signRequest("DELETE", path, 0, "", date, "");

            String url = String.format("https://objectstorage.%s.oraclecloud.com%s", region, path);

            restClient.delete()
                    .uri(url)
                    .header("Date", date)
                    .header("Authorization", signature)
                    .header("Host", "objectstorage." + region + ".oraclecloud.com")
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from OCI: " + e.getMessage(), e);
        }
    }

    private String signRequest(String method, String path, long contentLength, String contentType, String date, String body) throws Exception {
        String signingString = String.format("(request-target): %s %s\ndate: %s\nhost: objectstorage.%s.oraclecloud.com",
                method.toLowerCase(), path, date, props.getRegion());

        if (contentLength > 0) {
            signingString += String.format("\ncontent-length: %d\ncontent-type: %s", contentLength, contentType);
        }

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(signingString.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signer.sign();
        String signature = Base64.getEncoder().encodeToString(signatureBytes);

        return String.format("Signature version=\"1\", keyId=\"%s/%s\", algorithm=\"rsa-sha256\", headers=\"(request-target) date host%s%s\", signature=\"%s\"",
                props.getTenancyId(),
                props.getUserId(),
                contentLength > 0 ? " content-length content-type" : "",
                contentLength > 0 ? " (request-target) date host content-length content-type" : "(request-target) date host",
                signature);
    }

    private String buildObjectUrl(String objectName) {
        return String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                props.getRegion(), props.getNamespace(), props.getBucketName(), objectName);
    }
}
