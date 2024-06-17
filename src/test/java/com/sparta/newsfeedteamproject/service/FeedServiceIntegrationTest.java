package com.sparta.newsfeedteamproject.service;

import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedResDto;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.FeedRepository;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @BeforeEach
    void setUp() {
        user = new User("spartaclub",
                "Password123!",
                "Sparta Club",
                "sparta@email.com",
                "My name is Sparta Club.",
                Status.ACTIVATE,
                LocalDateTime.now());

        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.delete(user);
    }

    private Feed setFeed(String contents) {
        FeedReqDto feedReqDto = new FeedReqDto();

        ReflectionTestUtils.setField(feedReqDto, "contents", contents);

        Feed feed = new Feed(feedReqDto, user);

        return feed;
    }


    @Test
    @Transactional
    @DisplayName("게시글 등록")
    void testCreatedFeed() {
        // given
        String contents = "Test Feed";

        FeedReqDto feedReqDto = new FeedReqDto();
        ReflectionTestUtils.setField(feedReqDto, "contents", contents);

        // when
        MessageResDto<FeedResDto> messageResDto = feedService.createFeed(feedReqDto, user);

        // then
        assertEquals(contents, messageResDto.getData().getContents(), "feed 내용이 올바르게 생성되지 않았습니다.");
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdateFeedTest {

        @Test
        @DisplayName("게시글 수정 - 성공")
        @Transactional
        void testUpdateFeed() {
            // given
            Feed feed = setFeed("Test Feed");
            feedRepository.save(feed);

            String contents = "UPDATE Test Feed";

            FeedReqDto feedReqDto = new FeedReqDto();
            ReflectionTestUtils.setField(feedReqDto, "contents", contents);

            // when
            MessageResDto<FeedResDto> messageResDto = feedService.updateFeed(feed.getId(), feedReqDto, user);

            // then
            assertEquals(contents, messageResDto.getData().getContents(), "feed 내용이 올바르게 수정되지 않았습니다.");
        }

        @Test
        @DisplayName("게시글 수정 - 실패")
        @Transactional
        void testUpdateFeedFail() {
            // given
            Feed feed = setFeed("Test Feed");
            feedRepository.save(feed);

            String contents = "UPDATE Test Feed";

            FeedReqDto feedReqDto = new FeedReqDto();
            ReflectionTestUtils.setField(feedReqDto, "contents", contents);

            User differentUser = new User();
            differentUser.setId(user.getId() + 1L);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> feedService.updateFeed(feed.getId(), feedReqDto, differentUser));
            assertEquals("해당 작업은 작성자만 수정/삭제 할 수 있습니다!", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }

    @Test
    @DisplayName("모든 게시글 조회")
    @Transactional
    void testGetAllFeeds() {
        // given
        Feed firstTestFeed = setFeed("Test Feed 1");
        feedRepository.save(firstTestFeed);

        Feed secondTestFeed = setFeed("Test Feed 2");
        feedRepository.save(secondTestFeed);

        // when
        MessageResDto<List<FeedResDto>> messageResDto = feedService.getAllFeeds(0, "createdAt", null, null);

        // then
        List<FeedResDto> foundFeedResDtoList = messageResDto.getData().stream().collect(Collectors.toList());

        assertNotNull(foundFeedResDtoList, "모든 feed가 올바르게 조회되지 않았습니다.");
        assertEquals(firstTestFeed.getId(), foundFeedResDtoList.get(1).getId(), "feed Id가 올바르게 조회되지 않았습니다.");
        assertEquals(secondTestFeed.getId(), foundFeedResDtoList.get(0).getId(), "feed Id가 올바르게 조회되지 않았습니다.");
        assertEquals(firstTestFeed.getContents(), foundFeedResDtoList.get(1).getContents(), "feed 내용이 올바르게 조회되지 않았습니다.");
        assertEquals(secondTestFeed.getContents(), foundFeedResDtoList.get(0).getContents(), "feed 내용이 올바르게 조회되지 않았습니다.");
    }

    @Test
    @DisplayName("단건 게시글 조회")
    @Transactional
    void testGetFeed() {
        // given
        String contents = "Test Feed";
        Feed testFeed = setFeed(contents);
        feedRepository.save(testFeed);

        // when
        MessageResDto<FeedResDto> messageResDto = feedService.getFeed(testFeed.getId());

        // then
        FeedResDto foundFeedResDto = messageResDto.getData();

        assertEquals(contents, foundFeedResDto.getContents(), "조회할 feed가 올바르게 조회되지 않았습니다.");
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeleteFeedTest {
        @Test
        @DisplayName("게시글 삭제 - 성공")
        @Transactional
        void deleteFeed() {
            // given
            Feed testFeed = setFeed("Test Feed");
            feedRepository.save(testFeed);

            // when
            MessageResDto<FeedResDto> messageResDto = feedService.deleteFeed(testFeed.getId(), user);

            // then
            assertEquals("게시물 삭제가 완료되었습니다!", messageResDto.getMessage(), "feed가 올바르게 삭제되지 않았습니다.");
        }

        @Test
        @DisplayName("게시글 삭제 - 실패")
        @Transactional
        void deleteFeedFail() {
            // given
            Feed testFeed = setFeed("Test Feed");
            feedRepository.save(testFeed);

            User differentUser = new User();
            differentUser.setId(user.getId() + 1L);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> feedService.deleteFeed(testFeed.getId(), differentUser));
            assertEquals("해당 작업은 작성자만 수정/삭제 할 수 있습니다!", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }
}
