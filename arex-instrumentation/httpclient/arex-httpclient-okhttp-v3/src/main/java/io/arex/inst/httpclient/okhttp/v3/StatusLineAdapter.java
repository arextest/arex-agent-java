package io.arex.inst.httpclient.okhttp.v3;

import okhttp3.Protocol;
import okhttp3.internal.http.StatusLine;

import java.io.IOException;
import java.net.ProtocolException;

/**
 * This file is a copy of okhttp3.internal.http.StatusLine, compatible with version 3.x
 * Version 4.x is in the Inner class StatusLine#companion#parse
 */
public class StatusLineAdapter {
    private static final String EXCEPTION_PREFIX = "Unexpected status line: ";

    private StatusLineAdapter() {}

    public static StatusLine parse(String statusLine) throws IOException {
        // H T T P / 1 . 1   2 0 0   T e m p o r a r y   R e d i r e c t
        // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0

        // Parse protocol like "HTTP/1.1" followed by a space.
        int codeStart;
        Protocol protocol;
        if (statusLine.startsWith("HTTP/1.")) {
            if (statusLine.length() < 9 || statusLine.charAt(8) != ' ') {
                throw new ProtocolException(EXCEPTION_PREFIX + statusLine);
            }
            int httpMinorVersion = statusLine.charAt(7) - '0';
            codeStart = 9;
            if (httpMinorVersion == 0) {
                protocol = Protocol.HTTP_1_0;
            } else if (httpMinorVersion == 1) {
                protocol = Protocol.HTTP_1_1;
            } else {
                throw new ProtocolException(EXCEPTION_PREFIX + statusLine);
            }
        } else if (statusLine.startsWith("ICY ")) {
            // Shoutcast uses ICY instead of "HTTP/1.0".
            protocol = Protocol.HTTP_1_0;
            codeStart = 4;
        } else {
            throw new ProtocolException(EXCEPTION_PREFIX + statusLine);
        }

        // Parse response code like "200". Always 3 digits.
        if (statusLine.length() < codeStart + 3) {
            throw new ProtocolException(EXCEPTION_PREFIX + statusLine);
        }
        int code;
        try {
            code = Integer.parseInt(statusLine.substring(codeStart, codeStart + 3));
        } catch (NumberFormatException e) {
            throw new ProtocolException(EXCEPTION_PREFIX + statusLine);
        }

        // Parse an optional response message like "OK" or "Not Modified". If it
        // exists, it is separated from the response code by a space.
        String message = "";
        if (statusLine.length() > codeStart + 3) {
            if (statusLine.charAt(codeStart + 3) != ' ') {
                throw new ProtocolException(EXCEPTION_PREFIX + statusLine);
            }
            message = statusLine.substring(codeStart + 4);
        }

        return new StatusLine(protocol, code, message);
    }
}
