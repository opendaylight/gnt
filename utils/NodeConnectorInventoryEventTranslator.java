/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.gnt.utils.portspeaker;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder;

/**
 * NodeConnectorInventoryEventTranslator is listening for changes in inventory operational DOM tree
 * and update LLDPSpeaker and topology.
 */
public class NodeConnectorInventoryEventTranslator<T extends DataObject>
        implements DataTreeChangeListener<T>, AutoCloseable {

    private static final InstanceIdentifier<State> II_TO_STATE
        = InstanceIdentifier.builder(Nodes.class)
            .child(Node.class)
            .child(NodeConnector.class)
            .augmentation(FlowCapableNodeConnector.class)
            .child(State.class)
            .build();

    private static final InstanceIdentifier<FlowCapableNodeConnector> II_TO_FLOW_CAPABLE_NODE_CONNECTOR
        = InstanceIdentifier.builder(Nodes.class)
            .child(Node.class)
            .child(NodeConnector.class)
            .augmentation(FlowCapableNodeConnector.class)
            .build();

    private static final long STARTUP_LOOP_TICK = 500L;
    private static final int STARTUP_LOOP_MAX_RETRIES = 8;
    private static final Logger LOG = LoggerFactory.getLogger(NodeConnectorInventoryEventTranslator.class);

    private final ListenerRegistration<DataTreeChangeListener> listenerOnPortRegistration;
    private final ListenerRegistration<DataTreeChangeListener> listenerOnPortStateRegistration;
    private final Set<NodeConnectorEventsObserver> observers = Sets.newCopyOnWriteArraySet();
    private final Map<InstanceIdentifier<?>,FlowCapableNodeConnector> iiToDownFlowCapableNodeConnectors = new HashMap<>();

 
	
    public NodeConnectorInventoryEventTranslator(DataBroker dataBroker,NodeConnectorEventsObserver...observers1) {
        //this.observers = ImmutableSet.copyOf(observers);
        LOG.info("1111NodeConnectorInventoryEventTranslator111");
        LLDPSpeaker lLDPSpeaker = new LLDPSpeaker(dataBroker);
        observers.add(lLDPSpeaker);
        final DataTreeIdentifier<T> dtiToNodeConnector =
                new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, II_TO_FLOW_CAPABLE_NODE_CONNECTOR);
        final DataTreeIdentifier<T> dtiToNodeConnectorState =
                new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, II_TO_STATE);
        final SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
        try {
            listenerOnPortRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<DataTreeChangeListener>>() {
                @Override
                public ListenerRegistration<DataTreeChangeListener> call() throws Exception {
                    return dataBroker.registerDataTreeChangeListener(dtiToNodeConnector, NodeConnectorInventoryEventTranslator.this);
                }
            });
            listenerOnPortStateRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<DataTreeChangeListener>>() {
                @Override
                public ListenerRegistration<DataTreeChangeListener> call() throws Exception {
                    return dataBroker.registerDataTreeChangeListener(dtiToNodeConnectorState, NodeConnectorInventoryEventTranslator.this);
                }
            });
        } catch (Exception e) {
            LOG.error("DataTreeChangeListeners registration failed: {}", e);
            throw new IllegalStateException("NodeConnectorInventoryEventTranslator startup failed!", e);
        }
        LOG.info("NodeConnectorInventoryEventTranslator has started.");
    }

	

    @Override
    public void close() {
        if (listenerOnPortRegistration != null) {
            listenerOnPortRegistration.close();
        }
        if (listenerOnPortStateRegistration != null) {
            listenerOnPortStateRegistration.close();
        }
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<T>> modifications) {
        for(DataTreeModification modification : modifications) {
            LOG.info("Nodeconnectors in inventory changed -> {}", modification.getRootNode().getModificationType());
            switch (modification.getRootNode().getModificationType()) {
                case WRITE:
                    processAddedConnector(modification);
                    break;
                case SUBTREE_MODIFIED:
                    processUpdatedConnector(modification);
                    break;
                case DELETE:
                    processRemovedConnector(modification);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type: {}" +
                            modification.getRootNode().getModificationType());
            }
        }
    }

    private void processAddedConnector(final DataTreeModification<T> modification) {

		LOG.info("----------------processAddedConnector------------------");
        final InstanceIdentifier<T> identifier = modification.getRootPath().getRootIdentifier();
        InstanceIdentifier<NodeConnector> nodeConnectorInstanceId =identifier.firstIdentifierOf(NodeConnector.class);
        if (compareIITail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            FlowCapableNodeConnector flowConnector = (FlowCapableNodeConnector) modification.getRootNode().getDataAfter();
            if (!isPortDown(flowConnector)) {
				LOG.info("----------------processAddedConnector add and port is up------------------");
                notifyNodeConnectorAppeared(nodeConnectorInstanceId, flowConnector);
            } else {
                iiToDownFlowCapableNodeConnectors.put(nodeConnectorInstanceId, flowConnector);
            }
        }
    }

    private void processUpdatedConnector(final DataTreeModification<T> modification) {
        final InstanceIdentifier<T> identifier = modification.getRootPath().getRootIdentifier();
        InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = identifier.firstIdentifierOf(NodeConnector.class);
        if (compareIITail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            FlowCapableNodeConnector flowConnector = (FlowCapableNodeConnector) modification.getRootNode().getDataAfter();
            if (isPortDown(flowConnector)) {
                notifyNodeConnectorDisappeared(nodeConnectorInstanceId);
				LOG.info("----------------processUpdatedConnector1 add and port is down------------------");
            } else {
                
                notifyNodeConnectorAppeared(nodeConnectorInstanceId, flowConnector);
            }
        } else if (compareIITail(identifier, II_TO_STATE)) {
            FlowCapableNodeConnector flowNodeConnector = iiToDownFlowCapableNodeConnectors.get(nodeConnectorInstanceId);
            if (flowNodeConnector != null) {
                State state = (State) modification.getRootNode().getDataAfter();
                if (!state.isLinkDown()) {
					LOG.info("----------------processUpdatedConnector2 add and port is down------------------");
                    FlowCapableNodeConnectorBuilder flowCapableNodeConnectorBuilder =
                            new FlowCapableNodeConnectorBuilder(flowNodeConnector);
                    flowCapableNodeConnectorBuilder.setState(state);
                    notifyNodeConnectorAppeared(nodeConnectorInstanceId, flowCapableNodeConnectorBuilder.build());
                    iiToDownFlowCapableNodeConnectors.remove(nodeConnectorInstanceId);
                }
            }
        }
    }

    private void processRemovedConnector(final DataTreeModification<T> modification) {
        final InstanceIdentifier<T> identifier = modification.getRootPath().getRootIdentifier();
		LOG.info("----------------processRemovedConnector port------------------");
        if (compareIITail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = identifier.firstIdentifierOf(NodeConnector.class);
            notifyNodeConnectorDisappeared(nodeConnectorInstanceId);
        }
    }

    private boolean compareIITail(final InstanceIdentifier<?> ii1, final InstanceIdentifier<?> ii2) {
        return Iterables.getLast(ii1.getPathArguments()).equals(Iterables.getLast(ii2.getPathArguments()));
    }

    private static boolean isPortDown(final FlowCapableNodeConnector flowCapableNodeConnector) {
        PortState portState = flowCapableNodeConnector.getState();
        PortConfig portConfig = flowCapableNodeConnector.getConfiguration();
        return portState != null && portState.isLinkDown()
                || portConfig != null && portConfig.isPORTDOWN();
    }

    private void notifyNodeConnectorAppeared(final InstanceIdentifier<NodeConnector> nodeConnectorInstanceId,
                                             final FlowCapableNodeConnector flowConnector) {
        LOG.info("111notifyNodeConnectorAppeared111");
                                             
        for (NodeConnectorEventsObserver observer : observers) {
			LOG.info("222notifyNodeConnectorAppeared222");
            observer.nodeConnectorAdded(nodeConnectorInstanceId, flowConnector);
        }
    }

    private void notifyNodeConnectorDisappeared(final InstanceIdentifier<NodeConnector> nodeConnectorInstanceId) {
        for (NodeConnectorEventsObserver observer : observers) {
            observer.nodeConnectorRemoved(nodeConnectorInstanceId);
        }
    }

   // dpid ofport is down or up...
   public boolean isPortDown(String dpid,Long ofport) {
        for (NodeConnectorEventsObserver observer : observers) {
			return observer.isPortDown(dpid,ofport);
			
        }

		return false;
	   
    }

   public void addL2Group(String dpid,String ofport,String groupid,Long vni) {

		  for (NodeConnectorEventsObserver observer : observers) {
               observer.addL2Group(dpid,ofport,groupid,vni);			  			
          }

		  return ;

   }	

   public void delL2Group(String dpid,String ofport,String groupid) {

		  for (NodeConnectorEventsObserver observer : observers) {
               observer.delL2Group(dpid,ofport,groupid);			  			
          }

		  return ;

   }	
   

   public void addPort(String dpid,PortBuilder portBuilder,Long ofport) {

		for (NodeConnectorEventsObserver observer : observers) {
			 observer.addPort(dpid,portBuilder,ofport);
			 return ;
			
        }

		return ;
	   
    }
   
   public void delPort(String dpid,PortBuilder portBuilder,Long ofport) {

		for (NodeConnectorEventsObserver observer : observers) {
			 observer.delPort(dpid,portBuilder,ofport);
			 return ;
			
        }

		return ;
	   
    }

   

	
   public  void createL2InterfaceGroupVxlan(Long dpid,String groupName,Long groupid,String segmentationId, Long ofPort) {

		 for (NodeConnectorEventsObserver observer : observers) {
			 observer.createL2InterfaceGroupVxlan(dpid,groupName,groupid,segmentationId,ofPort);
			 return ;
			
         } 

         return ;
		 

   }

   public void createL2InterfaceGroupUntagged(Long dpid,String groupName,Long groupid,Long ofPort) {

         for (NodeConnectorEventsObserver observer : observers) {
			 observer.createL2InterfaceGroupUntagged(dpid,groupName,groupid,ofPort);
			 return ;
			
         } 

         return ; 

   }

    /*

    public  void createL2VxlanAll(Long dpid,String groupName,Long groupid,String segmentationId, Long ofPort,String macaddr) {

		 for (NodeConnectorEventsObserver observer : observers) {
			 observer.createL2VxlanAll(dpid,groupName,groupid,segmentationId,ofPort,macaddr);
			 return ;
			
         } 

         return ;
		 

   }

   public void createL2UntaggedAll(Long dpid,String groupName,Long groupid,String segmentationId,Long ofPort,String macaddr) {

         for (NodeConnectorEventsObserver observer : observers) {
			 observer.createL2UntaggedAll(dpid,groupName,groupid,segmentationId,ofPort,macaddr);
			 return ;
			
         } 

         return ;



   }

 */

   public void createL2InterfaceGroupTagged(Long dpid,String groupName,Long groupid,Long ofPort) {

        for (NodeConnectorEventsObserver observer : observers) {
			 observer.createL2InterfaceGroupTagged(dpid,groupName,groupid,ofPort);
			 return ;
			
         } 

         return ;
    }

    public void destroyGroup(Long dpid,String groupName,Long groupid,Long ofport){

		 for (NodeConnectorEventsObserver observer : observers) {
			 observer.destroyGroup(dpid,groupName,groupid,ofport);
			 return ;
			
         } 

         return ;



    }	
	

}
