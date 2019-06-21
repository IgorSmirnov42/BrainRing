package ru.spbhse.brainring.utils;

/** Roles in local game with codes for quick game automatching */
public enum LocalGameRoles {
    ROLE_ADMIN(1),
    ROLE_GREEN(1 << 1),
    ROLE_RED(1 << 2);

    private int connectionCode;

    LocalGameRoles(int code) {
        this.connectionCode = code;
    }

    public int getCode() {
        return connectionCode;
    }
}
