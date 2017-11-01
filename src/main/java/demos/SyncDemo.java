package demos;

class Counter {
    static int count;

    public static void increment() {
        count++;//count = count+1;
    }
}

//class CounterNonStatic {
//    int count;
//
//    public void increment() {
//        count++;//count = count+1;
//    }
//}

class TunaCustomer implements Runnable {
//    CounterNonStatic c;
//
//    public TunaCustomer(CounterNonStatic cns){
//        c = cns;
//    }

    public void run() {
        try {
            for (int i = 0; i < 10000; i++) {
                Counter.increment();
//                c.increment();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class SyncDemo {

    public static void main(String[] args) {
//        CounterNonStatic cns = new CounterNonStatic();

        //create threads
//        Thread t1 = new Thread(new TunaCustomer(cns), "bobby");
//        Thread t2 = new Thread(new TunaCustomer(cns), "joe");
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
//        System.out.println("final count (cns): " + cns.count);
    }

}


//lambda ex.
//        Thread t2 = new Thread(() -> {
//            System.out.println("hello;");
//        }, "bobby");
