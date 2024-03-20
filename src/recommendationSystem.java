import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class recommendationSystem {

    static void getBusinesses(ArrayList<Business> businesses, String path) throws Exception {
        //Setting up the Scanners for both the business and review files
        File businessFile = new File(path + File.separator + "yelp_academic_dataset_business.json");
        InputStream businessStream = new FileInputStream(businessFile);
        Scanner businessScanner = new Scanner(businessStream);
        File reviewFile = new File(path + File.separator + "yelp_academic_dataset_review.json");
        InputStream reviewStream = new FileInputStream(reviewFile);
        Scanner reviewScanner = new Scanner(reviewStream);

        Hashtable<String, String> reviews = new Hashtable<>(); //<id, text>

        while (businesses.size() < 10000) { //Until 10000 valid businesses have been found

            /*if (!businessScanner.hasNextLine()) {
                System.out.println("trigger");
                businessScanner = new Scanner(new FileInputStream(businessFile));
                reviews = new Hashtable<>();
            }*/

            while (reviews.size() < 10000) { //Fill reviews to same number as remaining businesses
                String line = reviewScanner.nextLine();
                JSONObject object = (JSONObject) new JSONParser().parse(line);
                String text = (String) object.get("text");
                String businessID = (String) object.get("business_id");
                if (!reviews.containsKey(businessID)) { //If there isnt a review with matching businessID then put
                    reviews.put(businessID, text);
                }
            }
            Set<String> reviewKeySet = reviews.keySet();

            String line = businessScanner.nextLine();
            JSONObject object = (JSONObject) new JSONParser().parse(line);
            String name = (String) object.get("name");
            String businessID = (String) object.get("business_id");
            for (String revID : reviewKeySet) { //for each id in the reviews table
                if (businessID.equalsIgnoreCase(revID)) {
                    businesses.add(new Business(name, reviews.get(revID)));
                    reviewKeySet.remove(revID);
                    break;
                }
            }

        }

    }

    static Business findBusiness(String text, ArrayList<Business> businesses) {
        for (Business b : businesses) {
            if (b.name.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    static Hashtable<String, Float> getTF(Business business) {
        //words array stuff
        String text = business.review;
        ArrayList<String> words = new ArrayList<>(Arrays.asList(text.split("\\W+")));
        words.replaceAll(String::toLowerCase);  //Set to lowercase to make Target and target the same when analyzed
        float wordCount = words.size();
        //Set up two tables: occurrences and termFrequencies with occurrences -> termFrequencies
        Hashtable<String, Integer> occurrenceTable = new Hashtable<>();   //This will represent our occurrences
        Hashtable<String, Float> termFrequencies = new Hashtable<>();


        while (!words.isEmpty()) {  //Run through words list which accounts for changing array size, maybe implement in getBusinesses
            String word = words.get(0); //This acts as the word to find frequency for
            int occurrences = 0;    //count for occurrences of given word
            for (String s : words) {    //Now cycle through words array and match
                if (s.equals(word)) {   //Just .equals should work due to .replaceAll() being done earlier
                    occurrences++;
                }
            }
            occurrenceTable.put(word, occurrences);   //Place our occurrences in a table...
            words.removeAll(Collections.singleton(word));   //...and subsequently remove given word from words
        }
        //Now get keySet from occurrences and use values to quickly calculate tf
        Set<String> keySet = occurrenceTable.keySet();
        for (String s : keySet) {
            float occurrences = occurrenceTable.get(s);
            float frequency = occurrences / wordCount;
            termFrequencies.put(s, frequency);
        }

        return termFrequencies;   //Return our very useful and filled table
    }

    static Hashtable<String, Float> getIDF(ArrayList<Business> businesses, Business business) {
        //Variable Setup
        String text = business.review;
        ArrayList<String> words = new ArrayList<>(Arrays.asList(text.split("\\W+")));
        words.replaceAll(String::toLowerCase); //Removes capital letter repeat words

        HashSet<String> wordSet = new HashSet<>(words); //Makes a set of the words
        Hashtable<String, Integer> docOccurrences = new Hashtable<>(); //Variable for representing num of documents a word occurs in
        Hashtable<String, Float> idf_table = new Hashtable<>();  //Will later represent the log(D/t) values

        //Iterate through businesses array searching for words in wordset
        for (Business b : businesses) {
            Set<String> businessSet = b.getWordSet(); //turn words in Business b into a wordSet for comparisons
            for (String word: wordSet) { //Want to search for occurrences of wordset so interate through wordSet and compare
                if (businessSet.contains(word)) { //If the businessesWordSet contains a word from wordSet then add element to docOccurrences
                    if (!docOccurrences.containsKey(word)) {docOccurrences.put(word, 1);} //If word not yet in table, add it to table with one occurrence
                    else {docOccurrences.put(word, docOccurrences.get(word) + 1);} //If word is in table, increment it
                }
            }
        }

        Set<String> keySet = docOccurrences.keySet();
        for (String s : keySet) {
            float occurrences = docOccurrences.get(s);
            float idf = (float) Math.log10(businesses.size() / occurrences);
            idf_table.put(s, idf);
        }
        return idf_table;
    }

    static Hashtable<String, Float> getTF_IDF(Business business, Hashtable<String, Float> idfTable) {
        Hashtable<String, Float> tfTable = getTF(business); //Get table of tf values for given business
        Hashtable<String, Float> tfidf_Table = new Hashtable<>(); //Table that will store our tfidf values for each business

        Set<String> tfKeys = tfTable.keySet();
        for (String s : tfKeys) {
            if (idfTable.containsKey(s)) {
                float tfidf = tfTable.get(s) * idfTable.get(s);
                tfidf_Table.put(s, tfidf);
            }
        }
        return tfidf_Table;
    }

    static float sumTF_IDF(Business business, Hashtable<String, Float> idfTable) {
        Hashtable<String, Float> business_tfidf = getTF_IDF(business, idfTable); //all tfidf values from given business

        float sum_tfidf = 0F;
        Set<String> businessKeySet = business_tfidf.keySet();
        for (String s : businessKeySet) {
            sum_tfidf += business_tfidf.get(s);
        }

        return sum_tfidf;
    }

    //maps each summed tfidf or similarity value to its respective business
    static void getSimilarityTables(ArrayList<Business> businesses, Hashtable<String, Float> idfTable) {
        for (Business business : businesses) {
            float similarityValue = sumTF_IDF(business, idfTable);
            business.setSimilarityValue(similarityValue);
        }
    }

    static ArrayList<Business> mostSimilarBusinesses(Business inputtedBusiness, ArrayList<Business> businesses) {
        ArrayList<Business> similarBusinesses = new ArrayList<>();
        float inputSimilarity = inputtedBusiness.similarityValue;
        businesses.remove(inputtedBusiness);

        for (Business business : businesses) {
            if (similarBusinesses.size() < 2) { //Fill the first two read businesses to default as most similar
                similarBusinesses.add(business);
                continue;
            }
            float difference = Math.abs(inputSimilarity - business.similarityValue);    //Save the inputted businesses difference to business for use in foreach loop
            for (int i = 0; i < similarBusinesses.size(); i++) {
                Business simBusiness = similarBusinesses.get(i);
                float simDifference = Math.abs(inputSimilarity - simBusiness.similarityValue);
                if (difference < simDifference) {
                    similarBusinesses.set(i, business);
                    break;
                }
            }
        }

        return similarBusinesses;
    }

    public static void main(String[] args) throws Exception {

        //Get and create businesses
        String folderPath = "D:\\Semester 4\\CSC365\\YelpDatabase";
        ArrayList<Business> allBusinesses = new ArrayList<>(); //keeps a track of all businesses
        getBusinesses(allBusinesses, folderPath);

        for (Business b : allBusinesses) {
            System.out.println(b.name);
        }

        gui mainFrame = new gui();
        mainFrame.setVisible(true);

        while (true) {

            if (mainFrame.inputExists) {    //If the user has inputted any input
                Business business = findBusiness(mainFrame.businessName, allBusinesses);  //Finds the users inputted business
                if (business != null) {
                    ArrayList<Business> businesses = new ArrayList<>(allBusinesses); //treats like temporary businesses since my search removes the inputted business from the list of all businesses so if the same list is kept, options will slowly be reduces and searches will not be consistent
                    Hashtable<String, Float> idf_table = getIDF(businesses, business); //Create an IDF table for all the businesses

                    getSimilarityTables(businesses, idf_table); //Gives each business its similarity rating

                    ArrayList<Business> simBusinesses = mostSimilarBusinesses(business, businesses);
                    String bothBusinesses = simBusinesses.get(0).name + ", " + simBusinesses.get(1).name;
                    mainFrame.textArea.setText(bothBusinesses);
                    mainFrame.inputExists = false;
                }
                else {
                    mainFrame.textArea.setText("Cannot Find Business");
                }

            }
            else {
                Thread.sleep(1000); //Prevents my computer from exploding
            }

        }





    }

}
