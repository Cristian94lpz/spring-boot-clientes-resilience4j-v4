package com.example.clientes.configuraciones.resilience;



import org.springframework.context.annotation.Configuration;


@Configuration
public class CircuitBreakConfiguration {

    /*
        Configurar programaticamente un CircuitBreaker personalizado
     */
    /*
    @Bean
    public CircuitBreaker customCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .minimumNumberOfCalls(5)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.ofSeconds(5l))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slowCallRateThreshold(100)
                .slowCallDurationThreshold(Duration.ofSeconds(60))
                .maxWaitDurationInHalfOpenState(Duration.ofNanos(0))
                .build();

        return CircuitBreakerRegistry.of(config)
                .circuitBreaker("customCircuitBreaker");
    }

    @Bean
    public Retry customRetry(){
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1l))
                .failAfterMaxAttempts(false)
                .build();
        return RetryRegistry.of(config)
                .retry("customRetry");
    }

    @Bean
    public RateLimiter customRateLimiter(){
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .timeoutDuration(Duration.ofNanos(0l))
                .build();
        return RateLimiterRegistry.of(config)
                .rateLimiter("customRateLimiter");
    }

    @Bean
    public TimeLimiter customTimeLimiter(){
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(2))
                .cancelRunningFuture(true)
                .build();
        return TimeLimiterRegistry.of(config)
                .timeLimiter("customTimeLimiter");
    }

*/
}
