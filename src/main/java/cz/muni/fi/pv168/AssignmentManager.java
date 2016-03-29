/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168;

import java.util.List;

/**
 *
 * @author Matej Sojak 433294
 */
public interface AssignmentManager {

    void createAssignment(Assignment assignment);

    void updateAssignment(Assignment assignment);

    void deleteAssignment(Assignment assignment);

    Assignment getAssignment(Long id);

    List<Assignment> findAllAssignments();

    List<Assignment> findAssignmentsOfAgent(Agent agent);

    List<Assignment> findAssignmentsOfMission(Mission mission);

    Agent findAgentOnAssignment(Assignment assignment);

    public List<Assignment> findActualAssignmentOfAgent(Agent agent);

}
