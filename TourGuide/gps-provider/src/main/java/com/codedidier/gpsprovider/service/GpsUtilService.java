package com.codedidier.gpsprovider.service;


import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GpsUtilService{
    @Autowired
    private GpsUtil gpsUtil;

    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
   }

    public VisitedLocation getUserLocation(UUID userId) {
        return gpsUtil.getUserLocation(userId);
    }
}
