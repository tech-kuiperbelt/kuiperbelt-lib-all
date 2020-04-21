package tech.kuiperbelt.lib.common.jpa;

import java.util.regex.Pattern;

/**
 * 根据机器 IP 计算机器的序号的辅助类
 */
class IpAddressUtil {
    /**
     * 根据机器IP 以及掩码， 计算出当前机器在子网中的顺序
     * @param ipStr 机器IP
     * @param maskStr 子网掩码
     * @return
     */
    public static long  sequence(String ipStr, String maskStr) {
        return parse(ipStr) & (~parse(maskStr));
    }

    private static long  parse(String ipStr) {
        long result = 0L;
        // iterate over each octet
        for(String part : ipStr.split(Pattern.quote("."))) {
            // shift the previously parsed bits over by 1 byte
            result = result << 8;
            // set the low order bits to the current octet
            result |= Integer.parseInt(part);
        }
        return result;
    }
}
