package com.ra1n.top.api.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePlayerRequest(

        @NotBlank(message = "must not be blank")
        @Pattern(regexp = "^[A-Za-z0-9_-]{3,32}$",
                message = "must be 3-32 characters long and contain only letters, digits, '_' or '-'")
        String nickname,

        @Size(max = 64, message = "must be at most 64 characters long")
        String displayName) {
}
