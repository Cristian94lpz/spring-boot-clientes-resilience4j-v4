package com.example.clientes.servicios;

import com.example.clientes.feign.FeignClientPostalCode;
import com.example.clientes.pojo.PostalCode;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


import java.util.List;

@Slf4j
@Service
public class ApiNinjaService {

    private FeignClientPostalCode feignClientPostalCode;
    private RestClient restClient;
    private String url;
    private String apiKey;

    public ApiNinjaService(RestClient.Builder restClientBuilder,
                           FeignClientPostalCode feignClientPostalCode,
                           @Value("${api-ninja.url}") String url,
                           @Value("${api-ninja.api-key}") String apiKey
    ){
        this.url = url;
        this.apiKey = apiKey;
        this.restClient = restClientBuilder.build();
        this.feignClientPostalCode = feignClientPostalCode;
        log.info("ApiKeyNinja : {}",apiKey);
    }


    //https://api.api-ninjas.com/v1/postalcode?postal_code=J0X2G0
    //X-Api-Key:zpsyaNJj4eZJa0IOlimGKQ==m2FKzSnqcZqIJhoP
    public List<PostalCode> getPostalCodeJOX2G0RestClient(){
        ParameterizedTypeReference<List<PostalCode>> typeRef = new ParameterizedTypeReference<List<PostalCode>>() {};
        return this.restClient.mutate()
                .baseUrl(url)
                .build()
                .get()
                .uri("/v1/postalcode?postal_code=J0X2G0")
                .header("x-api-key",apiKey)
                .retrieve()
                .body(typeRef);
    }

    @Retry(name = "postalCodeRetry")
    public List<PostalCode> getPostalCodeJOX2G0RestClientRetry(){
        ParameterizedTypeReference<List<PostalCode>> typeRef = new ParameterizedTypeReference<List<PostalCode>>() {};
        return this.restClient.mutate()
                .baseUrl(url)
                .build()
                .get()
                .uri("/v1/postalcode?postal_code=J0X2G0")
                .header("x-api-key",apiKey)
                .retrieve()
                .body(typeRef);
    }


    @CircuitBreaker(name="postalCodeBreak")
    public List<PostalCode> getPostalCodeJOX2G0RestClientBreak(){
        log.debug("Se ejecuta metodo");
        ParameterizedTypeReference<List<PostalCode>> typeRef = new ParameterizedTypeReference<List<PostalCode>>() {};
        return this.restClient.mutate()
                .baseUrl(url)
                .build()
                .get()
                .uri("/v1/postalcode?postal_code=J0X2G0")
                .header("x-api-key",apiKey)
                .retrieve()
                /*.onStatus(HttpStatusCode::is5xxServerError,((request, response) ->
                {

                    throw new RestClientResponseException(
                            "Error en el servidor externo",
                            response.getStatusCode(),
                            response.getStatusText(),
                            response.getHeaders(),
                            null,null);
                }))*/
                .body(typeRef);
    }

    @RateLimiter(name = "postalCodeRateLimiter")
    public List<PostalCode> getPostalCodeJOX2G0RestClientRateLimiter(){
        ParameterizedTypeReference<List<PostalCode>> typeRef = new ParameterizedTypeReference<List<PostalCode>>() {};
        return this.restClient.mutate()
                .baseUrl(url)
                .build()
                .get()
                .uri("/v1/postalcode?postal_code=J0X2G0")
                .header("x-api-key",apiKey)
                .retrieve()
                .body(typeRef);
    }

    @TimeLimiter(name = "postalCodeTimeLimiter")
    public List<PostalCode> getPostalCodeJOX2G0RestClientTimeLimiter(){
        ParameterizedTypeReference<List<PostalCode>> typeRef = new ParameterizedTypeReference<List<PostalCode>>() {};
        return this.restClient.mutate()
                .baseUrl(url)
                .build()
                .get()
                .uri("/v1/postalcode?postal_code=J0X2G0")
                .header("x-api-key",apiKey)
                .retrieve()
                .body(typeRef);
    }

    @Bulkhead(name = "postalCodeBulkhead")
    public List<PostalCode> getPostalCodeJOX2G0RestClientBulkhead(){
        ParameterizedTypeReference<List<PostalCode>> typeRef = new ParameterizedTypeReference<List<PostalCode>>() {};
        return this.restClient.mutate()
                .baseUrl(url)
                .build()
                .get()
                .uri("/v1/postalcode?postal_code=J0X2G0")
                .header("x-api-key",apiKey)
                .retrieve()
                .body(typeRef);
    }


    //https://api.api-ninjas.com/v1/postalcode?postal_code=J0X2G0
    //X-Api-Key:zpsyaNJj4eZJa0IOlimGKQ==m2FKzSnqcZqIJhoP
    public List<PostalCode> getPostalCodeJOX2G0FeignClient(){
        return this.feignClientPostalCode.getPostalCodeJ0X2G0("J0X2G0");
    }
}
