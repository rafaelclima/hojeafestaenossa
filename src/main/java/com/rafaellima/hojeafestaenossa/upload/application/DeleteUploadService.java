package com.rafaellima.hojeafestaenossa.upload.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.infra.storage.StorageService;
import com.rafaellima.hojeafestaenossa.shared.exception.BusinessException;
import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;
import com.rafaellima.hojeafestaenossa.upload.domain.Upload;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteUploadService {

    private final UploadRepository uploadRepository;
    private final StorageService storageService;

    public void execute(UUID uploadId, UUID eventId) {

        Upload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new NotFoundException("404", "Upload não encontrado!"));

        if (!upload.getEventId().equals(eventId)) {
            throw new BusinessException("403", "Upload não pertence a esse evento!");
        }

        storageService.delete(upload.getStorageKey());
        uploadRepository.deleteById(uploadId);

    }
}
