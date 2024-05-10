import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

public class recommendationSystem {

    //Global Variables
    //static String directoryPath = "C:\\Users\\GooseAdmin\\IdeaProjects\\yelpProject"; // Path to the project folder
    static String directoryPath = "C:\\Users\\scott\\IdeaProjects\\yelpProject";
    static String persistentHashTableName = "persistentHT.ser";

    // Creates all 10000 businesses as serialized files and adds <name,ID> pairs to pht
    static void getBusinesses(PHT pht, String path) throws Exception {
        //Setting up the Scanners for both the business and review files
        File businessFile = new File(path + File.separator + "yelp_academic_dataset_business.json");
        InputStream businessStream = new FileInputStream(businessFile);
        Scanner businessScanner = new Scanner(businessStream);
        File reviewFile = new File(path + File.separator + "yelp_academic_dataset_review.json");
        InputStream reviewStream = new FileInputStream(reviewFile);
        Scanner reviewScanner = new Scanner(reviewStream);

        Hashtable<String, String> reviews = new Hashtable<>(); //<id, text>

        int createdBusinesses = 0;
        while (createdBusinesses < 10000) { //Until 10000 valid businesses have been found

            // This special if stmt resets the business scanner on eof which is needed to read 10000 true unique businesses
            if (!businessScanner.hasNextLine()) {
                businessScanner = new Scanner(new FileInputStream(businessFile));
                reviews = new Hashtable<>();
            }

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
            double latitude = (double) object.get("latitude");
            double longitude = (double) object.get("longitude");
            for (String revID : reviewKeySet) { //for each id in the reviews table
                // If the key can be placed in the pht successfully then serialize related business into a serialized file
                if (businessID.equalsIgnoreCase(revID) && pht.put(name, businessID)) {
                    Business b = new Business(name, reviews.get(revID), businessID, latitude, longitude);
                    new ObjectOutputStream(new FileOutputStream(directoryPath + "\\businesses\\" + businessID + ".ser")).writeObject(b);
                    createdBusinesses++;
                    reviewKeySet.remove(revID);
                    break;
                }
            }

        }

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

        return termFrequencies;
    }

    static Hashtable<String, Float> getIDF(PHT pht, Business business) throws IOException, ClassNotFoundException {
        //Variable Setup
        String text = business.review;
        ArrayList<String> words = new ArrayList<>(Arrays.asList(text.split("\\W+")));
        words.replaceAll(String::toLowerCase); //Removes capital letter repeat words

        HashSet<String> wordSet = new HashSet<>(words); //Makes a set of the words
        Hashtable<String, Integer> docOccurrences = new Hashtable<>(); //Variable for representing num of documents a word occurs in
        Hashtable<String, Float> idf_table = new Hashtable<>();  //Will later represent the log(D/t) values

        for (long l : pht.indexArray.index) { // For each bucket...
            Bucket bucket = pht.getBucket(l);
            for (String key : bucket.keys) { // For each key
                if (key == null) break; //You've reached the end of the list...next bucket
                Business b = (Business) new ObjectInputStream(new FileInputStream("businesses\\" + bucket.get(key) + ".ser")).readObject();
                for (String word : wordSet) {
                    if (b.getWordSet().contains(word)) {
                        if (!docOccurrences.containsKey(word)) {docOccurrences.put(word, 1);} //If word not yet in table, add it to table with one occurrence
                        else {docOccurrences.put(word, docOccurrences.get(word) + 1);} //If word is in table, increment it
                    }
                }
            }
        }

        Set<String> keySet = docOccurrences.keySet();
        for (String s : keySet) {
            float occurrences = docOccurrences.get(s);
            float idf = (float) Math.log10(10000 / occurrences);
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
    static void getSimilarityTables(PHT pht, Hashtable<String, Float> idfTable) throws IOException, ClassNotFoundException {
//        for (Business business : businesses) {
//            float similarityValue = sumTF_IDF(business, idfTable);
//            business.setSimilarityValue(similarityValue);
//        }
        for (long l : pht.indexArray.index) { // For each bucket...
            Bucket bucket = pht.getBucket(l);
            for (String key : bucket.keys) { // For each key
                if (key == null) break; //You've reached the end of the list...next bucket
                Business b = (Business) new ObjectInputStream(new FileInputStream("businesses\\" + bucket.get(key) + ".ser")).readObject();
                b.similarityValue = sumTF_IDF(b, idfTable);
                new ObjectOutputStream(new FileOutputStream("businesses\\" + b.ID + ".ser")).writeObject(b); // Update the serializable
            }
        }
    }

    static ArrayList<Business> mostSimilarBusinesses(Business inputtedBusiness, PHT pht) throws IOException, ClassNotFoundException {
        ArrayList<Business> similarBusinesses = new ArrayList<>();
        float inputSimilarity = inputtedBusiness.similarityValue;

        for (long l : pht.indexArray.index) { // For each bucket...
            Bucket bucket = pht.getBucket(l);
            for (String key : bucket.keys) { // For each key
                if (key == null) break; //You've reached the end of the list...next bucket
                Business business = (Business) new ObjectInputStream(new FileInputStream("businesses\\" + bucket.get(key) + ".ser")).readObject();
                if (similarBusinesses.size() < 2) { //Fill the first two read businesses to default as most similar
                    similarBusinesses.add(business);
                    continue;
                }
                float difference = Math.abs(inputSimilarity - business.similarityValue);    //Save the inputted businesses difference to business for use in foreach loop
                for (int i = 0; i < similarBusinesses.size(); i++) {
                    Business simBusiness = similarBusinesses.get(i);
                    float simDifference = Math.abs(inputSimilarity - simBusiness.similarityValue);
                    if (difference < simDifference && !business.equals(inputtedBusiness)) {
                        similarBusinesses.set(i, business);
                        break;
                    }
                }
            }
        }

        return similarBusinesses;
    }

    // Arbitrarily assigns centers since location in arrayList doesn't affect similarity value
    static ArrayList<Group> clusterSimilarityValues(PHT pht) throws IOException, ClassNotFoundException {
        ArrayList<Group> groups = new ArrayList<>();

        Bucket b = pht.getBucket(7);
        int i = 0;
        for (String s : b.keys) {
            if (i >= 5) break;
            Business business = (Business) new ObjectInputStream(new FileInputStream("businesses\\" + pht.get(s) + ".ser")).readObject();
            groups.add(new Group(business));
            i++;
        }

        //Cycle through businesses array and compare each business value to group center value and assign to closest cluster
        for (long l : pht.indexArray.index) { // For each bucket...
            Bucket bucket = pht.getBucket(l);
            for (String key : bucket.keys) { // For each key
                if (key == null) break; //You've reached the end of the list...next bucket
                Business business = (Business) new ObjectInputStream(new FileInputStream("businesses\\" + bucket.get(key) + ".ser")).readObject();
                Group closestGroup = null;
                for (Group g : groups) {
                    if (closestGroup == null) {closestGroup = g;}
                    else if (Math.abs(g.center.similarityValue - business.similarityValue) < Math.abs(closestGroup.center.similarityValue - business.similarityValue)) {
                        closestGroup = g;
                    }
                }
                closestGroup.addToGroup(business);
            }
        }

        for (Group g : groups) {
            System.out.println(g.center.name + ": " + g.group.size());
        }

        return groups;
    }

    static void reclusterGroups(ArrayList<Group> groups) {
        /*
        Take each group, swap group center with nearest point, then reassign businesses to groups
         */
        for (Group group : groups) {
            Business center = group.center;
            Business newCenter = null;
            for (Business business : group.group) {
                if (center.name.equals(business.name)) continue;
                else if (newCenter == null) newCenter = business;
                else if (Math.abs(center.similarityValue - business.similarityValue) < Math.abs(center.similarityValue - newCenter.similarityValue)) {
                    newCenter = business;
                }
            }
            group.center = newCenter;
        }

        for (Group g : groups) {
            System.out.println(g.center.name);
        }

    }

    static void runGUI(PHT pht) throws InterruptedException, IOException, ClassNotFoundException {
        // Create GUI object
        GUI mainFrame = new GUI();
        mainFrame.setVisible(true);

        // Infinite loop until user closes the window
        while (true) {

            if (mainFrame.inputExists) {    //If the user has inputted any input
                Business business = (Business) new ObjectInputStream(new FileInputStream("businesses\\" + pht.get(mainFrame.businessName) + ".ser")).readObject();
                if (business != null) {
                    //ArrayList<Business> businesses = new ArrayList<>(allBusinesses); //treats like temporary businesses since my search removes the inputted business from the list of all businesses so if the same list is kept, options will slowly be reduces and searches will not be consistent
                    Hashtable<String, Float> idf_table = getIDF(pht, business); //Create an IDF table for all the businesses
                    getSimilarityTables(pht, idf_table); //Gives each business its similarity rating

                    Group inputGroup = null;
                    ArrayList<Group> groups = clusterSimilarityValues(pht);
                    for (Group g : groups) {
                        if (g.contains(business.name)) {
                            inputGroup = g;
                            break;
                        }
                    }
                    //reclusterGroups(groups);

                    ArrayList<Business> simBusinesses = mostSimilarBusinesses(business, pht);
                    String bothBusinesses = simBusinesses.get(0).name + ", " + simBusinesses.get(1).name;

                    //Handling GUI display and state
                    mainFrame.textArea.setText(bothBusinesses + "\nCluster center: " + inputGroup.center.name); // display
                    mainFrame.inputExists = false; // state
                }
                else { // Invalid case
                    mainFrame.textArea.setText("Cannot Find Business");
                }

            }
            else {
                Thread.sleep(1000); //Prevents my computer from exploding
            }

        }
    }

    public static void main(String[] args) throws Exception {

        // Path and File variables
        //String folderPath = "C:\\Users\\GooseAdmin\\OneDrive\\Desktop\\YelpDataset"; // Path to the yelp database
        String folderPath = "D:\\Semester 4\\CSC365\\YelpDatabase";

        // Serialize all businesses and create a PHT
        PHT pht = new PHT();
        Graph graph = new Graph();
        //getBusinesses(pht, folderPath);
        //graph.getNodes(pht);
        //graph.createEdges();
        //pht.printAllBucketContents();
        //graph.nodeNames();
        //graph.getIDF(graph.getBusiness(graph.nodes.get(2).ID)); // Create IDF table
//        for (String s : graph.idfTable.keySet()) {
//            System.out.println(s + ", " + graph.idfTable.get(s));
//        }
//        graph.assignSimilarityValues(); // Internally assign sim vals based on indiv businesses tf tables and the src node IDF table
        //graph.nodesInformation(); // Output/sanity check
        graph.findPath(graph.nodes.get(100), null);
        graph.nodesInformation();

        //runGUI(pht);

    }

}
