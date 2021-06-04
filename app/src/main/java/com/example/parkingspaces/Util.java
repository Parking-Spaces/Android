package com.example.parkingspaces;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static boolean isNumeric(final String str) {

        // null or empty
        if (str == null || str.length() == 0)
            return false;

        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }

        return true;
    }


    public static boolean isPlate(String plate) {

        Pattern pattern = Pattern.compile("\\A(\\d{2}-\\d{2}-[A-Z]{2}|\\d{2}-[A-Z]{2}-\\d{2}|[A-Z]{2}-\\d{2}-\\d{2})\\z");
        Matcher matcher =  pattern.matcher(plate);

        return matcher.find();
    }

    public static String formatPlate(String plate) {
        return plate.replaceAll("-", "");
    }
}
