package com.sparta.newsfeedteamproject.serviceTest;

import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.FeedRepository;
import com.sparta.newsfeedteamproject.service.FeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class FeedServiceUnitTest {

    @Mock
    FeedRepository feedRepository;

    @InjectMocks
    FeedService feedService;

    @Nested
    @DisplayName("게시글 찾기")
    class FindFeedTest {

        String contents = "Test Feed";
        String username = "spartaclub";
        String password = "Password123!";
        String name = "Sparta Club";
        String email = "sparta@email.com";
        String userInfo = "My name is Sparta Club.";
        Status status = Status.ACTIVATE;

        FeedReqDto feedReqDto;
        User user;

        @BeforeEach
        void beforeFindFeedTest() throws NoSuchFieldException, IllegalAccessException {
            feedReqDto = new FeedReqDto();
            Field field = FeedReqDto.class.getDeclaredField("contents");
            field.setAccessible(true);
            field.set(feedReqDto, contents);

            user = new User(username, password, name, email, userInfo, status, LocalDateTime.now());
        }

        @Test
        @DisplayName("게시글 찾기 - 성공")
        void testFindFeed() {
            // given
            Feed feed = new Feed(feedReqDto, user);

            given(feedRepository.findById(feed.getId())).willReturn(Optional.of(feed));

            // when
            Feed foundFeed = feedService.findFeed(feed.getId());

            // then
            assertNotNull(foundFeed, "게시글을 올바르게 찾아지지 않았습니다.");
            assertEquals(user, foundFeed.getUser(), "게시글을 작성한 User가 올바르지 않습니다.");
            assertEquals(contents, foundFeed.getContents(), "게시글 Contents가 올바르지 않습니다.");
        }

        @Test
        @DisplayName("게시글 찾기 - 실패")
        void testFindFeedNoFeedFail() {
            // given
            Feed feed = new Feed(feedReqDto, user);

            given(feedRepository.findById(feed.getId())).willReturn(Optional.empty());

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> feedService.findFeed(feed.getId()));
            assertEquals("해당 요소가 존재하지 않습니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }

    }
}
