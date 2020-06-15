package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class Prices {

    private static String json;

    private Prices(){ }

    private static void initData(){
        try {
            URL url = new URL("https://rsbuddy.com/exchange/summary.json");
            URLConnection connect = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            json = in.readLine();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<String> get(String itemName, BeerBuyer.Property property){

        if(json == null)
        {
            initData();
        }

        Optional<String> itemBlock =  Arrays
                .stream(json.split("\\{"))
                .filter(phrase -> findString(itemName, phrase))
                .findFirst();

        if(itemBlock.isPresent())
        {
            return Arrays
                    .stream(itemBlock.get().split(","))
                    .filter(phrase -> findString(property.getProperty(), phrase))
                    .map(Prices::clean)
                    .findFirst();
        }
        else
        {
            return Optional.empty();
        }
    }

    private static boolean findString(String target, String phrase){
        return phrase.matches(".*\"" + Pattern.quote(target) + "\".*");
    }

    private static String clean(String value){
        value = value.split(":")[1];
        return value.replaceAll("}","").replaceAll("\"","");
    }
}