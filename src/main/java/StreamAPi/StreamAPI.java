package StreamAPi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

public class StreamAPI {

	private static Logger log = Logger.getLogger(StreamAPI.class);
	
	public static void main(String[] args) {
		
		List<String> names = Arrays.asList("a1", "b2", "c3", "d4", "e5" );
		names
			.stream()
			.filter(s -> s.startsWith("c"))
			.map(String::toLowerCase)		
			.forEach(log::info); // prints c3
		log.info("\n");
		List<Integer> nums = Arrays.asList(1, 65, 12, 4, 9, 64, 22, 11);
		List<Integer> evenNums = nums.stream().filter(x -> x % 2 == 0).collect(Collectors.toList());
		evenNums.forEach(log::info);
		List<String> numsAsString = nums.stream().map(x -> x = x * 10).map(x -> String.valueOf(x)).collect(Collectors.toList());
				
		System.out.println();
		System.out.print("Nums as Strings: ");
		numsAsString.forEach(x -> log.info(x + " "));
		log.info("\n");
		
		log.info("\n");
		HashMap<String, Integer> marks = new HashMap<>();
		marks.put("Math", 5);
		marks.put("CS", 6);
		marks.put("Physics", 3);
		Person ivan = new Person("Ivan", 23, marks);
		marks = new HashMap<>();
		marks.put("Math", 2);
		marks.put("CS", 3);
		marks.put("Physics", 4);
		Person pesho = new Person("Pesho", 18, marks);
		marks = new HashMap<>();
		marks.put("Math", 6);
		marks.put("CS", 3);
		marks.put("Physics", 2);
		Person gosho = new Person("Gosho", 21, marks);
		List<Person> students = Arrays.asList(ivan, pesho, gosho);
		students.stream().filter(s1 -> s1.getMarks().get("Math").compareTo(4) > 0).forEach(s -> log.info(s.getName() + " " + s.getAge()));
		
		log.info("\n");
		log.info("Find first: ");
		Stream.of(10, 20, 30, 40, 50)
			.findFirst()
			.ifPresent(log::info);
		
		log.info("\n");
		log.info("IntStream: ");
		IntStream.range(1, 4).forEach(log::info);
		log.info("\n");
		
		
		log.info("\n");
		log.info("Average: ");
		Arrays.stream(new int[] { 1, 2, 3})
			.map(n -> 2 * n + 1)
			.average()
			.ifPresent(log::info);
		
		Stream.of("a1", "a2", "a3")
			.map(s -> s.substring(1))
			.mapToInt(Integer::parseInt)
			.max()
			.ifPresent(log::info);
		
		
		IntStream.range(1, 4)
			.mapToObj(i -> "a" + i)
			.forEach(log::info);
		
		Stream.of(1.0, 2.0, 3.0)
			.mapToInt(Double::intValue)
			.mapToObj(x -> "a" + x)
			.forEach(log::info);
		
		Stream.of("d1", "a2", "b1", "b3", "c")
			.filter(s -> {
				log.info("filter: " + s);
				return true;
			})
			.forEach(s -> log.info("forEach: " + s));
		
		Stream.of("d1", "a2", "b1", "b3", "c")
			.map(s -> {
				log.info("map: " + s);
				return s.toUpperCase();
			})
			.anyMatch(s -> {
				log.info("anyMatch: " + s);
				return s.startsWith("A");
			});
		
		System.out.println();
		Stream.of("d1", "a2", "b1", "b3", "c")
		.map(s -> {
			log.info("map: " + s);
			return s.toUpperCase();
		})
		.filter(s -> {
			log.info("map " + s);
			return s.startsWith("A");
		})
		.forEach(s -> log.info("forEach: " + s));
		log.info("\n");		
		
		Stream.of("d1", "a2", "b1", "b3", "c")
		.filter(s -> {
			log.info("map " + s);
			return s.startsWith("A");
		})
		.map(s -> {
			log.info("map: " + s);
			return s.toUpperCase();
		})		
		.forEach(s -> log.info("forEach: " + s));
		
		log.info("\n");
		Stream.of("d2", "a2", "b1", "b3", "c")
	    .sorted((s1, s2) -> {
	        System.out.printf("sort: %s; %s\n", s1, s2);
	        return s1.compareTo(s2);
	    })
	    .filter(s -> {
	        log.info("filter: " + s);
	        return s.startsWith("a");
	    })
	    .map(s -> {
	        log.info("map: " + s);
	        return s.toUpperCase();
	    })
	    .forEach(s -> log.info("forEach: " + s));
		
		
		log.info("\n");
		Stream.of("d2", "a2", "b1", "b3", "c")
	    .filter(s -> {
	        log.info("filter: " + s);
	        return s.startsWith("a");
	    })
	    .sorted((s1, s2) -> {
	        System.out.printf("sort: %s; %s\n", s1, s2);
	        return s1.compareTo(s2);
	    })
	    .map(s -> {
	        log.info("map: " + s);
	        return s.toUpperCase();
	    })
	    .forEach(s -> log.info("forEach: " + s));
	}

}