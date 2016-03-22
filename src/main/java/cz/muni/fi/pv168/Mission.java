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

        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null.");
        }
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


    @Override
    public String toString() {
        return "Mission{" + "id=" + id + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Mission other = (Mission) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
