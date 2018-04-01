package rx_java_examples;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

public class Helper {
	
	private static Logger log = Logger.getLogger(Helper.class);
	
	public static void wait(CountDownLatch waitOn) {
        try {
            waitOn.await();
        } catch (InterruptedException e) {
            log.error("Interrupted waiting on CountDownLatch");
            throw new RuntimeException("Interrupted thread");
        }
    }
	
	public static void sleepMillis(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("Interrupted Thread");
            throw new RuntimeException("Interrupted thread");
        }
    }
}
