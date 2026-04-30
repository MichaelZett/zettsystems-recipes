package de.zettsystems.exercise;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StudentManagement {
    private static final String html =
            "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<body>\n" +
                    "\n" +
                    "<h1>My First Heading</h1>\n" +
                    "\n" +
                    "<p>My first paragraph.</p>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>\n";

    public static void main(String[] args) {
        List<Student> x = new ArrayList<>();
        x.add(new Graduate("Alice", 20, Gender.FEMALE, "Jever"));
        x.add(new Undergraduate("Bob", 17, Gender.MALE));
        x.add(new Graduate("Charlie", 21, Gender.OTHER, "Warsteiner"));

        instanceOfImprovable(x);

        final List<Student> adults = getAllAdults(x);
        System.out.printf("The adults are: %s.", adults);

        final List<Student> female = getAllFemales(x);
        System.out.printf("The females are: %s.", female);

        printHtml();
    }

    private static List<Student> getAllFemales(List<Student> x) {
        return x.stream()
                .filter(s -> s.getGender() == Gender.FEMALE)
                .collect(Collectors.toList());
    }

    private static List<Student> getAllAdults(List<Student> x) {
        return x.stream()
                .filter(s -> s.getAge() >= 18)
                .collect(Collectors.toUnmodifiableList());
    }

    private static void instanceOfImprovable(List<Student> x) {
        for (Student student : x) {
            if (student instanceof Undergraduate) {
                Undergraduate undergrad = (Undergraduate) student;
                System.out.printf("%s is an Undergraduate student.", undergrad.getName());
            } else if (student instanceof Graduate) {
                Graduate grad = (Graduate) student;
                System.out.printf("%s is a Graduate student.", grad.getName());
            }
        }
    }

    private static void printHtml() {
        System.out.println(html);
    }
}

interface Student {
    String getName();

    int getAge();

    Gender getGender();
}

class Undergraduate implements Student {
    private final String name;
    private final int age;
    private final Gender gender;

    public Undergraduate(String name, int age, Gender gender) {
        Objects.requireNonNull(name);
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public Gender getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return "Undergraduate{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                '}';
    }
}

class Graduate implements Student {
    private final String name;
    private final int age;
    private final Gender gender;
    private final String favouriteBeer;

    public Graduate(String name, int age, Gender gender, String favouriteBeer) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(favouriteBeer);
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.favouriteBeer = favouriteBeer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public Gender getGender() {
        return gender;
    }

    public String getFavouriteBeer() {
        return favouriteBeer;
    }

    @Override
    public String toString() {
        return "Graduate{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", favouriteBeer='" + favouriteBeer + '\'' +
                '}';
    }
}

enum Gender {
    MALE, FEMALE, OTHER
}
