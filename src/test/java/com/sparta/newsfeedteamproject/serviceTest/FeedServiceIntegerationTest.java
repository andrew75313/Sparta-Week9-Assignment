package com.sparta.newsfeedteamproject.serviceTest;

import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedResDto;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import com.sparta.newsfeedteamproject.service.FeedService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeedServiceIntegerationTest {

    @Autowired
    FeedService feedService;
    @Autowired
    UserRepository userRepository;

    User user;
    Feed createdFeed = null;
    String feedContents = "";

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
}
