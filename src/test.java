import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class test {

    static String serializedObjectName = "cereal.ser";

    static void serializeObject(Object o, String filePath) {     //For when a serialized obj file has not been created yet or can't be found
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(o);
            out.close(); fileOut.close();
            System.out.printf("Serialized Object saved to: %s%n", serializedObjectName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Object deserializeObject(String fileName) {
        Object o = null;
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            o = in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return o;
    }

    public static void main(String[] args) {

//        ArrayList<Person> people = new ArrayList<>();
//        people.add(new Person("Scott", 19));
//        people.add(new Person("Maddy", 19));
//        people.add(new Person("Dijiste", 100));
//
//        String outputFilePath = "C:\\Users\\GooseAdmin\\IdeaProjects\\yelpProject\\testOutput";
//        File file = new File(String.format(outputFilePath + File.separator + serializedObjectName));
//        System.out.println(file);
//
//        if (file.isFile()) {    //Serializable exists already
//            ArrayList<Person> personArrayList = (ArrayList<Person>) deserializeObject(file.toString());
//            for (Person p : personArrayList) {
//                System.out.println(p.name + ", " + p.age);
//            }
//        } else {
//            serializeObject(people, file.toString());
//        }

        // Create int list and groups as well as remove the medoid values from the int list
        ArrayList<Integer> ints = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7,8,9));
        Group g1 = new Group(3);
        Group g2 = new Group(7);
        ints.remove(Integer.valueOf(3)); ints.remove(Integer.valueOf(7));

        for (int val : ints) {
            if (Math.abs(g1.center - val) < Math.abs(g2.center - val)) {
                g1.addToGroup(val);
            } else {
                g2.addToGroup(val);
            }
        }

        System.out.println("Group 1:");
        for (int a : g1.group) {
            System.out.println(a);
        }
        System.out.println("Group 2:");
        for (int a : g2.group) {
            System.out.println(a);
        }

    }

}

class Group {
    int center;
    ArrayList<Integer> group = new ArrayList<>();
    public Group(int center) {
        this.center = center;
        group.add(center);
    }
    public void addToGroup(int value) {
        group.add(value);
    }
}

class Person implements Serializable {

    String name;
    int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String toString() {

        return name + ", " + age;
    }

}