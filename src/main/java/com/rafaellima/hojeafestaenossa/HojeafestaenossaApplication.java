package com.rafaellima.hojeafestaenossa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import com.rafaellima.hojeafestaenossa.shared.config.OciProperties;

@ConfigurationPropertiesScan(basePackageClasses = OciProperties.class)
@SpringBootApplication
public class HojeafestaenossaApplication {

	public static void main(String[] args) {
		SpringApplication.run(HojeafestaenossaApplication.class, args);
	}

}
