package com.codedidier;

import com.codedidier.pricerprovider.controller.PricerController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

@SpringBootTest
public class PricerControllerTest {
    @Autowired
    private PricerController pricerController;

    @Test
    public void testGetProviders() {
        UUID uuid = UUID.randomUUID();
        System.out.println("uuid = " + uuid);//uuid = 45f928e7-b54c-4d9c-a297-fdddb7ddfacc
        List<Provider> providers = pricerController.getPrice("test", uuid, 2, 3, 3, 3);
        Assertions.assertNotNull(providers);
        Assertions.assertEquals(providers.size(), 5);
    }
}
