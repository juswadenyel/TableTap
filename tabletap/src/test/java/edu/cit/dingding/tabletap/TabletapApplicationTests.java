package edu.cit.dingding.tabletap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:tabletap-context-test;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jwt.secret=context-test-secret-that-is-longer-than-thirty-two-characters",
		"app.jwt.expiration-minutes=60",
		"app.seed.owner-email=",
		"app.seed.owner-password=",
		"app.seed.staff-email=",
		"app.seed.staff-password="
})
class TabletapApplicationTests {

	@Test
	void contextLoads() {
	}

}
