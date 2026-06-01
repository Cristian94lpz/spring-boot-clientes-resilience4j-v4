package com.example.clientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ExampleClientesV4Application {

	public static void main(String[] args) {
		SpringApplication.run(ExampleClientesV4Application.class, args);
	}

}
