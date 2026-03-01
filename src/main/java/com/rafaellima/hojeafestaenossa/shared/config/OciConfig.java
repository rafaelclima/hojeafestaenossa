package com.rafaellima.hojeafestaenossa.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;

@Configuration
public class OciConfig {

    @Value("${oci.tenancy-id")
    private String tenancyId;

    @Value("${oci.user-id")
    private String userId;

    @Value("${oci.fingerprint")
    private String fingerprint;

    @Value("${oci.private-key-file")
    private String privateKeyFilePath;

    @Value("${oci.region")
    private String region;

    @Bean
    public ObjectStorageClient objectStorageClient(
            BasicAuthenticationDetailsProvider provider,
            OciProperties props) {

        ObjectStorageClient client = ObjectStorageClient.builder()
                .region(props.getRegion())
                .build(provider);

        return client;
    }

}
