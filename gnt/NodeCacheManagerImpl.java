/*
 * Copyright (c) 2015 CMCC, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.gnt.netvirt.gntimpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.gnt.netvirt.AbstractEvent;
import org.opendaylight.gnt.netvirt.AbstractHandler;
import org.opendaylight.gnt.netvirt.ConfigInterface;
import org.opendaylight.gnt.netvirt.api.Action;
import org.opendaylight.gnt.netvirt.api.EventDispatcher;
import org.opendaylight.netvirt.utils.servicehelper.ServiceHelper;

import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.*;

/**
 * @author cmcc
 */
public class NodeCacheManagerImpl extends AbstractHandler implements NodeCacheManager, ConfigInterface {
    private static final Logger LOG = LoggerFactory.getLogger(NodeCacheManagerImpl.class);
    private Map<String, Node> nodeCache = new ConcurrentHashMap<>();
    private Map<Long, NodeCacheListener> handlers = Maps.newHashMap();
    private volatile GntOp gntop;
    @Override
    public void nodeAdded(Node node) {
        LOG.info("nodeAdded: {}", node);
        enqueueEvent(new NodeCacheManagerEvent(node, Action.UPDATE));
    }

    @Override
    public void nodeRemoved(Node node) {
        LOG.info("nodeRemoved: {}", node);
        enqueueEvent(new NodeCacheManagerEvent(node, Action.DELETE));
    }


    @Override
    public Node getNode(String nodeId) {
        Node node = nodeCache.get(nodeId);
        return node;
    }

    private void processNodeUpdate(Node node) {
        Action action = Action.UPDATE;

        String dpid = gntop.getdpid(node);//node.getNodeId();
        if (nodeCache.get(dpid) == null) {
            action = Action.ADD;
        } else {
             
        }
        nodeCache.put(dpid, node);

        LOG.info("--nodecachenode dpid  is {},node is {}-------",dpid,node);

        for (NodeCacheListener handler : handlers.values()) {
            try {
                handler.notifyNode(node, action);
            } catch (Exception e) {
                LOG.error("Failed notifying node add event", e);
            }
        }
        LOG.info("processNodeUpdate: Done processing");
    }

    private void processNodeRemoved(Node node) {
        nodeCache.remove(gntop.getdpid(node));
        for (NodeCacheListener handler : handlers.values()) {
            try {
                handler.notifyNode(node, Action.DELETE);
            } catch (Exception e) {
                LOG.error("Failed notifying node remove event", e);
            }
        }
        LOG.info("processNodeRemoved: Done processing");
    }

    /**
     * Process the event.
     *
     * @param abstractEvent the {@link AbstractEvent} event to be handled.
     * @see EventDispatcher
     */
    @Override
    public void processEvent(AbstractEvent abstractEvent) {
        if (!(abstractEvent instanceof NodeCacheManagerEvent)) {
            LOG.error("Unable to process abstract event {}", abstractEvent);
            return;
        }
        NodeCacheManagerEvent ev = (NodeCacheManagerEvent) abstractEvent;
        LOG.trace("NodeCacheManagerImpl: dequeue Event: {}", ev.getAction());
        switch (ev.getAction()) {
            case DELETE:
                processNodeRemoved(ev.getNode());
                break;
            case UPDATE:
                processNodeUpdate(ev.getNode());
                break;
            default:
                LOG.warn("Unable to process event action {}", ev.getAction());
                break;
        }
    }

    public void cacheListenerAdded(final ServiceReference ref, NodeCacheListener handler){
        Long pid = (Long) ref.getProperty(org.osgi.framework.Constants.SERVICE_ID);
        handlers.put(pid, handler);
        LOG.info("Node cache listener registered, pid : {} handler : {}", pid,
                handler.getClass().getName());
    }

    public void cacheListenerRemoved(final ServiceReference ref){
        Long pid = (Long) ref.getProperty(org.osgi.framework.Constants.SERVICE_ID);
        handlers.remove(pid);
        LOG.debug("Node cache listener unregistered, pid {}", pid);
    }
 
    @Override
    public List<Node> getNodes() {
        LOG.info("-------getNodes1111111111111,nodeCache is {}-------------\n",nodeCache);
        List<Node> nodes = Lists.newArrayList();
        for (Node node : nodeCache.values()) {
		    LOG.info("getNodes111 node is {}\n",node);
            nodes.add(node);
        }
		LOG.info("getNodes222 nodes is {}\n",nodes);
        return nodes;
    }

    private void populateNodeCache() {
        LOG.debug("populateNodeCache : Populating the node cache");
        List<Node> nodes = gntop.readNodes();
        for(Node node : nodes) {
            this.nodeCache.put(gntop.getdpid(node), node);
        }
        LOG.info("populateNodeCache : Node cache population is done. Total nodes : {}",this.nodeCache.size());
    }

    @Override
    public void setDependencies(ServiceReference serviceReference) {
        gntop =
                (GntOp) ServiceHelper.getGlobalInstance(GntOp.class, this);
        eventDispatcher =
                (EventDispatcher) ServiceHelper.getGlobalInstance(EventDispatcher.class, this);
        eventDispatcher.eventHandlerAdded(serviceReference, this);
        populateNodeCache();
    }

    @Override
    public void setDependencies(Object impl) {}
}

