package tourGuide;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.location.Attraction;
import tourGuide.model.location.Location;
import tourGuide.model.tripPricer.Provider;
import tourGuide.proxies.GpsFeignProxy;
import tourGuide.proxies.PricerFeignProxy;
import tourGuide.proxies.RewardFeignProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;

import javax.money.Monetary;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TestTourGuideService {

	@Autowired
	private GpsFeignProxy gpsFeignProxy;

	@Autowired
	private RewardFeignProxy rewardFeignProxy;

	@Autowired
	private PricerFeignProxy pricerFeignProxy;
	@BeforeEach
	public void init() {
		Locale.setDefault(Locale.US);
	}

	@Test
	public void getUserLocation() {
		RewardsService rewardsService = new RewardsService(gpsFeignProxy, rewardFeignProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsFeignProxy, rewardsService, pricerFeignProxy);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();
		assertTrue(user.getLastVisitedLocation().userId.equals(user.getUserId()));
	}
	
	@Test
	public void addUser() {
		RewardsService rewardsService = new RewardsService(gpsFeignProxy, rewardFeignProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsFeignProxy, rewardsService, pricerFeignProxy);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();
		
		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}
	
	@Test
	public void getAllUsers() {
		RewardsService rewardsService = new RewardsService(gpsFeignProxy, rewardFeignProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsFeignProxy,rewardsService, pricerFeignProxy);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	@Test
	public void testGetAllCurrentLocations() {
		RewardsService rewardsService = new RewardsService(gpsFeignProxy, rewardFeignProxy);
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsFeignProxy, rewardsService, pricerFeignProxy);

		Map<String, Location> allCurrentLocations = tourGuideService.getAllCurrentLocations();
		assertEquals(100, allCurrentLocations.size());
	}

	@Test
	public void testUpdateUserPreferences() {
		RewardsService rewardsService = new RewardsService(gpsFeignProxy, rewardFeignProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsFeignProxy, rewardsService, pricerFeignProxy);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.addUser(user);

		//1, Set an old UserPreferences:
		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setTicketQuantity(2);
		userPreferences.setTripDuration(3);
		userPreferences.setLowerPricePoint(Money.of(500, Monetary.getCurrency("USD")));
		userPreferences.setHighPricePoint(Money.of(1000, Monetary.getCurrency("USD")));
		userPreferences.setAttractionProximity(10);
		userPreferences.setNumberOfAdults(2);
		userPreferences.setNumberOfChildren(2);

		user.setUserPreferences(userPreferences);

		//2, Set a new UserPreferencesDto:
		UserPreferencesDto userPreferencesDto = new UserPreferencesDto();
		//3, Username should not be changed:
		userPreferencesDto.setUserName(user.getUserName());

		//4, Update only what is provided:
		userPreferencesDto.setNumberOfChildren(4);
		userPreferencesDto.setNumberOfAdults(4);
		userPreferencesDto.setTripDuration(5);

		tourGuideService.updateUserPreferences(userPreferencesDto);

		assertEquals(user.getUserPreferences().getNumberOfAdults(), 4);
		assertEquals(user.getUserPreferences().getNumberOfChildren(), 4);
		assertEquals(user.getUserPreferences().getTripDuration(), 5);
	}
	@Test
	public void trackUser() {
		RewardsService rewardsService = new RewardsService(gpsFeignProxy, rewardFeignProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsFeignProxy, rewardsService, pricerFeignProxy);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();

		
		assertEquals(user.getUserId(), user.getLastVisitedLocation().userId);
	}
	
// Not yet implemented
	@Test
	public void getNearbyAttractions() {
		RewardsService rewardsService = new RewardsService(gpsFeignProxy, rewardFeignProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsFeignProxy, rewardsService, pricerFeignProxy);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user);
		
		List<Attraction> attractions = tourGuideService.getNearByAttractions(user.getLastVisitedLocation());
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, attractions.size());
	}
	@Test
	public void getTripDeals() {
		RewardsService rewardsService = new RewardsService(gpsFeignProxy, rewardFeignProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsFeignProxy, rewardsService, pricerFeignProxy);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, providers.size());
	}
	
	
}
