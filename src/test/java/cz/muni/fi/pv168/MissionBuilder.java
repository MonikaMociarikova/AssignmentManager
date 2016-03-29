package cz.muni.fi.pv168;

/**
 * Created by MONNY on 29-Mar-16.
 */
public class MissionBuilder {

    private Long id;
    private String place;
    private boolean completed;

    public MissionBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public MissionBuilder place(String place) {
        this.place = place;
        return this;
    }

    public MissionBuilder completed(boolean completed) {
        this.completed = completed;
        return this;
    }

    public Mission build() {
        Mission mission = new Mission();
        mission.setId(id);
        mission.setCompleted(completed);
        mission.setPlace(place);
        return mission;
    }
}
