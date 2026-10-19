package com.capstone.backend.security.oauth;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Map;

public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    // 소셜 로그인은 Google만 운영해요. 제공자를 늘리면 OAuth2UserInfo 구현을 추가하고 여기서 분기해요 (TS-21)
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        }

        throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
                "지원하지 않는 OAuth provider입니다: " + registrationId);
    }
}
