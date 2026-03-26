package com.rafaellima.hojeafestaenossa.infra.storage;

import java.io.InputStream;
import java.io.OutputStream;

public interface StorageService {

    String upload(
            String objectName,
            InputStream inputStream,
            long contentLength,
            String contentType);

    void delete(String objectName);

    void download(String objectName, OutputStream outputStream);

}
