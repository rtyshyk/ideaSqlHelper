package com.pivasyk.ideaPlugin;

import org.apache.commons.lang.StringUtils;

public class StringHelper {

    public static String padLeftStringByLine(String string, int col) {

        StringBuilder sb = new StringBuilder();
        String placeholder = StringHelper.getPlaceholder(" ", col);
        for (String line : string.split("\n")) {
            sb.append(placeholder.concat(line).concat("\n"));
        }

        return sb.toString();
    }

    public static String getPlaceholder(String padString, int size) {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append(padString);
        }

        return builder.toString();
    }

    public static String removeStringOffset(String string){

        StringBuilder sb = new StringBuilder();
        for (String line : string.split("\n")) {
            sb.append(line.trim().concat("\n"));
        }

        return sb.toString();
    }

}
