package demos;

class Counter {
    static int count = 20000;

    //note synchronized
    public synchronized static void decrement() {
        count--;//count = count-1;
    }
}

public class SyncDemo {

    //this will be executed inside the run method.
    public static void decrementCounter() {
        for (int i = 10000; i > 0; i--) {
            Counter.decrement();
        }
    }

    public static void main(String[] args) {
        //create threads; with lambda functions.
        Thread t1 = new Thread(() -> SyncDemo.decrementCounter(), "bobby");
        Thread t2 = new Thread(() -> SyncDemo.decrementCounter(), "joe");

        //start them
        t1.start();
        t2.start();

        //wait for them to finish and then join them to the main thread
        try {
            t1.join();
            t2.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //show count
        System.out.println("final count: " + Counter.count);
    }
}