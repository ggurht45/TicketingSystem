import org.junit.*;

/**
 * Created by jahangir on 10/29/2017.
 */
public class TestSuite_1 extends TestRules{
    //create first test. using test annotation. also need to use public void
    @Test
    public void testAdditionMethod(){
        System.out.println("wohoo! I am testing add method");
//        Assert.assertTrue(false);
        int x = Main.add(3,2);
        Assert.assertEquals(5, x);

    }

    @Test
    public void test2(){
        System.out.println("wohoo! I am test ");
    }

}
