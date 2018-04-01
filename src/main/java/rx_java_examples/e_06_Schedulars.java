package rx_java_examples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class e_06_Schedulars implements BaseRXJava {

	private static Logger log = Logger.getLogger(e_06_Schedulars.class);

	public static void main(String[] args) {

		e_06_Schedulars myObject = new e_06_Schedulars();
		/*
		 * RxJava provides some high level concepts for concurrent execution, like
		 * ExecutorService we're not dealing with the low level constructs like creating
		 * the Threads ourselves. Instead we're using a which create Workers who are
		 * responsible for scheduling and running code. By default RxJava will not
		 * introduce concurrency and will run the operations on the subscription thread.
		 *
		 * There are two methods through which we can introduce Schedulers into our
		 * chain of operations: - <b>subscribeOn allows to specify which Scheduler
		 * invokes the code contained in the lambda code for Observable.create() -
		 * observeOn allows control to which Scheduler executes the code in the
		 * downstream operators
		 *
		 * RxJava provides some general use Schedulers already implemented: -
		 * Schedulers.computation() - to be used for CPU intensive tasks. A threadpool
		 * equal to the numbers of available CPUs - Schedulers.io() - to be used for IO
		 * bound tasks - Schedulers.from(Executor) - custom ExecutorService -
		 * Schedulers.newThread() - always creates a new thread when a worker is needed.
		 * Since it's not thread pooled and always creates a new thread instead of
		 * reusing one, this scheduler is not very useful
		 *
		 * Although we said by default RxJava doesn't introduce concurrency, some
		 * operators that involve waiting like 'delay', 'interval' need to run on a
		 * Scheduler, otherwise they would just block the subscribing thread. By default
		 * **Schedulers.computation()** is used, but the Scheduler can be passed as a
		 * parameter.
		 */
		log.info("Starting");

		Observable.<Integer>create(subscriber -> {
			log.info("Someone subscribed");
			subscriber.onNext(1);
			subscriber.onNext(2);

			subscriber.onComplete();
		}).map(val -> {
			int newValue = val * 10;
			log.info("Mapping " + val + " to " + newValue);
			// Helpers.sleepMillis(2000);
			return newValue;
		}).subscribe(myObject.logNext());

		// subscribing thread
		CountDownLatch latch = new CountDownLatch(1);

		Observable<Integer> observable = Observable.<Integer>create(subscriber -> {
			log.info("Someone subscribed");
			new Thread(() -> {
				log.info("Emitting..");
				subscriber.onNext(1);
				subscriber.onComplete();
			}, "custom-thread").start();
		}).map(val -> {
			int newValue = val * 10;
			log.info("Mapping " + val + " to " + newValue);

			return newValue;
		});

		observable.subscribe(myObject.logNext(), myObject.logError(), myObject.logComplete(latch));
		Helper.wait(latch);

		log.info("Blocking Subscribe");
		observable.blockingSubscribe(myObject.logNext(), myObject.logError(), myObject.logComplete());
		observable.observeOn(Schedulers.trampoline());
		log.info("Got");

		/*
		 * subscribeOn allows to specify which Scheduler invokes the code contained in
		 * the lambda code for Observable.create()
		 */
		log.info("Starting");

		Observable<Integer> observable2 = Observable.create(subscriber -> { // code that will execute inside the IO
																			// ThreadPool
			log.info("Starting slow network op");
			Helper.sleepMillis(2000);

			log.info("Emitting 1st");
			subscriber.onNext(1);

			subscriber.onComplete();
		});

		observable2 = observable2.subscribeOn(Schedulers.io()) // Specify execution on the IO Scheduler
				.map(val -> {
					int newValue = val * 10;
					log.info("Mapping " + val + " to " + newValue);
					return newValue;
				});

		myObject.subscribeWithLogOutputWaitingForComplete(observable2);

		/*
		 * observeOn switches the thread that is used for the subscribers downstream. If
		 * we initially subscribedOn the IoScheduler we and we further make another .
		 */
		log.info("Starting");

		Flowable<Integer> observable3 = myObject.simpleFlowable().subscribeOn(Schedulers.io())
				.observeOn(Schedulers.computation()).map(val -> {
					int newValue = val * 10;
					log.info("Mapping " + val + " to " + newValue);
					return newValue;
				}).observeOn(Schedulers.newThread());

		myObject.subscribeWithLogOutputWaitingForComplete(observable3);

		/*
		 * Multiple calls to subscribeOn have no effect, just the first one will take
		 * effect, so we'll see the code execute on an IoScheduler thread.
		 */
		log.info("Starting");

		Flowable<Integer> observable4 = myObject.simpleFlowable().subscribeOn(Schedulers.io())
				.subscribeOn(Schedulers.computation()).map(val -> {
					int newValue = val * 2;
					log.info("Mapping new val " + newValue);
					return newValue;
				});

		myObject.subscribeWithLogOutputWaitingForComplete(observable4);

		/*
		 * Controlling concurrency in flatMap
		 *
		 * By using subscribeOn in flatMap you can control the thread on which flapMap
		 * subscribes to the particular stream. By using a scheduler from a custom
		 * executor to which we allow a limited number of threads, we can also control
		 * how many concurrent threads are handling stream operations inside the flatMap
		 */
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);

		Flowable<String> observable5 = Flowable.range(1, 5).observeOn(Schedulers.io()) // Scheduler for multiply
				.map(val -> {
					log.info("Multiplying " + val);
					return val * 10;
				}).flatMap(val -> simulateRemoteOp(val).subscribeOn(Schedulers.from(fixedThreadPool)));

		myObject.subscribeWithLogOutputWaitingForComplete(observable5);
	}

	private static Flowable<String> simulateRemoteOp(Integer val) {
		return Single.<String>create(subscriber -> {
			log.info("Simulate remote call " + val);
			Helper.sleepMillis(3000);
			subscriber.onSuccess("***" + val + "***");
		}).toFlowable();
	}
}
