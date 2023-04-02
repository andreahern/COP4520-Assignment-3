import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class BirthdayPresents {
    public static final int BIRTHDAY_PRESENTS_NUM = 500_000;
    public static final int SERVANTS_NUM = 4;

    public static void main(String[] args) {
        Thread[] servants = new Thread[SERVANTS_NUM];

        try {
            for (int i = 0; i < SERVANTS_NUM; i++) {
                servants[i] = new Thread();
                servants[i].start();
                servants[i].join();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();            
        }
    }

    private class Servant implements Runnable {
        private int ID;

        public Servant(int ID) {
            this.ID = ID;
        }

        public void run() {

        }
    }
}