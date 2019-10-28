package hacker;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by gaofeng on 2019/10/25.
 */
public class SplitIpSet {

    private boolean[] flags = new boolean[65536];
    private Queue<Integer> activeGroups = new ConcurrentLinkedQueue<>();
    private SplitIpSet[] groupElements = new SplitIpSet[65536];
    private int[] counts = new int[65536];

    public void addIp(String ip) {
        addNum(IpUtil.ipToInt(ip));
    }

    public void addNum(int num) {
        int group = num >> 16;
        int element = num & 0xffff;
        // addGroup
        if (!flags[group]) {
            flags[group] = true;
            activeGroups.add(group);
            groupElements[group] = new SplitIpSet();
        }
        groupElements[group].addElement(element);
    }

    private void addElement(int element) {
        if (!flags[element]) {
            flags[element] = true;
            activeGroups.add(element);
        }
        counts[element]++;
    }

    public String allIpElements() {
        StringBuilder sb = new StringBuilder();
        for (Integer group : this.activeGroups) {
            int bigGroup = group << 16;
            SplitIpSet elementSet = groupElements[group];
            for (Integer element : elementSet.activeGroups) {
                sb.append(IpUtil.intToIp(bigGroup + element)).append(' ').append(elementSet.counts[element]).append('\n');
            }
        }
        return sb.toString();
    }

    public List<IpNum> topN(int n) {
        Comparator<IpNum> cp = Comparator.comparingInt(IpNum::getCount);
        PriorityQueue<IpNum> pq = new PriorityQueue<>(n, cp);// 小顶堆
        for (Integer group : this.activeGroups) {
            int bigGroup = group << 16;
            SplitIpSet elementSet = groupElements[group];
            for (Integer element : elementSet.activeGroups) {
                IpNum ipNum = new IpNum(bigGroup + element, elementSet.counts[element]);
                if (pq.size() < n) {
                    pq.add(ipNum);
                } else {
                    IpNum peek = pq.peek();
                    if (cp.compare(peek, ipNum) < 0) {
                        pq.poll();
                        pq.add(ipNum);
                    }
                }
            }
        }
        List<IpNum> result = new ArrayList<>(n);
        int i = 0;
        while (!pq.isEmpty() && i++ < n) {
            result.add(pq.poll());
        }
        return result;
    }


}
