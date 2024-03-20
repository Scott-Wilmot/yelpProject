import java.util.*;

public class Business {
    String name;
    String review;
    float similarityValue;
    public Business(String name, String review) {
        this.name = name;
        this.review = review;
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
