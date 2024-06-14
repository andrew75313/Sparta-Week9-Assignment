package com.sparta.newsfeedteamproject.serviceTest;

import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedResDto;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.FeedRepository;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import com.sparta.newsfeedteamproject.service.FeedService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeedServiceIntegrationTest {

    @Autowired
    FeedService feedService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FeedRepository feedRepository;

    User user;
    Feed createdFeed = null;
    String feedContents = "";
    Long feedId;


    @Test
    @Order(1)
    @DisplayName("게시글 등록")
    void testCreatedFeed() throws NoSuchFieldException, IllegalAccessException {
        // given
        String contents = "Test Feed";

        FeedReqDto feedReqDto = new FeedReqDto();
        Field field = FeedReqDto.class.getDeclaredField("contents");
        field.setAccessible(true);
        field.set(feedReqDto, contents);

        user = userRepository.findById(1L).orElse(null);

        // when
        MessageResDto<FeedResDto> messageResDto = feedService.createFeed(feedReqDto, user);

        // then
        assertEquals(contents, messageResDto.getData(), "feed 내용이 올바르게 생성되지 않았습니다.");

        createdFeed = new Feed(feedReqDto, user);
    }

    @Nested
    @DisplayName("게시글 찾기 기능")
    class FindFeedTest {
        @Test
        @Order(2)
        @DisplayName("게시글 찾기 기능 - 성공")
        void testFindFeed() {
            // given
            Long feedId = createdFeed.getId();

            // when
            Feed feed = feedService.findFeed(feedId);

            // then
            assertEquals(feedId, feed.getId(), "게시글을 올바르게 찾을 수 없습니다.");
        }

        @Test
        @Order(3)
        @DisplayName("게시글 찾기 기능 - 실패")
        void testFindFeedFail() {
            // given
            Long feedId = 10000000L;

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> feedService.findFeed(feedId));
            assertEquals("해당 요소가 존재하지 않습니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }

    @Nested
    @DisplayName("좋아요 기능")
    class LikeTest {

        @Test
        @Order(4)
        @DisplayName("좋아요 추가")
        @Transactional
        void testIncreasFeedLike() {
            // given
            Long feedId = createdFeed.getId();

            // when
            feedService.increaseFeedLikes(feedId);

            // then
            assertEquals(1L, createdFeed.getLikes(), "좋아요가 올바르게 추가되지 않았습니다.");
        }

        @Test
        @Order(5)
        @DisplayName("좋아요 삭제")
        @Transactional
        void testDecreaseFeedLike() {
            // given
            Long feedId = createdFeed.getId();

            // when
            feedService.decreaseFeedLikes(feedId);

            // then
            assertEquals(0L, createdFeed.getLikes(), "좋아요가 올바르게 삭제되지 않았습니다.");
        }
    }

    @Test
    @Order(6)
    @DisplayName("게시글 수정")
    @Transactional
    void testUpdateFeed() throws NoSuchFieldException, IllegalAccessException {
        // given
        Long feedId = this.createdFeed.getId();
        String contents = "UPDATE Test Feed";

        FeedReqDto feedReqDto = new FeedReqDto();
        Field field = FeedReqDto.class.getDeclaredField("contents");
        field.setAccessible(true);
        field.set(feedReqDto, contents);

        user = userRepository.findById(1L).orElse(null);

        // when
        MessageResDto<FeedResDto> messageResDto = feedService.updateFeed(feedId, feedReqDto, user);

        // then
        assertEquals(contents, messageResDto.getData(), "feed 내용이 올바르게 수정되지 않았습니다.");

        this.feedContents = contents;
    }

    @Test
    @Order(7)
    @DisplayName("모든 게시글 조회")
    void testGetAllFeeds() {
        // given
        user = userRepository.findById(1L).orElse(null);

        // when
        MessageResDto<List<FeedResDto>> messageResDto = feedService.getAllFeeds(0, "createdAt", null, null);

        // then
        Long createdFeedId = this.createdFeed.getId();

        FeedResDto foundFeedResDto = messageResDto.getData().stream()
                .filter(feed -> feed.getId().equals(createdFeed))
                .findFirst()
                .orElse(null);

        assertNotNull(foundFeedResDto, "feed가 올바르게 조회되지 않았습니다.");
        assertEquals(createdFeedId, foundFeedResDto.getId(), "feed Id가 올바르게 조회되지 않았습니다.");
        assertEquals(this.createdFeed.getContents(), foundFeedResDto.getContents(), "feed 내용이 올바르게 조회되지 않았습니다.");

        feedId = createdFeedId;
    }

    @Test
    @Order(8)
    @DisplayName("단건 게시글 조회")
    void testGetFeed() {
        // when
        MessageResDto<FeedResDto> messageResDto = feedService.getFeed(feedId);

        // then
        FeedResDto foundFeedResDto = messageResDto.getData();

        assertEquals(this.feedContents, foundFeedResDto.getContents(), "조회할 feed가 올바르게 조회되지 않았습니다.");
    }

    @Test
    @Order(9)
    @DisplayName("게시글 삭제")
    void deleteFeed() {
        // given
        user = userRepository.findById(1L).orElse(null);

        // when
        MessageResDto<FeedResDto> messageResDto = feedService.deleteFeed(feedId, user);

        // then
        assertEquals("게시물 삭제가 완료되었습니다!", messageResDto.getMessage(), "feed가 올바르게 삭제되지 않았습니다.");
    }
}
