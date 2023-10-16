package com.ikubinfo.plumbershop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;


@SpringBootApplication
@EnableMongoAuditing
public class PlumberShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlumberShopApplication.class, args);
	}


}
