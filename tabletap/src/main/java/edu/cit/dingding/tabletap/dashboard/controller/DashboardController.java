package edu.cit.dingding.tabletap.dashboard.controller;

import edu.cit.dingding.tabletap.dashboard.dto.DashboardResponse;
import edu.cit.dingding.tabletap.user.model.Role;
import edu.cit.dingding.tabletap.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/diner")
    public DashboardResponse diner(@AuthenticationPrincipal Jwt jwt) {
        return response(jwt, Role.DINER);
    }

    @GetMapping("/staff")
    public DashboardResponse staff(@AuthenticationPrincipal Jwt jwt) {
        return response(jwt, Role.STAFF);
    }

    @GetMapping("/owner")
    public DashboardResponse owner(@AuthenticationPrincipal Jwt jwt) {
        return response(jwt, Role.OWNER);
    }

    private DashboardResponse response(Jwt jwt, Role role) {
        var user = userService.findById(UUID.fromString(jwt.getSubject()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<String> features = switch (role) {
            case DINER -> List.of("Browse restaurants", "Manage reservations", "View dining history");
            case STAFF -> List.of("Manage reservations", "View floor plan", "Update table status");
            case OWNER -> List.of("Manage floor plans", "Manage tables", "View restaurant operations");
        };
        return new DashboardResponse(role.name(), "Welcome, " + user.getFullName(), user.getFullName(), features);
    }
}
