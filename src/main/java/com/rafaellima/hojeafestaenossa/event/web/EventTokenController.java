package com.rafaellima.hojeafestaenossa.event.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rafaellima.hojeafestaenossa.event.application.GenerateTokenService;
import com.rafaellima.hojeafestaenossa.event.domain.EventToken;
import com.rafaellima.hojeafestaenossa.event.repository.EventTokenRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/tokens")
@RequiredArgsConstructor
public class EventTokenController {

    private final GenerateTokenService generateTokenService;
    private final EventTokenRepository tokenRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventTokenResponse generate() {
        EventToken token = generateTokenService.execute();
        return new EventTokenResponse(token.getToken(), token.getCreatedAt());
    }

    @GetMapping
    public List<EventTokenResponse> listAll() {
        return tokenRepository.findAllOrderByCreatedAtDesc().stream()
            .map(t -> new EventTokenResponse(
                t.getToken(),
                t.getCreatedAt(),
                t.getUsedAt(),
                t.getEventId()
            ))
            .toList();
    }
}