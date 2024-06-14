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

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeedServiceIntegerationTest {

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

    @Test
    @Order(2)
    @DisplayName("게시글 수정")
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
    @Order(3)
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
    @Order(4)
    @DisplayName("단건 게시글 조회")
    void testGetFeed() {
        // when
        MessageResDto<FeedResDto> messageResDto = feedService.getFeed(feedId);

        // then
        FeedResDto foundFeedResDto = messageResDto.getData();

        assertEquals(this.feedContents, foundFeedResDto.getContents(), "조회할 feed가 올바르게 조회되지 않았습니다.");
    }

    @Test
    @Order(5)
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
