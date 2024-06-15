package com.sparta.newsfeedteamproject.serviceTest;

import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import com.sparta.newsfeedteamproject.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    // 실제 UserService 를 사용하기 위해
    @InjectMocks
    private UserService userService;

    String username = "spartaclub";
    String password = "Password123!";
    String name = "Sparta Club";
    String email = "sparta@email.com";
    String userInfo = "My name is Sparta Club.";
    Status status = Status.ACTIVATE;

    @Nested
    @DisplayName("Username으로 사용자 찾기")
    class FindByUsernameTest {

        @Test
        @DisplayName("Username으로 사용자 찾기 - 성공")
        void testFindByUsername() {
            // given
            User user = new User(username, password, name, email, userInfo, status, LocalDateTime.now());

            given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

            // when
            User foundUser = userService.findByUsername(username);

            // then
            assertNotNull(foundUser, "사용자가 올바르게 찾아지지 않았습니다.");
            assertEquals(username, foundUser.getUsername(), "사용자의 Username이 올바르지 않습니다.");
            assertEquals(name, foundUser.getName(), "사용자의 Name이 올바르지 않습니다.");
            assertEquals(email, foundUser.getEmail(), "사용자의 Email이 올바르지 않습니다.");
            assertEquals(userInfo, foundUser.getUserInfo(), "사용자의 UserInfo가 올바르지 않습니다.");
        }

        @Test
        @DisplayName("Username으로 사용자 찾기 - 실패")
        void testFindByUsernameNoUserFail() {
            // given
            given(userRepository.findByUsername(username)).willReturn(Optional.empty());

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.findByUsername(username));
            assertEquals("존재하지 않는 사용자입니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }

    @Nested
    @DisplayName("Email로 사용자 찾기")
    class FindByEmailTest {

        @Test
        @DisplayName("Email으로 사용자 찾기 - 성공")
        void testFindByEmail() {
            // given
            User user = new User(username, password, name, email, userInfo, status, LocalDateTime.now());

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

            // when
            User foundUser = userService.findByEmail(email);

            // then
            assertNotNull(foundUser, "사용자가 올바르게 찾아지지 않았습니다.");
            assertEquals(username, foundUser.getUsername(), "사용자의 Username이 올바르지 않습니다.");
            assertEquals(name, foundUser.getName(), "사용자의 Name이 올바르지 않습니다.");
            assertEquals(email, foundUser.getEmail(), "사용자의 Email이 올바르지 않습니다.");
            assertEquals(userInfo, foundUser.getUserInfo(), "사용자의 UserInfo가 올바르지 않습니다.");
        }

        @Test
        @DisplayName("Email으로 사용자 찾기 - 실패")
        void testFindByEmailNoUserFail() {
            // given
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // when - then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.findByEmail(email));
            assertEquals("존재하지 않는 사용자입니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }
}
