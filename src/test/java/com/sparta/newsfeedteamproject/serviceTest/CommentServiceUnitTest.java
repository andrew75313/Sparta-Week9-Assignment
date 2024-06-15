package com.sparta.newsfeedteamproject.serviceTest;

import com.sparta.newsfeedteamproject.dto.comment.CommentReqDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import com.sparta.newsfeedteamproject.entity.Comment;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.CommentRepository;
import com.sparta.newsfeedteamproject.service.CommentService;
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
public class CommentServiceUnitTest {

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    CommentService commentService;

    String contents = "Test Contents";
    String username = "spartaclub";
    String password = "Password123!";
    String name = "Sparta Club";
    String email = "sparta@email.com";
    String userInfo = "My name is Sparta Club.";
    Status status = Status.ACTIVATE;
    Long likes = 0L;

    FeedReqDto feedReqDto;
    CommentReqDto commentReqDto;
    User user;
    Feed feed;

    @Nested
    @DisplayName("댓글 찾기")
    class FindCommentTest {

        @BeforeEach
        void beforeCommentTest() throws NoSuchFieldException, IllegalAccessException {
            user = new User(username, password, name, email, userInfo, status, LocalDateTime.now());

            feedReqDto = new FeedReqDto();
            Field feedContentsField = FeedReqDto.class.getDeclaredField("contents");
            feedContentsField.setAccessible(true);
            feedContentsField.set(feedReqDto, contents);

            Feed feed = new Feed(feedReqDto, user);

            commentReqDto = new CommentReqDto();
            Field commentContentField = CommentReqDto.class.getDeclaredField("contents");
            commentContentField.setAccessible(true);
            commentContentField.set(commentReqDto, contents);
        }

        @Test
        @DisplayName("댓글 찾기 - 성공")
        void testFindComment() {
            // given
            Comment comment = new Comment(commentReqDto, feed, user, likes);

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            // when
            Comment foundComment = commentService.findComment(comment.getId());

            // then
            assertNotNull(foundComment, "댓글이 올바르게 찾아지지 않았습니다.");
            assertEquals(user, foundComment.getUser(), "댓글을 작성한 User가 올바르지 않습니다.");
            assertEquals(feed, foundComment.getFeed(), "댓글이 작성된 Feed가 올바르지 않습니다.");
            assertEquals(contents, foundComment.getContents(), "댓글 Contents가 올바르지 않습니다.");
        }

        @Test
        @DisplayName("댓글 찾기 - 실패")
        void testFindCommentNoCommentFail() {
            // given
            Comment comment = new Comment(commentReqDto, feed, user, likes);

            given(commentRepository.findById(comment.getId())).willReturn(Optional.empty());

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentService.findComment(comment.getId()));
            assertEquals("해당 요소가 존재하지 않습니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }

    @Nested
    @DisplayName("좋아요 기능")
    class LikeTest {

        @BeforeEach
        void beforeFindLikeTest() throws NoSuchFieldException, IllegalAccessException {
            user = new User(username, password, name, email, userInfo, status, LocalDateTime.now());

            feedReqDto = new FeedReqDto();
            Field feedContentsField = FeedReqDto.class.getDeclaredField("contents");
            feedContentsField.setAccessible(true);
            feedContentsField.set(feedReqDto, contents);

            feed = new Feed(feedReqDto, user);

            commentReqDto = new CommentReqDto();
            Field commentContentField = CommentReqDto.class.getDeclaredField("contents");
            commentContentField.setAccessible(true);
            commentContentField.set(commentReqDto, contents);
        }

        @Test
        @DisplayName("댓글 좋아요 추가")
        void testIncreaseCommentLikes() {
            // given
            Comment comment = new Comment(commentReqDto, feed, user, likes);

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            // when
            commentService.increaseCommentLikes(comment.getId());

            // then
            assertEquals(1L, comment.getLikes(), "좋아요가 올바르게 추가되지 않았습니다!");
        }


        @Test
        @DisplayName("댓글 좋아요 삭제")
        void testDecreaseCommentLikes() {
            // given
            Comment comment = new Comment(commentReqDto, feed, user, likes);

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            // when
            commentService.decreaseCommentLikes(comment.getId());

            // then
            assertEquals(-1L, comment.getLikes(), "좋아요가 올바르게 삭제되지 않았습니다!");
        }
    }
}
