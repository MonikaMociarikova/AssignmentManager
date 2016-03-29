package cz.muni.fi.pv168;

import cz.muni.fi.pv168.common.ServiceFailureException;

import java.util.List;

/**
 * Created by MM on 14-Mar-16.
 * Interface for mission manager
 */
public interface MissionManager {

    /**
     *
     * @param mission
     * @throws ServiceFailureException
     */
    void createMission(Mission mission) throws ServiceFailureException;

    void updateMission(Mission mission) throws ServiceFailureException;

    void deleteMission(Mission mission) throws ServiceFailureException;

    Mission getMission(Long id) throws ServiceFailureException;

    List<Mission> findAllMissions() throws ServiceFailureException;

    List<Mission> findUncompletedMissions() throws ServiceFailureException;

    List<Mission> findMissionsByPlace(String place) throws ServiceFailureException;
}
