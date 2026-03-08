package com.rafaellima.hojeafestaenossa.infra.storage;

import java.io.InputStream;

public interface StorageService {

    String upload(
            String objectName,
            InputStream inputStream,
            long contentLength,
            String contentType);

    void delete(String objectName);

}
