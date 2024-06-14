package com.sparta.newsfeedteamproject.serviceTest;

import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentReqDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentResDto;
import com.sparta.newsfeedteamproject.entity.Comment;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.CommentRepository;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import com.sparta.newsfeedteamproject.service.CommentService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
