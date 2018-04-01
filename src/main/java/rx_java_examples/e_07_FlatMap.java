package rx_java_examples;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.flowables.GroupedFlowable;
import io.reactivex.schedulers.Schedulers;
import javafx.util.Pair;

public class e_07_FlatMap implements BaseRXJava {

	private static Logger log = Logger.getLogger(e_07_FlatMap.class);

	public static void main(String[] args) {

		e_07_FlatMap myObject = new e_07_FlatMap();

		/*
		 * The flatMap operator is so important and has so many different uses it
		 * deserves it's own category to explain I like to think of it as a sort of
		 * fork-join operation because what flatMap does is it takes individual items
		 * and maps each of them to an Observable(so it creates new Streams from each
		 * object) and then 'flattens' the events from these Streams back into a single
		 * Stream. Why this looks like fork-join because for each element you can fork
		 * some async jobs that emit some items before completing, and these items are
		 * sent downstream to the subscribers as coming from a single stream
		 *
		 * RuleOfThumb 1: When you have an 'item' as parameter and you need to invoke
		 * something that returns an Observable<T> instead of <T>, you need flatMap
		 * RuleOfThumb 2: When you have Observable<Observable<T>> you probably need
		 * flatMap.
		 */

		/*
		 * Common usecase when for each item you make an async remote call that returns
		 * a stream of items (an Observable<T>)
		 *
		 * The thing to notice that it's not clear upfront that events from the flatMaps
		 * 'substreams' don't arrive in a guaranteed order and events from a substream
		 * might get interleaved with the events from other substreams.
		 */
		Flowable<String> colors = Flowable.just("orange", "red", "green")
				.flatMap(colorName -> simulateRemoteOperation(colorName));

		myObject.subscribeWithLogOutputWaitingForComplete(colors);

		/*
		 * Inside the flatMap we can operate on the substream with the same stream
		 * operators like for ex count
		 */
		Flowable<String> colors2 = Flowable.just("orange", "red", "green", "blue");

		Flowable<Pair<String, Long>> colorsCounted = colors2.flatMap(colorName -> {
			Flowable<Long> timer = Flowable.interval(2, TimeUnit.SECONDS);

			return simulateRemoteOperation(colorName) // <- Still a stream
					.zipWith(timer, (val, timerVal) -> val).count().map(counter -> new Pair<>(colorName, counter))
					.toFlowable();
		});

		myObject.subscribeWithLogOutputWaitingForComplete(colorsCounted);

		/*
		 * Controlling the level of concurrency of the substreams. In the ex. below,
		 * only one of the substreams(the Observables returned by
		 * simulateRemoteOperation) is subscribed. As soon the substream completes,
		 * another substream is subscribed. Since only one substream is subscribed at
		 * any time, this way we don't see any values interleaved
		 */
		Flowable<String> colors3 = Flowable.just("orange", "red", "green")
				.flatMap(colorName -> simulateRemoteOperation(colorName), 1);

		myObject.subscribeWithLogOutputWaitingForComplete(colors3);

		/*
		 * As seen above flatMap might mean that events emitted by multiple streams
		 * might get interleaved
		 *
		 * concatMap operator acts as a flatMap with 1 level of concurrency which means
		 * only one of the created substreams(Observable) is subscribed and thus only
		 * one emits events so it's just this substream which emits events until it
		 * finishes and a new one will be subscribed and so on
		 */
		Flowable<String> colors4 = Flowable.just("orange", "red", "green", "blue").subscribeOn(Schedulers.io())
				.concatMap(val -> simulateRemoteOperation(val));

		myObject.subscribeWithLogOutputWaitingForComplete(colors4);

		/*
		 * When you have a Stream of Streams - Observable<Observable<T>>
		 */
		Flowable<String> colors5 = Flowable.just("red", "green", "blue", "red", "yellow", "green", "green");

		Flowable<GroupedFlowable<String, String>> groupedColorsStream = colors5.groupBy(val -> val);// grouping key
		// is the String itself, the color

		Flowable<Pair<String, Long>> countedColors = groupedColorsStream.flatMap(groupedFlow -> groupedFlow.count()
				.map(countVal -> new Pair<>(groupedFlow.getKey(), countVal)).toFlowable());

		myObject.subscribeWithLogOutputWaitingForComplete(countedColors);

		/*
		 * 'switchIfEmpty' push some value(s) when the original stream just completes
		 * without 'returning' anything
		 */
		Flowable<String> colors6 = Flowable.just("red", "", "blue")
				.flatMap(colorName -> simulateRemoteOperation(colorName).switchIfEmpty(Flowable.just("NONE")));

		myObject.subscribeWithLogOutputWaitingForComplete(colors6);
	}

	/**
	 * Simulated remote operation that emits as many events as the length of the
	 * color string
	 * 
	 * @param color
	 *            color
	 * @return stream of events
	 */
	private static Flowable<String> simulateRemoteOperation(String color) {
		return Flowable.<String>create(subscriber -> {
			for (int i = 0; i < color.length(); i++) {
				subscriber.onNext(color + i);
				Helper.sleepMillis(200);
			}

			subscriber.onComplete();
		}, BackpressureStrategy.MISSING);
	}
}
