package tourGuide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gpsUtil.location.Location;

import java.util.List;

public class RecommendAttractionsDto {
    @JsonProperty("userLocation")
    private Location userLocation;
    @JsonProperty("recommendAttractions")
    private List<RecommendAttraction> attractionList;

    public RecommendAttractionsDto() {
    }

    public RecommendAttractionsDto(Location userLocation, List<RecommendAttraction> attractionList) {
        this.userLocation = userLocation;
        this.attractionList = attractionList;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public List<RecommendAttraction> getAttractionList() {
        return attractionList;
    }

    public void setAttractionList(List<RecommendAttraction> attractionList) {
        this.attractionList = attractionList;
    }
}
