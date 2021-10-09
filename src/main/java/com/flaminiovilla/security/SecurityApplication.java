package com.flaminiovilla.security;

import com.flaminiovilla.security.config.AppProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Log4j2
@EnableSwagger2
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class SecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityApplication.class, args);
	}

	@Component
	public class Runner implements CommandLineRunner {
		@Override
		public void run(String... args) throws Exception {
			log.info("Log4j2 logger works!");

		}
	}
}
