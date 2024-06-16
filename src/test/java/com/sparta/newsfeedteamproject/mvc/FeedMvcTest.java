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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        // 임의의 FEED 생성
        Long feedId = id;
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
        int page = 1;
        String sortBy = "createdAt";
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();

        List<FeedResDto> feedResDtoList = new ArrayList<>();
        feedResDtoList.add(mockFeedResDtoSetup(1L));
        feedResDtoList.add(mockFeedResDtoSetup(2L));
        feedResDtoList.add(mockFeedResDtoSetup(3L));

        MessageResDto<List<FeedResDto>> response = new MessageResDto<>(200, "게시물 조회가 완료되었습니다!", feedResDtoList);

        String getInfo = objectMapper.writeValueAsString(response);

        // when
        given(feedService.getAllFeeds(anyInt(), anyString(), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(response);

        // then
        mvc.perform(get("/feeds/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", String.valueOf(page))
                        .param("sortBy", sortBy)
                        .param("startDate", String.valueOf(startDate))
                        .param("endDate", String.valueOf(endDate)))
                .andExpect(status().isOk())
                .andExpect(content().json(getInfo))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].contents").value("Test"))
                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].contents").value("Test"))
                .andExpect(jsonPath("$.data[2].id").value(3L))
                .andExpect(jsonPath("$.data[2].contents").value("Test"))
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

    @Test
    @DisplayName("게시글 수정")
    void testUpdateFeed() throws Exception {
        // given
        FeedReqDto feedReqDto = mockFeedReqDtoSetup();
        User user = mockUserSetup().getUser();
        Feed feed = new Feed(feedReqDto, user);
        Long feedId = 1L;
        FeedResDto feedResDto = new FeedResDto(feed);
        MessageResDto<FeedResDto> response = new MessageResDto<>(200, "게시물 수정이 완료되었습니다!", feedResDto);

        String putInfo = objectMapper.writeValueAsString(feedReqDto);

        // when
        given(feedService.updateFeed(feedId, feedReqDto, user)).willReturn(response);

        // then
        mvc.perform(put("/feeds/{feedId}", feedId)
                        .content(putInfo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 삭제")
    void testDeleteFeed() throws Exception {
        // given
        Long feedId = 1L;
        User user = mockUserSetup().getUser();
        MessageResDto<FeedResDto> response = new MessageResDto<>(200, "게시물 삭제가 완료되었습니다!", null);

        // when
        given(feedService.deleteFeed(feedId, user)).willReturn(response);

        // then
        mvc.perform(delete("/feeds/{feedId}", feedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }
}
