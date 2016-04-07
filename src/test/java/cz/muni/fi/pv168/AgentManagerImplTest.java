/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168;

import java.sql.Connection;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

import static org.hamcrest.CoreMatchers.*;
import org.junit.After;
import static org.junit.Assert.*;

/**
 *
 * @author Matej Sojak 433294
 */
public class AgentManagerImplTest {

    private AgentManagerImpl manager;
    private DataSource dataSource;

    @Before
    public void setUp() throws Exception {
        BasicDataSource bds = new BasicDataSource();
        bds.setUrl("jdbc:derby:memory:AgentManagerTest;create=true");
        this.dataSource = bds;
        //create new empty table before every test
        try (Connection conn = bds.getConnection()) {
            conn.prepareStatement("CREATE TABLE agent ("
                    + "id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "name VARCHAR(255),"
                    + "born DATE)").executeUpdate();
        }
        manager = new AgentManagerImpl(bds);
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            con.prepareStatement("DROP TABLE agent").executeUpdate();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNull() throws Exception {
        manager.createAgent(null);
    }

    @Test
    public void createAgent() {
        Agent agent = newAgent("James Bond", LocalDate.of(1950, 1, 1));
        manager.createAgent(agent);

        Long agentId = agent.getId();
        assertThat("saved agent has null id", agent.getId(), is(not(equalTo(null))));

        Agent result = manager.getAgent(agentId);
        assertThat("retrieved agent differs from the saved one", result, is(equalTo(agent)));
        assertThat("retrieved agent is the same instance", result, is(not(sameInstance(agent))));
        assertDeepEquals(agent, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAgentWithWrongId() throws Exception {
        Agent agent = newAgent("James Bond", LocalDate.of(1950, 1, 1));
        agent.setId(1L);
        manager.createAgent(agent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAgentWithWrongName() throws Exception {
        Agent agent = newAgent(null, LocalDate.of(1950, 1, 1));
        manager.createAgent(agent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAgentWithWrongBirth() throws Exception {
        Agent agent = newAgent("James Bond", null);
        manager.createAgent(agent);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getNullAgent() throws Exception {
        Agent agent = manager.getAgent(null);
    }

    @Test
    public void getAllAgents() {

        assertTrue(manager.findAllAgents().isEmpty());

        Agent a1 = newAgent("Agent Smith", LocalDate.of(1969, 12, 12));
        Agent a2 = newAgent("James Bond", LocalDate.of(1950, 1, 1));

        manager.createAgent(a1);
        manager.createAgent(a2);

        List<Agent> expected = Arrays.asList(a1, a2);
        List<Agent> actual = manager.findAllAgents();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals("saved and retrieved agents differ", expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void deleteAgent() {

        Agent a1 = newAgent("Agent Smith", LocalDate.of(1969, 12, 12));
        Agent a2 = newAgent("James Bond", LocalDate.of(1950, 1, 1));
        manager.createAgent(a1);
        manager.createAgent(a2);

        assertNotNull(manager.getAgent(a1.getId()));
        assertNotNull(manager.getAgent(a2.getId()));

        manager.deleteAgent(a1);

        assertNull(manager.getAgent(a1.getId()));
        assertNotNull(manager.getAgent(a2.getId()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteAgentNull() throws Exception {
        Agent agent = newAgent("James Bond", LocalDate.of(1950, 1, 1));
        manager.deleteAgent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteAgentWithNullId() throws Exception {
        Agent agent = newAgent("James Bond", LocalDate.of(1950, 1, 1));
        agent.setId(null);
        manager.deleteAgent(agent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteAgentWithWrongId() throws Exception {
        Agent agent = newAgent("James Bond", LocalDate.of(1950, 1, 1));
        agent.setId(1L);
        manager.deleteAgent(agent);
    }

    private static Agent newAgent(String name, LocalDate born) {
        Agent agent = new Agent();
        agent.setName(name);
        agent.setBorn(born);
        return agent;
    }

    private void assertDeepEquals(List<Agent> expectedList, List<Agent> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Agent expected = expectedList.get(i);
            Agent actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Agent expected, Agent actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getBorn(), actual.getBorn());
    }

    private static Comparator<Agent> idComparator = new Comparator<Agent>() {
        @Override
        public int compare(Agent o1, Agent o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}
