package com.greenops.admin;

import com.greenops.auth.Role;
import com.greenops.auth.User;
import com.greenops.auth.UserRepository;
import com.greenops.auth.UserStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AdminControllerTest {

    @Test
    void shouldListUsersWithActiveFlagMappedFromUserStatus() {
        UserRepository userRepo = Mockito.mock(UserRepository.class);
        SystemCoefficientRepository coefRepo = Mockito.mock(SystemCoefficientRepository.class);

        User activeUser = new User();
        activeUser.setId(1L);
        activeUser.setEmail("dev@greenops.io");
        activeUser.setRole(Role.ROLE_DEV);
        activeUser.setStatus(UserStatus.ACTIVE);

        User pendingUser = new User();
        pendingUser.setId(2L);
        pendingUser.setEmail("new@greenops.io");
        pendingUser.setRole(Role.ROLE_USER);
        pendingUser.setStatus(UserStatus.PENDING);

        when(userRepo.findAll()).thenReturn(List.of(activeUser, pendingUser));

        AdminController controller = new AdminController(userRepo, coefRepo);
        List<?> result = controller.listUsers();

        assertEquals(2, result.size());
        Map<?, ?> first = (Map<?, ?>) result.get(0);
        Map<?, ?> second = (Map<?, ?>) result.get(1);
        assertEquals(true, first.get("active"));
        assertEquals(false, second.get("active"));
    }

    @Test
    void shouldUpdateUserRoleAndStatus() {
        UserRepository userRepo = Mockito.mock(UserRepository.class);
        SystemCoefficientRepository coefRepo = Mockito.mock(SystemCoefficientRepository.class);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@greenops.io");
        user.setRole(Role.ROLE_USER);
        user.setStatus(UserStatus.PENDING);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminController controller = new AdminController(userRepo, coefRepo);

        ResponseEntity<?> roleResp = controller.updateRole(1L, Map.of("role", "ROLE_INFRA"));
        assertEquals(HttpStatus.OK, roleResp.getStatusCode());
        assertEquals(Role.ROLE_INFRA, user.getRole());

        ResponseEntity<?> statusResp = controller.updateStatus(1L, Map.of("active", true));
        assertEquals(HttpStatus.OK, statusResp.getStatusCode());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void shouldRejectNonPositiveCoefficientUpdate() {
        UserRepository userRepo = Mockito.mock(UserRepository.class);
        SystemCoefficientRepository coefRepo = Mockito.mock(SystemCoefficientRepository.class);
        AdminController controller = new AdminController(userRepo, coefRepo);

        ResponseEntity<?> resp = controller.updateCoefficient("DEFAULT_PUE", Map.of("paramValue", -1.5));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}