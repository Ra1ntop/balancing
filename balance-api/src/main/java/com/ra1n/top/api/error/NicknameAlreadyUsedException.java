package com.ra1n.top.api.error;

public class NicknameAlreadyUsedException extends RuntimeException {

    public NicknameAlreadyUsedException(String nickname) {
        super("Nickname '%s' is already taken".formatted(nickname));
    }
}
