/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168;

import java.util.List;

/**
 *
 * @author Matej Sojak 433294
 */
public interface AgentManager {

    void createAgent(Agent agent) throws ServiceFailureException;

    void updateAgent(Agent agent) throws ServiceFailureException;

    void deleteAgent(Agent agent) throws ServiceFailureException;

    List<Agent> findAllAgents() throws ServiceFailureException;

    Agent getAgent(Long id) throws ServiceFailureException;
}
