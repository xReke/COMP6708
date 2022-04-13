package comp6708.service;

import comp6708.model.Person;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GeneratePersonData {

    private static List<String> personIdList;

    static {
        try {
            InputStream inputStream = GeneratePersonData.class.getResourceAsStream("/People");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            personIdList = new ArrayList<>();
            String line = "";
            while ((line = br.readLine()) != null) {
                personIdList.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Person generate() throws IOException {
        String id = personIdList.get(ThreadLocalRandom.current().nextInt(0, personIdList.size()));
        String gender = randomGender();
        Person person = new Person();
        person.setPersonID(id);
        person.setPersonGender(gender);
        person.setPersonAge(ThreadLocalRandom.current().nextInt(10, 80));
        return person;
    }

    private static String randomGender() {
        String[] genderList = new String[]{"M", "F"};
        int randomNum = ThreadLocalRandom.current().nextInt(0, 2);
        return genderList[randomNum];
    }

}
