import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class test {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String ex = "bucket000bucket001bucket002bucket003";
        long[] index = new long[4];
        ArrayList<String> bucketNames = new ArrayList<>();
        ByteBuffer buff;

        File file = new File("ex.ser");
        if (!file.isFile()) {
            new ObjectOutputStream(new FileOutputStream("ex.ser")).writeObject(ex);
            System.out.println(file.isFile());
        } else {
//            byte[] bytes = Files.readAllBytes(Paths.get("ex.ser"));
//            buff = ByteBuffer.allocate(bytes[6]);
//            System.out.println(Arrays.toString(bytes));
//            buff.put(bytes, 7, bytes[6]);
//            System.out.println(Arrays.toString(buff.array()));
//            System.out.println(new String(buff.array()));
            buff = ByteBuffer.wrap(Files.readAllBytes(Paths.get("ex.ser")));
            byte[] temp = new byte[9];
            System.out.println(Arrays.toString(buff.array()));
            buff.position(7);
            while (buff.hasRemaining()) {
                buff.get(temp, 0, 9);
                bucketNames.add(new String(temp));
            }
            System.out.println(bucketNames);
        }

    }

}
