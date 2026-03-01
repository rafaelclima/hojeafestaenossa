package com.rafaellima.hojeafestaenossa.upload.application;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.upload.domain.Upload;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadMediaService {

    private final UploadRepository uploadRepository;

    public Upload execute(Upload upload) {
        return uploadRepository.save(upload);
    }
}
