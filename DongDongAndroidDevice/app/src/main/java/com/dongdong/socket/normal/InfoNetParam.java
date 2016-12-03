package com.dongdong.socket.normal;

/**
 * 网络配置信息
 */
public class InfoNetParam {
    /**
     * 当前网络类型 见DSDefine.java
     */
    public byte nettype = 0;

    /**
     * 当前网口地址获取类型 见DSDefine.java
     */
    public byte ethaddrtype = 0;

    /**
     * 网口IP
     */
    public int ethip = 0;

    /**
     * 网口子网掩码
     */
    public int ethmask = 0;

    /**
     * 缺省网关
     */
    public int defaultgw = 0;

    /**
     * 当前DNS地址获取类型 见DSDefine.java
     */
    public byte dnsaddrtype = 0;

    /**
     * 首选DNS服务器
     */
    public int primarydns = 0;

    /**
     * 备用DNS服务器
     */
    public int secondarydns = 0;

    /**
     * MAC地址, 如 00:0C:29:93:EF:EB
     */
    public String macaddress = "00:00:00:00:00:00";

    /**
     * 构造函数
     */
    public InfoNetParam() {
    }

    /**
     * 获取mac地址 macaddress字段的填写方式： 比如MAC地址为：00:0C:29:93:EF:EB，填写方式如下：
     * Macaddress[0]=0x00; Macaddress[1]=0x0C; Macaddress[2]=0x29;
     * Macaddress[3]=0x93; Macaddress[4]=0xEF; Macaddress[5]=0xEB;
     */
    public byte[] getMacAddress() {
        String[] mac = macaddress.split(":");
        String mac2 = "";
        for (int i = 0; i < mac.length; i++) {
            mac2 += mac[i];
        }
        return hexStringToByte(mac2);
    }

    /**
     * 设置mac地址 macaddress字段的填写方式： 比如MAC地址为：00:0C:29:93:EF:EB，填写方式如下：
     * Macaddress[0]=0x00; Macaddress[1]=0x0C; Macaddress[2]=0x29;
     * Macaddress[3]=0x93; Macaddress[4]=0xEF; Macaddress[5]=0xEB;
     */
    public void setMacAddress(byte[] mac) {
        macaddress = bytesToHexString(mac);
    }

    private String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
            if ((i + 1) != bArray.length) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    private byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    private byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    public String intToIp(int ipInt) {
        return new StringBuilder().append(((ipInt >> 24) & 0xff)).append('.')
                .append((ipInt >> 16) & 0xff).append('.')
                .append((ipInt >> 8) & 0xff).append('.').append((ipInt & 0xff))
                .toString();
    }

    public String getNettype() {

		/*
         * public final static int NETTYPE_WIRED = 1; // 使用有线网络 public final
		 * static int NETTYPE_WIFI = 2; // 使用Wifi网络 public final static int
		 * NETTYPE_3G = 4; // 使用3G网络 public final static int NETTYPE_4G = 8; //
		 * 使用4G网络
		 */
        if (nettype == 1) {
            return "有线网络";
        } else if (nettype == 2) {
            return "WIFI网络";
        } else if (nettype == 3) {
            return "3G网络";
        } else if (nettype == 4) {
            return "4G网络";
        }

        return "未知";
    }

    public void setNettype(byte nettype) {
        this.nettype = nettype;
    }

    public byte getEthaddrtype() {
        return ethaddrtype;
    }

    public void setEthaddrtype(byte ethaddrtype) {
        this.ethaddrtype = ethaddrtype;
    }

    public String getEthip() {

        return intToIp(ethip);
    }

    public void setEthip(int ethip) {
        this.ethip = ethip;
    }

    public String getEthmask() {
        return intToIp(ethmask);
    }

    public void setEthmask(int ethmask) {
        this.ethmask = ethmask;
    }

    public String getDefaultgw() {
        return intToIp(defaultgw);
    }

    public void setDefaultgw(int defaultgw) {
        this.defaultgw = defaultgw;
    }

    public byte getDnsaddrtype() {
        return dnsaddrtype;
    }

    public void setDnsaddrtype(byte dnsaddrtype) {
        this.dnsaddrtype = dnsaddrtype;
    }

    public String getPrimarydns() {
        return intToIp(primarydns);
    }

    public void setPrimarydns(int primarydns) {
        this.primarydns = primarydns;
    }

    public String getSecondarydns() {
        return intToIp(secondarydns);
    }

    public void setSecondarydns(int secondarydns) {
        this.secondarydns = secondarydns;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    @Override
    public String toString() {
        return getNettype() + " & " + getEthaddrtype() + " & " + getEthip()
                + " & " + getEthmask() + "& " + getDefaultgw() + " & "
                + getDnsaddrtype() + " & " + getPrimarydns() + " & "
                + getSecondarydns() + " &　" + getMacaddress();
    }

}
