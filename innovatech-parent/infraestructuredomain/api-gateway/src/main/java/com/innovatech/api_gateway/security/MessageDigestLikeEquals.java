package com.innovatech.api_gateway.security;

final class MessageDigestLikeEquals {

    private MessageDigestLikeEquals() {
    }

    static boolean equals(byte[] first, byte[] second) {
        if (first.length != second.length) {
            return false;
        }

        int result = 0;
        for (int index = 0; index < first.length; index++) {
            result |= first[index] ^ second[index];
        }

        return result == 0;
    }
}
