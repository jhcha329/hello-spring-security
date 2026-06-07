package kr.ac.hansung.service;

import kr.ac.hansung.entity.User;
import kr.ac.hansung.repository.RoleRepository;
import kr.ac.hansung.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@DisplayName("UserService 테스트")
class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserService userService = new UserService(userRepository, roleRepository, passwordEncoder);

    @Test
    @DisplayName("현재 비밀번호가 맞으면 새 비밀번호로 변경한다")
    void changePassword_success() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword(passwordEncoder.encode("oldPassword"));

        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));

        userService.changePassword("user@test.com", "oldPassword", "newPassword");

        then(userRepository).should().save(user);
        assertThat(passwordEncoder.matches("newPassword", user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("현재 비밀번호가 다르면 예외가 발생한다")
    void changePassword_wrongCurrentPassword() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword(passwordEncoder.encode("oldPassword"));

        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changePassword("user@test.com", "wrongPassword", "newPassword"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("현재 비밀번호가 일치하지 않습니다.");

        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("사용자를 찾을 수 없으면 예외가 발생한다")
    void changePassword_userNotFound() {
        given(userRepository.findByEmail("missing@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword("missing@test.com", "oldPassword", "newPassword"))
            .isInstanceOf(UsernameNotFoundException.class);
    }
}
