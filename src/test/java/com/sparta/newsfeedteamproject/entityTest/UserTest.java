package com.sparta.newsfeedteamproject.entityTest;

import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    User user;
    Long id;
    String username;
    String password;
    String name;
    String email;
    String userInfo;
    Status status;
    String refreshToken;
    LocalDateTime statusModTime;

    @BeforeEach
    void setUp() {
        id = 1L;
        username = "Sparta";
        password = "Password123!";
        name = "Sparta Club";
        email = "spartaclub@example.com";
        userInfo = "My name is Sparta.";
        status = Status.ACTIVATE;
        refreshToken = "refreshToken123";
        statusModTime = LocalDateTime.now();

        user = new User(username, password, name, email, userInfo, status, statusModTime);
    }

    // Setter 테스트
    @Test
    @DisplayName("User 엔터티 Setter 테스트")
    void testSetter() {
        // when
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setName(name);
        user.setEmail(email);
        user.setUserInfo(userInfo);
        user.setStatus(status);
        user.setRefreshToken(refreshToken);
        user.setStatusModTime(statusModTime);

        // then
        assertEquals(id, user.getId(), "ID가 설정되지 못했습니다.");
        assertEquals(username, user.getUsername(), "Username이 설정되지 못했습니다.");
        assertEquals(password, user.getPassword(), "Password가 설정되지 못했습니다.");
        assertEquals(name, user.getName(), "Name이 설정되지 못했습니다.");
        assertEquals(email, user.getEmail(), "Email이 설정되지 못했습니다.");
        assertEquals(userInfo, user.getUserInfo(), "User info가 설정되지 못했습니다.");
        assertEquals(status, user.getStatus(), "Status가 설정되지 못했습니다.");
        assertEquals(refreshToken, user.getRefreshToken(), "Refresh token이 설정되지 못했습니다.");
        assertEquals(statusModTime, user.getStatusModTime(), "Status modification time이 설정되지 못했습니다.");
    }

    // Getter 테스트
    @Test
    @DisplayName("User 엔터티 Getter 테스트")
    void testGetter() {
        // when - then
        assertEquals(1L, user.getId(), "ID가 반환되지 못했습니다.");
        assertEquals("Sparta", user.getUsername(), "Username이 반환되지 못했습니다.");
        assertEquals("Password123!", user.getPassword(), "Password가 반환되지 못했습니다.");
        assertEquals("Sparta Club", user.getName(), "Name이 반환되지 못했습니다.");
        assertEquals("spartaclub@example.com", user.getEmail(), "Email이 반환되지 못했습니다.");
        assertEquals("My name is Sparta.", user.getUserInfo(), "User info가 반환되지 못했습니다.");
        assertEquals(Status.ACTIVATE, user.getStatus(), "Status가 반환되지 못했습니다.");
        assertEquals("refreshToken123", user.getRefreshToken(), "Refresh token이 반환되지 못했습니다.");
        assertEquals(statusModTime, user.getStatusModTime(), "Status modification time이 반환되지 못했습니다.");
    }

    // 생성자 테스트
    @Test
    @DisplayName("생성자 테스트")
    void testValidUserConstrcutor() {
        // when
        User user = new User(username, password, name, email, userInfo, status, statusModTime);

        // then
        assertEquals(username, user.getUsername(), "Username이 올바르게 설정되지 않았습니다.");
        assertEquals(password, user.getPassword(), "Password가 올바르게 설정되지 않았습니다.");
        assertEquals(name, user.getName(), "Name이 올바르게 설정되지 않았습니다.");
        assertEquals(email, user.getEmail(), "Email이 올바르게 설정되지 않았습니다.");
        assertEquals(userInfo, user.getUserInfo(), "User info가 올바르게 설정되지 않았습니다.");
        assertEquals(status, user.getStatus(), "Status가 올바르게 설정되지 않았습니다.");
        assertEquals(statusModTime, user.getStatusModTime(), "Status modification time이 올바르게 설정되지 않았습니다.");
    }

    // deleteRefreshToken 테스트
    @Test
    @DisplayName("Refresh Token 삭제 테스트")
    void testDeleteRefreshToken() {
        // given
        user.setRefreshToken(refreshToken);

        // when
        user.deleteRefreshToken();

        // then
        assertEquals("", user.getRefreshToken(), "Refresh Token이 삭제되지 않았습니다.");
    }

    // update 테스트
    @Test
    @DisplayName("update 테스트")
    void update() {
        // given
        String newName = "Sparta_update";
        String newUserInfo = "My name is Spara_update.";
        String newPassword = "Password123!_update";
        LocalDateTime newModifiedAt = LocalDateTime.now();

        // when
        user.update(newName, newUserInfo, newPassword, newModifiedAt);

        // then
        assertEquals(newName, user.getName(), "Name이 업데이트되지 않았습니다.");
        assertEquals(newUserInfo, user.getUserInfo(), "User info가 업데이트되지 않았습니다.");
        assertEquals(newPassword, user.getPassword(), "Password가 업데이트되지 않았습니다.");
        assertEquals(newModifiedAt, user.getModifiedAt(), "Modified time이 업데이트되지 않았습니다.");

    }

    // updateRefreshToken 테스트
    @Test
    @DisplayName("Refresh Token 업데이트 테스트")
    void testUpdateRefreshToken() {
        // given
        user.setRefreshToken(refreshToken);

        String updateToken = "refreshToken456";

        // when
        user.updateRefreshToken(updateToken);

        // then
        assertEquals("refreshToken456", user.getRefreshToken(), "Refresh Token이 업데이트되지 않았습니다.");
    }

}
