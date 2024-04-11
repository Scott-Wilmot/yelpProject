import java.io.*;
import java.nio.ByteBuffer;

// sketches for persistent hash tables from (bounded) strings to strings
class Bucket {
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
        for (int j = 0; j < count; ++j) {
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
            System.out.println(keys[i] + " ," + vals[i]);
        }
    }

}
class IndexArray implements Serializable {
    long[] index = new long[64]; //Starts with
    int size = 1; //Keeps track of the total number of possible indices

    void put(String key) {
        if (index == null)
    }

    long getBucketPosition(String key) {
        return index[(key.hashCode() & (size - 1))];
    }

}
class PHT {
    static final String bucketFile = "BUCKETS";
    static final String indexFile = "INDEX";
    IndexArray indexArray;
    String[] buckets;
    PHT() throws IOException, ClassNotFoundException {
        if (fileCheck()) {
            indexArray = (IndexArray)
                    new ObjectInputStream(new FileInputStream(indexFile)).readObject();
            buckets = (String[])
                    new ObjectInputStream(new FileInputStream(bucketFile)).readObject();
        }
        else {
            indexArray = new IndexArray();
            buckets = new String[1];
        }
    }

    boolean fileCheck() {
        File bucket = new File(bucketFile);
        File index = new File(indexFile);
        return bucket.isFile() && index.isFile();
    }

    // Strictly uses put methods for IndexArray and Bucket, do not worry about resizing here
    void put(String key, String value) throws IOException, ClassNotFoundException {
        String bucketPtr = buckets[(int) indexArray.getBucketPosition(key)];
        Bucket bucket = (Bucket) new ObjectInputStream(new FileInputStream(bucketPtr)).readObject();

        indexArray.put(key);
        //bucket.put();
    }

}
    
