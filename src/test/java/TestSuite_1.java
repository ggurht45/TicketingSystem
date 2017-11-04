import org.junit.*;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TestSuite_1 extends TestRules {
    @Test
    public void testInitialValuesForStaticVariables() {
        assertEquals(15, TestVenue.MAX_THREADS);
        assertEquals(18, TestVenue.ROW_LIMIT);
        assertEquals(30, TestVenue.COL_LIMIT);
        assertEquals(18 * 30, TestVenue.TOTAL_SEATS);
        assertEquals(1000, TestVenue.EXPIRE_MIN);
        assertEquals(3000, TestVenue.EXPIRE_MAX);
        assertEquals(1, TestVenue.MIN_SEATS);
        assertEquals(20, TestVenue.MAX_SEATS);
        assertEquals(0, TestVenue.NUM_OF_SEATS_HELD.get());
        assertEquals(0, TestVenue.NUM_OF_SEATS_RESERVED.get());
//        assertEquals(0, TestVenue.mapOfSeatQueues.size());
//        assertEquals(0, TestVenue.mapOfReservedSeats.size());
    }


    @Test
    public void testPopulationOfSeatQueueMap() {
        TestVenue.populateMap();
        Assert.assertEquals(9, TestVenue.mapOfSeatQueues.size());

        //check priorities in map
        Set<Integer> keys = TestVenue.mapOfSeatQueues.keySet();
        Set<Integer> s = new HashSet<>();
        s.add(1);
        s.add(2);
        s.add(3);
        s.add(4);
        s.add(5);
        s.add(6);
        s.add(7);
        s.add(8);
        s.add(9);
        assertEquals(s, keys);
    }





}
