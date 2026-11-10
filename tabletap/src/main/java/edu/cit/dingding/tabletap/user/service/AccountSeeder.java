package edu.cit.dingding.tabletap.user.service;

import edu.cit.dingding.tabletap.user.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// This component seeds optional owner and staff accounts at startup.
@Component
public class AccountSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AccountSeeder.class);

    private final UserService userService;
    private final String ownerEmail;
    private final String ownerPassword;
    private final String staffEmail;
    private final String staffPassword;

    public AccountSeeder(
            UserService userService,
            @Value("${app.seed.owner-email:}") String ownerEmail,
            @Value("${app.seed.owner-password:}") String ownerPassword,
            @Value("${app.seed.staff-email:}") String staffEmail,
            @Value("${app.seed.staff-password:}") String staffPassword
    ) {
        this.userService = userService;
        this.ownerEmail = ownerEmail;
        this.ownerPassword = ownerPassword;
        this.staffEmail = staffEmail;
        this.staffPassword = staffPassword;
    }

    @Override
    public void run(String... args) {
        seedAccount(ownerEmail, ownerPassword, Role.OWNER);
        seedAccount(staffEmail, staffPassword, Role.STAFF);
    }

    private void seedAccount(String email, String password, Role role) {
        if (email.isBlank() || password.isBlank() || userService.findByEmail(email).isPresent()) {
            return;
        }

        userService.createWithRole(role.name(), email, password, role);
        logger.info("Seeded {} account", role);
    }
}
