package cz.muni.fi.pv168;

import cz.muni.fi.pv168.common.DBUtils;
import java.sql.Connection;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbcp2.BasicDataSource;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by MONNY on 27-Mar-16.
 */
public class AssignmentManagerImplTest {

    private AssignmentManagerImpl manager;
    private MissionManagerImpl missionManager;
    private AgentManagerImpl agentManager;
    private DataSource ds;

    private final static ZonedDateTime NOW
            = LocalDateTime.of(2016, Month.MARCH, 29, 12, 00).atZone(ZoneId.of("UTC"));

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:AssignmentManagerTest");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        manager = new AssignmentManagerImpl(ds);
        missionManager = new MissionManagerImpl(ds);
        agentManager = new AgentManagerImpl(ds);
        DBUtils.executeSqlScript(ds, AssignmentManager.class.getResource("CreateTables.sql"));
       
        prepareTestData();
    }

    @After
    public void tearDown() throws Exception {
        /*try (Connection con = ds.getConnection()) {
            con.prepareStatement("DROP TABLE assignment").executeUpdate();
            con.prepareStatement("DROP TABLE mission").executeUpdate();
            con.prepareStatement("DROP TABLE agent").executeUpdate();
        }*/
        DBUtils.executeSqlScript(ds, AssignmentManager.class.getResource("DropTables.sql"));
    }

    private Mission m1, m2, m3, missionWithNullId, missionNotInDB;
    private Agent ag1, ag2, ag3, agentWithNullId, agentNotInDB;
    private Assignment ass1, ass2, ass3, ass4, ass5;

    private void prepareTestData() {

        m1 = new MissionBuilder().place("Slovakia").completed(false).build();
        m2 = new MissionBuilder().place("Czech").completed(false).build();
        m3 = new MissionBuilder().place("Czech").completed(true).build();
        missionManager.createMission(m1);
        missionManager.createMission(m2);
        missionManager.createMission(m3);
        missionWithNullId = new MissionBuilder().id(null).build();
        missionNotInDB = new MissionBuilder().id(m3.getId() + 1000).build();
        assertThat(missionManager.getMission(missionNotInDB.getId())).isNull();

        ag1 = new AgentBuilder().name("007").born(LocalDate.of(1980, 12, 1)).build();
        ag2 = new AgentBuilder().name("James Bond").born(LocalDate.of(1980, 8, 21)).build();
        ag3 = new AgentBuilder().name("Zorro").born(LocalDate.of(1955, 12, 15)).build();
        agentManager.createAgent(ag1);
        agentManager.createAgent(ag2);
        agentManager.createAgent(ag3);
        agentWithNullId = new AgentBuilder().id(null).build();
        agentNotInDB = new AgentBuilder().id(ag3.getId() + 1000).build();
        assertThat(agentManager.getAgent(agentNotInDB.getId())).isNull();

        ass1 = new AssignmentBuilder().agent(ag1).mission(m1)
                .from(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2020, Month.JULY, 9, 11, 00).atZone(ZoneId.of("UTC"))).build();
        ass2 = new AssignmentBuilder().agent(ag1).mission(m2)
                .from(LocalDateTime.of(2016, Month.NOVEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2018, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC"))).build();
        ass3 = new AssignmentBuilder().agent(ag2).mission(m1)
                .from(LocalDateTime.of(2017, Month.MAY, 27, 7, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2017, Month.MAY, 29, 20, 00).atZone(ZoneId.of("UTC"))).build();
        ass4 = new AssignmentBuilder().agent(ag3).mission(m1)
                .from(LocalDateTime.of(2015, Month.OCTOBER, 14, 16, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2016, Month.APRIL, 9, 10, 00).atZone(ZoneId.of("UTC"))).build();
        ass5 = new AssignmentBuilder().agent(ag3).mission(m3)
                .from(LocalDateTime.of(2011, Month.JANUARY, 19, 8, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2013, Month.DECEMBER, 31, 23, 59).atZone(ZoneId.of("UTC"))).build();

       
    }

    @Test
    public void testCreateAssignment() throws Exception {

        assertThat(ass1.getId()).isNull();

        manager.createAssignment(ass1);
        assertThat("Assignment id is null.", ass1.getId(), is(not(equalTo(null))));

        Long assignmentId = ass1.getId();
        Assignment loadedAssignment = manager.getAssignment(assignmentId);
        assertThat(loadedAssignment).isEqualTo(ass1);
        assertThat(loadedAssignment).isEqualToComparingFieldByField(ass1);
        assertThat("Assignment and loaded assignment is the same instance.", loadedAssignment, is(not(sameInstance(ass1))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAssignmentWithNullAssignment() {
        manager.createAssignment(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAssignmentWithNullAgent() {
        Assignment assignment = new AssignmentBuilder().agent(null).mission(m1)
                .from(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC"))).build();
        manager.createAssignment(assignment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAssignmentWithNullMission() {
        Assignment assignment = new AssignmentBuilder().agent(ag1).mission(null)
                .from(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC"))).build();
        manager.createAssignment(assignment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAssignmentWithId() {
        Assignment assignment = new AssignmentBuilder().agent(ag1).mission(m1)
                .from(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC"))).build();
        assignment.setId(1L);
        manager.createAssignment(assignment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAssignmentWithWrongTime() {
        Assignment assignment = new AssignmentBuilder().agent(ag1).mission(m1)
                .from(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2014, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC"))).build();
        manager.createAssignment(assignment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAssignmentWithAgentWithNullId() {
        Assignment assignment = new AssignmentBuilder().agent(agentWithNullId).mission(m1)
                .from(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC"))).build();
        manager.createAssignment(assignment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAssignmentWithMissionWithNullId() {
        Assignment assignment = new AssignmentBuilder().agent(ag1).mission(missionWithNullId)
                .from(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC"))).build();
        manager.createAssignment(assignment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAssignmentWithMissionNotInDB() {
        Assignment assignment = new AssignmentBuilder().agent(ag1).mission(missionNotInDB)
                .from(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC"))).build();
        manager.createAssignment(assignment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAssignmentWithAgentNotInDB() {
        Assignment assignment = new AssignmentBuilder().agent(agentNotInDB).mission(m1)
                .from(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")))
                .to(LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC"))).build();
        manager.createAssignment(assignment);
    }

    
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAssignmentWithNullId() {
        manager.getAssignment(ass1.getId());
    }

    @Test
    public void testGetAssignment() {

        manager.createAssignment(ass1);
        Long assignmentId = ass1.getId();
        Assignment loadedAssignment = manager.getAssignment(assignmentId);

        assertThat("Assignment and loaded assignment differ.", loadedAssignment, is(equalTo(ass1)));
        assertThat(loadedAssignment).isEqualToComparingFieldByField(ass1);
        //assertDeepEquals(ass1, loadedAssignment);
    }


    @Test
    public void testFindAssignmentsOfMission() throws Exception {

        assertThat(manager.findAssignmentsOfMission(m1)).isEmpty();
        assertThat(manager.findAssignmentsOfMission(m2)).isEmpty();
        assertThat(manager.findAssignmentsOfMission(m3)).isEmpty();

        manager.createAssignment(ass1);
        manager.createAssignment(ass2);
        manager.createAssignment(ass3);

        assertThat(manager.findAssignmentsOfMission(m3)).isEmpty();

        List<Assignment> listForM1 = new ArrayList<>();
        listForM1.add(ass1);
        assertThat(manager.findAssignmentsOfMission(m1)).isEqualTo(listForM1);

        List<Assignment> listForM2 = new ArrayList<>();
        listForM2.add(ass1);
        listForM2.add(ass3);
        assertThat(manager.findAssignmentsOfMission(m2)).isEqualTo(listForM2);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindAssignmentsOfNullMission() throws Exception {
        manager.findAssignmentsOfMission(null);
    }

    @Test
    public void testFindAgentOnAssignment() {

        manager.createAssignment(ass1);
        manager.createAssignment(ass2);

        Long ass2Id = ass2.getId();
        Assignment assignmentBefore = manager.getAssignment(ass2Id);

        Agent agent = manager.findAgentOnAssignment(ass1);

        assertThat(agent).isEqualTo(ass1.getAgent());
        assertThat(agent).isEqualToComparingFieldByField(ass1.getAgent());

        assertThat(assignmentBefore).isEqualTo(ass2);
        assertThat(assignmentBefore).isEqualToComparingFieldByField(ass2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAgentOnNullAssignment() {
        manager.findAgentOnAssignment(null);
    }

    @Test
    public void testFindActualAssignmentOfAgent() throws Exception {

        assertThat(manager.findActualAssignmentOfAgent(ag1)).isEmpty();
        assertThat(manager.findActualAssignmentOfAgent(ag2)).isEmpty();
        assertThat(manager.findActualAssignmentOfAgent(ag3)).isEmpty();

        manager.createAssignment(ass2);
        manager.createAssignment(ass4);
        manager.createAssignment(ass5);

        assertThat(manager.findActualAssignmentOfAgent(ag2)).isEmpty();

        List<Assignment> listForAg1 = new ArrayList<>();
        listForAg1.add(ass2);
        assertThat(manager.findActualAssignmentOfAgent(ag1)).isEqualTo(listForAg1);

        List<Assignment> listForAg3 = new ArrayList<>();
        listForAg3.add(ass4);
        assertThat(manager.findActualAssignmentOfAgent(ag3)).isEqualTo(listForAg3);
    }

    /*
    private static Assignment newAssignment(ZonedDateTime from, ZonedDateTime to, Agent agent, Mission mission) {
        Assignment assignment = new Assignment();
        assignment.setFrom(from);
        assignment.setTo(to);
        assignment.setAgent(agent);
        assignment.setMission(mission);
        return assignment;
    }

    private void assertDeepEquals(List<Assignment> expectedList, List<Assignment> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Assignment expected = expectedList.get(i);
            Assignment actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }
     */
 /*  private void assertDeepEquals(Assignment expected, Assignment actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFrom(), actual.getFrom());
        assertEquals(expected.getTo(), actual.getTo());
        assertEquals(expected.getAgent(), actual.getAgent());
        assertEquals(expected.getMission(), actual.getMission());
    }*/

 /* private static Comparator<Assignment> idComparator = new Comparator<Assignment>() {
        @Override
        public int compare(Assignment o1, Assignment o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };*/
}
