package tourGuide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.Locale;

@SpringBootApplication
@EnableFeignClients("TourGuide")
public class Application {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        SpringApplication.run(Application.class, args);
    }

}
