package hacker;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Author LTY
 * Date 2019/10/25
 */
public class FileUtil {

    private static FileOutputStream[] fileOutputStreams;
    private static FileChannel[] fileChannels = new FileChannel[4];

    static {
        initChannel();
    }

    private static void initChannel() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "log";
            File file = new File(basePath);
            file.mkdir();
            File file0 = new File(basePath + File.separator + "0.log");
            File file1 = new File(basePath + File.separator + "1.log");
            File file2 = new File(basePath + File.separator + "2.log");
            File file3 = new File(basePath + File.separator + "3.log");
            boolean flag0 = file0.createNewFile();
            boolean flag1 = file1.createNewFile();
            boolean flag2 = file2.createNewFile();
            boolean flag3 = file3.createNewFile();
            fileOutputStreams = new FileOutputStream[]{new FileOutputStream(file0), new FileOutputStream(file1), new FileOutputStream(file2), new FileOutputStream(file3)};
            for (int i = 0; i < 4; i++) {
                fileChannels[i] = fileOutputStreams[i].getChannel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeChannel() {
        for (FileOutputStream fileOutputStream : fileOutputStreams) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (FileChannel fileChannel : fileChannels) {
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void write(StringBuilder[] data) {
        try {
            for (int i = 0; i < data.length; i++) {
                ByteBuffer buffer = ByteBuffer.wrap(data[i].toString().getBytes());
                fileChannels[i].write(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeResult(String result) throws IOException {
        File file = new File(System.getProperty("user.dir") + File.separator + "result");
        file.createNewFile();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(result);
            bufferedWriter.flush();
        }
    }

    public static void readFile(File file, boolean isIpLogFile) throws Exception {
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "r")) {
            FileChannel fileChannel = accessFile.getChannel();
            long fileLength = fileChannel.size();
            if (fileLength > Integer.MAX_VALUE) {
                long offset = 0;
                long regionSize = Integer.MAX_VALUE;
                long remaining = fileLength;
                byte[] headBuff = null;
                while (remaining > 0) {
                    boolean isFileEnd = false;
                    if (fileLength - offset < Integer.MAX_VALUE) {
                        regionSize = fileLength - offset;
                        isFileEnd = true;
                    }
                    MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, offset, regionSize);
                    headBuff = readContent(mappedByteBuffer, headBuff, isFileEnd, isIpLogFile);
                    offset += regionSize;
                    remaining -= regionSize;
                    clean(mappedByteBuffer);
                }
            } else {
                MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileLength);
                readContent(mappedByteBuffer, null, true, isIpLogFile);
                clean(mappedByteBuffer);
            }
        }
    }

    private static byte[] readContent(MappedByteBuffer mappedByteBuffer, byte[] headBuff, boolean isFileEnd, boolean isIpLogFile) {
        int limit = mappedByteBuffer.limit();
        int buffSize = 16 * 1024 * 1024;
        byte[] headBuffTemp = headBuff;
        while (limit - mappedByteBuffer.position() > buffSize) {
            byte[] buffByte = new byte[buffSize];
            mappedByteBuffer.get(buffByte);
            int end = buffByte.length;
            for (int i = buffByte.length - 1; i > 0; i--) {
                if (buffByte[i] == '\n') {
                    end = i + 1;
                }
            }
            if (isIpLogFile) {
                IpUtil.calculateIpCount(headBuffTemp, buffByte, end);
            } else {
                IpUtil.asyncCalculateIp(headBuffTemp, buffByte, end);
            }
            headBuffTemp = new byte[buffByte.length - end];
            for (int i = end, j = 0; i < buffByte.length; i++, j++) {
                headBuffTemp[j] = buffByte[i];
            }
        }
        byte[] buffByte = new byte[limit - mappedByteBuffer.position()];
        mappedByteBuffer.get(buffByte);
        int end = buffByte.length;
        for (int i = buffByte.length - 1; i > 0; i--) {
            if (buffByte[i] == '\n') {
                end = i + 1;
            }
        }
        if (isIpLogFile) {
            if (isFileEnd) {
                IpUtil.calculateIpCount(headBuffTemp, buffByte, buffByte.length);
                IpUtil.calculateTopN();
            } else {
                IpUtil.calculateIpCount(headBuffTemp, buffByte, end);
            }
        } else {
            if (isFileEnd) {
                IpUtil.asyncCalculateIp(headBuffTemp, buffByte, buffByte.length);
            } else {
                IpUtil.asyncCalculateIp(headBuffTemp, buffByte, end);
            }
        }

        headBuffTemp = new byte[buffByte.length - end];
        for (int i = end, j = 0; i < buffByte.length; i++, j++) {
            headBuffTemp[j] = buffByte[i];
        }
        return headBuffTemp;
    }

    private static void clean(MappedByteBuffer mappedByteBuffer) {
        if (mappedByteBuffer == null || !mappedByteBuffer.isDirect() || mappedByteBuffer.capacity() == 0)
            return;
        invoke(invoke(viewed(mappedByteBuffer), "cleaner"), "clean");
    }

    private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
        return AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Method method = method(target, methodName, args);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static Method method(Object target, String methodName, Class<?>[] args) throws NoSuchMethodException {
        try {
            return target.getClass().getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            return target.getClass().getDeclaredMethod(methodName, args);
        }
    }

    private static ByteBuffer viewed(ByteBuffer buffer) {
        String methodName = "viewedBuffer";
        Method[] methods = buffer.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals("attachment")) {
                methodName = "attachment";
                break;
            }
        }
        ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
        if (viewedBuffer == null)
            return buffer;
        else
            return viewed(viewedBuffer);
    }

    private FileUtil() {
    }
}
