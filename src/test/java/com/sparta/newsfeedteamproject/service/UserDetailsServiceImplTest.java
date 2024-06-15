package com.sparta.newsfeedteamproject.service;

import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import com.sparta.newsfeedteamproject.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsServiceImpl;

    @Nested
    @DisplayName("Username으로 사용자 찾기")
    class LoadUserByUsernameTest {

        @Test
        @DisplayName("Username으로 사용자 찾기 - 성공")
        void testLoadUserByUsername() {
            // given
            String username = "spartaclub";
            String password = "Password123!";
            String name = "Sparta Club";
            String email = "sparta@email.com";
            String userInfo = "My name is Sparta Club.";
            Status status = Status.ACTIVATE;

            User user = new User(username, password, name, email, userInfo, status, LocalDateTime.now());

            given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

            // when
            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);

            // then
            assertNotNull(userDetails, "사용자가 올바르게 찾아지지 않았습니다.");
            assertEquals(username, userDetails.getUsername(), "사용자의 Username이 올바르지 않습니다.");
        }

        @Test
        @DisplayName("Username으로 사용자 찾기 - 실패")
        void testLoadUserByUsernameNoUserFail() {
            // given
            String username = "spartaclub";
            String password = "Password123!";
            String name = "Sparta Club";
            String email = "sparta@email.com";
            String userInfo = "My name is Sparta Club.";
            Status status = Status.ACTIVATE;

            User user = new User(username, password, name, email, userInfo, status, LocalDateTime.now());

            given(userRepository.findByUsername(username)).willReturn(Optional.empty());

            // when - then
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userDetailsServiceImpl.loadUserByUsername(username));
            assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage(), "올바른 예외가 발생되지 않았습니다.");
        }
    }
}
