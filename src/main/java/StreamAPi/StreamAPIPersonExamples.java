package StreamAPi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;


public class StreamAPIPersonExamples {

	private static Logger log = Logger.getLogger(StreamAPIPersonExamples.class);
	
	public static void main(String[] args) {

		List<Person2> persons =
			    Arrays.asList(
			        new Person2("Pesho", 18),
			        new Person2("Gosho", 23),
			        new Person2("Klementina", 23),
			        new Person2("David", 12));
		
		List<Person2> filtered = persons
				.stream()
				.filter(p -> p.name.startsWith("P"))
				.collect(Collectors.toList());
		log.info(filtered);
		
		Map<Integer, List<Person2>> personByAge = persons
				.stream()
				.collect(Collectors.groupingBy(p -> p.age));
		
		personByAge
			.forEach((age, p) -> log.info("age " + age + " : " + p));
		
		IntSummaryStatistics averageAge = persons
				.stream()
				.collect(Collectors.summarizingInt(p -> p.age));
		log.info(averageAge.getAverage());
		
		String phrase = persons
				.stream()
				.filter(p -> p.age >= 10)
				.map(p -> p.name)
				.collect(Collectors.joining(" and ", "In Bulgaria ", " are of legal age."));
		log.info(phrase);
		
		Map<Integer, String> map = persons
				.stream()
				.collect(Collectors.toMap(
						p -> p.age,
						p -> p.name,
						(name1, name2) -> name1 + ";" + name2));
		log.info(map);		
		
		Collector<Person2, StringJoiner, String> personNameCollector = 
				Collector.of(
						() -> new StringJoiner(" | "),
						(j, p) -> j.add(p.name.toUpperCase()),
						(j1, j2) -> j1.merge(j2),
						StringJoiner::toString);		
		String names = persons
				.stream()
				.collect(personNameCollector);
		log.info(names);
		
		
		List<Foo> foos = new ArrayList<>();
		
		IntStream
		    .range(1, 4)
		    .forEach(i -> foos.add(new Foo("Foo" + i)));
		
		foos.forEach(f ->
		    IntStream
		        .range(1, 4)
		        .forEach(i -> f.bars.add(new Bar("Bar" + i + " <- " + f.name))));
		
		foos.stream()
	    .flatMap(f -> f.bars.stream())
	    .forEach(b -> log.info(b.name));
		
	}

}
