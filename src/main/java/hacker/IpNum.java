package hacker;

/**
 * Created by gaofeng on 2019/10/25.
 */
public class IpNum {
    int ip;
    int count;

    IpNum(int ip, int count) {
        this.ip = ip;
        this.count = count;
    }

    public int getIp() {
        return ip;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return ip + " " + count + '\n';
    }
}
