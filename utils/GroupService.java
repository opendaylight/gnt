/*
 * Copyright (c) 2017 china mobile, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.gnt.utils.portspeaker;
import com.google.common.base.Optional;

import java.util.concurrent.ExecutionException;


import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
//group java io
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;

import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;


import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg0;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.netvirt.utils.mdsal.openflow.ActionUtils;
import org.opendaylight.netvirt.utils.mdsal.openflow.FlowUtils;
import org.opendaylight.netvirt.utils.mdsal.openflow.InstructionUtils;
import org.opendaylight.netvirt.utils.mdsal.openflow.MatchUtils;
import java.math.BigInteger;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.*;
import java.util.ArrayList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.*;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.*;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import com.google.common.collect.Lists;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.*;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import java.lang.*;

//add for group operational
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.actions._case.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.*;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;

public class GroupService {
    private static final Logger LOG = LoggerFactory.getLogger(GroupService.class);
    private static final String OPENFLOW = "openflow";


   protected DataBroker dataBroker = null;
  	
   public GroupService(DataBroker dataBroker1) {
        dataBroker = dataBroker1;
    }
	
	
	public  String getNodeName(char nodeuuid) {
        return OPENFLOW + ":" + nodeuuid;
    }

    public  NodeConnectorId getNodeConnectorId(long ofPort, String nodeName) {
        return new NodeConnectorId(nodeName + ":" + ofPort);
    }

    public  NodeConnectorId getSpecialNodeConnectorId(char nodeuuid, String portName) {
        return new NodeConnectorId(getNodeName(nodeuuid) + ":" + portName);
    }

    public  NodeConnectorId getNodeConnectorId(char nodeuuid, long ofPort) {
        return getNodeConnectorId(ofPort, getNodeName(nodeuuid));
    }



  

    public  InstanceIdentifier<Group> createGroupPath(GroupBuilder groupBuilder, NodeBuilder nodeBuilder) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, nodeBuilder.getKey())
                .augmentation(FlowCapableNode.class)
                .child(Group.class,groupBuilder.getKey()).build();
    }

	
    public static InstanceIdentifier<Node> createNodePath(NodeBuilder nodeBuilder) {
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeBuilder.getKey()).build();
    }



	
    public  void createL2InterfaceGroupVxlan(Long dpid,String groupName,Long groupid,String segmentationId, Long ofPort){

       
       NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpid);
	   GroupBuilder groupBuilder = new GroupBuilder();
	   initGroupBuilder(groupBuilder,groupName,groupid,2);
	   
	    List<Action> actionList = new ArrayList<>();
        ActionBuilder ab1 = new ActionBuilder();
		ActionBuilder ab2 = new ActionBuilder();
        ActionBuilder ab3 = new ActionBuilder();

        PopVlanActionBuilder popVlanActionBuilder = new PopVlanActionBuilder();
        ab1.setAction(new PopVlanActionCaseBuilder().setPopVlanAction(popVlanActionBuilder.build()).build());
        ab1.setOrder(0);
        actionList.add(ab1.build());

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();

        // Build the Set Tunnel Field Action
        TunnelBuilder tunnel = new TunnelBuilder();
        tunnel.setTunnelId(new BigInteger(segmentationId));
        setFieldBuilder.setTunnel(tunnel.build());
        ab2.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab2.setOrder(1);
        ab2.setKey(new ActionKey(1));
        actionList.add(ab2.build());

        OutputActionBuilder oab = new OutputActionBuilder();
		NodeConnectorId ncid = new NodeConnectorId("openflow:" + dpid + ":" + ofPort);
        oab.setOutputNodeConnector(ncid);

        ab3.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
        ab3.setOrder(2);
        ab3.setKey(new ActionKey(2));
        actionList.add(ab3.build());	

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

		BucketBuilder bb = new BucketBuilder();
        BucketsBuilder bsb = new BucketsBuilder();
		List<Bucket> bs = Lists.newArrayList();
		
		
	    bb.setAction(actionList);
		bb.setBucketId(new BucketId(1L));
	    bs.add(bb.build());
        bsb.setBucket(bs);
		groupBuilder.setBuckets(bsb.build());		
		writeGroup(groupBuilder,nodeBuilder);

   	}	
   
   public void createL2InterfaceGroupUntagged(Long dpid,String groupName,Long groupid,Long ofPort){

       NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpid);
	   GroupBuilder groupBuilder = new GroupBuilder();
	   initGroupBuilder(groupBuilder,groupName,groupid,2);
	   
	    List<Action> actionList = new ArrayList<>();
        ActionBuilder ab1 = new ActionBuilder();
		ActionBuilder ab2 = new ActionBuilder();
        ActionBuilder ab3 = new ActionBuilder();

        PopVlanActionBuilder popVlanActionBuilder = new PopVlanActionBuilder();
        ab1.setAction(new PopVlanActionCaseBuilder().setPopVlanAction(popVlanActionBuilder.build()).build());
        ab1.setOrder(0);
        actionList.add(ab1.build());


        OutputActionBuilder oab = new OutputActionBuilder();
		NodeConnectorId ncid = new NodeConnectorId("openflow:" + dpid + ":" + ofPort);
        oab.setOutputNodeConnector(ncid);

        ab3.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
        ab3.setOrder(1);
        ab3.setKey(new ActionKey(1));
        actionList.add(ab3.build());	

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

		BucketBuilder bb = new BucketBuilder();
        BucketsBuilder bsb = new BucketsBuilder();
		List<Bucket> bs = Lists.newArrayList();
		
		
	    bb.setAction(actionList);
		bb.setBucketId(new BucketId(1L));
	    bs.add(bb.build());
        bsb.setBucket(bs);
		groupBuilder.setBuckets(bsb.build());
		writeGroup(groupBuilder,nodeBuilder);

   	}

    public void createL2InterfaceGroupTagged (Long dpid,String groupName,Long groupid,Long ofPort){

       NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpid);
	   GroupBuilder groupBuilder = new GroupBuilder();
	   initGroupBuilder(groupBuilder,groupName,groupid,2);
	   
	    List<Action> actionList = new ArrayList<>();
        ActionBuilder ab1 = new ActionBuilder();
		ActionBuilder ab2 = new ActionBuilder();
        ActionBuilder ab3 = new ActionBuilder();

        PushVlanActionBuilder pushVlanActionBuilder = new PushVlanActionBuilder();
        ab1.setAction(new PushVlanActionCaseBuilder().setPushVlanAction(pushVlanActionBuilder.build()).build());
        ab1.setOrder(0);
        actionList.add(ab1.build());


        OutputActionBuilder oab = new OutputActionBuilder();
		NodeConnectorId ncid = new NodeConnectorId("openflow:" + dpid + ":" + ofPort);
        oab.setOutputNodeConnector(ncid);

        ab3.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
        ab3.setOrder(1);
        ab3.setKey(new ActionKey(1));
        actionList.add(ab3.build());	

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

		BucketBuilder bb = new BucketBuilder();
        BucketsBuilder bsb = new BucketsBuilder();
		List<Bucket> bs = Lists.newArrayList();
		
		
	    bb.setAction(actionList);
		bb.setBucketId(new BucketId(1L));
	    bs.add(bb.build());
        bsb.setBucket(bs);
		groupBuilder.setBuckets(bsb.build());
		writeGroup(groupBuilder,nodeBuilder);

   	}


	public void createL3UnicastGroup(Long dpid,String groupName,Long groupid,Long l2_group_id,String src,String des){

       NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpid);
	   GroupBuilder groupBuilder = new GroupBuilder();
	   initGroupBuilder(groupBuilder,groupName,groupid,2);
	   LOG.info("createL3UnicastGroup group id is {}\n",groupid);
	    List<Action> actionList = new ArrayList<>();
        ActionBuilder ab1 = new ActionBuilder();
		ActionBuilder ab2 = new ActionBuilder();
        ActionBuilder ab3 = new ActionBuilder();

        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        EthernetMatchBuilder ethernet1 = new EthernetMatchBuilder();
		if (src != null) {
            EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
            ethSourceBuilder.setAddress(new MacAddress(src));
            ethernet1.setEthernetSource(ethSourceBuilder.build());
        }
        /*if (des != null) {
            EthernetDestinationBuilder ethDestinationBuild = new EthernetDestinationBuilder();
            ethDestinationBuild.setAddress(new MacAddress(des));
            ethernet.setEthernetDestination(ethDestinationBuild.build());
        }*/
		setFieldBuilder1.setEthernetMatch(ethernet1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setOrder(0);
        ab1.setKey(new ActionKey(0));
        actionList.add(ab1.build());

	    SetFieldBuilder setFieldBuilder2 = new SetFieldBuilder();
        EthernetMatchBuilder ethernet2 = new EthernetMatchBuilder();
		/*if (src != null) {
            EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
            ethSourceBuilder.setAddress(new MacAddress(src));
            ethernet.setEthernetSource(ethSourceBuilder.build());
        }*/
        if (des != null) {
            EthernetDestinationBuilder ethDestinationBuild = new EthernetDestinationBuilder();
            ethDestinationBuild.setAddress(new MacAddress(des));
            ethernet2.setEthernetDestination(ethDestinationBuild.build());
        }
		setFieldBuilder2.setEthernetMatch(ethernet2.build());
        ab2.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder2.build()).build());
        ab2.setOrder(1);
        ab2.setKey(new ActionKey(1));
        actionList.add(ab2.build());

        GroupActionBuilder gab = new GroupActionBuilder();
		gab.setGroupId(l2_group_id);

        ab3.setAction(new GroupActionCaseBuilder().setGroupAction(gab.build()).build());
        ab3.setOrder(2);
        ab3.setKey(new ActionKey(2));
        actionList.add(ab3.build());	

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

		BucketBuilder bb = new BucketBuilder();
        BucketsBuilder bsb = new BucketsBuilder();
		List<Bucket> bs = Lists.newArrayList();
		
		
	    bb.setAction(actionList);
		bb.setBucketId(new BucketId(1L));
	    bs.add(bb.build());
        bsb.setBucket(bs);
		groupBuilder.setBuckets(bsb.build());
		writeGroup(groupBuilder,nodeBuilder);

   	}


   	public void createL3UnicastGroupGw(Long dpid,String groupName,Long groupid,Long l2_group_id){

       NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpid);
	   GroupBuilder groupBuilder = new GroupBuilder();
	   initGroupBuilder(groupBuilder,groupName,groupid,2);
	   LOG.info("createL3UnicastGroupGw group id is {}\n",groupid);
	    List<Action> actionList = new ArrayList<>();
        ActionBuilder ab3 = new ActionBuilder();

        GroupActionBuilder gab = new GroupActionBuilder();
		gab.setGroupId(l2_group_id);

        ab3.setAction(new GroupActionCaseBuilder().setGroupAction(gab.build()).build());
        ab3.setOrder(0);
        ab3.setKey(new ActionKey(0));
        actionList.add(ab3.build());	

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

		BucketBuilder bb = new BucketBuilder();
        BucketsBuilder bsb = new BucketsBuilder();
		List<Bucket> bs = Lists.newArrayList();
		
		
	    bb.setAction(actionList);
		bb.setBucketId(new BucketId(1L));
	    bs.add(bb.build());
        bsb.setBucket(bs);
		groupBuilder.setBuckets(bsb.build());
		writeGroup(groupBuilder,nodeBuilder);

   	}

    public static InstructionBuilder createGroupInstructions(InstructionBuilder ib,Long group_id) {

        List<Action> actionList = new ArrayList<>();
        ActionBuilder ab = new ActionBuilder();
		ActionBuilder ab2 = new ActionBuilder();
	    
        ab.setAction(ActionUtils.groupAction(group_id));
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

		DecNwTtlBuilder decNwTtlBuilder = new DecNwTtlBuilder();	    
        ab2.setAction(new DecNwTtlCaseBuilder().setDecNwTtl(decNwTtlBuilder.build()).build());
        ab2.setOrder(1);
        ab2.setKey(new ActionKey(1));
        actionList.add(ab2.build());
		

        WriteActionsBuilder wab = new WriteActionsBuilder();
        wab.setAction(actionList);

        // Wrap the Apply Action in an InstructionBuilder and return
        ib.setInstruction(new WriteActionsCaseBuilder().setWriteActions(wab.build()).build());

        return ib;
    }


    public static InstructionBuilder createGroupInstructionsGw(InstructionBuilder ib,Long group_id) {

        List<Action> actionList = new ArrayList<>();
        ActionBuilder ab = new ActionBuilder();
		ActionBuilder ab2 = new ActionBuilder();
	    
        ab.setAction(ActionUtils.groupAction(group_id));
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
		

        WriteActionsBuilder wab = new WriteActionsBuilder();
        wab.setAction(actionList);

        // Wrap the Apply Action in an InstructionBuilder and return
        ib.setInstruction(new WriteActionsCaseBuilder().setWriteActions(wab.build()).build());

        return ib;
    }

	
   public void programBridgeFlowUnicast(Long dpidLong,String dstmacaddr,Long metadata,Long l2_group_id,boolean write) {

	   NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpidLong);
	   FlowBuilder flowBuilder = new FlowBuilder();
	   String flowName = "programBridgeFlowUnicast_" + dpidLong  + "_" + dstmacaddr + "_" + metadata.toString() + "_";
	   FlowUtils.initFlowBuilder(flowBuilder, flowName, (short)50).setPriority(10000);
   
	   MatchBuilder matchBuilder = new MatchBuilder();
	   //long zerol = 176362094591l;
	   long zerol = 0xffffffffl;
	   
	   MatchUtils.createMetadataMatch(matchBuilder,BigInteger.valueOf(metadata),BigInteger.valueOf(zerol));
	   MatchUtils.createEthSrcDstMatch(matchBuilder,null, new MacAddress(dstmacaddr));
       flowBuilder.setMatch(matchBuilder.build());
	   
   
	   if (write) {
	   	
		   InstructionBuilder ib2 = new InstructionBuilder();
		   InstructionBuilder ib3 = new InstructionBuilder();
		   InstructionsBuilder isb = new InstructionsBuilder();
		
		   // Instructions List Stores Individual Instructions
		   List<Instruction> instructions = Lists.newArrayList();

           createGroupInstructions(ib3,l2_group_id);
		   ib3.setOrder(0);
		   ib3.setKey(new InstructionKey(0));	   
		   instructions.add(ib3.build());
		
		   // Next service GOTO Instructions Need to be appended to the List
		   InstructionUtils.createGotoTableInstructions(ib2,(short)60);
		   ib2.setOrder(1);
		   ib2.setKey(new InstructionKey(1));
		   instructions.add(ib2.build());
		


			// Add InstructionBuilder to the Instruction(s)Builder List
		   isb.setInstruction(instructions);
		

			// Add InstructionsBuilder to FlowBuilder
	       flowBuilder.setInstructions(isb.build());
   
		   writeFlow(flowBuilder, nodeBuilder);
		
	   } else {
		   removeFlow(flowBuilder, nodeBuilder);
	   }

   	}  


	public void programRoutingFlowUnicast(Long dpidLong,String dstIp,Long metadata,Long l3_group_id,boolean write) {

	   NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpidLong);
	   FlowBuilder flowBuilder = new FlowBuilder();
	   String flowName = "programRoutingFlowUnicast_" + dpidLong + "_" + dstIp + "_" + metadata.toString() + "_";
	   FlowUtils.initFlowBuilder(flowBuilder, flowName, (short)30).setPriority(10000);
   
	   MatchBuilder matchBuilder = new MatchBuilder();
	   long zerol = 0x7fffffff00000000l;
	   
	   //long zerol = 0xffffffffl;
	   MatchUtils.createMetadataMatch(matchBuilder,BigInteger.valueOf(metadata),BigInteger.valueOf(zerol));
	   

	   MatchUtils.createEtherTypeMatch(matchBuilder, new EtherType(0x0800L));

	   Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
       ipv4match.setIpv4Destination(new Ipv4Prefix(dstIp + "/32"));
       matchBuilder.setLayer3Match(ipv4match.build());
	   flowBuilder.setMatch(matchBuilder.build());
   
	   if (write) {

        
		   //InstructionBuilder ib1 = new InstructionBuilder();
		   InstructionBuilder ib2 = new InstructionBuilder();
		   InstructionBuilder ib3 = new InstructionBuilder();
		   InstructionsBuilder isb = new InstructionsBuilder();
		
		   // Instructions List Stores Individual Instructions
		   List<Instruction> instructions = Lists.newArrayList();

           createGroupInstructions(ib3,l3_group_id);
		   ib3.setOrder(1);
		   ib3.setKey(new InstructionKey(1));	   
		   instructions.add(ib3.build());

          
		  //createSetTTLInstructions(ib3);
		  //ib3.setOrder(2);
		  //ib3.setKey(new InstructionKey(2));	  
		  //instructions.add(ib3.build());
		   
		
		   // Next service GOTO Instructions Need to be appended to the List
		   InstructionUtils.createGotoTableInstructions(ib2,(short)60);
		   ib2.setOrder(2);
		   ib2.setKey(new InstructionKey(2));
		   instructions.add(ib2.build());
		


			// Add InstructionBuilder to the Instruction(s)Builder List
		   isb.setInstruction(instructions);
		

			// Add InstructionsBuilder to FlowBuilder
	       flowBuilder.setInstructions(isb.build());
   
		   writeFlow(flowBuilder, nodeBuilder);
		
	   } else {
		   removeFlow(flowBuilder, nodeBuilder);
	   }
   }

    public void programRoutingFlowUnicastGw(Long dpidLong,Long metadata,Long l2_group_id,boolean write) {

	   NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpidLong);
	   FlowBuilder flowBuilder = new FlowBuilder();
	   String flowName = "programRoutingFlowUnicastGw" + dpidLong + "_" + metadata.toString() + "_";
	   FlowUtils.initFlowBuilder(flowBuilder, flowName, (short)30).setPriority(9900);
   
	   MatchBuilder matchBuilder = new MatchBuilder();
	   long zerol = 0x7fffffff00000000l;
	   
	   //long zerol = 0xffffffffl;
	   MatchUtils.createMetadataMatch(matchBuilder,BigInteger.valueOf(metadata),BigInteger.valueOf(zerol));
	   

	   MatchUtils.createEtherTypeMatch(matchBuilder, new EtherType(0x0800L));
	   Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
	   String dstIp = "0.0.0.0";
       ipv4match.setIpv4Destination(new Ipv4Prefix(dstIp + "/0"));
       matchBuilder.setLayer3Match(ipv4match.build());
	   flowBuilder.setMatch(matchBuilder.build());
   
	   if (write) {

        
		   //InstructionBuilder ib1 = new InstructionBuilder();
		   InstructionBuilder ib2 = new InstructionBuilder();
		   InstructionBuilder ib3 = new InstructionBuilder();
		   InstructionsBuilder isb = new InstructionsBuilder();
		
		   // Instructions List Stores Individual Instructions
		   List<Instruction> instructions = Lists.newArrayList();

           createGroupInstructionsGw(ib3,l2_group_id);
		   ib3.setOrder(1);
		   ib3.setKey(new InstructionKey(1));	   
		   instructions.add(ib3.build());

          
		  //createSetTTLInstructions(ib3);
		  //ib3.setOrder(2);
		  //ib3.setKey(new InstructionKey(2));	  
		  //instructions.add(ib3.build());
		   
		
		   // Next service GOTO Instructions Need to be appended to the List
		   
		   InstructionUtils.createGotoTableInstructions(ib2,(short) 60);
		   ib2.setOrder(2);
		   ib2.setKey(new InstructionKey(2));
		   instructions.add(ib2.build());
		   


			// Add InstructionBuilder to the Instruction(s)Builder List
		   isb.setInstruction(instructions);
		

			// Add InstructionsBuilder to FlowBuilder
	       flowBuilder.setInstructions(isb.build());
   
		   writeFlow(flowBuilder, nodeBuilder);
		
	   } else {
		   removeFlow(flowBuilder, nodeBuilder);
	   }
   }

   

   public void destroyGroup(Long dpid,String groupName,Long groupid) {
   	   NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpid);
       GroupBuilder groupBuilder = new GroupBuilder();
	   initGroupBuilder(groupBuilder,groupName,groupid,1);
	   removeGroup(groupBuilder,nodeBuilder);	   
    }
 
 

    // grouptype = 0 groupall
    //grouptype = 1 groupselect
    //grouptype = 2 groupindirect
    //grouptype = 3 groupff
    public static GroupBuilder initGroupBuilder(GroupBuilder groupBuilder, String groupname,Long groupid,int grouptype) {
        final GroupId groupId = new GroupId(groupid);
        groupBuilder
                .setGroupId(groupId)
                .setBarrier(false)
                .setGroupName(groupname)
                .setKey(new GroupKey(groupId));
		if(grouptype==0)
			groupBuilder.setGroupType(GroupTypes.GroupAll);
		if(grouptype==1)
			groupBuilder.setGroupType(GroupTypes.GroupSelect);
		if(grouptype==2)
			groupBuilder.setGroupType(GroupTypes.GroupIndirect);
		if(grouptype==3)
			groupBuilder.setGroupType(GroupTypes.GroupFf);	
        return groupBuilder;
    }


	public  void writeGroup(GroupBuilder groupBuilder, NodeBuilder nodeBuilder) {
      
            WriteTransaction modification = dataBroker.newWriteOnlyTransaction();
            InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class).child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory
                    .rev130819.nodes.Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class).child(Group.class,
                            new GroupKey(groupBuilder.getGroupId())).build();
            modification.put(LogicalDatastoreType.CONFIGURATION, path1, groupBuilder.build(), true /*createMissingParents*/);

            CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
            try {
                commitFuture.get();  // TODO: Make it async (See bug 1362)
                LOG.debug("Transaction success for write of Group {}", groupBuilder.getGroupName());
            } catch (InterruptedException|ExecutionException e) {
                LOG.error("Failed to write group {}", groupBuilder.getGroupName(), e);
            }
        
		}

    public void removeGroup(GroupBuilder groupBuilder, NodeBuilder nodeBuilder) {
      
            WriteTransaction modification = dataBroker.newWriteOnlyTransaction();
            InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class).child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory
                    .rev130819.nodes.Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class).child(Group.class,
                            new GroupKey(groupBuilder.getGroupId())).build();
            modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
            CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();

            try {
                commitFuture.get();  // TODO: Make it async (See bug 1362)
                LOG.debug("Transaction success for deletion of Group {}", groupBuilder.getGroupName());
            } catch (InterruptedException|ExecutionException e) {
                LOG.error("Failed to remove group {}", groupBuilder.getGroupName(), e);
            }
    	}


	 public static InstanceIdentifier<Flow> createFlowPath(FlowBuilder flowBuilder, NodeBuilder nodeBuilder) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                        nodeBuilder.getKey())
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId()))
                .child(Flow.class, flowBuilder.getKey()).build();
    }


	public static InstanceIdentifier<Table> createFlowPath2(short tableid, NodeBuilder nodeBuilder) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                        nodeBuilder.getKey())
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableid)).build();
    }
	 
	public void writeFlow(FlowBuilder flowBuilder, NodeBuilder nodeBuilder) {

				WriteTransaction modification = dataBroker.newWriteOnlyTransaction();
				//modification.put(LogicalDatastoreType.CONFIGURATION, createNodePath(nodeBuilder),
				//		  nodeBuilder.build(), true /*createMissingParents*/);
				modification.put(LogicalDatastoreType.CONFIGURATION, createFlowPath(flowBuilder, nodeBuilder),
						flowBuilder.build(), true /*createMissingParents*/);
				CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
				try {
					commitFuture.checkedGet();	// TODO: Make it async (See bug 1362)
					LOG.debug("Transaction success for write of Flow : {}", flowBuilder.getFlowName());
				} catch (Exception e) {
					LOG.error("Failed to write flow : {}", flowBuilder.getFlowName(), e);
					modification.cancel();
				}
		}
	
	 public void removeFlow(FlowBuilder flowBuilder, NodeBuilder nodeBuilder) {

			WriteTransaction modification = dataBroker.newWriteOnlyTransaction();
		    modification.delete(LogicalDatastoreType.CONFIGURATION, createFlowPath(flowBuilder, nodeBuilder));
	
		    CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
		    try {
					commitFuture.get();  // TODO: Make it async (See bug 1362)
					LOG.debug("Transaction success for deletion of Flow : {}", flowBuilder.getFlowName());
				} catch (Exception e) {
					LOG.error("Failed to remove flow : {}", flowBuilder.getFlowName(), e);
					modification.cancel();
				}

		}


	 public Flow getFlow(FlowBuilder flowBuilder, NodeBuilder nodeBuilder) {
        ReadOnlyTransaction readTx = dataBroker.newReadOnlyTransaction();
        try {
            Optional<Flow> data =
                    readTx.read(LogicalDatastoreType.CONFIGURATION, createFlowPath(flowBuilder, nodeBuilder)).get();
            if (data.isPresent()) {
                return data.get();
            }
        } catch (InterruptedException|ExecutionException e) {
            LOG.error("Failed to get flow : {}", flowBuilder.getFlowName(), e);
        }

        LOG.debug("Cannot find data for Flow : {}", flowBuilder.getFlowName());
        return null;
    }

	public List<Flow> getTableFlows(NodeBuilder nodeBuilder, short table) {
		
		ReadOnlyTransaction readTx = dataBroker.newReadOnlyTransaction();
        try {
            Optional<Table> data = readTx.read(LogicalDatastoreType.CONFIGURATION, createFlowPath2(table,nodeBuilder)).get();
            if (data.isPresent()) {
                //return data.get();
                return data.get().getFlow();
            }
        } catch (InterruptedException|ExecutionException e) {
            LOG.error("Failed to get table {}", table, e);
        }

        LOG.info("Cannot find data for table {}", table);
        return null;
    }

    public  Group getGroup(Long groupid, NodeBuilder nodeBuilder) {

		final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
		InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class).child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory
                    .rev130819.nodes.Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class).child(Group.class,
                            new GroupKey(new GroupId(groupid))).build();
		try{
		    Optional<Group> data = transaction.read(LogicalDatastoreType.CONFIGURATION, path1).get();
		    if (data.isPresent()) {
                return data.get();
            }
	     }catch (InterruptedException|ExecutionException e) {
            LOG.error("Failed to get group");
        }

		LOG.info("Cannot find data for group!");
        return null;		

    }


	


    //delete table 30 or 50 flow..
    // reflow table 30 or 50 for groupid...
	public void reFlowTable(Long dpid,Long groupid,short tableid) {

          //short tableid = tableid;
		  NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpid);
		  List<Flow> flows = getTableFlows(nodeBuilder,tableid);
          String gpid = groupid.toString();
		  for (Flow flow : flows) {
              LOG.info("------------reFlowTable{}---flow is {}------------",tableid,flow);
			  List<Action> existingActions;
			  List<Instruction> existingInstructions = InstructionUtils.extractExistingInstructions(flow);
			  for(Instruction instruction : existingInstructions) {
                   if (instruction.getInstruction() instanceof WriteActionsCase) {
                       existingActions = (((WriteActionsCase) instruction.getInstruction()).getWriteActions().getAction());
                       for (Action action : existingActions) {
                            if (action.getAction() instanceof GroupActionCase) {
                                GroupActionCase gpAction = (GroupActionCase) action.getAction();
								GroupActionCaseBuilder gpActionBuilder = new GroupActionCaseBuilder(gpAction);
								GroupActionBuilder groupActionBuilder = new GroupActionBuilder(gpActionBuilder.getGroupAction());
								Long gpid2 = groupActionBuilder.getGroupId();
								LOG.info("----------reFlowTable---gpid is {}-----gpid2 is {}----------",gpid,gpid2);
								if(gpid.equals(gpid2.toString())) {
                                    LOG.info("reFlowTable{}----find flow...",tableid);
								    removeFlow(new FlowBuilder(flow),nodeBuilder);
								    writeFlow(new FlowBuilder(flow),nodeBuilder);
									LOG.info("reflowtable{} end!!!!",tableid);
								    
								}

                            }
                       	}
                   }
			  }

		  }	

          return ;

	}

    public Long getL2Group(Long dpid,Long groupl3id) {

	   NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpid);
       Group l2Group = getGroup(groupl3id,nodeBuilder);
	   GroupBuilder groupBuilder = new GroupBuilder(l2Group);
       Buckets buckets = groupBuilder.getBuckets();
       for (Bucket bucket : buckets.getBucket()) {
            List<Action> bucketActions = bucket.getAction();
            LOG.info("----------getL2Group: bucketActions {}---------", bucketActions);
            for (Action action : bucketActions) {
                 if (action.getAction() instanceof GroupActionCase) {
                       GroupActionCase gpAction = (GroupActionCase) action.getAction();
				       GroupActionCaseBuilder gpActionBuilder = new GroupActionCaseBuilder(gpAction);
				       GroupActionBuilder groupActionBuilder = new GroupActionBuilder(gpActionBuilder.getGroupAction());
					   Long gpid = groupActionBuilder.getGroupId();
					   return gpid;

                    }
                }
            }

	   return 0L;
    }	
	
    public Long reGroupL3(Long dpid,Long groupid) {

		  NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(dpid);
		  short tableid = 30;
		  Long reL3 = -1L;
		  List<Flow> flow30 = getTableFlows(nodeBuilder,tableid);
          String gpid = groupid.toString();
		  for (Flow flow : flow30) {
              LOG.info("--reGroupL3-----reFlowTable30---flow is {}...",flow);
			  List<Action> existingActions;
			  List<Instruction> existingInstructions = InstructionUtils.extractExistingInstructions(flow);
			  for(Instruction instruction : existingInstructions) {
                   if (instruction.getInstruction() instanceof WriteActionsCase) {
                       existingActions = (((WriteActionsCase) instruction.getInstruction()).getWriteActions().getAction());
                       for (Action action : existingActions) {
                            if (action.getAction() instanceof GroupActionCase) {
                                GroupActionCase gpAction = (GroupActionCase) action.getAction();
								GroupActionCaseBuilder gpActionBuilder = new GroupActionCaseBuilder(gpAction);
								GroupActionBuilder groupActionBuilder = new GroupActionBuilder(gpActionBuilder.getGroupAction());
								Long gpidL3 = groupActionBuilder.getGroupId();
								Long gpidL2 = getL2Group(dpid,gpidL3);
								String gpidL2S = gpidL2.toString();
								LOG.info("--l3group is {},l2group is {}--",gpidL3,gpidL2);
								if(gpidL2S.equals(gpid)) {
									LOG.info("----startdeleteL31---");
								    Group group = getGroup(gpidL3,nodeBuilder);
									removeGroup(new GroupBuilder(group),nodeBuilder);
									LOG.info("----startwriteL32---");
								    writeGroup(new GroupBuilder(group),nodeBuilder);
									return gpidL3;
								    
								}

                            }
                       	}
                   }
			  }

		  }	

         return reL3;

	}
	


   	}

