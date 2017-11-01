import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestSuite_1.class
})
public class TestPrimer {

    public static void main(String[] args){
        JUnitCore.runClasses(TestPrimer.class);
    }
}

