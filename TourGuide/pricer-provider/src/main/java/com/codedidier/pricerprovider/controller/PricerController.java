package com.codedidier.pricerprovider.controller;

import com.codedidier.pricerprovider.service.PriceProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

@RestController
public class PricerController {

    @Autowired
    private PriceProviderService priceProviderService;

    @GetMapping("/price/{apiKey}/{attractionId}/{adults}/{children}/{nightsStay}/{rewardsPoints}")
    public List<Provider> getPrice(@PathVariable("apiKey") String apiKey, @PathVariable("attractionId") UUID attractionId, @PathVariable("adults") int adults, @PathVariable("children") int children, @PathVariable("nightsStay") int nightsStay, @PathVariable("rewardsPoints") int rewardsPoints) {
        return priceProviderService.getPrice(apiKey, attractionId, adults, children, nightsStay, rewardsPoints);
    }

}
