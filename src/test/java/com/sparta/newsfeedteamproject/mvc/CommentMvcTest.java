package com.sparta.newsfeedteamproject.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeedteamproject.config.SecurityConfig;
import com.sparta.newsfeedteamproject.controller.CommentController;
import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentDelResDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentReqDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentResDto;
import com.sparta.newsfeedteamproject.entity.Comment;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.security.UserDetailsImpl;
import com.sparta.newsfeedteamproject.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Field;
import java.security.Principal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {CommentController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = SecurityConfig.class
                )
        }
)
public class CommentMvcTest {
    private MockMvc mvc;

    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    CommentService commentService;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    private UserDetailsImpl mockUserSetup() {
        String username = "spartaclub";
        String password = "Password123!";
        String name = "Sparta Club";
        String email = "sparta@email.com";
        String userInfo = "My name is Sparta Club.";
        Status status = Status.ACTIVATE;
        User testUser = new User(username, password, name, email, userInfo, status, LocalDateTime.now());
        UserDetailsImpl testUserDetails = new UserDetailsImpl(testUser);
        mockPrincipal = new UsernamePasswordAuthenticationToken(testUserDetails, "", testUserDetails.getAuthorities());

        return testUserDetails;
    }

    private Feed mockFeedSetup(Long feedId) throws NoSuchFieldException, IllegalAccessException {
        String contents = "Test";
        Long likes = 0L;
        Feed feed = new Feed();

        Field idField = Feed.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(feed, feedId);

        User user = mockUserSetup().getUser();

        Field userField = Feed.class.getDeclaredField("user");
        userField.setAccessible(true);
        userField.set(feed, user);

        Field contentsField = Feed.class.getDeclaredField("contents");
        contentsField.setAccessible(true);
        contentsField.set(feed, contents);

        Field likesField = Feed.class.getDeclaredField("likes");
        likesField.setAccessible(true);
        likesField.set(feed, likes);

        return feed;
    }

    private CommentReqDto mockCommentReqDtoSetup() throws NoSuchFieldException, IllegalAccessException {
        String contents = "Test";
        CommentReqDto commentReqDto = new CommentReqDto();

        Field contentsField = CommentReqDto.class.getDeclaredField("contents");
        contentsField.setAccessible(true);
        contentsField.set(commentReqDto, contents);

        return commentReqDto;
    }

    private CommentResDto mockCommentResDtoSetup(Long feedId, Long commentId) throws NoSuchFieldException, IllegalAccessException {
        String contents = "Test";
        Long likes = 0L;
        Comment comment = new Comment();
        Feed feed = mockFeedSetup(feedId);

        Field idField = Comment.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(comment, commentId);

        User user = mockUserSetup().getUser();

        Field userField = Comment.class.getDeclaredField("user");
        userField.setAccessible(true);
        userField.set(comment, user);

        Field contentsField = Comment.class.getDeclaredField("contents");
        contentsField.setAccessible(true);
        contentsField.set(comment, contents);

        Field likesField = Comment.class.getDeclaredField("likes");
        likesField.setAccessible(true);
        likesField.set(comment, likes);

        Field feedField = Comment.class.getDeclaredField("feed");
        feedField.setAccessible(true);
        feedField.set(comment, feed);

        CommentResDto commentResDto = new CommentResDto(comment);

        return commentResDto;
    }

    @Test
    @DisplayName("댓글 생성")
    void testCreateComment() throws Exception {
        // given
        Long feedId = 1L;
        CommentReqDto commentReqDto = mockCommentReqDtoSetup();
        User user = mockUserSetup().getUser();
        Feed feed = mockFeedSetup(feedId);
        Comment comment = new Comment(commentReqDto, feed, user, 0L);
        CommentResDto commentResDto = new CommentResDto(comment);
        MessageResDto<CommentResDto> response = new MessageResDto<>(200, "댓글 작성이 완료되었습니다!", commentResDto);

        String postInfo = objectMapper.writeValueAsString(commentReqDto);

        // when
        given(commentService.createComment(anyLong(), any(CommentReqDto.class), any(User.class))).willReturn(response);

        // then
        mvc.perform(post("/feeds/{feedId}/comments", feedId)
                        .content(postInfo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 단건 조회")
    void testGetComment() throws Exception {
        // given
        Long feedId = 1L;
        Long commentId = 1L;
        CommentResDto commentResDto = mockCommentResDtoSetup(feedId, commentId);
        MessageResDto<CommentResDto> response = new MessageResDto<>(200, "댓글 조회가 완료되었습니다!", commentResDto);

        // when
        given(commentService.getComment(anyLong(), anyLong())).willReturn(response);

        // then
        mvc.perform(get("/feeds/{feedId}/comments/{commentId}", feedId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.contents").value("Test"))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 수정")
    void testUpdateComment() throws Exception {
        // given
        Long feedId = 1L;
        CommentReqDto commentReqDto = mockCommentReqDtoSetup();
        User user = mockUserSetup().getUser();
        Feed feed = mockFeedSetup(feedId);
        Comment comment = new Comment(commentReqDto, feed, user, 0L);
        Long commentId = comment.getId();
        CommentResDto commentResDto = new CommentResDto(comment);
        MessageResDto<CommentResDto> response = new MessageResDto<>(200, "댓글 수정이 완료되었습니다!", commentResDto);

        String putInfo = objectMapper.writeValueAsString(commentReqDto);

        // when
        given(commentService.updateComment(feedId, commentId, commentReqDto, user)).willReturn(response);

        // then
        mvc.perform(put("/{feedId}/comments/{commentId}", feedId, commentId)
                        .content(putInfo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 삭제")
    void testDeleteComment() throws Exception {
        // given
        Long feedId = 1L;
        Long commentId = 1L;
        User user = mockUserSetup().getUser();
        CommentDelResDto commentDelResDto = new CommentDelResDto(commentId);
        MessageResDto<CommentDelResDto> response = new MessageResDto<>(200, "댓글 삭제가 완료되었습니다!", commentDelResDto);

        // when
        given(commentService.deleteComment(feedId, commentId, user)).willReturn(response);

        // then
        mvc.perform(delete("/{feedId}/comments/{commentId}", feedId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }
}
