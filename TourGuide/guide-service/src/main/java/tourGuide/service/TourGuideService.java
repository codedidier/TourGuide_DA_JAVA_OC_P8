package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.dto.RecommendAttraction;
import tourGuide.dto.RecommendAttractionsDto;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.proxies.GpsFeignProxy;
import tourGuide.proxies.PricerFeignProxy;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

	@Service
	public class TourGuideService {
		private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
		private final GpsFeignProxy gpsFeignProxy;
		private final RewardsService rewardsService;
		private final PricerFeignProxy pricerFeignProxy;
		public final Tracker tracker;
		boolean testMode = true;

		public TourGuideService(GpsFeignProxy gpsFeignProxy, RewardsService rewardsService, PricerFeignProxy pricerFeignProxy) {
			this.gpsFeignProxy = gpsFeignProxy;
			this.rewardsService = rewardsService;
			this.pricerFeignProxy = pricerFeignProxy;
			if(testMode) {
				logger.info("TestMode enabled");
				logger.debug("Initializing users");
				initializeInternalUsers();
				logger.debug("Finished initializing users");
			}
			tracker = new Tracker(this);
			addShutDownHook();
		}
		public List<Attraction> getAllAttractions() {
			List<Attraction> attractions = gpsFeignProxy.getAttractions();
			return attractions;
		}
		public VisitedLocation getUserLocation(UUID userId) {
			return gpsFeignProxy.getUserLocation(userId);
		}
		public List<Provider> getPrice(String apiKey, UUID attractionId, int adults, int children, int nightsStay, int rewardsPoints) {
			return pricerFeignProxy.getPrice(apiKey, attractionId, adults, children, nightsStay, rewardsPoints);
		}
		ThreadPoolExecutor executorService = new ThreadPoolExecutor(
				5,
				8,
				1,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(3),
				Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.DiscardOldestPolicy()//Will wait and try.
		);
		public List<UserReward> getUserRewards(User user) {
			return user.getUserRewards();
		}

		public VisitedLocation getUserLocation(User user) {
			VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
					user.getLastVisitedLocation() :
					trackUserLocation(user);
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
			List<Provider> providers = getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
					user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
			user.setTripDeals(providers);
			return providers;
		}
		public VisitedLocation trackUserLocation(User user) {
			CompletableFuture<VisitedLocation> future = CompletableFuture.supplyAsync(() -> getUserLocation(user.getUserId()), executorService)
					.thenApply(visitedLocation -> {
						user.addToVisitedLocations(visitedLocation);
						rewardsService.calculateRewards(user);
						return visitedLocation;
					});
			VisitedLocation visitedLocation = future.join();
			return visitedLocation;
		}
		public UserPreferences getUserPreferences(String userName) {
			return getUser(userName).getUserPreferences();
		}


			public UserPreferences updateUserPreferences(UserPreferencesDto newUserPreferences) {
				String userName = newUserPreferences.getUserName();
				User user = getUser(userName);

				UserPreferences oldUserPreferences = user.getUserPreferences();
				if (newUserPreferences.getHighPricePoint() != null) {
					oldUserPreferences.setHighPricePoint(newUserPreferences.getHighPricePoint());
				}
				if (newUserPreferences.getLowerPricePoint() != null) {
					oldUserPreferences.setLowerPricePoint(newUserPreferences.getLowerPricePoint());
				}
				if (newUserPreferences.getNumberOfAdults() >= 0) {
					oldUserPreferences.setNumberOfAdults(newUserPreferences.getNumberOfAdults());
				}
				if (newUserPreferences.getNumberOfChildren() >= 0) {
					oldUserPreferences.setNumberOfChildren(newUserPreferences.getNumberOfChildren());
				}
				if (newUserPreferences.getTripDuration() >= 0) {
					oldUserPreferences.setTripDuration(newUserPreferences.getTripDuration());
				}
				if (newUserPreferences.getTicketQuantity() >= 0) {
					oldUserPreferences.setTicketQuantity(newUserPreferences.getTicketQuantity());
				}
				return oldUserPreferences;
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
				List<Attraction> attractions = getAllAttractions();
				// Recommend the closest five tourist attractions:
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
