package ticketing;


import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;


public class Venue implements TicketService {

    //instance of class
    private static Venue venueInstance;

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
    private static ConcurrentHashMap<Integer, SeatHold> mapOfReservedSeats = new ConcurrentHashMap<>();

    //thread factory naming
    private static ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("Customer_%d_@gmail.com")
            .setDaemon(true)
            .build();

    //see 2.
    private synchronized static SeatHold holdSeats(int numSeats, String customerEmail) {
        ConcurrentLinkedQueue<Seat> seatsBeingHeld = new ConcurrentLinkedQueue<>();

        //keep the original number in arg. decrement this variable
        int numSeatsToHold = numSeats;

        Seat seat = null;
        for (Map.Entry<Integer, ConcurrentLinkedQueue<Seat>> entry : mapOfWaitLists.entrySet()) {  // look in each wait list for the first used
            //get key, value for each entry
            Integer key = entry.getKey();
            ConcurrentLinkedQueue<Seat> waitingListValue = entry.getValue();

            //peek top seat
            seat = waitingListValue.peek();

            //while current list is not empty, and still need more seats
            //then, collect the seat that was polled, and update counter and seat variable
            while (seat != null && numSeatsToHold > 0) {
                seat = waitingListValue.poll(); //pop seat
                seatsBeingHeld.add(seat);
                numSeatsToHold--;
                NUM_OF_SEATS_HELD.incrementAndGet();
                seat = waitingListValue.peek(); //peek next seat
            }

            //if all the seats have been collected, then return them.
            if (numSeatsToHold == 0) {
                SeatHold seatHold = new SeatHold(seatsBeingHeld, customerEmail, numSeats);
                return seatHold;
            }
        }
        int numberOfSeatsFound = numSeats - numSeatsToHold;
        SeatHold seatHold = new SeatHold(seatsBeingHeld, customerEmail, numberOfSeatsFound);
        printStatement1(seatHold, customerEmail, "customer only found " + numberOfSeatsFound + " out of " + numSeats);
        return seatHold; // no seats found
    }


    private synchronized static void printStatement1(SeatHold sh, String email, String msg) {
        System.out.println("-----------" + email + "----" + msg);
        System.out.println(sh);
        printStatement2_staticVars();
        System.out.println("End-----------" + email + "\n");
    }

    private synchronized static void printStatement2_staticVars() {
        System.out.println(mapOfWaitLists);
        System.out.println(mapOfReservedSeats);
        System.out.println("NUM_OF_SEATS_HELD: " + NUM_OF_SEATS_HELD.get());
        System.out.println("NUM_OF_SEATS_RESERVED: " + NUM_OF_SEATS_RESERVED.get());
        System.out.println("venueInstance.numSeatsAvailable(): " + venueInstance.numSeatsAvailable());
    }

    private synchronized static void printStatement3(String threadName) {
        System.out.println("-----------" + threadName + "----seat queues seem empty..\n");
    }


    //this will be executed inside the run method of the threads that will be imitating customers using this service
    private static void imitateCustomer() {
        //request for and hold a random number of seats, each customer.
        int numSeatsToHold = ThreadLocalRandom.current().nextInt(MIN_SEATS, MAX_SEATS + 1);
        String customerEmail = Thread.currentThread().getName();


        //each thread is like a person. should hold a seat
        SeatHold seatHold = venueInstance.findAndHoldSeats(numSeatsToHold, customerEmail);
        printStatement1(seatHold, customerEmail, "Seats Held");

        //then go to sleep for a few seconds. (to imitate customer contemplating seat choices)
        try {
            int timeSecs = ThreadLocalRandom.current().nextInt(EXPIRE_MIN, EXPIRE_MAX + 1);
            System.out.println(customerEmail + " thread sleeping for " + (timeSecs / 1000) + "\n");
            Thread.sleep(timeSecs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //after wakes up, should decide if to return the seat or reserve it.
        int reserveOrNot = ThreadLocalRandom.current().nextInt(0, 2);
        if (reserveOrNot == 0) {
            Venue.expireHold(seatHold.getSeatsBeingHeld());
            printStatement1(seatHold, customerEmail, "Seats Hold Expired");
        } else {
            int seatHoldId = Venue.saveToReservationMap(seatHold);
            venueInstance.reserveSeats(seatHoldId, seatHold.getCustomerEmail());
            printStatement1(seatHold, customerEmail, "Seats Hold Reserved");
        }
    }

    //i dont think synchronized is needed here,.. now with queue rather than single seat, maybe we do.
    //still dont think we need sync, cuz just adding to the q. multiple threads can do that to the unbounded q.
    private synchronized static void expireHold(ConcurrentLinkedQueue<Seat> seats) {
        for (Seat seat : seats) {
            NUM_OF_SEATS_HELD.decrementAndGet();
            mapOfWaitLists.get(seat.getPriority()).add(seat);
        }
    }

    private static int saveToReservationMap(SeatHold seatHold) {
        int hash = seatHold.getSeatsBeingHeld().hashCode();
        seatHold.setHashId(hash);
        mapOfReservedSeats.put(hash, seatHold);
        int num = seatHold.getNumberOfSeats();
        NUM_OF_SEATS_HELD.addAndGet(-num);  //remove from the held seats counter
        NUM_OF_SEATS_RESERVED.addAndGet(num); //add to the reserved seats counter
        return hash;
    }


    public static void main(String[] args) {
        //create an instance of this class to be used in other places
        venueInstance = new Venue();

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

        //set total num seats;
        TOTAL_NUM_OF_SEATS.getAndSet(10);


        //put waitlists in hashmap
        mapOfWaitLists.put(5, waitList_5);
        mapOfWaitLists.put(10, waitList_10);

        //initial print
        System.out.println("--------------initial vars:");
        printStatement2_staticVars();
        System.out.println("----------------------------\n");


        //create several threads that remove highest priority seat and put them back after a few seconds.
        ExecutorService executor = Executors.newFixedThreadPool(5, threadFactory);//creating a pool of 5 threads
        while (true) {
            //execute method takes in a runnable object
            executor.execute(() -> Venue.imitateCustomer());
            if (venueInstance.numSeatsAvailable() == 0 && NUM_OF_SEATS_HELD.get() == 0) {
                System.out.println("sa: " + venueInstance.numSeatsAvailable() + "sh: " + NUM_OF_SEATS_HELD.get());
                executor.shutdown();
                break;
            }

        }

//        while (!executor.isTerminated()) {
//        }


        //final print
        System.out.println("\n--------------final vars:");
        printStatement2_staticVars();
    }

    public int numSeatsAvailable() {
        return TOTAL_NUM_OF_SEATS.get() - (NUM_OF_SEATS_HELD.get() + NUM_OF_SEATS_RESERVED.get());
    }

    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        return Venue.holdSeats(numSeats, customerEmail);
    }

    public String reserveSeats(int seatHoldId, String customerEmail) {
        //seats should already have been stored in the reservation map, thats why we have seatHoldID,
        //perform any other business logic here
        String i = (new Integer(seatHoldId)).toString();
        return (i).concat(customerEmail.substring(0, 3));
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


//2. think about numSeatsToHold, and synchronization, and multi threads.
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

