package com.example.clientes.rest;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestClientComponent {

    /*
        Podemos obtener una instancia de RestClient.Builder preconfigurado por Spring
        y modificarlo, para obtener una instancia de RestClient con el metodo build()
     */
    private RestClient.Builder restClientBuilder;

    public RestClientComponent(RestClient.Builder restClientBuilder){
        this.restClientBuilder = restClientBuilder;
    }
}
