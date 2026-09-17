package com.ra1n.top.api.error;

import java.util.UUID;

public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(UUID id) {
        super("Player %s was not found".formatted(id));
    }
}
