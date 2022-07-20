package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {

	ThreadPoolExecutor executorService = new ThreadPoolExecutor(
			5,
			8,
			3,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(3),
			Executors.defaultThreadFactory(),
			new ThreadPoolExecutor.DiscardOldestPolicy()//Will wait and try.
	);

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardCentral;
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardCentral = rewardCentral;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}
	
	public  List<UserReward> calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> allAttractions = gpsUtil.getAttractions();
		List<VisitedLocation> locations = new CopyOnWriteArrayList<>(userLocations);
		List<UserReward> rewards = new ArrayList<>();

		for(Attraction attraction : allAttractions) {
			for(VisitedLocation userLocation : locations) {
				if(nearAttraction(userLocation, attraction)) {

		//Si l'utilisateur n'a pas encore reçu la recompense :
					if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
						UserReward userReward = new UserReward(userLocation, attraction, 0);
						user.addUserReward(userReward);
						rewards.add(userReward);
					}
				}
			}
		}
		for (UserReward reward : rewards) {
			CompletableFuture.supplyAsync(() -> getRewardPoints(reward.attraction, user), executorService)
					.thenAccept((points) -> user.addUserReward(new UserReward(user.getLastVisitedLocation(), reward.attraction, points)));
		}
		return user.getUserRewards();
	}
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	public int getRewardPoints(Attraction attraction, User user) {
		return rewardCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

}
