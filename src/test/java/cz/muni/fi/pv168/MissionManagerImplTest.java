package cz.muni.fi.pv168;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by MM on 14-Mar-16.
 */
public class MissionManagerImplTest {

    private MissionManagerImpl missionManager;
    private DataSource dataSource;

    @Before
    public void setUp() throws Exception {
        BasicDataSource bds = new BasicDataSource();
        bds.setUrl("jdbc:derby:memory:MissionManagerTest;create=true");
        this.dataSource = bds;
        //create new empty table before every test
        try (Connection conn = bds.getConnection()) {
            conn.prepareStatement("CREATE TABLE MISSION ("
                    + "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "place VARCHAR(255) NOT NULL ,"
                    + "completed BOOLEAN)").executeUpdate();
        }
        missionManager = new MissionManagerImpl(bds);    }

    @After
    public void tearDown() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            con.prepareStatement("DROP TABLE MISSION").executeUpdate();
        }
    }

    @Test
    public void testCreateMissionWithOneParameter() {
        Mission mission = newMissionOneParameter("USA");
        missionManager.createMission(mission);

        //id check
        //assertThat("Mission id is null.",mission.getId(), is(not(equalTo(null))));
        assertNotNull(mission.getId());
        assertThat("Completed is not false.",mission.isCompleted(), is(equalTo(false)));

        Long missionId = mission.getId();
        Mission loadedMission = missionManager.getMission(missionId);
        assertThat("Loaded mission differs from the saved one.",loadedMission,is(equalTo(mission)));
        assertDeepEquals(mission,loadedMission);
        assertThat("Mission and loaded mission is the same instance.",loadedMission,is(not(sameInstance(mission))));
    }

    @Test
    public void testCreateMissionWithTwoParameters() {
        Mission mission = newMissionTwoParameters("USA", true);
        missionManager.createMission(mission);

        //id check
        assertThat("Mission id is null.",mission.getId(), is(not(equalTo(null))));

        Long missionId = mission.getId();
        Mission loadedMission = missionManager.getMission(missionId);
        //assertThat("Loaded mission differs from the saved one.",loadedMission,is(equalTo(mission)));
        assertDeepEquals(mission,loadedMission);
        assertThat("Mission and loaded mission is the same instance.",loadedMission,is(not(sameInstance(mission))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNullMission() throws Exception{
        missionManager.createMission(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testCreateNullPlaceOneParameter() {
        Mission mission = newMissionOneParameter(null);
        missionManager.createMission(mission);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testCreateNullPlaceTwoParameters() {
        Mission mission = newMissionTwoParameters(null, false);
        missionManager.createMission(mission);
    }


    @Test (expected = IllegalArgumentException.class)
    public void createMissionWithId() throws Exception {
        Mission mission = newMissionTwoParameters("Russia", false);
        mission.setId(1L);
        missionManager.createMission(mission);
    }

    @Test
    public void testUpdateMission() {

        Mission missionToUpdate = newMissionTwoParameters("Slovakia",false);
        Mission otherMission = newMissionOneParameter("Secret");
        missionManager.createMission(missionToUpdate);
        missionManager.createMission(otherMission);

        Long missionId = missionToUpdate.getId();
        missionToUpdate.setPlace("Czech");
        missionManager.updateMission(missionToUpdate);
        missionToUpdate = missionManager.getMission(missionId);
        assertThat("Mission place was not changed.",missionToUpdate.getPlace(),is(equalTo("Czech")));
        assertThat("Mission completion was not changed.",missionToUpdate.isCompleted(),is(equalTo(false)));

        missionToUpdate.setCompleted(true);
        missionManager.updateMission(missionToUpdate);
        missionToUpdate = missionManager.getMission(missionId);
        assertThat("Mission place was not changed.",missionToUpdate.getPlace(),is(equalTo("Czech")));
        assertThat("Mission completion was not changed.",missionToUpdate.isCompleted(),is(equalTo(true)));

        assertDeepEquals(otherMission, missionManager.getMission(otherMission.getId()));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testUpdateNullMission() throws Exception{
        missionManager.updateMission(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testUpdateMissionWrongId() throws Exception {
        Mission mission = newMissionTwoParameters("Secret", false);
        missionManager.createMission(mission);
        Long missionId = mission.getId();
        mission.setId(missionId - 1);
        missionManager.updateMission(mission);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testUpdateMissionWrongPlace() throws Exception {
        Mission mission = newMissionTwoParameters("Secret", false);
        missionManager.createMission(mission);
        Long missionId = mission.getId();
        mission.setPlace(null);
        missionManager.updateMission(mission);
    }


    @Test
    public void testDeleteMission() {

        Mission mission1 = newMissionTwoParameters("Slovakia", true);
        Mission mission2 = newMissionOneParameter("Hungary");

        missionManager.createMission(mission1);
        missionManager.createMission(mission2);

        assertNotNull(missionManager.getMission(mission1.getId()));
        assertNotNull(missionManager.getMission(mission2.getId()));

        missionManager.deleteMission(mission1);

        assertNull(missionManager.getMission(mission1.getId()));
        assertNotNull(missionManager.getMission(mission2.getId()));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testDeleteNullMission() throws Exception{
        missionManager.deleteMission(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testDeleteMissionWithNullId() {
        Mission mission = newMissionTwoParameters("Germany",false);
        mission.setId(null);
        missionManager.deleteMission(mission);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testDeleteMissionWithSetId() {
        Mission mission = newMissionTwoParameters("Germany",false);
        mission.setId(1L);
        missionManager.deleteMission(mission);
    }

    @Test
    public void testGetMission() {

        Mission mission = newMissionOneParameter("France");
        missionManager.createMission(mission);

        Long missionId = mission.getId();
        Mission loadedMission = missionManager.getMission(missionId);

        assertThat("Mission and loaded mission differ.",loadedMission,is(equalTo(mission)));
        assertDeepEquals(mission,loadedMission);
    }

    @Test
    public void testFindAllMissions() {

        assertTrue(missionManager.findAllMissions().isEmpty());

        Mission mission1 = newMissionTwoParameters("Slovakia", false);
        Mission mission2 = newMissionOneParameter("Czech");

        missionManager.createMission(mission1);
        missionManager.createMission(mission2);

        List<Mission> expected = Arrays.asList(mission1, mission2);
        List<Mission> actual = missionManager.findAllMissions();

        Collections.sort(expected, idComparator);
        Collections.sort(actual, idComparator);

        //assertThat("Expected and actual lists differ.", expected, is(equalTo(actual)));
        assertEquals(expected, actual);
        assertDepEquals(expected, actual);
    }

    @Test
    public void testFindUncompletedMissions() {

        assertTrue(missionManager.findUncompletedMissions().isEmpty());

        Mission mission1 = newMissionTwoParameters("Slovakia", false);
        Mission mission2 = newMissionTwoParameters("Austria", true);
        Mission mission3 = newMissionOneParameter("Czech");

        missionManager.createMission(mission1);
        missionManager.createMission(mission2);
        missionManager.createMission(mission3);

        List<Mission> expected = Arrays.asList(mission1, mission3);
        List<Mission> actual = missionManager.findUncompletedMissions();

        Collections.sort(expected, idComparator);
        Collections.sort(actual, idComparator);

        assertThat("Expected and actual lists differ.", expected, is(equalTo(actual)));
        assertDepEquals(expected, actual);
    }

    @Test
    public void testFindMissionsByPlace() {

        assertTrue(missionManager.findMissionsByPlace("Czech").isEmpty());

        Mission mission1 = newMissionTwoParameters("Czech", false);
        Mission mission2 = newMissionTwoParameters("Austria", true);
        Mission mission3 = newMissionOneParameter("Czech");

        missionManager.createMission(mission1);
        missionManager.createMission(mission2);
        missionManager.createMission(mission3);

        List<Mission> expected = Arrays.asList(mission1, mission3);
        List<Mission> actual = missionManager.findMissionsByPlace("Czech");

        Collections.sort(expected, idComparator);
        Collections.sort(actual, idComparator);

        assertThat("Expected and actual lists differ.", expected, is(equalTo(actual)));
        assertDepEquals(expected, actual);
    }



    private static Mission newMissionOneParameter(String place) {
        Mission mission  = new Mission();
        mission.setPlace(place);
        return mission;
    }

    private static Mission newMissionTwoParameters(String place, boolean completed){
        Mission mission  = new Mission();
        mission.setPlace(place);
        mission.setCompleted(completed);
        return mission;
    }

    private void assertDeepEquals(Mission mission, Mission loadedMission){
        assertEquals("Id value differs.",mission.getId(),loadedMission.getId());
        assertEquals("Place value differs.",mission.getPlace(),loadedMission.getPlace());
        assertEquals("Completed value differs.",mission.isCompleted(),loadedMission.isCompleted());
    }

    private void assertDepEquals(List<Mission> missionList, List<Mission> loadedMissionList){
        for (int i = 0; i < loadedMissionList.size(); i++) {
            Mission mission = missionList.get(i);
            Mission loadedMission = loadedMissionList.get(i);
            assertDeepEquals(mission,loadedMission);
        }
    }

    private static Comparator<Mission> idComparator = new Comparator<Mission>() {
        @Override
        public int compare(Mission o1, Mission o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}