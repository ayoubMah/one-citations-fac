package org.gso.citations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class CitationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CitationsApplication.class, args);
	}

}
