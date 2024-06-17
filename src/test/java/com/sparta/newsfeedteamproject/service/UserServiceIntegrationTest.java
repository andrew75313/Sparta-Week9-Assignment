package com.sparta.newsfeedteamproject.service;

import com.sparta.newsfeedteamproject.dto.user.ProfileResDto;
import com.sparta.newsfeedteamproject.dto.user.SignupReqDto;
import com.sparta.newsfeedteamproject.dto.user.UpdateReqDto;
import com.sparta.newsfeedteamproject.dto.user.UserAuthReqDto;
import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import com.sparta.newsfeedteamproject.security.UserDetailsImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    String username = "spartaclub";
    String password = "Password123!";
    String name = "Sparta Club";
    String email = "sparta@email.com";
    String userInfo = "My name is Sparta Club.";
    SignupReqDto signupReqDto;

    @BeforeEach
    void setUp() {
        this.signupReqDto = setSignupReqDto(this.username, this.password, this.name, this.email, this.userInfo);
    }

    private SignupReqDto setSignupReqDto(String username, String password, String name, String email, String userInfo) {
        signupReqDto = new SignupReqDto();

        ReflectionTestUtils.setField(signupReqDto, "username", username);
        ReflectionTestUtils.setField(signupReqDto, "password", password);
        ReflectionTestUtils.setField(signupReqDto, "name", name);
        ReflectionTestUtils.setField(signupReqDto, "email", email);
        ReflectionTestUtils.setField(signupReqDto, "userInfo", userInfo);

        return signupReqDto;
    }

    @Nested
    @DisplayName("회원가입")
    class SignupTest {

        @Test
        @DisplayName("회원가입 - 성공")
        @Transactional
        void testSigntup() {
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
        @DisplayName("회원가입 - 중복 username 실패")
        @Transactional
        void testSignupDuplicatedUsernameFail() {
            // given
            userService.signup(signupReqDto);

            String password = "Password456!";
            String name = "Sparta Club2";
            String email = "sparta2@email.com";
            String userInfo = "My name is Sparta Club 2.";

            SignupReqDto duplicatedReqDto = setSignupReqDto(username, password, name, email, userInfo);

            // when -then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.signup(duplicatedReqDto));
            assertEquals("중복된 사용자 이름 입니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }

        @Test
        @DisplayName("회원가입 - 중복 Email 실패")
        @Transactional
        void testSignupDuplicatedEmailFail() {
            // given
            userService.signup(signupReqDto);

            String username = "spartaclub2";
            String password = "Password456!";
            String name = "Sparta Club2";
            String userInfo = "My name is Sparta Club 2.";

            SignupReqDto duplicatedReqDto = setSignupReqDto(username, password, name, email, userInfo);

            // when -then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.signup(duplicatedReqDto));
            assertEquals("중복된 이메일 입니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }

    @Nested
    @DisplayName("회원탈퇴")
    class WithdrawTest {

        @Test
        @Transactional
        @DisplayName("회원탈퇴 - 성공")
        void testWithdraw() {
            // given
            userService.signup(signupReqDto);
            User user = userService.findByUsername(username);

            Long userId = user.getId();

            UserAuthReqDto reqDto = new UserAuthReqDto();
            ReflectionTestUtils.setField(reqDto, "password", password);

            UserDetailsImpl userDetails = new UserDetailsImpl(user);

            // when
            userService.withdraw(userId, reqDto, userDetails);

            // then
            User withdrawedUser = userRepository.findById(userId).orElse(null);
            assertEquals(Status.DEACTIVATE, withdrawedUser.getStatus(), "회원탈퇴가 올바르게 진행되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("회원탈퇴 - 실패")
        void testWithdrawUnmatchedUsernameFail() {
            // given
            userService.signup(signupReqDto);
            User savedUser = userService.findByUsername(username);

            UserAuthReqDto reqDto = new UserAuthReqDto();
            ReflectionTestUtils.setField(reqDto, "password", password);

            String username = "spartaclub2";
            String email = "sparta2@email.com";
            User differentUser = new User(username,
                    passwordEncoder.encode(password),
                    name,
                    email,
                    userInfo,
                    Status.ACTIVATE,
                    LocalDateTime.now());

            userRepository.save(differentUser);

            UserDetailsImpl userDetails = new UserDetailsImpl(differentUser);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.withdraw(savedUser.getId(), reqDto, userDetails));
            assertEquals("프로필 사용자와 일치하지 않아 요청을 처리할 수 없습니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class LogoutTest {

        @Test
        @Transactional
        @DisplayName("로그아웃 - 성공")
        void testLogout() {
            // given
            userService.signup(signupReqDto);
            User user = userService.findByUsername(username);

            Long userId = user.getId();

            UserDetailsImpl userDetails = new UserDetailsImpl(user);

            // when
            userService.logout(userId, userDetails);

            // then
            User foundUser = userRepository.findById(userId).orElse(null);
            assertEquals("", foundUser.getRefreshToken(), "로그아웃이 올바르게 진행되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("로그아웃 - 사용자 ID 불일치 실패")
        void testLogoutUnmatchedUseridFail() {
            // given
            userService.signup(signupReqDto);
            User user = userService.findByUsername(username);

            Long userId = user.getId();

            String username = "spartaclub2";
            String email = "sparta2@email.com";
            User differentUser = new User(username,
                    passwordEncoder.encode(password),
                    name,
                    email,
                    userInfo,
                    Status.ACTIVATE,
                    LocalDateTime.now());

            userRepository.save(differentUser);

            UserDetailsImpl userDetails = new UserDetailsImpl(differentUser);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.logout(userId, userDetails));
            assertEquals("프로필 사용자와 일치하지 않아 요청을 처리할 수 없습니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }

    @Nested
    @DisplayName("프로필 조회")
    class TestGetProfile {

        @Test
        @Transactional
        @DisplayName("프로필 조회 - 성공")
        void testGetProfile() {
            // given
            userService.signup(signupReqDto);
            User user = userService.findByUsername(username);

            Long userId = user.getId();

            // when
            ProfileResDto profileResDto = userService.getProfile(userId);

            // then
            assertNotNull(profileResDto, "프로필이 올바르게 조회되지 않았습니다.");
            assertEquals(username, profileResDto.getUsername(), "Username이 올바르게 조회되지 않았습니다.");
            assertEquals(name, profileResDto.getName(), "Name이 올바르게 조회되지 않았습니다.");
            assertEquals(email, profileResDto.getEmail(), "Email이 올바르게 조회되지 않았습니다.");
            assertEquals(userInfo, profileResDto.getUserInfo(), "UserInfo가 올바르게 조회되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("프로필 조회 - 사용자 Status 비활성화 상태 실패")
        void testGetProfileUnmatchedStatusFail() {
            // given
            User user = new User(username,
                    passwordEncoder.encode(password),
                    name,
                    email,
                    userInfo,
                    Status.DEACTIVATE,
                    LocalDateTime.now());

            userRepository.save(user);

            Long userId = user.getId();

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.getProfile(userId));
            assertEquals("탈퇴한 회원입니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }

    @Nested
    @DisplayName("프로필 수정")
    class TestEditProfile {

        String newPassword = "NEW" + password;
        String newName = "NEW " + name;
        String newUserInfO = "NEW " + userInfo;
        UpdateReqDto updateReqDto;


        @BeforeEach
        void beforeTestEditProfile() {
            updateReqDto = new UpdateReqDto();

            ReflectionTestUtils.setField(updateReqDto, "password", password);
            ReflectionTestUtils.setField(updateReqDto, "newPassword", newPassword);
            ReflectionTestUtils.setField(updateReqDto, "newName", newName);
            ReflectionTestUtils.setField(updateReqDto, "newUserInfo", newUserInfO);
        }

        @Test
        @Transactional
        @DisplayName("프로필 수정 - 성공")
        void testEditProfile() {
            // given
            userService.signup(signupReqDto);
            User user = userService.findByUsername(username);

            Long userId = user.getId();

            UserDetailsImpl userDetails = new UserDetailsImpl(user);

            // when
            ProfileResDto profileResDto = userService.editProfile(userId, updateReqDto, userDetails);

            // then
            assertNotNull(profileResDto, "프로필이 올바르게 수정되지 않았습니다.");
            assertEquals(newName, profileResDto.getName(), "Name이 올바르게 수정되지 않았습니다.");
            assertEquals(newUserInfO, profileResDto.getUserInfo(), "UserInfo가 수정되지 조회되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("프로필 수정 - 로그인 유저 Username 불일치 실패")
        void testEditProfileUnmatchedUsernameFail() {
            // given
            userService.signup(signupReqDto);
            User user = userService.findByUsername(username);

            Long userId = user.getId();

            String username = "spartaclub2";
            String email = "sparta2@email.com";
            User differentUser = new User(username,
                    passwordEncoder.encode(password),
                    name,
                    email,
                    userInfo,
                    Status.ACTIVATE,
                    LocalDateTime.now());

            userRepository.save(differentUser);

            UserDetailsImpl userDetails = new UserDetailsImpl(differentUser);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.editProfile(userId, updateReqDto, userDetails));
            assertEquals("프로필 사용자와 일치하지 않아 요청을 처리할 수 없습니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("프로필 수정 - 동일한 Password로 수정 실패")
        void testEditProfileDuplicatedPasswordFail() {
            // given
            userService.signup(signupReqDto);
            User user = userService.findByUsername(username);

            Long userId = user.getId();

            UserDetailsImpl userDetails = new UserDetailsImpl(user);

            ReflectionTestUtils.setField(updateReqDto, "newPassword", password);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.editProfile(userId, updateReqDto, userDetails));
            assertEquals("기존 비밀번호와 일치하여 수정이 불가능합니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }
}
