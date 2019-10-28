package hacker;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Author LTY
 * Date 2019/10/25
 */
public class Hacker {
    public static void main(String[] args) throws Exception {
        //获取传入参数 目录名称
        String dir = args[0];
        File file = new File(dir);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0)
                for (File file1 : files) {
                    //读取目录下的文件 异步处理IP统计
                    FileUtil.readFile(file1, false);
                }
        }
        //等待线程池统计任务执行完毕 然后写入文件
        IpUtil.poolExecutor.shutdown();
        IpUtil.poolExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);
        //最后将内存的IP写入文件
        IpUtil.writeIpCntsToFile(IpUtil.writeIpCnts);

        //
        File writeFile = new File(System.getProperty("user.dir") + File.separator + "log");
        if (writeFile.isDirectory()) {
            File[] files = writeFile.listFiles();
            if (files != null && files.length > 0)
                for (File file1 : files) {
                    //读取目录下的文件 异步处理IP统计
                    FileUtil.readFile(file1, true);
                }
        }
//        System.out.println(IpUtil.getTopN());
        FileUtil.writeResult(IpUtil.getTopN());
//        IpUtil.countPoolExecutor.shutdown();
        FileUtil.closeChannel();
    }
}
