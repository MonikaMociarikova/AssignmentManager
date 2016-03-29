package cz.muni.fi.pv168;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.*;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by MONNY on 27-Mar-16.
 */
public class AssignmentManagerImplTest {

    private AssignmentManagerImpl manager;
    private DataSource dataSource;

    private final static ZonedDateTime NOW
            = LocalDateTime.of(2016, Month.MARCH, 29, 12, 00).atZone(ZoneId.of("UTC"));


    @Before
    public void setUp() throws Exception {
        BasicDataSource bds = new BasicDataSource();
        bds.setUrl("jdbc:derby:memory:AssignmentManagerTest;create=true");
        this.dataSource = bds;
        //create new empty table before every test
        try (Connection conn = bds.getConnection()) {
            conn.prepareStatement("CREATE TABLE assignment ("
                    + "id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "from DATE,"
                    + "to DATE,"
                    + "agentId BIGINT REFERENCES agent(id) on delete cascade,"
                    + "missionId BIGINT REFERENCES mission(id) on delete cascade)").executeUpdate();
        }
        manager = new AssignmentManagerImpl(bds);
    }

    @After
    public void tearDown() throws Exception {
        try (Connection con = dataSource.getConnection()) {
            con.prepareStatement("DROP TABLE assignment").executeUpdate();
        }
    }

    @Test
    public void testCreateAssignment() throws Exception {
        Assignment assignment =
                newAssignment(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")),
                        LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC")), null, new Mission());
        manager.createAssignment(assignment);

        assertThat("Assignment id is null.",assignment.getId(), is(not(equalTo(null))));

        Long assignmentId = assignment.getId();
        Assignment loadedAssignment = manager.getAssignment(assignmentId);
        assertDeepEquals(assignment,loadedAssignment);
        assertThat("Assignment and loaded assignment is the same instance.",loadedAssignment,is(not(sameInstance(assignment))));
    }

    @Test (expected = IllegalArgumentException.class)
    public void createAssignmentWithNullAssignment() {
        manager.createAssignment(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createAssignmentWithNullAgent() {
        Assignment assignment =
                newAssignment(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")),
                        LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC")), null, new Mission());
        manager.createAssignment(assignment);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createAssignmentWithNullMission() {
        Assignment assignment =
                newAssignment(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")),
                        LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC")), new Agent(), null);
        manager.createAssignment(assignment);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createAssignmentWithId() {
        Assignment assignment =
                newAssignment(LocalDateTime.of(2015, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")),
                        LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC")), new Agent(), new Mission());
        assignment.setId(1L);
        manager.createAssignment(assignment);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createAssignmentWithWrongTime() {
        Assignment assignment =
                newAssignment(LocalDateTime.of(2017, Month.SEPTEMBER, 14, 15, 00).atZone(ZoneId.of("UTC")),
                        LocalDateTime.of(2016, Month.MAY, 29, 13, 00).atZone(ZoneId.of("UTC")), new Agent(), new Mission());
        manager.createAssignment(assignment);
    }



    @Test
    public void testUpdateAssignment() throws Exception {

    }

    @Test
    public void testDeleteAssignment() throws Exception {

    }

    @Test
    public void testGetAssignment() throws Exception {

    }

    @Test
    public void testFindAllAssignments() throws Exception {

    }

    @Test
    public void testFindAssignmentsOfAgent() throws Exception {

    }

    @Test
    public void testFindAssignmentsOfMission() throws Exception {

    }

    @Test
    public void testFindAgentOnAssignment() throws Exception {

    }

    @Test
    public void testFindActualAssignmentOfAgent() throws Exception {

    }


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

    private void assertDeepEquals(Assignment expected, Assignment actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFrom(), actual.getFrom());
        assertEquals(expected.getTo(), actual.getTo());
        assertEquals(expected.getAgent(), actual.getAgent());
        assertEquals(expected.getMission(), actual.getMission());
    }

    private static Comparator<Assignment> idComparator = new Comparator<Assignment>() {
        @Override
        public int compare(Assignment o1, Assignment o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}