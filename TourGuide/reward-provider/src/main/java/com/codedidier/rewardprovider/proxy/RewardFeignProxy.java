package com.codedidier.rewardprovider.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(value = "reward-provider", url = "localhost:8082")
public interface RewardFeignProxy {
    @GetMapping("/rewardpoints/{attractionId}/{userId}")
    int getAttractionRewardPoints(@PathVariable("attractionId") UUID attractionId, @PathVariable("userId") UUID userId);

}
