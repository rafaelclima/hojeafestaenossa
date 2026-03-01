package com.rafaellima.hojeafestaenossa.infra.storage.oci;

import java.io.InputStream;
import org.springframework.stereotype.Service;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.rafaellima.hojeafestaenossa.infra.storage.StorageService;
import com.rafaellima.hojeafestaenossa.shared.config.OciProperties;

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

}
