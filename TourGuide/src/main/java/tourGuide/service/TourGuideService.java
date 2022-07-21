package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.dto.RecommendAttraction;
import tourGuide.dto.RecommendAttractionsDto;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtilService gpsUtilService;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	
	public TourGuideService(GpsUtilService gpsUtilService, RewardsService rewardsService) {
		this.gpsUtilService = gpsUtilService;
		this.rewardsService = rewardsService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}
	
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}
	
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
				gpsUtilService.getUserLocation(user).join();
		return visitedLocation;
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}
	
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}
	public Map<String, Location> getAllCurrentLocations() {
		Map<String, Location> allLocations = new ConcurrentHashMap<>();
		getAllUsers().forEach(user -> {
			allLocations.put(user.getUserId().toString(), user.getLastVisitedLocation().location);
		});
		return allLocations;
	}
	
	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}
	
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}
	public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
		return gpsUtilService.getUserLocation(user)
				.thenApply(visitedLocation -> {
					user.addToVisitedLocations(visitedLocation);
					rewardsService.calculateRewards(user);
					return visitedLocation;
				});
	}
	public UserPreferences getUserPreferences(String userName) {
		return getUser(userName).getUserPreferences();
	}

	public UserPreferences updateUserPreferences(UserPreferencesDto userPreferencesDto) {
		UserPreferences newUserPreferences = new UserPreferences();
		newUserPreferences.setAttractionProximity(userPreferencesDto.getAttractionProximity());
		newUserPreferences.setHighPricePoint(userPreferencesDto.getHighPricePoint());
		newUserPreferences.setLowerPricePoint(userPreferencesDto.getLowerPricePoint());
		newUserPreferences.setTicketQuantity(userPreferencesDto.getTicketQuantity());
		newUserPreferences.setTripDuration(userPreferencesDto.getTripDuration());
		newUserPreferences.setNumberOfAdults(userPreferencesDto.getNumberOfAdults());
		newUserPreferences.setNumberOfChildren(userPreferencesDto.getNumberOfChildren());

		String userName = userPreferencesDto.getUserName();
		User user = getUser(userName);
		user.setUserPreferences(newUserPreferences);

		return newUserPreferences;
	}

	public RecommendAttractionsDto getRecommendAttractions(String userName) {
			RecommendAttractionsDto recommendAttractionsDto = new RecommendAttractionsDto();

			User user = getUser(userName);
			VisitedLocation visitedLocation = getUser(userName).getLastVisitedLocation();
			Location location = visitedLocation.location;
			List<RecommendAttraction> attractions = new CopyOnWriteArrayList<>();

			List<Attraction> nearByAttractions = getNearByAttractions(visitedLocation);
			for (Attraction attraction : nearByAttractions) {
				RecommendAttraction recommendAttraction = new RecommendAttraction();
				recommendAttraction.setName(attraction.attractionName);
				recommendAttraction.setLocation(attraction.latitude, attraction.longitude);
				recommendAttraction.setDistance(rewardsService.getDistance(attraction, location));
				recommendAttraction.setRewardPoints(rewardsService.getRewardPoints(attraction, user));
				attractions.add(recommendAttraction);
			}
			recommendAttractionsDto.setUserLocation(location);
			recommendAttractionsDto.setAttractionList(attractions);

			return recommendAttractionsDto;
		}

	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
			List<Attraction> attractions = gpsUtilService.getAttractions();
			// Recommandez les cinq attractions touristiques les plus proches :
			return attractions.stream()
					.sorted(Comparator.comparing(attraction -> rewardsService.getDistance(visitedLocation.location, attraction)))
					.limit(5)
					.collect(Collectors.toList());
		}
	
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}
	
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}
	
	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
	
}
