package com.cas.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.cas", "com.campassistant"})
@EntityScan(basePackages = {"com.cas.login.model", "com.campassistant.model", "com.cas.asistencias.model", "com.cas.packinglist.model"})
@EnableJpaRepositories(basePackages = {"com.cas.login.repository", "com.campassistant.repository", "com.cas.asistencias.repository", "com.cas.packinglist.repository"})
public class LoginServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoginServiceApplication.class, args);
	}

}
