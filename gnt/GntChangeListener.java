/*
 * Copyright (c) 2017 cmcc  Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
 
package org.opendaylight.gnt.netvirt.gntimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Map;

//logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.controller.md.sal.binding.api.ClusteredDataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.*;

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.network.attributes.*;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.network.attributes.networks.*;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.subnets.*;

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.*;
import java.lang.Long;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.network.attributes.networks.Network;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.subnets.Subnet;

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.subnets.SubnetKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.network.attributes.networks.NetworkKey;


import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;

import org.opendaylight.gnt.netvirt.api.Action;

import com.google.common.collect.ImmutableBiMap;
import java.util.Set;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.*;

public class GntChangeListener implements ClusteredDataChangeListener, AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(GntChangeListener.class);
    private DataBroker dataService;
	private ListenerRegistration<DataChangeListener> listenerReg;

	private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
        .setNameFormat("NV-OvsdbDCL-%d").build();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1, threadFactory);
    public GntChangeListener(DataBroker db){
        this.dataService= db;
    }


	public InstanceIdentifier<Node> createInstanceIdentifierNodes() {
        return InstanceIdentifier.create(Topo.class)
                .child(Nodes.class)
                .child(Node.class);
    }

	public InstanceIdentifier<Port> createInstanceIdentifierPorts() {
        return InstanceIdentifier.create(Topo.class)
                .child(Nodes.class)
                .child(Node.class)
                .child(Ports.class)
                .child(Port.class);
    }

   public InstanceIdentifier<Router> createInstanceIdentifierRoutes() {
        return InstanceIdentifier.create(Topo.class)
                .child(Routers.class)
                .child(Router.class);
    }

   public InstanceIdentifier<Topo> createInstanceIdentifierTopo() {
        return InstanceIdentifier.create(Topo.class);
   }
   
   public InstanceIdentifier<Subnets> createInstanceIdentifierRouteInterfaces() {
        return InstanceIdentifier.create(Topo.class)
                .child(Routers.class)
                .child(Router.class)
                .child(Subnets.class);
    }
	
    public void start() {
        InstanceIdentifier<Topo> path= createInstanceIdentifierTopo();
        listenerReg = dataService.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, path, this, DataChangeScope.SUBTREE);
        LOG.info("-------GntChangeListener-----start-------------");
    }
	
    @Override
    public void onDataChanged(
            final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        LOG.info("GntChangeListener onDataChanged: {}", changes);
        processNodeCreation(changes);
		processPortCreation(changes);
		processPortUpdate(changes);
		processPortDelete(changes);
	    processRoutersCreation(changes);
		processRouterInterfacesCreation(changes);
		processRouterInterfacesUpdate(changes);
		processRouterInterfacesDelete(changes);
		processRoutersUpdate(changes);
		processRoutersDelete(changes);
		processNodeUpdate(changes);
		processNodeDelete(changes);
		
    }    



	private void processRoutersCreation(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {        
        for (Entry<InstanceIdentifier<?>, DataObject> newRpc : changes.getCreatedData().entrySet()) {
            LOG.info("processCreation, createdEntry: {}", newRpc);
            if(newRpc.getKey().getTargetType().equals(Router.class)){
			    Router router  = (Router)newRpc.getValue();
				LOG.info("processRouterCreation,Router is {}",router);
				RouterUpdate(router,router,GntInventoryListener.GntType.router,Action.ADD);
            }
        }
    }


	private void processRoutersUpdate(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {

        for (Map.Entry<InstanceIdentifier<?>, DataObject> newRpc : changes.getUpdatedData().entrySet()){
            if (newRpc.getKey().getTargetType().equals(Router.class)) {
			      Router router  = (Router)newRpc.getValue();
				  LOG.info("processRouterUpdate,Router is {}",router);
				  RouterUpdate(router,router,GntInventoryListener.GntType.router,Action.UPDATE);
            	}
        	}
	 }
	 

	 private void processRoutersDelete(
		   AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {		 
	   for (InstanceIdentifier<?> newRpc : changes.getRemovedPaths()) {
		   //LOG.info("processPortDelete, deleteEntry: {}", newRpc);
		   if(newRpc.getTargetType().equals(Router.class)){ 
			   Router router = getDataChanges(changes.getOriginalData(),(InstanceIdentifier<Router>)newRpc);
			   RouterUpdate(router,router,GntInventoryListener.GntType.router,Action.DELETE);
		   }
	   }
    }

    private void processRouterInterfacesCreation(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {        
        for (Entry<InstanceIdentifier<?>, DataObject> newRpc : changes.getCreatedData().entrySet()) {
            LOG.info("processNodeCreation, createdEntry: {}", newRpc);
            if(newRpc.getKey().getTargetType().equals(Subnets.class)){
			    Router router_new  = getRouter(changes.getCreatedData(),newRpc);
                Subnets interface_new = (Subnets)newRpc.getValue();
				LOG.info("processRouterInterfaceCreation,routerinterface is {}",interface_new);
				RouterUpdate(router_new,interface_new,GntInventoryListener.GntType.routerinterface,Action.ADD);
            }
        }
    }


	private void processRouterInterfacesUpdate(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {

        for (Map.Entry<InstanceIdentifier<?>, DataObject> newRpc : changes.getUpdatedData().entrySet()){
            if (newRpc.getKey().getTargetType().equals(Subnets.class)){
				  Router router_new  = getRouter(changes.getCreatedData(),newRpc);
                  Subnets interface_new = (Subnets)newRpc.getValue();
				  LOG.info("processRouterInterfaceUpdated,routerinterface is {}",interface_new);
				  RouterUpdate(router_new,interface_new,GntInventoryListener.GntType.routerinterface,Action.UPDATE);
            	}
        	}
	 }
	 

	 private void processRouterInterfacesDelete(
		   AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {		 
	   for (InstanceIdentifier<?> newRpc : changes.getRemovedPaths()) {
		   //LOG.info("processPortDelete, deleteEntry: {}", newRpc);
		   if(newRpc.getTargetType().equals(Subnets.class)){
			   //Port port_new = (Port)newRpc;
			   Router router_new  = getRouter(changes.getOriginalData(),newRpc);
			   Subnets interface_new = getDataChanges(changes.getOriginalData(),(InstanceIdentifier<Subnets>)newRpc);
			   LOG.info("processRouterInterfacesDelete,routerinterface is {}",interface_new);
			   RouterUpdate(router_new,interface_new,GntInventoryListener.GntType.routerinterface,Action.DELETE);
		   }
	   }
    }

	 
    private void processPortCreation(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {        
        for (Entry<InstanceIdentifier<?>, DataObject> newRpc : changes.getCreatedData().entrySet()) {
            LOG.info("processNodeCreation, createdEntry: {}", newRpc);
            if(newRpc.getKey().getTargetType().equals(Port.class)){
			    Node p_node  = getNode(changes.getCreatedData(),newRpc);
                Port port_new = (Port)newRpc.getValue();
			//	LOG.info("processPortCreation,Port is {}",port_new);
				NodeUpdate(p_node,port_new,GntInventoryListener.GntType.port,Action.ADD);
            }
        }
    }


	private void processPortUpdate(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {

        for (Map.Entry<InstanceIdentifier<?>, DataObject> newRpc : changes.getUpdatedData().entrySet()){
            if (newRpc.getKey().getTargetType().equals(Port.class)){
				  Node p_node  = getNode(changes.getCreatedData(),newRpc);
                  Port port_new = (Port)newRpc.getValue();
              //    LOG.info("processPortUpdate: <{}>", port_new);
                  NodeUpdate(p_node,port_new,GntInventoryListener.GntType.port,Action.UPDATE);
            	}
        	}
	 }
	 

	 private void processPortDelete(
		   AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {		 
	   for (InstanceIdentifier<?> newRpc : changes.getRemovedPaths()) {
		   //LOG.info("processPortDelete, deleteEntry: {}", newRpc);
		   if(newRpc.getTargetType().equals(Port.class)){
			   //Port port_new = (Port)newRpc;
			   //---Node p_node  =  getNode(changes.getOriginalData(),newRpc);
			   Node p_node  =  getNode(changes.getUpdatedData(),newRpc);
			   Port port_new = getDataChanges(changes.getOriginalData(),(InstanceIdentifier<Port>)newRpc);
			   //LOG.info("processPortDelete,Port is {}",port_new);
			   NodeUpdate(p_node,port_new,GntInventoryListener.GntType.port,Action.DELETE);
			   LOG.info("processPortDelete node is {},port is {}\n",p_node,port_new);
		   }
	   }
    }

	private void processNodeCreation(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {        
        for (Entry<InstanceIdentifier<?>, DataObject> newRpc : changes.getCreatedData().entrySet()) {
            //LOG.info("processNodeCreation, createdEntry: {}", newRpc);
            if(newRpc.getKey().getTargetType().equals(Node.class)){
                Node node_new = (Node)newRpc.getValue();
				LOG.info("processNodeCreation,Node is {}",node_new);
				NodeUpdate(node_new,node_new,GntInventoryListener.GntType.node,Action.ADD);
            }
        }
    }


	 private void processNodeUpdate(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {

        for (Map.Entry<InstanceIdentifier<?>, DataObject> newRpc : changes.getUpdatedData().entrySet()){
            if (newRpc.getKey().getTargetType().equals(Node.class)){
                  Node node_new = (Node)newRpc.getValue();
                //  LOG.info("processNodeUpdate: <{}>", node_new);
				 //NodeUpdate(node_new,node_new,GntInventoryListener.GntType.node,Action.UPDATE);
            	}
        	}
	 }

	 private void processNodeDelete(
		   AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {		 
	   for (InstanceIdentifier<?> newRpc : changes.getRemovedPaths()) {
		   //LOG.info("processNodeDelete, deleteEntry: {}", newRpc);
		   if(newRpc.getTargetType().equals(Node.class)){
			   //Node node_new = (Node)newRpc;
			   Node node_new = getDataChanges(changes.getOriginalData(),(InstanceIdentifier<Node>)newRpc);
			   //LOG.info("processNodeDelete,Node is {}",node_new);
			   NodeUpdate(node_new,node_new,GntInventoryListener.GntType.node,Action.DELETE);
		   }
	   }
    }
    private <T extends DataObject> T getDataChanges(
            Map<InstanceIdentifier<?>, DataObject> changes,InstanceIdentifier<T> path){

        for(Map.Entry<InstanceIdentifier<?>,DataObject> change : changes.entrySet()){
            if(change.getKey().getTargetType().equals(path.getTargetType())){
                @SuppressWarnings("unchecked")
                T dataObject = (T) change.getValue();
                return dataObject;
            }
        }
        return null;
    }


     private Router getRouter(Map<InstanceIdentifier<?>, DataObject> changes,
						  Map.Entry<InstanceIdentifier<?>, DataObject> change) {
		 InstanceIdentifier<Router> routerInstanceIdentifier = change.getKey().firstIdentifierOf(Router.class);
		 return (Router)changes.get(routerInstanceIdentifier);
	 }

     private Router getRouter(Map<InstanceIdentifier<?>, DataObject> changes,InstanceIdentifier<?> path) {
		 InstanceIdentifier<Router> nodeInstanceIdentifier = path.firstIdentifierOf(Router.class);
		 return (Router)changes.get(nodeInstanceIdentifier);
	 }
	 
	 
   	 private Node getNode(Map<InstanceIdentifier<?>, DataObject> changes,
						  Map.Entry<InstanceIdentifier<?>, DataObject> change) {
		 InstanceIdentifier<Node> nodeInstanceIdentifier = change.getKey().firstIdentifierOf(Node.class);
		 return (Node)changes.get(nodeInstanceIdentifier);
	 }
	 
	 private Node getNode(Map<InstanceIdentifier<?>, DataObject> changes,InstanceIdentifier<?> path) {
		 InstanceIdentifier<Node> nodeInstanceIdentifier = path.firstIdentifierOf(Node.class);
		 return (Node)changes.get(nodeInstanceIdentifier);
	 }

	 public void close() throws Exception {
        listenerReg.close();
     }

   

	private void NodeUpdate(Node node, DataObject resourceAugmentationDataChanges,
            GntInventoryListener.GntType gntType, Action action) {

		LOG.info("-------NodeUpdata-------nodedata is {},resourceAugmentationDataChanges is {},gnttype is {},action is {}----",node,resourceAugmentationDataChanges,gntType,action);
        Set<GntInventoryListener> gntInventoryListeners = GntInventoryServiceImpl.getGntInventoryListeners();
        for (GntInventoryListener gntInventoryListener : gntInventoryListeners) {
            gntInventoryListener.nodeUpdate(node, resourceAugmentationDataChanges, gntType, action);
        }
    }


    private void RouterUpdate(Router router, DataObject resourceAugmentationDataChanges,
            GntInventoryListener.GntType gntType, Action action) {

		LOG.info("-------RouterUpdata-------routerdata is {},resourceAugmentationDataChanges is {},gnttype is {},action is {}----",router,resourceAugmentationDataChanges,gntType,action);
        Set<GntInventoryListener> gntInventoryListeners = GntInventoryServiceImpl.getGntInventoryListeners();
        for (GntInventoryListener gntInventoryListener : gntInventoryListeners) {
            gntInventoryListener.routerUpdate(router, resourceAugmentationDataChanges, gntType, action);
        }
    }
}

