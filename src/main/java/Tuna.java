import java.util.*;


public class Tuna implements Runnable {
    String name;
    int time;
    String seat;
    Random r = new Random();


    public Tuna(String n) {
        name = n;
        time = 200;//r.nextInt(999);
    }

    public void run() {
        try {
            System.out.println(name + " is sleeping for " + time);
            Thread.sleep(time);
            getSeat();
            System.out.println(name + " is done sleeping and picked seat: " + seat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSeat(){
        seat = Main.s.pop();
//        System.out.println("poped another seat: " + Main.s);
    }
}
