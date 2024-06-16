package com.sparta.newsfeedteamproject.service;

import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentDelResDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentReqDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentResDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import com.sparta.newsfeedteamproject.entity.*;
import com.sparta.newsfeedteamproject.repository.CommentRepository;
import com.sparta.newsfeedteamproject.repository.FeedRepository;
import com.sparta.newsfeedteamproject.repository.LikeRepository;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

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
    FeedRepository feedRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    LikeRepository likeRepository;

    User user;
    Feed feed;
    Comment createdComment = null;
    String commentContents = "";

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        user = new User("spartaclub",
                "Password123!",
                "Sparta Club",
                "sparta@email.com",
                "My name is Sparta Club.",
                Status.ACTIVATE,
                LocalDateTime.now());

        userRepository.save(user);

        FeedReqDto feedReqDto = new FeedReqDto();
        Field contentsField = FeedReqDto.class.getDeclaredField("contents");
        contentsField.setAccessible(true);
        contentsField.set(feedReqDto, "Test Feed");

        feed = new Feed(feedReqDto, user);

        feedRepository.save(feed);
    }

    @AfterEach
    void tearDown() {
        userRepository.delete(user);
        feedRepository.delete(feed);
    }

    private Comment setComment(String contents) throws NoSuchFieldException, IllegalAccessException {
        CommentReqDto commentReqDto = new CommentReqDto();

        Field contentsField = CommentReqDto.class.getDeclaredField("contents");
        contentsField.setAccessible(true);
        contentsField.set(commentReqDto, contents);

        Comment comment = new Comment(commentReqDto, feed, user, 0L);

        return comment;
    }

    @Test
    @DisplayName("댓글 등록")
    @Transactional
    void testCreatedComment() throws NoSuchFieldException, IllegalAccessException {
        // given
        String contents = "Test Comment";

        CommentReqDto commentReqDto = new CommentReqDto();
        Field contentsField = CommentReqDto.class.getDeclaredField("contents");
        contentsField.setAccessible(true);
        contentsField.set(commentReqDto, contents);

        // when
        MessageResDto<CommentResDto> messageResDto = commentService.createComment(feed.getId(), commentReqDto, user);

        // then
        assertEquals(contents, messageResDto.getData().getContents(), "comment 내용이 올바르게 생성되지 않았습니다.");
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

    @Nested
    @DisplayName("댓글 수정")
    class UpdateCommentTest {
        @Test
        @Order(6)
        @DisplayName("댓글 수정 - 성공")
        @Transactional
        void testUpdateComment() throws NoSuchFieldException, IllegalAccessException {
            // given
            Long commentId = createdComment.getId();
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

            commentContents = contents;
        }

        @Test
        @Order(6)
        @DisplayName("댓글 수정 - 실패")
        @Transactional
        void testUpdateCommentFail() throws NoSuchFieldException, IllegalAccessException {
            // given
            Long commentId = createdComment.getId();
            String contents = "UPDATE Test Comment";

            CommentReqDto commentReqDto = new CommentReqDto();
            Field field = CommentReqDto.class.getDeclaredField("contents");
            field.setAccessible(true);
            field.set(commentReqDto, contents);

            user = userRepository.findById(2L).orElse(null);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentService.updateComment(1L, commentId, commentReqDto, user));
            assertEquals("해당 작업은 작성자만 수정/삭제 할 수 있습니다!", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");

        }
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

    @Test
    @Order(8)
    @DisplayName("댓글의 좋아요 전체 삭제")
    void testDeleteLikes() {
        // given
        Long commentId = createdComment.getId();
        Long feedId = createdComment.getFeed().getId();
        user = userRepository.findById(1L).orElse(null);
        Like like = new Like(user, commentId, Contents.COMMENT);

        likeRepository.save(like);

        // when
        commentService.deleteComment(feedId, commentId, user);

        // then
        List<Like> foundLikeList = likeRepository.findAllByContentsIdAndContents(commentId, Contents.COMMENT).orElse(null);
        assertEquals(0, foundLikeList.size(), "좋아요가 올바르게 삭제되지 않았습니다.");
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteCommentTest {
        @Test
        @Order(10)
        @DisplayName("댓글 삭제 - 성공")
        void testdeleteComment() {
            // given
            Long commentId = createdComment.getId();
            Long feedId = createdComment.getFeed().getId();
            user = userRepository.findById(1L).orElse(null);

            // when
            MessageResDto<CommentDelResDto> messageResDto = commentService.deleteComment(feedId, commentId, user);

            // then
            assertEquals("댓글 삭제가 완료되었습니다!", messageResDto.getMessage(), "댓글이 올바르게 삭제되지 않았습니다.");
        }

        @Test
        @Order(11)
        @DisplayName("댓글 삭제 - 실패")
        void testdeleteFeedFail() {
            // given
            Long commentId = createdComment.getId();
            Long feedId = createdComment.getFeed().getId();
            user = userRepository.findById(1L).orElse(null);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentService.deleteComment(feedId, commentId, user));
            assertEquals("해당 작업은 작성자만 수정/삭제 할 수 있습니다!", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }
}
