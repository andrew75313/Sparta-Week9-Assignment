package com.sparta.newsfeedteamproject.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeedteamproject.config.SecurityConfig;
import com.sparta.newsfeedteamproject.controller.CommentController;
import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentReqDto;
import com.sparta.newsfeedteamproject.dto.comment.CommentResDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

        Feed feed = new Feed();

        Field idField = FeedReqDto.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(feed, feedId);

        Field contentsField = FeedReqDto.class.getDeclaredField("contents");
        contentsField.setAccessible(true);
        contentsField.set(feed, contents);

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
        given(commentService.createComment(comment.getId(), commentReqDto, user)).willReturn(response);

        // then
        mvc.perform(post("/{feedId}/comments", feedId)
                        .content(postInfo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }
}
