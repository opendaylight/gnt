/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.gnt.utils.portspeaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.*;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;

import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.port.attributes.*;
import org.opendaylight.gnt.utils.gnt.utils.*;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.ports.rev150712.ports.attributes.ports.PortBuilder;
import java.util.HashMap;

/**
 * Objects of this class send LLDP frames over all flow-capable ports that can
 * be discovered through inventory.
 */
public class LLDPSpeaker implements AutoCloseable, NodeConnectorEventsObserver, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPSpeaker.class);
    private static final long LLDP_FLOOD_PERIOD = 5;

    //private final PacketProcessingService packetProcessingService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Map<InstanceIdentifier<NodeConnector>, String> nodeConnectorMap =
            new ConcurrentHashMap<>();
    private final ScheduledFuture<?> scheduledSpeakerTask;

    public Map<InstanceIdentifier<NodeConnector>,PortBuilder> portBuilderMap = 
		new ConcurrentHashMap<>();

	public GntUtils gntUtils;

	public GroupService groupProvider;

	public  Map<String,Long> l2_group = new HashMap<String,Long>();

	public  Map<String,String> l2_flow = new HashMap<String,String>();
	public  Map<String,String> l3_flow = new HashMap<String,String>();
	
    public LLDPSpeaker(DataBroker dataBroker) {
	
        this(Executors.newSingleThreadScheduledExecutor());
	    gntUtils = new GntUtils(dataBroker);
		groupProvider = new GroupService(dataBroker);
    }


    public LLDPSpeaker(
                       final ScheduledExecutorService scheduledExecutorService) {
                       
        this.scheduledExecutorService = scheduledExecutorService;
        scheduledSpeakerTask = this.scheduledExecutorService
                .scheduleAtFixedRate(this, LLDP_FLOOD_PERIOD,LLDP_FLOOD_PERIOD, TimeUnit.SECONDS);
        LOG.info("------------------PortSpeaker-----------");
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     */
    @Override
    public void close() {
        nodeConnectorMap.clear();
        scheduledExecutorService.shutdown();
        scheduledSpeakerTask.cancel(true);
        LOG.trace("PortSpeaker stopped sending LLDP frames.");
    }

	
    /**
     * Send LLDPDU frames to all known openflow switch ports.
     */
    @Override
    public void run() {
        if (true) {
         /*   LOG.info("portSpeaker is running!....");
              for (InstanceIdentifier<NodeConnector> nodeConnectorInstanceId : nodeConnectorMap.keySet()) {
                   NodeConnectorId nodeConnectorId = InstanceIdentifier.keyOf(nodeConnectorInstanceId).getId();
                
            }*/
        }
    }

    public InstanceIdentifier<NodeConnector> createInstanceIdentifier(String dpid,Long ofport) {
		String nodeuri = "openflow:" + dpid;
		String nodeconnectoruri = "openflow:" + dpid + ":" + ofport.toString();
		NodeId nodeId = new NodeId(nodeuri);
		NodeKey key1 = new NodeKey(nodeId);
        NodeConnectorId nodeConnectorId = new NodeConnectorId(nodeconnectoruri);		
		NodeConnectorKey key2 = new NodeConnectorKey(nodeConnectorId);
        return  InstanceIdentifier.create(Nodes.class)
            .child(Node.class,key1)
            .child(NodeConnector.class,key2);
    }

	public boolean isPortDown(String dpid,Long ofport) {
        //String uri = "openflow:" + dpid + ":" + ofport.toString(); 
        InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = createInstanceIdentifier(dpid,ofport);
		LOG.info("isPortDown nodeConnectorInstanceId is {},nodeConnectorMap is {}!",nodeConnectorInstanceId,nodeConnectorMap);
		if (nodeConnectorMap.containsKey(nodeConnectorInstanceId)) {
			LOG.info("isPortup");
            return true;
        }

		return false;


	}

	public void addL2Group(String dpid,String ofport,String groupid,Long vni) {
		
		  String key = dpid + ":" + ofport + ":" + groupid;
		  l2_group.put(key,vni);
		  return ;

   }	

   public void delL2Group(String dpid,String ofport,String groupid) {
		
		  String key = dpid + ":" + ofport + ":" + groupid;
		  l2_group.remove(key);
		  return ;

   }	


   public void addL2Flow(String dpid,String ofport,String groupid,String vni,String macaddr) {
		
		  String key = dpid + ":" + ofport + ":" + groupid;
		  String value = vni + "," + macaddr;
		  l2_flow.put(key,value);
		  return ;

   }	

   public void delL2Flow(String dpid,String ofport,String groupid) {
		
		  String key = dpid + ":" + ofport + ":" + groupid;
		  l2_group.remove(key);
		  return ;

   }	

    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeConnectorAdded(final InstanceIdentifier<NodeConnector> nodeConnectorInstanceId,
                                   final FlowCapableNodeConnector flowConnector) {
        NodeConnectorId nodeConnectorId = InstanceIdentifier.keyOf(nodeConnectorInstanceId).getId();
        LOG.info("1111nodeConnectorAdded111");  
        // nodeConnectorAdded can be called even if we already sending LLDP
        // frames to
        // port, so first we check if we actually need to perform any action
        if (nodeConnectorMap.containsKey(nodeConnectorInstanceId)) {
            LOG.trace(
                    "Port {} already in LLDPSpeaker.nodeConnectorMap, no need for additional processing",
                    nodeConnectorId.getValue());
            return;
        }

        // Prepare to build LLDP payload
        InstanceIdentifier<Node> nodeInstanceId = nodeConnectorInstanceId.firstIdentifierOf(Node.class);
        NodeId nodeId = InstanceIdentifier.keyOf(nodeInstanceId).getId();
        MacAddress srcMacAddress = flowConnector.getHardwareAddress();
        Long outputPortNo = flowConnector.getPortNumber().getUint32();
        LOG.info("--nodeConnectorInstanceId is {}--nodeConnectorAdded----nodeid is {},port is {}-----------",nodeConnectorId,nodeId,outputPortNo);

        if (outputPortNo == null) {
            LOG.trace("Port {} is local, not sending LLDP frames through it", nodeConnectorId.getValue());
            return;
        }

		

		nodeConnectorMap.put(nodeConnectorInstanceId,"ok");
	    NodeConnectorKey key = new NodeConnectorKey(nodeConnectorId);
	    String uri = key.toString();
	    LOG.info("nodeConnectorAdded uri is {}.",uri);
        int len =  uri.indexOf(":");
	    String dpid = uri.substring(len);
	    dpid = dpid.substring(1,dpid.lastIndexOf(":"));
	    int lastlenstart =  uri.lastIndexOf(":");
		int lastlenend =  uri.lastIndexOf("]");
	    Long ofport = new Long(Long.parseLong(uri.substring(lastlenstart+1,lastlenend-1)));
	    //Long ofport = outputPortNo;
	    LOG.info("nodeConnectorAdded  dpid is {},ofport is {}..",dpid,ofport);

		if(portBuilderMap.containsKey(nodeConnectorInstanceId)) {
			PortBuilder portBuilder = portBuilderMap.get(nodeConnectorInstanceId);
            LOG.info("portisupcreateport portBuilder is {}!",portBuilder);
			gntUtils.addPort(dpid,portBuilder.build(),ofport);
			
		} else {
            String outkey = dpid + ":" + ofport.toString() + ":";
		    for (String key_l2 : l2_group.keySet()) {
	              if(key_l2.indexOf(outkey)!=-1) {
                         //l2_group...
                         LOG.info("----nodeConnectorAdded--start to l2 flow-----------");
                         Long dpidL = new Long(Long.parseLong(dpid));
                         LOG.info("nodeConnectorAdded---starttol2group----dpid is {},ofport is {}---",dpid,ofport);
                         Long vni = l2_group.get(key_l2);
						 int lastlen1 =  key_l2.lastIndexOf(":");
						 Long groupid = new Long(Long.parseLong(key_l2.substring(lastlen1 + 1)));
						 LOG.info("nodeConnectorAdded---starttol2group----groupid is {},vni is {}---",groupid,vni);
						 if(vni > 0) {
						 	 LOG.info("nodeConnectorAdded1234");
                             groupProvider.createL2InterfaceGroupVxlan(dpidL,"GNTGROUP",groupid,vni.toString(),ofport);
							 LOG.info("nodeConnectorAdded12345");
							 Long groupid3 = groupProvider.reGroupL3(dpidL,groupid);
							 LOG.info("nodeConnectorAdded123456");
							 LOG.info("nodeConnectorAdded groupid3 is {}.",groupid3);
							 if(groupid3 > 0) {
							     groupProvider.reFlowTable(dpidL,groupid3,(short)30);
							 }  else {
                                 groupProvider.reFlowTable(dpidL,groupid,(short)50);                                  
							 }
						 } else {
                             groupProvider.createL2InterfaceGroupUntagged(dpidL,"GNTGROUP",groupid,ofport);
                             Long groupid3 = groupProvider.reGroupL3(dpidL,groupid);
                             if(groupid3 > 0) {
							     groupProvider.reFlowTable(dpidL,groupid3,(short)30);
							 }  else {
                                 groupProvider.reFlowTable(dpidL,groupid,(short)50);                                  
							 }
						 }

						 //l2_group.remove(key_l2);						 

					  }	
                }
              



		}
        

    }


	public void addPort(String dpid,PortBuilder portBuilder,Long ofport) {
        InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = createInstanceIdentifier(dpid,ofport);		
		portBuilderMap.put(nodeConnectorInstanceId,portBuilder);
		return ;
	   
    }

   public void delPort(String dpid,PortBuilder portBuilder,Long ofport) {
        InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = createInstanceIdentifier(dpid,ofport);		
		portBuilderMap.remove(nodeConnectorInstanceId);
		return ;
	   
    }

   public void createL2InterfaceGroupVxlan(Long dpid,String groupName,Long groupid,String segmentationId, Long ofPort) {
        InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = createInstanceIdentifier(dpid.toString().toString(),ofPort);
		if (nodeConnectorMap.containsKey(nodeConnectorInstanceId)) {

			// add flow....
			groupProvider.createL2InterfaceGroupVxlan(dpid,groupName,groupid,segmentationId,ofPort);
			LOG.info("--dpid---is {}--ofport is {}--nodeConnectorAdded--createL2InterfaceGroupVxlan-----",dpid,ofPort);

        } else {

		}

		Long vni = new Long(Long.parseLong(segmentationId));;
	    addL2Group(dpid.toString(),ofPort.toString(),groupid.toString(),vni);
	    LOG.info("---dpid---is {}---ofport is {}---nodeConnectorAdded--addl2-----",dpid,ofPort);

		return ;

   }

   public void createL2InterfaceGroupUntagged(Long dpid,String groupName,Long groupid,Long ofPort) {

        InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = createInstanceIdentifier(dpid.toString(),ofPort);
		if (nodeConnectorMap.containsKey(nodeConnectorInstanceId)) {

			// add flow....
			groupProvider.createL2InterfaceGroupUntagged(dpid,groupName,groupid,ofPort);
			LOG.info("----nodeConnectorAdded--createL2InterfaceGroupUntagged-----");
        } else {

		}

		Long vni = 0L;
		addL2Group(dpid.toString(),ofPort.toString(),groupid.toString(),vni);
	    LOG.info("----nodeConnectorAdded--addl2-----");

		return ;

   }

   /*

   public void createL2VxlanAll(Long dpid,String groupName,Long groupid,String segmentationId, Long ofPort,String macaddr){

       
	       InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = createInstanceIdentifier(dpid.toString().toString(),ofPort);
		   if (nodeConnectorMap.containsKey(nodeConnectorInstanceId)) {
	   
			   // add flow....
			   Long vni = new Long(Long.parseLong(segmentationId));
			   groupProvider.createL2InterfaceGroupVxlan(dpid,groupName,groupid,segmentationId,ofPort);
			   groupProvider.programBridgeFlowUnicast(dpid,macaddr,vni,groupid,true);
			   LOG.info("-createL2VxlanAll----dpid---is {}--ofport is {}--nodeConnectorAdded--createL2InterfaceGroupVxlan-----",dpid,ofPort);
	   
		   } else {
	   
		   }
	   
		   addL2Flow(dpid.toString(),ofPort.toString(),groupid.toString(),segmentationId,macaddr);
		   LOG.info("-createL2VxlanAll--dpid---is {}---ofport is {}---nodeConnectorAdded--addl2-----",dpid,ofPort);
	   
		   return ;

   }	
   
   public void createL2UntaggedAll(Long dpid,String groupName,Long groupid,String segmentationId,Long ofPort,String macaddr){

        InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = createInstanceIdentifier(dpid.toString(),ofPort);
		if (nodeConnectorMap.containsKey(nodeConnectorInstanceId)) {

			// add flow....
			Long vni = new Long(Long.parseLong(segmentationId));
			groupProvider.createL2InterfaceGroupUntagged(dpid,groupName,groupid,ofPort);
			groupProvider.programBridgeFlowUnicast(dpid,macaddr,vni,groupid,true);
			LOG.info("----nodeConnectorAdded--createL2InterfaceGroupUntagged-----");
        } else {

		}

		Long vni = 0L;
		addL2Flow(dpid.toString(),ofPort.toString(),groupid.toString(),segmentationId,macaddr);
	    LOG.info("----nodeConnectorAdded--addl2-----");

		return ;

   }*/


    public void destroyGroup(Long dpid,String groupName,Long groupid,Long ofport) {

		InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = createInstanceIdentifier(dpid.toString(),ofport);
		if (nodeConnectorMap.containsKey(nodeConnectorInstanceId)) {
			// destory flow....
			groupProvider.destroyGroup(dpid,groupName,groupid);
			LOG.info("----nodeConnectorAdded--destroyGroup1-----");
        } else {
            Long vni = 0L;
			delL2Group(dpid.toString(),ofport.toString(),groupid.toString());
			LOG.info("----nodeConnectorAdded--destroyGroup2-----");
		}

		return ;   



    }	


   public void createL2InterfaceGroupTagged(Long dpid,String groupName,Long groupid,Long ofPort) {

        InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = createInstanceIdentifier(dpid.toString(),ofPort);
		if (nodeConnectorMap.containsKey(nodeConnectorInstanceId)) {

			// add flow....
			groupProvider.createL2InterfaceGroupTagged(dpid,groupName,groupid,ofPort);
			LOG.info("----nodeConnectorAdded--createL2InterfaceGroupTagged-----");
        } else {

		}
		
		Long vni = 0L;
	    addL2Group(dpid.toString(),ofPort.toString(),groupid.toString(),vni);
	    LOG.info("----nodeConnectorAdded--addl2-----");

		return ;
   }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeConnectorRemoved(final InstanceIdentifier<NodeConnector> nodeConnectorInstanceId) {
        nodeConnectorMap.remove(nodeConnectorInstanceId);
        NodeConnectorId nodeConnectorId = InstanceIdentifier.keyOf(nodeConnectorInstanceId).getId();
        LOG.info("--------nodeConnectorRemoved nodeid is {}-------", nodeConnectorId.getValue());

		NodeConnectorKey key = new NodeConnectorKey(nodeConnectorId);
	    String uri = key.toString();
	    LOG.info("nodeConnectorRemoved uri is {}.",uri);
        int len =  uri.indexOf(":");
	    String dpid = uri.substring(len);
	    dpid = dpid.substring(1,dpid.lastIndexOf(":"));
		
        int lastlenstart =  uri.lastIndexOf(":");
		int lastlenend =  uri.lastIndexOf("]");
	    Long ofport = new Long(Long.parseLong(uri.substring(lastlenstart+1,lastlenend-2)));
	    LOG.info("nodeConnectorRemoved  dpid is {},ofport is {}..",dpid,ofport);

		
	    if(portBuilderMap.containsKey(nodeConnectorInstanceId)) {
		    PortBuilder portBuilder = portBuilderMap.get(nodeConnectorInstanceId);
            LOG.info("portidowndropport portBuilder is {}!",portBuilder);
			LOG.info("nodeConnectorRemoved  dpid is {},ofport is {}..",dpid,ofport);
			gntUtils.delPort(dpid,portBuilder.build(),ofport);
			
		} else {

			String outkey = dpid + ":" + ofport.toString() + ":";
		    for (String key_l2 : l2_group.keySet()) {
	              if(key_l2.indexOf(outkey)!=-1) {
                         //l2_group...
                         LOG.info("----nodeConnectorRemoved--start to l2 flow-----------");
                         Long dpidL = new Long(Long.parseLong(dpid));
                         LOG.info("nodeConnectorRemoved---starttol2group----dpid is {},ofport is {}---",dpid,ofport);
                         Long vni = l2_group.get(key_l2);
						 int lastlen1 =  key_l2.lastIndexOf(":");
						 Long groupid = new Long(Long.parseLong(key_l2.substring(lastlen1 + 1)));
						 LOG.info("nodeConnectorRemoved---starttol2group----groupid is {},vni is {}---",groupid,vni);
                         groupProvider.destroyGroup(dpidL,"GNTGROUP",groupid);

				  }	


		      }
		
		   }
    	}

}
