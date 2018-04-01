package rx_java_examples;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

public class e_08_Error implements BaseRXJava {

	private static Logger log = Logger.getLogger(e_08_Error.class);

	private static final ConcurrentHashMap<String, AtomicInteger> attemptsMap = new ConcurrentHashMap<>();

	public static void main(String[] args) {
		/*
		 * Exceptions are for exceptional situations. The Observable contract specifies
		 * that exceptions are terminal operations. There are however operator available
		 * for error flow control
		 */
		e_08_Error myObject = new e_08_Error();

		/*
		 * After the map() operator encounters an error, it triggers the error handler
		 * in the subscriber which also unsubscribes from the stream, therefore 'yellow'
		 * is not even sent downstream.
		 */
		Flowable<String> colors = Flowable.just("green", "blue", "red", "yellow").map(color -> {
			if ("red".equals(color)) {
				throw new RuntimeException("Encountered red");
			}
			return color + "*";
		}).map(val -> val + "XXX");

		myObject.subscribeWithLog(colors);

		/*
		 * The 'onErrorReturn' operator doesn't prevent the unsubscription from the
		 * 'colors' but it does translate the exception for the downstream operators and
		 * the final Subscriber receives it in the 'onNext()' instead in 'onError()'
		 */
		Flowable<String> colors2 = Flowable.just("green", "blue", "red", "yellow").map(color -> {
			if ("red".equals(color)) {
				throw new RuntimeException("Encountered red");
			}
			return color + "*";
		}).onErrorReturn(th -> "-blank-").map(val -> val + "XXX");

		myObject.subscribeWithLog(colors2);

		// onError with flatMap
		// flatMap encounters an error when it subscribes to 'red' substreams and thus
		// unsubscribe from
		// 'colors' stream and the remaining colors still are not longer emitted
		Flowable<String> colors3 = Flowable.just("green", "blue", "red", "white", "blue")
				.flatMap(color -> simulateRemoteOperation(color)).onErrorReturn(throwable -> "-blank-"); // onErrorReturn
																											// just has
																											// the
																											// effect of
																											// translating

		myObject.subscribeWithLog(colors3);

		log.info("*****************");

		// bellow onErrorReturn() is applied to the flatMap substream and thus
		// translates the exception to
		// a value and so flatMap continues on with the other colors after red
		colors3 = Flowable.just("green", "blue", "red", "white", "blue")
				.flatMap(color -> simulateRemoteOperation(color).onErrorReturn(throwable -> "-blank-") // onErrorReturn
																										// doesn't
																										// trigger
		// the onError() inside flatMap so it doesn't unsubscribe from 'colors'
		);

		myObject.subscribeWithLog(colors3);

		/*
		 * onErrorResumeNext() returns a stream instead of an exception and subscribes
		 * to that stream instead, useful for example to invoke a fallback method that
		 * returns also a stream
		 */
		Flowable<String> colors4 = Flowable.just("green", "blue", "red", "white", "blue")
				.flatMap(color -> simulateRemoteOperation(color).onErrorResumeNext(th -> {
					if (th instanceof IllegalArgumentException) {
						return Flowable.error(new RuntimeException("Fatal, wrong arguments"));
					}
					return fallbackRemoteOperation();
				}));

		myObject.subscribeWithLog(colors4);

		/*
		 ************* Retry Logic ****************
		 */

		/*
		 * timeout operator raises exception when there are no events incoming before
		 * it's predecessor in the specified time limit
		 * 
		 * retry() resubscribes in case of exception to the Observable
		 */
		Flowable<String> colors5 = Flowable.just("red", "blue", "green", "yellow")
				.concatMap(color -> myObject.delayedByLengthEmitter(TimeUnit.SECONDS, color)
						.timeout(6, TimeUnit.SECONDS).retry(2).onErrorResumeNext(Flowable.just("blank")));

		myObject.subscribeWithLog(colors5);
		// there is also

		/*
		 * When you want to retry based on the number considering the thrown exception
		 * type
		 */
		Flowable<String> colors6 = Flowable.just("blue", "red", "black", "yellow");

		colors6 = colors6
				.flatMap(colorName -> simulateRemoteOperation(colorName, 2).retry((retryAttempt, exception) -> {
					if (exception instanceof IllegalArgumentException) {
						log.error(colorName + " encountered non retry exception ");
						return false;
					}
					log.info("Retry attempt " + retryAttempt + " for " + colorName);
					return retryAttempt <= 3;
				}).onErrorResumeNext(Flowable.just("generic color")));

		myObject.subscribeWithLog(colors6);

		/*
		 * A more complex retry logic like implementing a backoff strategy in case of
		 * exception This can be obtained with retryWhen(exceptionObservable ->
		 * Observable)
		 * 
		 * retryWhen resubscribes when an event from an Observable is emitted. It
		 * receives as parameter an exception stream
		 * 
		 * we zip the exceptionsStream with a .range() stream to obtain the number of
		 * retries, however we want to wait a little before retrying so in the zip
		 * function we return a delayed event - .timer()
		 * 
		 * The delay also needs to be subscribed to be effected so we also need flatMap
		 */
		Flowable<String> colors7 = Flowable.just("blue", "green", "red", "black", "yellow");

		colors7 = colors7.flatMap(colorName -> simulateRemoteOperation(colorName, 3)
				.retryWhen(exceptionStream -> exceptionStream.zipWith(Flowable.range(1, 3), (exc, attempts) -> {
					// don't retry for IllegalArgumentException
					if (exc instanceof IllegalArgumentException) {
						return Flowable.error(exc);
					}

					if (attempts < 3) {
						log.info("Attempt " + attempts + ", waiting before retry");
						return Flowable.timer(2 * attempts, TimeUnit.SECONDS);
					}
					return Flowable.error(exc);
				}).flatMap(val -> val)).onErrorResumeNext(Flowable.just("generic color")));

		myObject.subscribeWithLog(colors7);

		/*
		 * repeatWhen is identical to retryWhen only it responds to 'onCompleted'
		 * instead of 'onError'
		 */
		Flowable<Integer> remoteOperation = Flowable.defer(() -> {
			Random random = new Random();
			return Flowable.just(random.nextInt(10));
		});

		remoteOperation = remoteOperation.repeatWhen(completed -> completed.delay(2, TimeUnit.SECONDS)).take(10);
		myObject.subscribeWithLogOutputWaitingForComplete(remoteOperation);
	}

	private static Flowable<String> simulateRemoteOperation(String color) {
		return simulateRemoteOperation(color, Integer.MAX_VALUE);
	}

	private static Flowable<String> simulateRemoteOperation(String color, int workAfterAttempts) {
		return Flowable.create(subscriber -> {
			AtomicInteger attemptsHolder = attemptsMap.computeIfAbsent(color, (colorKey) -> new AtomicInteger(0));
			int attempts = attemptsHolder.incrementAndGet();

			if ("red".equals(color)) {
				checkAndThrowException(color, attempts, workAfterAttempts,
						new RuntimeException("Color red raises exception"));
			}
			if ("black".equals(color)) {
				checkAndThrowException(color, attempts, workAfterAttempts,
						new IllegalArgumentException("Black is not a color"));
			}

			String value = "**" + color + "**";

			log.info("Emitting " + value);
			subscriber.onNext(value);
			subscriber.onComplete();
		}, BackpressureStrategy.BUFFER);
	}

	private static void checkAndThrowException(String color, int attempts, int workAfterAttempts, Exception exception) {
		if (attempts < workAfterAttempts) {
			log.info("Emitting " + exception.getClass() + " for " + color);
			throw new IllegalArgumentException("Black is not a color");
		} else {
			log.info("After attempt " + attempts + " we don't throw exception");
		}
	}

	private static Flowable<String> fallbackRemoteOperation() {
		return Flowable.just("blank");
	}
}
