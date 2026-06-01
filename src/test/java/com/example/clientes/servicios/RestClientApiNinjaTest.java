package com.example.clientes.servicios;

import com.example.clientes.feign.FeignClientPostalCode;

import com.example.clientes.pojo.PostalCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadAutoConfiguration;
import io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadMetricsAutoConfiguration;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerMetricsAutoConfiguration;
import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterMetricsAutoConfiguration;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryMetricsAutoConfiguration;
import io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterAutoConfiguration;
import io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterMetricsAutoConfiguration;
import lombok.extern.slf4j.Slf4j;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.HttpServerErrorException;

import tools.jackson.databind.json.JsonMapper;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;


/*
    Importante @RestClientTest no sirve para testear el cliente feign, solo carga los componentes
    justos para correr los clientes RestClient y RestTemplate, si necesita cargar componentes o
    propiedades, utilizar @Import y @TestPropertySource.
    El cliente Feign debe ser probado con el contexto completo de Spring, osea , con @SpringBootTest.
    Si la clase de servicio a probar usa el cliente Feign como dependencia interna, debera
    mockearla.
 */
@Disabled
@Slf4j
@RestClientTest(ApiNinjaService.class)
@TestPropertySource("classpath:application.properties")
//@Import()
@ImportAutoConfiguration({
        AopAutoConfiguration.class,
        BulkheadAutoConfiguration.class,
        BulkheadMetricsAutoConfiguration.class,
        RetryAutoConfiguration.class,
        RetryMetricsAutoConfiguration.class,
        CircuitBreakerAutoConfiguration.class,
        CircuitBreakerMetricsAutoConfiguration.class,
        RateLimiterAutoConfiguration.class,
        RateLimiterMetricsAutoConfiguration.class,
        TimeLimiterAutoConfiguration.class,
        TimeLimiterMetricsAutoConfiguration.class,
})
public class RestClientApiNinjaTest {


    @Autowired
    private ApiNinjaService apiNinjaService;

    @Value("${api-ninja.api-key}")
    private String apiKey;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private FeignClientPostalCode feignClientPostalCode;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setup() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("postalCodeBreak");
        CircuitBreakerConfig config = circuitBreaker.getCircuitBreakerConfig();
        log.debug("Configuracion del circuit breaker postalCodeBreak");
        log.debug("sliding-window-type: {}",config.getSlidingWindowType().name());
        log.debug("sliding-window-size: {}",config.getSlidingWindowSize());
        log.debug("failure-rate-treshold: {}",config.getFailureRateThreshold());
        log.debug("minimum-number-of-calls: {}",config.getMinimumNumberOfCalls());
        log.debug("automatic-transition-from-open-to-half-open-enabled: {}",config.isAutomaticTransitionFromOpenToHalfOpenEnabled());
        log.debug("permitted-number-of-calls-in-half-of-open-state: {}",config.getPermittedNumberOfCallsInHalfOpenState());
        log.debug("slow-call-duration-threshold: {}",config.getSlowCallDurationThreshold());
        log.debug("slow-call-rate-threshold: {}",config.getSlowCallRateThreshold());
        log.debug("max-wait-duration-in-half-open-state: {}",config.getMaxWaitDurationInHalfOpenState());
    }

    @Test
    public void testGetPostalCodeJOX2G0RestClientBreak() throws InterruptedException {

        List<PostalCode> postales = getDefaultListPostalCode();
        String jsonPostales = jsonMapper.writeValueAsString(postales);

        /*
            Los mocks se ejecutan en el mismo orden que se definieron a menos que lo especifique.
         */
        String URL = "https://api.api-ninjas.com/v1/postalcode?postal_code=J0X2G0";
        //org.springframework.test.web.client.match.MockRestRequestMatchers
        //org.springframework.test.web.client.response.MockRestResponseCreators
        mockServer.expect(ExpectedCount.once(),requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-api-key",apiKey))
                .andRespond(MockRestResponseCreators.withServiceUnavailable());

        mockServer.expect(ExpectedCount.once(),requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("postal_code","J0X2G0"))
                .andExpect(header("x-api-key",apiKey))
                .andRespond(MockRestResponseCreators.withSuccess(jsonPostales, MediaType.APPLICATION_JSON));
        //Se abre disyuntor
        mockServer.expect(ExpectedCount.times(2),requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("postal_code","J0X2G0"))
                .andExpect(header("x-api-key",apiKey))
                .andRespond(MockRestResponseCreators.withSuccess(jsonPostales, MediaType.APPLICATION_JSON));



        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        //Falla
        assertThrows(HttpServerErrorException.ServiceUnavailable.class,() ->  apiNinjaService.getPostalCodeJOX2G0RestClientBreak());
        //Exito
        assertThat(apiNinjaService.getPostalCodeJOX2G0RestClientBreak()).isEqualTo(postales);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        //Falla
        assertThrows(CallNotPermittedException.class,() ->  apiNinjaService.getPostalCodeJOX2G0RestClientBreak());
        //Falla
        assertThrows(CallNotPermittedException.class,() ->  apiNinjaService.getPostalCodeJOX2G0RestClientBreak());
        //Falla
        assertThrows(CallNotPermittedException.class,() ->  apiNinjaService.getPostalCodeJOX2G0RestClientBreak());
        //Falla
        assertThrows(CallNotPermittedException.class,() ->  apiNinjaService.getPostalCodeJOX2G0RestClientBreak());

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Thread.sleep(3000l);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        //Falla
        assertThat(apiNinjaService.getPostalCodeJOX2G0RestClientBreak()).isEqualTo(postales);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        //Exito
        assertThat(apiNinjaService.getPostalCodeJOX2G0RestClientBreak()).isEqualTo(postales);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);



        mockServer.verify();

        /*
            Conclusion:
            Si se activa el calculo de la tasa de fallo y esta iguala el failure-rate (no hace falta que la
            supere), el disyuntor pasara a estado OPEN, el resto de las solicitudes que se hagan fallaran
            inmediatamente con CallNotPermittedException sin ejecutar el metodo. Luego de espera el tiempo
            wait-duration-in-open-state, el disyuntor bajo esta configuracion seguira en estado OPEN hasta
            que llegue una solicitud. Llegada la solicitud pasa a estado HALF_OPEN. Se permitira un numero
            permitted-number-of-calls-in-half-open-state de solicitudes para que sirvan al calculo de la tasa
            de fallos, el cual se activara si minimum-number-of-calls se realizan, en base a ello se determinara
            si se pasa al estado CLOSED o se vuelve a OPEN. Si permitted-number-of-calls-in-half-open-state es
            menor que minimum-number-of-calls y la llamada falla, el disyuntor vuelve al estado OPEN.
         */
    }

    public List<PostalCode> getDefaultListPostalCode(){
        PostalCode defaultPostalCode = new PostalCode();
        defaultPostalCode.setTimezone("America/Toronto");
        defaultPostalCode.setPostalCode("J0X 2G0");
        defaultPostalCode.setAreaCode("819");
        defaultPostalCode.setLon("-76.0167");
        defaultPostalCode.setLat("45.5336");
        defaultPostalCode.setProvince("QC");
        defaultPostalCode.setCity("Luskville");
        return new ArrayList<>(Arrays.asList(defaultPostalCode));
    }

    public void registrarCircuitBreakerState(){
        log.debug("Estado circuitbreaker: {}",circuitBreaker.getState().name());
    }
}
