package com.rafaellima.hojeafestaenossa.infra.storage.oci;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.rafaellima.hojeafestaenossa.infra.storage.StorageService;
import com.rafaellima.hojeafestaenossa.shared.config.OciProperties;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.storage.type", havingValue = "oci")
@RequiredArgsConstructor
public class OciRestStorageService implements StorageService {

    private final OciProperties props;
    private ObjectStorage objectStorageClient;

    @PostConstruct
    public void init() {
        // O SDK da OCI é projetado para ler o arquivo PEM diretamente.
        // Você fornece um "supplier" para o InputStream, e o SDK lida com o parsing.
        // Isso é muito mais seguro e robusto do que a manipulação manual de strings.
        Supplier<InputStream> privateKeySupplier = () -> {
            try {
                return new FileInputStream(props.getPrivateKeyPath());
            } catch (FileNotFoundException e) {
                log.error("Arquivo de chave privada do OCI não encontrado no caminho: {}", props.getPrivateKeyPath(),
                        e);
                throw new UncheckedIOException(
                        "Arquivo de chave privada do OCI não encontrado: " + props.getPrivateKeyPath(), e);
            }
        };

        AuthenticationDetailsProvider provider = SimpleAuthenticationDetailsProvider.builder()
                .tenantId(props.getTenancyId())
                .userId(props.getUserId())
                .fingerprint(props.getFingerprint())
                .privateKeySupplier(privateKeySupplier)
                .region(Region.fromRegionCode(props.getRegion()))
                .build();

        this.objectStorageClient = ObjectStorageClient.builder().build(provider);

        log.info("OCI Object Storage Service inicializado com sucesso para a região: {}", props.getRegion());
    }

    @Override
    public String upload(String objectName, InputStream inputStream, long contentLength, String contentType) {
        log.info("Iniciando upload para OCI: objectName={}, contentLength={}, contentType={}", 
                objectName, contentLength, contentType);
        log.info("OCI Config: namespace={}, bucketName={}", props.getNamespace(), props.getBucketName());
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .namespaceName(props.getNamespace())
                    .bucketName(props.getBucketName())
                    .objectName(objectName)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .putObjectBody(inputStream)
                    .build();
            
            log.info("Enviando request para OCI...");
            objectStorageClient.putObject(putObjectRequest);
            log.info("Upload concluído com sucesso para: {}", objectName);

            String url = buildObjectUrl(objectName);
            log.info("URL gerada: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Falha ao fazer upload do objeto '{}' para o bucket OCI '{}'. Namespace: {}, Bucket: {}", 
                    objectName, props.getBucketName(), props.getNamespace(), props.getBucketName(), e);
            throw new RuntimeException("Failed to upload to OCI: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String objectName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .namespaceName(props.getNamespace())
                    .bucketName(props.getBucketName())
                    .objectName(objectName)
                    .build();

            objectStorageClient.deleteObject(deleteObjectRequest);
            log.info("Objeto '{}' deletado com sucesso do bucket OCI '{}'", objectName, props.getBucketName());
        } catch (Exception e) {
            log.error("Falha ao deletar o objeto '{}' do bucket OCI '{}'", objectName, props.getBucketName(), e);
            throw new RuntimeException("Failed to delete from OCI: " + e.getMessage(), e);
        }
    }

    private String buildObjectUrl(String objectName) {
        // Este formato de URL assume que o bucket tem visibilidade pública.
        return String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                props.getRegion(), props.getNamespace(), props.getBucketName(), objectName);
    }
}
