package tourGuide;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tourGuide.model.location.Attraction;
import tourGuide.proxies.GpsFeignProxy;

import java.util.List;


@SpringBootTest
public class FeignTest {
    @Autowired
    private GpsFeignProxy gpsFeignProxy;

    @Test
    public void testGetAllAttractions() {
        List<Attraction> attractions = gpsFeignProxy.getAttractions();
        System.out.println(attractions);
        Assertions.assertEquals(attractions.size(), 26);
    }
}
