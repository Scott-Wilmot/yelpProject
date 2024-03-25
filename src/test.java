import java.io.*;


public class test {

    static String serializedObjectName = "person.ser";

    static void serializeObject(Object o) {     //For when a serialized obj file has not been created yet or can't be found
        try {
            FileOutputStream fileOut = new FileOutputStream(serializedObjectName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(o);
            out.close();
            fileOut.close();
            System.out.println(String.format("Serialized Object saved to: %s", serializedObjectName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Person deserializeObject(String fileName) {
        Person p = null;
        try {
            FileInputStream fileIn = new FileInputStream(serializedObjectName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            p = (Person) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return p;
    }

    public static void main(String[] args) {

        Person p = new Person("Scott", 19);
        File file = new File(String.format(".\\%s", serializedObjectName));

        if (file.isFile()) { //See if serialized object already exists
            System.out.println("file exists");
            Person fetchedP = deserializeObject(serializedObjectName);
            System.out.println(fetchedP.name + ", " + fetchedP.age);
        } else {             //Create serialized oject file since file not found
            System.out.println("file does not exist...yet");
            serializeObject(p);
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

    public void info() {
        System.out.println(name + ", " + age);
    }

}