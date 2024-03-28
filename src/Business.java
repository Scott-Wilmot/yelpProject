import java.util.*;
import java.io.Serializable;

public class Business implements Serializable {
    String name;
    String review;
    String ID;
    float similarityValue;
    public Business(String name, String review, String ID) {
        this.name = name;
        this.review = review;
        this.ID = ID;
    }
    public Set<String> getWordSet() {   //Responsible for generating wordSet if one does not exist for object yet
        ArrayList<String> words = new ArrayList<>(Arrays.asList(review.split("\\W+")));
        words.replaceAll(String::toLowerCase);
        Set<String> wordSet = new HashSet<>(words);
        return wordSet;
    }
    public void setSimilarityValue(float similarityValue) {
        this.similarityValue = similarityValue;
    }
}
