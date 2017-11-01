import java.util.Stack;

import org.joda.time.LocalTime;


/**
 * Created by jahangir on 10/29/2017.
 */
public class Main {


    public static int add(int a, int b) {
        return a + b;
    }

    public static Stack<String> s = new Stack<>();
    public static void main(String[] args) throws Exception{
        LocalTime time = new LocalTime();
        System.out.println("current time is: " + time);

        s.push("seat1");
        s.push("seat2");
        s.push("seat3");
        s.push("seat4");
        s.push("seat5");
        System.out.println("the stack: " + s);
//
//
//        //create a few customers and give them seats.
//        Customer c1 = new Customer("c1", s.pop());
//        Customer c2 = new Customer("c2", s.pop());
//        Customer c3 = new Customer("c3", s.pop());
//
//        System.out.println(c1.toString()+c2.toString()+c3.toString());


        Thread t1 = new Thread(new Tuna("one"));
        Thread t2 = new Thread(new Tuna("two"));
        Thread t3 = new Thread(new Tuna("three"));
        Thread t4 = new Thread(new Tuna("four"));

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        //should be randomly able to check the current status of the stack.

        //
        System.out.println();
        System.out.println("is t1 still alive? before the join: " + t1.isAlive());

        //wait for the 2 threads to finish and join into the main thread, before proceeding with the next code.
        t1.join();
        t2.join();

        System.out.println("is t1 still alive? after the join: " + t1.isAlive());
        System.out.println("the stack at the end: " + s);

    }


}

class Customer {
    String name;
    String seat = "";

    public Customer(String n, String s) {
        name = n;
        seat = s;
    }

    public void receiveSeat(String s) {
        seat = s;
    }

    public String releaseSeat() {
        String tmp;
        if (seat != null && seat != "") {
            tmp = seat;
            seat = "";
            return seat;
        }
        return ""; //or throw exception.
    }

    @Override
    public String toString() {
        return "Customer{" +
                "name='" + name + '\'' +
                ", seat='" + seat + '\'' +
                '}';
    }
}
