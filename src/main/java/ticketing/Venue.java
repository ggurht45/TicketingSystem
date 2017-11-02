package ticketing;


import java.util.concurrent.ConcurrentLinkedQueue;

public class Venue {


    public static void main(String[] args) {
        System.out.println("hello world");


        //create a 2 waitlists. give them 5 seats each. each seat has priority. 5, 10. five is higher priority.

        //seat = string for now. will later be x,y position.
        ConcurrentLinkedQueue<String> waitList_5 = new ConcurrentLinkedQueue<>();
        waitList_5.add("seat1");
        waitList_5.add("seat2");
        waitList_5.add("seat3");
        waitList_5.add("seat4");
        waitList_5.add("seat5");

        ConcurrentLinkedQueue<String> waitList_10 = new ConcurrentLinkedQueue<>();
        waitList_10.add("seat6");
        waitList_10.add("seat7");
        waitList_10.add("seat8");
        waitList_10.add("seat9");
        waitList_10.add("seat10");


        //should use a concurrent hashmap then an arraylist; if out of 10 priorities, we are only using 2, then hashmap will only have 2 keys.
        // also hashmap may be able to support different notions of priorities such as red class, blue class or green class (whatever they mean)
        // therefore its a more flexible solution without much of a overhead.
        // we will never really be adding or removing waitlists from the hashmap except in the intial set up when all the seats are put inside
        // the hashmap. 











        //create several threads that remove highest priority seat and put them back after a few seconds.

        //run that inside a loop.


    }
}
