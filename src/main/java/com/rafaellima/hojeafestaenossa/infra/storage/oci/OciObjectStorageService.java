package com.rafaellima.hojeafestaenossa.infra.storage.oci;

import java.io.InputStream;
import org.springframework.stereotype.Service;

import com.oracle.bmc.auth.exception.AuthClientException;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.rafaellima.hojeafestaenossa.infra.storage.StorageService;
import com.rafaellima.hojeafestaenossa.shared.config.OciProperties;
import com.rafaellima.hojeafestaenossa.shared.exception.TechnicalException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OciObjectStorageService implements StorageService {

    private final ObjectStorageClient client;
    private final OciProperties props;

    @Override
    public String upload(
            String objectName,
            InputStream inputStream,
            long contentLength,
            String contentType) {

        PutObjectRequest request = PutObjectRequest.builder()
                .namespaceName(props.getNamespace())
                .bucketName(props.getBucketName())
                .objectName(objectName)
                .contentLength(contentLength)
                .contentType(contentType)
                .putObjectBody(inputStream)
                .build();

        client.putObject(request);

        return buildObjectUrl(objectName);
    }

    private String buildObjectUrl(String objectName) {
        return String.format(
                "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                props.getRegion(),
                props.getNamespace(),
                props.getBucketName(),
                objectName);
    }

    @Override
    public void delete(String objectName) {

        try {

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .namespaceName(props.getNamespace())
                    .bucketName(props.getBucketName())
                    .objectName(objectName)
                    .build();

            client.deleteObject(request);

        } catch (AuthClientException e) {
            // TODO: handle exception
            throw new TechnicalException("500", "Erro de authentication " + e.getMessage());
        } catch (BmcException e) {
            // TODO: handle exception
            throw new TechnicalException("500", "Erro de bucket " + e.getMessage());
        } catch (Exception e) {
            // TODO: handle exception
            throw new TechnicalException("500", "Erro ao deletar arquivo do storage " + e.getMessage());
        }

    }

}
