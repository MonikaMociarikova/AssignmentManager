package cz.muni.fi.pv168;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MM on 14-Mar-16.
 */
public class MissionManagerImpl implements MissionManager {

    final static Logger log = LoggerFactory.getLogger(MissionManagerImpl.class);
    private final DataSource dataSource;

    public MissionManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createMission(Mission mission) throws ServiceFailureException {
        if (mission == null) {
            throw new IllegalArgumentException("Mission is null.");
        }
        if (mission.getId() != null) {
            throw new IllegalArgumentException("Mission id is already set.");
        }
        if (mission.getPlace() == null) {
            throw new IllegalArgumentException("Mission place is null.");
        }
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO mission (place,completed) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1,mission.getPlace());
                st.setBoolean(2, mission.isCompleted());

                int addedRows = st.executeUpdate();
                if (addedRows != 1) {
                    throw new ServiceFailureException("Internal Error: more rows inserted when trying to insert misson " + mission);
                }
                ResultSet keyRS = st.getGeneratedKeys();
                mission.setId(getKey(keyRS, mission));
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when creating a mission", ex);
        }

    }

    //ResultSet je tabulka, vysledok nasho sql dotazu. Objekt obsahuje index, ukazuje pred prvy riadok tabulky
    private Long getKey(ResultSet keyRS, Mission mission) throws SQLException {
        if (keyRS.next()) {
            //vracia mi to 1 kluc, takze ocakavam 1 stlpec
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert mission " + mission
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1); //tu mozeme pouzit aj string parameter getLong("nazov stlpca")
            //id musi byt unikatne, takze len 1 zaznam tam musi byt
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert mission " + mission
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert mission " + mission
                    + " - no key found");
        }
    }


    @Override
    public void updateMission(Mission mission) /*throws ServiceFailureException*/ {
        if (mission == null) {
            throw new IllegalArgumentException("Mission is null.");
        }
        if (mission.getId()==null) {
            throw new IllegalArgumentException("Mission id is null.");
        }
        if (mission.getPlace() == null) {
            throw new IllegalArgumentException("Mission place is null.");
        }
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("UPDATE mission SET place=?,completed=? WHERE id=?")){
                st.setString(1,mission.getPlace());
                st.setBoolean(2,mission.isCompleted());
                st.setLong(3,mission.getId());
                if (st.executeUpdate()!= 1) {
                    throw new IllegalArgumentException("Cannot update mission " + mission);
                }
            }
        } catch (SQLException ex) {
            log.error("Database connection problem.",ex);
            throw new ServiceFailureException("Error when updating a mission",ex);
        }

    }
/* Long id
    @Override
    public void deleteMission(Long missionId) throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM mission WHERE id=?")){
                st.setLong(1,missionId);
                if (st.executeUpdate()!= 1) {
                    throw new IllegalArgumentException("Cannot delete mission with id" + missionId);
                }
            }
        } catch (SQLException ex) {
            log.error("Database connection problem.",ex);
            throw new ServiceFailureException("Error when retrieving all graves",ex);
        }
    }
*/

    @Override
    public void deleteMission(Mission mission) throws ServiceFailureException {
        if (mission == null) {
            throw new IllegalArgumentException("Mission is null.");
        }
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM mission WHERE id=?")){
                st.setLong(1,mission.getId());
                if (st.executeUpdate()!= 1) {
                    throw new IllegalArgumentException("Cannot delete mission " + mission);
                }
            }
        } catch (SQLException ex) {
            log.error("Database connection problem.",ex);
            throw new ServiceFailureException("Error when deleting a mission",ex);
        }
    }

    @Override
    public Mission getMission(Long id) /*throws ServiceFailureException*/ {
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("SELECT id,place,completed FROM MISSION WHERE id=?")){
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Mission mission = resultSetToMission(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException(
                                "Internal error: More entities with the same id found "
                                        + "(source id: " + id + ", found " + mission + " and " + resultSetToMission(rs));
                    }
                    return mission;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("Database connection problem",ex);
            throw new ServiceFailureException("Error when retrieving a mission", ex);
        }
    }

    private Mission resultSetToMission(ResultSet rs) throws SQLException {
        Mission mission = new Mission();
        //miesto indexu pouzivame nazov stlpca, je to prehladnejsie, pytame sa fciu konkretne nazov typu kt stlpec je
        mission.setId(rs.getLong("id"));
        mission.setPlace(rs.getString("place"));
        mission.setCompleted(rs.getBoolean("completed"));
        return mission;
    }

    @Override
    public List<Mission> findAllMissions() /*throws ServiceFailureException*/ {
        log.debug("Finding all missions"); //?????????
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("SELECT id,place,completed FROM mission")){
                ResultSet rs = st.executeQuery();
                List<Mission> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToMission(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all missions", ex);
        }
    }

    @Override
    public List<Mission> findUncompletedMissions() /*throws ServiceFailureException*/ {
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("SELECT id,place,completed FROM mission WHERE completed=false")){
                ResultSet rs = st.executeQuery();
                List<Mission> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToMission(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all missions", ex);
        }
    }

    @Override
    public List<Mission> findMissionsByPlace(String place) /*throws ServiceFailureException*/ {
        try (Connection conn = dataSource.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("SELECT id,place,completed FROM mission WHERE place=?")){
                st.setString(1,place);
                ResultSet rs = st.executeQuery();
                List<Mission> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToMission(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all missions", ex);
        }
    }
}
