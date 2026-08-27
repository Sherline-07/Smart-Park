package com.smartpark.util;

import java.util.*;

/**
 * Lightweight JSON utility for serialization and parsing without external dependencies.
 */
public class JsonUtil {

    /**
     * Parse a flat or simple JSON string into a Map of key-value pairs.
     */
    public static Map<String, String> parseObject(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null || json.trim().isEmpty()) {
            return map;
        }

        String content = json.trim();
        if (content.startsWith("{")) content = content.substring(1);
        if (content.endsWith("}")) content = content.substring(0, content.length() - 1);

        // Simple token parser for "key": "value" or "key": number/boolean
        boolean inQuotes = false;
        StringBuilder currentToken = new StringBuilder();
        List<String> pairs = new ArrayList<>();

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                currentToken.append(c);
            } else if (c == ',' && !inQuotes) {
                pairs.add(currentToken.toString().trim());
                currentToken.setLength(0);
            } else {
                currentToken.append(c);
            }
        }
        if (currentToken.length() > 0) {
            pairs.add(currentToken.toString().trim());
        }

        for (String pair : pairs) {
            int colonIdx = -1;
            boolean quote = false;
            for (int i = 0; i < pair.length(); i++) {
                char c = pair.charAt(i);
                if (c == '\"' && (i == 0 || pair.charAt(i - 1) != '\\')) {
                    quote = !quote;
                } else if (c == ':' && !quote) {
                    colonIdx = i;
                    break;
                }
            }
            if (colonIdx != -1) {
                String key = cleanJsonString(pair.substring(0, colonIdx).trim());
                String val = cleanJsonString(pair.substring(colonIdx + 1).trim());
                map.put(key, val);
            }
        }
        return map;
    }

    private static String cleanJsonString(String str) {
        if (str == null) return "";
        str = str.trim();
        if (str.startsWith("\"") && str.endsWith("\"") && str.length() >= 2) {
            str = str.substring(1, str.length() - 1);
        }
        return str.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escapeJson((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return String.valueOf(obj);
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJson(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escapeJson(String.valueOf(obj)) + "\"";
    }

    public static String escapeJson(String str) {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
