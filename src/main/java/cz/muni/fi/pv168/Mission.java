package cz.muni.fi.pv168;

/**
 * Created by MM on 14-Mar-16.
 */
public class Mission {

    private Long id;
    private String place;
    private boolean completed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /*public Mission(String place) {
        this.place = place;
    }

    public Mission(String place, boolean completed) {
        this.place = place;
        this.completed = completed;
    }*/
}
