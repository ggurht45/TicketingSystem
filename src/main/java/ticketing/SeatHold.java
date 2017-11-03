package ticketing;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SeatHold {

    //contains a bunch of seats.
    private ConcurrentLinkedQueue<Seat> seatsBeingHeld;
    private String customerEmail;
    private int numberOfSeats;
    private int hashId; //gets set when seat is actually reserved


    public ConcurrentLinkedQueue<Seat> getSeatsBeingHeld() {
        return seatsBeingHeld;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public int getHashId() {
        return hashId;
    }

    public void setHashId(int hashId) {
        this.hashId = hashId;
    }

    public SeatHold(ConcurrentLinkedQueue<Seat> seatsBeingHeld, String customerEmail, int numberOfSeats) {
        this.seatsBeingHeld = seatsBeingHeld;
        this.customerEmail = customerEmail;
        this.numberOfSeats = numberOfSeats;
    }

    @Override
    public String toString() {
        return "SeatHold{" +
                "seatsBeingHeld=" + seatsBeingHeld +
                ", customerEmail='" + customerEmail + '\'' +
                ", numberOfSeats=" + numberOfSeats +
                ", hashId=" + hashId +
                '}';
    }
}
