package rx_java_examples;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class e_02_Operators implements BaseRXJava {

	private static Logger log = Logger.getLogger(e_02_Operators.class);
	
	public static void main(String[] args) throws InterruptedException {
		
		e_02_Operators myObject = new e_02_Operators();
		
		/*
	     * Delay operator - the Thread.sleep of the reactive world, it's pausing for a particular 
	     * increment of time before emitting the whole range 
	     * events which are thus shifted by the specified time amount.
	     *
	     * The delay operator uses a Scheduler by default, which actually means it's
	     * running the operators and the subscribe operations on a different thread, 
	     * which means the test method
	     * will terminate before we see the text from the log. That is why we use the 
	     * CountDownLatch waiting for the
	     * completion of the stream.
	     *
	     */
		log.info("Starting on delay operation.");
		CountDownLatch latch = new CountDownLatch(1);
		Flowable.range(0, 3)
			.doOnNext(x -> log.info("Emitted: " + x))
			.delay(5, TimeUnit.SECONDS)
			.subscribe(
					tick -> log.info("Tick: " + tick),
					error -> log.error("Error: " + error),
					() -> {
						log.info("Completed");
						latch.countDown();
					});
		Helper.wait(latch);		
		
		/*
	     * Timer operator waits for a specific amount of time before
	     * it emits an event and then completes
	     */
		log.info("Starting timer operation.");
		Flowable<Long> flowable = Flowable.timer(5, TimeUnit.SECONDS);
		myObject.subscribeWithLogOutputWaitingForComplete(flowable);
		
		/*
		 * Delay operator with variable.
		 */
		log.info("Starting delay opearation with variable.");
		Flowable<Integer> flowable2 = Flowable.range(1, 5)
				.doOnNext(x -> log.info("Emitted: " + x))
				.delay(delay -> Flowable.timer(delay * 2, TimeUnit.SECONDS));
		myObject.subscribeWithLogOutputWaitingForComplete(flowable2);
				
		/*
	     * Periodically emits a number starting from 0 and then increasing the value on each emission
	     */
		log.info("Starting intreval operation.");
        Flowable<Long> flowable3 = Flowable.interval(1, TimeUnit.SECONDS)
                                    .take(5);
        myObject.subscribeWithLogOutputWaitingForComplete(flowable3);
        
        /*
         * scan operator - takes an initial value and a function(accumulator, currentValue). 
         * It goes through the events
         * sequence and combines the current event value with the previous result(accumulator) 
         * emitting downstream the
         * the function's result for each event(the initial value is used for the first event).
         */
        Flowable<Integer> numbers = Flowable.just(3, 5, -2, 9)
                .scan(0, (totalUntilNow, currentValue) -> {
                    log.info("totalUntilNow=" + totalUntilNow + ", emitted=" + currentValue);
                    return totalUntilNow + currentValue;
                });

        myObject.subscribeWithLog(numbers);
        
        /*
         * reduce operator acts like the scan operator but it only passes downstream the final result
         * (doesn't pass the intermediate results downstream) so the subscriber receives just one event
         */
        Single<Integer> numbers2 = Flowable.just(3, 5, -2, 9)
                .reduce(0, (totalUntilNow, x) -> {
                    log.info("totalSoFar=" + totalUntilNow + ", emitted=" + x);
                    return totalUntilNow + x;
                });
        myObject.subscribeWithLog(numbers2);
        
        /*
         * collect operator acts similar to the reduce() operator, but while the reduce()
         * operator uses a reduce function
         * which returns a value, the collect() operator takes a container supplie and
         * a function which doesn't return
         * anything(a consumer). The mutable container is passed for every event
         * and thus you get a chance to modify it
         * in this collect consumer function
         */
        Single<List<Integer>> numbers3 = Flowable.just(3, 5, -2, 9)
                .collect(ArrayList::new, (container, value) -> {
                    log.info("Adding " + value + " to container");
                    container.add(value);
                    //notice we don't need to return anything
                });
        myObject.subscribeWithLog(numbers3);
        
        /*
         * repeat resubscribes to the observable after it receives onComplete
         */
        Flowable<Integer> random = Flowable.defer(() -> {
            Random rand = new Random();
            return Flowable.just(rand.nextInt(20));
        })
        .repeat(5);

        myObject.subscribeWithLogOutputWaitingForComplete(random);
	}

}
