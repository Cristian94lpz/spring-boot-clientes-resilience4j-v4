package com.example.clientes.servicios;

import com.example.clientes.pojo.PostalCode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

//@Disabled
@Slf4j
@SpringBootTest
public class ApiNinjaServiceTest {

    private WireMockServer wireMockServer;
    private static final int port = 8080;
    private static final String host = "localhost";
    @Value("${api-ninja.api-key}")
    private String apiKey; //System.getenv("API_KEY");

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;

    @Autowired
    private ApiNinjaService apiNinjaService;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("api-ninja.url", () -> "http://"+host+":"+port);

    }



    @BeforeEach
    void setup() {
        // Obtener la instancia del CircuitBreaker por el nombre definido en la anotación
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


        /*
            Si el servidor wiremock se esta ejecutando en la maquina local, osea, localhost,

            new WireMockServer(wireMockConfig()
            .bindAddress("192.168.1.111") Si uso una ip diferente del localhost, sino lo omito
            .port(8080));
            Debo usar el metodo stubFor() desde la instancia no usar el metodo stubFor() estatico
         */
        wireMockServer = new WireMockServer(WireMockConfiguration.options().bindAddress(host).port(port));
        wireMockServer.start();
        /*
            Si el servidor wiremock se esta ejecutando en un host remoto
            debo configurar el host con configureFor() y usar el metodo estatico stubFor()
            configureFor() se usa para configurar el cliente para comunicarse con un servidor wiremock remoto
         */
        //configureFor("https","api.api-ninjas.com",port);

    }

    @AfterEach
    void teardown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    @Disabled
    @Test
    public void testGetPostalCodeJOX2G0RestClient(){
        String URL = "/v1/postalcode?postal_code=J0X2G0";

        //La url especificada en el stub debe ser relativa
        wireMockServer.stubFor(get(URL)
                .withHeader("x-api-key",equalTo(apiKey))
                .willReturn(ok()
                        .withHeader("Content-Type","application/json")
                        .withBody("[{\"city\":\"Luskville\",\"province\":\"QC\",\"timezone\":\"America/Toronto\",\"lat\":\"45.5336\",\"lon\":\"-76.0167\",\"postal_code\":\"J0X 2G0\",\"area_code\":\"819\"}]"))
        );

        List<PostalCode> respuesta = apiNinjaService.getPostalCodeJOX2G0RestClient();


        List<PostalCode> postales = getPostales();

        assertThat(respuesta).isEqualTo(postales);

        /*
            verify() se usa para confirmar que el servidor simulado recibio la
            solicitud HTTP especificada
         */

        wireMockServer.verify(getRequestedFor(urlEqualTo(URL))
                .withHeader("x-api-key",equalTo(apiKey)));


    }

    @Disabled
    @Test
    public void testGetPostalCodeJ0X2G0FeignClient(){
        String URL = "/v1/postalcode?postal_code=J0X2G0";

        wireMockServer.stubFor(get(URL)
                .withHeader("x-api-key",equalTo(this.apiKey))
                .willReturn(ok()
                        .withHeader("Content-Type","application/json")
                        .withBody("[{\"city\":\"Luskville\",\"province\":\"QC\",\"timezone\":\"America/Toronto\",\"lat\":\"45.5336\",\"lon\":\"-76.0167\",\"postal_code\":\"J0X 2G0\",\"area_code\":\"819\"}]"))
        );

        List<PostalCode> respuesta = apiNinjaService.getPostalCodeJOX2G0FeignClient();


        List<PostalCode> postales = getPostales();

        assertThat(respuesta).isEqualTo(postales);

        /*
            verify() se usa para confirmar que el servidor simulado recibio la
            solicitud HTTP especificada
         */

        wireMockServer.verify(getRequestedFor(urlEqualTo(URL))
                .withHeader("x-api-key",equalTo(apiKey)));

    }


    //@Disabled
    @Test
    public void getPostalCodeJOX2G0RestClientBreak() throws InterruptedException {
        String URL = "/v1/postalcode?postal_code=J0X2G0";
        String scenarioName = "circuitoRoto";
        ResponseDefinitionBuilder responsePostales = ok()
                .withHeader("Content-Type","application/json")
                .withBody("[{\"city\":\"Luskville\",\"province\":\"QC\",\"timezone\":\"America/Toronto\",\"lat\":\"45.5336\",\"lon\":\"-76.0167\",\"postal_code\":\"J0X 2G0\",\"area_code\":\"819\"}]");


        createStubForInScenario("stub 1",URL,scenarioName,Scenario.STARTED,"estado 1",1,aResponse().withStatus(503));

        createStubForInScenario("stub 2",URL,scenarioName,"estado 1","estado 2",2,responsePostales);
        //abre el disyuntor
        createStubForInScenario("stub 3",URL,scenarioName,"estado 2","estado 3",3,responsePostales);

        createStubForInScenario("stub 4",URL,scenarioName,"estado 3","estado 4",4,responsePostales);



        List<PostalCode> postales = getPostales();


        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        assertThrows(HttpServerErrorException.ServiceUnavailable.class,() ->  apiNinjaService.getPostalCodeJOX2G0RestClientBreak());

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        assertThat(apiNinjaService.getPostalCodeJOX2G0RestClientBreak()).isEqualTo(postales);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        assertThrows(CallNotPermittedException.class,() ->  apiNinjaService.getPostalCodeJOX2G0RestClientBreak());

        assertThrows(CallNotPermittedException.class,() ->  apiNinjaService.getPostalCodeJOX2G0RestClientBreak());

        assertThrows(CallNotPermittedException.class,() ->  apiNinjaService.getPostalCodeJOX2G0RestClientBreak());

        assertThrows(CallNotPermittedException.class,() ->  apiNinjaService.getPostalCodeJOX2G0RestClientBreak());

        Thread.sleep(3000l);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        assertThat(apiNinjaService.getPostalCodeJOX2G0RestClientBreak()).isEqualTo(postales);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        assertThat(apiNinjaService.getPostalCodeJOX2G0RestClientBreak()).isEqualTo(postales);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        registrarWiremockServeEvents();

        wireMockServer.verify(4,getRequestedFor(urlEqualTo(URL))
                .withHeader("x-api-key",equalTo(apiKey)));

    }

    public void registrarWiremockServeEvents(){
        List<ServeEvent> allEvents = wireMockServer.getAllServeEvents();
        for (ServeEvent event : allEvents) {
            // Verificar si la petición hizo match con algún stub
            if (event.getWasMatched()) {
                log.debug("##########################################################################################");
                log.debug("Date: " + event.getRequest().getLoggedDateString());
                log.debug("Protocolo: " + event.getRequest().getProtocol());
                log.debug("Metodo: " + event.getRequest().getMethod());
                log.debug("URL Solicitada: " + event.getRequest().getUrl());
                log.debug("URL absoluta: " + event.getRequest().getAbsoluteUrl());
                log.debug("Encabezados: " + event.getRequest().getHeaders());

                // Imprimir el ID y nombre del Stub utilizado
                log.debug("Stub ID: " + event.getStubMapping().getId());
                log.debug("Stub Nombre: " + event.getStubMapping().getName());
                log.debug("Stub scenario state: " + event.getStubMapping().getNewScenarioState());
                log.debug("Stub response headers: " + event.getStubMapping().getResponse().getHeaders());
                log.debug("Stub response status: " + event.getStubMapping().getResponse().getStatus());
                log.debug("##########################################################################################");

            } else {
                log.debug("Petición sin coincidencia: " + event.getRequest().getUrl());
            }
        }
    }

    public void registrarCircuitBreakerState(){
        log.debug("Estado circuitbreaker: {}",circuitBreaker.getState().name());
    }

    public List<PostalCode> getPostales(){
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

    public void createStubForInScenario(String stubName,
                                        String url,
                                        String scenario,
                                        String stateIs,
                                        String stateTo,
                                        int priority,
                                        ResponseDefinitionBuilder response){
        wireMockServer.stubFor(get(urlEqualTo(url))
                .withName(stubName)
                .atPriority(priority)
                .withHeader("x-api-key",equalTo(this.apiKey))
                .inScenario(scenario)
                .whenScenarioStateIs(stateIs)
                .willSetStateTo(stateTo)
                .willReturn(response));
    }
}
