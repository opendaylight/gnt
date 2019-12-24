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


import java.math.BigInteger;
import java.security.InvalidParameterException;

import java.util.ArrayList;
import java.lang.String;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.gnt.netvirt.ClusterAwareMdsalUtils;
import org.opendaylight.gnt.netvirt.MdsalHelper;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
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
import com.google.common.collect.ImmutableBiMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.tunnels.*;

import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.sysset.*;
import org.opendaylight.gnt.netvirt.translator.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.rev150712.Neutron;

import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIpsBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.*;
import java.net.UnknownHostException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.SubnetRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.provider.ext.rev150712.NetworkProviderExtension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.provider.ext.rev150712.NetworkProviderExtensionBuilder;



/**
 * Utility class to wrap mdsal transactions.
 *
 * @author cmcc
 */
 
public class GntImpl implements GntOp {
    private static final Logger LOG = LoggerFactory.getLogger(GntImpl.class);
    private final DataBroker databroker;
    private static final String PATCH_PORT_TYPE = "patch";
    private final ClusterAwareMdsalUtils mdsalUtils;

	private static final int GNT_UPDATE_TIMEOUT = 100;
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
	
	private static final String OWNER_ROUTER_GATEWAY = "network:router_gateway";
	private static final String OWNER_ROUTER_INTERFACE = "network:router_interface";
    /**
     * Class constructor setting the data broker.
     *
     * @param dataBroker the {@link org.opendaylight.controller.md.sal.binding.api.DataBroker}
     */
    public GntImpl(DataBroker dataBroker) {
        this.databroker = dataBroker;
        mdsalUtils = new ClusterAwareMdsalUtils(dataBroker);
    }

    public DataBroker getDatabroker() {
        return databroker;
    }

	public String getdpid(Node node) {
              NodeBuilder nodeBuilder = new NodeBuilder(node);
			  return nodeBuilder.getDatapathId();

		}	


    public String getdpid(String ipaddr) {
		
          NodeBuilder nodeBuildernew;
	      LOG.info("getdpid ip is {}\n",ipaddr);
          List<Node> nodes = readNodes();
	      for (Node node_new : nodes) {
			 nodeBuildernew = new NodeBuilder(node_new);
             org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuildernew.getPorts();
			 if(ports == null)
			 	continue;
	         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	         List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
			 if(portList == null)
			 	continue;
	         for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    		        
			     if(ipaddr.equals(getIp(port_new))) {
				 	 LOG.info("getdpid ip is {},dpid is {}\n",ipaddr,getdpid(node_new));					 
				 	 return getdpid(node_new);
			     }		 
	          }
			          
	      	}
           return null;       
        
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

	
   public InstanceIdentifier<Node> createInstanceIdentifier_dpid(String dpid) {
	   NodeKey key = new NodeKey(dpid);
	   return InstanceIdentifier.create(Topo.class)
			   .child(Nodes.class)
			   .child(Node.class,key);
   }

     
   public InstanceIdentifier<Nodes> createInstanceIdentifierNodes() {
        return InstanceIdentifier.create(Topo.class)
                .child(Nodes.class);
    }

	public  InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet> createInstanceIdentifier_subnet(String subnetuuid) {
		org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetKey key = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetKey(toUuid(subnetuuid));
		return InstanceIdentifier.create(Neutron.class)
			   .child(org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.Subnets.class)
			   .child(org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet.class,key);
    }
	
    static private String getTypeString(Class<? extends NodeTypeBase> type) {
        String ret = NODE_MAP.get(type);
        if (ret == null) {
            ret = "error";
        }
        return ret;
    }
	
    public String getTypeNode(Node node) {
		 NodeBuilder nodeBuilder = new NodeBuilder(node);
	     return getTypeString(nodeBuilder.getType());
	         
    }


    static private String getSwitchTypeString(Class<? extends SwitchTypeBase> type) {
        String ret = SWITCH_MAP.get(type);
        if (ret == null) {
            ret = "error";
        }
        return ret;
    }
	
    public String getSwitchType(Node node) {
		 NodeBuilder nodeBuilder = new NodeBuilder(node);
	     return getSwitchTypeString(nodeBuilder.getNodeSwitchType());
	         
    }
	
    public List<Node> readNodes() {
        List<Node> Nodes = new ArrayList<>();
        InstanceIdentifier<Nodes> topologyInstanceIdentifier = createInstanceIdentifierNodes();
        Nodes topology = mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, topologyInstanceIdentifier);
        if (topology != null && topology.getNode() != null) {
            for (Node node : topology.getNode()) {
				    LOG.info("-------------readnode is {}------------",node);
                    Nodes.add(node);
            	}
        	}
        return Nodes;
      }

	 public List<Node> readSwitchNodes() {
        List<Node> Nodes = new ArrayList<>();
		String sw = "switch";
        InstanceIdentifier<Nodes> topologyInstanceIdentifier = createInstanceIdentifierNodes();
        Nodes topology = mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, topologyInstanceIdentifier);
        if (topology != null && topology.getNode() != null) {
            for (Node node : topology.getNode()) {
				 if(sw.equals(getTypeNode(node)))
                     Nodes.add(node);
            	}
        	}
        return Nodes;
      }

     public List<Node> readOvsNodes() {
        List<Node> Nodes = new ArrayList<>();
		String ovs = "ovs";
        InstanceIdentifier<Nodes> topologyInstanceIdentifier = createInstanceIdentifierNodes();
        Nodes topology = mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, topologyInstanceIdentifier);
        if (topology != null && topology.getNode() != null) {
            for (Node node : topology.getNode()) {
				   if(ovs.equals(getTypeNode(node)))
                      Nodes.add(node);
            	}
        	}
        return Nodes;
      }

	 public List<Node> readVrfNodes() {
        List<Node> Nodes = new ArrayList<>();
		String vrf = "vrf";
        InstanceIdentifier<Nodes> topologyInstanceIdentifier = createInstanceIdentifierNodes();
        Nodes topology = mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, topologyInstanceIdentifier);
        if (topology != null && topology.getNode() != null) {
            for (Node node : topology.getNode()) {
				  if(vrf.equals(getTypeNode(node)))
                     Nodes.add(node);
            	}
        	}
        return Nodes;
      }

      public Tunnel getTunnel(Node node,long ofport) {

		   NodeBuilder nodeBuilder = new NodeBuilder(node);
		   InstanceIdentifier<Node> nodeid = createInstanceIdentifier_dpid(nodeBuilder.getDatapathId());

		   Long ofP = new Long(ofport);
		   TunnelBuilder tunnelBuilder = new TunnelBuilder();
		   tunnelBuilder.setNodeId(new NodeRef(nodeid));
		   tunnelBuilder.setOfport(ofP);
		   tunnelBuilder.setTunnelUuid(nodeBuilder.getDatapathId());

		   return tunnelBuilder.build();

      }	
	  
      public boolean addTunnelNode(Node node,List<Tunnel> tunnels,Long ofport) {
          LOG.info("---addTunnelNode--tunnels is {}----\n",tunnels);  

		  NodeBuilder nodeBuilder = new NodeBuilder(node);
		  TunnelsBuilder tunnelsBuilder = new TunnelsBuilder();
		  tunnelsBuilder.setTunnel(tunnels);
          nodeBuilder.setTunnels(tunnelsBuilder.build()); 
          nodeBuilder.setMinport(ofport);
		  nodeBuilder.setSwitchMac(new MacAddress(getMacaddr(node)));
		  
          InstanceIdentifier Path =  createInstanceIdentifier_dpid(nodeBuilder.getDatapathId());
		  mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, Path, nodeBuilder.build());	
	      LOG.info("---addTunnelNode1---node is {}-----\n",nodeBuilder.build());
		  
		  if (GNT_UPDATE_TIMEOUT != 0) {
				 try {
					  Thread.sleep(GNT_UPDATE_TIMEOUT);
				  } catch (InterruptedException e) {
					  LOG.warn("Interrupted while waiting after port on node {}",
							   e);
				  }
			   }
		  return true;

       	}
      
	   /* api for add a tunnel */
	   // tunnel node1->node2
       public boolean addTunnelNode(Node node1,Node node2) {
		   LOG.info("-----addtunnel node1 is {},node2 is {}---",node1,node2);
	  
		   NodeBuilder nodeBuilder1 = new NodeBuilder(node1);
		   NodeBuilder nodeBuilder2 = new NodeBuilder(node2);
	  
		   InstanceIdentifier<Node> nodeid = createInstanceIdentifier_dpid(nodeBuilder2.getDatapathId());
			   
		   TunnelBuilder tunnelBuilder = new TunnelBuilder();
		   tunnelBuilder.setNodeId(new NodeRef(nodeid));
		   Long useport = nodeBuilder1.getMinport();
		   tunnelBuilder.setOfport(useport);
		   Long luseport = new Long(useport.longValue() + 1);
		   nodeBuilder1.setMinport(luseport);
		   tunnelBuilder.setTunnelUuid(nodeBuilder2.getDatapathId());
			   
		   Tunnels tunnels = nodeBuilder1.getTunnels();
		   List<Tunnel> tunnel;
		   TunnelsBuilder tunnelsBuilder;
		   if(tunnels == null)
			   {
			       tunnelsBuilder = new TunnelsBuilder();
				   tunnel =  new ArrayList<>();			  
				   LOG.info("tunnels is null,so create it");
				   
			   }
			else
			   {
				    tunnelsBuilder = new TunnelsBuilder(tunnels);
					tunnel = tunnelsBuilder.getTunnel();
					
			   }
			 tunnel.add(tunnelBuilder.build());	
			 tunnelsBuilder.setTunnel(tunnel);
			 LOG.info("tunnelsBuilder is {},tunnel is {}",tunnelsBuilder,tunnel);
			 nodeBuilder1.setTunnels(tunnelsBuilder.build());  
	  
			 InstanceIdentifier Path =  createInstanceIdentifier_dpid(nodeBuilder1.getDatapathId());
			 mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, Path, nodeBuilder1.build());	
	  
			 if (GNT_UPDATE_TIMEOUT != 0) {
				 try {
					  Thread.sleep(GNT_UPDATE_TIMEOUT);
				  } catch (InterruptedException e) {
					  LOG.warn("Interrupted while waiting after port on node {}",
							   e);
				  }
			   }
				return true;
	  }
      
       // get TunnelBuilder 
       public TunnelBuilder getTunnelBuilder(Node node1,Node node2) {

           LOG.info("--getTunnelBuilder---\n");
           NodeBuilder nodeBuilder1 = new NodeBuilder(node1);
		   NodeBuilder nodeBuilder2 = new NodeBuilder(node2);

		   String dpid = nodeBuilder2.getDatapathId();
		   Tunnels tunnels = nodeBuilder1.getTunnels();
		   TunnelsBuilder tunnelsBuilder = new TunnelsBuilder(tunnels);
		   List<Tunnel> tunnel = tunnelsBuilder.getTunnel();
		   Iterator<Tunnel> iter = tunnel.iterator();
		   TunnelBuilder tunnelBuilder;
		   String dpidnew;
		   while(iter.hasNext())
			  {
                  tunnelBuilder = new TunnelBuilder(iter.next());
			      dpidnew = tunnelBuilder.getTunnelUuid();
	              if(dpid.equals(dpidnew))
	            	{
                       return tunnelBuilder;
				    }


		    }

			return null;
			
       }


	   public TunnelBuilder getTunnelBuilder(Node node1) {

           LOG.info("--getTunnelBuilder---\n");
           NodeBuilder nodeBuilder1 = new NodeBuilder(node1);

		   String dpid = "GW";
		   Tunnels tunnels = nodeBuilder1.getTunnels();
		   TunnelsBuilder tunnelsBuilder = new TunnelsBuilder(tunnels);
		   List<Tunnel> tunnel = tunnelsBuilder.getTunnel();
		   Iterator<Tunnel> iter = tunnel.iterator();
		   TunnelBuilder tunnelBuilder;
		   String dpidnew;
		   while(iter.hasNext())
			  {
                  tunnelBuilder = new TunnelBuilder(iter.next());
			      dpidnew = tunnelBuilder.getTunnelUuid();
	              if(dpid.equals(dpidnew))
	            	{
                       return tunnelBuilder;
				    }


		    }

			return null;
			
       }

	   /* api for delete a tunnel */
	   // tunnel node1->node2
       public boolean delTunnelNode(Node node1,Node node2) {
		   LOG.info("-----addtunnel node1 is {},node2 is {}---",node1,node2);
	  
		   NodeBuilder nodeBuilder1 = new NodeBuilder(node1);
		   NodeBuilder nodeBuilder2 = new NodeBuilder(node2);
	  
		   InstanceIdentifier<Node> nodeid = createInstanceIdentifier_dpid(nodeBuilder2.getDatapathId());
			   
		   TunnelBuilder tunnelBuilder = getTunnelBuilder(node1,node2);
           if(tunnelBuilder == null)
		   	 return false;
			   
		   Tunnels tunnels = nodeBuilder1.getTunnels();
		   TunnelsBuilder  tunnelsBuilder = new  TunnelsBuilder(tunnels);
		   List<Tunnel> tunnel = tunnelsBuilder.getTunnel();
           if(tunnels != null)
		   	{
               tunnel.remove(tunnelBuilder.build());
		    }

			 tunnelsBuilder.setTunnel(tunnel);
			 nodeBuilder1.setTunnels(tunnelsBuilder.build());   
	  
			 InstanceIdentifier Path =  createInstanceIdentifier_dpid(nodeBuilder1.getDatapathId());
			 mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, Path, nodeBuilder1.build());	
	  
			 if (GNT_UPDATE_TIMEOUT != 0) {
				 try {
					  Thread.sleep(GNT_UPDATE_TIMEOUT);
				  } catch (InterruptedException e) {
					  LOG.warn("Interrupted while waiting after port on node {}",
							   e);
				  }
			   }

			 return true;
	  }
	  public Long getOfPort(Node node) {
	  	 
           NodeBuilder nodeBuilder = new NodeBuilder(node);
		   return nodeBuilder.getMinport();
	  }
	  
	  public void addtunnel(Node node) {
          String nodetype = getTypeNode(node);
		  NodeBuilder nodeBuilder = new NodeBuilder(node);
		  NodeBuilder nodeBuildernew;
		  List<Tunnel> tunnel = new ArrayList<>();
		  if(nodetype.equals("switch")) {
              //if node is switch
              List<Node> nodes = readNodes();
			  Long ofport = getOfPort(node);
			  long ofp = ofport.longValue();
			  for (Node node_new : nodes) {
			  	   nodeBuildernew = new NodeBuilder(node_new);
			  	    if(nodeBuildernew.getDatapathId().equals(nodeBuilder.getDatapathId()))
						continue;
					  //LOG.info("add1 node is {},node_new is {}\n",node,node_new);
			          //addTunnelNode(node,node_new);
			          tunnel.add(getTunnel(nodeBuildernew.build(),ofp));
					  ofp++;
			          
            	}
			  Long OFPORT = new Long(ofp);
			  addTunnelNode(node,tunnel,OFPORT);
			  LOG.info("addtunnelnode is {},tunnel is {},port is {}\n",node,tunnel,OFPORT);
			  List<Node> nodeswitch = readSwitchNodes();
			  for (Node node_new : nodeswitch) {
			  	    nodeBuildernew = new NodeBuilder(node_new);
			  	    if(nodeBuildernew.getDatapathId().equals(nodeBuilder.getDatapathId()))
						continue;
					LOG.info("add2 node_new is {},node is {}\n",node_new,node);
			        addTunnelNode(node_new,node);
					//addTunnelNode(node,node_new);
            	}
		  	}
		  else {
              
              List<Node> nodeswitch = readSwitchNodes();
			  for (Node node_new : nodeswitch) {
			        addTunnelNode(node_new,node);
			   }

		  	}


	   }

       public void deltunnel(Node node) {
          String nodetype = getTypeNode(node);
		  NodeBuilder nodeBuilder = new NodeBuilder(node);
		  NodeBuilder nodeBuildernew ;
		  if(nodetype.equals("switch")) {
              //if node is switch
			  List<Node> nodeswitch = readSwitchNodes();
			  for (Node node_new : nodeswitch) {
			  	    nodeBuildernew = new NodeBuilder(node_new);
			  	    if(nodeBuildernew.getDatapathId().equals(nodeBuilder.getDatapathId()))
						continue;
			        delTunnelNode(node_new,node);
            	}
		  	}
		  else {
              
              List<Node> nodeswitch = readSwitchNodes();
			  for (Node node_new : nodeswitch) {
			        delTunnelNode(node_new,node);
			   }

		  	}

	  }

     
	 public  InstanceIdentifier<Routers> createInstanceIdentifierRouters() {
		 return InstanceIdentifier.create(Topo.class)
				.child(Routers.class);
	 }

     public  InstanceIdentifier<Router> createInstanceIdentifierRouter(String routerid) {
	 	 RouterKey routerKey = new RouterKey(toUuid(routerid));
		 return InstanceIdentifier.create(Topo.class)
				.child(Routers.class)
				.child(Router.class,routerKey);
	 }
	 
	 public  InstanceIdentifier<Syssets> createInstanceIdentifierSyssets() {
		 return InstanceIdentifier.create(Topo.class)
				.child(Syssets.class);
	 }

	 public Syssets getSyssets() {
		InstanceIdentifier<Syssets> syssetsIid = createInstanceIdentifierSyssets();
        return mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, syssetsIid);
		
     } 
	 
     public Routers getRouters() {

		InstanceIdentifier<Routers> routerIid = createInstanceIdentifierRouters();
        return mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, routerIid);
		
     }

     public Router getRouter(String routerid) {

		InstanceIdentifier<Router> routerIid = createInstanceIdentifierRouter(routerid);
        return mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, routerIid);
		
     }
	 
     public Ports getPorts() {

		InstanceIdentifier<Ports> path = InstanceIdentifier
                .create(Neutron.class)
                .child(Ports.class);
        return mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, path);
		
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

	public Long getMinVni() {
        Syssets syssets = getSyssets();
		SyssetsBuilder syssetsBuilder = new SyssetsBuilder(syssets);
		return syssetsBuilder.getVniMin();

	}
	
   public boolean setMinVni(Long min) {
        Syssets syssets = getSyssets();
		SyssetsBuilder syssetsBuilder = new SyssetsBuilder(syssets);
		syssetsBuilder.setVniMin(min);
	    InstanceIdentifier Path =  createInstanceIdentifierSyssets();
		mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, Path, syssetsBuilder.build());	
	  
		 if (GNT_UPDATE_TIMEOUT != 0) {
		     try {
					  Thread.sleep(GNT_UPDATE_TIMEOUT);
				  } catch (InterruptedException e) {
					  LOG.warn("Interrupted while waiting after port on node {}",
							   e);
				  }
			   }
		 return true;
		

   }
	
   public Long getMaxVni() {
        Syssets syssets = getSyssets();
		SyssetsBuilder syssetsBuilder = new SyssetsBuilder(syssets);
		return syssetsBuilder.getVniMax();

	}

   public String getVni() {
         
       Long vni = getMinVni();
       return vni.toString();
   	}

   public String getMacaddr(String subnetuuid) {
        
          org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.Ports ports = getPorts();
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.PortsBuilder portsBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.PortsBuilder(ports);
		  List<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port> port = portsBuilder.getPort();
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder portBuilder;
		  for(Port portnew : port) {
                portBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder(portnew);
				if(portBuilder.getDeviceOwner().equalsIgnoreCase(OWNER_ROUTER_INTERFACE)) {
				    if (portBuilder.getFixedIps() != null) {
                       List<Neutron_IPs> ips = new ArrayList<>();
                       for (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps mdIP : portBuilder.getFixedIps()) {
                           if (subnetuuid.equals(mdIP.getSubnetId().getValue()))                          
						       return portBuilder.getMacAddress().getValue();
                
                    	 }
            
			        }

			    }
		  	}		

		  return null;
   	 }


    public String getMacaddrArp(String ip) {
       // if for l2 ip get macaddr.
       NodeBuilder nodeBuildernew;
	   LOG.info("getMacaddr ip is {}\n",ip);
       List<Node> nodes = readNodes();
	   for (Node node_new : nodes) {
			 nodeBuildernew = new NodeBuilder(node_new);
             org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuildernew.getPorts();
			 if(ports == null)
			 	continue;
	         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	         List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
			 if(portList == null)
			 	continue;
	         for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    		        
			     if(ip.equals(getIp(port_new))) {
				 	 LOG.info("getMacaddr ip is {},macaddr is {}\n",ip,getMacaddr(port_new));
				 	 return getMacaddr(port_new);
			     }		 
	         }
			          
        }
         LOG.info("-----getMacaddrArp--------1-------");
	     //l3 ip get macaddr....
	     Routers routers = getRouters();
		 if(routers == null)
		 	return null;
		 LOG.info("-----getMacaddrArp--------2-------");
		 RoutersBuilder routersBuilder = new RoutersBuilder(routers);
		 List<Router> router =  routersBuilder.getRouter();
		 RouterBuilder routerBuilder;
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets> subnets;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder subnetsBuilder;
		 InstanceIdentifier<?> subnetIid;

		 for (Router router_new : router) {		    
		      routerBuilder =  new RouterBuilder(router_new);		      
		      subnets = routerBuilder.getSubnets();
			  LOG.info("-----getMacaddrArp--------3-------");
			  for(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets subnet_new : subnets) {
			       subnetsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder(subnet_new);
				   LOG.info("-----getMacaddrArp--------4-ip is {}------",String.valueOf(subnetsBuilder.getIpAddress().getValue()));
				   
			       if(ip.equals(String.valueOf(subnetsBuilder.getIpAddress().getValue()))) {
				   	  LOG.info("getMacaddr ip is {},macaddr is {}\n",ip,String.valueOf(subnetsBuilder.getMacaddr().getValue()));
				   	  return subnetsBuilder.getMacaddr().getValue();
			       	}
			  	   
			  }	   
         }

         LOG.info("do not find ip {} for macaddr!\n",ip);
		 return null;

	}
	

	public List<Neutron_IPs> getRouterInterface(String routerid) {
          LOG.info("getRouterInterface routerid is {}\n",routerid);
		  List<Neutron_IPs> ips = new ArrayList<>();
		  
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.Ports ports = getPorts();
		  LOG.info("ports is {}\n",ports); 
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.PortsBuilder portsBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.PortsBuilder(ports);
		  List<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port> port = portsBuilder.getPort();
		  LOG.info("getRouterInterface1 \n");
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder portBuilder;
		  for(org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port portnew : port) {
                portBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder(portnew);
				if((portBuilder.getDeviceOwner().equalsIgnoreCase(OWNER_ROUTER_INTERFACE))&&(routerid.equals(portBuilder.getDeviceId()))) {
					LOG.info("getRouterInterface routerinterface is finded!\n");
				    if (portBuilder.getFixedIps() != null) {
						LOG.info("getRouterInterface routerinterface2! \n");
                        for (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.FixedIps mdIP : portBuilder.getFixedIps()) {
                              Neutron_IPs IPs = new  Neutron_IPs();
							  IPs.setIpAddress(String.valueOf(mdIP.getIpAddress().getValue()));
							  IPs.setSubnetUUID(mdIP.getSubnetId().getValue());
                              ips.add(IPs);
							  LOG.info("getRouterInterface ips is {}! \n",ips);
                         }
                     }
            
			     }

			}		
          
		  return ips;

    }


	
	public boolean addRouter(NeutronRouter neutronRouter) {
		 
         InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet> SubnetIid = null; 
		 
		 RouterBuilder routerBuilder = new RouterBuilder();
		 routerBuilder.setRouterId(toUuid(neutronRouter.getRouterUUID()));
         List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets> subnets = new ArrayList<>();
		 InetAddress inetAddress = null;
		 LOG.info("addRouter uuid is {},neutronRouter is {}\n",toUuid(neutronRouter.getRouterUUID()),neutronRouter);
		
		 List<Neutron_IPs> neutron_IPs = getRouterInterface(neutronRouter.getRouterUUID());
		 LOG.info("-------addRouter1-----");
		 if(neutron_IPs == null)
		 	return true;
		 LOG.info("addRouter Neutron_IPs is {}\n",neutron_IPs);
		 for ( Neutron_IPs neutron_ips : neutron_IPs ) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder subnetsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder();
			    SubnetIid = createInstanceIdentifier_subnet(neutron_ips.getSubnetUUID());
				subnetsBuilder.setSubnetId(new SubnetRef(SubnetIid));
				if(neutron_ips.getIpAddress()!=null) {
				   try {
                        inetAddress = InetAddress.getByName(neutron_ips.getIpAddress());
                    } catch (UnknownHostException e) {
                      LOG.info("Could not allocate InetAddress", e);
                    }
					
	                subnetsBuilder.setIpAddress(createIpAddress(inetAddress));
				}
                subnetsBuilder.setMacaddr(new MacAddress(getMacaddr(neutron_ips.getSubnetUUID())));

				subnets.add(subnetsBuilder.build());
				LOG.info("--addRouter-subnets is {}\n",subnets);

		 	}

		 routerBuilder.setSubnets(subnets);
		 String vni = getVni();
		 routerBuilder.setVni(vni);
		 long vniNew = Long.parseLong(vni) + 1;
		 Long vniNewLong = new Long(vniNew);
		 setMinVni(vniNewLong);
		 LOG.info("-------addRouter2-----");
		 InstanceIdentifier Path =  createInstanceIdentifierRouter(neutronRouter.getRouterUUID());
		 mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, Path, routerBuilder.build());	
	  
		 if (GNT_UPDATE_TIMEOUT != 0) {
		     try {
					  Thread.sleep(GNT_UPDATE_TIMEOUT);
				  } catch (InterruptedException e) {
					  LOG.warn("Interrupted while waiting after port on node {}",
							   e);
				  }
			   }
		 return true;
     }

     public boolean delRouter(NeutronRouter neutronRouter) {
         InstanceIdentifier Path =  createInstanceIdentifierRouter(neutronRouter.getRouterUUID());
		 mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION, Path);	
	  
		 if (GNT_UPDATE_TIMEOUT != 0) {
		     try {
					  Thread.sleep(GNT_UPDATE_TIMEOUT);
				  } catch (InterruptedException e) {
					  LOG.warn("Interrupted while waiting after port on node {}",
							   e);
		     }
	      }
		 return true;
      }
	 
      public boolean delRouter(String routeruuid) {
         InstanceIdentifier Path =  createInstanceIdentifierRouter(routeruuid);
		 mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION, Path);	
	  
		 if (GNT_UPDATE_TIMEOUT != 0) {
		     try {
					  Thread.sleep(GNT_UPDATE_TIMEOUT);
				  } catch (InterruptedException e) {
					  LOG.warn("Interrupted while waiting after port on node {}",
							   e);
		     }
	      }
		 return true;
     }
	  
	  public boolean updatedRouter(NeutronRouter neutronRouter) {
	  	 delRouter(neutronRouter.getRouterUUID());
		 addRouter(neutronRouter);
		 return true;
	  }

	  public boolean addRouterInterface(NeutronPort neutronPort) {

		 Router router = getRouter(neutronPort.getDeviceID());
         InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet> SubnetIid = null; 
		 if(router == null)
		 	return false;
		 RouterBuilder routerBuilder = new RouterBuilder(router);
         List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets> subnets = routerBuilder.getSubnets();
		 if(subnets == null) {
              subnets = new ArrayList<>();
			  LOG.info("addRouterInterface add subnets!\n");
		 }
		 
		 InetAddress inetAddress = null;
		 LOG.info("addRouterInterface uuid is {}\n",neutronPort.getDeviceID());
		
		 List<Neutron_IPs> neutron_IPs = neutronPort.getFixedIPs();
		 LOG.info("-------addRouterInterface-----");
		 if(neutron_IPs == null)
		 	return true;
		 LOG.info("addRouter Neutron_IPs is {}\n",neutron_IPs);
		 for ( Neutron_IPs neutron_ips : neutron_IPs ) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder subnetsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder();
			    SubnetIid = createInstanceIdentifier_subnet(neutron_ips.getSubnetUUID());
				subnetsBuilder.setSubnetId(new SubnetRef(SubnetIid));
				if(neutron_ips.getIpAddress()!=null) {
				   try {
                        inetAddress = InetAddress.getByName(neutron_ips.getIpAddress());
                    } catch (UnknownHostException e) {
                      LOG.info("Could not allocate InetAddress", e);
                    }
					
	                subnetsBuilder.setIpAddress(createIpAddress(inetAddress));
				}
                subnetsBuilder.setMacaddr(new MacAddress(getMacaddr(neutron_ips.getSubnetUUID())));

				subnets.add(subnetsBuilder.build());
				LOG.info("--addRouter-subnets is {}\n",subnets);

		 	}

		 routerBuilder.setSubnets(subnets);
		 InstanceIdentifier Path =  createInstanceIdentifierRouter(neutronPort.getDeviceID());
		 mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, Path, routerBuilder.build());	
	  
		 if (GNT_UPDATE_TIMEOUT != 0) {
		     try {
					  Thread.sleep(GNT_UPDATE_TIMEOUT);
				  } catch (InterruptedException e) {
					  LOG.warn("Interrupted while waiting after port on node {}",
							   e);
				  }
			   }
		 return true;
     }

     public boolean delRouterInterface(NeutronPort neutronPort) {

		 Router router = getRouter(neutronPort.getDeviceID());
         InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet> SubnetIid = null; 
		 
		 RouterBuilder routerBuilder = new RouterBuilder(router);
         List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets> subnets = routerBuilder.getSubnets();
		 
		 InetAddress inetAddress = null;
		 LOG.info("delRouterInterface uuid is {}\n",neutronPort.getDeviceID());
		
		 List<Neutron_IPs> neutron_IPs = neutronPort.getFixedIPs();
		 LOG.info("-------delRouterInterface-----");
		 if(neutron_IPs == null)
		 	return true;
		 LOG.info("delRouter Neutron_IPs is {}\n",neutron_IPs);
		 for ( Neutron_IPs neutron_ips : neutron_IPs ) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder subnetsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder();
			    SubnetIid = createInstanceIdentifier_subnet(neutron_ips.getSubnetUUID());
				subnetsBuilder.setSubnetId(new SubnetRef(SubnetIid));
				if(neutron_ips.getIpAddress()!=null) {
				   try {
                        inetAddress = InetAddress.getByName(neutron_ips.getIpAddress());
                    } catch (UnknownHostException e) {
                      LOG.info("Could not allocate InetAddress", e);
                    }
					
	                subnetsBuilder.setIpAddress(createIpAddress(inetAddress));
				}
                subnetsBuilder.setMacaddr(new MacAddress(neutronPort.getMacAddress()));
				subnets.remove(subnetsBuilder.build());
				LOG.info("--delRouter-subnets is {}\n",subnets);

		 	}

		 routerBuilder.setSubnets(subnets);
		 InstanceIdentifier Path =  createInstanceIdentifierRouter(neutronPort.getDeviceID());
		 mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, Path, routerBuilder.build());	
	  
		 if (GNT_UPDATE_TIMEOUT != 0) {
		     try {
					  Thread.sleep(GNT_UPDATE_TIMEOUT);
				  } catch (InterruptedException e) {
					  LOG.warn("Interrupted while waiting after port on node {}",
							   e);
				  }
			   }
		 return true;
     }


	 public boolean portIsTheNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node node) {
         NodeBuilder nodeBuilder = new NodeBuilder(node);        
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	     List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
	     String portuuid = portBuilder.getPortUuid().getValue();
	     for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
		     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			 if(portuuid.equals(portBuilder_new.getPortUuid().getValue()))
			 	return true;
          }

		 return false;
   	 }

     /*network is the node*/
     public boolean networkIsTheNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node node) {

		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 InstanceIdentifier<?> netwrokIid;
		 netwrokIid = portBuilder.getNetworkId().getValue();		 
		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network network = (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, netwrokIid);
		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkBuilder networkBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkBuilder(network);
		 String networkuuid = networkBuilder.getUuid().getValue();
		 	
         NodeBuilder nodeBuilder = new NodeBuilder(node);
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	     List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
	  
		 InstanceIdentifier<?> netwrokIid_new;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network network_new;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkBuilder networkBuilder_new;
		 
	     for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
		      portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);		      
		      netwrokIid_new = portBuilder_new.getNetworkId().getValue();
		      network_new = (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, netwrokIid_new);
			  networkBuilder_new = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkBuilder(network_new);
			  if(networkuuid.equals(networkBuilder_new.getUuid()))
			  	  return true;
         }

		 return false;
   	 }

    public boolean isSubnet(InstanceIdentifier<?> subnetIid,List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps) {
         LOG.info("isSubnet subnetIid is {}!\n",subnetIid);		
         org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet subnet =  (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, subnetIid);
		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetBuilder subnetBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetBuilder(subnet);
		 String subnetuuid = subnetBuilder.getUuid().getValue();
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIpsBuilder fixedIpsBuilder;
		 InstanceIdentifier<?> subnetid;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet subnet_new;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetBuilder subnetBuilder_new;
		 
		 for(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps mdIP : fixedIps) {
             fixedIpsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIpsBuilder(mdIP);
			 subnetid = fixedIpsBuilder.getSubnetId().getValue();
		     subnet_new =  (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, subnetid);	
			 subnetBuilder_new = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetBuilder(subnet_new);
			 if(subnetuuid.equals(subnetBuilder_new.getUuid().getValue()))
			 	 return true;


		 }

		 return false;
		 


    }

	
    public boolean isSubnet(InstanceIdentifier<?> subnetIid,List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps1,List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps2) {
		if(isSubnet(subnetIid,fixedIps1) && isSubnet(subnetIid,fixedIps2))
			 return true;
		return false;

    }

	
    public boolean isRouter(List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps1,List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps2) {
         Routers routers = getRouters();
		 RoutersBuilder routersBuilder = new RoutersBuilder(routers);
		 List<Router> router =  routersBuilder.getRouter();
		 RouterBuilder routerBuilder;
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets> subnets;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder subnetsBuilder;
		 InstanceIdentifier<?> subnetIid;

		 for (Router router_new : router) {		    
		      routerBuilder =  new RouterBuilder(router_new);		      
		      subnets = routerBuilder.getSubnets();
			  for(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets subnet_new : subnets) {
			       subnetsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder(subnet_new);
			       subnetIid = subnetsBuilder.getSubnetId().getValue();
                   if(isSubnet(subnetIid,fixedIps1,fixedIps2))
			  	      return true;
			  }	   
         }

         return false;

    }

	
	/*router is the node*/
	public boolean routerIsTheNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node node) {
         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps = portBuilder.getFixedIps();
		 
		 	
         NodeBuilder nodeBuilder = new NodeBuilder(node);
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	     List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();

		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new;
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps_new;
		 
	     for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
		      portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);	
		      fixedIps_new = portBuilder_new.getFixedIps();
			  if(isRouter(fixedIps,fixedIps_new))
			  	  return true;
		      

         }

		 return false;  
   	}

    // node switch type;
    // duicheng  or no duicheng
	public String getSwtichType(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {
         List<Node> nodeList = readNodes();
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
	     String portuuid = portBuilder.getPortUuid().getValue();
		 for(Node node : nodeList) {
		       NodeBuilder nodeBuilder = new NodeBuilder(node);
	           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
			   if(ports == null)
			   	  continue;
	           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	           List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
			   if(portList == null)
			   	  continue;
	           for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
		           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			       if(portuuid.equals(portBuilder_new.getPortUuid().getValue())) {
                        return getSwitchType(node); 
			       	 }
			 	    
	           	}
		  }	   

		  return null;
		}

    public Long getOfPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node nodeL) {
         List<Node> nodeList = readNodes();
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
	     String portuuid = portBuilder.getPortUuid().getValue();
		 NodeBuilder nodeBuilder;
		 for(Node node : nodeList) {
		       nodeBuilder = new NodeBuilder(node);
	           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
			   if(ports == null)
			   	  continue;
	           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	           List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
			   if(portList == null)
			   	  continue;
	           for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
		           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			       if(portuuid.equals(portBuilder_new.getPortUuid().getValue())) {
                        // find the node.
                        TunnelBuilder tunnelBuilder = getTunnelBuilder(nodeL,node);
					    LOG.info("vni is {}\n",tunnelBuilder.getOfport());
                        return tunnelBuilder.getOfport();
			       	 }
			 	    
	           	}
		  }	   
		
          return new Long(-1l);
    }


    public Long getOfPort(Node nodeL,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {
         List<Node> nodeList = readNodes();
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
	     String portuuid = portBuilder.getPortUuid().getValue();
		 NodeBuilder nodeBuilder;
		 for(Node node : nodeList) {
		       nodeBuilder = new NodeBuilder(node);
	           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
			   if(ports == null)
			   	  continue;
	           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	           List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
			   if(portList == null)
			   	  continue;
	           for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
		           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			       if(portuuid.equals(portBuilder_new.getPortUuid().getValue())) {
                        // find the node.
                        TunnelBuilder tunnelBuilder = getTunnelBuilder(node,nodeL);
					    LOG.info("vni is {}\n",tunnelBuilder.getOfport());
                        return tunnelBuilder.getOfport();
			       	 }
			 	    
	           	}
		  }	   
		
          return new Long(-1l);
    }
	public String getMacaddr(Node node) {
         NodeBuilder nodeBuilder = new NodeBuilder(node);
		 String dpid = nodeBuilder.getDatapathId();
		 LOG.info("---getMacaddr is {}----\n",dpid.substring(6));
		 return dpid.substring(6);
		 
	}	

    public void writeMacaddr(Node node) {
         String macaddr = getMacaddr(node);
		 NodeBuilder nodeBuilder = new NodeBuilder(node);
		 nodeBuilder.setSwitchMac(new MacAddress(macaddr));
		 InstanceIdentifier Path =  createInstanceIdentifier_dpid(nodeBuilder.getDatapathId());
         mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, Path, nodeBuilder.build());  			 
	}	

	public long getDataPathId(Node node) {
        long dpid = 0L;
		NodeBuilder nodeBuilder = new NodeBuilder(node);
        String datapathId = nodeBuilder.getDatapathId();
		/*String datapathid = datapathId.substring(6);
        if (datapathid != null) {
            dpid = new BigInteger(datapathid.replaceAll(":", ""), 16).longValue();
        }*/
        if (datapathId != null) 
            dpid = new BigInteger(datapathId.replaceAll(":", ""), 10).longValue();
        return dpid;
    }

	

    public Long getNoSymmetryVni(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {

		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 InstanceIdentifier<?> netwrokIid;
		 netwrokIid = portBuilder.getNetworkId().getValue();		 
		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network network = (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, netwrokIid);
		 org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkBuilder networkBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkBuilder(network);
		
         NetworkProviderExtension providerExtension = network.getAugmentation(NetworkProviderExtension.class);
		 Long vni = new Long(Long.parseLong(providerExtension.getSegmentationId()));
		 return vni;
    }

	public Long getSymmetryVni(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {
         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps = portBuilder.getFixedIps();
		 
		 Routers routers = getRouters();
		 RoutersBuilder routersBuilder = new RoutersBuilder(routers);
		 List<Router> router =  routersBuilder.getRouter();
		 RouterBuilder routerBuilder;
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets> subnets;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder subnetsBuilder;
		 InstanceIdentifier<?> subnetIid;

		 for (Router router_new : router) {		    
		      routerBuilder =  new RouterBuilder(router_new);		      
		      subnets = routerBuilder.getSubnets();
			  for(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets subnet_new : subnets) {
			       subnetsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder(subnet_new);
			       subnetIid = subnetsBuilder.getSubnetId().getValue();
			  	   if(isSubnet(subnetIid,fixedIps)) {
				   	    Long vni = new Long(Long.parseLong(routerBuilder.getVni()));
			            return vni;
			  	   	}	
			  	   
			  }	   
         }
		   
		   return new Long(-1l);
          
    }



   public Long getSymmetryVni(Router router,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {
         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps = portBuilder.getFixedIps();
		 
		 RouterBuilder routerBuilder;
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets> subnets;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder subnetsBuilder;
		 InstanceIdentifier<?> subnetIid;

		    
		 routerBuilder =  new RouterBuilder(router);		      
		 subnets = routerBuilder.getSubnets();
	     for(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets subnet_new : subnets) {
			 subnetsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder(subnet_new);
			 subnetIid = subnetsBuilder.getSubnetId().getValue();
			 if(isSubnet(subnetIid,fixedIps)) {
				   Long vni = new Long(Long.parseLong(routerBuilder.getVni()));
			       return vni;
			 }	
			  	   
	     	}   
		   
		   return new Long(-1l);
          
    }

	public String getPortMacaddr(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {
         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 return portBuilder.getMacAddress().getValue();           
	}

    public Long getofPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {
         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 if(portBuilder.getOfport() == null)
		 	return new Long(-1L);
		 return portBuilder.getOfport();         
	}

	public long getDpid(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {
         List<Node> nodeList = readNodes();
		 int flag = 0;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
	     String portuuid = portBuilder.getPortUuid().getValue();
		 if(portuuid != null) {
              // no
              flag = 1;

		 	}else {
                  InstanceIdentifier<?> portid;
		          portid = portBuilder.getPortId().getValue();
		          LOG.info("----------getDpid----portiid is {}\n",portid);
			      org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port port1 = (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port ) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, portid);
                  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder portbuilder1 = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder(port1);
			      portuuid = portbuilder1.getUuid().getValue();
				  flag = 2;

			}
		     NodeBuilder nodeBuilder;
		     for(Node node : nodeList) {
		           nodeBuilder = new NodeBuilder(node);
	               org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
			       if(ports == null)
			   	      continue;
	               org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	               List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
	               for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
		                org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
                       if(flag == 1) {
						   if(portuuid.equals(portBuilder_new.getPortUuid().getValue())) {
                               // find the node.
                               return getDataPathId(node);
			       	      }
                       	}
					   if(flag == 2) {
                               InstanceIdentifier<?> portid;
		                       portid = portBuilder_new.getPortId().getValue();
		                       LOG.info("----------getDpid----portiid is {}\n",portid);
			                   org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port port1 = (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port ) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, portid);
                               org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder portbuilder1 = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder(port1);
			                   String port_uuid = portbuilder1.getUuid().getValue();
							   if(portuuid.equals(port_uuid)) {
                                   return getDataPathId(node); 

							   }


					   }
			 	    
	           	 }
		     }
		
          return new Long(-1l);
    }

    public String getMacaddr(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {
         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 return portBuilder.getMacAddress().getValue();         

    }		

	public String getNetworkUUID(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {

		  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		  InstanceIdentifier<?> netwrokIid;
		  netwrokIid = portBuilder.getNetworkId().getValue();
		  LOG.info("netwrokIid is {}\n",netwrokIid);
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network network = (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.Network) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, netwrokIid);
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkBuilder networkBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.networks.rev150712.networks.attributes.networks.NetworkBuilder(network);
		  String networkuuid = networkBuilder.getUuid().getValue();
		  return networkuuid;

	}	

   public String getRouterUUID(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {

         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps = portBuilder.getFixedIps();
		 
		 Routers routers = getRouters();
		 if(routers == null)
		 	return null;
		 RoutersBuilder routersBuilder = new RoutersBuilder(routers);
		 List<Router> router =  routersBuilder.getRouter();
		 RouterBuilder routerBuilder;
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets> subnets;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder subnetsBuilder;
		 InstanceIdentifier<?> subnetIid;

		 for (Router router_new : router) {		    
		      routerBuilder =  new RouterBuilder(router_new);		      
		      subnets = routerBuilder.getSubnets();
			  for(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets subnet_new : subnets) {
			       subnetsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder(subnet_new);
			       subnetIid = subnetsBuilder.getSubnetId().getValue();
			  	   if(isSubnet(subnetIid,fixedIps))
			            return routerBuilder.getRouterId().getValue();
			  	   
			  }	   
         }

         return null;

	}	

	public List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> getSameNetworkPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node node)  {
		  String networkUuid = getNetworkUUID(port);
		  
          NodeBuilder nodeBuilder = new NodeBuilder(node);
		  List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> listPort = new ArrayList<>();
	      org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
	      org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	      List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
	      for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
		       org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			   if(networkUuid.equals(getNetworkUUID(portBuilder_new.build()))) {
                      listPort.add(portBuilder_new.build());         
			    }
	      }

		  return listPort;
	 } 	
	
	 public List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> getSubnetPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets subnet1) {
	 	
          org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder subnetsBuilder1 = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder(subnet1);
		  InstanceIdentifier<?> subnetIid = subnetsBuilder1.getSubnetId().getValue();

		  /*org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.Subnets subnet = (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.Subnets)mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, subnetIid);
          org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.SubnetsBuilder subnetsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.SubnetsBuilder(subnet);
		  List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.subnets.Subnet> subnet2 = subnetsBuilder.getSubnet();
          String subnetUuid = null;
		  for(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.subnet.attributes.subnets.Subnet subnet_temp : subnet2) {
              SubnetBuilder subnetBuilder = new SubnetBuilder(subnet_temp);
			  subnetUuid = subnetBuilder.getSubnetUuid().getValue();
			  break;
		  }	*/
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet subnet = (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, subnetIid);
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetBuilder subnetBuiulder = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetBuilder(subnet);
		  String subnetUuid = null;
		  subnetUuid = subnetBuiulder.getUuid().getValue();
		  LOG.info("getSubnetPort subnetuuid is {}\n",subnetUuid);

		  List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> listPort = new ArrayList<>();
		  //List<Node> nodeList = readSwitchNodes();
		  List<Node> nodeList = readNodes();
		  for(Node node : nodeList) {
              NodeBuilder nodeBuilder = new NodeBuilder(node);
	          org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
			  if(ports == null)
			   	   continue;
	          org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	          List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
	          for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
		          org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			      if(subnetUuid.equals(getSubnetUUID(portBuilder_new.build()))) {
                      listPort.add(portBuilder_new.build());         
			      }
	           }
		  }
		  return listPort;

	 }

	 
	 public List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> getSameRouterPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node node) {
          String routerUuid = getRouterUUID(port);
		  if(routerUuid == null)
		  	  return null;
		  
          NodeBuilder nodeBuilder = new NodeBuilder(node);
		  List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> listPort = new ArrayList<>();
	      org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
	      org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	      List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
	      for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
		       org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			   if(routerUuid.equals(getRouterUUID(portBuilder_new.build()))) {
                      listPort.add(portBuilder_new.build());         
			    }
	      }

		  return listPort;

	 }


   	public Node getNode(String ipaddr) {
         List<Node> nodeList = readNodes();
		 NodeBuilder nodeBuilder;
		 for(Node node : nodeList) {
		       nodeBuilder = new NodeBuilder(node);
	           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
			   if(ports == null)
			   	  continue;
	           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	           List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
			   if(portList == null)
			   	  continue; 
	           for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
			       if(ipaddr.equals(getIp(port_new))) {
                      LOG.info("getNode ipaddr is {},node is {}\n",ipaddr,node);
					  return node;

			       }
			       
			 	    
	           	}
		  }	   
		
          return null;
	}

	public Node getNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {
         List<Node> nodeList = readNodes();
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
	     String portuuid = portBuilder.getPortUuid().getValue();
		 NodeBuilder nodeBuilder;
		 for(Node node : nodeList) {
		       nodeBuilder = new NodeBuilder(node);
	           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
			   if(ports == null)
			   	  continue;
	           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	           List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
			   if(portList == null)
			   	  continue; 
	           for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {		    
		           org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			       if(portuuid.equals(portBuilder_new.getPortUuid().getValue())) {
                        return node;
			       	 }
			 	    
	           	}
		  }	   
		
          return null;

	}

	public Long getOfPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port1,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port2) {
        Node node1 = getNode(port1);
		Node node2 = getNode(port2);
		TunnelBuilder tunnelBuilder = getTunnelBuilder(node1,node2);
		LOG.info("getofPort is {}\n",tunnelBuilder.getOfport());
        return tunnelBuilder.getOfport();
	 }	

	public Long getOfPort(Node node1,Node node2){

		TunnelBuilder tunnelBuilder = getTunnelBuilder(node1,node2);
		LOG.info("getofPort is {}\n",tunnelBuilder.getOfport());
        return tunnelBuilder.getOfport();

	}

   public Long getOfGWPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port1){

        Node node1 = getNode(port1);
		TunnelBuilder tunnelBuilder = getTunnelBuilder(node1);
		LOG.info("getofPort is {}\n",tunnelBuilder.getOfport());
        return tunnelBuilder.getOfport();

	}

   public Long getOfGWPort(Node node){
   	
		TunnelBuilder tunnelBuilder = getTunnelBuilder(node);
		LOG.info("getofPort is {}\n",tunnelBuilder.getOfport());
        return tunnelBuilder.getOfport();

	}
   
	public boolean isSameTheNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port1,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port2) {

		Node node1 = getNode(port1);
		Node node2 = getNode(port2);
		LOG.info("------node1 is {},node2 is {}----------",node1,node2);
		NodeBuilder nodeBuilder1 = new NodeBuilder(node1);
		NodeBuilder nodeBuilder2 = new NodeBuilder(node2);
		String node1Dpid = nodeBuilder1.getDatapathId();
		String node2Dpid = nodeBuilder2.getDatapathId();
		if(node1Dpid.equals(node2Dpid))
			return true;
		return false;

	}	

    public boolean isSameTheNode(Node node,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {

	    Node node2 = getNode(port);
		NodeBuilder nodeBuilder1 = new NodeBuilder(node);
		NodeBuilder nodeBuilder2 = new NodeBuilder(node2);
		String node1Dpid = nodeBuilder1.getDatapathId();
		String node2Dpid = nodeBuilder2.getDatapathId();
		if(node1Dpid.equals(node2Dpid))
			return true;
		return false;
		  

    }

	public boolean isSameTheNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,Node node) {

	    Node node2 = getNode(port);
		NodeBuilder nodeBuilder1 = new NodeBuilder(node);
		NodeBuilder nodeBuilder2 = new NodeBuilder(node2);
		String node1Dpid = nodeBuilder1.getDatapathId();
		String node2Dpid = nodeBuilder2.getDatapathId();
		if(node1Dpid.equals(node2Dpid))
			return true;
		return false;
		  

    }

	
	public String getGatewayMac(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {

		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps = portBuilder.getFixedIps();
		 
		 Routers routers = getRouters();
		 RoutersBuilder routersBuilder = new RoutersBuilder(routers);
		 List<Router> router =  routersBuilder.getRouter();
		 RouterBuilder routerBuilder;
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets> subnets;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder subnetsBuilder;
		 InstanceIdentifier<?> subnetIid;

		 for (Router router_new : router) {		    
		      routerBuilder =  new RouterBuilder(router_new);		      
		      subnets = routerBuilder.getSubnets();
			  for(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets subnet_new : subnets) {
			       subnetsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder(subnet_new);
			       subnetIid = subnetsBuilder.getSubnetId().getValue();
			  	   if(isSubnet(subnetIid,fixedIps)) {
				   	    return subnetsBuilder.getMacaddr().getValue();
			  	   	}	
			  	   
			  }	   
         }
		   
		 return null;
		 

	}	


	public String getGatewayMac(Router router,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {

		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps = portBuilder.getFixedIps();

		 
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets> subnets;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder subnetsBuilder;
		 InstanceIdentifier<?> subnetIid;
	    
		 RouterBuilder routerBuilder =  new RouterBuilder(router);		      
		 subnets = routerBuilder.getSubnets();
		 for(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.Subnets subnet_new : subnets) {
			       subnetsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.router.attributes.routers.router.SubnetsBuilder(subnet_new);
			       subnetIid = subnetsBuilder.getSubnetId().getValue();
			  	   if(isSubnet(subnetIid,fixedIps)) {
				   	    return subnetsBuilder.getMacaddr().getValue();
			  	   	}	
			  	   
			  }	   
		   
		 return null;
		 

	}	

	public String getIp(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {
         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIpsBuilder fixedIpsBuilder;
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps = portBuilder.getFixedIps();

		 for(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps mdIP : fixedIps) {
             fixedIpsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIpsBuilder(mdIP);
			 return String.valueOf(fixedIpsBuilder.getIpAddress().getValue());
		 }
		 
		 return null;
	}


    public boolean  isLastNetworkPort(Node node,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port)  {
       	 String networkUuid = getNetworkUUID(port);

		 NodeBuilder nodeBuilder;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 String portuuid = portBuilder.getPortUuid().getValue();

		 nodeBuilder = new NodeBuilder(node);
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		 if(ports == null)
			return false;
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	     List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
		 if(portList == null)
			return false;
	     for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {			
			 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			 if((networkUuid.equals(getNetworkUUID(portBuilder_new.build())))&&(!portuuid.equals(portBuilder_new.getPortUuid().getValue()))) {
			   	    return false;
				     
			   }	

	     	}

		  return true;
      } 

	  public boolean  isLastNetworkPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type)  {
       	 String networkUuid = getNetworkUUID(port);

         List<Node> nodeList;
		 if(type == 0) {
		 	 nodeList = readNodes();
	     } else if(type == 1) {
		 	nodeList = readSwitchNodes();
		 } else if(type == 2) {
		 	nodeList = readOvsNodes(); 
		 } else {	
             nodeList = readNodes();
		 }
		 
		 NodeBuilder nodeBuilder;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 String portuuid = portBuilder.getPortUuid().getValue();

		 for(Node node : nodeList) {
		     nodeBuilder = new NodeBuilder(node);
	         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		     if(ports == null)
			      continue;
	         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	         List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
		     if(portList == null)
			     continue;
	         for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {			
			     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			      if((networkUuid.equals(getNetworkUUID(portBuilder_new.build())))&&(!portuuid.equals(portBuilder_new.getPortUuid().getValue()))) {
			   	      return false;
				     
			   }	

	     	}
		  }
		  return true;
      } 
		 
	public List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> getSameNetworkPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type)  {
		 String networkUuid = getNetworkUUID(port);
		 List<Node> nodeList;
		 if(type == 0) {
		 	 nodeList = readNodes();
	     } else if(type == 1) {
		 	nodeList = readSwitchNodes();
		 } else if(type == 2) {
		 	nodeList = readOvsNodes(); 
		 } else {	
             nodeList = readNodes();
		 }
		 
		 NodeBuilder nodeBuilder;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 String portuuid = portBuilder.getPortUuid().getValue();
		 
		 List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> listPort = new ArrayList<>();
		 LOG.info("---getSameNetworkPort---log1----\n"); 
		 for(Node node : nodeList) {
		    nodeBuilder = new NodeBuilder(node);
	        org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		    if(ports == null)
				continue;
			LOG.info("---getSameNetworkPort---log2----\n"); 
	        org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	        List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
			if(portList == null)
				 continue;
			LOG.info("---getSameNetworkPort,portuuid is----\n");
	        for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {			
			   org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			   LOG.info("---addport uuid is {},same netwok uuid is {}\n",portuuid,portBuilder_new.getPortUuid().getValue());
			   if((networkUuid.equals(getNetworkUUID(portBuilder_new.build())))&&(!portuuid.equals(portBuilder_new.getPortUuid().getValue()))) {
			   	    LOG.info("---getSameNetworkPort2----\n");
				    listPort.add(portBuilder_new.build());		 
			   }
	        }
		 }	
	
	     return listPort;
	 }	
	/*
		 
    public boolean isLastNetworkPort(Node node,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port)  {
		 String networkUuid = getNetworkUUID(port);
		 NodeBuilder nodeBuilder;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 String portuuid = portBuilder.getPortUuid().getValue();
		 
		 nodeBuilder = new NodeBuilder(node);
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		 if(ports == null)
			 return true;
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	     List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
		 if(portList == null)
			return true;
	     for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {			
			   org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			   if((networkUuid.equals(getNetworkUUID(portBuilder_new.build())))&&(!portuuid.equals(portBuilder_new.getPortUuid().getValue()))) {
			   	    return false;
				     
			   }
		 }	
	
	     return true;
	 }	*/
	 
	public List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> getSameRouterPort(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type) {
	      String routerUuid = getRouterUUID(port);
		  if(routerUuid == null)
			  return null;
		  List<Node> nodeList;
          if(type == 0) {
		 	  nodeList = readNodes();
	      } else if(type == 1) {
		 	  nodeList = readSwitchNodes();
		  } else if(type == 2) {
		 	 nodeList = readOvsNodes(); 
		  } else {
             nodeList = readNodes();
		  }
		  NodeBuilder nodeBuilder;
		  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		  String portuuid = portBuilder.getPortUuid().getValue();
		  String networkUuid2 = getNetworkUUID(port);
		  
		  List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> listPort = new ArrayList<>();
		  for(Node node : nodeList) {
		      nodeBuilder = new NodeBuilder(node);
	          org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
			  if(ports == null)
			  	continue;
	          org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	          List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
			  if(portList == null)
			  	 continue;
	          for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {			
			      org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
				  String networkUuid1 = getNetworkUUID(port_new);				  
			      if((routerUuid.equals(getRouterUUID(portBuilder_new.build())))&&(!portuuid.equals(portBuilder_new.getPortUuid().getValue()))&&(!networkUuid1.equals(networkUuid2))) {
				     listPort.add(portBuilder_new.build());		 
			      }
	          }
		  }	
	
	     return listPort; 
			
	
		 }


		public List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> getSameRouterPort(Router router,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type) {
	      //String routerUuid = getRouterUUID(port);
	      RouterBuilder routerBuilder =  new RouterBuilder(router);
	      String routerUuid = routerBuilder.getRouterId().getValue();
		  if(routerUuid == null)
			  return null;
		  List<Node> nodeList;
          if(type == 0) {
		 	  nodeList = readNodes();
	      } else if(type == 1) {
		 	  nodeList = readSwitchNodes();
		  } else if(type == 2) {
		 	 nodeList = readOvsNodes(); 
		  } else {
             nodeList = readNodes();
		  }
		  NodeBuilder nodeBuilder;
		  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		  String portuuid = portBuilder.getPortUuid().getValue();
		  String networkUuid2 = getNetworkUUID(port);
		  
		  List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> listPort = new ArrayList<>();
		  for(Node node : nodeList) {
		      nodeBuilder = new NodeBuilder(node);
	          org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
			  if(ports == null)
			  	continue;
	          org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	          List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
			  if(portList == null)
			  	 continue;
	          for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {			
			      org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
				  String networkUuid1 = getNetworkUUID(port_new);				  
			      if((routerUuid.equals(getRouterUUID(portBuilder_new.build())))&&(!portuuid.equals(portBuilder_new.getPortUuid().getValue()))&&(!networkUuid1.equals(networkUuid2))) {
				     listPort.add(portBuilder_new.build());		 
			      }
	          }
		  }	
	
	     return listPort; 
			
	
		 }

     public boolean isLastrouterofNode(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type) {

	     String routerUuid = getRouterUUID(port);
		 NodeBuilder nodeBuilder;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 String portuuid = portBuilder.getPortUuid().getValue();

		 List<Node> nodeList;
         if(type == 0) {
		 	  nodeList = readNodes();
	      } else if(type == 1) {
		 	  nodeList = readSwitchNodes();
		  } else if(type == 2) {
		 	 nodeList = readOvsNodes(); 
		  } else {
             nodeList = readNodes();
		  }

		 for(Node node : nodeList) { 
		     nodeBuilder = new NodeBuilder(node);
	         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		     if(ports == null)
			     continue;
	         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	         List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
		     if(portList == null)
		     	 continue;
	         for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {			
			      org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			      if((routerUuid.equals(getRouterUUID(portBuilder_new.build())))&&(!portuuid.equals(portBuilder_new.getPortUuid().getValue()))) {
			   	      return false;
				     
			       }
	         	}	
			 }
	
	     return true;
		   
	 }	

	  public boolean isLastrouterofNode(Router router,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port,int type) {

	     RouterBuilder routerBuilder =  new RouterBuilder(router);
	     String routerUuid = routerBuilder.getRouterId().getValue();
		 NodeBuilder nodeBuilder;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 String portuuid = portBuilder.getPortUuid().getValue();

		 List<Node> nodeList;
         if(type == 0) {
		 	  nodeList = readNodes();
	      } else if(type == 1) {
		 	  nodeList = readSwitchNodes();
		  } else if(type == 2) {
		 	 nodeList = readOvsNodes(); 
		  } else {
             nodeList = readNodes();
		  }

		 for(Node node : nodeList) { 
		     nodeBuilder = new NodeBuilder(node);
	         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		     if(ports == null)
			     continue;
	         org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	         List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
		     if(portList == null)
		     	 continue;
	         for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {			
			      org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			      if((routerUuid.equals(getRouterUUID(portBuilder_new.build())))&&(!portuuid.equals(portBuilder_new.getPortUuid().getValue()))) {
			   	      return false;
				     
			       }
	         	}	
			 }
	
	     return true;
		   
	 }	
	 public boolean isLastrouterofNode(Node node,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {

	     String routerUuid = getRouterUUID(port);
		 NodeBuilder nodeBuilder;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 String portuuid = portBuilder.getPortUuid().getValue();
		 
		 nodeBuilder = new NodeBuilder(node);
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		 if(ports == null)
			 return true;
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	     List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
		 if(portList == null)
			return true;
	     for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {			
			   org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			   if((routerUuid.equals(getRouterUUID(portBuilder_new.build())))&&(!portuuid.equals(portBuilder_new.getPortUuid().getValue()))) {
			   	    LOG.info("routeruuid is {}.portuuid is {}.portBuilder_new is {}...",routerUuid,portuuid,portBuilder_new.getPortUuid().getValue());
			   	    return false;
				     
			   }
		 }	
	
	     return true;
		   
	 }	

	 
	 public boolean isLastrouterofNode(Router router,Node node,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {

	     RouterBuilder routerBuilder =  new RouterBuilder(router);
	     String routerUuid = routerBuilder.getRouterId().getValue();
		 NodeBuilder nodeBuilder;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 String portuuid = portBuilder.getPortUuid().getValue();
		 
		 nodeBuilder = new NodeBuilder(node);
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		 if(ports == null)
			 return true;
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	     List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
		 if(portList == null)
			return true;
	     for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {			
			   org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			   if((routerUuid.equals(getRouterUUID(portBuilder_new.build())))&&(!portuuid.equals(portBuilder_new.getPortUuid().getValue()))) {
			   	    return false;
				     
			   }
		 }	
	
	     return true;
		   
	 }	

     public String getSubnetUUID(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {
          InstanceIdentifier<?> subnetIid = null;
          org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		  List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps> fixedIps = portBuilder.getFixedIps();
		  for(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIps fixip : fixedIps) {
              org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIpsBuilder fixedIpsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.port.attributes.FixedIpsBuilder(fixip);
			  subnetIid = fixedIpsBuilder.getSubnetId().getValue(); 
              break;
		   }
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet subnet = (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.Subnet) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, subnetIid);
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetBuilder subnetBuiulder = new org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.subnets.rev150712.subnets.attributes.subnets.SubnetBuilder(subnet);
		  String subnetuuid = subnetBuiulder.getUuid().getValue();
		  return subnetuuid;

     }


	 
	 public boolean isLastSubnetPort(Node node,org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {

		 String subnetUuid = getSubnetUUID(port);
		 NodeBuilder nodeBuilder;
		 org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		 String portuuid = portBuilder.getPortUuid().getValue();
		 
		 nodeBuilder = new NodeBuilder(node);
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.Ports ports = nodeBuilder.getPorts();
		 if(ports == null)
			 return true;
	     org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder portsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.PortsBuilder(ports);
	     List<org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port> portList = portsBuilder.getPort();
		 if(portList == null)
			return true;
	     for (org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port_new : portList) {			
			   org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder_new =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port_new);
			   if((subnetUuid.equals(getSubnetUUID(portBuilder_new.build())))&&(!portuuid.equals(portBuilder_new.getPortUuid().getValue()))) {
			   	    return false;
				     
			   }
		 }	
	
	     return true;  


	 	}		


	 public org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port getPortFromUUID(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {

		  org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder2 =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port);
		  InstanceIdentifier<?> portid;
		  portid = portBuilder2.getPortId().getValue();
		  LOG.info("portiid is {}\n",portid);
		  org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port port1 = (org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port ) mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, portid);
          return port1;
		 
	 	}


	public List<String> getSecurityGroup(org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.Port port) {

            
            org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder portBuilder2 =  new org.opendaylight.yang.gen.v1.urn.opendaylight.topo.rev170914.logictopo.nodes.node.ports.PortBuilder(port); 
            List<Uuid> slist = new ArrayList<Uuid>();
			List<String> rlist = new ArrayList<String>();
			slist.addAll(portBuilder2.getSecurityGroups());
			for(Uuid uid : slist){
				rlist.add(uid.getValue());

			}

			return rlist;

	}

	   
  
}

   





