package com.example.clientes.feign;

import feign.hc5.ApacheHttp5Client;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GlobalFeignConfiguration {

    @Bean
    public ApacheHttp5Client client(){
        /*
            Podemos cambiar la version del protocolo HTTP configurando un TlsConfig con
            setVersionPolicy(), los valores posibles son:
            NEGOTIATE (negocia con el navegador que protocolo HTTP utilizar)
            FORCE_HTTP_1
            FORCE_HTTP_2
         */
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
                        .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                        .build())
                .setDefaultConnectionConfig(connConfig)
                .build();

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
        return new ApacheHttp5Client(httpClient);
    }

}
