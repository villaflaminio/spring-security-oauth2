package com.flaminiovilla.security;

import com.flaminiovilla.security.config.AppProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Log4j2
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@SecurityScheme(name = "elis-api", scheme = "basic", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@OpenAPIDefinition(info = @Info(title = "Spring security API", version = "3.0", description = "Api testing for base spring security"))
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
