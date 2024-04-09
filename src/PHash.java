import java.io.File;

class Bucket {
    String id;
    Bucket(String id) {
        this.id = id;
    }

}

class IndexArray {

    Bucket[] buckets = new Bucket[0];

    void resize() {
        Bucket[] oldArray = buckets;
        Bucket[] newArray = new Bucket[buckets.length << 1];
        for (int i = 0; i < buckets.length; i++) {
            //int h =
        }
    }

    void put(String key) {
        int index = key.hashCode() & buckets.length;
        buckets[index] = new Bucket();
    }

}

public class PHash {

    //File names for the serialized Indicies and Buckets
    String indexFile = "INDEXES.ser";
    String bucketFile = "BUCKETS.ser";

    IndexArray indexArray = new IndexArray();

    boolean fileCheck() {
        File index = new File(indexFile);
        File bucket = new File(bucketFile);
        return index.isFile() && bucket.isFile();
    }



}
