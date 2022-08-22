package com.codedidier.rewardprovider.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class RewardControllerTest {
    @Autowired
    private RewardController rewardController;

    @Test
    public void testGetRewardPoints() {
        UUID uuid1 = UUID.randomUUID();
        System.out.println("uuid1 = " + uuid1);//uuid1 = 3e134199-fe04-41c7-989f-d353c1c16c65
        UUID uuid2 = UUID.randomUUID();
        System.out.println("uuid2 = " + uuid2);//uuid2 = e76d2799-c84e-477e-a186-1874b36ce647
        int rewardPoints = rewardController.getAttractionRewardPoints(uuid1, uuid2);
        System.out.println("rewardPoints = " + rewardPoints);//rewardPoints = 323
        Assertions.assertNotNull(rewardPoints);
    }
}
