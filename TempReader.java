public class TempReader {
    public static final int TEMP_READER_NUM = 8;

    public static void main(String[] args) {
        Thread[] readers = new Thread[TEMP_READER_NUM];

        startReaders(readers);
    }

    public static void startReaders(Thread[] readers) {
        for (int i = 0; i < TEMP_READER_NUM; i++) {
            // TODO: ADD RUNNABLE TO READER
            readers[i] = new Thread(new Reader(i));
            readers[i].start();
        }
        
        try {
            for (Thread reader : readers) {
                reader.join();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Reader implements Runnable {
    int ID;

    public Reader(int ID) {
        this.ID = ID;
    }

    public void run() {
        // TODO: Implement run
    }
}