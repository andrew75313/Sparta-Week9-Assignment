package com.sparta.newsfeedteamproject.dto;

import com.sparta.newsfeedteamproject.dto.user.UserAuthReqDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserAuthReqDtoTest {

    Validator validator;
    UserAuthReqDto userAuthReqDto;

    @BeforeEach
    void setUp() {
        userAuthReqDto = new UserAuthReqDto();

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    // 정상적인 유효한 UserAuthReqDto 생성 테스트
    @Test
    @DisplayName("유효한 UserAuthReqDto 생성 테스트")
    void testValidUserAuthReqDto() throws IllegalAccessException, NoSuchFieldException {
        // given
        String username = "spartaclub";
        String password = "Password123!";

        // DTO 필드가 모두 private 이므로 Reflection을 사용해서 모두 지정
        Field usernameField = UserAuthReqDto.class.getDeclaredField("username");
        usernameField.setAccessible(true);
        usernameField.set(userAuthReqDto, username);

        Field passwordField = UserAuthReqDto.class.getDeclaredField("password");
        passwordField.setAccessible(true);
        passwordField.set(userAuthReqDto, password);

        // when - then
        assertEquals(username, userAuthReqDto.getUsername(), "Username이 올바르게 설정되지 않았습니다.");
        assertEquals(password, userAuthReqDto.getPassword(), "Password가 올바르게 설정되지 않았습니다.");
    }

    // Username 필드 테스트
    @Nested
    @DisplayName("Username 필드 유효 테스트")
    class UsernameValidTest {

        @Test
        @DisplayName("@NotBlank Validation 테스트")
        void testUsernameNotBlank() throws NoSuchFieldException, IllegalAccessException {
            // given
            String username = "";

            Field field = UserAuthReqDto.class.getDeclaredField("username");
            field.setAccessible(true);
            field.set(userAuthReqDto, username);

            // when
            List<ConstraintViolation<UserAuthReqDto>> validationList = validator.validate(userAuthReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[username:blank] 사용자 아이디를 작성해주세요!"), "@NotBlank Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Size Validation 테스트")
        void testUsernameSize() throws NoSuchFieldException, IllegalAccessException {
            // given
            String username = "TestUser1";

            Field field = UserAuthReqDto.class.getDeclaredField("username");
            field.setAccessible(true);
            field.set(userAuthReqDto, username);

            // when
            List<ConstraintViolation<UserAuthReqDto>> validationList = validator.validate(userAuthReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[username:size] 10자 이상 20자 이하로 작성해주세요!"), "@Size Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Pattern Validation 테스트")
        void testUsernamePattern() throws NoSuchFieldException, IllegalAccessException {
            // given
            String username = "test!user";

            Field field = UserAuthReqDto.class.getDeclaredField("username");
            field.setAccessible(true);
            field.set(userAuthReqDto, username);

            // when
            List<ConstraintViolation<UserAuthReqDto>> validationList = validator.validate(userAuthReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[username:pattern] 숫자와 영문 대소문자를 포함하여 작성해주세요!"), "@Pattern Validation 적용이 안되었습니다.");
        }
    }

    // Password 필드 테스트
    @Nested
    @DisplayName("Password 필드 유효 테스트")
    class PasswordValidTest {

        @Test
        @DisplayName("@NotBlank Validation 테스트")
        void testPasswordNotBlank() throws NoSuchFieldException, IllegalAccessException {
            // given
            String password = "";

            Field field = UserAuthReqDto.class.getDeclaredField("password");
            field.setAccessible(true);
            field.set(userAuthReqDto, password);

            // when
            List<ConstraintViolation<UserAuthReqDto>> validationList = validator.validate(userAuthReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[password:blank] 비밀번호를 작성해주세요!"), "@NotBlank Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Size Validation 테스트")
        void testPasswordSize() throws NoSuchFieldException, IllegalAccessException {
            // given
            String password = "TestPw1!";

            Field field = UserAuthReqDto.class.getDeclaredField("password");
            field.setAccessible(true);
            field.set(userAuthReqDto, password);

            // when
            List<ConstraintViolation<UserAuthReqDto>> validationList = validator.validate(userAuthReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[password:size] 10자 이상으로 작성해주세요!"), "@Size Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Pattern Validation 테스트")
        void testPasswordPattern() throws NoSuchFieldException, IllegalAccessException {
            // given
            String password = "password123";

            Field field = UserAuthReqDto.class.getDeclaredField("password");
            field.setAccessible(true);
            field.set(userAuthReqDto, password);

            // when
            List<ConstraintViolation<UserAuthReqDto>> validationList = validator.validate(userAuthReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[password:pattern] 숫자, 영문 대소문자, 특수기호를 최소 한개씩 포함하여 작성해주세요!"), "@Pattern Validation 적용이 안되었습니다.");
        }
    }


}
