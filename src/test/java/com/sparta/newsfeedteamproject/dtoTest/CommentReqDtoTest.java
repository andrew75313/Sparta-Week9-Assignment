package com.sparta.newsfeedteamproject.dtoTest;

import com.sparta.newsfeedteamproject.dto.comment.CommentReqDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommentReqDtoTest {

    Validator validator;
    CommentReqDto commentReqDto;

    @BeforeEach
    void setUp() {
        commentReqDto = new CommentReqDto();

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    // 정상적인 유효한 CommentReqDto 생성 테스트
    @Test
    @DisplayName("유효한 CommentReqDto 생성 테스트")
    void testValidContents() throws NoSuchFieldException, IllegalAccessException {
        // given
        String contents = "Comment test";

        // when
        Field field = CommentReqDto.class.getDeclaredField("contents");
        field.setAccessible(true);
        field.set(commentReqDto, contents);

        // then
        assertEquals(contents, commentReqDto.getContents(), "Contents가 올바르게 설정되지 않았습니다.");
    }

    // Contents 필드 유효성 테스트
    @Test
    @DisplayName("Contents 필드 @Notblank Validation 테스트")
    void testContentsNotBlank() throws NoSuchFieldException, IllegalAccessException {
        // given
        String contents = "";

        Field field = CommentReqDto.class.getDeclaredField("contents");
        field.setAccessible(true);
        field.set(commentReqDto, contents);

        // when
        List<ConstraintViolation<CommentReqDto>> validationList = validator.validate(commentReqDto).stream().collect(Collectors.toList());

        // then
        List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
        assertTrue(messageList.contains("[contents:blank] 댓글 내용을 작성해주세요!"), "@NotBlank Validation 적용이 안되었습니다.");
    }
}
