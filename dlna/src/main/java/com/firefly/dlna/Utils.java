package com.firefly.dlna;

public class Utils {
    public static int timeStrToMsec(String timeStr) {
        int time = 0;

        try {
            String[] parts = timeStr.split(":");

            if (parts.length == 1) {
                time = Integer.valueOf(parts[0]) * 1000;
            } else if (parts.length == 2) {
                time = Integer.valueOf(parts[0]) * 60 * 1000
                        + Integer.valueOf(parts[1]) * 1000;
            } else if (parts.length == 3) {
                time = Integer.valueOf(parts[0]) * 60 * 60 * 1000
                        + Integer.valueOf(parts[1]) * 60 * 1000
                        + Integer.valueOf(parts[2]) * 1000;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return time;
    }

    public static int rescaling(int x, int xMin, int xMax, int yMin, int yMax) {
        return (yMax - yMin) * (x - xMin) / (xMax - xMin) + yMin;
    }
}
