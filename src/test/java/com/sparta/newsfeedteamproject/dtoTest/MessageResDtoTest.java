package com.sparta.newsfeedteamproject.dtoTest;

import com.sparta.newsfeedteamproject.dto.MessageResDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageResDtoTest {

    MessageResDto messageResDto;

    @BeforeEach
    void setUp() {
        messageResDto = new MessageResDto();
    }

    // 생성자 테스트
    @Test
    @DisplayName("생성자 테스트")
    void testMessageResDtoConstructor() {
        // given
        int statusCode = 200;
        String message = "OK";
        String data = "Test Data";

        // when
        messageResDto = new MessageResDto(statusCode, message, data);

        // then
        assertEquals(200, messageResDto.getStatusCode(), "StatusCode가 올바르게 설정되지 않았습니다.");
        assertEquals("OK", messageResDto.getMessage(), "Message가 올바르게 설정되지 않았습니다.");
        assertEquals("Test Data", messageResDto.getData(), "Datat가 올바르게 설정되지 않았습니다.");
    }

    // @JsonInclude 테스트
    @Test
    @DisplayName("JsonInclude 애너테이션 테스트")
    void testJsonIncludeAnnotation() {
        // given
        int statusCode = 200;
        String message = "OK";
        String data = null;

        // when
        messageResDto = new MessageResDto(statusCode, message, data);

        // then
        assertNull(messageResDto.getData(), "Data가 null 로 설정되지 않았습니다.");
    }
}
