package tourGuide.proxies;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import tourGuide.model.location.Attraction;

import java.util.List;
import java.util.UUID;

@Component
@FeignClient(value = "gps-provider", url = "${gps.serviceUrl}")
public interface GpsFeignProxy {
    @GetMapping("/gps/attractions")
    List<Attraction> getAttractions();

    @PostMapping("/gps/userLocation/{id}")
    VisitedLocation getUserLocation(@PathVariable UUID id);
}
