package com.sparta.newsfeedteamproject.entity;

import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class FeedTest {

    Feed feed;
    User user;
    FeedReqDto feedReqDto;

    @BeforeEach
    void setUp() {
        // Mock 객체들 초기화
        MockitoAnnotations.openMocks(this);

        // 임의의 User 객체
        user = new User("Sparta",
                "Password123!",
                "Sparta Club",
                "spartaclub@example.com",
                "My name is Sparta.",
                Status.ACTIVATE,
                LocalDateTime.now());

        // 임의의 FeedReqDto 객체
        feedReqDto = setFeedReqDto("게시글 테스트입니다.");

        // 임의의 Feed 객체
        feed = new Feed(feedReqDto, user);
    }

    private FeedReqDto setFeedReqDto(String contents) {

        FeedReqDto feedReqDto = new FeedReqDto();
        ReflectionTestUtils.setField(feedReqDto,"contents", contents);

        return feedReqDto;
    }

    // 생성자 테스트
    @Test
    @DisplayName("생성자 테스트")
    void testFeedConstrcutor() {
        // when - then
        assertEquals("게시글 테스트입니다.", feed.getContents(), "게시글이 올바르게 설정되지 않았습니다.");
        assertEquals(user, feed.getUser(), "User가 올바르게 설정되지 않았습니다.");
        assertEquals(0L, feed.getLikes(), "Likes가 올바르게 설정되지 않았습니다.");
    }

    // update 테스트
    @Test
    @DisplayName("update 테스트")
    void testUpdate() {
        // given
        String updateContents = "게시글 Update 테스트 입니다.";
        FeedReqDto updateFeedReqDto = setFeedReqDto(updateContents);

        // when
        feed.update(updateFeedReqDto);

        // then
        assertEquals(updateContents, feed.getContents(), "게시글이 업데이트되지 않았습니다.");
    }

    // like 기능 테스트
    @Nested
    @DisplayName("좋아요 기능 테스트")
    class TestLike {

        @Test
        @DisplayName("좋아요 추가 테스트")
        void testIncreaseLikes() {
            // when
            feed.increaseLikes();

            // then
            assertEquals(1, feed.getLikes(), "좋아요가 추가되지 않았습니다.");
        }

        @Test
        @DisplayName("좋아요 삭제 테스트")
        void testDecreaseLikes() {
            // when
            feed.decreaseLikes();

            // then
            assertEquals(-1, feed.getLikes(), "좋아요가 삭제되지 않았습니다.");
        }
    }
}
