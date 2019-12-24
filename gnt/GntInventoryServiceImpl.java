/*
 * Copyright (c) 2017 CMCC, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.gnt.netvirt.gntimpl;

import com.google.common.collect.Sets;

import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.gnt.netvirt.translator.iaware.impl.NeutronRouterChangeListener;
import org.opendaylight.gnt.netvirt.translator.iaware.impl.NeutronSecurityRuleDataChangeListener;
import org.opendaylight.gnt.netvirt.ClusterAwareMdsalUtils;
import org.opendaylight.gnt.netvirt.ConfigInterface;
import org.opendaylight.gnt.netvirt.NetvirtProvider;
import org.opendaylight.gnt.netvirt.api.Constants;
import org.opendaylight.gnt.netvirt.api.OvsdbInventoryService;
import org.opendaylight.gnt.netvirt.api.OvsdbInventoryListener;
//import org.opendaylight.gnt.netvirt.translator.iaware.impl.NeutronFloatingIPChangeListener;
import org.opendaylight.gnt.netvirt.translator.iaware.impl.NeutronNetworkChangeListener;
import org.opendaylight.gnt.netvirt.translator.iaware.impl.NeutronPortChangeListener;
import org.opendaylight.gnt.netvirt.translator.iaware.impl.NeutronSubnetChangeListener;
import org.opendaylight.gnt.netvirt.translator.iaware.impl.NeutronLoadBalancerPoolChangeListener;
import org.opendaylight.gnt.netvirt.translator.iaware.impl.NeutronLoadBalancerPoolMemberChangeListener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * GntInventoryServiceImpl is the implementation for {@link GntInventoryService}
 *
 * @author cmcc
 */
public class GntInventoryServiceImpl implements ConfigInterface, GntInventoryService {
    private static final Logger LOG = LoggerFactory.getLogger(GntInventoryServiceImpl.class);
    private final DataBroker dataBroker;
    private static Set<GntInventoryListener> gntInventoryListeners = Sets.newCopyOnWriteArraySet();
    private GntChangeListener gntChangeListener = null;
    private static ClusterAwareMdsalUtils mdsalUtils = null;

    public GntInventoryServiceImpl(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        LOG.info("ChinacRpcInventoryServiceImpl initialized");
        gntChangeListener = new GntChangeListener(dataBroker);
        mdsalUtils = new ClusterAwareMdsalUtils(dataBroker);
    }

    @Override
    public void listenerAdded(GntInventoryListener listener) {
        gntInventoryListeners.add(listener);
        LOG.info("listenerAdded: {}", listener);
    }

    @Override
    public void listenerRemoved(GntInventoryListener listener) {
        gntInventoryListeners.remove(listener);
        LOG.info("listenerRemoved: {}", listener);
    }

    @Override
    public void providersReady() {
        gntChangeListener.start();
    }

    public static Set<GntInventoryListener> getGntInventoryListeners() {
        return gntInventoryListeners;
    }

    @Override
    public void setDependencies(ServiceReference serviceReference) {}

    @Override
    public void setDependencies(Object impl) {}

}

