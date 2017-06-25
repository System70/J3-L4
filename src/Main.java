
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;

public class Main {

    private final static Object mon = new Object();
    private static CountDownLatch cdl1;

    private volatile static char desiredLetter = 'A';

    interface LetterPrinter {
        void printLetter(char currentLetter, char nextLetter);
    }

    private final static String FILE_NAME = "threadFile.txt";

    interface String2FileWriter {
        void fileWrite(String string);
    }

    public static void main(String[] args) {

        // 1. Create 3 threads sout'ing own letter A, B, C 5 times in sequential order.
        // Use wait/notify/notifyAll
        LetterPrinter pl = (char currentLetter, char nextLetter) -> {
            synchronized (mon) {
                try {
                    for (int i = 0; i < 5; i++) {
                        while (desiredLetter != currentLetter)
                            mon.wait();
                        System.out.print(currentLetter);
                        desiredLetter = nextLetter;
                        mon.notifyAll();
                    }
                    cdl1.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        cdl1 = new CountDownLatch(3);
        ExecutorService es1 = Executors.newFixedThreadPool(3);
        es1.execute(new Thread(() ->  pl.printLetter('A', 'B')));
        es1.execute(new Thread(() ->  pl.printLetter('B', 'C')));
        es1.execute(new Thread(() ->  pl.printLetter('C', 'A')));
        es1.shutdown();
        try {
            cdl1.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\n");


        // 2. Create method containing 3 threads writing 10 data strings into file each 20 ms
        absolutelySmallMethod();
        System.out.println();


        // 3. Create class MFU simultaneously printing & scanning documents.
        // Printing or scanning 2 documents at the same time is not allowed (display messages
        // "Printing page: 1, 2, 3..." or "Scanning..." each 50 ms)
        CountDownLatch cdl3 = new CountDownLatch(4);
        MFU mfu = new MFU(cdl3);
        ExecutorService es3 = Executors.newFixedThreadPool(4);
        es3.execute(new Thread(() -> mfu.printDocument("Speech.txt", 5)));
        es3.execute(new Thread(() -> mfu.scanDocument("Report.doc", 8)));
        es3.execute(new Thread(() -> mfu.scanDocument("Letter.rtf", 2)));
        es3.execute(new Thread(() -> mfu.printDocument("Offer.odt", 2)));
        es3.shutdown();
        try {
            cdl3.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Jobs completed");
    }

    private static void absolutelySmallMethod() {
        CountDownLatch cdl2 = new CountDownLatch(3);
        String2FileWriter s2fw = (String string) -> {
            for (int i = 0; i < 10; i++) {
                try (FileWriter fw = new FileWriter(FILE_NAME, true)) {
                    fw.write(string + System.lineSeparator());
                    fw.flush();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cdl2.countDown();
            }
        };
        ExecutorService es2 = Executors.newFixedThreadPool(3);
        es2.execute(new Thread(() -> s2fw.fileWrite("String 1")));
        es2.execute(new Thread(() -> s2fw.fileWrite("String 2")));
        es2.execute(new Thread(() -> s2fw.fileWrite("String 3")));
        es2.shutdown();
        try {
            cdl2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("File " + FILE_NAME + " appended");
    }
}
