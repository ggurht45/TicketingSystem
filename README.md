# TicketingSystem

##Assumptions:
 * Seats closer to the stage are of higher value;
 * Integers are used to represent the priorities of seating;
 * Low integers mean a higher value seat. Seat with level 1 is better than seat with level 5;
 
         /** 18x30 grid seating layout example
                                {{Stage}}
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
 
 * seats at the edges are all given high value;
 
 
 ## Build and Run
 * `gradle build` to build project and run tests
 * `gradle run` to run project
 * Test results in "build/reports/tests/test/index.html"
 

## Output Description
Venue implements the TicketService and employs a threadpool to demonstrate how the methods would be used. The output printed to the console will look something like:
 
`
-----------Customer_5_@gmail.com----Seats Hold Expired
SeatHold{seatsBeingHeld=[(16,4)-9], customerEmail='Custome
NUM_OF_SEATS_HELD: 53
NUM_OF_SEATS_RESERVED: 486
venueInstance.numSeatsAvailable(): 1
End-----------Customer_5_@gmail.com
`


In the beginning of the program, `NUM_OF_SEATS_HELD` and `NUM_OF_SEATS_RESERVED` are 0 and `venueInstance.numSeatsAvailable()` has all the seats. As the program runs, the number of seats being held by customers should increase; eventually the number of seats reserved increases also until at the end of the program execution, all of the seats should have been reserved. 
