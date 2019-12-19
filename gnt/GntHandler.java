/*
 * Copyright (c) 2017 CMCC, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.gnt.netvirt.gntimpl;


import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.gnt.netvirt.translator.crud.INeutronNetworkCRUD;
import java.util.Map.Entry;

import org.opendaylight.gnt.netvirt.ConfigInterface;

import org.opendaylight.gnt.netvirt.api.Action;
import org.opendaylight.gnt.netvirt.api.BridgeConfigurationManager;
import org.opendaylight.gnt.netvirt.api.ConfigurationService;
import org.opendaylight.gnt.netvirt.api.Constants;
import org.opendaylight.gnt.netvirt.api.EventDispatcher;
import org.opendaylight.gnt.netvirt.api.NetworkingProviderManager;
import org.opendaylight.gnt.netvirt.api.OvsdbInventoryListener;
import org.opendaylight.gnt.netvirt.api.OvsdbInventoryService;
import org.opendaylight.gnt.netvirt.api.Southbound;
import org.opendaylight.gnt.netvirt.api.TenantNetworkManager;
import org.opendaylight.gnt.netvirt.translator.NeutronNetwork;
import org.opendaylight.gnt.netvirt.translator.NeutronPort;

//add for openflow.
import org.opendaylight.gnt.netvirt.api.TTPTable0Provider;
import org.opendaylight.gnt.netvirt.api.TTPTable10Provider;
import org.opendaylight.gnt.netvirt.api.TTPTable20Provider;
import org.opendaylight.gnt.netvirt.api.TTPTable30Provider;
import org.opendaylight.gnt.netvirt.api.TTPTable50Provider;
import org.opendaylight.gnt.netvirt.api.TTPTable60Provider;
import org.opendaylight.gnt.netvirt.api.TTPTable61Provider;
import org.opendaylight.gnt.netvirt.api.GroupProvider;

import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.provider.ext.rev150712.NetworkProviderExtension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.provider.ext.rev150712.NetworkProviderExtensionBuilder;
import org.opendaylight.netvirt.utils.servicehelper.ServiceHelper;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;



import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;

import org.opendaylight.gnt.netvirt.translator.NeutronNetwork;
import org.opendaylight.gnt.netvirt.translator.NeutronPort;
import org.opendaylight.gnt.netvirt.translator.NeutronSecurityRule;


import java.util.List;
import java.util.concurrent.ExecutionException;

import java.util.ArrayList;
import java.util.Iterator;

import java.net.UnknownHostException;

import org.opendaylight.gnt.netvirt.AbstractEvent;
import org.opendaylight.gnt.netvirt.AbstractHandler;

import org.opendaylight.gnt.netvirt.api.EgressAclProvider;

import java.math.BigInteger;

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
import org.opendaylight.gnt.utils.gnt.utils.*;
import org.opendaylight.gnt.netvirt.translator.crud.INeutronNetworkCRUD;
import org.opendaylight.gnt.netvirt.translator.crud.INeutronSecurityRuleCRUD;


import java.util.concurrent.locks.*;

import org.opendaylight.gnt.netvirt.translator.crud.impl.*;
import org.opendaylight.gnt.utils.portspeaker.*;

/*
 * @author cmcc
 */
public class GntHandler extends AbstractHandler 
           implements ConfigInterface,GntInventoryListener  {

    private static final Logger LOG = LoggerFactory.getLogger(GntHandler.class);

    // The implementation for each of these services is resolved by the OSGi Service Manager
    private volatile ConfigurationService configurationService;
    private volatile TenantNetworkManager tenantNetworkManager;
    private volatile NetworkingProviderManager networkingProviderManager;
    private volatile NodeCacheManager gtnNodeCacheManager;

	private volatile GntOp gntOp;
    private volatile GntInventoryService gntInventoryService;

    private volatile   TTPTable0Provider tTPTable0Provider;
	private volatile   TTPTable10Provider tTPTable10Provider;
	private volatile   TTPTable20Provider tTPTable20Provider;
	private volatile   TTPTable30Provider tTPTable30Provider;
	private volatile   TTPTable50Provider tTPTable50Provider;
	private volatile   TTPTable60Provider tTPTable60Provider;
	private volatile   TTPTable61Provider tTPTable61Provider;
	private volatile   GroupProvider groupProvider;

	private Map<String, Node> nodeCache = new ConcurrentHashMap<>();
	
    private volatile GntUtils gntUtils;
	private volatile IniFile inifile;
	
	private volatile NodeConnectorInventoryEventTranslator nodeConnectorInventoryEventTranslator;

    private volatile INeutronNetworkCRUD neutronNetworkCache;

	private volatile INeutronSecurityRuleCRUD neutronSecurityRuleCache;

	private static Long groupid = 2000L;
	private static Long groupL3id = 0x20000000L;
	// key is dpid + vni,value is groupid... 
	private static Map<String, Long> dpidGroupCache = new ConcurrentHashMap<>();
	private static Map<String, Long> groupCache = new ConcurrentHashMap<>();
	private static Lock lock = new ReentrantLock();

    //protocol number...
	public static final short ICMP_SHORT = 1;
    public static final short TCP_SHORT = 6;
    public static final short UDP_SHORT = 17;
    public static final short SCTP_SHORT = 132;

	public GntHandler() {
    }
	

	private GntEvent.Type gntTypeToGntEventType(GntType gntType) {
        GntEvent.Type type = GntEvent.Type.Node;

        switch (gntType) {
            case node:
                type = GntEvent.Type.Node;
				LOG.info("gntTypeToGntEventType type is {}.",type);
                break;
		    case port:
				type = GntEvent.Type.Port;
				LOG.info("gntTypeToGntEventType type is {}.",type);
				break;
			case router:
				type = GntEvent.Type.Router;
				LOG.info("gntTypeToGntEventType type is {}.",type);
				break;	
			case routerinterface:
				type = GntEvent.Type.RouterInterface;
				LOG.info("gntTypeToGntEventType type is {}.",type);
				break;	
            default:
                LOG.warn("Invalid type: {}", gntType);
                break;
        }
        return type;
    }



	public void nodeUpdate(Node node, DataObject resourceAugmentationDataChanges,
            GntInventoryListener.GntType gntType, Action action) {
		 enqueueEvent(new GntEvent(node,resourceAugmentationDataChanges,gntTypeToGntEventType(gntType), action));
    }


	public void routerUpdate(Router router, DataObject resourceAugmentationDataChanges,
            GntInventoryListener.GntType gntType, Action action) {
		 enqueueEvent(new GntEvent(router,resourceAugmentationDataChanges,gntTypeToGntEventType(gntType), action));
    }

    /*
     * Process the event.
     *
     * @param abstractEvent the {@link AbstractEvent} event to be handled.
     * @see EventDispatcher
     */

    public void processEvent(AbstractEvent abstractEvent) {
        if (!(abstractEvent instanceof GntEvent)) {
            LOG.error("processEvent Unable to process abstract event : {}", abstractEvent);
            return;
        }
        GntEvent ev = (GntEvent) abstractEvent;
        LOG.info("processEvent : {} for TransactionId : {}", ev, ev.getTransactionId());
        switch (ev.getType()) {
            case Node:
                processNodeEvent(ev);
				LOG.info("----processEvent node-------------");
                break;
            case Port:
                processPortEvent(ev);
				LOG.info("----processEvent port-------------");
                break;
			case Router:
				processRouterEvent(ev);
				LOG.info("----processEvent Router-------------");
				break;
			case RouterInterface:
				processRouterInterfaceEvent(ev);
				LOG.info("----processEvent RouterInterface-------------");
				break;
            default:
                LOG.warn("Unable to process type : {} action : {} for node : {}", ev.getType(), ev.getAction(), ev.getNode());
                break;
        }
        LOG.info("processEvent exit : {} for TransactionId : {}", ev, ev.getTransactionId());
    }

    private void processNodeEvent(GntEvent ev) {
        switch (ev.getAction()) {
            case ADD:
                processNodeCreate(ev.getNode());
                break;
            case UPDATE:
                processNodeUpdate(ev.getNode());
                break;
            case DELETE:
                processNodeDelete(ev.getNode());
                break;
        }
    }
    
   public static Long getValue(Map<String, Long> map,String key) {
         Long max = 0L;
         for (Entry<String, Long> entry : map.entrySet()) {
		 	if(entry.getKey().contains(key)) {
                   max = entry.getValue();
				   return max;
            }
         }
		 return -1L;
   	}

	
	public List<Node> getNodes() {
          List<Node> nodes = Lists.newArrayList();
          for (Node node : nodeCache.values()) {
              nodes.add(node);
          }
	    LOG.info("getNodesnodes is {}\n",nodes); 
        return nodes;		   

	}	
	
    private void processNodeCreate(Node node) {
        LOG.info(" beforeprocessttp Node Create : {} ", node); 
	    //gntOp.writeMacaddr(node);
		//gntOp.addtunnel(node);
		//start to add vxlan tunnel..
		//start to default openflow...
		//start to get switch macaddr....
		// if node is switch....
		String sw = "switch";
		if(sw.equals(gntOp.getTypeNode(node))) {
		    programSwitchAdd(node);
			
		    Long dpid = new Long(gntOp.getDataPathId(node));
			dpidGroupCache.put(dpid.toString(),groupid);
            //lldap-speak send lldp....
		}
		LOG.info("---xxxxx---"); 
		LOG.info("dpidnodecache is {}\n",dpidGroupCache);
		/*
		lock.lock();
		
		Long dpidlocal = new Long(gntOp.getDataPathId(node));
		nodeCache.put(dpidlocal.toString(),node);
        List <NeutronNetwork> networks;
		List<Node> nodes = getNodes();
		LOG.info("processNodeCreatenode node is {} shenxiaonodes is {}\n",node,nodes);
		networks = neutronNetworkCache.getAllNetworks();
		LOG.info("processNodeCreatenetworks is {}\n",networks);
		if (neutronNetworkCache != null) {
            Long groupidtemp = groupid;
			 if (!networks.isEmpty()) {
  			 	   for (NeutronNetwork nk : networks)  {   
                         String vni = nk.getProviderSegmentationID();
						 dpidGroup.put("out" + dpidlocal.toString() + vni , groupidtemp);
						 groupidtemp++;
						 LOG.info("dipdlocalvni is key {},value is {}\n",dpidlocal.toString() + vni,groupidtemp);

			 	   	}
			  }

		}
		*//*
        if (neutronNetworkCache != null) {
            if (!networks.isEmpty()) {
                  LOG.info("write dpidGroup!");
				  Long groupidtemp = getMaxValue(dpidGroup,"out" + dpidlocal.toString()) + 1;;
				  for(Node noderemote : nodes) {
					  	 if(!nodes.isEmpty()) {
					  	     for (NeutronNetwork nk : networks)  {                      
                                String vni = nk.getProviderSegmentationID();
					  	        Long dpidremote = new Long(gntOp.getDataPathId(noderemote));
								LOG.info("1dpidlocal is {},dpidremote is {}\n",dpidlocal,dpidremote);*/
						        //if(/*!(dpidremote.toString().equal(dpidlocal.toString()))*/true) {
						      /*  if(true) {
									String dpidremotestring = dpidremote.toString();
									String dpidlocalstring = dpidlocal.toString();
									LOG.info("1dpidremotestring is {},dpidlocalstring is {}\n",dpidremotestring,dpidlocalstring);
									if(!dpidremotestring.equals(dpidlocalstring)) {
					                    String groupkey1 = "out" + dpidlocal.toString() + dpidremote.toString() + vni;
								        groupidtemp++;
					                    LOG.info("dpid is {},groupkey1 is {},groupvalue is {}",dpidlocal.toString(), groupkey1, groupidtemp);
								        // insert map..
								        dpidGroup.put(groupkey1,groupidtemp);
								        String groupkey2 = "in"  + dpidlocal.toString() + dpidremote.toString()  + vni;
								        groupidtemp++;
									    LOG.info("dpid is {},groupkey1 is {},groupvalue is {}",dpidlocal.toString(),groupkey2,groupidtemp);
								        dpidGroup.put(groupkey2,groupidtemp);
									}	
						        }  
						   	}		  
					  	} 
				   }
            	}
        	}
		   *//*
            for(Node noderemote : nodes) {
				if(!nodes.isEmpty()) {
				  Long dpidremote = new Long(gntOp.getDataPathId(noderemote));
				  if(true) {
						String dpidremotestring = dpidremote.toString();
						String dpidlocalstring = dpidlocal.toString();
					    LOG.info("2dpidremotestring is {},dpidlocalstring is {}\n",dpidremotestring,dpidlocalstring);
						if(!dpidremotestring.equals(dpidlocalstring)) {
				            LOG.info("2dpidlocal is {},dpidremote is {}\n",dpidlocal,dpidremote);
				            if(!dpidremote.toString().equals(dpidlocal.toString())) {
					   	       if (!networks.isEmpty()) {
							  	   for (NeutronNetwork nk : networks) {
							  	       String vni = nk.getProviderSegmentationID();
					                   String groupkey1 = "out" + dpidremote.toString() + dpidlocal.toString() + vni;
								       Long groupidtemp = getMaxValue(dpidGroup,"out" + dpidremote.toString()) + 1;
					                   LOG.info("dpid id {},groupkey2 is {},groupkey is {},groupvalue is {}",dpidremote.toString(),groupkey1,groupidtemp);
								       // insert map..
								       dpidGroup.put(groupkey1,groupidtemp);
									   String groupkey2 = "in"  +  dpidremote.toString() + dpidlocal.toString() + vni;
									   groupidtemp = getMaxValue(dpidGroup,"in" + dpidremote.toString()) + 1;
									   LOG.info("dpid is {},groupkey2 is {},groupvalue is {}",dpidremote.toString(),groupkey2,groupidtemp);
								       dpidGroup.put(groupkey2,groupidtemp);
									   
							  	     }
					   	       }		  
					  	    } 
             	       }
                   }	
				}
            }	
		     lock.unlock();
		     LOG.info("dpidGroup is {}",dpidGroup);
			 */
		
		
		}

    private void processNodeUpdate(Node node) {
        LOG.info("processttp Node Update : {} ", node);
        gtnNodeCacheManager.nodeAdded(node);
    }

    private void processNodeDelete(Node node) {
        LOG.info("processttp Node Delete : {} ", node);
		String sw = "switch";
		if(sw.equals(gntOp.getTypeNode(node))) {
            gtnNodeCacheManager.nodeRemoved(node);
		    Long dpid = new Long(gntOp.getDataPathId(node));
		    // start to delete vxlan tunnel.. 
		    //gntOp.deltunnel(node);
		    programSwitchDelete(node);
		}		
    }

    private void processPortEvent(GntEvent ev) {
        switch (ev.getAction()) {
            case ADD:
            case UPDATE:
                processPortUpdate(ev.getNode(), (Port) ev.getAugmentationData(), ev.getAction());
                break;
            case DELETE:
                processPortDelete(ev.getNode(), (Port) ev.getAugmentationData(), null);
                break;
        }
    }

    private void processPortUpdate(Node node, Port port, Action action) {
        LOG.info("processttpupdate port is : {}", port);
		String sw = "switch";
		String ovs = "ovs";
        //lock.lock();
		if(sw.equals(gntOp.getTypeNode(gntOp.getNode(port)))) {
		   LOG.info("nodeisswitch!");
		   programPortAdd(port);
		   //security group 
		   //securityGroupAdd(port);
		   String security_enable = "true";
		   if(security_enable.equals(inifile.getSecurityGroup())){
              securityGroupAdd(port); 
		   	}
		   Map<String, String> mirrorInput = new HashMap();
		   mirrorInput.putAll(inifile.getMirrorInput());
		   programMirrorAdd(port,mirrorInput);
		   
		} else {

		     //if(ovs.equals(gntOp.getTypeNode(gntOp.getNode(port)))) {
		     //if ovs node....
		    // gntUtils.addPort(gntOp.getDpid(port).toString(),gntOp.getPortFromUUID(port),ofport);
		 /*   if(gntOp.getofPort(port)==-1) {
			     LOG.info("nodeisovs1!"); 	
				 Long ofport = 0L;
		         gntUtils.addPort(Long.toString(new Long (gntOp.getDpid(port))),gntOp.getPortFromUUID(port),ofport);
			 }*/		 
		/*	if(gntOp.getofPort(port)==0) {
			     LOG.info("nodeisovs2!"); 	
		         programPortOvsAdd(port);
			 }*/
			//add ovsdb port....
	       if(ovs.equals(gntOp.getTypeNode(gntOp.getNode(port))))
		 	   programPortOvsAdd(port); 
		}

		// lock.unlock();

    }

	private void programMirrorAdd(Port port,Map<String, String> mirrorinput) {
         Long ofport =  gntOp.getofPort(port);
         
		 Long dpid = new Long(gntOp.getDpid(port));
		 String ofports = dpid.toString() + ":" + ofport.toString();
		 LOG.info("----programMirrorAdd-----ofport is {}-----",ofports);
		 Iterator iter = mirrorinput.entrySet().iterator();
         while (iter.hasNext()) {
		 	  Map.Entry entry = (Map.Entry) iter.next();
			  Object key = entry.getKey();
			  Object val = entry.getValue();
			  if(((String)key).equals(ofports)) {
                     String str = (String)val;
					 String mirorport[] = str.split(",");
					 List<Long> plist = new ArrayList<Long>();
                     for (int i = 0 ;i < mirorport.length ; i++) {                           
                             long mirport = Long.parseLong(mirorport[i]);
		                     Long mirrport = new Long(mirport);
							 LOG.info("programMirrorAdd mirrport is {}..",mirrport);
							 plist.add(mirrport);
                     }
					 LOG.info("programMirrorAdd plist is {}..",plist); 
					 tTPTable60Provider.programOutPort(dpid,ofport,plist,true);
					 //String sip = gntOp.getIp(port) + "/32";
					 //tTPTable60Provider.programOutPortIp(dpid,sip,plist,true);
                }
            
         }           
         return ;

	}	

    private void programMirrorDelete(Node node,Port port,Map<String, String> mirrorinput) {
         Long ofport =  gntOp.getofPort(port);
         //String ofports = ofport.toString();
		 Long dpid = new Long(gntOp.getDataPathId(node)); 
		 String ofports = dpid.toString() + ":" + ofport.toString();
		 LOG.info("----programMirrorDelete-----dpid is {},ofport is {}-----",dpid,ofport);
		 Iterator iter = mirrorinput.entrySet().iterator();               
         while (iter.hasNext()) {
		 	  Map.Entry entry = (Map.Entry) iter.next();
			  Object key = entry.getKey();
			  Object val = entry.getValue();
			  if(((String)key).equals(ofports)) {
                     String str = (String)val;
					 String mirorport[] = str.split(",");
					 List<Long> plist = new ArrayList<Long>();
                     for (int i = 0 ;i < mirorport.length ; i++) {                           
                             long mirport = Long.parseLong(mirorport[i]);
		                     Long mirrport = new Long(mirport);
							 LOG.info("programMirrorDelete mirrport is {}..",mirrport);
							 plist.add(mirrport);
                     }
					 LOG.info("programMirrorDelete plist is {}..",plist); 
					 tTPTable60Provider.programOutPort(dpid,ofport,plist,false);
                }
            
         }           
         return ;

	}

    private void processPortDelete(Node node, Port port, Action action) {
        LOG.info("processttpdelete node is : {},port is {}\n ", node,port);
		String sw = "switch";
		String ovs = "ovs";
		if(sw.equals(gntOp.getTypeNode(node))){
		   programPortDelete(node,port);
		   //securityGroupDelete(port);
		   String security_enable = "true";
		   if(security_enable.equals(inifile.getSecurityGroup())){
              securityGroupDelete(node,port); 
		   	}

		   Map<String, String> mirrorInput = new HashMap();
		   mirrorInput.putAll(inifile.getMirrorInput());
		   programMirrorDelete(node,port,mirrorInput);
		}else {

		   if(ovs.equals(gntOp.getTypeNode(node)))
                  programPortOvsDelete(node,port); 
		}
		
    }
	
	private void processRouterEvent(GntEvent ev) {
        switch (ev.getAction()) {
            case ADD:
                processRouterCreate(ev.getRouter());
                break;
            case UPDATE:
                processRouterUpdate(ev.getRouter());
                break;
            case DELETE:
                processRouterDelete(ev.getRouter());
                break;
        }
    }

    private void processRouterCreate(Router router) {
        LOG.info(" beforeprocessttp router Create : {} ", router); 	
				
    }

    private void processRouterUpdate(Router router) {
        LOG.info("processttp router Update : {} ", router);
    }

    private void processRouterDelete(Router router) {
        LOG.info("processttp router Delete : {} ", router);
    }


	private void processRouterInterfaceEvent(GntEvent ev) {
        switch (ev.getAction()) {
            case ADD:
            case UPDATE:
                processRouterInterfaceUpdate(ev.getRouter(), (Subnets) ev.getAugmentationData(), ev.getAction());
                break;
            case DELETE:
                processRouterInterfaceDelete(ev.getRouter(), (Subnets) ev.getAugmentationData(), null);
                break;
        }
	 }

     private void processRouterInterfaceUpdate(Router router, Subnets subnet, Action action) {
	 	LOG.info("-----test1111--------");
        LOG.info("processttpupdaterouter is {},subnet is : {} ", router,subnet);
		List<Port> subnetPortList = gntOp.getSubnetPort(subnet);
		String sw = "switch";
		String ovs = "ovs";
        for(Port port : subnetPortList) {
		   // gw openflow...
		   LOG.info("-----test2222--------");
		   if(sw.equals(gntOp.getTypeNode(gntOp.getNode(port)))) {
		   	      LOG.info("-----test3333--------");
		   	      //LOG.info("processRouterInterfaceUpdate port is {},subnet is {}\n",port,subnet);
		          //writeGwL3(port);
		          //after add gateway flow....
		          List<Port> l3PortList = gntOp.getSameRouterPort(port,1);
		          if((l3PortList.size()!=0) && (l3PortList!=null)) {
                       for(Port port2 : l3PortList) {
			 	       LOG.info("processRouterInterfaceUpdatestart1 to write L3 flow!\n");
                       writeL3Flow(port,port2);
				       writeL3Flow(port2,port);
                     }
		         }

				 
			      List<Port> l3OvsPortList = gntOp.getSameRouterPort(port,2);
		          if((l3OvsPortList.size()!=0) && (l3OvsPortList!=null)) {
                     for(Port port2 : l3OvsPortList) {
				  	    LOG.info("processRouterInterfaceUpdatestart2 to write L3 flow!\n");
			 	        LOG.info("start to write L3 flow!\n");
                        writeL3OvsFlow(port,port2);
				        writeL3OvsFlow(port2,port);
				 
                     }
                 }	
			     	  
		   	}


           if(ovs.equals(gntOp.getTypeNode(gntOp.getNode(port)))) { 
		   	    LOG.info("-----test4444--------");
		        List<Port> l3ovsPortList = gntOp.getSameRouterPort(port,1);
		        if((l3ovsPortList.size()!=0) && (l3ovsPortList!=null)) {
                    for(Port port2 : l3ovsPortList) {
			 	       LOG.info("processRouterInterfaceUpdatestart3 to write L3 flow!\n");
                       writeL3OvsFlow(port,port2);
					   writeL3OvsFlow(port2,port);
                   }
                 }
           	 }

           writeGwL3(port);
		   
        }

		 
		
		 LOG.info("write L3 flow end!\n");

     }

     private void processRouterInterfaceDelete(Router router, Subnets subnet, Action action) {
		LOG.info("processttpupdaterouter is {},subnet is : {} ", router,subnet);
		List<Port> subnetPortList = gntOp.getSubnetPort(subnet);
		LOG.info("subnetport is {}",subnetPortList);
        for(Port port : subnetPortList) {

			//find node.....
			Node node = gntOp.getNode(port);
		    LOG.info("processRouterInterfaceDeleteGW");
			deleteGwL3(router,node,port);
			LOG.info("processRouterInterfaceDelete1");
		  	List<Port> l3PortList = gntOp.getSameRouterPort(router,port,1);
			LOG.info("l3PortList is {},port is {},node is {}...",l3PortList,port,node);
			
		    if(l3PortList!=null) {
			   LOG.info("processRouterInterfaceDelete2");
			   if(l3PortList.size()!=0) {
                   for(Port port2 : l3PortList) {			   	   
			 	      LOG.info("start to delete L3 flow!\n");
				      LOG.info("processRouterInterfaceDelete3");
                      deleteRouterL3FlowEgress(router,node,port,port2);
				      deleteRouterL3FlowIngress(router,node,port2,port);
                    }
			   	}   
		     }
        	}      
		  LOG.info("delete L3 flow end!\n");

     }

	 
      private void securityGroupAdd(Port port) {

		 List<String> securityGroupList = gntOp.getSecurityGroup(port);
		 if(securityGroupList.size() == 0) {
		 	 LOG.info("securityGroupisnull!");
             return;
		  }	
		 	
		 LOG.info("111securityGroupList is {}!",securityGroupList);
		 String egress = "egress";
		 String ingress = "ingress";
		 Long dpid = new Long(gntOp.getDpid(port));
		 int flag = 0;
		 short portocol_s = 0;
		 String dip = gntOp.getIp(port) + "/32";
		 String sip = gntOp.getIp(port) + "/32";
		 //default security group....
		 LOG.info("---222securityGroupAdd---dpid is {},dip is {},sip is {}---------------",dpid,dip,sip);
		 tTPTable60Provider.ingressAclClear(dpid,dip,(short)0,true);
		 tTPTable60Provider.egressAclClear(dpid,sip,(short)0,true);
		 List<NeutronSecurityRule> neutronSecurityRule = neutronSecurityRuleCache.getAllNeutronSecurityRules();
		 if(neutronSecurityRule == null)
		 	return;
		 LOG.info("securityGroupAdd1");
	     for(String uuid1 : securityGroupList) {
		 	 for(NeutronSecurityRule nsr : neutronSecurityRule) {
			 	 LOG.info("securityGroupAdd2");
                 if(nsr.getSecurityRuleGroupID().equals(uuid1)) {
				 	 LOG.info("securityGroupAdd3");
			         //String ipv4src = gntOp.getIp(port) + "/32";			
			         
			         LOG.info("NeutronSecurityRuleis {},dpid is {}!",neutronSecurityRule,dpid);
			         if(egress.equals(nsr.getSecurityRuleDirection())) {
					 	 String ipv4src = gntOp.getIp(port) + "/32";	
                         String ipv4des = nsr.getSecurityRuleRemoteIpPrefix();
				         String protocol = nsr.getSecurityRuleProtocol();
						 int minport = -1;
						 int maxport = -1;
						 if (nsr.getSecurityRulePortMin() != null && nsr.getSecurityRulePortMax() != null) {
				              minport = nsr.getSecurityRulePortMin();
				              maxport = nsr.getSecurityRulePortMax();
						 }  
				         LOG.info("egress ipv4src is {},ipv4des is {},minport is {},maxport is {},protocol is {}!",ipv4src,ipv4des,minport,maxport,protocol);
				
				     if(protocol.equals("tcp")) {
                         flag = 6;
					     portocol_s = 6;
				     }
				     if(protocol.equals("udp")) {
                         flag = 17;
					     portocol_s = 17;
				     }
					 if(protocol.equals("icmp")) {
                         flag = 1;
						 portocol_s = 1;

					 }	
					 //tTPTable60Provider.ingressAclClear(dpid,ipv4src,ipv4des,portocol_s,true);	
				     tTPTable60Provider.egressAclOutput(dpid,ipv4src,ipv4des,portocol_s,minport,maxport,flag,true);
				     			

			  }
			 //ingress...................
			 if(ingress.equals(nsr.getSecurityRuleDirection())) {
                   String ipv4src = nsr.getSecurityRuleRemoteIpPrefix();
				   String ipv4des = gntOp.getIp(port) + "/32";
				   String protocol = nsr.getSecurityRuleProtocol();
				   int minport = -1;
				   int maxport = -1;
				   if (nsr.getSecurityRulePortMin() != null && nsr.getSecurityRulePortMax() != null) {
				              minport = nsr.getSecurityRulePortMin();
				              maxport = nsr.getSecurityRulePortMax();
				   }  
				   LOG.info("ingress ipv4src is {},ipv4des is {},minport is {},maxport is {},protocol is {}!",ipv4src,ipv4des,minport,maxport,protocol);
				
				 if(protocol.equals("tcp")) {
                      flag = 6;
					  portocol_s = 6;
				 }
				 if(protocol.equals("udp")) {
                      flag = 17;
					  portocol_s = 17;
				 }
				 if(protocol.equals("icmp")) {
                         flag = 1;
						 portocol_s = 1;

					 }	
                 //tTPTable61Provider.egressAclClear(dpid,ipv4src,ipv4des,portocol_s,true);
				 tTPTable60Provider.ingressAclOutput(dpid,ipv4src,ipv4des,portocol_s,minport,maxport,flag,true);
				 
			 	}

                 	}	
		 	 }

	     	}
      }


	  private void securityGroupDelete(Node node,Port port) {
	  	
         List<String> securityGroupList = gntOp.getSecurityGroup(port);
		 if(securityGroupList == null) {
		 	 LOG.info("securityGroupisnull!");
             return;
		  }	
		 LOG.info("333securityGroupList is {}!",securityGroupList);
		 String egress = "egress";
		 String ingress = "ingress";
		 Long dpid = new Long(gntOp.getDataPathId(node)); 
		 int flag = 0;
		 short portocol_s = 0;
		 //String dip = "0.0.0.0/32";
		 String sip = gntOp.getIp(port) + "/32";
		 String dip = gntOp.getIp(port) + "/32";
		 LOG.info("---444securityGroupAdd---dpid is {},dip is {},sip is {}---------------",dpid,dip,sip);
		 //default security group....
		 tTPTable60Provider.ingressAclClear(dpid,dip,(short)0,false);
		 tTPTable60Provider.egressAclClear(dpid,sip,(short)0,false);
		 List<NeutronSecurityRule> neutronSecurityRule = neutronSecurityRuleCache.getAllNeutronSecurityRules();
		 if(neutronSecurityRule == null)
		 	return;
		 LOG.info("securityGroupAdd1");
	     for(String uuid1 : securityGroupList) {
		 	 for(NeutronSecurityRule nsr : neutronSecurityRule) {
			 	 LOG.info("securityGroupAdd2");
                 if(nsr.getSecurityRuleGroupID().equals(uuid1)) {
				 	 LOG.info("securityGroupAdd3");
				     //Long dpid = new Long(gntOp.getDpid(port));
			         //String ipv4src = gntOp.getIp(port) + "/32";			
			         LOG.info("NeutronSecurityRuleis {},dpid is {}!",neutronSecurityRule,dpid);
			         if(egress.equals(nsr.getSecurityRuleDirection())) {
                         String ipv4src = gntOp.getIp(port) + "/32";	
                         String ipv4des = nsr.getSecurityRuleRemoteIpPrefix();
				         String protocol = nsr.getSecurityRuleProtocol();
				         int minport = -1;
						 int maxport = -1;
						 if (nsr.getSecurityRulePortMin() != null && nsr.getSecurityRulePortMax() != null) {
				              minport = nsr.getSecurityRulePortMin();
				              maxport = nsr.getSecurityRulePortMax();
						 }  
				         LOG.info("egress ipv4src is {},ipv4des is {},minport is {},maxport is {},protocol is {}!",ipv4src,ipv4des,minport,maxport,protocol);
				
				     if(protocol.equals("tcp")) {
                         flag = 6;
					     portocol_s = 6;
				     }
				     if(protocol.equals("udp")) {
                         flag = 17;
					     portocol_s = 17;
				     }
					 if(protocol.equals("icmp")) {
                         flag = 1;
						 portocol_s = 1;

					 }	
				     tTPTable60Provider.egressAclOutput(dpid,ipv4src,ipv4des,portocol_s,minport,maxport,flag,false);
				     //tTPTable60Provider.ingressAclClear(dpid,ipv4src,ipv4des,portocol_s,false);				

			  }
			 //ingress...................
			 if(ingress.equals(nsr.getSecurityRuleDirection())) {
                   String ipv4src = nsr.getSecurityRuleRemoteIpPrefix();
				   String ipv4des = gntOp.getIp(port) + "/32";
				   String protocol = nsr.getSecurityRuleProtocol();
				   int minport = -1;
				   int maxport = -1;
				   if (nsr.getSecurityRulePortMin() != null && nsr.getSecurityRulePortMax() != null) {
				         minport = nsr.getSecurityRulePortMin();
				         maxport = nsr.getSecurityRulePortMax();
				   }  
				   LOG.info("ingress ipv4src is {},ipv4des is {},minport is {},maxport is {},protocol is {}!",ipv4src,ipv4des,minport,maxport,protocol);
				
				 if(protocol.equals("tcp")) {
                      flag = 6;
					  portocol_s = 6;
				 }
				 if(protocol.equals("udp")) {
                      flag = 17;
					  portocol_s = 17;
				 }
                 if(protocol.equals("icmp")) {
                         flag = 1;
						 portocol_s = 1;

			     }	
				 tTPTable60Provider.ingressAclOutput(dpid,ipv4src,ipv4des,portocol_s,minport,maxport,flag,false);
				 //tTPTable61Provider.egressAclClear(dpid,ipv4src,ipv4des,portocol_s,false);
			 	}

                 	}	
		 	 }

	     	}
      }
	  
	 private void programSwitchAdd(Node node) {
	 	NodeBuilder nodeBuilder = new NodeBuilder(node);
		String dpIid = nodeBuilder.getDatapathId();
		//Long dpid = new Long(Long.parseLong(dpIid));
		//Long dpid = new Long(34116064366l);
		Long dpid = new Long(gntOp.getDataPathId(node));
		LOG.info("programSwitchAdd dpid is {}\n",dpid);
		tTPTable0Provider.programDropIngress(dpid,true);
		tTPTable10Provider.programDropVlanFlow(dpid,true);
		tTPTable20Provider.programGotoTable50(dpid,true);
		tTPTable30Provider.programDropRoutingFlow(dpid,true);
		//tTPTable50Provider.programBridgeFlowToController(dpid,true);
		tTPTable50Provider.programGotoTable60(dpid,true);
		tTPTable60Provider.ingressAclArpToController(dpid,true);
		LOG.info("----programSwitchAdd2----");
		tTPTable60Provider.ingressAclDhcpToController(dpid,true);
		tTPTable60Provider.programGotoTable61(dpid,true);
		tTPTable61Provider.programDropTable61(dpid,true);		
	  }
    

	private void programSwitchDelete(Node node) {
		   NodeBuilder nodeBuilder = new NodeBuilder(node);
		   String dpIid = nodeBuilder.getDatapathId();
		   //Long dpid = new Long(Long.parseLong(dpIid));
		   //Long dpid = new Long(34116064366l);
		   Long dpid = new Long(gntOp.getDataPathId(node));
		   LOG.info("programSwitchdelete dpid is {}\n",dpid);
		   tTPTable0Provider.programDropIngress(dpid,false);
		   tTPTable10Provider.programDropVlanFlow(dpid,false);
		   tTPTable20Provider.programGotoTable50(dpid,false);
		   tTPTable30Provider.programDropRoutingFlow(dpid,false);
		   //tTPTable50Provider.programBridgeFlowToController(dpid,false);
		   tTPTable50Provider.programGotoTable60(dpid,false);
		   tTPTable60Provider.ingressAclArpToController(dpid,false);
		   tTPTable60Provider.ingressAclDhcpToController(dpid,false);

		   tTPTable60Provider.programGotoTable61(dpid,false);
		   tTPTable61Provider.programDropTable61(dpid,false);
		   
    }


	private void writeDhcpFlow(Port port) {

          LOG.info("---writeDhcpFlow port is {}\n",port);
	      Long dpid = new Long(gntOp.getDpid(port));
	      String vni = gntOp.getNoSymmetryVni(port).toString();
	      tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port),true);
          tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port),true);
	
  }
	private void programPortAdd(Port port) {
         // the node flow...
         LOG.info("-------programPortAdd-------------\n");
		 //dhcp flow...
		 writeDhcpFlow(port);
         List<Port> l2PortList = gntOp.getSameNetworkPort(port,1);
         if(l2PortList.size()!=0) {
             for(Port port2 : l2PortList) {
			 	 LOG.info("start to write L2 flow!\n");
				 LOG.info("swtcihadd port1 is {},port2 is {}\n",port,port2);
                 writeL2Flow(port,port2);
				 writeL2Flow(port2,port);
             }
          }

		 List<Port> l2OvsPortList = gntOp.getSameNetworkPort(port,2);
         if(l2OvsPortList.size()!=0) {
             for(Port port2 : l2OvsPortList) {
                 writeovsL2Flow(port,port2);
				 writeovsL2Flow(port2,port);
             }
          }

		  List<Port> l3PortList = gntOp.getSameRouterPort(port,1);
		  if(l3PortList!=null) {
		  	LOG.info("l3testnull!");
		  	if(l3PortList.size()!=0) {
                for(Port port2 : l3PortList) {
			 	   LOG.info("start to write L3 flow!\n");
                   writeL3Flow(port,port2);
				   writeL3Flow(port2,port);
                }   
		  	}
          }	

          // ovs L3
		  List<Port> l3OvsPortList = gntOp.getSameRouterPort(port,2);
		  if(l3OvsPortList!=null) {
		  	LOG.info("l3ovstestnull!");
		  	if(l3OvsPortList.size()!=0) {
              for(Port port2 : l3OvsPortList) {
			 	  LOG.info("start to write L3 flow!\n");
                  writeL3OvsFlow(port,port2);
				  writeL3OvsFlow(port2,port);
               }
             }
           }	

          // l3 gw 
          String routeruuid = gntOp.getRouterUUID(port);
		  LOG.info("programPortAdd routeruuid is {}\n",routeruuid);
		  if(routeruuid!=null) {
		  	  String gateway_enable = "true";
			  LOG.info("----startgatewayflow1----");
		      if(gateway_enable.equals(inifile.getGateway())){
			  	   LOG.info("----startgatewayflow2----");
                   writeGwL3(port);
		   	    } 
		  }	
          
		  LOG.info("write flow end!\n");
		
	  }

	 private void programPortOvsAdd(Port port) {
         // the node flow...
         LOG.info("-------programPortOvsAdd-------------\n");
         List<Port> l2PortList = gntOp.getSameNetworkPort(port,1);
         if(l2PortList.size()!=0) {
             for(Port port2 : l2PortList) {
			 	 LOG.info("programPortOvsAdd start to write L2 flow!\n");
				 //writeL2OvsFlow(port2,port);
				 writeovsL2Flow(port2,port);
				 writeovsL2Flow(port,port2);
             }
          }

		  List<Port> l3PortList = gntOp.getSameRouterPort(port,1);
		  if((l3PortList.size()!=0) && (l3PortList!=null)) {
             for(Port port2 : l3PortList) {
			 	 LOG.info("programPortOvsAdd start to write L3 flow!\n");
				 writeL3OvsFlow(port2,port);
				 writeL3OvsFlow(port,port2);
             }
          }	
          
		  LOG.info("programPortOvsAdd write flow end!\n");
		
	  }

    
	  //port1->port2 l2 flow.
	  private void writeL2OvsFlow(Port port1,Port port2) {
	
		  
		  Long dpid = new Long(gntOp.getDpid(port1));
		  Long dpid2 = new Long(gntOp.getDpid(port2));
		  String vni = gntOp.getNoSymmetryVni(port1).toString();
		  tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),true);
		  tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),true);
		  //tTPTable50Provider.programBridgeFlowUnicast3(dpid,"ff:ff:ff:ff:ff:ff",new Long(Long.parseLong(vni)),true);
		  LOG.info("----writeL2OvsFlow----port1 is {},port2 is {},dpid s is {},dpid2 is {}------\n",port1,port2,dpid,dpid2);
		  if(gntOp.isSameTheNode(port1,port2)) {
			// no same node.....
			  
		  } else {
			  // port1 port2 is not the same node.
			 // port1 flow.
			  String flowName = "programBridgeFlowUnicast_" + dpid  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";
		      String flowName2 = "programBridgeFlowUnicast_" + dpid  + "_" + gntOp.getMacaddr(port1) + "_" + vni + "_";
   		      if(groupCache.get(flowName)==null) {
			          LOG.info("------writeL2OvsFlownosamenodebefore-----");
                      Long l2_group1 = dpidGroupCache.get(dpid.toString());
		              dpidGroupCache.put(dpid.toString(),new Long(l2_group1 + 1));
		              Long l2_group2 =  dpidGroupCache.get(dpid.toString());
			          dpidGroupCache.put(dpid.toString(),new Long(l2_group2 + 1));

              
		              groupCache.put(flowName,l2_group1); 		    
		              groupCache.put(flowName2,l2_group2); 

			  
			          Long ofport1 = gntOp.getOfPort(port1,port2);
			          LOG.info("ovs group-key is {},l2_group1 is {},l2_group2 is {}---",dpid.toString()+dpid2.toString()+vni,l2_group1,l2_group2);
			          nodeConnectorInventoryEventTranslator.createL2InterfaceGroupVxlan(dpid,"GNTGROUP",l2_group1,vni,ofport1);
			          LOG.info("------writeL2OvsFlownosamenodeafter-----");
			          tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group1,true);
		               //hui flow...
		              tTPTable0Provider.programLocalInPort(dpid,vni,vni,ofport1,true);
		               //tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),true);
			          nodeConnectorInventoryEventTranslator.createL2InterfaceGroupUntagged(dpid,"GNTGROUP",l2_group2,gntOp.getofPort(port1));
          
                      tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port1),gntOp.getNoSymmetryVni(port1),l2_group2,true);		  
   		     }else {

			  }
			}
	
	  }


	   //port1->port2 l3 flow.
     private void writeL3OvsFlow(Port port1,Port port2) {

	   String sw = "switch";
	   String ovs = "ovs";
	   int flag = -1;
       if(sw.equals(gntOp.getTypeNode(gntOp.getNode(port1))))
	  	  flag = 0;
	   if(ovs.equals(gntOp.getTypeNode(gntOp.getNode(port1))))
	  	  flag = 1;

	    Long dpid = new Long(gntOp.getDpid(port1));
	    Long dpid2 = new Long(gntOp.getDpid(port2));
	    String vni = gntOp.getNoSymmetryVni(port1).toString();
	    //tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),true);
        //tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),true);
	    String attachedMac = gntOp.getGatewayMac(port1);
	    LOG.info("writeL3Flow attachedMac is {}\n",attachedMac);
	    Long metadata = new Long(0l);
	    Long tunnel_key = new Long(0l);
	    Long lowMetadataMask = new Long(0xffffffffl);
	    Long highMetadataMask = new Long(0x7fffffff00000000l);
		
	    LOG.info("lowMetadataMask is {},highMetadataMask is {}\n",lowMetadataMask,highMetadataMask);
	    //tunnel_key = gntOp.getNoSymmetryVni(port2);

		//tunnel_key = gntOp.getNoSymmetryVni(port2);
	    //metadata = tunnel_key;
		//metadata = gntOp.getSymmetryVni(port2);
		if(sw.equals(gntOp.getTypeNode(gntOp.getNode(port1)))) {
			 LOG.info("-----writeL3OvsFlow------dpid is {}.",dpid);
			 metadata = gntOp.getSymmetryVni(port1);
			 tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),true);
			 tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),true);
             tTPTable20Provider.programMacFlow(dpid,attachedMac,vni,metadata<<32,highMetadataMask,true);
	    }
	    if(gntOp.isSameTheNode(port1,port2)) {
	  	    // no same node......
		  
         } else {
          // port1 port2 is not the same node.
          // port1 flow.
           if(flag == 0) {
		   	    tunnel_key = gntOp.getNoSymmetryVni(port2);
			    metadata = gntOp.getSymmetryVni(port2);
               String dstIpaddr = gntOp.getIp(port2);
	  	       String flowName1 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L3_";
		       String flowName2 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
		       //String flowName3 = "programRoutingFlowUnicast_" + dpid2 + "_" + dstIpaddr + "_" + metadata.toString() + "_L2";
		       if(groupCache.get(flowName1)==null) {
	  	             String dstIp = gntOp.getIp(port2);
		             String src = gntOp.getGatewayMac(port2);
		             String des = gntOp.getMacaddr(port2);
		             Long ofport1 = gntOp.getOfPort(port1,port2);

		             Long l2_group1 = dpidGroupCache.get(dpid.toString());
		             dpidGroupCache.put(dpid.toString(),new Long(l2_group1 + 1));
		             Long l3_group1 =  dpidGroupCache.get(dpid.toString());
		             dpidGroupCache.put(dpid.toString(),new Long(l3_group1 + 1));
                   		
		             groupCache.put(flowName1,l3_group1);
		             groupCache.put(flowName2,l2_group1);
		  
		             LOG.info("dstip is {},src is {},des is {},ofport is {}\n",dstIp,src,des,ofport1);
	                 nodeConnectorInventoryEventTranslator.createL2InterfaceGroupVxlan(dpid,"GNTGROUP",l2_group1,tunnel_key.toString(),ofport1);	      
	                 groupProvider.createL3UnicastGroup(dpid,"GNTGROUP",l3_group1 + groupL3id,l2_group1,src, des);
	                 tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIp,metadata<<32,l3_group1 + groupL3id,true);	
		       	}

           	}else {
                       if(flag == 1)  {
					   	    String dstIpaddr = gntOp.getIp(port2);
						    metadata = gntOp.getSymmetryVni(port2);
					   	    String flowName3 = "programRoutingFlowUnicast_"  + dpid + "_" + gntOp.getMacaddr(port2) + "_" + metadata.toString() + "_";
					   	    //String flowName3 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
							if(groupCache.get(flowName3)==null) {
								  metadata = gntOp.getNoSymmetryVni(port2);
								  Long l2_group11 = dpidGroupCache.get(dpid2.toString());
		                          dpidGroupCache.put(dpid2.toString(),new Long(l2_group11 + 1));
                                  groupCache.put(flowName3,l2_group11);
		                          Long ofport2 = gntOp.getOfPort(port2,port1); 
		                          tTPTable0Provider.programLocalInPort(dpid2,metadata.toString(),metadata.toString(),ofport2,true);
		                          nodeConnectorInventoryEventTranslator.createL2InterfaceGroupUntagged(dpid2,"GNTGROUP",l2_group11,gntOp.getofPort(port2));
                                  tTPTable50Provider.programBridgeFlowUnicast(dpid2,gntOp.getMacaddr(port2),metadata,l2_group11,true);	  
                                  LOG.info("writeL3OvsFlow metadatais {}",metadata);

							 }
                             

                       	}


			}
        
	  	    	}
	   	}

	  
	private void programPortDelete(Node node,Port port) {
         // the node flow...
         List<Port> l2PortList = gntOp.getSameNetworkPort(port,1);
         if(l2PortList.size()!=0) {
             for(Port port2 : l2PortList) {
			 	 LOG.info("start to delete L2 flow!\n");
                 deleteL2FlowEgress(node,port,port2);
				 deleteL2FlowIngress(node,port2,port);
             }
          }

         List<Port> l2OvsPortList = gntOp.getSameNetworkPort(port,2);
         if(l2OvsPortList.size()!=0) {
             for(Port port2 : l2OvsPortList) {
			 	 LOG.info("start to delete L2 flow!\n");
                 deleteL2OvsFlow(node,port,port2);
             }
          }
		 

		  List<Port> l3PortList = gntOp.getSameRouterPort(port,1);
		  /*  if((l3PortList.size()!=0) && (l3PortList!=null)) {
             for(Port port2 : l3PortList) {
			 	 LOG.info("start to delete L3 flow!\n");
                 deleteL3FlowEgress(port,port2);
				 deleteL3FlowIngress(port2,port);
             }
          }*/	

         if(l3PortList!=null) {
		  	LOG.info("l3testnull!");
		  	if(l3PortList.size()!=0) {
                for(Port port2 : l3PortList) {
			 	   LOG.info("start to delete L3 flow!\n");
                   deleteL3FlowEgress(node,port,port2);
				   deleteL3FlowIngress(node,port2,port);
                }   
		  	}
          }	

		 
          List<Port> l3OvsPortList = gntOp.getSameRouterPort(port,2);
		  /*  if((l3OvsPortList.size()!=0) && (l3OvsPortList!=null)) {
             for(Port port2 : l3PortList) {
			 	 LOG.info("start to delete L3 flow!\n");
                 deleteL3OvsFlow(port,port2);
             }
          }*/	

		  if(l3OvsPortList!=null) {
		  	LOG.info("l3ovstestnull!");
		  	if(l3OvsPortList.size()!=0) {
              for(Port port2 : l3OvsPortList) {
			 	  LOG.info("start to write L3 flow!\n");
                  deleteL3OvsFlow(node,port,port2);
               }
             }
           }	



          String routeruuid = gntOp.getRouterUUID(port);
		  LOG.info("programPortAdd routeruuid is {}\n",routeruuid);
		  if(routeruuid!=null) {
               deleteGwL3(node,port);
		  }	
		  

		  LOG.info("write flow end!\n");
		
	  }


	 private void programPortOvsDelete(Node node,Port port) {
         // the node flow...
         List<Port> l2PortList = gntOp.getSameNetworkPort(port,1);
         if(l2PortList.size()!=0) {
             for(Port port2 : l2PortList) {
			 	 LOG.info("start to delete L2 flow!\n");
				 deleteL2ovsFlow(node,port2,port);
				 
             }
          }

		  List<Port> l3PortList = gntOp.getSameRouterPort(port,1);
		  if((l3PortList.size()!=0) && (l3PortList!=null)) {
             for(Port port2 : l3PortList) {
			 	 LOG.info("start to delete L3 flow!\n");
				 deleteL3ovsFlow(node,port2,port);
             }
          }	

		  LOG.info("write flow end!\n");
		
	  }


       //port1->port2 l2 ovs flow.
    private void deleteL2OvsFlow(Node node,Port port1,Port port2) {
     
	  //Node node = gntOp.getNode(port1);
	  long dpid = new Long(gntOp.getDataPathId(node)); 
	  Long dpid2 = new Long(gntOp.getDpid(port2));
	  String vni = gntOp.getNoSymmetryVni(port1).toString();
	  tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),false);
      tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),false);
	  
      LOG.info("----writeL2Flow----port1 is {},port2 is {},dpid s is {},dpid2 is {}------\n",port1,port2,dpid,dpid2);
	  String flowName2 = "programBridgeFlowUnicast_" + dpid2  + "_" + gntOp.getMacaddr(port1) + "_" + vni + "_";
	  String flowName = "programBridgeFlowUnicast_" + dpid  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";

      Long l2_group3 = groupCache.get(flowName2);
      groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group3);
	  LOG.info("------writeL2Flowsamenodeafter2-----");
	  tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port1),gntOp.getNoSymmetryVni(port1),l2_group3,false);  
	  groupCache.remove(flowName2);
	  
	  
	  if(gntOp.isSameTheNode(node,port2)) {
        // no same node


	   }else {

			// no same node...
              Node node2 = gntOp.getNode(port2);
			  if(gntOp.isLastNetworkPort(node,port1)) {
			  	    Long l2_group2 = groupCache.get(flowName);
                    groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group2);
	                LOG.info("------writeL2Flowsamenodeafter2-----");
	                tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group2,false);  
	                groupCache.remove(flowName);
			  	    if(gntOp.isLastNetworkPort(port2,0)) {
						 Long ofport2 = gntOp.getOfPort(node,gntOp.getNode(port2)); 
                         tTPTable0Provider.programLocalInPort(dpid,vni,vni,ofport2,false);
						

			  	    }

			  	}
		
               
            }
     }



   private void deleteL2ovsFlow(Node node,Port port1,Port port2) {
      // port1 switch...port2 is ovs   port2 is delete....

	    Long dpid = new Long(gntOp.getDpid(port1));
	    Long dpid2 = new Long(gntOp.getDataPathId(node));
	    String vni = gntOp.getNoSymmetryVni(port1).toString();
		Node node2 = node;
		Node node1 = gntOp.getNode(port1);

		String flowName = "programBridgeFlowUnicast_" + dpid  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";
		String flowName2 = "programBridgeFlowUnicast_" + dpid2  + "_" + gntOp.getMacaddr(port1) + "_" + vni + "_";

		Long l2_group1 = groupCache.get(flowName); 
		groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
		LOG.info("------writeL2Flownosamenodeafter-----");
		tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group1,false);
	    groupCache.remove(flowName);
		if(false) {


		}	else {
              // not same the  node...
               if(gntOp.isLastNetworkPort(node2,port2)) {
			   	         LOG.info("------shenxiao909-----");
				         Long ofport2 = gntOp.getOfPort(gntOp.getNode(port1),node); 
				         tTPTable0Provider.programLocalInPort(dpid,vni,vni,ofport2,false);

			         } else {
                     
               
			   }

			  if(gntOp.isLastNetworkPort(port1,0)) {
			  	    LOG.info("------shenxiao1234-----");
					LOG.info("groupCache is {}",groupCache);
                    Long l2_group2 = groupCache.get(flowName2); 
		            groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group2);
		            tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port1),gntOp.getNoSymmetryVni(port1),l2_group2,false);
	                groupCache.remove(flowName); 
			   }
			} 
	  
	   
     }


     private void deleteL3ovsFlow(Node node,Port port1,Port port2) {

        Long dpid = new Long(gntOp.getDpid(port1));
	    Long dpid2 = new Long(gntOp.getDataPathId(node));
	    String vni = gntOp.getNoSymmetryVni(port1).toString();
		Node node2 = node;
		Node node1 = gntOp.getNode(port1);

		
		String attachedMac = gntOp.getGatewayMac(port2);
        LOG.info("writeL3Flow attachedMac is {}\n",attachedMac);
	    Long metadata = new Long(0l);
	    Long metadataMask = new Long(0l);
	    String symmetry = "symmetry";
	    String nosymmetry = "nosymmetry";
	    /*if(symmetry.equals(gntOp.getSwitchType(node))) {
				 // duicheng;
				 metadata = gntOp.getSymmetryVni(port2);
				 LOG.info("symmetry metadata is {}\n",metadata);
		 }
		if(nosymmetry.equals(gntOp.getSwitchType(node))) {
				// no duicheng;
			    metadata = gntOp.getNoSymmetryVni(port2);
				LOG.info("nosymmetry metadata is {}\n",metadata);
		
	    }*/
	    metadata = gntOp.getSymmetryVni(port2);
	  
        LOG.info("-----deleteL3ovsFlow-----groupCache is {}",groupCache);
		String dstIpaddr = gntOp.getIp(port2);
	    String flowName1 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L3_";
        String flowName2 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
		String flowName3 = "programRoutingFlowUnicast_"  + dpid2 + "_" + gntOp.getMacaddr(port1) + "_" + metadata.toString() + "_";
		Long l3_group1 = groupCache.get(flowName1);
	    Long l2_group1 = groupCache.get(flowName2);
        groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
	    groupProvider.destroyGroup(dpid,"GNTGROUP",l3_group1 + groupL3id); 
	    groupCache.remove(flowName1);
	    groupCache.remove(flowName2);
	    tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIpaddr,metadata<<32,l3_group1 + groupL3id,false);    

		if(false) {
  

		}	else {
              // not same the  node...
               if(gntOp.isLastrouterofNode(node2,port2)) {
			   	        LOG.info("------shenxiao12345-----");
                        Long ofport2 = gntOp.getOfPort(gntOp.getNode(port1),node); 
				        tTPTable0Provider.programLocalInPort(dpid,vni,vni,ofport2,false);
						

			         } else {      
               
			   }

			 if(gntOp.isLastrouterofNode(port1,0)) {
			  	    LOG.info("------shenxiao1234-----");
					LOG.info("groupCache is {}",groupCache);
                    Long l2_group2 = groupCache.get(flowName3); 
		            groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group2);
		            tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port1),gntOp.getNoSymmetryVni(port1),l2_group2,false);
	                groupCache.remove(flowName3); 
			   }
					 
			} 
        
      }

    //port1->port2 l2 flow.
    
    private void deleteL2FlowEgress(Node node,Port port1,Port port2) {
     
	  //Long dpid = new Long(gntOp.getDpid(port1));
	  //Node node = gntOp.getNode(port1);
	  long dpid = new Long(gntOp.getDataPathId(node)); 
	  Long dpid2 = new Long(gntOp.getDpid(port2));
	  String vni = gntOp.getNoSymmetryVni(port1).toString();
	  tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),false);
      tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),false);
	  
      LOG.info("----writeL2Flow----port1 is {},port2 is {},dpid s is {},dpid2 is {}------\n",port1,port2,dpid,dpid2);
	  String flowName2 = "programBridgeFlowUnicast_" + dpid2  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";
	  String flowName = "programBridgeFlowUnicast_" + dpid  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";
	  if(gntOp.isSameTheNode(node,port2)) {
           //todo ....
		   if(gntOp.isLastNetworkPort(port2,0)) {
                   Long l2_group1 = groupCache.get(flowName);
		           groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
		           LOG.info("------writeL2Flowsamenodeafter2-----");
		           tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group1,false);  
		           groupCache.remove(flowName);

              	}	


	   }else {//---

			// no same node...
              Node node2 = gntOp.getNode(port2);
			  if(gntOp.isLastNetworkPort(node,port1)) {
			         if(gntOp.isLastNetworkPort(port2,0)) {
                             Long l2_group1 = groupCache.get(flowName);
				             //List<Port> l2PortList = gntOp.getSameNetworkPort(port2,1);
				             groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
		                     LOG.info("------writeL2Flownosamenodeafter-----");
		                     tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group1,false);
				             groupCache.remove(flowName);
				             Long ofport2 = gntOp.getOfPort(port2,node); 
				             tTPTable0Provider.programLocalInPort(dpid2,vni,vni,ofport2,false);                
			                 Long l2_group2 = groupCache.get(flowName2);

		                     groupProvider.destroyGroup(dpid2,"GNTGROUP",l2_group2);	
							 groupCache.remove(flowName2);
                             tTPTable50Provider.programBridgeFlowUnicast(dpid2,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group2,false);

			           } else {
                             Long l2_group1 = groupCache.get(flowName);
							 groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
		                     LOG.info("------writeL2Flownosamenodeafter-----");
		                     tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group1,false);
				             groupCache.remove(flowName);
				             Long ofport2 = gntOp.getOfPort(port2,node); 
				             tTPTable0Provider.programLocalInPort(dpid2,vni,vni,ofport2,false); 
                           

					   }

			  	} else {

						
                     
               
			  		}
		
               
           }
     }

    private void deleteL2FlowIngress(Node node,Port port1,Port port2) {
		
        Long dpid = new Long(gntOp.getDpid(port1));
	    //Long dpid2 = new Long(gntOp.getDpid(port2));
	    long dpid2 = new Long(gntOp.getDataPathId(node)); 
	    String vni = gntOp.getNoSymmetryVni(port1).toString();
		Node node2 = node;
		Node node1 = gntOp.getNode(port1);

		String flowName = "programBridgeFlowUnicast_" + dpid  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";
		String flowName2 = "programBridgeFlowUnicast_" + dpid2  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";

		Long l2_group2 = groupCache.get(flowName2);
		groupProvider.destroyGroup(dpid2,"GNTGROUP",l2_group2);
		LOG.info("------writeL2Flowsamenodeafter2-----");
		tTPTable50Provider.programBridgeFlowUnicast(dpid2,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group2,false);  
		groupCache.remove(flowName2);

		 Long l2_group1 = groupCache.get(flowName); 
		 groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
		 LOG.info("------writeL2Flownosamenodeafter-----");
		 tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group1,false);
	     groupCache.remove(flowName);
		 if(gntOp.isSameTheNode(port1,node)) {
              if((gntOp.isLastNetworkPort(port1,0))&& (gntOp.isLastrouterofNode(port1,0))) {
                    tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),false);
                    tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),false);

            	}  

		}	else {
              // not same the  node...
               if(gntOp.isLastNetworkPort(node2,port2)) {
			   	
				         Long ofport2 = gntOp.getOfPort(node2,port1); 
				         tTPTable0Provider.programLocalInPort(dpid2,vni,vni,ofport2,false);

			         } else {
                     
               
			   }
			} 

  }

   //port1->port2 l3 flow.
  private void deleteL3FlowEgress(Node node,Port port1,Port port2) {

      //Node node = gntOp.getNode(port1);
	  long dpid = new Long(gntOp.getDataPathId(node)); 
	  Long dpid2 = new Long(gntOp.getDpid(port2));
	  String vni = gntOp.getNoSymmetryVni(port1).toString();
	  tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),false);
      tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),false);
	  String attachedMac = gntOp.getGatewayMac(port1);
	  LOG.info("writeL3Flow attachedMac is {}\n",attachedMac);
	  Long metadata = new Long(0l);
	  Long metadataMask = new Long(0l);
	  String symmetry = "symmetry";
	  String nosymmetry = "nosymmetry";
	  if(symmetry.equals(gntOp.getSwtichType(port2))) {
		   // duicheng;
		   metadata = gntOp.getSymmetryVni(port2);
		   LOG.info("symmetry metadata is {}\n",metadata);
       }
	   if(nosymmetry.equals(gntOp.getSwtichType(port2))) {
          // no duicheng;
          metadata = gntOp.getNoSymmetryVni(port2);
		  LOG.info("nosymmetry metadata is {}\n",metadata);

	  }

	  if(gntOp.isLastSubnetPort(node,port1)) {
	  	 LOG.info("-----------------delete table20gw------------------");
	  	 //Long metadata1 = gntOp.getNoSymmetryVni(port1);
         //tTPTable20Provider.programMacFlow(dpid,attachedMac,vni,metadata<<32,metadataMask,false);
	  }

	  
      String dstIpaddr = gntOp.getIp(port2);
	  String flowName1 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L3_";
      String flowName2 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
	  if(gntOp.isSameTheNode(node,port2)) {
           //todo ....
            LOG.info("test8909890");
		   if(gntOp.isLastrouterofNode(node,port2)) {
		   	    LOG.info("test8909891");
                Long l3_group1 = groupCache.get(flowName1);
				Long l2_group1 = groupCache.get(flowName2);
                groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
				groupProvider.destroyGroup(dpid,"GNTGROUP",l3_group1 + groupL3id); 
				groupCache.remove(flowName1);
				groupCache.remove(flowName2);
	            tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIpaddr,metadata<<32,l3_group1 + groupL3id,false);

           }	


	   }else {//---

			// no same node...
		      String flowName3 = "programRoutingFlowUnicast_" + dpid2 + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
              Node node2 = gntOp.getNode(port2);
			  if(gntOp.isLastrouterofNode(node,port1)) {
			  	       
			  	       Long l3_group1 =  groupCache.get(flowName1);
		               Long l2_group1 =  groupCache.get(flowName2);
					   LOG.info("---------------testlastrouter1----------l2_group1 is {},l3_group1 is {}.",l2_group1,l3_group1);
		               groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
				       groupProvider.destroyGroup(dpid,"GNTGROUP",l3_group1 + groupL3id); 
					   groupCache.remove(flowName1);
				       groupCache.remove(flowName2);
	                   tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIpaddr,metadata<<32,l3_group1 + groupL3id,false);	
		                  // port2 flow.
		               Long ofport2 = gntOp.getOfPort(gntOp.getNode(port2),node); 
		               tTPTable0Provider.programLocalInPort(dpid2,metadata.toString(),metadata.toString(),ofport2,false); 
					   LOG.info("metadata is {},port number is {}.",metadata,ofport2);
			        if(gntOp.isLastrouterofNode(port2,0)) {
						  LOG.info("---------------testlastrouter1----------");
		                  Long l2_group2 =  groupCache.get(flowName3);	
						  groupCache.remove(flowName3);
		                  groupProvider.destroyGroup(dpid2,"GNTGROUP",l2_group2);
						  LOG.info("------------l2_group2 is {},metadate is {}------",l2_group2,metadata);
                          tTPTable50Provider.programBridgeFlowUnicast(dpid2,gntOp.getMacaddr(port2),metadata,l2_group2,false);	    
                             						  
			           } else {
                           

					   }

			  	} else {

     
			  		}
		
               
           }

	  
	  
		   	
    }


    private void deleteL3FlowIngress(Node node,Port port1,Port port2) {

        Long dpid = new Long(gntOp.getDpid(port1));
	    //Long dpid2 = new Long(gntOp.getDpid(port2));
	    Long dpid2 = new Long(gntOp.getDataPathId(node)); 
	    String vni = gntOp.getNoSymmetryVni(port1).toString();
		Node node2 = node;
		Node node1 = gntOp.getNode(port1);

		
		String attachedMac = gntOp.getGatewayMac(port2);
        LOG.info("writeL3Flow attachedMac is {}\n",attachedMac);
	    Long metadata = new Long(0l);
	    Long metadataMask = new Long(0l);
	    String symmetry = "symmetry";
	    String nosymmetry = "nosymmetry";
	    //if(symmetry.equals(gntOp.getSwtichType(port2))) {
        if(symmetry.equals(gntOp.getSwitchType(node))) {
				 // duicheng;
				 metadata = gntOp.getSymmetryVni(port2);
				 LOG.info("symmetry metadata is {}\n",metadata);
		}
		if(nosymmetry.equals(gntOp.getSwitchType(node))) {
				// no duicheng;
			    metadata = gntOp.getNoSymmetryVni(port2);
				LOG.info("nosymmetry metadata is {}\n",metadata);
		
	    }

		String dstIpaddr = gntOp.getIp(port2);
	    String flowName1 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L3_";
        String flowName2 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
	    LOG.info("groupcache is {}.flowname1 is {},flowname2 is {}.",groupCache,flowName1,flowName2);
		Long l3_group1 = groupCache.get(flowName1);
	    Long l2_group1 = groupCache.get(flowName2);
        groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
	    groupProvider.destroyGroup(dpid,"GNTGROUP",l3_group1 + groupL3id); 
	    groupCache.remove(flowName1);
	    groupCache.remove(flowName2);
	    tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIpaddr,metadata<<32,l3_group1 + groupL3id,false);

		String flowName3 = "programRoutingFlowUnicast_" + dpid2 + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
        Long l2_group2 =  groupCache.get(flowName3);
		// fix bug for the same node...
		if(dpid2.longValue()!=dpid.longValue()) {
			LOG.info("test1234567");
		    groupCache.remove(flowName3);
		    groupProvider.destroyGroup(dpid2,"GNTGROUP",l2_group2);
		    LOG.info("------------l2_group2 is {},metadate is {}------",l2_group2,metadata);
            tTPTable50Provider.programBridgeFlowUnicast(dpid2,gntOp.getMacaddr(port2),metadata,l2_group2,false);	    
	    }                    						  
		if(gntOp.isSameTheNode(port1,node)) {
              if((gntOp.isLastNetworkPort(port1,0))&& (gntOp.isLastrouterofNode(port1,0))) {
			  	     LOG.info("delete0901");
				    //tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),false);
				    //tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),false);
                                       

              	}	

		}	else {
              // not same the  node...
               if(gntOp.isLastrouterofNode(node2,port2)) {
                        Long ofport2 = gntOp.getOfPort(node2,node1); 
				        //tTPTable0Provider.programLocalInPort(dpid2,vni,vni,ofport2,false);
                        tTPTable0Provider.programLocalInPort(dpid2,metadata.toString(),metadata.toString(),ofport2,false);
			         } else {

						
                     
               
			   }
			} 
        
      }


     //port1->port2 l3 flow.
    private void deleteRouterL3FlowEgress(Router router,Node node,Port port1,Port port2) {

      //Node node = gntOp.getNode(port1);
	  long dpid = new Long(gntOp.getDataPathId(node)); 
	  Long dpid2 = new Long(gntOp.getDpid(port2));
	  String vni = gntOp.getNoSymmetryVni(port1).toString();
	  tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),false);
      tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),false);
	  String attachedMac = gntOp.getGatewayMac(port1);
	  LOG.info("writeL3Flow attachedMac is {}\n",attachedMac);
	  Long metadata = new Long(0l);
	  Long metadataMask = new Long(0l);
	  String symmetry = "symmetry";
	  String nosymmetry = "nosymmetry";
	  if(symmetry.equals(gntOp.getSwtichType(port2))) {
		   // duicheng;
		   metadata = gntOp.getSymmetryVni(port2);
		   LOG.info("symmetry metadata is {}\n",metadata);
       }
	   if(nosymmetry.equals(gntOp.getSwtichType(port2))) {
          // no duicheng;
          metadata = gntOp.getNoSymmetryVni(port2);
		  LOG.info("nosymmetry metadata is {}\n",metadata);

	  }

	  if(gntOp.isLastSubnetPort(node,port1)) {
	  	 LOG.info("-----------------delete table20gw------------------");
	  	 //Long metadata1 = gntOp.getNoSymmetryVni(port1);
         //tTPTable20Provider.programMacFlow(dpid,attachedMac,vni,metadata<<32,metadataMask,false);
	  }

	  
      String dstIpaddr = gntOp.getIp(port2);
	  String flowName1 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L3_";
      String flowName2 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
	  if(gntOp.isSameTheNode(node,port2)) {
           //todo ....
		   if(gntOp.isLastrouterofNode(router,node,port2)) {
                Long l3_group1 = groupCache.get(flowName1);
				Long l2_group1 = groupCache.get(flowName2);
                groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
				groupProvider.destroyGroup(dpid,"GNTGROUP",l3_group1); 
				groupCache.remove(flowName1);
				groupCache.remove(flowName2);
	            tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIpaddr,metadata<<32,l3_group1 + groupL3id,false);

           }	


	   }else {//---

			// no same node...
		      String flowName3 = "programRoutingFlowUnicast_" + dpid2 + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
              Node node2 = gntOp.getNode(port2);
			  if(gntOp.isLastrouterofNode(router,node,port1)) {
			  	       
			  	       Long l3_group1 =  groupCache.get(flowName1);
		               Long l2_group1 =  groupCache.get(flowName2);
					   LOG.info("---------------testlastrouter1----------l2_group1 is {},l3_group1 is {}.",l2_group1,l3_group1);
		               groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
				       groupProvider.destroyGroup(dpid,"GNTGROUP",l3_group1 + groupL3id); 
					   groupCache.remove(flowName1);
				       groupCache.remove(flowName2);
	                   tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIpaddr,metadata<<32,l3_group1 + groupL3id,false);	
		                  // port2 flow.
		               Long ofport2 = gntOp.getOfPort(gntOp.getNode(port2),node); 
		               tTPTable0Provider.programLocalInPort(dpid2,metadata.toString(),metadata.toString(),ofport2,false); 
					   LOG.info("metadata is {},port number is {}.",metadata,ofport2);
			        if(gntOp.isLastrouterofNode(router,port2,0)) {
						  LOG.info("---------------testlastrouter1----------");
		                  Long l2_group2 =  groupCache.get(flowName3);	
						  groupCache.remove(flowName3);
		                  groupProvider.destroyGroup(dpid2,"GNTGROUP",l2_group2);
						  LOG.info("------------l2_group2 is {},metadate is {}------",l2_group2,metadata);
                          tTPTable50Provider.programBridgeFlowUnicast(dpid2,gntOp.getMacaddr(port2),metadata,l2_group2,false);	    
                             						  
			           } else {
                           

					   }

			  	} else {

     
			  		}
		
               
           }

		   	
    }

    private void deleteRouterL3FlowIngress(Router router,Node node,Port port1,Port port2) {

        Long dpid = new Long(gntOp.getDpid(port1));
	    //Long dpid2 = new Long(gntOp.getDpid(port2));
	    Long dpid2 = new Long(gntOp.getDataPathId(node)); 
	    String vni = gntOp.getNoSymmetryVni(port1).toString();
		Node node2 = node;
		Node node1 = gntOp.getNode(port1);

		
		String attachedMac = gntOp.getGatewayMac(port2);
        LOG.info("writeL3Flow attachedMac is {}\n",attachedMac);
	    Long metadata = new Long(0l);
	    Long metadataMask = new Long(0l);
	    String symmetry = "symmetry";
	    String nosymmetry = "nosymmetry";
	    //if(symmetry.equals(gntOp.getSwtichType(port2))) {
        if(symmetry.equals(gntOp.getSwitchType(node))) {
				 // duicheng;
				 metadata = gntOp.getSymmetryVni(port1);
				 LOG.info("symmetry metadata is {}\n",metadata);
		 }
		if(nosymmetry.equals(gntOp.getSwitchType(node))) {
				// no duicheng;
			    metadata = gntOp.getNoSymmetryVni(port2);
				LOG.info("nosymmetry metadata is {}\n",metadata);
		
	    }

		String dstIpaddr = gntOp.getIp(port2);
	    String flowName1 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L3_";
        String flowName2 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
	    LOG.info("groupcache is {}.flowname1 is {},flowname2 is {}.",groupCache,flowName1,flowName2);
		Long l3_group1 = groupCache.get(flowName1);
	    Long l2_group1 = groupCache.get(flowName2);
        groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
	    groupProvider.destroyGroup(dpid,"GNTGROUP",l3_group1 + groupL3id); 
	    groupCache.remove(flowName1);
	    groupCache.remove(flowName2);
	    tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIpaddr,metadata<<32,l3_group1 + groupL3id,false);

		String flowName3 = "programRoutingFlowUnicast_" + dpid2 + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
        Long l2_group2 =  groupCache.get(flowName3);	
		groupCache.remove(flowName3);
		groupProvider.destroyGroup(dpid2,"GNTGROUP",l2_group2);
		LOG.info("------------l2_group2 is {},metadate is {}------",l2_group2,metadata);
        tTPTable50Provider.programBridgeFlowUnicast(dpid2,gntOp.getMacaddr(port2),metadata,l2_group2,false);	    
                             						  
		if(gntOp.isSameTheNode(port1,node)) {
              if((gntOp.isLastNetworkPort(port1,0))&& (gntOp.isLastrouterofNode(router,port1,0))) {
			  	  
				    tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),false);
				    tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),false);
                                       

              	}	

		}	else {
              // not same the  node...
               if(gntOp.isLastrouterofNode(router,node2,port2)) {
			   	        LOG.info("shenxiaoxiao");
                        Long ofport2 = gntOp.getOfPort(node2,node1); 
					    LOG.info("dpid2 is {},ofport2 is {},vni is {}.",dpid2,ofport2,vni);
				        tTPTable0Provider.programLocalInPort(dpid2,metadata.toString(),metadata.toString(),ofport2,false);

			         } else {

						
                     
               
			   }
			} 
        
      }
	 
	private void deleteL3OvsFlow(Node node,Port port1,Port port2) {

      //Node node = gntOp.getNode(port1);
	  long dpid = new Long(gntOp.getDataPathId(node)); 
	  Long dpid2 = new Long(gntOp.getDpid(port2));
	  String vni = gntOp.getNoSymmetryVni(port1).toString();
	  tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),false);
      tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),false);
	  String attachedMac = gntOp.getGatewayMac(port1);
	  LOG.info("writeL3Flow attachedMac is {}\n",attachedMac);
	  Long metadata = new Long(0l);
	  Long metadataMask = new Long(0l);
	  String symmetry = "symmetry";
	  String nosymmetry = "nosymmetry";
	 /* if(symmetry.equals(gntOp.getSwtichType(port2))) {
		   // duicheng;
		   metadata = gntOp.getSymmetryVni(port2);
		   LOG.info("symmetry metadata is {}\n",metadata);
       }
	   if(nosymmetry.equals(gntOp.getSwtichType(port2))) {
          // no duicheng;
          metadata = gntOp.getNoSymmetryVni(port2);
		  LOG.info("nosymmetry metadata is {}\n",metadata);

	  }*/
	  //metadata = gntOp.getNoSymmetryVni(port1);
        metadata = gntOp.getSymmetryVni(port2);
	    Long tunnel_key = gntOp.getNoSymmetryVni(port1);
	 
	  if(gntOp.isLastSubnetPort(node,port1)) {
         //tTPTable20Provider.programMacFlow(dpid,attachedMac,vni,metadata,metadataMask,false);
	  }
      String dstIpaddr = gntOp.getIp(port2);
	  String flowName1 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L3_";
      String flowName2 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
	  if(false) {
           //todo ....

	   }else {//---

			// no same node...
		      //String flowName3 = "programRoutingFlowUnicast_" + dpid2 + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
			  String flowName3 = "programRoutingFlowUnicast_"  + dpid2 + "_" + gntOp.getMacaddr(port1) + "_" + metadata.toString() + "_";
              Node node2 = gntOp.getNode(port2);
			  if(gntOp.isLastrouterofNode(node,port1)) {
			  	       Long l3_group1 =  groupCache.get(flowName1);
		               Long l2_group1 =  groupCache.get(flowName2);
					   LOG.info("groupCache is {},l2_group1 is l3_group1 is {}..",groupCache,l2_group1,l3_group1);
		               groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
				       groupProvider.destroyGroup(dpid,"GNTGROUP",l3_group1 + groupL3id); 
					   groupCache.remove(flowName1);
				       groupCache.remove(flowName2);
	                   tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIpaddr,metadata<<32,l3_group1 + groupL3id,false);	
		                  // port2 flow.
		          
			        if(gntOp.isLastrouterofNode(port2,0)) {
		                    Long l2_group11 = groupCache.get(flowName3);
		                    Long ofport2 = gntOp.getOfPort(port2,node); 
		                    tTPTable0Provider.programLocalInPort(dpid,vni,vni,ofport2,false);
						    groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group11);	
							groupCache.remove(flowName3);
                            tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port1),tunnel_key,l2_group11,false);	  
                             						  
			           } else {
                            Long l2_group11 = groupCache.get(flowName3);
						    groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group11);	
							groupCache.remove(flowName3);
                            tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port1),tunnel_key,l2_group11,false);  

					   }

			  	} else {

     
			  		}
		
               
           }

	  
	  
		   	
    }

   //port1->port2 l2 flow.
   private void writeovsL2Flow(Port port1,Port port2) {

      String sw = "switch";
	  String ovs = "ovs";
	  int flag = -1;
      if(sw.equals(gntOp.getTypeNode(gntOp.getNode(port1))))
	  	flag = 0;
	  if(ovs.equals(gntOp.getTypeNode(gntOp.getNode(port1))))
	  	flag = 1;
	  
      LOG.info("---writeL2Flowport1 is {},port2 is {}\n",port1,port2);
	  Long dpid = new Long(gntOp.getDpid(port1));
	  Long dpid2 = new Long(gntOp.getDpid(port2));
	  String vni = gntOp.getNoSymmetryVni(port1).toString();
	  if(flag == 0) {
	     tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),true);
         tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),true);
	  }	 
      LOG.info("----writeL2Flow----port1 is {},port2 is {},dpid s is {},dpid2 is {}------\n",port1,port2,dpid,dpid2);
	  if(gntOp.isSameTheNode(port1,port2)) {
		  
      } else {

	     LOG.info("------writeL2Flownosamenodebefore-----");
		 String flowName = "programBridgeFlowUnicast_" + dpid  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";
		
		 if(flag == 0) {
   		     if(groupCache.get(flowName)==null) {
                 // port1 port2 is not the same node.
                 // port1 flow.
           
				 //PORT1 IS SWITCH PORT2 IS OVS
                 LOG.info("------writeL2Flownosamenodebefore-----");
                 Long ofport1 = gntOp.getOfPort(port1,port2); 
		         Long l2_group1 = dpidGroupCache.get(dpid.toString());
		         dpidGroupCache.put(dpid.toString(),new Long(l2_group1 + 1));
		         LOG.info("writeOvsL2Flowgroup dpidGroupCache is {}, dpid1 is {},dpid2 is {} l2_group1 is {},l2_group2 is {}\n",dpidGroupCache,dpid,dpid2,l2_group1);		
		         groupCache.put(flowName,l2_group1); 	  
			 
                 nodeConnectorInventoryEventTranslator.createL2InterfaceGroupVxlan(dpid,"GNTGROUP",l2_group1,vni,ofport1);
		         LOG.info("------writeL2Flownosamenodeafter-----");
		         tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group1,true);
   		      }

      	   }else {
      	       if(flag == 1) {
			   	  String flowName2 = "programBridgeFlowUnicast_" + dpid  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";
			   	  if(groupCache.get(flowName2)==null) { 
                    
				       Long ofport1 = gntOp.getOfPort(port2,port1);  
				       Long l2_group2 =  dpidGroupCache.get(dpid2.toString());
				       dpidGroupCache.put(dpid2.toString(),new Long(l2_group2 + 1));
				       groupCache.put(flowName2,l2_group2); 
				       tTPTable0Provider.programLocalInPort(dpid2,vni,vni,ofport1,true);
				       //tTPTable0Provider.programPhysicalIn(dpid,ofport1,true);
					   nodeConnectorInventoryEventTranslator.createL2InterfaceGroupUntagged(dpid2,"GNTGROUP",l2_group2,gntOp.getofPort(port2)); 
					   tTPTable50Provider.programBridgeFlowUnicast(dpid2,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group2,true); 
			   	  	}

      	       	}

			       
      	    
      	  }
		  
	     }

  }

  //port1->port2 l2 flow.
  private void writeL2Flow(Port port1,Port port2) {

      LOG.info("---writeL2Flowport1 is {},port2 is {}\n",port1,port2);
	  Long dpid = new Long(gntOp.getDpid(port1));
	  Long dpid2 = new Long(gntOp.getDpid(port2));
	  String vni = gntOp.getNoSymmetryVni(port1).toString();
	  tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),true);
      tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),true);
      LOG.info("----writeL2Flow----port1 is {},port2 is {},dpid s is {},dpid2 is {}------\n",port1,port2,dpid,dpid2);
	  if(gntOp.isSameTheNode(port1,port2)) {
	  	 // port1 port2 is the same node.
	  	  String flowName = "programBridgeFlowUnicast_" + dpid  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";
	  	  if(groupCache.get(flowName)==null) {
		  
               Long l2_group1 = dpidGroupCache.get(dpid.toString());
		       dpidGroupCache.put(dpid.toString(),new Long(l2_group1 + 1));
	  	       LOG.info("------writeL2Flowsamenodebeforel2_group is {}---------",l2_group1);
		       nodeConnectorInventoryEventTranslator.createL2InterfaceGroupUntagged(dpid,"GNTGROUP",l2_group1,gntOp.getofPort(port2));
		       LOG.info("------writeL2Flowsamenodeafter2-----");
		       tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group1,true);
		 
		       groupCache.put(flowName,l2_group1);
	  	   } else {
	  	      

	  	   	}
		  
      } else {
          // port1 port2 is not the same node.
         // port1 flow.
          LOG.info("------writeL2Flownosamenodebefore-----");
		  String flowName = "programBridgeFlowUnicast_" + dpid  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";
		  String flowName2 = "programBridgeFlowUnicast_" + dpid2  + "_" + gntOp.getMacaddr(port2) + "_" + vni + "_";
		  if((groupCache.get(flowName)==null)&&(groupCache.get(flowName2)==null)) {
		         Long ofport1 = gntOp.getOfPort(port1,port2); 
		         Long l2_group1 = dpidGroupCache.get(dpid.toString());
		         dpidGroupCache.put(dpid.toString(),new Long(l2_group1 + 1));
		         Long l2_group2 =  dpidGroupCache.get(dpid2.toString());
		         LOG.info("switchgroup dpidGroupCache is {}, dpid1 is {},dpid2 is {} l2_group1 is {},l2_group2 is {}\n",dpidGroupCache,dpid,dpid2,l2_group1,l2_group2);
		         dpidGroupCache.put(dpid2.toString(),new Long(l2_group2 + 1));
                 nodeConnectorInventoryEventTranslator.createL2InterfaceGroupVxlan(dpid,"GNTGROUP",l2_group1,vni,ofport1);
		         LOG.info("------writeL2Flownosamenodeafter-----");
		         tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group1,true);
		
		         groupCache.put(flowName,l2_group1); 

		         // port2 flow.
		         Long ofport2 = gntOp.getOfPort(port2,port1); 
		         tTPTable0Provider.programLocalInPort(dpid2,vni,vni,ofport2,true);
		         nodeConnectorInventoryEventTranslator.createL2InterfaceGroupUntagged(dpid2,"GNTGROUP",l2_group2,gntOp.getofPort(port2));	
                 tTPTable50Provider.programBridgeFlowUnicast(dpid2,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group2,true);	
		         //String flowName2 = "programBridgeFlowUnicast_" + dpid2 + "_" + gntOp.getMacaddr(port1)+ "_" + gntOp.getMacaddr(port2) + "_" + vni + "_L2";
		         groupCache.put(flowName2,l2_group2);

	  	   }
		   if((groupCache.get(flowName)!=null)&&(groupCache.get(flowName2)!=null)) {
		        // todo nothing...
		        

	  	   }
		   if((groupCache.get(flowName)==null)&&(groupCache.get(flowName2)!=null)) {
                 Long ofport1 = gntOp.getOfPort(port1,port2); 
		         Long l2_group1 = dpidGroupCache.get(dpid.toString());
		         dpidGroupCache.put(dpid.toString(),new Long(l2_group1 + 1));
		         Long l2_group2 =  dpidGroupCache.get(dpid2.toString());
		         LOG.info("switchgroup dpidGroupCache is {}, dpid1 is {},dpid2 is {} l2_group1 is {},l2_group2 is {}\n",dpidGroupCache,dpid,dpid2,l2_group1,l2_group2);
		         dpidGroupCache.put(dpid2.toString(),new Long(l2_group2 + 1));
                 nodeConnectorInventoryEventTranslator.createL2InterfaceGroupVxlan(dpid,"GNTGROUP",l2_group1,vni,ofport1);
		         LOG.info("------writeL2Flownosamenodeafter-----");
		         tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group1,true);
		
		         groupCache.put(flowName,l2_group1); 

		         // port2 flow.
		         Long ofport2 = gntOp.getOfPort(port2,port1); 
		         tTPTable0Provider.programLocalInPort(dpid2,vni,vni,ofport2,true);
		         //nodeConnectorInventoryEventTranslator.createL2InterfaceGroupUntagged(dpid2,"GNTGROUP",l2_group2,gntOp.getofPort(port2));	
                 //tTPTable50Provider.programBridgeFlowUnicast(dpid2,gntOp.getMacaddr(port2),gntOp.getNoSymmetryVni(port1),l2_group2,true);	
		         //String flowName2 = "programBridgeFlowUnicast_" + dpid2 + "_" + gntOp.getMacaddr(port1)+ "_" + gntOp.getMacaddr(port2) + "_" + vni + "_L2";
		         //groupCache.put(flowName2,l2_group2);  
  
		   }

		   if((groupCache.get(flowName)!=null)&&(groupCache.get(flowName2)==null)) {
                 // nothing...
  
		   }
		   
 
      	}

  }

   //port1->port2 l3 flow.
  private void writeL3Flow(Port port1,Port port2) {

	  Long dpid = new Long(gntOp.getDpid(port1));
	  Long dpid2 = new Long(gntOp.getDpid(port2));
	  String vni = gntOp.getNoSymmetryVni(port1).toString();
	  tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port1),true);
      tTPTable10Provider.programVlanFlow2(dpid,vni,gntOp.getofPort(port1),true);
	  String attachedMac = gntOp.getGatewayMac(port1);
	  LOG.info("writeL3Flow attachedMac is {}\n",attachedMac);
	  Long metadata = new Long(0l);
	  Long tunnel_key = new Long(0l);
	  Long lowMetadataMask = new Long(0xffffffffl);
	  Long highMetadataMask = new Long(0x7fffffff00000000l);
	  LOG.info("lowMetadataMask is {},highMetadataMask is {}\n",lowMetadataMask,highMetadataMask);
	  String symmetry = "symmetry";
	  String nosymmetry = "nosymmetry";
	  if(symmetry.equals(gntOp.getSwtichType(port2))) {
		   // duicheng;
		   tunnel_key = gntOp.getSymmetryVni(port2);
		   LOG.info("symmetry tunnel_key is {}\n",tunnel_key);
       }
	   if(nosymmetry.equals(gntOp.getSwtichType(port2))) {
          // no duicheng;
          tunnel_key = gntOp.getNoSymmetryVni(port2);
		  LOG.info("nosymmetry tunnel_key is {}\n",tunnel_key);

	  }
	  metadata = gntOp.getSymmetryVni(port2);
      tTPTable20Provider.programMacFlow(dpid,attachedMac,vni,metadata<<32,highMetadataMask,true);	  
	  if(gntOp.isSameTheNode(port1,port2)) {

		   String dstIpaddr = gntOp.getIp(port2);
	  	   String flowName1 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L3_";
		   String flowName2 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
		   if(groupCache.get(flowName1)==null) {
	  	        // port1 port2 is the same node.
	  	        String dstIp = gntOp.getIp(port2);
		        String src = gntOp.getGatewayMac(port2);
		        String des = gntOp.getMacaddr(port2);

		        Long l2_group1 = dpidGroupCache.get(dpid.toString());
		        dpidGroupCache.put(dpid.toString(),new Long(l2_group1 + 1));
		        Long l3_group1 =  dpidGroupCache.get(dpid.toString());
		        dpidGroupCache.put(dpid.toString(),new Long(l3_group1 + 1));

		        groupCache.put(flowName1,l3_group1);
				groupCache.put(flowName2,l2_group1);
		  
		        LOG.info("dstip is {},src is {},des is {}\n",dstIp,src,des);
	            nodeConnectorInventoryEventTranslator.createL2InterfaceGroupUntagged(dpid,"GNTGROUP",l2_group1,gntOp.getofPort(port2));
	            groupProvider.createL3UnicastGroup(dpid,"GNTGROUP",l3_group1 + groupL3id,l2_group1,src, des);
	            tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIp,metadata<<32,l3_group1 + groupL3id,true);
		   	}else {
		   	
		}
		  
      } else {
        // port1 port2 is not the same node.
        // port1 flow.
        
		 String dstIpaddr = gntOp.getIp(port2);
	  	 String flowName1 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L3_";
		 String flowName2 = "programRoutingFlowUnicast_" + dpid + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
		 String flowName3 = "programRoutingFlowUnicast_" + dpid2 + "_" + dstIpaddr + "_" + metadata.toString() + "_L2_";
		 if((groupCache.get(flowName1)==null)&&(groupCache.get(flowName3)==null)) {
	  	      String dstIp = gntOp.getIp(port2);
		      String src = gntOp.getGatewayMac(port2);
		      String des = gntOp.getMacaddr(port2);
		      Long ofport1 = gntOp.getOfPort(port1,port2);

		      Long l2_group1 = dpidGroupCache.get(dpid.toString());
		      dpidGroupCache.put(dpid.toString(),new Long(l2_group1 + 1));
		      Long l3_group1 =  dpidGroupCache.get(dpid.toString());
		      dpidGroupCache.put(dpid.toString(),new Long(l3_group1 + 1));
              Long l2_group2 = dpidGroupCache.get(dpid2.toString());
		      dpidGroupCache.put(dpid2.toString(),new Long(l2_group2 + 1));
		      groupCache.put(flowName1,l3_group1);
		      groupCache.put(flowName2,l2_group1);
		      groupCache.put(flowName3,l2_group2);
		  
		      LOG.info("writeL3Flow dpid1 is {},l2_group1 is {},l3_group1 is {},dpid2 is {},l2_group2 is {}\n",dpid,l2_group1,l3_group1,dpid2,l2_group2);
		      LOG.info("dstip is {},src is {},des is {},ofport is {}\n",dstIp,src,des,ofport1);
	          nodeConnectorInventoryEventTranslator.createL2InterfaceGroupVxlan(dpid,"GNTGROUP",l2_group1,tunnel_key.toString(),ofport1);	      
	          groupProvider.createL3UnicastGroup(dpid,"GNTGROUP",l3_group1 + groupL3id,l2_group1,src, des);
	          tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIp,metadata<<32,l3_group1 + groupL3id,true);	
		      // port2 flow.
		      Long ofport2 = gntOp.getOfPort(port2,port1); 
		      tTPTable0Provider.programLocalInPort(dpid2,metadata.toString(),metadata.toString(),ofport2,true);
		      nodeConnectorInventoryEventTranslator.createL2InterfaceGroupUntagged(dpid2,"GNTGROUP",l2_group2,gntOp.getofPort(port2));
              tTPTable50Provider.programBridgeFlowUnicast(dpid2,gntOp.getMacaddr(port2),metadata,l2_group2,true);		  		 
		  }

		  if((groupCache.get(flowName1)==null)&&(groupCache.get(flowName3)!=null)) {
              String dstIp = gntOp.getIp(port2);
		      String src = gntOp.getGatewayMac(port2);
		      String des = gntOp.getMacaddr(port2);
		      Long ofport1 = gntOp.getOfPort(port1,port2);

		      Long l2_group1 = dpidGroupCache.get(dpid.toString());
		      dpidGroupCache.put(dpid.toString(),new Long(l2_group1 + 1));
		      Long l3_group1 =  dpidGroupCache.get(dpid.toString());
		      dpidGroupCache.put(dpid.toString(),new Long(l3_group1 + 1));
    
		      groupCache.put(flowName1,l3_group1);
		      groupCache.put(flowName2,l2_group1);
		  
		      LOG.info("writeL3Flow dpid1 is {},l2_group1 is {},l3_group1 is {},dpid2 is {},l2_group2 is {}\n",dpid,l2_group1,l3_group1,dpid2);
		      LOG.info("dstip is {},src is {},des is {},ofport is {}\n",dstIp,src,des,ofport1);
	          nodeConnectorInventoryEventTranslator.createL2InterfaceGroupVxlan(dpid,"GNTGROUP",l2_group1,tunnel_key.toString(),ofport1);	      
	          groupProvider.createL3UnicastGroup(dpid,"GNTGROUP",l3_group1 + groupL3id,l2_group1,src, des);
	          tTPTable30Provider.programRoutingFlowUnicast(dpid,dstIp,metadata<<32,l3_group1 + groupL3id,true);	
		      // port2 flow.
		      Long ofport2 = gntOp.getOfPort(port2,port1); 
		      tTPTable0Provider.programLocalInPort(dpid2,metadata.toString(),metadata.toString(),ofport2,true);


		   }

          if((groupCache.get(flowName1)!=null)&&(groupCache.get(flowName3)==null)) {
		  	 // no ke neng...



		   }

		 if((groupCache.get(flowName1)!=null)&&(groupCache.get(flowName3)!=null)) {
              // do nothing...


		   }
		  
		  
		
		 }
		 
        
      }

  




   private void deleteGwL3(Node node,Port port) {

	  LOG.info("-------------writeGwL3112-------------\n");
	  Long dpid = new Long(gntOp.getDataPathId(node));
	  Long vni = gntOp.getNoSymmetryVni(port);

	  Long metadata = new Long(0l);
	  Long tunnel_key = new Long(0l);
	  Long lowMetadataMask = new Long(0xffffffffl);
	  Long highMetadataMask = new Long(0x7fffffff00000000l);
	  LOG.info("lowMetadataMask is {},highMetadataMask is {}\n",lowMetadataMask,highMetadataMask);
	  String symmetry = "symmetry";
	  String nosymmetry = "nosymmetry";
	  
      String attachedMac = gntOp.getGatewayMac(port);
	  metadata = gntOp.getSymmetryVni(port);
	  //metadata = vni;
      
	  tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port),false);
      tTPTable10Provider.programVlanFlow2(dpid,vni.toString(),gntOp.getofPort(port),false);

	  if(gntOp.isLastNetworkPort(node,port)) {
           tTPTable20Provider.programMacFlow(dpid,attachedMac,vni.toString(),metadata<<32,highMetadataMask,false);
      }   

	  String flowName1 = "programRoutingFlowUnicastGw_" + dpid + "_" + metadata.toString() + "_L2";
	  String flowName2 = "programRoutingFlowUnicastGw_" + dpid + "_" + metadata.toString() + "_L3";
	 
	  String flowName3 = "programRoutingFlowUnicastGw_"  + dpid + "_" + gntOp.getMacaddr(port)  + "_";

	  if(gntOp.isLastrouterofNode(node,port)) { 
	  	  LOG.info("------lastrouter1112---");
	      Long l2_group1 = groupCache.get(flowName1);
          groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
	      Long l3_group1 =  groupCache.get(flowName2);
	      groupProvider.destroyGroup(dpid,"GNTGROUP",l3_group1 + groupL3id);
	      groupCache.remove(flowName1);
	      groupCache.remove(flowName2);
          LOG.info("GWdelete l2_group is {},l3_group is {}.",l2_group1,l3_group1);

	      tTPTable30Provider.programRoutingFlowUnicastGw(dpid,metadata<<32,l3_group1 + groupL3id,false);
	      LOG.info("------deleteGWL3 vni is {},metadata is {},l2_group1 is {},l3_group1 + groupL3id is {}------------\n",vni,metadata,l2_group1,l2_group1 + groupL3id);
	  }
	  
	  if(gntOp.isLastrouterofNode(node,port)) {
	  	    LOG.info("------lastrouter1111---");
            Long ofport = gntOp.getOfGWPort(node);
			tTPTable0Provider.programLocalInPort(dpid,vni.toString(),vni.toString(),ofport,false);
	  }	

      Long l2_group11 = groupCache.get(flowName3);
	  groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group11);
	  groupCache.remove(flowName3);
      tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port),vni,l2_group11,false);		  		        
	  
   }

   private void deleteGwL3(Router router,Node node,Port port) {

	  LOG.info("-------------writeGwL3112-------------\n");
	  Long dpid = new Long(gntOp.getDataPathId(node));
	  Long vni = gntOp.getNoSymmetryVni(port);
	  
	  Long metadata = new Long(0l);
	  Long tunnel_key = new Long(0l);
	  Long lowMetadataMask = new Long(0xffffffffl);
	  Long highMetadataMask = new Long(0x7fffffff00000000l);
	  LOG.info("lowMetadataMask is {},highMetadataMask is {}\n",lowMetadataMask,highMetadataMask);
	  String symmetry = "symmetry";
	  String nosymmetry = "nosymmetry";
	  
      String attachedMac = gntOp.getGatewayMac(router,port);
	  metadata = gntOp.getNoSymmetryVni(port);
	  //metadata = vni;
	  LOG.info("deleteGwL3---metadata is {}---",metadata);
	  if(gntOp.isLastNetworkPort(node,port)) {
	  	   LOG.info("----deletetable0,10,20-----");
	  	   tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port),false);
           tTPTable10Provider.programVlanFlow2(dpid,vni.toString(),gntOp.getofPort(port),false);
           tTPTable20Provider.programMacFlow(dpid,attachedMac,vni.toString(),metadata<<32,highMetadataMask,false);
	  }	
	  
	  String flowName1 = "programRoutingFlowUnicastGw_" + dpid + "_" + metadata.toString() + "_L2";
	  String flowName2 = "programRoutingFlowUnicastGw_" + dpid + "_" + metadata.toString() + "_L3";
	 
	  String flowName3 = "programRoutingFlowUnicastGw_"  + dpid + "_" + gntOp.getMacaddr(port)  + "_";
	 
	  Long l2_group1 = groupCache.get(flowName1);
      groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group1);
	  Long l3_group1 =  groupCache.get(flowName2);
	  groupProvider.destroyGroup(dpid,"GNTGROUP",l3_group1 + groupL3id);
	  groupCache.remove(flowName1);
	  groupCache.remove(flowName2);
      LOG.info("GWdelete l2_group is {},l3_group is {}.",l2_group1,l3_group1);

	  tTPTable30Provider.programRoutingFlowUnicastGw(dpid,metadata<<32,l3_group1 + groupL3id,false);
	  LOG.info("------deleteGWL3 vni is {},metadata is {},l2_group1 is {},l3_group1 + groupL3id is {}------------\n",vni,metadata,l2_group1,l2_group1 + groupL3id);
      LOG.info("-----deleteGwL31111---groupCache is {}----",groupCache);
	  if(gntOp.isLastrouterofNode(router,node,port)) {
	  	    LOG.info("------lastrouter---");
            Long ofport = gntOp.getOfGWPort(node);
			tTPTable0Provider.programLocalInPort(dpid,vni.toString(),vni.toString(),ofport,false);
	  }	
      LOG.info("-----deleteGwL3---groupCache is {}----",groupCache);
      Long l2_group11 = groupCache.get(flowName3);
	  groupProvider.destroyGroup(dpid,"GNTGROUP",l2_group11);
	  groupCache.remove(flowName3);
      tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port),vni,l2_group11,false);		  		        
	  
   }
  private void writeGwL3(Port port) {
      LOG.info("-------------writeGwL3111-------------\n");
	  Long dpid = new Long(gntOp.getDpid(port));
	  Long vni = gntOp.getNoSymmetryVni(port);

      tTPTable0Provider.programPhysicalIn(dpid,gntOp.getofPort(port),true);
      tTPTable10Provider.programVlanFlow2(dpid,vni.toString(),gntOp.getofPort(port),true);
	  Long metadata = new Long(0l);
	  Long tunnel_key = new Long(0l);
	  Long lowMetadataMask = new Long(0xffffffffl);
	  Long highMetadataMask = new Long(0x7fffffff00000000l);
	  LOG.info("lowMetadataMask is {},highMetadataMask is {}\n",lowMetadataMask,highMetadataMask);
	  String symmetry = "symmetry";
	  String nosymmetry = "nosymmetry";
	  
      String attachedMac = gntOp.getGatewayMac(port);
	  //metadata = gntOp.getNoSymmetryVni(port);
	  metadata = gntOp.getSymmetryVni(port);
	  //metadata = vni;
      tTPTable20Provider.programMacFlow(dpid,attachedMac,vni.toString(),metadata<<32,highMetadataMask,true);
	  String flowName1 = "programRoutingFlowUnicastGw_" + dpid + "_" + metadata.toString() + "_L2";
	  String flowName2 = "programRoutingFlowUnicastGw_" + dpid + "_" + metadata.toString() + "_L3";
	 
	  String flowName3 = "programRoutingFlowUnicastGw_"  + dpid + "_" + gntOp.getMacaddr(port)  + "_";
	  if(groupCache.get(flowName1)==null) { 

	          Long ofport = gntOp.getOfGWPort(port);
	          //Long groupid = gntOp.getSymmetryVni(port);
	          Long l2_group1 = dpidGroupCache.get(dpid.toString());
	          dpidGroupCache.put(dpid.toString(),new Long(l2_group1 + 1));
	          Long l3_group1 =  dpidGroupCache.get(dpid.toString());
	          dpidGroupCache.put(dpid.toString(),new Long(l3_group1 + 1));
			  LOG.info("GW l2_group is {},l3_group is {}.",l2_group1,l3_group1);

			  //Long l2_group11 = dpidGroupCache.get(dpid.toString());
	          //dpidGroupCache.put(dpid.toString(),new Long(l2_group11 + 1));

	          groupCache.put(flowName1,l2_group1);
	          groupCache.put(flowName2,l3_group1);

			  //groupCache.put(flowName3,l2_group11);
	
	          //tTPTable30Provider.programTable60RoutingFlow(dpid,true);
	          nodeConnectorInventoryEventTranslator.createL2InterfaceGroupVxlan(dpid,"GNTGROUP",l2_group1,vni.toString(),ofport);	
	          groupProvider.createL3UnicastGroupGw(dpid,"GNTGROUP",l3_group1 + groupL3id,l2_group1);
	          tTPTable30Provider.programRoutingFlowUnicastGw(dpid,metadata<<32,l3_group1 + groupL3id,true);
	           //tTPTable60Provider.programOutPort(dpid,vni.toString(),vni,gntOp.getofPort(port),ofport,true);
	          LOG.info("------write GWL3 ofport is {},vni is {},metadata is {},l2_group1 is {},l3_group1 + groupL3id is {}------------\n",ofport,vni,metadata,l2_group1,l2_group1 + groupL3id);


	          // gw flow inport.......inport flow......................
	          LOG.info("-------write GW inport flow------------");
	          //tTPTable0Provider.programPhysicalIn(dpid,ofport,true);
	          tTPTable0Provider.programLocalInPort(dpid,vni.toString(),vni.toString(),ofport,true);
			  
	          //nodeConnectorInventoryEventTranslator.createL2InterfaceGroupUntagged(dpid,"GNTGROUP",l2_group11,gntOp.getofPort(port));
              //tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port),vni,l2_group11,true);		  		 
	  	}
        if(groupCache.get(flowName3)==null) { 
             Long l2_group11 = dpidGroupCache.get(dpid.toString());
	         dpidGroupCache.put(dpid.toString(),new Long(l2_group11 + 1));
		     groupCache.put(flowName3,l2_group11);
	         nodeConnectorInventoryEventTranslator.createL2InterfaceGroupUntagged(dpid,"GNTGROUP",l2_group11,gntOp.getofPort(port));
             tTPTable50Provider.programBridgeFlowUnicast(dpid,gntOp.getMacaddr(port),vni,l2_group11,true);		  		        
        }
   }

  
    @Override
    public void setDependencies(ServiceReference serviceReference) {
        configurationService =
                (ConfigurationService) ServiceHelper.getGlobalInstance(ConfigurationService.class, this);
        networkingProviderManager =
                (NetworkingProviderManager) ServiceHelper.getGlobalInstance(NetworkingProviderManager.class, this);
        tenantNetworkManager =
                (TenantNetworkManager) ServiceHelper.getGlobalInstance(TenantNetworkManager.class, this);
        
        gtnNodeCacheManager =
                (NodeCacheManager) ServiceHelper.getGlobalInstance(NodeCacheManager.class, this);
       
       

		gntOp =
                (GntOp) ServiceHelper.getGlobalInstance(GntOp.class, this);

		gntInventoryService =
                (GntInventoryService) ServiceHelper.getGlobalInstance(GntInventoryService.class, this);
        gntInventoryService.listenerAdded(this);

		eventDispatcher =
                (EventDispatcher) ServiceHelper.getGlobalInstance(EventDispatcher.class, this);
        eventDispatcher.eventHandlerAdded(serviceReference, this);

		tTPTable0Provider =
                (TTPTable0Provider) ServiceHelper.getGlobalInstance(TTPTable0Provider.class, this);

		tTPTable10Provider =
                (TTPTable10Provider) ServiceHelper.getGlobalInstance(TTPTable10Provider.class, this);

		tTPTable20Provider =
                (TTPTable20Provider) ServiceHelper.getGlobalInstance(TTPTable20Provider.class, this);

		tTPTable30Provider =
                (TTPTable30Provider) ServiceHelper.getGlobalInstance(TTPTable30Provider.class, this);

	    tTPTable50Provider =
                (TTPTable50Provider) ServiceHelper.getGlobalInstance(TTPTable50Provider.class, this);

		tTPTable60Provider =
                (TTPTable60Provider) ServiceHelper.getGlobalInstance(TTPTable60Provider.class, this);

		tTPTable61Provider =
                (TTPTable61Provider) ServiceHelper.getGlobalInstance(TTPTable61Provider.class, this);	
		groupProvider =
                (GroupProvider) ServiceHelper.getGlobalInstance(GroupProvider.class, this);

	    gntUtils =
                (GntUtils) ServiceHelper.getGlobalInstance(GntUtils.class, this);

        inifile =
                (IniFile) ServiceHelper.getGlobalInstance(IniFile.class, this);
		

	    neutronSecurityRuleCache = 
	  	        (INeutronSecurityRuleCRUD) ServiceHelper.getGlobalInstance(INeutronSecurityRuleCRUD.class, this);

		nodeConnectorInventoryEventTranslator = 
			    (NodeConnectorInventoryEventTranslator) ServiceHelper.getGlobalInstance(NodeConnectorInventoryEventTranslator.class, this);

    }

    @Override
    public void setDependencies(Object impl) {
        
		if (impl instanceof TTPTable0Provider) {
            tTPTable0Provider = (TTPTable0Provider)impl;
        }else if (impl instanceof TTPTable10Provider) {
            tTPTable10Provider = (TTPTable10Provider)impl;
        }else if (impl instanceof TTPTable20Provider) {
            tTPTable20Provider = (TTPTable20Provider)impl;
        }else if (impl instanceof TTPTable30Provider) {
            tTPTable30Provider = (TTPTable30Provider)impl;
        }else if (impl instanceof TTPTable50Provider) {
            tTPTable50Provider = (TTPTable50Provider)impl;
        }else if (impl instanceof TTPTable60Provider) {
            tTPTable60Provider = (TTPTable60Provider)impl;
        }else if (impl instanceof TTPTable61Provider) {
            tTPTable61Provider = (TTPTable61Provider)impl;
        }else if (impl instanceof GroupProvider) {
            groupProvider = (GroupProvider)impl;
        }else  if (impl instanceof INeutronNetworkCRUD) {
            neutronNetworkCache = (INeutronNetworkCRUD)impl;
        }

	  }
}

