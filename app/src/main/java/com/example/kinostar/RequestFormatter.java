package com.example.kinostar;

import java.net.MalformedURLException;
import java.net.URL;

public class RequestFormatter {
    private static URL url;
    private String search;
    private int type;

    public static URL makeRequest(){
        try {
            url = new URL("https://api.kinopoisk.dev/movie?search=%D0%BD%D0%B0%D1%8F%20%D0%BA%D0%BD%D0%B8&field=name&isStrict=false&token=ZQQ8GMN-TN54SGK-NB3MKEC-ZKB8V06");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


}
