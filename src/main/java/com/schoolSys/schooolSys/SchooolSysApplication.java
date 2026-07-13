package com.schoolSys.schooolSys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the School Management System.
 * <p>
 * This is a multi-tenant application where each school has its own
 * PostgreSQL schema. The tenant is identified by the {@code X-Tenant-ID}
 * HTTP header on each request.
 * </p>
 */
@SpringBootApplication
@EnableAsync
public class SchooolSysApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchooolSysApplication.class, args);
	}
}
