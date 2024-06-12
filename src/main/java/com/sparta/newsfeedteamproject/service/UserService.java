package com.sparta.newsfeedteamproject.service;

import com.sparta.newsfeedteamproject.dto.user.ProfileResDto;
import com.sparta.newsfeedteamproject.dto.user.SignupReqDto;
import com.sparta.newsfeedteamproject.dto.user.UpdateReqDto;
import com.sparta.newsfeedteamproject.dto.user.UserAuthReqDto;
import com.sparta.newsfeedteamproject.entity.Status;
import com.sparta.newsfeedteamproject.entity.User;
import com.sparta.newsfeedteamproject.exception.ExceptionMessage;
import com.sparta.newsfeedteamproject.repository.UserRepository;
import com.sparta.newsfeedteamproject.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupReqDto reqDto) {

        String username = reqDto.getUsername();
        String password = passwordEncoder.encode(reqDto.getPassword());
        String name = reqDto.getName();
        String email = reqDto.getEmail();
        String userInfo = reqDto.getUserInfo();

        Optional<User> checkUsername = userRepository.findByUsername(username);
        if (checkUsername.isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DUPLICATE_USERNAME.getExceptionMessage());
        }

        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException(ExceptionMessage.DUPLICATE_EMAIL.getExceptionMessage());
        }

        Status status = Status.ACTIVATE;
        LocalDateTime statusModTime = LocalDateTime.now();

        User user = new User(username, password, name, email, userInfo, status, statusModTime);
        userRepository.save(user);
    }

    @Transactional
    public void withdraw(Long userId, UserAuthReqDto reqDto, UserDetailsImpl userDetails) {
        String username = userDetails.getUser().getUsername();

        User loginUser = findByUsername(username);
        User checkUser = findById(userId);

        if (!loginUser.getUsername().equals(checkUser.getUsername())) {
            throw new IllegalArgumentException(ExceptionMessage.INCORRECT_USER.getExceptionMessage());
        }

        String password = userDetails.getUser().getPassword();

        if (!passwordEncoder.matches(reqDto.getPassword(), password)) {
            throw new IllegalArgumentException(ExceptionMessage.INCORRECT_PASSWORD.getExceptionMessage());
        }

        if (checkUser.getStatus() == Status.DEACTIVATE) {
            throw new IllegalArgumentException(ExceptionMessage.DEATIVATE_USER.getExceptionMessage());
        }

        checkUser.setStatus(Status.DEACTIVATE);
        checkUser.setStatusModTime(LocalDateTime.now());

        userRepository.save(checkUser);
        logout(checkUser.getId(), userDetails);
    }

    @Transactional
    public void logout(Long userId, UserDetailsImpl userDetails) {
        User user = findById(userId);
        User jwtUser = userDetails.getUser();
        if (!user.getId().equals(jwtUser.getId())) {
            throw new IllegalArgumentException(ExceptionMessage.INCORRECT_USER.getExceptionMessage());
        }
        user.deleteRefreshToken();
    }

    public ProfileResDto getProfile(Long userId) {
        User checkUser = findById(userId);

        if (checkUser.getStatus().equals(Status.DEACTIVATE)) {
            throw new IllegalArgumentException(ExceptionMessage.DEATIVATE_USER.getExceptionMessage());
        }
        return new ProfileResDto(checkUser);
    }

    public ProfileResDto editProfile(Long userId, UpdateReqDto reqDto, UserDetailsImpl userDetails) {

        String username = userDetails.getUser().getUsername();

        User loginUser = findByUsername(username);
        User checkUser = findById(userId);

        if (!loginUser.getUsername().equals(checkUser.getUsername())) {
            throw new IllegalArgumentException(ExceptionMessage.INCORRECT_USER.getExceptionMessage());
        }

        if (checkUser.getStatus().equals(Status.DEACTIVATE)) {
            throw new IllegalArgumentException(ExceptionMessage.DEATIVATE_USER.getExceptionMessage());
        }

        String password = userDetails.getUser().getPassword();

        if (!passwordEncoder.matches(reqDto.getPassword(), password)) {
            throw new IllegalArgumentException(ExceptionMessage.INCORRECT_PASSWORD.getExceptionMessage());
        }

        if (reqDto.getNewPassword().equals(reqDto.getPassword())) {
            throw new IllegalArgumentException(ExceptionMessage.SAME_PASSWORD.getExceptionMessage());
        }

        String name = reqDto.getNewName();
        String userInfo = reqDto.getNewUserInfo();
        String newPassword = passwordEncoder.encode(reqDto.getNewPassword());
        LocalDateTime modifiedAt = LocalDateTime.now();

        checkUser.update(name, userInfo, newPassword, modifiedAt);
        userRepository.save(checkUser);

        return new ProfileResDto(checkUser);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException(ExceptionMessage.NOT_FOUND_USER.getExceptionMessage())
        );
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException(ExceptionMessage.NOT_FOUND_USER.getExceptionMessage())
        );
    }

    private User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException(ExceptionMessage.NOT_FOUND_USER.getExceptionMessage())
        );
    }
}
