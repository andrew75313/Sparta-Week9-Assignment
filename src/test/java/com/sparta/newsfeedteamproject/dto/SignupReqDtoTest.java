package com.sparta.newsfeedteamproject.dto;

import com.sparta.newsfeedteamproject.dto.user.SignupReqDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignupReqDtoTest {

    Validator validator;
    SignupReqDto signupReqDto;

    @BeforeEach
    void setUp() {
        signupReqDto = new SignupReqDto();

        // Validator 사용을 위한 객체 생성
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    // 정상적인 유효한 SignupReqDto 생성 테스트
    @Test
    @DisplayName("유효한 SignupReqDto 생성 테스트")
    void testValidSignupReqDto() throws IllegalAccessException, NoSuchFieldException {
        // given
        String username = "spartaclub";
        String password = "Password123!";
        String name = "Sparta Club";
        String email = "sparta@email.com";
        String userInfo = "My name is Sparta Club.";

        // DTO 필드가 모두 private 이므로 Reflection을 사용해서 모두 지정
        ReflectionTestUtils.setField(signupReqDto, "username", username);
        ReflectionTestUtils.setField(signupReqDto, "password", password);
        ReflectionTestUtils.setField(signupReqDto, "name", name);
        ReflectionTestUtils.setField(signupReqDto, "email", email);
        ReflectionTestUtils.setField(signupReqDto, "userInfo", userInfo);

        // when - then
        assertEquals(username, signupReqDto.getUsername(), "Username이 올바르게 설정되지 않았습니다.");
        assertEquals(password, signupReqDto.getPassword(), "Password가 올바르게 설정되지 않았습니다.");
        assertEquals(name, signupReqDto.getName(), "Name이 올바르게 설정되지 않았습니다.");
        assertEquals(email, signupReqDto.getEmail(), "Email이 올바르게 설정되지 않았습니다.");
        assertEquals(userInfo, signupReqDto.getUserInfo(), "UserInfo가 올바르게 설정되지 않았습니다.");

    }

    // Username 필드 테스트
    @Nested
    @DisplayName("Username 필드 유효 테스트")
    class UsernameValidTest {

        @Test
        @DisplayName("@Notblank Validation 테스트")
        void testUsernameNotBlank() {
            // given
            String username = "";

            ReflectionTestUtils.setField(signupReqDto, "username", username);

            // when
            List<ConstraintViolation<SignupReqDto>> validationList = validator.validate(signupReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[username:blank] 사용자 아이디를 작성해주세요!"), "@NotBlank Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Size Validation 테스트")
        void testUsernameSize() {
            // given
            String username = "Test1!";

            ReflectionTestUtils.setField(signupReqDto, "username", username);

            // when
            List<ConstraintViolation<SignupReqDto>> validationList = validator.validate(signupReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[username:size] 10자 이상 20자 이하로 작성해주세요!"), "@Size Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Pattern Validation 테스트")
        void testUsernamePattern() {
            // given
            String username = "testusername!";

            ReflectionTestUtils.setField(signupReqDto, "username", username);

            // when
            List<ConstraintViolation<SignupReqDto>> validationList = validator.validate(signupReqDto).stream().collect(Collectors.toList());

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
        @DisplayName("@Notblank Validation 테스트")
        void testPasswordNotBlank() {
            // given
            String password = "";

            ReflectionTestUtils.setField(signupReqDto, "password", password);

            // when
            List<ConstraintViolation<SignupReqDto>> validationList = validator.validate(signupReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[password:blank] 비밀번호를 작성해주세요!"), "@NotBlank Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Size Validation 테스트")
        void testPasswordSize() {
            // given
            String password = "Pw1!";

            ReflectionTestUtils.setField(signupReqDto, "password", password);

            // when
            List<ConstraintViolation<SignupReqDto>> validationList = validator.validate(signupReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[password:size] 10자 이상으로 작성해주세요!"), "@Size Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Pattern Validation 테스트")
        void testPasswordPattern() {
            // given
            String password = "passwordtest!";

            ReflectionTestUtils.setField(signupReqDto, "password", password);

            // when
            List<ConstraintViolation<SignupReqDto>> validationList = validator.validate(signupReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[password:pattern] 숫자, 영문 대소문자, 특수기호를 최소 한개씩 포함하여 작성해주세요!"), "@Pattern Validation 적용이 안되었습니다.");
        }
    }

    // Name 필드 테스트
    @Test
    @DisplayName("Name 필드 @Notblank Validation 테스트")
    void testNameNotBlank() {
        // given
        String name = "";

        ReflectionTestUtils.setField(signupReqDto, "name", name);

        // when
        List<ConstraintViolation<SignupReqDto>> validationList = validator.validate(signupReqDto).stream().collect(Collectors.toList());

        // then
        List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
        assertTrue(messageList.contains("[name:blank] 사용자 이름을 작성해주세요!"), "@NotBlank Validation 적용이 안되었습니다.");
    }


    // email 필드 테스트
    @Nested
    @DisplayName("Email 필드 유효 테스트")
    class EmailValidTest {

        @Test
        @DisplayName("@Notblank Validation 테스트")
        void testEmailNotBlank() {
            // given
            String email = "";

            ReflectionTestUtils.setField(signupReqDto, "email", email);

            // when
            List<ConstraintViolation<SignupReqDto>> validationList = validator.validate(signupReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[email:blank] 이메일을 작성해주세요!"), "@NotBlank Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Email Validation 테스트")
        void testPasswordPattern() {
            // given
            String email = "email";

            ReflectionTestUtils.setField(signupReqDto, "email", email);

            // when
            List<ConstraintViolation<SignupReqDto>> validationList = validator.validate(signupReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[email:pattern] 이메일 형식을 맞춰주세요!"), "@Email Validation 적용이 안되었습니다.");
        }
    }
}



