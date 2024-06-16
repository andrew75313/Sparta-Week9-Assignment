package com.sparta.newsfeedteamproject.service;

import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedResDto;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.FeedRepository;
import com.sparta.newsfeedteamproject.repository.UserRepository;
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
        assertEquals(contents, messageResDto.getData().getContents(), "feed 내용이 올바르게 생성되지 않았습니다.");

        createdFeed = new Feed(feedReqDto, user);
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdateFeedTest {
        @Test
        @Order(2)
        @DisplayName("게시글 수정 - 성공")
        @Transactional
        void testUpdateFeed() throws NoSuchFieldException, IllegalAccessException {
            // given
            Long feedId = createdFeed.getId();
            String contents = "UPDATE Test Feed";

            FeedReqDto feedReqDto = new FeedReqDto();
            Field field = FeedReqDto.class.getDeclaredField("contents");
            field.setAccessible(true);
            field.set(feedReqDto, contents);

            user = userRepository.findById(1L).orElse(null);

            // when
            MessageResDto<FeedResDto> messageResDto = feedService.updateFeed(feedId, feedReqDto, user);

            // then
            assertEquals(contents, messageResDto.getData().getContents(), "feed 내용이 올바르게 수정되지 않았습니다.");

            feedContents = contents;
        }

        @Test
        @Order(3)
        @DisplayName("게시글 수정 - 실패")
        @Transactional
        void testUpdateFeedFail() throws NoSuchFieldException, IllegalAccessException {
            // given
            Long feedId = createdFeed.getId();
            String contents = "UPDATE Test Feed";

            FeedReqDto feedReqDto = new FeedReqDto();
            Field field = FeedReqDto.class.getDeclaredField("contents");
            field.setAccessible(true);
            field.set(feedReqDto, contents);

            user = userRepository.findById(2L).orElse(null);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> feedService.updateFeed(feedId, feedReqDto, user));
            assertEquals("해당 작업은 작성자만 수정/삭제 할 수 있습니다!", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }

    @Test
    @Order(4)
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
        assertEquals(this.feedContents, foundFeedResDto.getContents(), "feed 내용이 올바르게 조회되지 않았습니다.");

        feedId = createdFeedId;
    }

    @Test
    @Order(5)
    @DisplayName("단건 게시글 조회")
    void testGetFeed() {
        // when
        MessageResDto<FeedResDto> messageResDto = feedService.getFeed(feedId);

        // then
        FeedResDto foundFeedResDto = messageResDto.getData();

        assertEquals(this.feedContents, foundFeedResDto.getContents(), "조회할 feed가 올바르게 조회되지 않았습니다.");
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeleteFeedTest {
        @Test
        @Order(6)
        @DisplayName("게시글 삭제 - 성공")
        void deleteFeed() {
            // given
            Long feedId = createdFeed.getId();
            user = userRepository.findById(1L).orElse(null);

            // when
            MessageResDto<FeedResDto> messageResDto = feedService.deleteFeed(feedId, user);

            // then
            assertEquals("게시물 삭제가 완료되었습니다!", messageResDto.getMessage(), "feed가 올바르게 삭제되지 않았습니다.");
        }

        @Test
        @Order(7)
        @DisplayName("게시글 삭제 - 실패")
        void deleteFeedFail() {
            // given
            Long feedId = createdFeed.getId();
            user = userRepository.findById(2L).orElse(null);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> feedService.deleteFeed(feedId, user));
            assertEquals("해당 작업은 작성자만 수정/삭제 할 수 있습니다!", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }
}
