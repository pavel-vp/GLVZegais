package com.glvz.egais.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {

    private static String formatDateJson = "yyyy-MM-dd'T'HH:mm:ss";
    private static SimpleDateFormat dateFormatterJson = new SimpleDateFormat(formatDateJson);

    private static String formatDateBottlingJson = "yyyy-MM-dd";
    private static SimpleDateFormat dateBottlingFormatterJson = new SimpleDateFormat(formatDateBottlingJson);

    private static String formatDateDisplay = "dd.MM.yyyy";
    private static SimpleDateFormat dateFormatterDisplay = new SimpleDateFormat(formatDateDisplay);

    private static String formatDateImg = "yyyy-MM-dd HH:mm:ss";
    private static SimpleDateFormat dateFormatterImg = new SimpleDateFormat(formatDateImg);

    public static Date jsonStringToDate(String jsonString) {
        Date date = null;
        try {
            date = dateFormatterJson.parse(jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date simpleStringToDate(String string) {
        Date date = null;
        try {
            date = dateFormatterDisplay.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date imgStringToDate(String string) {
        Date date = null;
        try {
            date = dateFormatterImg.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date jsonBottlingStringToDate(String jsonString) {
        Date date = null;
        try {
            date = dateBottlingFormatterJson.parse(jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formatDateDisplay(Date date) {
        if (date == null)
            return "";
        String s = dateFormatterDisplay.format(date);
        return s;
    }

    public static String formatDateImg(Date date) {
        if (date == null)
            return "";
        String s = dateFormatterImg.format(date);
        return s;
    }

    public static String formatQty(Double qty) {
        if (qty == null) {
            return "";
        }
        String s = String.format("%.3f", qty);
        s = s.indexOf(",") < 0 ? s : s.replaceAll("0*$", "").replaceAll("\\,$", "");
        return s;
    }

    public static boolean isEmptyOrNull(String val) {
        return (val == null ||
                val.trim().equals(""));
    }

    public static String formatDateJson(Date date) {
        if (date == null)
            return "";
        String s = dateBottlingFormatterJson.format(date);
        return s;
    }

    public static String formatQtyNull(double qty) {
        return qty == 0 ? formatQty(null) : formatQty(qty);
    }
}
