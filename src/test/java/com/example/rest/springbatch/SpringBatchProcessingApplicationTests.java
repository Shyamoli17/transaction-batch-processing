package com.example.rest.springbatch;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
class SpringBatchProcessingApplicationTests {

	@Test
	void contextLoads() {
		log.info("BatchServiceApplicationTests - ok");
	}

}
