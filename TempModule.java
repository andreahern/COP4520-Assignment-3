import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;

public class TempModule {
    public static final int TEMP_READER_NUM = 8;
    public static final int MIN_TEMP = -100;
    public static final int MAX_TEMP = 70;

    public static final int MINUTE_BUFFER = 60;
    public static final int ANALYZE_INTERVAL = 10;
    public static final int TEMPS_RECORDED_IN_HOUR = 5;

    public static final int WAIT_TIME = 1000;

    static ReentrantLock reader_lock = new ReentrantLock();
    static double[][] hour_memory = new double[TEMP_READER_NUM][MINUTE_BUFFER];
    static Random rand = new Random();

    public static void main(String[] args) {
        TempModule temp_module = new TempModule();

        temp_module.start();
    }

    public void start() {
        Thread[] readers = new Thread[TEMP_READER_NUM];
        double[] hour_highest_temp = new double[TEMPS_RECORDED_IN_HOUR];
        double[] hour_lowest_temp = new double[TEMPS_RECORDED_IN_HOUR];
        double maxDiff = 0;

        for (int i = 0; i < TEMPS_RECORDED_IN_HOUR; i++) {
            hour_highest_temp[i] = MIN_TEMP;
            hour_lowest_temp[i] = MAX_TEMP;
        }

        startReaders(readers);

        while (true) {
            System.out.println(Arrays.toString(hour_memory[0]));
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startReaders(Thread[] readers) {
        for (int i = 0; i < TEMP_READER_NUM; i++) {
            readers[i] = new Thread(new Reader(i));
            readers[i].start();
        }
    }

    private class Reader implements Runnable {
        int ID;
        int buffer_index;

        public Reader(int ID) {
            this.ID = ID;
            this.buffer_index = 0;
        }

        public void run() {
            while (true) {
                double temp = rand.nextDouble() * (MAX_TEMP - MIN_TEMP) + MIN_TEMP;
                reader_lock.lock();

                try {
                    hour_memory[ID][buffer_index] = temp;
                } finally {
                    reader_lock.unlock();
                }
                
                try {
                    Thread.sleep(WAIT_TIME);
                    buffer_index =(buffer_index + 1) % MINUTE_BUFFER;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}