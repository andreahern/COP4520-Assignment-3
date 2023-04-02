import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Stack;
import java.util.Collections;
import java.util.ArrayList;

public class BirthdayPresents {
    public static final int BIRTHDAY_PRESENTS_NUM = 500_000;
    public static final int SERVANTS_NUM = 4;

    private static ConcurrentLinkedDeque<Integer> orderedChain = new ConcurrentLinkedDeque<>();
    private static ReentrantLock chainLock = new ReentrantLock();

    private static AtomicInteger numThankYouNotes = new AtomicInteger(0);
    private static ArrayList<Integer> presentsBag = new ArrayList<>();

    public static void main(String[] args) {
        Thread[] servants = new Thread[SERVANTS_NUM];

        for (int i = 1; i <= BIRTHDAY_PRESENTS_NUM; i++)
            presentsBag.add(i);
        Collections.shuffle(presentsBag);

        startServants(servants);

        System.out.println("Finished! Num presents: " + BIRTHDAY_PRESENTS_NUM + ", Num thank you notes: "
                + numThankYouNotes.get());
    }

    private static void startServants(Thread[] servants) {
        try {
            for (int i = 0; i < SERVANTS_NUM; i++) {
                Servant servant = new Servant(i, BIRTHDAY_PRESENTS_NUM, SERVANTS_NUM, orderedChain, chainLock,
                        numThankYouNotes, presentsBag);
                servants[i] = new Thread(servant);
                servants[i].start();
                servants[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Servant implements Runnable {
    private int ID;
    private int BIRTHDAY_PRESENTS_NUM;
    private int SERVANTS_NUM;
    private ConcurrentLinkedDeque<Integer> orderedChain;
    private ReentrantLock chainLock;
    private AtomicInteger numThankYouNotes;
    private ArrayList<Integer> presentsBag;

    public Servant(int ID, int BIRTHDAY_PRESENTS_NUM, int SERVANTS_NUM, ConcurrentLinkedDeque<Integer> orderedChain,
            ReentrantLock chainLock, AtomicInteger numThankYouNotes, ArrayList<Integer> presentsBag) {
        this.ID = ID;
        this.BIRTHDAY_PRESENTS_NUM = BIRTHDAY_PRESENTS_NUM;
        this.SERVANTS_NUM = SERVANTS_NUM;
        this.orderedChain = orderedChain;
        this.chainLock = chainLock;
        this.numThankYouNotes = numThankYouNotes;
        this.presentsBag = presentsBag;
    }

    public void run() {
        boolean shouldWriteThankYou = false;
        int giftsToHandle = BIRTHDAY_PRESENTS_NUM / 4;
        for (int i = 0; i < giftsToHandle; i++) {
            if (shouldWriteThankYou) {
                writeThankYou();
            } else {
                addGiftToChain();
            }
        }

        shouldWriteThankYou = !shouldWriteThankYou;
    }

    private void writeThankYou() {
        Integer present = null;
        chainLock.lock();
        try {
            orderedChain.pollFirst();
        } finally {
            chainLock.unlock();
        }

        if (present != null) {
            numThankYouNotes.incrementAndGet();
        }
    }

    private void addGiftToChain() {
        int present = getPresentFromBag();
        chainLock.lock();
        try {
            Integer pred = orderedChain.peekLast();
            Stack<Integer> removed = new Stack<>();
            while (pred != null && pred > present) {
                pred = orderedChain.pollLast();
                removed.push(pred);
                pred = orderedChain.peekLast();
            }
            orderedChain.add(present);

            while (!removed.isEmpty()) {
                orderedChain.add(removed.pop());
            }
        } finally {
            chainLock.unlock();
        }
    }

    private int getPresentFromBag() {
        return presentsBag.remove(0);
    }
}