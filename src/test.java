import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;


public class test {

    static String serializedObjectName = "cereal.ser";
    final static String bucketFile = "BUCKETS";
    final static String indexFile = "INDEX";

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        PHT pht = new PHT();
        if (pht.fileCheck()) { // Serializables loaded to hash table
            //go right to getting idf and prompting input
        } else { // Need to generate new PHT
            //get businesses and put their names with id's in the PHT
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