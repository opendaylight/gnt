/*
 * Copyright (c) 2017 CMCC. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.gnt.utils.gnt.utils;


import com.google.common.collect.ImmutableBiMap;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import org.opendaylight.netvirt.utils.mdsal.utils.MdsalUtils;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

       

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.*;

import java.util.ArrayList;

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.*;
import org.opendaylight.neutron.spi.*;

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

//port yang ;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.tunnels.*;

import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.rev150712.Neutron;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.Networks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.Subnets;

import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.sysset.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.Sysset;

import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.provider.ext.rev150712.NetworkProviderExtension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.provider.ext.rev150712.NetworkProviderExtensionBuilder;

public class GntUtils {
    private static final Logger LOG = LoggerFactory.getLogger(GntUtils.class);
    private static final int GTN_UPDATE_TIMEOUT = 1000;
    private final MdsalUtils mdsalUtils;
    private static final int DEDASHED_UUID_LENGTH = 32;
    private static final int DEDASHED_UUID_START = 0;
    private static final int DEDASHED_UUID_DIV1 = 8;
    private static final int DEDASHED_UUID_DIV2 = 12;
    private static final int DEDASHED_UUID_DIV3 = 16;
    private static final int DEDASHED_UUID_DIV4 = 20;
    
    private static final ImmutableBiMap<Class<? extends NodeTypeBase>,String> NODE_MAP
            = new ImmutableBiMap.Builder<Class<? extends NodeTypeBase>,String>()
            .put(OvsTypeBase.class,"ovs")
            .put(SwitchTypeBase.class,"switch")
            .put(VrfTypeBase.class,"vrf")
            .build();

	private static final ImmutableBiMap<Class<? extends SwitchTypeBase>,String> SWITCH_MAP
            = new ImmutableBiMap.Builder<Class<? extends SwitchTypeBase>,String>()
            .put(SymmetryTypeBase.class,"symmetry")
            .put(NosymmetryTypeBase.class,"nosymmetry")
            .build();
	
	public GntUtils(MdsalUtils mdsalUtils) {
        this.mdsalUtils = mdsalUtils;
    }

   	
   public GntUtils(DataBroker dataBroker) {
        mdsalUtils = new MdsalUtils(dataBroker);
    }

    public InstanceIdentifier<Node> createInstanceIdentifier_dpid(String dpid) {
		NodeKey key = new NodeKey(dpid);
        return InstanceIdentifier.create(Topo.class)
                .child(Nodes.class)
                .child(Node.class,key);
    }

   /*
	public InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> createInstanceIdentifier_gntport(String dpid,Uuid portuuid) {
		NodeKey key1 = new NodeKey(dpid);
		PortKey key2 = new PortKey(toUuid(portuuid));
        return InstanceIdentifier.create(Topo.class)
                .child(Nodes.class)
                .child(Node.class,key1)
                .child(Ports.class)
                .child(Port.class,key2);
    }*/

	public InstanceIdentifier<Syssets> createInstanceIdentifier_sys() {
	   return InstanceIdentifier.create(Topo.class)
			   .child(Syssets.class);
    }

    public  InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network> createInstanceIdentifier_network(String networkuuid) {
		org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkKey key = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkKey(toUuid(networkuuid));
		return InstanceIdentifier.create(Neutron.class)
			   .child(org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.Networks.class)
			   .child(org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network.class,key);
    }

	public  InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet> createInstanceIdentifier_subnet(String subnetuuid) {
		org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetKey key = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetKey(toUuid(subnetuuid));
		return InstanceIdentifier.create(Neutron.class)
			   .child(org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.Subnets.class)
			   .child(org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet.class,key);
    }

   //import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.Ports;
   //import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port;
	public  InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port> createInstanceIdentifier_port(String portuuid) {
		org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortKey key = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortKey(toUuid(portuuid));
		return InstanceIdentifier.create(Neutron.class)
			   .child(org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.Ports.class)
			   .child(org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port.class,key);
    }

	public Uuid toUuid(String uuid) {
        Uuid result;
        try {
            result = new Uuid(uuid);
        } catch(final IllegalArgumentException e) {
            // OK... someone didn't follow RFC 4122... lets try this the hard way
            final String dedashed = uuid.replace("-", "");
            if(dedashed.length() == DEDASHED_UUID_LENGTH) {
                final String redashed = dedashed.substring(DEDASHED_UUID_START, DEDASHED_UUID_DIV1)
                        + "-"
                        + dedashed.substring(DEDASHED_UUID_DIV1, DEDASHED_UUID_DIV2)
                        + "-"
                        + dedashed.substring(DEDASHED_UUID_DIV2, DEDASHED_UUID_DIV3)
                        + "-"
                        + dedashed.substring(DEDASHED_UUID_DIV3, DEDASHED_UUID_DIV4)
                        + "-"
                        + dedashed.substring(DEDASHED_UUID_DIV4, DEDASHED_UUID_LENGTH);
                result = new Uuid(redashed);
            } else {
                throw e;
            }
        }
        return result;
    }

    // get L2 VNI from network...
	public Long getL2Vni(InstanceIdentifier<?> netwrokIid) {
		
		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network network = (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, netwrokIid);
		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkBuilder networkBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkBuilder(network);
		
         NetworkProviderExtension providerExtension = network.getAugmentation(NetworkProviderExtension.class);
		 Long vni = new Long(Long.parseLong(providerExtension.getSegmentationId()));
		 return vni;
    }
	
	public Node getNode(String dpid,LogicalDatastoreType store) {

		InstanceIdentifier<Node> nodeIid = createInstanceIdentifier_dpid(dpid);
        return mdsalUtils.read(store, nodeIid);
		
    }
	
   public static IpAddress createIpAddress(InetAddress address) {
        IpAddress ip = null;
        if (address instanceof Inet4Address) {
            ip = createIpAddress((Inet4Address)address);
        } else if (address instanceof Inet6Address) {
            ip = createIpAddress((Inet6Address)address);
        }
        return ip;
    }

    public static IpAddress createIpAddress(Inet4Address address) {
        return IetfInetUtil.INSTANCE.ipAddressFor(address);
    }

    public static IpAddress createIpAddress(Inet6Address address) {
        Ipv6Address ipv6 = new Ipv6Address(address.getHostAddress());
        return new IpAddress(ipv6);
    }
	
    public NodeBuilder addTunnelNode(Node node,Map<String,String> tunnel_map) {

		   
           LOG.info("------addTunnelNode11--tunnel is {}-----",tunnel_map);
		   NodeBuilder nodeBuilder1 = new NodeBuilder(node);
		   if(tunnel_map == null) {
		   	   LOG.info("----addTunnelNode----tunnel is null-----");
               return  nodeBuilder1;
		   }	
		   List<Tunnel> tunnel = new ArrayList<>();	
		   TunnelsBuilder tunnelsBuilder = new TunnelsBuilder();
		   
		   for(String dpid : tunnel_map.keySet()){  
                 InstanceIdentifier<Node> nodeid = createInstanceIdentifier_dpid(dpid);
				 TunnelBuilder tunnelBuilder = new TunnelBuilder();
		         tunnelBuilder.setNodeId(new NodeRef(nodeid));
		         tunnelBuilder.setOfport(new Long(Long.parseLong(tunnel_map.get(dpid))));
		         tunnelBuilder.setTunnelUuid(dpid);
				 tunnel.add(tunnelBuilder.build());	
                
           }  
	       tunnelsBuilder.setTunnel(tunnel);			
		   LOG.info("tunnelsBuilder is {},tunnel is {}",tunnelsBuilder,tunnel);
		   nodeBuilder1.setTunnels(tunnelsBuilder.build());  
	       return nodeBuilder1;
	  }
      
    protected Node toNodeMd(String dpid,String tunnelip,String type,String switchtype,Map<String,String> tunnel_map) {
       // to NodeMd
	   NodeBuilder nodeBuilder = new NodeBuilder();
	   InetAddress inetAddress = null;
	   LOG.info("toNodeMd tunnelip is {}\n",tunnelip);
       try {
            inetAddress = InetAddress.getByName(tunnelip);
        } catch (UnknownHostException e) {
            LOG.info("Could not allocate InetAddress", e);
        }
	   
        IpAddress address = createIpAddress(inetAddress);
        nodeBuilder.setTunnelIp(address);
		nodeBuilder.setDatapathId(dpid);
		final ImmutableBiMap<String, Class<? extends NodeTypeBase>> mapper =
                NODE_MAP.inverse();
        nodeBuilder.setType((Class<? extends NodeTypeBase>) mapper.get(type)); 

		final ImmutableBiMap<String, Class<? extends SwitchTypeBase>> mapper_switch =
                SWITCH_MAP.inverse();		
		nodeBuilder.setNodeSwitchType((Class<? extends SwitchTypeBase>) mapper_switch.get(switchtype)); 
		// set tunnel....
		nodeBuilder = addTunnelNode(nodeBuilder.build(),tunnel_map);
		return nodeBuilder.build();
    }

   
     protected Syssets toSysMd(String min,String max) {
       // to syssets.
       SyssetsBuilder syssetsBuilder = new SyssetsBuilder();
	   syssetsBuilder.setVniMin(new Long(Long.parseLong(min)));
	   syssetsBuilder.setVniMax(new Long(Long.parseLong(max)));
	   return syssetsBuilder.build();
    }

	 
	public boolean addSys(String min,String max){
		boolean result = mdsalUtils.put(LogicalDatastoreType.CONFIGURATION,
                createInstanceIdentifier_sys(),
                toSysMd(min,max));
        return result;
    }

	/* api for add a node */
	public boolean addNode(String dpid,String type,String tunnelip,String switchtype,Map<String,String> tunnel_map) {
	    LOG.info("-----addNode------------dpid is {},type is {},tunnelip is {},switchtype is {}---",dpid,type,tunnelip,switchtype);
        boolean addNodeFlag = addLogicalNode(dpid,tunnelip,type,switchtype,tunnel_map,GTN_UPDATE_TIMEOUT);
		return addNodeFlag;
    }
	
    public  boolean addLogicalNode(String dpid,String tunnelip,String type,String switchtype,Map<String,String> tunnel_map,long timeout) {
        boolean result = mdsalUtils.put(LogicalDatastoreType.CONFIGURATION,
                createInstanceIdentifier_dpid(dpid),
                toNodeMd(dpid,tunnelip,type,switchtype,tunnel_map));
        if (timeout != 0) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting after adding  node {}",
                        e);
            }
        }
        return result;
    }


	/* api for delete a node */
    public  boolean deleteNode(String dpid) {
	    boolean delNodeFlag = deleteLogicalNode(dpid, GTN_UPDATE_TIMEOUT);
		return delNodeFlag;
    }

    
	public	boolean deleteLogicalNode(String dpid,long timeout) {
		  boolean result = mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION,
				  createInstanceIdentifier_dpid(dpid));
		  if (timeout != 0) {
			  try {
				  Thread.sleep(timeout);
			  } catch (InterruptedException e) {
				  LOG.warn("Interrupted while waiting after delete	node {}",
						  e);
			  }
		  }
		  return result;
	  }

	/* api for add a port */
	public boolean addPort(String dpid,org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port port1,Long ofport) {
	     LOG.info("-----addport dpid is {},Port is {}---",dpid,port1);
		 Node node = getNode(dpid,LogicalDatastoreType.CONFIGURATION);

		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder ptbuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder(port1);

		 NodeBuilder nodeBuilder = new NodeBuilder(node);
		 InstanceIdentifier Path =  createInstanceIdentifier_dpid(dpid);

         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder();
		 portBuilder.setMacAddress(new MacAddress(ptbuilder.getMacAddress().getValue()));
		 portBuilder.setOfport(ofport);
		 portBuilder.setPortUuid(toUuid(String.valueOf(ptbuilder.getUuid().getValue())));

		 InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network> NetworkIid = createInstanceIdentifier_network(ptbuilder.getNetworkId().getValue());	 
		 portBuilder.setNetworkId(new NetworkRef(NetworkIid));
         portBuilder.setDeviceOwner("baremetal:none");
		 LOG.info("----addPort----networkid is {}----",NetworkIid);
		 Long vni = getL2Vni(NetworkIid);
		 portBuilder.setVni(vni.toString());
		 LOG.info("tenantidtest444!");
		 LOG.info("ptbuildergetTenantId() is {}\n",String.valueOf(ptbuilder.getTenantId().getValue()).replace("-", ""));
		 //portBuilder.setTenantid(String.valueOf(ptbuilder.getTenantId().getValue()));
		 portBuilder.setTenantid(String.valueOf(ptbuilder.getTenantId().getValue()).replace("-", ""));
         
		 
	     InetAddress inetAddress = null;
         InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet> SubnetIid = null;                       
		  if (ptbuilder.getFixedIps() != null) {
            final List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> listNeutronIPs = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps>();
            for (final org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps neutron_IPs : ptbuilder.getFixedIps()) {
                final org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIpsBuilder fixedIpsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIpsBuilder();
			    org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIpsBuilder fiBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIpsBuilder(neutron_IPs);	
			    try {
                      inetAddress = InetAddress.getByName(String.valueOf(fiBuilder.getIpAddress().getValue()));
                } catch (UnknownHostException e) {
                      LOG.info("Could not allocate InetAddress", e);
                }
	            fixedIpsBuilder.setIpAddress(createIpAddress(inetAddress));
                SubnetIid = createInstanceIdentifier_subnet(fiBuilder.getSubnetId().getValue());
				fixedIpsBuilder.setSubnetId(new SubnetRef(SubnetIid));
                listNeutronIPs.add(fixedIpsBuilder.build());
            }
            portBuilder.setFixedIps(listNeutronIPs);
        }

		// add sercutiy group....
	    portBuilder.setSecurityGroups(ptbuilder.getSecurityGroups());
		LOG.info("addSecurityGroups is {}.",ptbuilder.getSecurityGroups());

		  
		 

	    org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> port;
		org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder;
		if(ports == null)
	    {
             port =  new ArrayList<>();
			 portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder();
			 LOG.info("ports is null,so create it");

		}
		else
	    {
		      portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
		      port = portsBuilder.getPort();
		 }
         port.add(portBuilder.build());
		 portsBuilder.setPort(port);
		 nodeBuilder.setPorts(portsBuilder.build());	
		 
		 mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, Path, nodeBuilder.build());  

		 if (GTN_UPDATE_TIMEOUT != 0) {
           try {
                Thread.sleep(GTN_UPDATE_TIMEOUT);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting after port on node {}",
                         e);
            }
		 }
		  return true;
   
    }

   
 
    /* api for del a port */
	public boolean delPort(String dpid,org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port port1,Long ofport) {
	     LOG.info("-----delport dpid is {},Port is {}---",dpid,port1);
		 Node node = getNode(dpid,LogicalDatastoreType.CONFIGURATION);

		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder ptbuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder(port1);

		 NodeBuilder nodeBuilder = new NodeBuilder(node);
		 InstanceIdentifier Path =  createInstanceIdentifier_dpid(dpid);

         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder();
		 portBuilder.setMacAddress(new MacAddress(ptbuilder.getMacAddress().getValue()));
		 portBuilder.setOfport(ofport);
		 portBuilder.setPortUuid(toUuid(String.valueOf(ptbuilder.getUuid().getValue())));

		 InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network> NetworkIid = createInstanceIdentifier_network(ptbuilder.getNetworkId().getValue());	 
		 portBuilder.setNetworkId(new NetworkRef(NetworkIid));
         portBuilder.setDeviceOwner("baremetal:none");
		 Long vni = getL2Vni(NetworkIid);
		 portBuilder.setVni(vni.toString());
		 //portBuilder.setTenantid(String.valueOf(ptbuilder.getTenantId().getValue()));
		 //LOG.info("ptbuilder.getTenantId().getValue() is{}, {}\n",ptbuilder.getTenantId().getValue(),String.valueOf(ptbuilder.getTenantId().getValue()));
		 LOG.info("tenantidtest555!");
		 //LOG.info("ptbuildergetTenantId() is {}\n",String.valueOf(ptbuilder.getTenantId().getValue()));
		 //portBuilder.setTenantid(String.valueOf(ptbuilder.getTenantId().getValue()));
         portBuilder.setTenantid(String.valueOf(ptbuilder.getTenantId().getValue()).replace("-", ""));
		 
	     InetAddress inetAddress = null;
         InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet> SubnetIid = null;                       

         if (ptbuilder.getFixedIps() != null) {
            final List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> listNeutronIPs = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps>();
            for (final org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps neutron_IPs : ptbuilder.getFixedIps()) {
                final org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIpsBuilder fixedIpsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIpsBuilder();
			    org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIpsBuilder fiBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIpsBuilder(neutron_IPs);	
			    try {
                      inetAddress = InetAddress.getByName(String.valueOf(fiBuilder.getIpAddress().getValue()));
                } catch (UnknownHostException e) {
                      LOG.info("Could not allocate InetAddress", e);
                }
	            fixedIpsBuilder.setIpAddress(createIpAddress(inetAddress));
                SubnetIid = createInstanceIdentifier_subnet(fiBuilder.getSubnetId().getValue());
				fixedIpsBuilder.setSubnetId(new SubnetRef(SubnetIid));
                listNeutronIPs.add(fixedIpsBuilder.build());
            }
            portBuilder.setFixedIps(listNeutronIPs);
        }
		 
        // add sercutiy group....
	    portBuilder.setSecurityGroups(ptbuilder.getSecurityGroups());
		LOG.info("delSecurityGroups is {}.",ptbuilder.getSecurityGroups());

		
	    org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
		List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> port = portsBuilder.getPort();
		LOG.info("---delPortstart---!\n");
        if(ports != null)
		   	{
		   	   LOG.info("delPortstart---port is {},deleteport is {}\n",port,portBuilder.build());
               port.remove(portBuilder.build());

		    }
		 portsBuilder.setPort(port);
		 nodeBuilder.setPorts(portsBuilder.build());	
		 
		 mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, Path, nodeBuilder.build());  

		 if (GTN_UPDATE_TIMEOUT != 0) {
           try {
                Thread.sleep(GTN_UPDATE_TIMEOUT);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting after port on node {}",
                         e);
            }
		 }

		 return true;
		/*org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port1);
		boolean result = mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION,
		    createInstanceIdentifier_gntport(dpid,portBuilder.getPortUuid()));
		if (GTN_UPDATE_TIMEOUT != 0) {
			  try {
				  Thread.sleep(GTN_UPDATE_TIMEOUT);
			  } catch (InterruptedException e) {
				  LOG.warn("Interrupted while waiting after delete	node {}",
						  e);
			  }
		  }
		  return result;*/ 
   
    }

	/* api for add a nova port */
	public boolean addNovaPort(String dpid,String portuuid) {
	     LOG.info("-----addnovaport dpid is {},Port is {}---",dpid,portuuid);
		 Node node = getNode(dpid,LogicalDatastoreType.CONFIGURATION);

		 NodeBuilder nodeBuilder = new NodeBuilder(node);
		 InstanceIdentifier Path =  createInstanceIdentifier_dpid(dpid);

         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder();
	

		 InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port> portIid = createInstanceIdentifier_port(portuuid);	 
		 portBuilder.setPortId(new PortRef(portIid));
		 portBuilder.setPortUuid(toUuid(portuuid));

	    org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> port;
		org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder;
		if(ports == null)
	    {
             port =  new ArrayList<>();
			 portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder();
			 LOG.info("ports is null,so create it");

		}
		else
	    {
		      portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
		      port = portsBuilder.getPort();
		 }
         port.add(portBuilder.build());
		 portsBuilder.setPort(port);
		 nodeBuilder.setPorts(portsBuilder.build());	
		 
		 mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, Path, nodeBuilder.build());  

		 if (GTN_UPDATE_TIMEOUT != 0) {
           try {
                Thread.sleep(GTN_UPDATE_TIMEOUT);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting after port on node {}",
                         e);
            }
		 }
		  return true;
   
    }
	
	
}


