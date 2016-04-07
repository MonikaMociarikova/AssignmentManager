/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.sql.DataSource;

import cz.muni.fi.pv168.common.ServiceFailureException;
import java.time.Instant;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matej Sojak 433294
 */
public class AssignmentManagerImpl implements AssignmentManager {

    final static Logger log = LoggerFactory.getLogger(AssignmentManagerImpl.class);
    private final DataSource dataSource;
    private AgentManagerImpl am;
    private MissionManagerImpl mm;

    public AssignmentManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        am = new AgentManagerImpl(dataSource);
        mm =  new MissionManagerImpl(dataSource);
    }
    
    @Override
    public void createAssignment(Assignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment is null.");
        }
        if (assignment.getId() != null) {
            throw new IllegalArgumentException("Assignment id is already set.");
        }
        if (assignment.getAgent() == null) {
            throw new IllegalArgumentException("Assignment agent is null.");
        }
        if (assignment.getMission() == null) {
            throw new IllegalArgumentException("Assignment mission is null.");
        }
        if (assignment.getFrom() == null) {
            assignment.setFrom(ZonedDateTime.now(/*ZoneId.systemDefault()*/));
        }
        if (assignment.getTo().compareTo(assignment.getFrom()) < 0) {
            throw new IllegalArgumentException("Wrong time attibutes.");
        }
        if (assignment.getAgent().getId() == null) {
            throw new IllegalArgumentException("Assignment's agent id is null.");
        }
        if (assignment.getMission().getId() == null) {
            throw new IllegalArgumentException("Assignment's mission id is null.");
        }
        if(am.getAgent(assignment.getAgent().getId())==null){
            throw new IllegalArgumentException("Agent not in db.");
        }
        if(mm.getMission(assignment.getMission().getId())==null){
            throw new IllegalArgumentException("Mission not in db.");
        }
        

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO assignment (starts,ends,agentId,missionId) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                //st.setDate(1, new java.sql.Date(Date.from(assignment.getFrom().toInstant()).getTime()));
                //st.setDate(2, new java.sql.Date(Date.from(assignment.getFrom().toInstant()).getTime()));
                st.setLong(3, assignment.getAgent().getId());
                st.setLong(4, assignment.getMission().getId());
                st.setTimestamp(1, new java.sql.Timestamp(Timestamp.from(assignment.getFrom().toInstant()).getTime()));
                st.setTimestamp(2, new java.sql.Timestamp(Timestamp.from(assignment.getTo().toInstant()).getTime()));
                int addedRows = st.executeUpdate();
                if (addedRows != 1) {
                    throw new ServiceFailureException("Internal Error: more rows inserted when trying to insert assignment " + assignment);
                }
                ResultSet keyRS = st.getGeneratedKeys();
                assignment.setId(getKey(keyRS, assignment));
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when creating an assignment", ex);
        }
    }

    private Long getKey(ResultSet keyRS, Assignment assignment) throws SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert assignment " + assignment
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert assignment " + assignment
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert assignment " + assignment
                    + " - no key found");
        }
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
        if ((assignment.getFrom() == null) || (assignment.getTo() == null)) {
            throw new IllegalArgumentException("Assignment contains no date boundary.");
        }
        //if (assignment.getFrom().isAfter(assignment.getTo())) {
        //    throw new IllegalArgumentException("Assignment contains wrong date boundaries.");
        //}

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("UPDATE assignment SET starts=?,ends=?,agentId=?,missionId=? WHERE id=?")) {
                //st.setDate(1, new java.sql.Date(Date.from(assignment.getFrom().toInstant()).getTime()));
                //st.setDate(2, new java.sql.Date(Date.from(assignment.getFrom().toInstant()).getTime()));
                st.setTimestamp(1, new java.sql.Timestamp(Timestamp.from(assignment.getFrom().toInstant()).getTime()));
                st.setTimestamp(2, new java.sql.Timestamp(Timestamp.from(assignment.getTo().toInstant()).getTime()));
                st.setLong(3, assignment.getAgent().getId());
                st.setLong(4, assignment.getMission().getId());
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
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM assignment WHERE id=?")) {
                st.setLong(1, assignment.getId());
                if (st.executeUpdate() != 1) {
                    throw new IllegalArgumentException("Cannot delete assignment " + assignment);
                }
            }
        } catch (SQLException ex) {
            log.error("Database connection problem.", ex);
            throw new ServiceFailureException("Error when deleting an assignment", ex);
        }
    }

    @Override
    public Assignment getAssignment(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,starts,ends,agentId,missionId FROM assignment WHERE id = ?")) {
                st.setLong(1, id);

                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Assignment assignment = resultSetToAssignment(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException(
                                "Internal error: More entities with the same id found "
                                + "(source id: " + id + ", found " + assignment + " and " + resultSetToAssignment(rs));
                    }
                    return assignment;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("Database connection problem", ex);
            throw new ServiceFailureException("Error when retrieving a mission", ex);
        }
    }

    @Override
    public List<Assignment> findAllAssignments() {
        log.debug("Finding all assignments");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,starts,ends,agentId,missionId FROM assignment")) {
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
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,starts,ends,agentId,missionId FROM assignment WHERE agentId=?")) {
                st.setLong(1, agent.getId());
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
        if (mission == null) {
            throw new IllegalArgumentException("mission is null");
        }
        log.debug("Finding all assignments");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,starts,ends,agentId,missionId FROM assignment WHERE missionId=?")) {
                st.setLong(1, mission.getId());
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
    public Agent findAgentOnAssignment(Assignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment is null.");
        }
        if (assignment.getAgent() == null) {
            throw new IllegalArgumentException("Assignment contains no agent.");
        }
        return assignment.getAgent();
    }

    @Override
    public List<Assignment> findActualAssignmentOfAgent(Agent agent) {
        log.debug("Finding all assignments");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,starts,ends,agentId,missionId FROM assignment WHERE agentId=?")) {
                st.setLong(1, agent.getId());
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

    private Assignment resultSetToAssignment(ResultSet rs) throws SQLException {
        Assignment assignment = new Assignment();
        AgentManagerImpl am = new AgentManagerImpl(this.dataSource);
        MissionManagerImpl mm = new MissionManagerImpl(this.dataSource);

        //miesto indexu pouzivame nazov stlpca, je to prehladnejsie, pytame sa funkciu konkretne nazov typu kt stlpec je
        assignment.setId(rs.getLong("id"));
        //assignment.setFrom(ZonedDateTime.ofInstant(new java.util.Date(rs.getDate("starts").getTime()).toInstant(), ZoneId.of("UTC")));
        //assignment.setTo(ZonedDateTime.ofInstant(new java.util.Date(rs.getDate("ends").getTime()).toInstant(), ZoneId.of("UTC")));
        assignment.setFrom(ZonedDateTime.ofInstant(Instant.ofEpochMilli(rs.getTimestamp("starts").getTime()),ZoneOffset.UTC));
        assignment.setTo(ZonedDateTime.ofInstant(Instant.ofEpochMilli(rs.getTimestamp("ends").getTime()),ZoneOffset.UTC));

        assignment.setAgent(am.getAgent(rs.getLong("agentId")));
        assignment.setMission(mm.getMission(rs.getLong("missionId")));
        return assignment;
    }

}
