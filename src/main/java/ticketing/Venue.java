package ticketing;


import java.util.Map;
import java.util.concurrent.*;

import com.google.common.util.concurrent.ThreadFactoryBuilder;



public class Venue implements TicketService {

    private static final int EXPIRE_MIN = 1000;
    private static final int EXPIRE_MAX = 3000;

    //see 1.
    private static ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Seat>> mapOfWaitLists = new ConcurrentHashMap<>();
    //keys = customer name/email; value is seat they reserved. (later will be seatHold object)
    private static ConcurrentHashMap<String, Seat> mapOfReservedSeats = new ConcurrentHashMap<>();

    //thread factory naming
    private static ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("Customer_%d_@gmail.com")
            .setDaemon(true)
            .build();


    private static Seat holdSeat() {
        Seat seat = null; // return value

        for (Map.Entry<Integer, ConcurrentLinkedQueue<Seat>> entry : mapOfWaitLists.entrySet()) {  // look in each wait list for the first used
            //get key, value for each entry
            Integer key = entry.getKey();
            ConcurrentLinkedQueue<Seat> waitingListValue = entry.getValue();

            //get top seat; if possible
            seat = waitingListValue.poll();
            if (seat != null) return seat;  //when found a seat return it
        }
        return null; // no seats found
    }

    //this will be executed inside the run method of the threads that will be imitating customers using this service
    private static void imitateCustomer() {
        //each thread is like a person. should hold a seat
        Seat seat = Venue.holdSeat();
        System.out.println(Thread.currentThread().getName() + " got hold of seat: " + seat);
        String msg = "Map after " + seat + " held by " + Thread.currentThread().getName() + "\n";
        System.out.println("***" + msg + mapOfWaitLists + "\n***");

        //then go to sleep for a few seconds.
        try {
            int timeSecs = ThreadLocalRandom.current().nextInt(EXPIRE_MIN, EXPIRE_MAX + 1);
            System.out.println(Thread.currentThread().getName() + " about to sleep for(secs): " + (timeSecs / 1000.0));
            Thread.sleep(timeSecs);
        } catch (Exception e) {
            System.out.println(Thread.currentThread().getName() + " exception happened! ");
            e.printStackTrace();
        }

        //after wakes up, should decide if to return the seat or reserve it.
        int reserveOrNot = ThreadLocalRandom.current().nextInt(0, 2);
        if (reserveOrNot == 0) {
            //reserve
            Venue.reserve(seat);
            System.out.println("please reserve this seat: " + seat);
        } else {
            //put back
            Venue.holdExpired(seat);
            System.out.println("please put this seat back into its queue: " + seat);
        }


    }

    //i dont think synchronized is needed here, cuz
    private static void holdExpired(Seat seat) {
        System.out.println(seat + " is being put back...(priority:" + seat.getPriority()+ ")");
        mapOfWaitLists.get(seat.getPriority()).add(seat);
    }

    private static void reserve(Seat seat) {
        System.out.println(seat + " is being reserved...");
    }


    public static void main(String[] args) {
        //create a 2 waitlists. give them 5 seats each. each seat has priority. 5, 10. five is higher priority.
        ConcurrentLinkedQueue<Seat> waitList_5 = new ConcurrentLinkedQueue<>();
        waitList_5.add(new Seat(0,0, 5));
        waitList_5.add(new Seat(0,1, 5));
        waitList_5.add(new Seat(0,2, 5));
        waitList_5.add(new Seat(0,3, 5));
        waitList_5.add(new Seat(0,4, 5));

        ConcurrentLinkedQueue<Seat> waitList_10 = new ConcurrentLinkedQueue<>();
        waitList_5.add(new Seat(0,0, 10));
        waitList_5.add(new Seat(0,1, 10));
        waitList_5.add(new Seat(0,2, 10));
        waitList_5.add(new Seat(0,3, 10));
        waitList_5.add(new Seat(0,4, 10));


        //put waitlists in hashmap
        mapOfWaitLists.put(5, waitList_5);
        mapOfWaitLists.put(10, waitList_10);
        System.out.println("*** Initial map\n" + mapOfWaitLists + "\n***");


        //create several threads that remove highest priority seat and put them back after a few seconds.
        ExecutorService executor = Executors.newFixedThreadPool(5, threadFactory);//creating a pool of 5 threads
        for (int i = 0; i < 10; i++) {
            //execute method takes in a runnable object
            executor.execute(() -> Venue.imitateCustomer());
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }


        //show map after both threads finish
        System.out.println("*** Map at the end\n" + mapOfWaitLists + "\n***");
    }

    public int numSeatsAvailable() {
        return 0;
    }

    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        return new SeatHold();
    }

    public String reserveSeats(int seatHoldId, String customerEmail) {
        return "seats not reserved";
    }


}










































// 1.  Should use a concurrent hashmap then an arraylist; if out of 10 priorities, we are only using 2, then hashmap will only have 2 keys.
// also hashmap may be able to support different notions of priorities such as red class, blue class or green class (whatever they mean)
// therefore its a more flexible solution without much of a overhead.
// We will never really be adding or removing waitlists from the hashmap except in the intial set up when all the seats are put inside
// the hashmap.
//
//can use the default constructor for the ConcurrentHashMap, which sets the initial capacity of the hashmap to be 16, and load fac .75, concur 16