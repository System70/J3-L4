import java.util.concurrent.CountDownLatch;

public class MFU {

    private final int MODE_PRINT = 0;
    private final int MODE_SCAN = 1;
    private Object lockPrint = new Object();
    private Object lockScan = new Object();
    private CountDownLatch cdl;

    public MFU(CountDownLatch cdl) {
        this.cdl = cdl;
    }

    interface MFUtask {
        void doTask(int mode, String documentTitle, int numberOfPages);
    }

    MFUtask mfuTask = (int mode, String documentTitle, int numberOfPages) -> {
        synchronized ((mode == MODE_PRINT) ? lockPrint : lockScan) {
            for (int i = 0; i < numberOfPages; i++) {
                System.out.println(((mode == MODE_PRINT) ? "Printing " : "Scanning ") +
                        documentTitle + ": page " + (i + 1) + " of " + numberOfPages);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.cdl.countDown();
        }
    };

    public void printDocument(String documentTitle, int numberOfPages) {
        mfuTask.doTask(MODE_PRINT, documentTitle, numberOfPages);
    }

    public void scanDocument(String documentTitle, int numberOfPages) {
        mfuTask.doTask(MODE_SCAN, documentTitle, numberOfPages);
    }
}
