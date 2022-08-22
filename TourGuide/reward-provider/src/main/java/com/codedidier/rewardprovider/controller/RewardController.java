package com.codedidier.rewardprovider.controller;

import com.codedidier.rewardprovider.service.RewardProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class RewardController {
    @Autowired
    private RewardProviderService providerService;

    @GetMapping("/rewardpoints/{attractionId}/{userId}")
    int getAttractionRewardPoints(@PathVariable("attractionId") UUID attractionId, @PathVariable("userId") UUID userId) {
        return providerService.getAttractionRewardPoints(attractionId, userId);
   }
}
