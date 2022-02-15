package com.penguinwebstudio.buildings;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.penguinwebstudio.building.BuildingController;
import com.penguinwebstudio.logger.LogService;
import com.penguinwebstudio.user.UserController;

@SpringBootApplication
@ComponentScan(basePackageClasses=BuildingController.class)
@ComponentScan(basePackageClasses=UserController.class)
@ComponentScan(basePackageClasses=WebSecurityConfig.class)
@ComponentScan(basePackageClasses=LogService.class)
@EntityScan("com.penguinwebstudio.building")
@EnableJpaRepositories("com.penguinwebstudio.building")
public class BuildingsApplication {

	@Autowired
	private DataSource dataSource;
	
	public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BuildingsApplication.class.getPackage().getName());
		SpringApplication.run(BuildingsApplication.class, args);
	}

}
