//not needed, just need to run several times to see difference.

//package demos;
//
//class Counter2 {
//     int count;
//
//    public  void increment() {
//        count++;//count = count+1;
//    }
//}
//
//class TunaCustomer2 implements Runnable {
//    Counter2 c;
//
//    public TunaCustomer2(Counter2 c){
//        this.c = c;
//    }
//
//    public void run() {
//        try {
//            for (int i = 0; i < 10000; i++) {
//                c.increment();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
//
//public class Sync2 {
//
//    public static void main(String[] args) {
//        Counter2 c = new Counter2(); //note the 2
//
//        //create threads
//        Thread t1 = new Thread(new TunaCustomer2(c), "bobby");
//        Thread t2 = new Thread(new TunaCustomer2(c), "joe");
//
//        //start them
//        t1.start();
//        t2.start();
//
//        //wait for them to finish and then join them to the main thread
//        try {
//            t1.join();
//            t2.join();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        //show count
//        System.out.println("final count: " + c.count);
//    }
//
//}
//
//
////lambda ex.
////        Thread t2 = new Thread(() -> {
////            System.out.println("hello;");
////        }, "bobby");
