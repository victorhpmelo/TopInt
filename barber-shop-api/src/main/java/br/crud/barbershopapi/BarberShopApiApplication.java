package br.crud.barbershopapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BarberShopApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarberShopApiApplication.class, args);

	}
}
