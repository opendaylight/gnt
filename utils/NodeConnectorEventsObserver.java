/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

 package org.opendaylight.gnt.utils.portspeaker;


import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder;

/**
 * NodeConnectorEventsObserver can be added to NodeConnectorInventoryEventTranslator to receive events
 * when node connector added or removed.
 */
public interface NodeConnectorEventsObserver {
    /**
     * This method is called when new node connector is added to inventory or when existing
     * node connector changed it's status to UP. This method can be called multiple times for
     * the same creation event.
     *
     * @param nodeConnectorInstanceId Object that uniquely identify added node connector
     * @param flowConnector object containing almost all of details about node connector
     */
    void nodeConnectorAdded(InstanceIdentifier<NodeConnector> nodeConnectorInstanceId,
                                   FlowCapableNodeConnector flowConnector);

    /**
     * This method is called when some node connector is removed from inventory or when existing
     * node connector changed it's status to DOWN. This method can be called multiple times for
     * the same removal event.
     * @param nodeConnectorInstanceId Object that uniquely identify added node connector
     */
    void nodeConnectorRemoved(InstanceIdentifier<NodeConnector> nodeConnectorInstanceId);

	boolean isPortDown(String dpid,Long ofport);
	void addPort(String dpid,PortBuilder portBuilder,Long ofport);
	void delPort(String dpid,PortBuilder portBuilder,Long ofport);
	void addL2Group(String dpid,String ofport,String groupid,Long vni);
	void delL2Group(String dpid,String ofport,String groupid);
	void createL2InterfaceGroupVxlan(Long dpid,String groupName,Long groupid,String segmentationId, Long ofPort);
	void createL2InterfaceGroupUntagged(Long dpid,String groupName,Long groupid,Long ofPort);
	void createL2InterfaceGroupTagged(Long dpid,String groupName,Long groupid,Long ofPort); 
	void destroyGroup(Long dpid,String groupName,Long groupid,Long ofport);

   /*
	void createL2VxlanAll(Long dpid,String groupName,Long groupid,String segmentationId, Long ofPort,String macaddr);
	void createL2UntaggedAll(Long dpid,String groupName,Long groupid,String segmentationId,Long ofPort,String macaddr);
	void createL3VxlanAll(Long dpid,String groupName,Long l2groupid,String tunnelkey,Long ofPort,Long l3groupid,String srcaddr,String desaddr,String dip,Long metadata);
    */	
}
