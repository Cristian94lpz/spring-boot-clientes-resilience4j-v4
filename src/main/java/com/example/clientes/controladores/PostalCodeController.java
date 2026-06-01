package com.example.clientes.controladores;

import com.example.clientes.pojo.PostalCode;
import com.example.clientes.servicios.ApiNinjaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@ResponseBody
@RequestMapping("/postal-code")
public class PostalCodeController {

    private ApiNinjaService apiNinjaService;

    public PostalCodeController(ApiNinjaService apiNinjaService){
        this.apiNinjaService = apiNinjaService;
    }

    @GetMapping("/rest-client/J0X2G0")
    public List<PostalCode> getPostalCodeRestClient(){
        return this.apiNinjaService.getPostalCodeJOX2G0RestClient();
    }

    @GetMapping("/feign/J0X2G0")
    public List<PostalCode> getPostalCodeFeignClient(){
        return this.apiNinjaService.getPostalCodeJOX2G0FeignClient();
    }
}
