package com.example.clientes.feign;

import feign.Logger;
import feign.RequestInterceptor;
import feign.ResponseInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@PropertySource("classpath:application.properties")
public class FeignCustomConfiguration {

    private String apikey;

    public FeignCustomConfiguration(@Value("${api-ninja.api-key}") String apiKey){
        this.apikey = apiKey;
    }

    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            log.info("Request interceptor add x-api-key");
            requestTemplate.header("x-api-key",apikey );
        };
    }

    @Bean
    public ResponseInterceptor responseInterceptor(){
        return new FeignResponseInterceptor();
    }

    @Bean
    public ErrorDecoder errorDecoder(){
        return new CustomErrorDecoder();
    }
}
