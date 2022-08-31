package tourGuide.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tourGuide.model.location.Attraction;
import tourGuide.model.location.VisitedLocation;

import java.util.List;
import java.util.UUID;

@Component
@FeignClient(value = "gps-provider", url = "localhost:8181")
public interface GpsFeignProxy {
    @GetMapping("/gps/attractions")
    List<Attraction> getAttractions();

    @GetMapping("/gps/userLocation/{id}")
    VisitedLocation getUserLocation(@PathVariable UUID id);
}
