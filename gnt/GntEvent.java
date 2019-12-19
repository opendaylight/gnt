/*
 * Copyright (c) 2017 CMCC, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.gnt.netvirt.gntimpl;


import org.opendaylight.gnt.netvirt.api.Action;


import org.opendaylight.yangtools.yang.binding.DataObject;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netvirt.chinacrpc.rev161018.*;

//import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netvirt.chinacrpc.rev161018.Chinacrpc;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netvirt.chinacrpc.rev161018.chinacrpc.*;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netvirt.chinacrpc.rev161018.chinacrpc.jsonrpcarray.*;

import org.opendaylight.gnt.netvirt.AbstractEvent;
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

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.*;




public class GntEvent extends AbstractEvent {
    public enum Type {Node,Port,Router,RouterInterface}
    private Type type;
	Node node;
	Router router;
    private String uuid;
    private Object context;
    private DataObject augmentationData;


	public GntEvent(Node node, Action action) {
        super(HandlerType.GNT, action);
        this.type = Type.Node;
        this.node = node;
    }




    public GntEvent(Node node, Type type, Action action) {
        super(HandlerType.GNT, action);
        this.type = type;
        this.node = node;
    }

   public GntEvent(Node node, DataObject resourceAugmentationData, Type type, Action action) {
        super(HandlerType.GNT, action);
        this.type = type;
        this.node = node;
        this.augmentationData = resourceAugmentationData;
    }


   public GntEvent(Router router, Action action) {
        super(HandlerType.GNT, action);
        this.type = Type.Router;
        this.router = router;
    }
   
    public GntEvent(Router router, Type type, Action action) {
        super(HandlerType.GNT, action);
        this.type = type;
        this.router = router;
    }
	
	public GntEvent(Router router, DataObject resourceAugmentationData, Type type, Action action) {
        super(HandlerType.GNT, action);
        this.type = type;
        this.router = router;
        this.augmentationData = resourceAugmentationData;
    }

    public Type getType() {
        return type;
    }


	
    public String getUuid() {
        return uuid;
    }
    public Object getContext() {
        return context;
    }
   

    public DataObject getAugmentationData() {
        return augmentationData;
    }


	public Node getNode() {
        return node;
    }

    public Router getRouter() {
        return router;
    }
	

}

