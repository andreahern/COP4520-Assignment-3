import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;

public class TempModule {
    public static final int TEMP_READER_NUM = 8;
    public static final int MIN_TEMP = -100;
    public static final int MAX_TEMP = 70;

    public static final int MINUTE_BUFFER = 60;
    public static final int ANALYZE_INTERVAL = 10;
    public static final int TEMPS_RECORDED_IN_HOUR = 5;

    public static final int READER_WAIT_TIME = 100;
    public static final int ANALYZER_WAIT_TIME = 600;

    static ReentrantLock reader_lock = new ReentrantLock();
    static ReentrantLock analyzer_lock = new ReentrantLock();
    static AtomicBoolean reportReady = new AtomicBoolean(false);

    static int[][] hour_memory = new int[TEMP_READER_NUM][MINUTE_BUFFER];
    static int[] hour_highest_temp = new int[TEMPS_RECORDED_IN_HOUR];
    static int[] hour_lowest_temp = new int[TEMPS_RECORDED_IN_HOUR];
    static double max_diff = 0;

    static Random rand = new Random();

    public static void main(String[] args) {
        TempModule temp_module = new TempModule();

        temp_module.start();
    }

    public void start() {
        Thread[] readers = new Thread[TEMP_READER_NUM];

        for (int i = 0; i < TEMPS_RECORDED_IN_HOUR; i++) {
            hour_highest_temp[i] = MIN_TEMP;
            hour_lowest_temp[i] = MAX_TEMP;
        }

        startAnalyzer();
        startReaders(readers);
    }

    private void startReaders(Thread[] readers) {
        for (int i = 0; i < TEMP_READER_NUM; i++) {
            readers[i] = new Thread(new Reader(i));
            readers[i].start();
        }
    }

    private void startAnalyzer() {
        Thread analyzer = new Thread(new Analyzer());
        analyzer.start();
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
                int temp = (int) (rand.nextDouble() * (MAX_TEMP - MIN_TEMP) + MIN_TEMP);
                reader_lock.lock();

                try {
                    hour_memory[ID][buffer_index] = temp;
                } finally {
                    reader_lock.unlock();
                }

                try {
                    Thread.sleep(READER_WAIT_TIME);
                    buffer_index = (buffer_index + 1) % MINUTE_BUFFER;

                    if (buffer_index == 0) {
                        reportReady.set(true);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Analyzer implements Runnable {

        public void run() {
            while (true) {
                if (!reportReady.get()) {
                    continue;
                }

                reader_lock.lock();
                analyzer_lock.lock();

                try {
                    sort_memory();
                    for (int[] reader_memory : hour_memory) {
                        System.out.println(Arrays.toString(reader_memory));
                    }

                    for (int i = 0; i < TEMPS_RECORDED_IN_HOUR; i++) {
                        hour_highest_temp[i] = hour_memory[TEMP_READER_NUM - 1][MINUTE_BUFFER - 1 - i];
                        hour_lowest_temp[i] = hour_memory[0][i];
                    }

                    System.out.println("Highest Temperatures: " + Arrays.toString(hour_highest_temp));
                    System.out.println("Lowest Temperatures: " + Arrays.toString(hour_lowest_temp));

                } finally {
                    reader_lock.unlock();
                    analyzer_lock.unlock();
                    reportReady.set(false);
                }

                try {
                    Thread.sleep(ANALYZER_WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sort_memory() {
            for (int i = 0; i < TEMP_READER_NUM; i++) {
                for (int j = 0; j < MINUTE_BUFFER; j++) {
                    for (int k = 0; k < TEMP_READER_NUM; k++) {
                        for (int l = 0; l < MINUTE_BUFFER; l++) {
                            if (hour_memory[i][j] < hour_memory[k][l]) {
                                int temp = hour_memory[i][j];
                                hour_memory[i][j] = hour_memory[k][l];
                                hour_memory[k][l] = temp;
                            }
                        }
                    }
                }
            }
        }
    }
}