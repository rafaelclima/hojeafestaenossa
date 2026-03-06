package com.rafaellima.hojeafestaenossa.upload.application;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.upload.domain.Upload;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListModerationAdminService {

    private final UploadRepository uploadRepository;

    public Page<Upload> execute(UUID eventId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return uploadRepository.findAllByEventIdOrderByCreatedAtDesc(eventId, pageable);
    }

}
