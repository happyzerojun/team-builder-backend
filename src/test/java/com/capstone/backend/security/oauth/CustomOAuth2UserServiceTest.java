package com.capstone.backend.security.oauth;

import com.capstone.backend.entity.AuthProvider;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void createsGoogleUserWhenEmailDoesNotExist() {
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("google",
                Map.of("sub", "google-123", "email", "user@example.com", "name", "Google User"));

        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuth2User user = customOAuth2UserService.upsertOAuth2User("google", userInfo, Map.of("email", "user@example.com"));

        assertEquals("user@example.com", user.getName());
    }

    // Kakao·Naver 로그인은 제거했어요(TS-21). 등록되지 않은 제공자로 들어오면 사용자를 만들지 않고 거절해야 해요
    @Test
    void rejectsRemovedProviders() {
        for (String provider : new String[]{"kakao", "naver"}) {
            OAuth2AuthenticationException e = assertThrows(OAuth2AuthenticationException.class,
                    () -> OAuth2UserInfoFactory.getOAuth2UserInfo(provider, Map.of("id", "1")));
            assertEquals("unsupported_provider", e.getError().getErrorCode());
        }
    }
}
