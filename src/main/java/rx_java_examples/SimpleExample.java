package rx_java_examples;

import io.reactivex.Observable;

public class SimpleExample {

	static String result;

	public static void main(String[] args) {
		Observable<String> observer = Observable.just("Hello RXJava.");
		observer.subscribe(s -> result = s);
		System.out.println(result);

	}

}
