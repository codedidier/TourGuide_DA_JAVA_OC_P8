package com.codedidier.gpsprovider.controller;

import com.codedidier.gpsprovider.service.GpsUtilService;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/gps")
public class GpsUtilController {
    @Autowired
    private GpsUtilService gpsUtilService;

    @GetMapping("/attractions")
    public List<Attraction> getAttractions() {
        return gpsUtilService.getAttractions();
    }

    @GetMapping("/userLocation/{id}")
    public VisitedLocation getUserLocation(@PathVariable UUID id) {
        return gpsUtilService.getUserLocation(id);
    }
}
