package hacker;

/**
 * Created by gaofeng on 2019/10/25.
 */
public class IpTopN {

    public static void main(String[] args) {
        SplitIpSet splitIpSet = new SplitIpSet();
        splitIpSet.addIp("10.23.12.234");
        splitIpSet.addIp("10.23.12.234");
        splitIpSet.addIp("10.23.12.234");
        splitIpSet.addIp("10.23.12.234");
        splitIpSet.addIp("10.23.12.234");
        splitIpSet.addIp("10.23.12.234");
        splitIpSet.addIp("10.23.12.234");
        splitIpSet.addIp("10.23.12.234");
        splitIpSet.addIp("10.23.12.234");
        splitIpSet.addIp("10.23.12.234");
        splitIpSet.addIp("10.23.12.234");
        splitIpSet.addIp("10.23.12.235");
        splitIpSet.addIp("10.23.12.235");
        splitIpSet.addIp("10.23.12.236");
        splitIpSet.addIp("10.23.12.236");
        splitIpSet.addIp("10.23.12.237");
        splitIpSet.addIp("10.23.12.237");
        splitIpSet.addIp("10.23.12.238");
        splitIpSet.addIp("10.23.12.238");
        splitIpSet.addIp("10.23.12.239");
        splitIpSet.addIp("10.23.12.239");
        splitIpSet.addIp("10.23.12.245");
        splitIpSet.addIp("10.23.12.245");
        splitIpSet.addIp("10.23.12.246");
        splitIpSet.addIp("10.23.12.247");

        System.out.println(splitIpSet.topN(5));
    }

}
