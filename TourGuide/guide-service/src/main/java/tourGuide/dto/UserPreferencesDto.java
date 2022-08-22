package tourGuide.dto;

import org.javamoney.moneta.Money;

public class UserPreferencesDto {
    private String userName;
    private Money lowerPricePoint;
    private Money highPricePoint;
    private int tripDuration = 0;
    private int ticketQuantity = 0;
    private int numberOfAdults = 0;
    private int numberOfChildren = 0;

    public UserPreferencesDto() {
    }

    public UserPreferencesDto(String userName, Money lowerPricePoint, Money highPricePoint, int tripDuration, int ticketQuantity, int numberOfAdults, int numberOfChildren) {
        this.userName = userName;
        this.lowerPricePoint = lowerPricePoint;
        this.highPricePoint = highPricePoint;
        this.tripDuration = tripDuration;
        this.ticketQuantity = ticketQuantity;
        this.numberOfAdults = numberOfAdults;
        this.numberOfChildren = numberOfChildren;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

       public Money getLowerPricePoint() {
        return lowerPricePoint;
    }

    public void setLowerPricePoint(Money lowerPricePoint) {
        this.lowerPricePoint = lowerPricePoint;
    }

    public Money getHighPricePoint() {
        return highPricePoint;
    }

    public void setHighPricePoint(Money highPricePoint) {
        this.highPricePoint = highPricePoint;
    }

    public int getTripDuration() {
        return tripDuration;
    }

    public void setTripDuration(int tripDuration) {
        this.tripDuration = tripDuration;
    }

    public int getTicketQuantity() {
        return ticketQuantity;
    }

    public void setTicketQuantity(int ticketQuantity) {
        this.ticketQuantity = ticketQuantity;
    }

    public int getNumberOfAdults() {
        return numberOfAdults;
    }

    public void setNumberOfAdults(int numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    public int getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(int numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }
}
