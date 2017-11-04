package ticketing;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;


public class Venue implements TicketService {

    private static Venue venueInstance = new Venue();             //instance of this class, which implements TicketService
    private static final int MAX_THREADS = 15;                    //max number of customer threads

    //thread factory naming convention; email format with number
    private static ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("Customer_%d_@gmail.com")
            .setDaemon(true)
            .build();

    //for seating layout
    private static final int ROW_LIMIT = 18;
    private static final int COL_LIMIT = 30;
    private static final int TOTAL_SEATS = ROW_LIMIT * COL_LIMIT;    //total number of seats

    //sleeping times in miliseconds for customer threads's hold expiration
    private static final int EXPIRE_MIN = 1000;
    private static final int EXPIRE_MAX = 3000;

    //min, and max number of seats a customer can request
    private static final int MIN_SEATS = 1;
    private static final int MAX_SEATS = 20;

    //atomic variables keeping track of held and reserved seats
    private static AtomicInteger NUM_OF_SEATS_HELD = new AtomicInteger();
    private static AtomicInteger NUM_OF_SEATS_RESERVED = new AtomicInteger();

    //map of all the available seats; arranged in queues by priority of seats. low number = higher priority.
    private static ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Seat>> mapOfSeatQueues = new ConcurrentHashMap<>();
    //map of reserved seats. keys = hashcode of SeatHold obj; values = seatHold objects
    private static ConcurrentHashMap<Integer, SeatHold> mapOfReservedSeats = new ConcurrentHashMap<>();


    //this method returns a seatHold object containing number of seats requests or slightly less depending on seat
    //availabilities. It goes through the map of seat queues in order of seat priorities and builds a seatHold object.
    private synchronized static SeatHold holdSeats(int numSeats, String customerEmail) {
        //queue to hold seats you need
        ConcurrentLinkedQueue<Seat> seatsBeingHeld = new ConcurrentLinkedQueue<>();

        //create a variable to check off how many seats have yet to be found; numSeatsToHold will decrement as seats are found
        int numSeatsToHold = numSeats;

        //go through map of seat queues
        Seat seat = null;
        for (Map.Entry<Integer, ConcurrentLinkedQueue<Seat>> entry : mapOfSeatQueues.entrySet()) {
            //get key, value for each entry in map
            Integer key = entry.getKey();               //will be an integer representing priority level (1-9 for example)
            ConcurrentLinkedQueue<Seat> seatQueue = entry.getValue();       //queue of seats at a certain priority

            //peek top seat
            seat = seatQueue.peek();

            //collect seats
            while (seat != null && numSeatsToHold > 0) {
                seat = seatQueue.poll();            //pop seat
                seatsBeingHeld.add(seat);           //collect seat
                numSeatsToHold--;                   //decrement the count of needed seats
                NUM_OF_SEATS_HELD.incrementAndGet();    //increase static var that shows how many total seats are being held
                seat = seatQueue.peek();            //peek next seat
            }

            //return SeatHold object if found all the seats you were looking for
            if (numSeatsToHold == 0) {
                SeatHold seatHold = new SeatHold(seatsBeingHeld, customerEmail, numSeats);
                return seatHold;
            }
        }

        //return however many seats you managed to collect to the customer.
        int numberOfSeatsFound = numSeats - numSeatsToHold;
        SeatHold seatHold = new SeatHold(seatsBeingHeld, customerEmail, numberOfSeatsFound);
        //print info
        printStatement1(seatHold, customerEmail, "customer only found " + numberOfSeatsFound + " out of " + numSeats);
        return seatHold;
    }


    //helper print method 1
    private synchronized static void printStatement1(SeatHold sh, String email, String msg) {
        System.out.println("-----------" + email + "----" + msg);
        System.out.println(sh);
        printStatement2_staticVars();
        System.out.println("End-----------" + email + "\n");
    }

    //helper print method 2
    private synchronized static void printStatement2_staticVars() {
        System.out.println("mapOfSeatQueues: " + mapOfSeatQueues);
        System.out.println("mapOfReservedSeats: " + mapOfReservedSeats);
        System.out.println("NUM_OF_SEATS_HELD: " + NUM_OF_SEATS_HELD.get());
        System.out.println("NUM_OF_SEATS_RESERVED: " + NUM_OF_SEATS_RESERVED.get());
        System.out.println("venueInstance.numSeatsAvailable(): " + venueInstance.numSeatsAvailable());
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

    //Hold expired on these seats. Add them back into the map.
    private synchronized static void expireHold(ConcurrentLinkedQueue<Seat> seats) {
        for (Seat seat : seats) {
            NUM_OF_SEATS_HELD.decrementAndGet();                //decrement counter
            mapOfSeatQueues.get(seat.getPriority()).add(seat);
        }
    }

    //Add these seats to the map of reserved seats...
    private static int saveToReservationMap(SeatHold seatHold) {
        int hash = seatHold.getSeatsBeingHeld().hashCode();
        seatHold.setHashId(hash);
        mapOfReservedSeats.put(hash, seatHold);
        int num = seatHold.getNumberOfSeats();
        NUM_OF_SEATS_HELD.addAndGet(-num);              //subtract from "held" counter
        NUM_OF_SEATS_RESERVED.addAndGet(num);           //add to the reserved seats counter
        return hash;
    }


    private static void populateMap() {
        //using this layout of Venue: lower numbers means better seats (closer to the stage)
        /** 18x30 grid. example
         1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1
         1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1
         1 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 1
         1 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 1
         1 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 1
         1 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 1
         1 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 1
         1 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 1
         1 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 1
         1 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 5 1
         1 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 1
         1 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 6 1
         1 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 1
         1 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 1
         1 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 1
         1 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 1
         1 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 1
         1 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 1
         */

        //some temp variables
        int priority;
        Seat s;
        ConcurrentLinkedQueue<Seat> queue = new ConcurrentLinkedQueue<>();

        //populate map
        for (int row = 0; row < ROW_LIMIT; row++) {
            priority = row / 2 + 1;                 //priorities are between 1-9
            for (int col = 0; col < COL_LIMIT; col++) {
                s = new Seat(row, col, priority);
                queue.add(s);
            }
            mapOfSeatQueues.put(priority, queue);
        }
    }


    public static void main(String[] args) {
        populateMap();                  //populate map with queues of seats arranged by priority

        //Initial printout
        System.out.println("--------------initial vars:");
        printStatement2_staticVars();
        System.out.println("----------------------------\n");

        //create thread pool to represent customers buying tickets
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS, threadFactory);

        //keep running customer threads until stop conditions met.
        while (true) {
            //execute method takes in a lambda which references the "run" method for the thread
            executor.execute(() -> Venue.imitateCustomer());

            //if all the seats have been reserved, then stop program
            if (venueInstance.numSeatsAvailable() == 0 && NUM_OF_SEATS_HELD.get() == 0) {
                System.out.println("\n--------------final vars:");      //final printing of variables
                printStatement2_staticVars();
                executor.shutdown();
                break;
            }
        }
    }

    //implementation of the public methods as specified in the TicketService interface
    public int numSeatsAvailable() {
        return TOTAL_SEATS - (NUM_OF_SEATS_HELD.get() + NUM_OF_SEATS_RESERVED.get());
    }

    //uses a helper private static method
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        return Venue.holdSeats(numSeats, customerEmail);
    }

    //seats should already have been stored in the reservation map, thats why we have seatHoldID,
    public String reserveSeats(int seatHoldId, String customerEmail) {
        //perform any other business logic here (as required for a reservation)
        String i = (new Integer(seatHoldId)).toString();        //convert seatHold ID to string
        return (i).concat(customerEmail.substring(0, 3));       //return alphaNumeric string as confirmation code
    }

}
