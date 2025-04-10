package org.asteriskjava.live.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.asteriskjava.live.AgentState;
import org.asteriskjava.live.AsteriskAgent;
import org.asteriskjava.live.ManagerCommunicationException;
import org.asteriskjava.manager.ResponseEvents;
import org.asteriskjava.manager.action.AgentsAction;
import org.asteriskjava.manager.event.AgentCallbackLoginEvent;
import org.asteriskjava.manager.event.AgentCallbackLogoffEvent;
import org.asteriskjava.manager.event.AgentCalledEvent;
import org.asteriskjava.manager.event.AgentCompleteEvent;
import org.asteriskjava.manager.event.AgentConnectEvent;
import org.asteriskjava.manager.event.AgentLoginEvent;
import org.asteriskjava.manager.event.AgentLogoffEvent;
import org.asteriskjava.manager.event.AgentsEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.util.Log;
import org.asteriskjava.util.LogFactory;

/**
 * Manages all events related to agents on Asterisk server. For correct work
 * ensure enabled AgentCalledEvents. You have to set
 * <code>eventwhencalled = yes</code> in <code>queues.conf</code>.
 *
 * @author <a href="mailto:patrick.breucking{@nospam}gonicus.de">Patrick
 *         Breucking</a>
 * @version $Id: AgentManager.java 1184 2008-10-24 00:13:47Z srt $
 * @since 0.3.1
 */
public class AgentManager {

    private final Log logger = LogFactory.getLog(this.getClass());

    private final AsteriskServerImpl server;

    /**
     * A Map of agents by thier agentId.
     */
    private final Map<String, AsteriskAgentImpl> agents;

    /**
     * A Map of agent in state RINGING by the caller id. Needed to return agent
     * into idle state, if call was not conneted.
     */
    private final Map<String, AsteriskAgentImpl> ringingAgents;

    AgentManager(AsteriskServerImpl asteriskServerImpl) {
        this.server = asteriskServerImpl;
        agents = new HashMap<String, AsteriskAgentImpl>();
        ringingAgents = new HashMap<String, AsteriskAgentImpl>();
    }

    /**
     * Retrieves all agents registered at Asterisk server by sending an
     * AgentsAction.
     *
     * @throws ManagerCommunicationException if communication with Asterisk
     *                                       server fails.
     */
    void initialize() throws ManagerCommunicationException {
        ResponseEvents re;
        re = server.sendEventGeneratingAction(new AgentsAction());
        for (ManagerEvent event : re.getEvents()) {
            if (event instanceof AgentsEvent) {
                System.out.println(event);
                handleAgentsEvent((AgentsEvent) event);
            }
        }
    }

    void disconnected() {
        synchronized (agents) {
            agents.clear();
        }
    }

    /**
     * On AgentsEvent create a new Agent.
     *
     * @param event generated by Asterisk server.
     */
    void handleAgentsEvent(AgentsEvent event) {
        AsteriskAgentImpl agent = new AsteriskAgentImpl(server, event.getName(), "Agent/" + event.getAgent(), AgentState.valueOf(event.getStatus()));
        logger.info("Adding agent " + agent.getName() + "(" + agent.getAgentId() + ")");
        addAgent(agent);
    }

    /**
     * Add a new agent to the manager.
     *
     * @param agent agent to add.
     */
    private void addAgent(AsteriskAgentImpl agent) {
        synchronized (agents) {
            agents.put(agent.getAgentId(), agent);
        }
        server.fireNewAgent(agent);
    }

    /**
     * Return the requested agent.
     *
     * @param agentId identifier for agent
     * @return the requested agent
     */
    AsteriskAgentImpl getAgentByAgentId(String agentId) {
        synchronized (agents) {
            return agents.get(agentId);
        }
    }

    /**
     * Update state if agent was called.
     *
     * @param event
     */
    void handleAgentCalledEvent(AgentCalledEvent event) {
        AsteriskAgentImpl agent = getAgentByAgentId(event.getAgentCalled());
        if (agent == null) {
            logger.error("Ignored AgentCalledEvent for unknown agent " + event.getAgentCalled());
            return;
        }
        updateRingingAgents(event.getChannelCalling(), agent);
        updateAgentState(agent, AgentState.AGENT_RINGING);
    }

    /**
     * Set state of agent.
     *
     * @param agent
     */
    private void updateAgentState(AsteriskAgentImpl agent, AgentState newState) {
        logger.info("Set state of agent " + agent.getAgentId() + " to " + newState);
        synchronized (agent) {
            agent.updateState(newState);
        }
    }

    /**
     * Updates state of agent, if the call in a queue was redirect to the next
     * agent because the ringed agent doesn't answer the call. After reset
     * state, put the next agent in charge.
     *
     * @param channelCalling
     * @param agent
     */
    private void updateRingingAgents(String channelCalling, AsteriskAgentImpl agent) {
        synchronized (ringingAgents) {
            if (ringingAgents.containsKey(channelCalling)) {
                updateAgentState(ringingAgents.get(channelCalling), AgentState.AGENT_IDLE);
            }
            ringingAgents.put(channelCalling, agent);
        }
    }

    /**
     * Update state if agent was connected to channel.
     *
     * @param event
     */
    void handleAgentConnectEvent(AgentConnectEvent event) {
        AsteriskAgentImpl agent = getAgentByAgentId(event.getChannel());
        if (agent == null) {
            logger.error("Ignored AgentConnectEvent for unknown agent " + event.getChannel());
            return;
        }
        agent.updateState(AgentState.AGENT_ONCALL);
    }

    /**
     * Change state if agent logs in.
     *
     * @param event
     */
    void handleAgentLoginEvent(AgentLoginEvent event) {
        AsteriskAgentImpl agent = getAgentByAgentId("Agent/" + event.getAgent());
        if (agent == null) {
            synchronized (agents) {
                logger.error("Ignored AgentLoginEvent for unknown agent " + event.getAgent() + ". Agents: " + agents.values().toString());
            }
            return;
        }
        agent.updateState(AgentState.AGENT_IDLE);
    }

    /**
     * Change state if agent logs out.
     *
     * @param event
     */
    void handleAgentLogoffEvent(AgentLogoffEvent event) {
        AsteriskAgentImpl agent = getAgentByAgentId("Agent/" + event.getAgent());
        if (agent == null) {
            logger.error("Ignored AgentLogoffEvent for unknown agent " + event.getAgent() + ". Agents: " + agents.values().toString());
            return;
        }
        agent.updateState(AgentState.AGENT_LOGGEDOFF);
    }

    /**
     * Change state if agent logs in.
     *
     * @param event
     */
    void handleAgentCallbackLoginEvent(AgentCallbackLoginEvent event) {
        AsteriskAgentImpl agent = getAgentByAgentId("Agent/" + event.getAgent());
        if (agent == null) {
            synchronized (agents) {
                logger.error("Ignored AgentCallbackLoginEvent for unknown agent " + event.getAgent() + ". Agents: " + agents.values().toString());
            }
            return;
        }
        agent.updateState(AgentState.AGENT_IDLE);
    }

    /**
     * Change state if agent logs out.
     *
     * @param event
     */
    void handleAgentCallbackLogoffEvent(AgentCallbackLogoffEvent event) {
        AsteriskAgentImpl agent = getAgentByAgentId("Agent/" + event.getAgent());
        if (agent == null) {
            logger.error("Ignored AgentCallbackLogoffEvent for unknown agent " + event.getAgent() + ". Agents: " + agents.values().toString());
            return;
        }
        agent.updateState(AgentState.AGENT_LOGGEDOFF);
    }

    /**
     * Return all agents registered at Asterisk server.
     *
     * @return a collection of all agents.
     */
    Collection<AsteriskAgent> getAgents() {
        Collection<AsteriskAgent> copy;
        synchronized (agents) {
            copy = new ArrayList<AsteriskAgent>(agents.values());
        }
        return copy;
    }

    /**
     * Change state if connected call was terminated.
     *
     * @param event
     */
    void handleAgentCompleteEvent(AgentCompleteEvent event) {
        AsteriskAgentImpl agent = getAgentByAgentId(event.getChannel());
        if (agent == null) {
            logger.error("Ignored AgentCompleteEvent for unknown agent " + event.getChannel());
            return;
        }
        agent.updateState(AgentState.AGENT_IDLE);
    }
}
