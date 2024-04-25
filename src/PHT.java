import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

// sketches for persistent hash tables from (bounded) strings to strings
class Bucket implements Serializable {
    static final int SLEN = 32; // max string length for keys and vals
    static final int BLOCKSIZE = 4096;
    static final int ENTRYWIDTH = SLEN + SLEN;
    static final int MAX_COUNT = 63;
    static final int LONG_WIDTH = 8;
    static final int INT_WIDTH = 4;
    long pos;
    int mask;
    int count;
    String[] keys = new String[MAX_COUNT];
    String[] vals = new String[MAX_COUNT];
    static final int POS_INDEX = 0;
    static final int MASK_INDEX = POS_INDEX + LONG_WIDTH;
    static final int COUNT_INDEX = MASK_INDEX + INT_WIDTH;
    static final int FIRST_ENTRY_INDEX = COUNT_INDEX + INT_WIDTH;
    static int keyIndex(int i) { return FIRST_ENTRY_INDEX + i * ENTRYWIDTH; }
    static int valIndex(int i) { return keyIndex(i) + SLEN; }
    void read(ByteBuffer b) {  // for variety, uses relative positioning
        pos = b.getLong();
        mask = b.getInt();
        count = b.getInt();
        for (int i = 0; i < MAX_COUNT; ++i) {
            byte[] kb = new byte[SLEN], vb = new byte[SLEN];
            keys[i] = new String(String.valueOf(b.get(kb, 0, SLEN)));
            vals[i] = new String(String.valueOf(b.get(kb, 0, SLEN)));
        }
    }
    String get(String key) {
        for (int j = 0; j < MAX_COUNT; ++j) {
            if (key.equalsIgnoreCase(keys[j]))
                return vals[j];
        }
        return null;
    }

    boolean put(String key, String ID) {
        for (int i = 0; i < MAX_COUNT; i++) {
            if (keys[i] == null) {
                keys[i] = key; vals[i] = ID;
                return true; // Indicates a new value was inserted into the table successfully
            }
            else if (keys[i].equalsIgnoreCase(key)) return false; // Indicates that the (k,v) pair is already present in the table
        }
        return false;
    }

    void printBucketContents() {
        for (int i = 0; i < MAX_COUNT; i ++) {
            if (keys[i] == null) {return;}
            System.out.println(keys[i] + ", " + vals[i]);
        }
    }

}
class IndexArray implements Serializable {
    long[] index; // Tracks where filenames for serialized businesss are in the BUCKETS file
    int size; // Total number of indices, should be directly correlated with index.length
    int entries; // Tracks how many businesses total are in the table

    public IndexArray() throws IOException {
        index = new long[]{7}; // Initializes with 7 as first index
        size = index.length;
        entries = 0;
        new ObjectOutputStream(new FileOutputStream("BUCKETS.ser")).writeObject("bucket000"); // Creates a brand new buckets file since this will only happen if a buckets file is not found
        new ObjectOutputStream(new FileOutputStream("buckets\\" + "bucket000.ser")).writeObject(new Bucket());
    }

    long getBucketPosition (String key) {
        return index[(key.hashCode() & (size - 1))];
    }

}
class PHT {
    static final String bucketFile = "BUCKETS.ser";
    static final String indexFile = "INDEX.ser";
    byte[] bucketName = new byte[9]; // Set to size 9 for bucket names of format "bucketxxx"
    int bucketNameSize = bucketName.length;
    IndexArray indexArray;
    ByteBuffer buf;

    PHT() throws IOException, ClassNotFoundException {
        if (fileCheck()) {
            indexArray = (IndexArray) new ObjectInputStream(new FileInputStream(indexFile)).readObject();
        }
        else {
            indexArray = new IndexArray(); // Initialize new IndexArray with only one bucket
            buf = ByteBuffer.wrap(Files.readAllBytes(Path.of(bucketFile)));
            new ObjectOutputStream(new FileOutputStream(indexFile)).writeObject(indexArray);
        }
    }

    private boolean fileCheck() {
        File bucket = new File(bucketFile);
        return bucket.isFile();
    }

    private void updateBuffer() throws IOException { // Since the bucket file is loaded to the bytebuffer and can be changed whenever, it is importantto pass the most recent BUCKETS file to the buffer
        buf = ByteBuffer.wrap(Files.readAllBytes(Path.of(bucketFile)));
    }

    // Can implement this in many places to make code more readable
    Bucket getBucket(long position) throws IOException, ClassNotFoundException {
        updateBuffer();
        buf.position((int) position);
        buf.get(bucketName, 0, bucketNameSize);
        return (Bucket) new ObjectInputStream(new FileInputStream("buckets\\" + new String(bucketName) + ".ser")).readObject();
    }

    void put(String key, String value) throws IOException, ClassNotFoundException {
        updateBuffer();
        int pos = (int) indexArray.getBucketPosition(key); // Hashes key to find proper bucket position
        buf.position(pos);
        buf.get(bucketName, 0, bucketNameSize); // Gets proper bucket name
        Bucket b = (Bucket) new ObjectInputStream(new FileInputStream("buckets\\" + new String(bucketName) + ".ser")).readObject(); // Retrives bucket using bucketName
        if (b.put(key, value))  // Only increments entries if the table did not contain the key
            indexArray.entries++; new ObjectOutputStream(new FileOutputStream(indexFile)).writeObject(indexArray); // Increment the total number of entries in the table then re-serialize
        new ObjectOutputStream(new FileOutputStream("buckets\\" + new String(bucketName) + ".ser")).writeObject(b); // Updates and reserializes bucket
        if ((float) indexArray.entries/(indexArray.size*63) >= 0.75f)
            resize();
    }

    String get(String key) throws IOException, ClassNotFoundException {
        updateBuffer();
        int pos = (int) indexArray.getBucketPosition(key); // Gets the position in BUCKETS file of desired bucket to get value from
        buf.position(pos);
        buf.get(bucketName, 0, bucketNameSize); // Stores the bucket name in bucketName temporarily
        Bucket b = (Bucket) new ObjectInputStream(new FileInputStream("buckets\\" + new String(bucketName) + ".ser")).readObject(); // Retrives bucket using bucketName
        return b.get(key); // Uses Bucket class get method to retrieve value within the bucket class
    }

    void resize() throws IOException, ClassNotFoundException {
        //updateBuffer(); // Might be able to remove when resize is only called in put
        // Store a copy of the existing index and create a shell for the new indexes to go in
        long[] oldIndex = indexArray.index;
        int oldCapacity = oldIndex.length; int newCapacity = oldCapacity << 1;
        long[] newIndex = new long[newCapacity];
        for (int i = 0; i < newCapacity; i++) { // Sets bucket name positions for the new array
            newIndex[i] = 7 + ((long) bucketNameSize * i); // 7 is the String metadata padding for serialized objects
        }
        // Paste old buckets to a list for temp storage so new buckets can be serialized
        Bucket[] oldBuckets = new Bucket[oldCapacity];
        for (int i = 0; i < oldCapacity; i++) {
            buf.position((int) oldIndex[i]); // Sets position to position retrived from oldIndex
            buf.get(bucketName, 0, bucketNameSize); // Writes bucket name into temporary storage
            oldBuckets[i] = (Bucket) new ObjectInputStream(new FileInputStream( "buckets\\" + new String(bucketName) + ".ser")).readObject(); // Save old bucket to array
        }
        // set index equal to the new index and assign a new size as well as reserialize the new IndexArray
        indexArray.index = newIndex;
        indexArray.size = newCapacity;
        new ObjectOutputStream(new FileOutputStream(indexFile)).writeObject(indexArray);
        //Initialize new serialized bucket files for the newIndex
        String bucketsFile = "";
        for (int i = 0; i < newCapacity; i++) { // Creates the content of BUCKETS file
            int iLength = Integer.toString(i).length();
            String padding = "0".repeat(3 - iLength); // Determines how many zeroes are needed in file name
            String bucketName = String.format("bucket%s%d", padding, i);
            new ObjectOutputStream(new FileOutputStream("buckets\\" + bucketName + ".ser")).writeObject(new Bucket()); // Saves a new empty bucket to a file
            bucketsFile = bucketsFile.concat(bucketName);
        }
        new ObjectOutputStream(new FileOutputStream("BUCKETS.ser")).writeObject(bucketsFile); // Writes the new bucket names to the BUCKETS file
        // Now remap oldBuckets values to the new buckets
        int entryTrack = indexArray.entries; // Keeps a track of the actual number of entries to prevent overflow
        for (Bucket b : oldBuckets) { // Iterate through each bucket and its keyset to put them in new buckets
            for (String key : b.keys) {
                if (key == null) break; // the key list goes from start to end so whenever a null value is hit you know you've read all keys
                put(key, b.get(key)); // Put method should automatically rehash the key into the proper bucket
                indexArray.entries = entryTrack;
            }
        }
        System.out.println("Resize: " + oldCapacity + " -> " + newCapacity);
    }

    void printAllBucketContents() throws IOException, ClassNotFoundException {
        updateBuffer();
        int count = 0;
        for (long l : indexArray.index) {
            System.out.println(l + ": ");
            buf.position((int) l);
            buf.get(bucketName, 0, bucketNameSize);
            Bucket b = (Bucket) new ObjectInputStream(new FileInputStream("buckets\\" + new String(bucketName) + ".ser")).readObject();
            b.printBucketContents();
            count++;
        }
    }

}