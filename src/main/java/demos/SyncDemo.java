package demos;

class Counter {
    static int count = 20000;

//    public static void decrement() {
//        count--;//count = count-1;
//    }

    public synchronized static void decrement() {
        count--;//count = count-1;
    }

}

class TunaCustomer implements Runnable {

    public void run() {
        try {
            for (int i = 10000; i > 0; i--) {
                Counter.decrement();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class SyncDemo {

    public static void main(String[] args) {
        Thread t1 = new Thread(new TunaCustomer(), "bobby");
        Thread t2 = new Thread(new TunaCustomer(), "joe");

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


//lambda ex.
//        Thread t2 = new Thread(() -> {
//            System.out.println("hello;");
//        }, "bobby");
