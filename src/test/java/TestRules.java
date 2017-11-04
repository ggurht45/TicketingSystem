import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestRules {

    @BeforeClass
    public static void onceBeforeAll() {
        System.out.println(" Run this once before everything");
    }

    @Before
    public void before() {
        System.out.println(" Im the before method");
    }

    @After
    public void after() {
        System.out.println(" Im the after method");
    }

    @AfterClass
    public static void onceAfterAll() {
        System.out.println(" Run this once after everything");
    }

    @Rule
    public TestRule listen = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            super.succeeded(description);
            //logic for handling failed test cases
            System.out.println("test " + description.getMethodName() + " PASSED!");
        }

        @Override
        protected void failed(Throwable e, Description description) {
            super.failed(e, description);
            System.out.println("test " + description.getMethodName() + " FAILED!");
        }
    };
}
