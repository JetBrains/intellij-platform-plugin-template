package com.github.maiqingqiang.goormhelper.utils;

import java.util.Map;

public class Strings {

    // https://github.com/golang/lint/blob/master/lint.go#L770
    public static final Map<String, String> COMMON_INITIALISMS = Map.ofEntries(
            Map.entry("ACL", "acl"),
            Map.entry("API", "api"),
            Map.entry("ASCII", "ascii"),
            Map.entry("CPU", "cpu"),
            Map.entry("CSS", "css"),
            Map.entry("DNS", "dns"),
            Map.entry("EOF", "eof"),
            Map.entry("GUID", "guid"),
            Map.entry("HTML", "html"),
            Map.entry("HTTP", "http"),
            Map.entry("HTTPS", "https"),
            Map.entry("ID", "id"),
            Map.entry("IP", "ip"),
            Map.entry("JSON", "json"),
            Map.entry("LHS", "lhs"),
            Map.entry("QPS", "qps"),
            Map.entry("RAM", "ram"),
            Map.entry("RHS", "rhs"),
            Map.entry("RPC", "rpc"),
            Map.entry("SLA", "sla"),
            Map.entry("SMTP", "smtp"),
            Map.entry("SQL", "sql"),
            Map.entry("SSH", "ssh"),
            Map.entry("TCP", "tcp"),
            Map.entry("TLS", "tls"),
            Map.entry("TTL", "ttl"),
            Map.entry("UDP", "udp"),
            Map.entry("UI", "ui"),
            Map.entry("UID", "uid"),
            Map.entry("UUID", "uuid"),
            Map.entry("URI", "uri"),
            Map.entry("URL", "url"),
            Map.entry("UTF8", "utf8"),
            Map.entry("VM", "vm"),
            Map.entry("XML", "xml")
    );


    public static String replaceCommonInitialisms(String s) {
        for (Map.Entry<String, String> entry : COMMON_INITIALISMS.entrySet()) {
            s = s.replaceAll(entry.getKey(), entry.getValue());
        }

        return s;
    }

    public static String clearQuote(String s) {
        if (s.startsWith("`") && s.endsWith("`")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    public static String clearSingleQuotn(String s) {
        if (s.startsWith("'") && s.endsWith("'")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

}
