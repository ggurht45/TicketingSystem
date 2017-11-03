package ticketing;


import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;


public class Venue implements TicketService {

    //sleeping times in miliseconds
    private static final int EXPIRE_MIN = 1000;
    private static final int EXPIRE_MAX = 3000;

    //min, and max number of seats a customer can request
    private static final int MIN_SEATS = 1;
    private static final int MAX_SEATS = 6;

    //total number of seats
    private static AtomicInteger TOTAL_NUM_OF_SEATS = new AtomicInteger(10);

    //counter for currently held seats
    private static AtomicInteger NUM_OF_SEATS_HELD = new AtomicInteger();

    //counter for currently reserved seats
    private static AtomicInteger NUM_OF_SEATS_RESERVED = new AtomicInteger();


    //see 1.
    private static ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Seat>> mapOfWaitLists = new ConcurrentHashMap<>();
    //keys = customer name/email; value is seat they reserved. (later will be seatHold object)
    private static ConcurrentHashMap<String, Seat> mapOfReservedSeats = new ConcurrentHashMap<>();

    //thread factory naming
    private static ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("Customer_%d_@gmail.com")
            .setDaemon(true)
            .build();

    //think about numSeatsToHold, and synchronization, and multi threads.
    //just polling, maybe dont need to worry about sync. hm.. think later.
    //********this method collects all the seats in one go for one customer and then returns them.****
    //maybe could think of a method that satisfies many customers one seat at a time.
    // the customer could dynamically increase or decrease their seat requests.

    //********> before a thread gives up waiting to hold onto some elements, it should wait until no seats are being held.
    //in other words, if seats are being held currently, then the thread should not give up and say oh, no seats are available.
    //see producer consumer exmple.

    //----> customers = threads. customers who are currently holding seats are possible producers.
    //customers who are waiting to hold seats are possible consumers. ... hmm. maybe use BlockingQueue. sizes will be determined by initial read of data.


    //---> reason to not use blockingQ.
    //well we have more than one list. therefore, if one list is empty and it may really be empty, so we dont want to continuously wait on it.
    //

    private synchronized static ConcurrentLinkedQueue<Seat> holdSeats(int numSeatsToHold, String customerEmail) {
        ConcurrentLinkedQueue<Seat> seatsBeingHeld = new ConcurrentLinkedQueue<>();

        Seat seat = null; // return value
//        do {
//            //if all of the seats (that are not reserved already) are being held, then wait.
//            while (NUM_OF_SEATS_HELD.get() < (TOTAL_NUM_OF_SEATS.get() - NUM_OF_SEATS_RESERVED.get())) {
////                try {
////                    Thread.currentThread().wait();
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
//            }
            for (Map.Entry<Integer, ConcurrentLinkedQueue<Seat>> entry : mapOfWaitLists.entrySet()) {  // look in each wait list for the first used
                //get key, value for each entry
                Integer key = entry.getKey();
                ConcurrentLinkedQueue<Seat> waitingListValue = entry.getValue();

                //get top seat; if possible
                seat = waitingListValue.poll();

                //while current list is not empty, and still need more seats
                //then, collect the seat that was polled, and update counter and seat variable
                while (seat != null && numSeatsToHold > 0) {
                    seatsBeingHeld.add(seat);
                    numSeatsToHold--;
                    NUM_OF_SEATS_HELD.incrementAndGet();
                    seat = waitingListValue.poll(); //update seat variable
                }

                //if all the seats have been collected, then return them.
                if (numSeatsToHold == 0) {
                    return seatsBeingHeld;
                }
            }
//        } while (NUM_OF_SEATS_RESERVED != TOTAL_NUM_OF_SEATS);
        //dont give up if total number of seats != reserved seats.
        //wait until[#held < #rest]          (total - reserved) ==> rest of seats.
        //dont give up if seats are being held currently.


        System.out.println("!!!! empty q of seats being returned... !!!"); //<------ IF we see this message, (even once), then the final
        //map after everything needs to be empty. (i think).
        return seatsBeingHeld; // no seats found
    }

    //this will be executed inside the run method of the threads that will be imitating customers using this service
    private static void imitateCustomer() {
        //request for and hold a random number of seats, each customer.
        int numSeatsToHold = 6;//ThreadLocalRandom.current().nextInt(MIN_SEATS, MAX_SEATS + 1);

        //find seats based on neighbors later. for now select single best seats.


        //each thread is like a person. should hold a seat
        ConcurrentLinkedQueue<Seat> seats = Venue.holdSeats(numSeatsToHold, Thread.currentThread().getName());
        System.out.println(Thread.currentThread().getName() + " got hold of seat: " + seats);
        String msg = "Map after " + seats + " held by " + Thread.currentThread().getName() + "\n";
        System.out.println("***" + msg + mapOfWaitLists + "\n***");

        //then go to sleep for a few seconds. (to imitate customer contemplating seat choices)
        try {
            int timeSecs = 1000;//ThreadLocalRandom.current().nextInt(EXPIRE_MIN, EXPIRE_MAX + 1);
            System.out.println(Thread.currentThread().getName() + " about to sleep for(secs): " + (timeSecs / 1000.0));
            Thread.sleep(timeSecs);
        } catch (Exception e) {
            System.out.println(Thread.currentThread().getName() + " exception happened! ");
            e.printStackTrace();
        }

        //after wakes up, should decide if to return the seat or reserve it.
        int reserveOrNot = 0;//ThreadLocalRandom.current().nextInt(0, 2);
        //seats might be null if couldnt find any seats.
        if (reserveOrNot == 0) {
            //put back
            Venue.expireHold(seats);
            System.out.println("please put this seat back into its queue: " + seats);

        } else {
            //reserve
            Venue.reserve(seats);
            System.out.println("please reserve this seat: " + seats);
        }


    }

    //i dont think synchronized is needed here,.. now with queue rather than single seat, maybe we do.
    //still dont think we need sync, cuz just adding to the q. multiple threads can do that to the unbounded q.
    private synchronized static void expireHold(ConcurrentLinkedQueue<Seat> seats) {
        for (Seat seat : seats) {
            System.out.println(seat + " is being put back...(priority:" + seat.getPriority() + ")");
            NUM_OF_SEATS_HELD.decrementAndGet();
            mapOfWaitLists.get(seat.getPriority()).add(seat);
//            Thread.currentThread().notifyAll();
        }

    }

    private static void reserve(ConcurrentLinkedQueue<Seat> seats) {
        System.out.println(seats + " is being reserved...");
    }


    public static void main(String[] args) {
        //create a 2 waitlists. give them 5 seats each. each seat has priority. 5, 10. five is higher priority.
        ConcurrentLinkedQueue<Seat> waitList_5 = new ConcurrentLinkedQueue<>();
        waitList_5.add(new Seat(0, 0, 5));
        waitList_5.add(new Seat(0, 1, 5));
        waitList_5.add(new Seat(0, 2, 5));
        waitList_5.add(new Seat(0, 3, 5));
        waitList_5.add(new Seat(0, 4, 5));

        ConcurrentLinkedQueue<Seat> waitList_10 = new ConcurrentLinkedQueue<>();
        waitList_10.add(new Seat(1, 0, 10));
        waitList_10.add(new Seat(1, 1, 10));
        waitList_10.add(new Seat(1, 2, 10));
        waitList_10.add(new Seat(1, 3, 10));
        waitList_10.add(new Seat(1, 4, 10));


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


//need a snap-shot class that takes a snapshot and saves everything in one instance. think more. all of the maps, and current things being held, reserved and printed out should be shown.


// 1.  Should use a concurrent hashmap then an arraylist; if out of 10 priorities, we are only using 2, then hashmap will only have 2 keys.
// also hashmap may be able to support different notions of priorities such as red class, blue class or green class (whatever they mean)
// therefore its a more flexible solution without much of a overhead.
// We will never really be adding or removing waitlists from the hashmap except in the intial set up when all the seats are put inside
// the hashmap.
//
//can use the default constructor for the ConcurrentHashMap, which sets the initial capacity of the hashmap to be 16, and load fac .75, concur 16