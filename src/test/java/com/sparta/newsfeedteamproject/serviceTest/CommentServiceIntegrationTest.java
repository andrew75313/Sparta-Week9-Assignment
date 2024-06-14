package com.sparta.newsfeedteamproject.serviceTest;

import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentReqDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentResDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedResDto;
import com.sparta.newsfeedteamproject.entity.Comment;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.CommentRepository;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import com.sparta.newsfeedteamproject.service.CommentService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentServiceIntegrationTest {

    @Autowired
    CommentService commentService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CommentRepository commentRepository;

    User user;
    Feed feed;
    Comment createdComment = null;
    String commentContents = "";

    @Test
    @Order(1)
    @DisplayName("댓글 등록")
    void testCreatedComment() throws NoSuchFieldException, IllegalAccessException {
        // given
        String contents = "Test Coomment";

        CommentReqDto commentReqDto = new CommentReqDto();
        Field field = CommentReqDto.class.getDeclaredField("contents");
        field.setAccessible(true);
        field.set(commentReqDto, contents);

        user = userRepository.findById(1L).orElse(null);


        // when
        MessageResDto<CommentResDto> messageResDto = commentService.createComment(1L, commentReqDto, user);

        // then
        assertEquals(contents, messageResDto.getData(), "comment 내용이 올바르게 생성되지 않았습니다.");

        createdComment = new Comment(commentReqDto, feed, user, 0L);
    }

    @Nested
    @DisplayName("댓글 찾기 기능")
    class FindFeedTest {
        @Test
        @Order(2)
        @DisplayName("댓글 찾기 기능 - 성공")
        void testFindComment() {
            // given
            Long commentId = createdComment.getId();

            // when
            Comment comment = commentService.findComment(commentId);

            // then
            assertEquals(commentId, comment.getId(), "댓글을 올바르게 찾을 수 없습니다.");
        }

        @Test
        @Order(3)
        @DisplayName("댓글 찾기 기능 - 실패")
        void testFindCommentFail() {
            // given
            Long commentId = 10000000L;

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentService.findComment(commentId));
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
        void testIncreasCommentLike() {
            // given
            Long commentId = createdComment.getId();

            // when
            commentService.increaseCommentLikes(commentId);

            // then
            assertEquals(1L, createdComment.getLikes(), "좋아요가 올바르게 추가되지 않았습니다.");
        }

        @Test
        @Order(5)
        @DisplayName("좋아요 삭제")
        @Transactional
        void testDecreaseFeedLike() {
            // given
            Long commentId = createdComment.getId();

            // when
            commentService.decreaseCommentLikes(commentId);

            // then
            assertEquals(0L, createdComment.getLikes(), "좋아요가 올바르게 삭제되지 않았습니다.");
        }
    }

    @Test
    @Order(6)
    @DisplayName("댓글 수정")
    @Transactional
    void testUpdateComment() throws NoSuchFieldException, IllegalAccessException {
        // given
        Long commentId = this.createdComment.getId();
        String contents = "UPDATE Test Comment";

        CommentReqDto commentReqDto = new CommentReqDto();
        Field field = CommentReqDto.class.getDeclaredField("contents");
        field.setAccessible(true);
        field.set(commentReqDto, contents);

        user = userRepository.findById(1L).orElse(null);

        // when
        MessageResDto<CommentResDto> messageResDto = commentService.updateComment(1L, commentId, commentReqDto, user);

        // then
        assertEquals(contents, messageResDto.getData(), "댓글 내용이 올바르게 수정되지 않았습니다.");

        this.commentContents = contents;
    }

    @Test
    @Order(7)
    @DisplayName("단건 댓글 조회")
    void testGetComment() {
        // given
        Long commentId = this.createdComment.getId();
        Long feedId = this.createdComment.getFeed().getId();

        // when
        MessageResDto<CommentResDto> messageResDto = commentService.getComment(feedId, commentId);

        // then
        CommentResDto foundCommentResDto = messageResDto.getData();

        assertEquals(this.commentContents, foundCommentResDto.getContents(), "조회할 feed가 올바르게 조회되지 않았습니다.");
    }
}
