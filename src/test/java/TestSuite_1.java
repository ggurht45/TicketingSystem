import org.junit.*;

public class TestSuite_1 extends TestRules {
    @Test
    public void test1() {
        int x = 3+2;
        Assert.assertEquals(5, x);

    }

}
