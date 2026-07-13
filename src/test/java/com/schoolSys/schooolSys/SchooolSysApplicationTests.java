package com.schoolSys.schooolSys;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_AVAILABLE", matches = "true")
class SchooolSysApplicationTests {

	@Test
	void contextLoads() {
	}

}
