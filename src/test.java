import java.io.*;
import java.util.ArrayList;


public class test {

    static String serializedObjectName = "cereal.ser";

    static void serializeObject(Object o) {     //For when a serialized obj file has not been created yet or can't be found
        try {
            FileOutputStream fileOut = new FileOutputStream(serializedObjectName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(o);
            out.close();
            fileOut.close();
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

        ArrayList<Person> people = new ArrayList<>();
        people.add(new Person("Scott", 19));
        people.add(new Person("Maddy", 19));

        File file = new File(String.format(".\\%s", serializedObjectName));

        if (file.isFile()) { //See if serialized object already exists
            System.out.println("file exists");
            ArrayList<Person> fetchedObject = (ArrayList) deserializeObject(serializedObjectName);
            for (Person p : fetchedObject) {
                System.out.println(p.name + ", " + p.age);
            }
        } else {             //Create serialized oject file since file not found
            System.out.println("file does not exist...yet");
            serializeObject(people);
        }

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