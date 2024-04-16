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
            if (key.equals(keys[j]))
                return vals[j];
        }
        return null;
    }

    void put(String key, String ID) {
        for (int i = 0; i < MAX_COUNT; i++) {
            if (keys[i] == null) {
                keys[i] = key; vals[i] = ID;
                return;
            }
        }
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
    int size; // Number of possible indices

    public IndexArray() throws IOException {
        index = new long[1];
        size = index.length;
        if (!new File("BUCKETS").isFile()) {new ObjectOutputStream(new FileOutputStream("BUCKETS")).writeObject("bucket000");} // If a Buckets file does not exist, create a new buckets file with one default bucket
    }

    void resize() throws IOException, ClassNotFoundException {
        long[] oldIndex = index;
        int oldCapacity = index.length; int newCapacity = oldCapacity << 1;
        long[] newIndex = new long[newCapacity];
        for (int i = 0; i < newCapacity; i++) { // Makes a new index array capable of holding twice the buckets
            newIndex[i] = 7 + (9L * i); // the 7 is to skip the serialized String byte padding
        }
        Bucket[] buckets = new Bucket[newCapacity]; // Creates a temporary storage for buckets file before serialization and overwrite of old buckets
        for (int i = 0; i < newCapacity; i++) { // Populates buckets array with empty, new buckets
            buckets[i] = new Bucket();
        }
        // Now remap key-value pairs from indices in oldIndex
        ByteBuffer buf = ByteBuffer.wrap(Files.readAllBytes(Path.of("BUCKETS")));
        byte[] bytes = new byte[9];
        for (long l : oldIndex) {
            buf.position((int) l); // Sets position in byte array to that of
            buf.get(bytes, 0, 9); // Stores bucket name in bytes array
            Bucket oldBucket = (Bucket) new ObjectInputStream(new FileInputStream(new String(bytes))).readObject(); // Pulls an existing bucket out of storage using index[i]
            for (int i = 0; i < oldBucket.MAX_COUNT; i++) { // Reassign bucket (k,v)'s

            }
        }
        //Remember to update the size before closing the method for future use
    }

    long getBucketPosition (String key) {
        return index[(key.hashCode() & (size - 1))];
    }

}
class PHT {
    static final String bucketFile = "BUCKETS";
    static final String indexFile = "INDEX";
    byte[] bucketName = new byte[9]; // Set to size 9 for bucket names of format "bucketxxx"
    int bucketNameSize = bucketName.length;
    IndexArray indexArray;
    ByteBuffer buf;

    PHT() throws IOException, ClassNotFoundException {
        if (fileCheck())
            indexArray = (IndexArray)
                    new ObjectInputStream(new FileInputStream(indexFile)).readObject();
        else
            indexArray = new IndexArray();
    }

    private boolean fileCheck() {
        File bucket = new File(bucketFile);
        File index = new File(indexFile);
        return bucket.isFile() && index.isFile();
    }

    void put(String key, String value) throws IOException {
        int pos = (int) indexArray.getBucketPosition(key);

    }

    void get(String key) throws IOException, ClassNotFoundException {
        int pos = (int) indexArray.getBucketPosition(key); // Gets the position in BUCKETS file of desired bucket to get value from
        buf.position(pos);
        buf.get(bucketName, 0, bucketNameSize); // Stores the bucket name in bucketName temporarily
        Bucket b = (Bucket) new ObjectInputStream(new ByteArrayInputStream(bucketName)).readObject(); // Retrives bucket using bucketName
        b.get(key); // Uses Bucket class get method to retrieve value within the bucket class
    }

}
