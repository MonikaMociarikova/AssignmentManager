/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Matej Sojak 433294
 */
public class AssignmentManagerImpl implements AssignmentManager {
    final static Logger log = LoggerFactory.getLogger(AssignmentManagerImpl.class);
    private final DataSource dataSource;

    public AssignmentManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createAssignment(Assignment assignment) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAssignment(Assignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment is null.");
        }
        if (assignment.getId() == null) {
            throw new IllegalArgumentException("Assignment's id is null.");
        }
        if (assignment.getAgent() == null) {
            throw new IllegalArgumentException("Assignment contains no agent.");
        }
        if (assignment.getMission() == null) {
            throw new IllegalArgumentException("Assignment contains no mission.");
        }
        if((assignment.getFrom()==null) || (assignment.getTo()==null)){
            throw new IllegalArgumentException("Assignment contains no date boundary.");
        }
        if(assignment.getFrom().isAfter(assignment.getTo())){
           throw new IllegalArgumentException("Assignment contains wrong date boundaries."); 
        }
        
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("UPDATE assignment SET from=?,to=?,agent=?,mission=? WHERE id=?")) {
                st.setDate(1, new java.sql.Date(Date.from(assignment.getFrom().toInstant()).getTime()));
                st.setDate(2, new java.sql.Date(Date.from(assignment.getFrom().toInstant()).getTime()));
                st.setLong(3,assignment.getAgent().getId());
                st.setLong(4,assignment.getMission().getId());
                st.setLong(5, assignment.getId());
                if (st.executeUpdate() != 1) {
                    throw new IllegalArgumentException("Cannot update assignment " + assignment);
                }
            }
        } catch (SQLException ex) {
            log.error("Database connection problem.", ex);
            throw new ServiceFailureException("Error when updating an assignment", ex);
        }
    }

    @Override
    public void deleteAssignment(Assignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment is null.");
        }
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM assignment WHERE id=?")){
                st.setLong(1,assignment.getId());
                if (st.executeUpdate()!= 1) {
                    throw new IllegalArgumentException("Cannot delete assignment " + assignment);
                }
            }
        } catch (SQLException ex) {
            log.error("Database connection problem.",ex);
            throw new ServiceFailureException("Error when deleting an assignment",ex);
        }
    }

    @Override
    public Assignment getAssignment(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Assignment> findAllAssignments() {
        log.debug("Finding all assignments");
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("SELECT id,from,to,agent,mission FROM assignment")){
                ResultSet rs = st.executeQuery();
                List<Assignment> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToAssignment(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all assignments", ex);
        }
    }

    @Override
    public List<Assignment> findAssignmentsOfAgent(Agent agent) {
        log.debug("Finding all assignments");
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("SELECT id,from,to,agent,mission FROM assignment WHERE agent=?")){
                st.setLong(1,agent.getId());
                ResultSet rs = st.executeQuery();
                List<Assignment> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToAssignment(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all assignments", ex);
        }
    }

    @Override
    public List<Assignment> findAssignmentsOfMission(Mission mission) {
        //TODO
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Agent> findAgentsOnAssignment(Assignment assignment) {
        //TODO
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private Assignment resultSetToAssignment(ResultSet rs) throws SQLException {
        Assignment assignment = new Assignment();
        AgentManagerImpl am = new AgentManagerImpl(this.dataSource);
        MissionManagerImpl mm = new MissionManagerImpl(this.dataSource);
        
        //miesto indexu pouzivame nazov stlpca, je to prehladnejsie, pytame sa fciu konkretne nazov typu kt stlpec je
        assignment.setId(rs.getLong("id"));
        assignment.setFrom(ZonedDateTime.ofInstant(rs.getDate("from").toInstant(),ZoneId.systemDefault()));
        assignment.setTo(ZonedDateTime.ofInstant(rs.getDate("to").toInstant(),ZoneId.systemDefault()));
        assignment.setAgent(am.getAgent(rs.getLong("agent")));
        assignment.setMission(mm.getMission(rs.getLong("mission")));
        return assignment;
    }
    
}
