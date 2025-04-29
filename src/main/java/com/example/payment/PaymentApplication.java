package com.example.payment;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentApplication.class, args);
	}

	@PostConstruct
	public void logPort() {
		String port = System.getenv("PORT");
		System.out.println(">>> Railway assigned PORT: " + port);
	}
}