/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.channel;

import java.util.ArrayList;
import org.opendaylight.bier.driver.common.util.DataWriter;
import org.opendaylight.bier.driver.common.util.IidConstants;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.OptimisticLockFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

public class ChannelTestDataBuilder {
    //data
    public static final String NODE_ID = "nodeId";
    public static final IpAddress SRC_IP = new IpAddress(new Ipv4Address("10.41.42.60"));
    public static final short SRC_WILDCARD = 8;
    public static final IpAddress DEST_GROUP = new IpAddress(new Ipv4Address("224.0.0.5"));
    public static final short DEST_WILDCARD = 4;

    public static final String INGRESS_NODE_ID = "nodeId";
    public static final String EGRESS_NODE1 = "nodeId1";
    public static final String EGRESS_NODE2 = "nodeId2";
    public static final String INGRESS_NODE_TP1 = "nodeId_tp1";
    public static final String EGRESS_NODE1_TP1 = "nodeId1_tp1";
    public static final String EGRESS_NODE2_TP1 = "nodeId2_tp1";

    public static final int RETRY_WRITE_MAX = 1000;

    public static BierTopology bulidTpInfo() {

        ArrayList<BierTerminationPoint> ingressTpList = new ArrayList<>();
        ingressTpList.add(new BierTerminationPointBuilder()
                .setKey(new BierTerminationPointKey(INGRESS_NODE_TP1))
                .setTpId(INGRESS_NODE_TP1).setTpIndex(1L).build());
        ArrayList<BierTerminationPoint> egressTpList1 = new ArrayList<>();
        egressTpList1.add(new BierTerminationPointBuilder()
                .setKey(new BierTerminationPointKey(EGRESS_NODE1_TP1))
                .setTpId(EGRESS_NODE1_TP1).setTpIndex(1L).build());
        ArrayList<BierTerminationPoint> egressTpList2 = new ArrayList<>();
        egressTpList2.add(new BierTerminationPointBuilder()
                .setKey(new BierTerminationPointKey(EGRESS_NODE2_TP1))
                .setTpId(EGRESS_NODE2_TP1).setTpIndex(1L).build());

        ArrayList<BierNode> bierNodes = new ArrayList<>();
        bierNodes.add(new BierNodeBuilder().setNodeId(INGRESS_NODE_ID).setBierTerminationPoint(ingressTpList).build());
        bierNodes.add(new BierNodeBuilder().setNodeId(EGRESS_NODE1).setBierTerminationPoint(egressTpList1).build());
        bierNodes.add(new BierNodeBuilder().setNodeId(EGRESS_NODE2).setBierTerminationPoint(egressTpList2).build());

        return new BierTopologyBuilder().setTopologyId(IidConstants.TOPOLOGY_ID).setBierNode(bierNodes).build();
    }

    public static Channel buildChannelData(BfrId ingressBfrId,BfrId egressBfrId1,BfrId egressBfrId2) {
        ArrayList<RcvTp> rcvTpArrayList1 = new ArrayList<>();
        rcvTpArrayList1.add(new RcvTpBuilder().setTp(EGRESS_NODE1_TP1).build());
        ArrayList<RcvTp> rcvTpArrayList2 = new ArrayList<>();
        rcvTpArrayList2.add(new RcvTpBuilder().setTp(EGRESS_NODE2_TP1).build());
        ArrayList<EgressNode> egressNodeArrayList = new ArrayList<EgressNode>();
        egressNodeArrayList.add(new EgressNodeBuilder()
                .setEgressBfrId(new BfrId(egressBfrId1))
                .setNodeId(EGRESS_NODE1)
                .setRcvTp(rcvTpArrayList1)
                .build());
        egressNodeArrayList.add(new EgressNodeBuilder()
                .setEgressBfrId(new BfrId(egressBfrId2))
                .setNodeId(EGRESS_NODE2)
                .setRcvTp(rcvTpArrayList2)
                .build());
        return new ChannelBuilder()
                .setIngressNode(NODE_ID)
                .setSrcIp(SRC_IP)
                .setSrcTp(INGRESS_NODE_TP1)
                .setSourceWildcard(SRC_WILDCARD)
                .setDstGroup(DEST_GROUP)
                .setGroupWildcard(DEST_WILDCARD)
                .setIngressBfrId(new BfrId(ingressBfrId))
                .setEgressNode(egressNodeArrayList)
                .build();


    }

    //interface to datastore
    public static void writeInterfaceInfo(int tries, DataBroker dataBroker) {
        if (tries <= 0) {
            return;
        }
        try {
            tries = tries - 1;
            DataWriter.operate(DataWriter.OperateType.MERGE,
                    dataBroker,tries,
                    IidConstants.BIER_TOPO_IID, ChannelTestDataBuilder.bulidTpInfo()).checkedGet();


        } catch (TransactionCommitFailedException exception) {
            if (exception instanceof OptimisticLockFailedException) {
                writeInterfaceInfo(tries,dataBroker);
            }

        }

    }


    public static Channel buildChannelEgressNodeData(BfrId egressBfrId) {

        ArrayList<RcvTp> rcvTpArrayList1 = new ArrayList<>();
        rcvTpArrayList1.add(new RcvTpBuilder().setTp(ChannelTestDataBuilder.EGRESS_NODE1_TP1).build());

        ArrayList<EgressNode> egressNodeArrayList = new ArrayList<EgressNode>();
        egressNodeArrayList.add(new EgressNodeBuilder()
                .setEgressBfrId(egressBfrId)
                .setNodeId(ChannelTestDataBuilder.EGRESS_NODE1)
                .setRcvTp(rcvTpArrayList1)
                .build());

        return new ChannelBuilder()
                .setIngressNode(ChannelTestDataBuilder.INGRESS_NODE_ID)
                .setSrcIp(ChannelTestDataBuilder.SRC_IP)
                .setSrcTp(ChannelTestDataBuilder.INGRESS_NODE_TP1)
                .setSourceWildcard(ChannelTestDataBuilder.SRC_WILDCARD)
                .setDstGroup(ChannelTestDataBuilder.DEST_GROUP)
                .setGroupWildcard(ChannelTestDataBuilder.DEST_WILDCARD)
                .setIngressBfrId(new BfrId(1))
                .setEgressNode(egressNodeArrayList)
                .build();

    }


}
