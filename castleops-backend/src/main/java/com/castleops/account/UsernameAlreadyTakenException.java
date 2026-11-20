package com.castleops.account;

public class UsernameAlreadyTakenException extends RuntimeException {

    public UsernameAlreadyTakenException(Throwable cause) {
        super("Username is already taken.", cause);
    }
}