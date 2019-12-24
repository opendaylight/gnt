/*
 * Copyright (c) 2017 CMCC, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.gnt.netvirt.gntimpl;

import java.util.List;
import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.*;

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.network.attributes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.network.attributes.networks.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.subnets.*;

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.*;
import java.lang.Long;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.network.attributes.networks.Network;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.subnets.Subnet;

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.subnets.SubnetKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.network.attributes.networks.NetworkKey;
import org.opendaylight.gnt.netvirt.translator.*;

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.*;


/**
 * Utility class to wrap southbound transactions.
 *
 * @author cmcc
 */
public interface GntOp {
       String getdpid(Node node);
	   String getdpid(String ipaddr);
	   List<Node> readNodes();
	   List<Node> readSwitchNodes();	   
	   List<Node> readOvsNodes();
	   List<Node> readVrfNodes();
	   String getTypeNode(Node node);
	   void addtunnel(Node node);
	   void deltunnel(Node node);
	   boolean addRouter(NeutronRouter neutronRouter);
	   boolean delRouter(NeutronRouter neutronRouter);
	   boolean delRouter(String routeruuid);
	   boolean updatedRouter(NeutronRouter neutronRouter);
	   String getMacaddr(String subnetuuid);
	   boolean setMinVni(Long min);
	   Long getMaxVni();
	   String getVni();
	   Node getNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   boolean addRouterInterface(NeutronPort neutronPort);
	   boolean delRouterInterface(NeutronPort neutronPort);
	   boolean networkIsTheNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node node);
	   boolean routerIsTheNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node node) ;
	   Long getOfPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node nodeL);
	   Long getOfPort(Node node1,Node node2);
	   String getMacaddr(Node node);
	   void writeMacaddr(Node node);
	   String getSwtichType(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   boolean portIsTheNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node node);
	   long getDataPathId(Node node);
	   Long getSymmetryVni(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   Long getNoSymmetryVni(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   String getPortMacaddr(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   Long getofPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   long getDpid(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   String getSwitchType(Node node);
	   String getMacaddr(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);

	   List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> getSameNetworkPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type);
	   List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> getSameRouterPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type);
       
	   List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> getSubnetPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets subnet);
	   
	   Long getOfPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port1,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port2);
	   boolean isSameTheNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port1,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port2);
	   String getGatewayMac(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   String getIp(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port); 
	   boolean isLastNetworkPort(Node node,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) ;
	   boolean isLastrouterofNode(Node node,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   boolean isLastSubnetPort(Node node, org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   boolean isSameTheNode(Node node,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
       Long getOfGWPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   String getRouterUUID(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   String getMacaddrArp(String ip);

	   Node getNode(String ipaddr);
	   org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port getPortFromUUID(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   //boolean addNovaPort(NeutronPort neutronPort); 

	   boolean isLastrouterofNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type);
	   boolean  isLastNetworkPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type);

	   List<String> getSecurityGroup(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);

	   Long getOfPort(Node nodeL,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);

	   boolean isSameTheNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node node);

	   List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> getSameRouterPort(Router router,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type);


	   boolean isLastrouterofNode(Router router,Node node,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
	   boolean isLastrouterofNode(Router router,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type); 
	   Long getOfGWPort(Node node);

	   String getGatewayMac(Router router,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port);
}



