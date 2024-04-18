import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class test {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

//        String ex = "bucket000bucket001bucket002bucket003";
//        long[] index = new long[4];
//        ArrayList<String> bucketNames = new ArrayList<>();
//        ByteBuffer buff;
//
//        File file = new File("ex.ser");
//        if (!file.isFile()) {
//            new ObjectOutputStream(new FileOutputStream("ex.ser")).writeObject(ex);
//            System.out.println(file.isFile());
//        } else {
//            buff = ByteBuffer.wrap(Files.readAllBytes(Paths.get("ex.ser")));
//            byte[] temp = new byte[9];
//            System.out.println(Arrays.toString(buff.array()));
//            buff.position(7);
//            buff.get(temp, 0, 9);
//            bucketNames.add(new String(temp));
//            System.out.println(bucketNames);
//            System.out.println(buff.get());
//        }

//        Bucket testbucket;
//        Bucket bucket = new Bucket();
//        bucket.put("target", "XXOBU");
//        new ObjectOutputStream(new FileOutputStream("testbucket.ser")).writeObject(bucket);
//        ByteBuffer buf = ByteBuffer.wrap(Files.readAllBytes(Path.of("testbucket.ser")));
//        testbucket = (Bucket) new ObjectInputStream(new ByteArrayInputStream(buf.array())).readObject();
//        System.out.println(Arrays.toString(buf.array()));
//        testbucket.printBucketContents();
//        System.out.println(testbucket.get("target"));


        PHT pht = new PHT();
        for (int i = 0; i < 100; i++) {
            pht.put(Integer.toString(i), Integer.toString(i));
            System.out.println((float) pht.indexArray.entries / (pht.indexArray.size * 63));
        }
        System.out.println(pht.indexArray.size + " " + pht.indexArray.entries);
        pht.printAllBucketContents();

    }

}
