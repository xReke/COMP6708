package comp6708.model;

public class Visited {
    private String person_id;
    private String person_gender;
    private int person_age;
    private String place_name;
    private String place_district;
    private boolean visited;
    private String time;

    public String getPerson_id() {
        return person_id;
    }

    public void setPerson_id(String person_id) {
        this.person_id = person_id;
    }

    public String getPerson_gender() {
        return person_gender;
    }

    public void setPerson_gender(String person_gender) {
        this.person_gender = person_gender;
    }

    public int getPerson_age() {
        return person_age;
    }

    public void setPerson_age(int person_age) {
        this.person_age = person_age;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public String getPlace_district() {
        return place_district;
    }

    public void setPlace_district(String place_district) {
        this.place_district = place_district;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return new com.google.gson.Gson().toJson(this);
    }
}
