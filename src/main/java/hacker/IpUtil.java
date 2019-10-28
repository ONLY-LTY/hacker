package hacker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by gaofeng on 2019/10/25.
 */
public class IpUtil {

    static Map<Integer, Integer> ipCnts1 = new ConcurrentHashMap<>(1024);
    static Map<Integer, Integer> ipCnts2 = new ConcurrentHashMap<>(1024);
    static Map<Integer, Integer> writeIpCnts = ipCnts1;

    static Map<Integer, Integer> resultIpCnts = new ConcurrentHashMap<>(1024);
    static Comparator<IpNum> cp = Comparator.comparingInt(IpNum::getCount);
    static PriorityQueue<IpNum> totalPq = new PriorityQueue<>(5, cp);

    public static ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
//    public static ThreadPoolExecutor countPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    static {
        poolExecutor.prestartAllCoreThreads();
//        countPoolExecutor.prestartAllCoreThreads();
    }

    public static void print() {
        writeIpCnts.forEach((nip, count) -> System.out.println(intToIp(nip) + " " + count));
    }

    private static void addIpSet(Map<Integer, Integer> ipCnts, int num, int value) {
        ipCnts.compute(num, (k, ov) -> {
            if (ov == null) {
                return value;
            } else {
                return ov + value;
            }
        });
    }

    public static void asyncCalculateIp(byte[] head, byte[] bytes, int end) {
        poolExecutor.submit(() -> {
            calculateIp(head, bytes, end);
            if (writeIpCnts.size() > 10000) {
                Map<Integer, Integer> newWriteIpCnts = (writeIpCnts == ipCnts1) ? ipCnts2 : ipCnts1;
                newWriteIpCnts.clear();
                Map<Integer, Integer> readIpCnts = writeIpCnts;// 读取,写文件
                writeIpCnts = newWriteIpCnts;
                writeIpCntsToFile(readIpCnts);
            }
        });
    }

    public static void writeIpCntsToFile(Map<Integer, Integer> readIpCnts) {
        StringBuilder[] groups = new StringBuilder[4];
        for (int i = 0; i < 4; i++) {
            groups[i] = new StringBuilder();
        }
        readIpCnts.forEach((nip, count) -> groups[nip & 3].append(nip).append(' ').append(count).append('\n'));
        FileUtil.write(groups);
    }

    public static void calculateIp(byte[] head, byte[] bytes, int end) {
        boolean start = true;
        int num = 0, idx = 0, part = 0;
        if (head != null) {
            for (byte bt : head) {
                if (bt == '\n') {
                    start = true;
                    continue;
                }
                if (start) {
                    if (bt == '\t') {
                        num += part << (8 * (3 - idx));
                        addIpSet(writeIpCnts, num, 1);
                        start = false;
                        num = 0;
                        part = 0;
                        idx = 0;
                    } else if (bt == '.') {
                        num += part << (8 * (3 - idx));
                        part = 0;
                        idx++;
                    } else {
                        part = part * 10 + (((char) bt) - '0');
                    }
                }
            }
        }
        for (int i = 0; i < end; i++) {
            byte bt = bytes[i];
            if (bt == '\n') {
                start = true;
                continue;
            }
            if (start) {
                if (bt == '\t') {
                    num += part << (8 * (3 - idx));
                    addIpSet(writeIpCnts, num, 1);
                    start = false;
                    num = 0;
                    part = 0;
                    idx = 0;
                } else if (bt == '.') {
                    num += part << (8 * (3 - idx));
                    part = 0;
                    idx++;
                } else {
                    part = part * 10 + (((char) bt) - '0');
                }
            }
        }
//        System.out.println(splitIpSet.allIpElements());
    }

    public static void calculateIpCount(byte[] head, byte[] bytes, int end) {
        int[] val = new int[2];
        int idx = 0, num = 0;
        boolean neg = false;
        if (head != null) {
            for (byte bt : head) {
                if (bt == '\n') {
                    val[idx] = neg ? -num : num;
                    addIpSet(resultIpCnts, val[0], val[1]);
                    idx = 0;
                    num = 0;
                    neg = false;
                } else if (bt == ' ') {
                    val[idx] = neg ? -num : num;
                    num = 0;
                    neg = false;
                    idx++;
                } else if (bt == '-') {
                    neg = true;
                }  else {
                    num = num * 10 + (((char) bt) - '0');
                }
            }
        }
        for (int i = 0; i < end; i++) {
            byte bt = bytes[i];
            if (bt == '\n') {
                val[idx] = neg ? -num : num;
                addIpSet(resultIpCnts, val[0], val[1]);
//                System.out.println(Arrays.toString(val));
                idx = 0;
                num = 0;
                neg = false;
            } else if (bt == ' ') {
                val[idx] = neg ? -num : num;
                num = 0;
                neg = false;
                idx++;
            } else if (bt == '-') {
                neg = true;
            } else {
                num = num * 10 + (((char) bt) - '0');
            }
        }
    }

    public static void calculateTopN() {
        resultIpCnts.forEach(IpUtil::addToHeap);
        resultIpCnts.clear();
    }

    public static void addToHeap(int nip, int count) {
        IpNum ipNum = new IpNum(nip, count);
        if (totalPq.size() < 5) {
            totalPq.add(ipNum);
        } else {
            IpNum peek = totalPq.peek();
            if (cp.compare(peek, ipNum) < 0) {
                totalPq.poll();
                totalPq.add(ipNum);
            }
        }
    }

    public static String getTopN() {
        String[] result = new String[5];
        int idx = 4;
        while (!totalPq.isEmpty()) {
            IpNum ipNum = totalPq.poll();
            result[idx--] = intToIp(ipNum.ip) + " " + ipNum.count + '\n';
        }
        StringBuilder sb = new StringBuilder();
        for (String s : result) {
            if (s != null) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    public static int ipToInt(String ip) {
        String[] split = ip.split("\\.");
        int result = 0;
        for (int i = 0; i < split.length; i++) {
            result = result | (Integer.parseInt(split[i]) << (8 * (3 - i)));
        }
        return result;
    }

    public static String intToIp(long num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append((num >> (8 * (3 - i))) & 0xff).append('.');
        }
        sb.append(num & 0xff);
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println('9' - '0');
        long i = ipToInt("223.104.234.194");
        System.out.println(i);
        System.out.println(intToIp(791977876));
    }

}
