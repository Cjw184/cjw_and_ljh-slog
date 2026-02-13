package com.chenliao.chenliaoblog.utils;

import com.github.pagehelper.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import cn.hutool.json.JSONUtil;

public class IpUtil {
    /**
     * 获取ip地址（仅通过HttpServletRequest，完全移除Shiro依赖）
     * @param request HTTP请求对象
     * @return 客户端真实IP
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = null;

        // 依次从代理头中获取真实IP（覆盖所有常见的代理IP头）
        ip = request.getHeader("X-Forwarded-For");
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        // 如果所有代理头都拿不到，直接取请求的远程地址
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            // 处理本地回环地址（IPv4/IPv6）
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }

        // 处理多IP场景（多个IP用逗号分隔，取第一个）
        if (ip != null && ip.length() > 15 && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip == null ? "" : ip;
    }

    /**
     * 通过IP获取地址（调用百度开放接口）
     * @param ip IP地址
     * @return IP对应的地理位置
     */
    public static String getIpInfo(String ip) {
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "本地主机";
        }
        if (StringUtil.isEmpty(ip)) {
            return "";
        }

        String apiUrl = String.format("http://opendata.baidu.com/api.php?query=%s&co=&resource_id=6006&oe=utf8", ip);
        try (var reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(new java.net.URL(apiUrl).openConnection().getInputStream(), "utf-8"))) {

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Map<String, Object> resultMap = JSONUtil.toBean(result.toString(), Map.class);
            if (resultMap == null || !resultMap.containsKey("data")) {
                return "";
            }

            List<Map<String, String>> dataList = (List<Map<String, String>>) resultMap.get("data");
            if (dataList == null || dataList.isEmpty()) {
                return "";
            }

            return dataList.get(0).getOrDefault("location", "");

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}