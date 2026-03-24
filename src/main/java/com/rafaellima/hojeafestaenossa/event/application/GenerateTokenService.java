package com.rafaellima.hojeafestaenossa.event.application;

import java.security.SecureRandom;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.EventToken;
import com.rafaellima.hojeafestaenossa.event.repository.EventTokenRepository;
import com.rafaellima.hojeafestaenossa.shared.exception.TechnicalException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenerateTokenService {

    private final EventTokenRepository tokenRepository;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String PREFIX = "HAFEN-";
    private static final int SUFFIX_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 10;

    public EventToken execute() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String token = generateTokenCode();
            try {
                return tokenRepository.save(new EventToken(token));
            } catch (DataIntegrityViolationException e) {
                // Token duplicado, tenta novamente
            }
        }
        throw new TechnicalException("500", "Falha ao gerar token único. Tente novamente.");
    }

    private String generateTokenCode() {
        StringBuilder code = new StringBuilder(PREFIX);
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return code.toString();
    }
}