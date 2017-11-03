package ticketing;

public class Seat {
    private int xPosition;
    private int yPosition;
    private int priority;


    public Seat(int xPosition, int yPosition, int priority) {
        this.priority = priority;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }

    public int getPriority() {
        return priority;
    }

    public int getxPosition() {
        return xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    //only have a setter for priority, not the other fields
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "(" + xPosition + "," + yPosition + ")-" + priority;
    }
}
