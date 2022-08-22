package tourGuide.controller;


import tourGuide.dto.RecommendAttractionsDto;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import com.jsoniter.output.JsonStream;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tourGuide.service.TourGuideService;
import tripPricer.Provider;

import java.util.List;
import java.util.Map;


@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
    }

    // TODO : Modifier cette méthode pour qu'elle ne renvoie plus une liste d'attractions.
    // Au lieu de cela : Obtenir les cinq attractions touristiques les plus proches de l'utilisateur, quelle que soit leur distance.
    // Retourne un nouvel objet JSON qui contient :
    // Nom de l'attraction touristique,
    // Attractions touristiques lat/long,
    // La position de l'utilisateur (lat/long),
    // La distance en miles entre l'emplacement de l'utilisateur et chacune des attractions.
    // Les points de récompense pour la visite de chaque attraction.
    // Remarque : les points de récompense pour les attractions peuvent être obtenus auprès de RewardsCentral.

    @RequestMapping("/getNearbyAttractions") 
    public String getNearbyAttractions(@RequestParam String userName) {
        RecommendAttractionsDto recommendAttractions = tourGuideService.getRecommendAttractions(userName);
        return JsonStream.serialize(recommendAttractions);
    }
    
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
    	return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }
    
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
    	// TODO: Get a list of every user's most recent location as JSON
    	//- Note: does not use gpsUtil to query for their current location, 
    	//        but rather gathers the user's current location from their stored location history.
    	//
    	// Return object should be the just a JSON mapping of userId to Locations similar to:
    	//     {
    	//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371} 
    	//        ...
    	//     }

        Map<String, Location> allCurrentLocations = tourGuideService.getAllCurrentLocations();
        return JsonStream.serialize(allCurrentLocations);
    }

    @RequestMapping("/getUserPreferences")
    public String getUserPreferences(@RequestParam String userName) {
        UserPreferences userPreferences = tourGuideService.getUserPreferences(userName);
        return JsonStream.serialize(userPreferences);
    }

    @PostMapping("/updateUserPreferences")
    public String updateUserPreferences(@RequestBody UserPreferencesDto userPreferencesDto) {
        UserPreferences userPreferences = tourGuideService.updateUserPreferences(userPreferencesDto);
        return JsonStream.serialize(userPreferences);
    }
    
    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
    	List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
    	return JsonStream.serialize(providers);
    }
    @RequestMapping("/getUser")
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   

}