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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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

    private Feed mockFeedSetup(Long feedId) {
        String contents = "Test";
        Long likes = 0L;
        Feed feed = new Feed();

        User user = mockUserSetup().getUser();

        ReflectionTestUtils.setField(feed, "id", feedId);
        ReflectionTestUtils.setField(feed, "user", user);
        ReflectionTestUtils.setField(feed, "contents", contents);
        ReflectionTestUtils.setField(feed, "likes", likes);

        return feed;
    }

    private CommentReqDto mockCommentReqDtoSetup() {
        String contents = "Test";
        CommentReqDto commentReqDto = new CommentReqDto();

        ReflectionTestUtils.setField(commentReqDto, "contents", contents);


        return commentReqDto;
    }

    private CommentResDto mockCommentResDtoSetup(Long feedId, Long commentId) {
        String contents = "Test";
        Long likes = 0L;

        Comment comment = new Comment();

        Feed feed = mockFeedSetup(feedId);

        User user = mockUserSetup().getUser();

        ReflectionTestUtils.setField(comment, "id", commentId);
        ReflectionTestUtils.setField(comment, "user", user);
        ReflectionTestUtils.setField(comment, "contents", contents);
        ReflectionTestUtils.setField(comment, "likes", likes);
        ReflectionTestUtils.setField(comment, "feed", feed);

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
        Long commentId = 1L;
        CommentReqDto commentReqDto = mockCommentReqDtoSetup();

        Feed feed = mockFeedSetup(feedId);

        User user = mockUserSetup().getUser();

        Comment comment = new Comment(commentReqDto, feed, user, 0L);
        ReflectionTestUtils.setField(comment, "id", commentId);

        CommentResDto commentResDto = new CommentResDto(comment);
        MessageResDto<CommentResDto> response = new MessageResDto<>(200, "댓글 수정이 완료되었습니다!", commentResDto);

        String putInfo = objectMapper.writeValueAsString(commentReqDto);

        // when
        given(commentService.updateComment(anyLong(), anyLong(), any(CommentReqDto.class), any(User.class))).willReturn(response);

        // then
        mvc.perform(put("/feeds/{feedId}/comments/{commentId}", feedId, commentId)
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
        given(commentService.deleteComment(anyLong(), anyLong(), any(User.class))).willReturn(response);

        // then
        mvc.perform(delete("/feeds/{feedId}/comments/{commentId}", feedId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }
}
