package com.humascot.awsinspector.service.utils;

public class StringUtil {
    public static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
    
        String[] words = input.split("_");
        StringBuilder result = new StringBuilder();
    
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase());
            }
        }
    
        return result.toString();
    }
}
