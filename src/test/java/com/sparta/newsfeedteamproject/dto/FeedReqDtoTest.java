package com.sparta.newsfeedteamproject.dto;

import com.sparta.newsfeedteamproject.dto.feed.FeedReqDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FeedReqDtoTest {

    Validator validator;
    FeedReqDto feedReqDto;

    @BeforeEach
    void setUp() {
        feedReqDto = new FeedReqDto();

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    // 정상적인 유효한 FeedReqDto 생성 테스트
    @Test
    @DisplayName("유효한 FeedReqDto 생성 테스트")
    void testValidContents() {
        // given
        String contents = "Feed test";

        // when
        ReflectionTestUtils.setField(feedReqDto, "contents", contents);

        // then
        assertEquals(contents, feedReqDto.getContents(), "Contents가 올바르게 설정되지 않았습니다.");
    }

    // Contents 필드 유효성 테스트
    @Test
    @DisplayName("Contents 필드 @Notblank Validation 테스트")
    void testContentsNotBlank() {
        // given
        String contents = "";

        ReflectionTestUtils.setField(feedReqDto, "contents", contents);

        // when
        ConstraintViolation<FeedReqDto> violation = validator.validate(feedReqDto).stream().collect(Collectors.toList()).get(0);

        // then
        assertEquals("contents", violation.getPropertyPath().toString(), "@NotBlank Validation 적용이 안되었습니다.");
    }
}
