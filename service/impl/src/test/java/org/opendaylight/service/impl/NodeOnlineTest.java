/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;


public class NodeOnlineTest extends AbstractDataBrokerTest {

    private BierConfigWriterMock bierConfigWriterMock;
    private ChannelConfigWriterMock channelConfigWriterMock;
    private NetconfStateChangeListener netconfStateChangeListener;

    @Before
    public void setUp() {
        bierConfigWriterMock = new BierConfigWriterMock();
        channelConfigWriterMock = new ChannelConfigWriterMock();
        netconfStateChangeListener = new NetconfStateChangeListener(getDataBroker(), bierConfigWriterMock,
                channelConfigWriterMock);
    }

    @Test
    public void nodeOnlineTest() {
        addNodeToDatastore("1", constructDomainList(1, 1, 1, 1));
        addNodeToDatastore("2", constructDomainList(1, 2, 1, 2));
        addNodeToDatastore("3", null);
        addChannelToDatastore(constructBierChannel("flow:1", "channel-1", "10.84.220.5",
                "102.112.20.40", 1, 1, (short)30, (short)40, 2, "2",
                constructEgressNodeList(1, "1")));
        netconfStateChangeListener.onDataTreeChanged(setNodeData(constructNodeBefore("1"), constructNodeAfter("1"),
                ModificationType.SUBTREE_MODIFIED));
        netconfStateChangeListener.onDataTreeChanged(setNodeData(constructNodeBefore("2"), constructNodeAfter("2"),
                ModificationType.SUBTREE_MODIFIED));
        netconfStateChangeListener.onDataTreeChanged(setNodeData(constructNodeBefore("3"), constructNodeAfter("3"),
                ModificationType.SUBTREE_MODIFIED));
        assertDomainList(bierConfigWriterMock.getDomainProcessList());
        assertChannel(channelConfigWriterMock.getChannel());
    }

    private List<Domain> constructDomainList(int domainId, int domainBfrId, int subDomainId, int subdomainBfrId) {
        List<SubDomain> subDomainList = new ArrayList<>();
        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setSubDomainId(new SubDomainId(subDomainId));
        subDomainBuilder.setBfrId(new BfrId(subdomainBfrId));
        subDomainList.add(subDomainBuilder.build());

        BierGlobalBuilder bierGlobalBuilder = new BierGlobalBuilder();
        bierGlobalBuilder.setSubDomain(subDomainList);
        bierGlobalBuilder.setBfrId(new BfrId(domainBfrId));

        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setBierGlobal(bierGlobalBuilder.build());
        domainBuilder.setDomainId(new DomainId(domainId));

        List<Domain> domainList = new ArrayList<>();
        domainList.add(domainBuilder.build());
        return domainList;
    }

    private void addNodeToDatastore(String nodeId, List<Domain> domainList) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<BierNode> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey("flow:1"))
                .child(BierNode.class, new BierNodeKey(nodeId));
        BierNodeBuilder bierNodeBuilder = new BierNodeBuilder();
        bierNodeBuilder.setNodeId(nodeId);
        BierNodeParamsBuilder para = new BierNodeParamsBuilder();
        para.setDomain(domainList);
        bierNodeBuilder.setBierNodeParams(para.build());
        tx.put(LogicalDatastoreType.CONFIGURATION, path, bierNodeBuilder.build(), true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    private List<EgressNode> constructEgressNodeList(int bfrId, String egress) {
        EgressNodeBuilder builder = new EgressNodeBuilder();
        builder.setEgressBfrId(new BfrId(bfrId));
        builder.setNodeId(egress);
        builder.setKey(new EgressNodeKey(egress));
        List<EgressNode> egressList = new ArrayList<>();
        egressList.add(builder.build());
        return egressList;
    }

    private BierChannel constructBierChannel(String bierChannelKey, String name, String srcIp, String dstGroup,
                                             int domainId, int subDomainId, short srcWild, short groupWild, int bfrId,
                                             String ingress, List<EgressNode> egressList) {
        ChannelBuilder channelBuilder = new ChannelBuilder();
        channelBuilder.setName(name);
        channelBuilder.setKey(new ChannelKey(name));
        channelBuilder.setSrcIp(new IpAddress(new Ipv4Address(srcIp)));
        channelBuilder.setDstGroup(new IpAddress(new Ipv4Address(dstGroup)));
        channelBuilder.setDomainId(new DomainId(domainId));
        channelBuilder.setSubDomainId(new SubDomainId(subDomainId));
        channelBuilder.setSourceWildcard(srcWild);
        channelBuilder.setGroupWildcard(groupWild);
        channelBuilder.setIngressBfrId(new BfrId(bfrId));
        channelBuilder.setIngressNode(ingress);
        channelBuilder.setEgressNode(egressList);
        List<Channel> channelList = new ArrayList<>();
        channelList.add(channelBuilder.build());
        BierChannelBuilder bierChannelBuilder = new BierChannelBuilder();
        bierChannelBuilder.setChannel(channelList);
        bierChannelBuilder.setKey(new BierChannelKey(bierChannelKey));
        return bierChannelBuilder.build();
    }

    private void addChannelToDatastore(BierChannel channel) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<BierChannel> path = InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class, new BierChannelKey("flow:1"));
        tx.put(LogicalDatastoreType.CONFIGURATION, path, channel, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    private Node constructNodeBefore(String value) {
        NodeBuilder builder = new NodeBuilder();
        builder.setKey(new NodeKey(new NodeId(value)));
        builder.setNodeId(new NodeId(value));
        builder.addAugmentation(NetconfNode.class, new NetconfNodeBuilder()
                .setConnectionStatus(NetconfNodeConnectionStatus.ConnectionStatus.Connecting).build());
        return builder.build();
    }

    private Node constructNodeAfter(String value) {
        NodeBuilder builder = new NodeBuilder();
        builder.setKey(new NodeKey(new NodeId(value)));
        builder.setNodeId(new NodeId(value));
        builder.addAugmentation(NetconfNode.class, new NetconfNodeBuilder()
                .setConnectionStatus(NetconfNodeConnectionStatus.ConnectionStatus.Connected).build());
        return builder.build();
    }

    private static class DataTreeModificationMock implements DataTreeModification<Node> {
        private Node before;
        private Node after;
        private ModificationType type;

        public void setNodeData(Node before, Node after, ModificationType type) {
            this.before = before;
            this.after = after;
            this.type = type;
        }

        @Override
        public DataTreeIdentifier<Node> getRootPath() {
            InstanceIdentifier<Node> nodeId = InstanceIdentifier.create(NetworkTopology.class).child(Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName()))).child(Node.class);
            return new DataTreeIdentifier<Node>(LogicalDatastoreType.CONFIGURATION, nodeId);
        }

        @Override
        public DataObjectModification<Node> getRootNode() {
            DataObjectModificationMock mock = new DataObjectModificationMock();
            mock.setNodeData(before, after, type);
            return mock;
        }
    }

    private static class DataObjectModificationMock implements DataObjectModification<Node> {
        private Node before;
        private Node after;
        private ModificationType type;

        public void setNodeData(Node before, Node after, ModificationType type) {
            this.before = before;
            this.after = after;
            this.type = type;
        }

        @Override
        public ModificationType getModificationType() {
            return type;
        }

        @Override
        public Node getDataBefore() {
            return before;
        }

        @Override
        public Node getDataAfter() {
            return after;
        }

        @Override
        public PathArgument getIdentifier() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Class<Node> getDataType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<DataObjectModification<? extends DataObject>> getModifiedChildren() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends ChildOf<? super Node>> DataObjectModification<C> getModifiedChildContainer(
                Class<C> child) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends Augmentation<Node> & DataObject> DataObjectModification<C> getModifiedAugmentation(
                Class<C> augmentation) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends Identifiable<K> & ChildOf<? super Node>,
                K extends Identifier<C>> DataObjectModification<C> getModifiedChildListItem(
                Class<C> listItem, K listKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DataObjectModification<? extends DataObject> getModifiedChild(
                PathArgument childArgument) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private Collection<DataTreeModification<Node>> setNodeData(Node before, Node after, ModificationType type) {
        Collection<DataTreeModification<Node>> collection = new ArrayList<>();
        DataTreeModificationMock mock = new DataTreeModificationMock();
        mock.setNodeData(before, after, type);
        collection.add(mock);
        return collection;
    }

    private void assertDomainList(List<Domain> domainList) {
        Assert.assertEquals(domainList.size(), 2);
        Assert.assertEquals(domainList.get(0).getDomainId(), new DomainId(1));
        Assert.assertEquals(domainList.get(0).getBierGlobal().getBfrId(), new BfrId(1));
        Assert.assertEquals(domainList.get(0).getBierGlobal().getSubDomain().get(0).getSubDomainId(),
                new SubDomainId(1));

        Assert.assertEquals(domainList.get(1).getDomainId(), new DomainId(1));
        Assert.assertEquals(domainList.get(1).getBierGlobal().getBfrId(), new BfrId(2));
        Assert.assertEquals(domainList.get(1).getBierGlobal().getSubDomain().get(0).getSubDomainId(),
                new SubDomainId(1));
    }

    private void assertChannel(Channel channel) {
        Assert.assertEquals(channel.getName(), "channel-1");
        Assert.assertEquals(channel.getSrcIp(), new IpAddress(new Ipv4Address("10.84.220.5")));
        Assert.assertEquals(channel.getDstGroup(), new IpAddress(new Ipv4Address("102.112.20.40")));
        Assert.assertEquals(channel.getDomainId(), new DomainId(1));
        Assert.assertEquals(channel.getSubDomainId(), new SubDomainId(1));
        Assert.assertEquals(channel.getIngressBfrId(), new BfrId(2));
        Assert.assertEquals(channel.getIngressNode(), "2");
        Assert.assertEquals(channel.getEgressNode().size(), 1);
        Assert.assertEquals(channel.getEgressNode().get(0).getEgressBfrId(), new BfrId(1));
        Assert.assertEquals(channel.getEgressNode().get(0).getNodeId(),"1");
    }

    private static class BierConfigWriterMock implements BierConfigWriter {

        private List<Domain> domainProcessList = new ArrayList<>();

        @Override
        public ConfigurationResult writeDomain(ConfigurationType type, String nodeId, Domain domain) {
            switch (type) {
                case ADD:
                    if (null != domain) {
                        domainProcessList.add(domain);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                case MODIFY:
                    // TODO
                    break;
                case DELETE:
                    // TODO
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        @Override
        public ConfigurationResult writeSubdomain(ConfigurationType type, String nodeId, DomainId domainId,
                                                  SubDomain subDomain) {
            // TODO
            return null;
        }

        @Override
        public ConfigurationResult writeSubdomainIpv4(ConfigurationType type, String nodeId, DomainId domainId,
                                                       SubDomainId subDomainId, Ipv4 ipv4) {
            // TODO
            return null;
        }

        @Override
        public ConfigurationResult writeSubdomainIpv6(ConfigurationType type, String nodeId, DomainId domainId,
                                                      SubDomainId subDomainId, Ipv6 ipv6) {
            // TODO
            return null;
        }

        private List<Domain> getDomainProcessList() {
            return domainProcessList;
        }
    }

    private static class ChannelConfigWriterMock implements ChannelConfigWriter {

        private List<Channel> channelList = new ArrayList<>();

        @Override
        public ConfigurationResult writeChannel(ConfigurationType type, Channel channel) {
            switch (type) {
                case ADD:
                    if (null != channel) {
                        channelList.add(channel);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                case MODIFY:
                    // TODO
                    break;
                case DELETE:
                    // TODO
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        @Override
        public ConfigurationResult writeChannelEgressNode(ConfigurationType type, Channel channel) {
            // TODO
            return null;
        }

        public Channel getChannel() {
            return  channelList.get(0);
        }
    }
}
