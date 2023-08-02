package io.arex.inst.authentication.jwt;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.impl.*;
import io.jsonwebtoken.lang.Strings;

import java.util.Map;

public class JJWTGenerator {
    /**
     * refer to {@link DefaultJwtParser#parse(String)}
     * remove the exception throw (such as ExpiredJwtException)
     * @param jwt (token)
     * @return if null mean execute original business code, not replay
     */
    public static Jwt generate(String jwt) {
        if (!ContextManager.needReplay()) {
            return null;
        }
        try {
            return parse(jwt);
        } catch (Throwable e) {
            LogManager.warn("parse jjwt error", e.getMessage());
            return null;
        }
    }

    /**
     * this method is currently compatible with the following versions:
     * jjwt: 0.1 ~ 0.9.1
     * jjwt-api: 0.10.0 ~ 0.11.2
     * (there may be differences in the future versions)
     */
    private static Jwt parse(String jwt) {
        if (StringUtil.isEmpty(jwt)) {
            return null;
        }
        String base64UrlEncodedHeader = null;
        String base64UrlEncodedPayload = null;
        String base64UrlEncodedDigest = null;
        int delimiterCount = 0;
        StringBuilder sb = new StringBuilder(128);

        for (char c : jwt.toCharArray()) {
            if (c == '.') {
                CharSequence tokenSeq = Strings.clean(sb.toString());
                String token = tokenSeq != null ? tokenSeq.toString() : null;
                if (delimiterCount == 0) {
                    base64UrlEncodedHeader = token;
                } else if (delimiterCount == 1) {
                    base64UrlEncodedPayload = token;
                }

                delimiterCount++;
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        if (delimiterCount != 2) {
            return null;
        }
        if (sb.length() > 0) {
            base64UrlEncodedDigest = sb.toString();
        }

        if (base64UrlEncodedPayload == null) {
            return null;
        }

        // =============== Header =================
        Header header = null;
        if (base64UrlEncodedHeader != null) {
            String origValue = TextCodec.BASE64URL.decodeToString(base64UrlEncodedHeader);
            Map<String, Object> m = Serializer.deserialize(origValue, Map.class);
            if (m == null) {
                return null;
            }

            if (base64UrlEncodedDigest != null) {
                header = new DefaultJwsHeader(m);
            } else {
                header = new DefaultHeader(m);
            }
        }

        // =============== Body =================
        String payload = TextCodec.BASE64URL.decodeToString(base64UrlEncodedPayload);
        Claims claims = null;
        if (payload.charAt(0) == '{' && payload.charAt(payload.length() - 1) == '}') { //likely to be json, parse it:
            Map<String, Object> claimsMap = Serializer.deserialize(payload, Map.class);
            claims = new DefaultClaims(claimsMap);
        }

        Object body = claims != null ? claims : payload;
        if (base64UrlEncodedDigest != null) {
            return new DefaultJws((JwsHeader) header, body, base64UrlEncodedDigest);
        } else {
            return new DefaultJwt<>(header, body);
        }
    }
}
