package com.example.parkingspaces;

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

        int numbers = 0, letters = 0;

        if (plate.length() != 8 || plate.charAt(2) != '-' || plate.charAt(5) != '-')
            return false;

        for (int i= 0; i < plate.length(); i++) {

            if (Character.isLetter(plate.charAt(i))) {
                if (plate.charAt(i) == (plate.toUpperCase().charAt(i))) {
                    ++letters;
                    continue;
                }
            }
            if (Character.isDigit(plate.charAt(i)))
                ++numbers;

            if (i == 2) {
                if (numbers != 2 && letters != 2)
                    return false;
            }
            if (i == 4) {
                if ((numbers != 2 || letters != 2) && (numbers != 4))
                    return false;
            }

        }
        return (numbers == 4 && letters == 2);
    }
}
