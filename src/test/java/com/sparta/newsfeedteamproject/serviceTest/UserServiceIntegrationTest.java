package com.sparta.newsfeedteamproject.serviceTest;

import com.sparta.newsfeedteamproject.dto.user.ProfileResDto;
import com.sparta.newsfeedteamproject.dto.user.SignupReqDto;
import com.sparta.newsfeedteamproject.dto.user.UpdateReqDto;
import com.sparta.newsfeedteamproject.dto.user.UserAuthReqDto;
import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import com.sparta.newsfeedteamproject.security.UserDetailsImpl;
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

    String username = "spartaclub";
    String password = "Password123!";
    String name = "Sparta Club";
    String email = "sparta@email.com";
    String userInfo = "My name is Sparta Club.";
    SignupReqDto signupReqDto;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
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

    @Nested
    @DisplayName("회원가입")
    class SignupTest {

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

    @Nested
    @DisplayName("회원탈퇴")
    class WithdrawTest {

        @Test
        @Transactional
        @DisplayName("회원탈퇴 - 성공")
        void testWithdraw() throws NoSuchFieldException, IllegalAccessException {
            // given
            userService.signup(signupReqDto);
            User foundUser = userService.findByUsername(username);

            Long userId = foundUser.getId();

            UserAuthReqDto reqDto = new UserAuthReqDto();
            Field passwordField = UserAuthReqDto.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            passwordField.set(reqDto, password);

            UserDetailsImpl userDetails = new UserDetailsImpl(foundUser);

            // when
            userService.withdraw(userId, reqDto, userDetails);

            // then
            User withdrawedUser = userRepository.findById(userId).orElse(null);
            assertEquals(Status.DEACTIVATE, withdrawedUser.getStatus(), "회원탈퇴가 올바르게 진행되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("회원탈퇴 - 로그인 유저 username 불일치 실패")
        void testWithdrawUnmatchedUsernameFail() throws NoSuchFieldException, IllegalAccessException {
            // given
            User differentUser = new User();
            differentUser.setId(100000L);
            differentUser.setUsername("spartaclub2");
            userRepository.save(differentUser);

            userService.signup(signupReqDto);
            User foundUser = userService.findByUsername(username);

            Long userId = foundUser.getId();

            UserAuthReqDto reqDto = new UserAuthReqDto();

            UserDetailsImpl userDetails = new UserDetailsImpl(differentUser);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.withdraw(userId, reqDto, userDetails));
            assertEquals("프로필 사용자와 일치하지 않아 요청을 처리할 수 없습니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("회원탈퇴 - 로그인 유저 password 불일치 실패")
        void testWithdrawUnmatchedPasswordFail() throws NoSuchFieldException, IllegalAccessException {
            // given
            User differentUser = new User();
            differentUser.setId(100000L);
            differentUser.setUsername("spartaclub");
            differentUser.setPassword(passwordEncoder.encode("Password456!"));
            userRepository.save(differentUser);

            userService.signup(signupReqDto);
            User foundUser = userService.findByUsername(username);

            Long userId = foundUser.getId();

            UserAuthReqDto reqDto = new UserAuthReqDto();
            Field passwordField = UserAuthReqDto.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            passwordField.set(reqDto, password);

            UserDetailsImpl userDetails = new UserDetailsImpl(differentUser);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.withdraw(userId, reqDto, userDetails));
            assertEquals("비밀번호가 일치하지 않아 요청을 처리할 수 없습니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("회원탈퇴 - 로그인 유저 Status 불일치 실패")
        void testWithdrawUnmatchedStatusFail() throws NoSuchFieldException, IllegalAccessException {
            // given
            User differentUser = new User();
            differentUser.setId(100000L);
            differentUser.setUsername("spartaclub");
            differentUser.setPassword(passwordEncoder.encode("Password123!"));
            differentUser.setStatus(Status.DEACTIVATE);
            userRepository.save(differentUser);

            userService.signup(signupReqDto);
            User foundUser = userService.findByUsername(username);

            Long userId = foundUser.getId();

            UserAuthReqDto reqDto = new UserAuthReqDto();
            Field passwordField = UserAuthReqDto.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            passwordField.set(reqDto, password);

            UserDetailsImpl userDetails = new UserDetailsImpl(differentUser);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.withdraw(userId, reqDto, userDetails));
            assertEquals("탈퇴한 회원입니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
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
            User user = new User();
            user.setId(1L);
            user.setRefreshToken("refreshtoken");

            userRepository.save(user);

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
            User user = new User();
            user.setId(1L);
            user.setRefreshToken("refreshtoken");

            User differentUser = new User();
            differentUser.setId(1000L);

            userRepository.save(user);

            Long userId = user.getId();

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
            User user = new User();
            user.setId(1L);
            user.setUsername(username);
            user.setName(name);
            user.setEmail(email);
            user.setUserInfo(userInfo);
            user.setStatus(Status.ACTIVATE);

            userRepository.save(user);

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
            User user = new User();
            user.setId(1L);
            user.setStatus(Status.DEACTIVATE);

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

        UpdateReqDto updateReqDto;

        @BeforeEach
        void beforeTestEditProfile() throws NoSuchFieldException, IllegalAccessException {
            updateReqDto = new UpdateReqDto();

            Field passwordField = UpdateReqDto.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            passwordField.set(updateReqDto, password);

            Field newPasswordField = UpdateReqDto.class.getDeclaredField("newPassword");
            newPasswordField.setAccessible(true);
            newPasswordField.set(updateReqDto, "NEW" + password);

            Field newNamelField = UpdateReqDto.class.getDeclaredField("newName");
            newNamelField.setAccessible(true);
            newNamelField.set(updateReqDto, "NEW " + name);

            Field newUserInfoField = UpdateReqDto.class.getDeclaredField("newUserInfo");
            newUserInfoField.setAccessible(true);
            newUserInfoField.set(updateReqDto, "NEW " + userInfo);
        }

        @Test
        @Transactional
        @DisplayName("프로필 수정 - 성공")
        void testEditProfile() {
            // given
            User user = new User();
            user.setId(1L);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setStatus(Status.ACTIVATE);

            userRepository.save(user);

            Long userId = user.getId();

            UserDetailsImpl userDetails = new UserDetailsImpl(user);

            // when
            ProfileResDto profileResDto = userService.editProfile(userId, updateReqDto, userDetails);

            // then
            assertNotNull(profileResDto, "프로필이 올바르게 수정되지 않았습니다.");
            assertEquals(username, profileResDto.getUsername(), "Username이 올바르게 수정되지 않았습니다.");
            assertEquals(name, profileResDto.getName(), "Name이 올바르게 수정되지 않았습니다.");
            assertEquals(email, profileResDto.getEmail(), "Email이 올바르게 수정되지 않았습니다.");
            assertEquals(userInfo, profileResDto.getUserInfo(), "UserInfo가 수정되지 조회되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("프로필 수정 - 로그인 유저 Username 불일치 실패")
        void testEditProfileUnmatchedUsernameFail() {
            // given
            User user = new User();
            user.setId(1L);
            user.setUsername(username);

            User differentUser = new User();
            differentUser.setUsername("spartaclub2");

            userRepository.save(user);
            userRepository.save(differentUser);

            Long userId = user.getId();

            UserDetailsImpl userDetails = new UserDetailsImpl(differentUser);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.editProfile(userId, updateReqDto, userDetails));
            assertEquals("프로필 사용자와 일치하지 않아 요청을 처리할 수 없습니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("프로필 수정 - 로그인 유저 Status 비활성상태 실패")
        void testEditProfileUnmatchedStatusFail() {
            // given
            User user = new User();
            user.setId(1L);
            user.setUsername(username);
            user.setStatus(Status.DEACTIVATE);

            User differentUser = new User();
            differentUser.setUsername(username);

            userRepository.save(user);
            userRepository.save(differentUser);

            Long userId = user.getId();

            UserDetailsImpl userDetails = new UserDetailsImpl(differentUser);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.editProfile(userId, updateReqDto, userDetails));
            assertEquals("탈퇴한 회원입니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("프로필 수정 - 로그인 유저 Password 불일치 실패")
        void testEditProfileUnmatchedPasswordFail() {
            // given
            userService.signup(signupReqDto);

            User differentUser = new User();
            differentUser.setUsername(username);
            differentUser.setPassword("Password456!");

            userRepository.save(differentUser);

            Long userId = userRepository.findByUsername(username).orElse(null).getId();

            UserDetailsImpl userDetails = new UserDetailsImpl(differentUser);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.editProfile(userId, updateReqDto, userDetails));
            assertEquals("비밀번호가 일치하지 않아 요청을 처리할 수 없습니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }

        @Test
        @Transactional
        @DisplayName("프로필 수정 - 동일한 Password로 수정 실패")
        void testEditProfileDuplicatedPasswordFail() throws NoSuchFieldException, IllegalAccessException {
            // given
            userService.signup(signupReqDto);

            User differentUser = new User();
            differentUser.setUsername(username);
            differentUser.setPassword("Password123!");

            userRepository.save(differentUser);

            Long userId = userRepository.findByUsername(username).orElse(null).getId();

            UserDetailsImpl userDetails = new UserDetailsImpl(differentUser);

            Field newPasswordField = UpdateReqDto.class.getDeclaredField("newPassword");
            newPasswordField.setAccessible(true);
            newPasswordField.set(updateReqDto, password);

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.editProfile(userId, updateReqDto, userDetails));
            assertEquals("기존 비밀번호와 일치하여 수정이 불가능합니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }
}
