package com.sparta.newsfeedteamproject.service;

import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentDelResDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentReqDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentResDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import com.sparta.newsfeedteamproject.entity.Comment;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.CommentRepository;
import com.sparta.newsfeedteamproject.repository.FeedRepository;
import com.sparta.newsfeedteamproject.repository.LikeRepository;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

        FeedReqDto feedReqDto = new FeedReqDto();
        ReflectionTestUtils.setField(feedReqDto, "contents", "Test Feed");

        feed = new Feed(feedReqDto, user);

        feedRepository.save(feed);
    }

    @AfterEach
    void tearDown() {
        userRepository.delete(user);
        feedRepository.delete(feed);
    }

    private Comment setComment(String contents) {
        CommentReqDto commentReqDto = new CommentReqDto();
        
        ReflectionTestUtils.setField(commentReqDto, "contents", contents);

        Comment comment = new Comment(commentReqDto, feed, user, 0L);

        return comment;
    }

    @Test
    @DisplayName("댓글 등록")
    @Transactional
    void testCreatedComment() {
        // given
        String contents = "Test Comment";

        CommentReqDto commentReqDto = new CommentReqDto();
        ReflectionTestUtils.setField(commentReqDto, "contents", contents);
        
        // when
        MessageResDto<CommentResDto> messageResDto = commentService.createComment(feed.getId(), commentReqDto, user);

        // then
        assertEquals(contents, messageResDto.getData().getContents(), "comment 내용이 올바르게 생성되지 않았습니다.");
    }


    @Nested
    @DisplayName("댓글 수정")
    class UpdateCommentTest {
        @Test
        @DisplayName("댓글 수정 - 성공")
        @Transactional
        void testUpdateComment() {
            // given
            Comment comment = setComment("Test Comment");
            commentRepository.save(comment);

            String contents = "UPDATE Test Feed";

            CommentReqDto commentReqDto = new CommentReqDto();
            ReflectionTestUtils.setField(commentReqDto, "contents", contents);

            // when
            MessageResDto<CommentResDto> messageResDto = commentService.updateComment(feed.getId(), comment.getId(), commentReqDto, user);

            // then
            assertEquals(contents, messageResDto.getData().getContents(), "댓글 내용이 올바르게 수정되지 않았습니다.");
        }

        @Test
        @DisplayName("댓글 수정 - 실패")
        @Transactional
        void testUpdateCommentFail() {
            // given
            Comment comment = setComment("Test Comment");
            commentRepository.save(comment);

            String contents = "UPDATE Test Feed";

            CommentReqDto commentReqDto = new CommentReqDto();
            ReflectionTestUtils.setField(commentReqDto, "contents", contents);

            User differentUser = new User();
            differentUser.setUsername("spartaclub2");

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentService.updateComment(feed.getId(), comment.getId(), commentReqDto, differentUser));
            assertEquals("해당 작업은 작성자만 수정/삭제 할 수 있습니다!", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }

    @Test
    @DisplayName("단건 댓글 조회")
    @Transactional
    void testGetComment() {
        // given
        String contents = "Test Comment";
        Comment testComment = setComment(contents);
        commentRepository.save(testComment);

        // when
        MessageResDto<CommentResDto> messageResDto = commentService.getComment(feed.getId(), testComment.getId());

        // then
        CommentResDto foundCommentResDto = messageResDto.getData();

        assertEquals(contents, foundCommentResDto.getContents(), "조회할 feed가 올바르게 조회되지 않았습니다.");
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteCommentTest {
        @Test
        @DisplayName("댓글 삭제 - 성공")
        @Transactional
        void testdeleteComment() {
            // given
            Comment testComment = setComment("Test Comment");
            commentRepository.save(testComment);

            // when
            MessageResDto<CommentDelResDto> messageResDto = commentService.deleteComment(feed.getId(), testComment.getId(), user);

            // then
            assertEquals("댓글 삭제가 완료되었습니다!", messageResDto.getMessage(), "댓글이 올바르게 삭제되지 않았습니다.");
        }

        @Test
        @DisplayName("댓글 삭제 - 실패")
        @Transactional
        void testdeleteCommentFail() {
            // given
            Comment testComment = setComment("Test Comment");
            commentRepository.save(testComment);;

            User differentUser = new User();
            differentUser.setUsername("spartaclub2");

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentService.deleteComment(feed.getId(), testComment.getId(), differentUser));
            assertEquals("해당 작업은 작성자만 수정/삭제 할 수 있습니다!", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }
}
