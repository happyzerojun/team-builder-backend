package com.capstone.backend.controller;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Test
    void getProjectMyReviewsUsesJwtIdentity() {
        ReviewController controller = new ReviewController(reviewService);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "reviewer@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        var response = controller.getProjectMyReviews(3L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewService).getMyReviewsForProject(3L, "reviewer@example.com", null);
    }

    @Test
    void saveBulkReviewsUsesJwtIdentity() {
        ReviewController controller = new ReviewController(reviewService);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "reviewer@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        ReviewBulkSaveRequestDto request = new ReviewBulkSaveRequestDto();
        ReflectionTestUtils.setField(request, "projectId", 10L);
        ReflectionTestUtils.setField(request, "revieweeId", 2L);
        ReflectionTestUtils.setField(request, "rating", 5);
        ReflectionTestUtils.setField(request, "comment", "좋아요");

        var response = controller.saveProjectReviews(List.of(request), authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Reviews saved successfully", response.getBody());
        verify(reviewService).saveBulkReviews(eq(List.of(request)), eq("reviewer@example.com"));
    }

    @Test
    void receivedReviewsRequiresAuthentication() {
        ReviewController controller = new ReviewController(reviewService);

        assertThrows(UnauthorizedException.class, () -> controller.getMyReceivedReviews(null));
    }
}
