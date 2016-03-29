package cz.muni.fi.pv168;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Created by MONNY on 29-Mar-16.
 */
public class AgentBuilder {

    private Long id;
    private String name;
    private LocalDate born;

    public AgentBuilder id(Long id){
        this.id = id;
        return this;
    }

    public AgentBuilder name(String name){
        this.name = name;
        return this;
    }

    public AgentBuilder born(LocalDate born){
        this.born = born;
        return this;
    }

    public Agent build() {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setName(name);
        agent.setBorn(born);
        return agent;
    }

}
