import java.util.*;
import java.io.Serializable;

public class Business implements Serializable {
    String name;
    String review;
    String ID;

    //Hashtable<String, Float> tfTable = new Hashtable<>();
    float similarityValue;

    // Assignment 3
    double latitude;
    double longitude;

    public Business(String name, String review, String ID, double latitude, double longitude) {
        this.name = name;
        this.review = review;
        this.ID = ID;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public HashSet<String> getWordSet() {   //Responsible for generating wordSet if one does not exist for object yet
        ArrayList<String> words = new ArrayList<>(Arrays.asList(review.split("\\W+")));
        words.replaceAll(String::toLowerCase);
        HashSet<String> wordSet = new HashSet<>(words);
        return wordSet;
    }

}
