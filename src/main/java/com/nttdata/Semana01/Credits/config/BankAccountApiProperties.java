package com.nttdata.Semana01.Credits.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Setter
@Getter
@ConfigurationProperties(prefix = "bank-account-api")
public class BankAccountApiProperties {
	private String baseUrl;
}
