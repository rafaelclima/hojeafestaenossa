package com.rafaellima.hojeafestaenossa.web;

import java.util.UUID;

public record EventResponse(
                UUID id,
                String accessToken,
                boolean publicAlbum) {

}
