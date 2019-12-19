/*
 * Copyright (c) 2015 CMCC, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.gnt.netvirt.gntimpl;

import org.opendaylight.yangtools.yang.binding.DataObject;

import org.opendaylight.gnt.netvirt.api.Action;
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

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.*;

public interface GntInventoryListener {
    enum GntType {
       node,
	   port,
	   router,
	   routerinterface
    }
	
   void nodeUpdate(Node node, DataObject resourceAugmentationDataChanges,
            GntInventoryListener.GntType gntType, Action action);

   void routerUpdate(Router router, DataObject resourceAugmentationDataChanges,
            GntInventoryListener.GntType gntType, Action action);

   
}

