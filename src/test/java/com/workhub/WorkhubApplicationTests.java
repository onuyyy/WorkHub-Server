package com.workhub;

import com.workhub.config.TestAwsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestAwsConfig.class)
class WorkhubApplicationTests {

	@Test
	void contextLoads() {
	}

}
