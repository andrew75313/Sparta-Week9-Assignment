package com.sparta.newsfeedteamproject.serviceTest;

import com.sparta.newsfeedteamproject.dto.user.SignupReqDto;
import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import com.sparta.newsfeedteamproject.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceIntegrationTest {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("회원가입")
    class SignupTest {

        String username = "spartaclub";
        String password = "Password123!";
        String name = "Sparta Club";
        String email = "sparta@email.com";
        String userInfo = "My name is Sparta Club.";
        SignupReqDto signupReqDto;

        @BeforeEach
        void beforeSignup() throws NoSuchFieldException, IllegalAccessException {
            signupReqDto = new SignupReqDto();

            Field usernameField = SignupReqDto.class.getDeclaredField("username");
            usernameField.setAccessible(true);
            usernameField.set(signupReqDto, username);

            Field passwordField = SignupReqDto.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            passwordField.set(signupReqDto, password);

            Field nameField = SignupReqDto.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(signupReqDto, name);

            Field emailField = SignupReqDto.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(signupReqDto, email);

            Field userInfoField = SignupReqDto.class.getDeclaredField("userInfo");
            userInfoField.setAccessible(true);
            userInfoField.set(signupReqDto, userInfo);
        }

        @Test
        @Transactional
        @DisplayName("회원가입 - 성공")
        void testSigntup() throws NoSuchFieldException, IllegalAccessException {
            // when
            userService.signup(signupReqDto);

            // then
            User foundUser = userRepository.findByUsername(username).orElse(null);

            assertNotNull(foundUser, "회원가입이 올바르게 진행되지 않았습니다.");
            assertEquals(username, foundUser.getUsername(), "Username이 올바르게 저장되지 않았습니다.");
            assertTrue(passwordEncoder.matches(password, foundUser.getPassword()), "Password가 올바르게 저장되지 않았습니다.");
            assertEquals(name, foundUser.getName(), "Name이 올바르게 저장되지 않았습니다.");
            assertEquals(email, foundUser.getEmail(), "Email이 올바르게 저장되지 않았습니다.");
            assertEquals(userInfo, foundUser.getUserInfo(), "UserInfo가 올바르게 저장되지 않았습니다.");
            assertEquals(Status.ACTIVATE, foundUser.getStatus(), "Status가 올바르게 저장되지 않았습니다.");
            assertNotNull(foundUser.getStatusModTime(), "StatusModTime이 올바르게 저장되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("회원가입 - 중복 username 실패")
        void testSignupDuplicatedUsernameFail() throws NoSuchFieldException, IllegalAccessException {
            // given
            userService.signup(signupReqDto);

            SignupReqDto duplicatedReqDto = new SignupReqDto();

            Field usernameField = SignupReqDto.class.getDeclaredField("username");
            usernameField.setAccessible(true);
            usernameField.set(duplicatedReqDto, "spartaclub");

            // when -then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.signup(duplicatedReqDto));
            assertEquals("중복된 사용자 이름 입니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("회원가입 - 중복 Email 실패")
        void testSignupDuplicatedEmailFail() throws NoSuchFieldException, IllegalAccessException {
            // given
            userService.signup(signupReqDto);

            SignupReqDto duplicatedReqDto = new SignupReqDto();

            Field usernameField = SignupReqDto.class.getDeclaredField("username");
            usernameField.setAccessible(true);
            usernameField.set(duplicatedReqDto, "spartaclub2");

            Field emailField = SignupReqDto.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(duplicatedReqDto, "sparta@email.com");

            // when -then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.signup(duplicatedReqDto));
            assertEquals("중복된 이메일 입니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }
}
