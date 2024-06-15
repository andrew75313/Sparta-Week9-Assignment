package com.sparta.newsfeedteamproject.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeedteamproject.config.SecurityConfig;
import com.sparta.newsfeedteamproject.controller.CommentController;
import com.sparta.newsfeedteamproject.controller.FeedController;
import com.sparta.newsfeedteamproject.dto.MessageResDto;
import com.sparta.newsfeedteamproject.dto.feed.FeedResDto;
import com.sparta.newsfeedteamproject.entity.Feed;
import com.sparta.newsfeedteamproject.service.FeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @DisplayName("모든 게시글 조회")
    void testGetAllFeeds() throws Exception {
        // given
        List<FeedResDto> feedResDtoList = new ArrayList<>();
        feedResDtoList.add(mockFeedResDtoSetup(1L));
        feedResDtoList.add(mockFeedResDtoSetup(2L));
        feedResDtoList.add(mockFeedResDtoSetup(3L));

        MessageResDto<List<FeedResDto>> messageResDto = new MessageResDto<>(200, "게시물 조회가 완료되었습니다!", feedResDtoList);

        // when
        given(feedService.getAllFeeds(any(), any(), any(), any())).willReturn(messageResDto);

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

        MessageResDto<FeedResDto> messageResDto = new MessageResDto<>(200, "게시물 조회가 완료되었습니다!", feedResDto);

        // when
        given(feedService.getFeed(feedId)).willReturn(messageResDto);

        // then
        mvc.perform(get("/feeds/{feedId}", feedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.contents").value("Test"))
                .andDo(print());
    }
}
