package com.codedidier.gpsprovider.controller;

import com.codedidier.gpsprovider.controller.GpsUtilController;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SpringBootTest
public class GpsControllerTest {

    @Autowired
    private GpsUtilController gpsUtilController;

    @BeforeEach
    public void init() {
        Locale.setDefault(Locale.US);
    }
    @Test
    public void testGetAllAttractions() {
        List<Attraction> attractions = gpsUtilController.getAttractions();
        Assertions.assertNotNull(attractions);
        Assertions.assertEquals(attractions.size(), 26);
    }

    @Test
    public void testGetUserLocation() {
        UUID uuid = UUID.randomUUID();
        VisitedLocation userLocation = gpsUtilController.getUserLocation(uuid);
        System.out.println(uuid);//63029982-4a15-413d-acf4-69a7a583afe6
        Assertions.assertNotNull(userLocation);
    }

}
