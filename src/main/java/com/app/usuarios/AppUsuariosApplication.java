/*****************************************************************************
********	MICROSERVICES WITH SPRING BOOT				******
********	DEVELOPED BY: SANTIAGO GUERRERO				******
********	FROM UNIVERSITY OF ANTIOQUIA				******
*****************************************************************************/
package com.app.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableEurekaClient
@EnableFeignClients
@SpringBootApplication
public class AppUsuariosApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppUsuariosApplication.class, args);
	}

}
