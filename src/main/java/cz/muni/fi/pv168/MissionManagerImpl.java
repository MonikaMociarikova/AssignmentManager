package cz.muni.fi.pv168;

import java.util.List;

/**
 * Created by MM on 14-Mar-16.
 */
public class MissionManagerImpl implements MissionManager {
    @Override
    public void createMission(Mission mission) throws ServiceFailureException {

    }

    @Override
    public void updateMission(Mission mission) throws ServiceFailureException {

    }

    @Override
    public void deleteMission(Mission mission) throws ServiceFailureException {

    }

    @Override
    public Mission findMissionById(Long id) throws ServiceFailureException {
        return null;
    }

    @Override
    public List<Mission> findAllMissions() throws ServiceFailureException {
        return null;
    }

    @Override
    public List<Mission> findUncompletedMissions() throws ServiceFailureException {
        return null;
    }

    @Override
    public List<Mission> findMissionsByPlace(String place) throws ServiceFailureException {
        return null;
    }
}
