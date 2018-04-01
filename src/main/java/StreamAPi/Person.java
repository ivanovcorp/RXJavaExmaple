package StreamAPi;


import java.util.HashMap;

public class Person {
	private String name;
	private int age;
	private HashMap<String, Integer> marks;
	
	public Person(String studentName, int age, HashMap<String, Integer> marks)
	{
		this.name = studentName;
		this.age = age;
		this.marks = marks;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getAge() {
		return age;
	}
	
	public HashMap<String, Integer> getMarks() {
		return marks;
	}
}
