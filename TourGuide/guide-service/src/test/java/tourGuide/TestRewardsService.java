package tourGuide;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.location.Attraction;
import tourGuide.model.location.VisitedLocation;
import tourGuide.proxies.GpsFeignProxy;
import tourGuide.proxies.PricerFeignProxy;
import tourGuide.proxies.RewardFeignProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@SpringBootTest
public class TestRewardsService {

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
	public void userGetRewards() {
		RewardsService rewardsService = new RewardsService(gpsFeignProxy, rewardFeignProxy);

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsFeignProxy, rewardsService, pricerFeignProxy);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsFeignProxy.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user);
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
		System.out.println("rewards = " + userRewards.size());//rewards = 1, TourGuideService.trackUserLocation()

		assertTrue(userRewards.size() == 1);
	}
	
	@Test
	public void isWithinAttractionProximity() {
		RewardsService rewardsService = new RewardsService(gpsFeignProxy, rewardFeignProxy);
		Attraction attraction = gpsFeignProxy.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	// TODO: Needs fixed - can throw ConcurrentModificationException
	//modifications faites
	@Test
	public void nearAllAttractions() {
		RewardsService rewardsService = new RewardsService(gpsFeignProxy, rewardFeignProxy);
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsFeignProxy, rewardsService, pricerFeignProxy);
		
		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsFeignProxy.getAttractions().size(), userRewards.size());
	}
	
}
