package com.vv.personal.twm.tribalwars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author Vivek
 * @since 29/11/20
 * <p>
 * Access point changed to <__>:23555/swagger-ui/index.html
 */
@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication
public class TribalWarsServer {

    public static void main(String[] args) {
        SpringApplication.run(TribalWarsServer.class, args);
    }

    @Bean
    ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.vv.personal.twm.tribalwars"))
                .build();
    }
}
