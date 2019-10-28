package hacker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by gaofeng on 2019/10/25.
 */
public class FileCopy {

    public static void generateAccessLog() {
        try {
            long timeStar = System.currentTimeMillis();// 得到当前的时间
            ByteBuffer byteBuf = ByteBuffer.allocate(1024 * 14 * 1024);
            File file = new File("/Users/momo/gitProject/hacker-shenmegui/src/main/java/hacker/access0.txt");
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            fc.read(byteBuf);// 1 读取
            int pos = byteBuf.position();
            byte[] bbb = new byte[pos];
            System.out.println(pos);
            byteBuf.flip();
            byteBuf.get(bbb, 0, pos);
            System.out.println(fc.size()/1024);
            long timeEnd = System.currentTimeMillis();// 得到当前的时间
            System.out.println("Read time :" + (timeEnd - timeStar) + "ms");
            timeStar = System.currentTimeMillis();
            for (int i = 10; i < 30; i++) {
                String fileName = "/Users/momo/gitProject/hacker-shenmegui/access-" + i + ".log";
                FileOutputStream fos = new FileOutputStream(fileName);
                for (int j = 0; j < 8000; j++) {
                    fos.write(bbb);//2.写入
                }
                fos.flush();
                fos.close();
            }
            //mbb.flip();
            timeEnd = System.currentTimeMillis();
            System.out.println("Write time :" + (timeEnd - timeStar) + "ms");
            fc.close();
            fis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        generateAccessLog();
    }
}
