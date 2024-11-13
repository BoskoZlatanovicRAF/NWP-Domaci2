package example.model;

public class Student {
    private String id;
    private String name;
    private int year;

    public Student(String id, String name, int year) {
        this.id = id;
        this.name = name;
        this.year = year;
    }

    // getteri i setteri
    public String getId() { return id; }
    public String getName() { return name; }
    public int getYear() { return year; }
}