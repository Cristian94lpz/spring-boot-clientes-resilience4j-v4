package com.example.clientes.feign;

import com.example.clientes.pojo.PostalCode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "postal-code",
        url = "${api-ninja.url}",
        configuration = FeignCustomConfiguration.class
)
public interface FeignClientPostalCode {


    // postalcode?postal_code=J0X2G0
    @GetMapping("/v1/postalcode")
    public List<PostalCode> getPostalCodeJ0X2G0(@RequestParam(name = "postal_code",defaultValue = "J0X2G0") String postalCode);
}
