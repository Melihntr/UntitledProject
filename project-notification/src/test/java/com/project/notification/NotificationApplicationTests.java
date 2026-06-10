package com.project.notification;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
class NotificationApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainDelegatesToSpringApplication() {
		String[] args = {"--test=true"};

		try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
			NotificationApplication.main(args);
			springApplication.verify(() -> SpringApplication.run(NotificationApplication.class, args));
		}
	}

}
