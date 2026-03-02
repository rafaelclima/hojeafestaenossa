package com.rafaellima.hojeafestaenossa.upload.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;
import com.rafaellima.hojeafestaenossa.upload.domain.Upload;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final UploadRepository uploadRepository;

    public void setVisibility(UUID uploadId, boolean visible) {
        Upload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new NotFoundException("404", "Upload não encontrado"));

        upload.setVisibility(visible);
        uploadRepository.save(upload);
    }

}
