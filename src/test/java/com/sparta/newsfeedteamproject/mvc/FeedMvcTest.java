package com.sparta.newsfeedteamproject.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeedteamproject.config.SecurityConfig;
import com.sparta.newsfeedteamproject.controller.FeedController;
import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedResDto;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.security.UserDetailsImpl;
import com.sparta.newsfeedteamproject.service.FeedService;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {FeedController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = SecurityConfig.class
                )
        }
)
public class FeedMvcTest {
    private MockMvc mvc;

    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    FeedService feedService;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    private FeedResDto mockFeedResDtoSetup(Long id) throws NoSuchFieldException, IllegalAccessException {
        Long feedId = id;
        String contents = "Test";
        Feed feed = new Feed();

        Field idField = Feed.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(feed, feedId);

        Field contentsField = Feed.class.getDeclaredField("contents");
        contentsField.setAccessible(true);
        contentsField.set(feed, contents);

        FeedResDto feedResDto = new FeedResDto(feed);

        return feedResDto;
    }

    private FeedReqDto mockFeedReqDtoSetup() throws NoSuchFieldException, IllegalAccessException {
        String contents = "Test";
        FeedReqDto feedReqDto = new FeedReqDto();

        Field contentsField = FeedReqDto.class.getDeclaredField("contents");
        contentsField.setAccessible(true);
        contentsField.set(feedReqDto, contents);

        return feedReqDto;
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

    @Test
    @DisplayName("모든 게시글 조회")
    void testGetAllFeeds() throws Exception {
        // given
        List<FeedResDto> feedResDtoList = new ArrayList<>();
        feedResDtoList.add(mockFeedResDtoSetup(1L));
        feedResDtoList.add(mockFeedResDtoSetup(2L));
        feedResDtoList.add(mockFeedResDtoSetup(3L));

        MessageResDto<List<FeedResDto>> response = new MessageResDto<>(200, "게시물 조회가 완료되었습니다!", feedResDtoList);

        // when
        given(feedService.getAllFeeds(any(), any(), any(), any())).willReturn(response);

        // then
        mvc.perform(get("/feeds/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].contents").value("Test"))
                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].contents").value("Test"))
                .andExpect(jsonPath("$.data[2].id").value(3L))
                .andExpect(jsonPath("$.data[3].contents").value("Test"))
                .andDo(print());
    }

    @Test
    @DisplayName("단건 게시글 조회")
    void testGetFeed() throws Exception {
        // given
        Long feedId = 1L;
        FeedResDto feedResDto = mockFeedResDtoSetup(feedId);
        MessageResDto<FeedResDto> response = new MessageResDto<>(200, "게시물 조회가 완료되었습니다!", feedResDto);

        // when
        given(feedService.getFeed(feedId)).willReturn(response);

        // then
        mvc.perform(get("/feeds/{feedId}", feedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.contents").value("Test"))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 생성")
    void testCreateFeed() throws Exception {
        // given
        FeedReqDto feedReqDto = mockFeedReqDtoSetup();
        User user = mockUserSetup().getUser();
        Feed feed = new Feed(feedReqDto, user);
        FeedResDto feedResDto = new FeedResDto(feed);
        MessageResDto<FeedResDto> response = new MessageResDto<>(200, "게시물 작성이 완료되었습니다!", feedResDto);

        String postInfo = objectMapper.writeValueAsString(feedReqDto);

        // when
        given(feedService.createFeed(feedReqDto, user)).willReturn(response);

        // then
        mvc.perform(post("/feeds")
                        .content(postInfo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }
}
