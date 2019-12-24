/*
 * Copyright (c) 2015 CMCC, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.gnt.netvirt.gntimpl;

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
import org.opendaylight.gnt.netvirt.AbstractEvent;

/**
 * @cmcc
 */
public class NodeCacheManagerEvent extends AbstractEvent {
    private Node node;

    public NodeCacheManagerEvent(Node node, Action action) {
        super(HandlerType.GNTNODE, action);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "NodeCacheManagerEvent [action=" + super.getAction()
               + ", node=" + node
               + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        NodeCacheManagerEvent other = (NodeCacheManagerEvent) obj;
        if (node == null) {
            if (other.node != null) {
                return false;
            }
        } else if (!node.equals(other.node)) {
            return false;
        }
        return true;
    }
}
