package com.example.clientes.rest;

import com.example.clientes.interceptores.RestClientRequestInterceptor;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;


import java.time.Duration;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestClientConfiguration {

    /*
        Podemos usar una instancia de RestTemplate como argumento del metodo create() para
        pasar las configuraciones a la nueva instancia de RestClient.
     */
    /*
    @Bean
    public RestClient restClient(){
        return RestClient.create();
    }
    */
    @Bean
    @Primary
    public CloseableHttpClient httpClient(){
        ConnectionConfig connConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(4000l))//Determina el tiempo de espera hasta que se establezca completamente una nueva conexión.
                .setSocketTimeout(Timeout.ofMilliseconds(4000l))//Determina el valor de tiempo de espera predeterminado del socket para las operaciones de E/S en las conexiones creadas por esta configuración.
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(4000l))//Devuelve el tiempo de espera de la solicitud de arrendamiento de conexión que se utiliza al solicitar una conexión al administrador de conexiones.
                .setResponseTimeout(5000, TimeUnit.MILLISECONDS)//Tiempo max para esperar los datos
                .build();
        PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)           // Total de conexiones en el pool
                .setMaxConnPerRoute(20)// Maximo numero de conexiones por host de destino
                .setTlsConfigResolver(host -> TlsConfig.custom()
                        .setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_1)
                        .build())
                .setDefaultConnectionConfig(connConfig)
                .build();
        DefaultHttpRequestRetryStrategy requestRetryStrategy = new DefaultHttpRequestRetryStrategy(0, TimeValue.of(Duration.ofNanos(0)));


        return HttpClientBuilder.create()
                .setRetryStrategy(requestRetryStrategy)
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }


    @Bean
    @Primary
    public RestClient.Builder restClientBuilderCustom(JsonMapper jsonMapper){
        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient()))
                .configureMessageConverters(converters -> converters.addCustomConverter(new JacksonJsonHttpMessageConverter(jsonMapper)))
                .requestInterceptor(new RestClientRequestInterceptor());
    }


}
