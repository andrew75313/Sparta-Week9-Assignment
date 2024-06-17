package com.sparta.newsfeedteamproject.dto;

import com.sparta.newsfeedteamproject.dto.user.UpdateReqDto;
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

public class UpdateReqDtoTest {

    Validator validator;
    UpdateReqDto updateReqDto;

    @BeforeEach
    void setUp() {
        updateReqDto = new UpdateReqDto();

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    // 정상적인 유효한 UpdateReqDtoTest 생성 테스트
    @Test
    @DisplayName("유효한 SignupReqDto 생성 테스트")
    void testValidUpdateReqDtoTest() throws IllegalAccessException, NoSuchFieldException {
        // given
        String password = "Password123!";
        String newPassword = "NewPassword123!";
        String newName = "newsparta";
        String newUserInfo = "My name is New Sparta Club.";

        ReflectionTestUtils.setField(updateReqDto, "password", password);
        ReflectionTestUtils.setField(updateReqDto, "newPassword", newPassword);
        ReflectionTestUtils.setField(updateReqDto, "newName", newName);
        ReflectionTestUtils.setField(updateReqDto, "newUserInfo", newUserInfo);

        // when - then
        assertEquals(password, updateReqDto.getPassword(), "Password가 올바르게 설정되지 않았습니다.");
        assertEquals(newPassword, updateReqDto.getNewPassword(), "NewPassword가 올바르게 설정되지 않았습니다.");
        assertEquals(newName, updateReqDto.getNewName(), "NewName이 올바르게 설정되지 않았습니다.");
        assertEquals(newUserInfo, updateReqDto.getNewUserInfo(), "NewUserInfo가 올바르게 설정되지 않았습니다.");
    }

    // Password 필드 테스트
    @Nested
    @DisplayName("Password 필드 유효 테스트")
    class PasswordValidTest {

        @Test
        @DisplayName("@NotBlank Validation 테스트")
        void testPasswordNotBlank() {
            // given
            String password = "";

            ReflectionTestUtils.setField(updateReqDto, "password", password);

            // when
            List<ConstraintViolation<UpdateReqDto>> validationList = validator.validate(updateReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[password:blank] 비밀번호를 작성해주세요!"), "@NotBlank Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Size Validation 테스트")
        void testPasswordSize() {
            // given
            String password = "Pw1!";

            ReflectionTestUtils.setField(updateReqDto, "password", password);

            // when
            List<ConstraintViolation<UpdateReqDto>> validationList = validator.validate(updateReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[password:size] 10자 이상으로 작성해주세요!"), "@Size Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Pattern Validation 테스트")
        void testPasswordPattern() {
            // given
            String password = "passwordtest!";

            ReflectionTestUtils.setField(updateReqDto, "password", password);

            // when
            List<ConstraintViolation<UpdateReqDto>> validationList = validator.validate(updateReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[password:pattern] 숫자, 영문 대소문자, 특수기호를 최소 한개씩 포함하여 작성해주세요!"), "@Pattern Validation 적용이 안되었습니다.");
        }
    }

    // NewPassword 필드 테스트
    @Nested
    @DisplayName("NewPassword 필드 유효 테스트")
    class NewPasswordValidTest {

        @Test
        @DisplayName("@NotBlank Validation 테스트")
        void testNewPasswordNotBlank() {
            // given
            String newPassword = "";

            ReflectionTestUtils.setField(updateReqDto, "newPassword", newPassword);

            // when
            List<ConstraintViolation<UpdateReqDto>> validationList = validator.validate(updateReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[newPassword:blank] 비밀번호를 작성해주세요!"), "@NotBlank Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Size Validation 테스트")
        void testNewPasswordSize() {
            // given
            String newPassword = "NewPw1!";

            ReflectionTestUtils.setField(updateReqDto, "newPassword", newPassword);

            // when
            List<ConstraintViolation<UpdateReqDto>> validationList = validator.validate(updateReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[newPassword:size] 10자 이상으로 작성해주세요!"), "@Size Validation 적용이 안되었습니다.");
        }

        @Test
        @DisplayName("@Pattern Validation 테스트")
        void testNewPasswordPattern() {
            // given
            String newPassword = "newpasswordtest!";

            ReflectionTestUtils.setField(updateReqDto, "newPassword", newPassword);

            // when
            List<ConstraintViolation<UpdateReqDto>> validationList = validator.validate(updateReqDto).stream().collect(Collectors.toList());

            // then
            List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            assertTrue(messageList.contains("[newPassword:pattern] 숫자, 영문 대소문자, 특수기호를 최소 한개씩 포함하여 작성해주세요!"), "@Pattern Validation 적용이 안되었습니다.");
        }
    }

    // NewName 필드 테스트
    @Test
    @DisplayName("newName 필드 @Notblank Validation 테스트")
    void testNewNameNotBlank() {
        // given
        String newName = "";

        ReflectionTestUtils.setField(updateReqDto, "newName", newName);

        // when
        List<ConstraintViolation<UpdateReqDto>> validationList = validator.validate(updateReqDto).stream().collect(Collectors.toList());

        // then
        List<String> messageList = validationList.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
        assertTrue(messageList.contains("[newName:blank] 사용자 이름을 작성해주세요!"), "@NotBlank Validation 적용이 안되었습니다.");
    }

}
