/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;

import cz.muni.fi.pv168.common.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author Matej Sojak 433294
 */
public class AgentManagerImpl implements AgentManager {

    final static Logger log = LoggerFactory.getLogger(AgentManagerImpl.class);
    private final DataSource dataSource;

    public AgentManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createAgent(Agent agent) throws ServiceFailureException {
        if (agent == null) {
            throw new IllegalArgumentException("Agent is null.");
        }
        if (agent.getId() != null) {
            throw new IllegalArgumentException("Agent already exists.");
        }
        if (agent.getName() == null) {
            throw new IllegalArgumentException("Agent's name is null.");
        }
        if (agent.getBorn() == null) {
            throw new IllegalArgumentException("Agent's birth date is null.");
        }
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO agent (name,born) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, agent.getName());
                st.setDate(2, Date.valueOf(agent.getBorn()));

                int addedRows = st.executeUpdate();
                if (addedRows != 1) {
                    throw new ServiceFailureException("Internal Error: more rows inserted when trying to insert agent " + agent);
                }
                ResultSet keyRS = st.getGeneratedKeys();
                agent.setId(getKey(keyRS, agent));
            }
        } catch (SQLException sql) {
            throw new ServiceFailureException("Error when creating an agent.", sql);
        }

    }

    @Override
    public void updateAgent(Agent agent) throws ServiceFailureException {
        if (agent == null) {
            throw new IllegalArgumentException("Agent is null.");
        }
        if (agent.getId() == null) {
            throw new IllegalArgumentException("Agent's id is null.");
        }
        if (agent.getName() == null) {
            throw new IllegalArgumentException("Agent's name is null.");
        }
        if (agent.getBorn() == null) {
            throw new IllegalArgumentException("Agent's birth date is null.");
        }
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("UPDATE agent SET name=?,born=? WHERE id=?")) {
                st.setString(1, agent.getName());
                st.setDate(2, Date.valueOf(agent.getBorn()));
                st.setLong(3, agent.getId());
                if (st.executeUpdate() != 1) {
                    throw new IllegalArgumentException("Cannot update agent " + agent);
                }
            }
        } catch (SQLException ex) {
            log.error("Database connection problem.", ex);
            throw new ServiceFailureException("Error when updating an agent", ex);
        }
    }

    @Override
    public void deleteAgent(Agent agent) throws ServiceFailureException {
        if (agent == null) {
            throw new IllegalArgumentException("Agent is null.");
        }
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM agent WHERE id=?")){
                st.setLong(1,agent.getId());
                if (st.executeUpdate()!= 1) {
                    throw new IllegalArgumentException("Cannot delete agent " + agent);
                }
            }
        } catch (SQLException ex) {
            log.error("Database connection problem.",ex);
            throw new ServiceFailureException("Error when deleting an agent",ex);
        }
    }

    @Override
    public List<Agent> findAllAgents() throws ServiceFailureException {
        log.debug("Finding all agents");
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("SELECT id,name,born FROM agent")){
                ResultSet rs = st.executeQuery();
                List<Agent> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToAgent(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all agents", ex);
        }
    }

    @Override
    public Agent getAgent(Long id) throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("SELECT id,name,born FROM agent WHERE id=?")){
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Agent agent = resultSetToAgent(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException(
                                "Internal error: More entities with the same id found "
                                        + "(source id: " + id + ", found " + agent + " and " + resultSetToAgent(rs));
                    }
                    return agent;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("Database connection problem",ex);
            throw new ServiceFailureException("Error when retrieving an agent", ex);
        }
    }

    //ResultSet je tabulka, vysledok nasho sql dotazu. Objekt obsahuje index, ukazuje pred prvy riadok tabulky
    private Long getKey(ResultSet keyRS, Agent agent) throws SQLException {
        if (keyRS.next()) {
            //vracia mi to 1 kluc, takze ocakavam 1 stlpec
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert agent " + agent
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1); //tu mozeme pouzit aj string parameter getLong("nazov stlpca")
            //id musi byt unikatne, takze len 1 zaznam tam musi byt
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert agent " + agent
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert agent " + agent
                    + " - no key found");
        }
    }
    
    private Agent resultSetToAgent(ResultSet rs) throws SQLException {
        Agent agent = new Agent();
        //miesto indexu pouzivame nazov stlpca, je to prehladnejsie, pytame sa fciu konkretne nazov typu kt stlpec je
        agent.setId(rs.getLong("id"));
        agent.setName(rs.getString("name"));
        agent.setBorn(rs.getDate("born").toLocalDate());
        return agent;
    }

}
