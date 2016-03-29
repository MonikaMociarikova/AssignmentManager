package cz.muni.fi.pv168;

import java.time.ZonedDateTime;

/**
 * Created by MONNY on 29-Mar-16.
 */
public class AssignmentBuilder {

    private Long id;
    private ZonedDateTime from;
    private ZonedDateTime to;
    private Agent agent;
    private Mission mission;

    public AssignmentBuilder id (Long id) {
        this.id = id;
        return this;
    }
    public AssignmentBuilder from (ZonedDateTime from) {
        this.from = from;
        return this;
    }
    public AssignmentBuilder to (ZonedDateTime to) {
        this.to = to;
        return this;
    }
    public AssignmentBuilder agent (Agent agent) {
        this.agent = agent;
        return this;
    }
    public AssignmentBuilder mission (Mission mission) {
        this.mission = mission;
        return this;
    }
    public Assignment build() {
        Assignment assignment = new Assignment();
        assignment.setId(id);
        assignment.setMission(mission);
        assignment.setAgent(agent);
        assignment.setFrom(from);
        assignment.setTo(to);
        return assignment;
    }

}
