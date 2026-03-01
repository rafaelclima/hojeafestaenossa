package com.rafaellima.hojeafestaenossa.upload.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.upload.domain.Upload;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListSlideshowUploadsService {

    private final UploadRepository uploadRepository;

    public List<Upload> execute(UUID eventId) {
        return uploadRepository.findTop50ByEventIdAndVisibleTrueOrderByCreatedAtDesc(eventId);
    }

}
