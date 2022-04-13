package comp6708.service;

import comp6708.model.Place;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GeneratePlaceData {

    private static List<Place> placeList;

    static {
        try {
            InputStream inputStream = GeneratePersonData.class.getResourceAsStream("/Places");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            placeList = new ArrayList<>();
            String line = "";
            while((line = br.readLine()) != null) {
                String[] parts = line.split("	");
                Place place = new Place();
                place.setPlaceName(parts[0]);
                place.setPlaceDistrict(parts[1]);
                placeList.add(place);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Place generate() throws IOException {
        int n = placeList.size();
        int randomNum = ThreadLocalRandom.current().nextInt(0, n);
        return placeList.get(randomNum);
    }

}
