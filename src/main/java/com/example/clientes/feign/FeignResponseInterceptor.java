package com.example.clientes.feign;

import feign.InvocationContext;
import feign.ResponseInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignResponseInterceptor implements ResponseInterceptor {


    @Override
    public Object intercept(InvocationContext invocationContext, Chain chain) throws Exception {
        log.info("FeignResponseInterceptor status code: " + invocationContext.response().status());
        return invocationContext.proceed();
    }



}
