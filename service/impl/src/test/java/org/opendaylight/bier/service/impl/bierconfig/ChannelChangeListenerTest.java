/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.bierconfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.BierTeChannelWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.service.impl.ChannelChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierForwardingType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

public class ChannelChangeListenerTest extends AbstractConcurrentDataBrokerTest {


    private ChannelConfigWriterMock channelConfigWriterMock;
    private ChannelChangeListener channelChangeListener;
    @Mock
    private RpcConsumerRegistry rpcConsumerRegistry;
    @Mock
    private BierTeChannelWriter teChannelWriter;
    @Mock
    private BierTeBiftWriter bierTeBiftWriter;
    @Mock
    private BierTeBitstringWriter bierTeBitstringWriter;

    @Before
    public void setUp() {
        channelConfigWriterMock = new ChannelConfigWriterMock();
        channelChangeListener = new ChannelChangeListener(getDataBroker(), rpcConsumerRegistry,
                channelConfigWriterMock, teChannelWriter, bierTeBiftWriter, bierTeBitstringWriter);
    }

    @Test
    public void channelListenerTset() {
        addNodeToDatastore("node-1",1,2,5);
        addNodeToDatastore("node-2",1,2,6);
        addNodeToDatastore("node-3",1,2,7);
        addNodeToDatastore("node-4",1,2,8);

        //Test add channel without ingress node and egress node
        Channel channelAdd = constructChannel("channel-1","1.1.1.1","102.112.20.40",1,2,
                (short)30,(short)40,4,null,null, BierForwardingType.Bier);
        channelChangeListener.onDataTreeChanged(setChannelData(null,channelAdd,ModificationType.WRITE));
        Assert.assertNull(channelConfigWriterMock.getChannelFromList(channelAdd.getName()));
        Assert.assertNull(channelConfigWriterMock.getChannelEgressDelFromList(channelAdd.getName()));

        //Test modify channel without ingress node and egress node
        Channel channelModify = constructChannel("channel-1","10.84.220.5","102.112.20.40",1,2,
                (short)30,(short)40,5,null,null, BierForwardingType.Bier);
        channelChangeListener.onDataTreeChanged(setChannelData(channelAdd,
                channelModify,ModificationType.SUBTREE_MODIFIED));
        Assert.assertNull(channelConfigWriterMock.getChannelFromList(channelModify.getName()));
        Assert.assertNull(channelConfigWriterMock.getChannelEgressDelFromList(channelModify.getName()));

        //Test deploy channel with ingress node and egress node
        List<EgressNode> egressList = new ArrayList<>();
        egressList.add(constructEgressNode(6,"node-2"));
        Channel channelDeploy = constructChannel("channel-1","10.84.220.5","102.112.20.40",1,2,
                (short)30,(short)40,5,"node-1",egressList, BierForwardingType.Bier);
        channelChangeListener.onDataTreeChanged(setChannelData(channelModify,
                channelDeploy,ModificationType.SUBTREE_MODIFIED));
        assertChannelData(channelDeploy,channelConfigWriterMock.getChannelFromList(channelDeploy.getName()));
        Assert.assertNull(channelConfigWriterMock.getChannelEgressDelFromList(channelDeploy.getName()));

        //Test modify ingress node of channel
        List<EgressNode> egressList1 = new ArrayList<>();
        egressList1.add(constructEgressNode(5,"node-1"));
        Channel channelModifyDeploy = constructChannel("channel-1","10.84.220.5","102.112.20.40",1,2,
                (short)30,(short)40,6,"node-2",egressList1, BierForwardingType.Bier);
        channelChangeListener.onDataTreeChanged(
                setChannelData(channelDeploy, channelModifyDeploy, ModificationType.SUBTREE_MODIFIED));
        assertChannelData(channelModifyDeploy,
                channelConfigWriterMock.getChannelFromList(channelModifyDeploy.getName()));
        Assert.assertNull(channelConfigWriterMock.getChannelEgressDelFromList(channelModifyDeploy.getName()));

        //Test add two egressNode
        List<EgressNode> egressList2 = new ArrayList<>();
        egressList2.add(constructEgressNode(5,"node-1"));
        egressList2.add(constructEgressNode(7, "node-3"));
        egressList2.add(constructEgressNode(8, "node-4"));
        Channel channelModify2 = constructChannel("channel-1","10.84.220.5","102.112.20.40",1,2,
                (short)30,(short)40,6,"node-2",egressList2, BierForwardingType.Bier);
        channelChangeListener.onDataTreeChanged(
                setChannelData(channelModifyDeploy, channelModify2, ModificationType.SUBTREE_MODIFIED));
        assertChannelData(channelModifyDeploy,channelConfigWriterMock.getChannelFromList(channelModify2.getName()));
        Assert.assertNull(channelConfigWriterMock.getChannelEgressDelFromList(channelModify2.getName()));
        List<EgressNode> egressListAdd = new ArrayList<>();
        egressListAdd.add(constructEgressNode(7, "node-3"));
        egressListAdd.add(constructEgressNode(8, "node-4"));
        Channel channelAddEgress = constructChannel("channel-1","10.84.220.5","102.112.20.40",1,2,
                (short)30,(short)40,6,"node-2",egressListAdd, BierForwardingType.Bier);
        assertChannelData(channelAddEgress,
                channelConfigWriterMock.getChannelEgressAddFromList("channel-1"));

        //Test Delete EgressNode
        List<EgressNode> egressList3 = new ArrayList<>();
        egressList3.add(constructEgressNode(5,"node-1"));
        Channel channelDeleteEgress = constructChannel("channel-1","10.84.220.5","102.112.20.40",1,2,
                (short)30,(short)40,6,"node-2",egressList3, BierForwardingType.Bier);
        channelChangeListener.onDataTreeChanged(
                setChannelData(channelModify2,channelDeleteEgress,ModificationType.SUBTREE_MODIFIED));
        assertChannelData(channelModifyDeploy,
                channelConfigWriterMock.getChannelFromList(channelDeleteEgress.getName()));
        List<EgressNode> egressListDel = new ArrayList<>();
        egressListDel.add(constructEgressNode(7,"node-3"));
        egressListDel.add(constructEgressNode(8,"node-4"));
        Channel channelDeleteEgressNode = constructChannel("channel-1","10.84.220.5","102.112.20.40",1,2,
                (short)30,(short)40,6,"node-2",egressListDel, BierForwardingType.Bier);
        assertChannelData(channelDeleteEgressNode,
                channelConfigWriterMock.getChannelEgressDelFromList(channelDeleteEgress.getName()));

        //Test Delete channel
        channelChangeListener.onDataTreeChanged(setChannelData(channelDeleteEgress,null,ModificationType.DELETE));
        Assert.assertNull(channelConfigWriterMock.getChannelFromList(channelDeleteEgressNode.getName()));
    }

    private static class DataTreeModificationMock implements DataTreeModification<Channel> {
        private Channel before;
        private Channel after;
        private ModificationType type;

        public void setChannelData(Channel before,Channel after,ModificationType type) {
            this.before = before;
            this.after = after;
            this.type = type;
        }

        @Override
        public DataTreeIdentifier<Channel> getRootPath() {
            InstanceIdentifier<Channel> channelId = InstanceIdentifier.create(BierNetworkChannel.class)
                    .child(BierChannel.class, new BierChannelKey("example-linkstate-topology"))
                    .child(Channel.class);
            return new DataTreeIdentifier<Channel>(
                    LogicalDatastoreType.CONFIGURATION, channelId);
        }

        @Override
        public DataObjectModification<Channel> getRootNode() {
            DataObjectModificationMock mock = new DataObjectModificationMock();
            mock.setChannelData(before, after, type);
            return mock;
        }
    }

    private static class DataObjectModificationMock implements DataObjectModification<Channel> {
        private Channel before;
        private Channel after;
        private ModificationType type;

        public void setChannelData(Channel before,Channel after,ModificationType type) {
            this.before = before;
            this.after = after;
            this.type = type;
        }

        @Override
        public ModificationType getModificationType() {
            return type;
        }

        @Override
        public Channel getDataBefore() {
            return before;
        }

        @Override
        public Channel getDataAfter() {
            return after;
        }

        @Override
        public PathArgument getIdentifier() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Class<Channel> getDataType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<DataObjectModification<? extends DataObject>> getModifiedChildren() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends ChildOf<? super Channel>> DataObjectModification<C> getModifiedChildContainer(
                Class<C> child) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends Augmentation<Channel> & DataObject> DataObjectModification<C> getModifiedAugmentation(
                Class<C> augmentation) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends Identifiable<K> & ChildOf<? super Channel>,
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

    private static class ChannelConfigWriterMock implements ChannelConfigWriter {

        private List<Channel> channelList = new ArrayList<>();
        private List<Channel> channelEgressDelList = new ArrayList<>();
        private List<Channel> channelEgressAddList = new ArrayList<>();

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
                    if (null != channel) {
                        deleteChannelFromList(channel.getName());
                        channelList.add(channel);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                case DELETE:
                    if (null != channel && null != getChannelFromList(channel.getName())) {
                        deleteChannelFromList(channel.getName());
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        @Override
        public ConfigurationResult writeChannelEgressNode(ConfigurationType type, Channel channel) {
            switch (type) {
                case ADD:
                    if (null != channel) {
                        channelEgressAddList.add(channel);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                case MODIFY:
                    return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                case DELETE:
                    if (null != channel) {
                        channelEgressDelList.add(channel);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        @Override
        public ConfigurationResult writeChannelEgressNodeTp(ConfigurationType type, Channel channel) {
            // TODO
            return null;
        }

        public Channel getChannelFromList(String name) {
            if (null == name) {
                return null;
            }
            for (Channel channel : channelList) {
                if (channel.getName().equals(name)) {
                    return channel;
                }
            }
            return null;
        }

        public Channel getChannelEgressDelFromList(String name) {
            if (null == name) {
                return null;
            }
            for (Channel channel : channelEgressDelList) {
                if (channel.getName().equals(name)) {
                    return channel;
                }
            }
            return null;
        }

        public Channel getChannelEgressAddFromList(String name) {
            if (null == name) {
                return null;
            }
            for (Channel channel : channelEgressAddList) {
                if (channel.getName().equals(name)) {
                    return channel;
                }
            }
            return null;
        }

        public void deleteChannelFromList(String name) {
            if (null == name) {
                return;
            }
            for (Channel channel : channelList) {
                if (channel.getName().equals(name)) {
                    channelList.remove(channel);
                    return;
                }
            }
        }
    }

    private void addNodeToDatastore(String nodeId,int domainId,int subDomainId,int bfrId) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<BierNode> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey("example-linkstate-topology"))
                .child(BierNode.class,new BierNodeKey(nodeId));
        BierNodeBuilder bierNode = new BierNodeBuilder();
        bierNode.setNodeId(nodeId);

        List<SubDomain> subDomainList = new ArrayList<>();
        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setBfrId(new BfrId(bfrId));
        subDomainBuilder.setSubDomainId(new SubDomainId(subDomainId));
        subDomainList.add(subDomainBuilder.build());

        BierGlobalBuilder global = new BierGlobalBuilder();
        global.setSubDomain(subDomainList);

        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setBierGlobal(global.build());

        List<Domain> domainList = new ArrayList<>();
        domainList.add(domainBuilder.build());

        BierNodeParamsBuilder para = new BierNodeParamsBuilder();
        if (null != domainList && !domainList.isEmpty()) {
            para.setDomain(domainList);
        }
        bierNode.setBierNodeParams(para.build());

        tx.put(LogicalDatastoreType.CONFIGURATION, path, bierNode.build(), true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }


    private Channel constructChannel(String name, String srcIp, String dstGroup, int domainId, int subDomainId,
                                     short srcWild, short groupWild, int bfrId, String ingress,
                                     List<EgressNode> egressList, BierForwardingType type) {
        ChannelBuilder  builder = new ChannelBuilder();
        builder.setName(name);
        builder.setKey(new ChannelKey(name));
        builder.setSrcIp(new IpAddress(new Ipv4Address(srcIp)));
        builder.setDstGroup(new IpAddress(new Ipv4Address(dstGroup)));
        builder.setDomainId(new DomainId(domainId));
        builder.setSubDomainId(new SubDomainId(subDomainId));
        builder.setSourceWildcard(srcWild);
        builder.setGroupWildcard(groupWild);
        builder.setIngressBfrId(new BfrId(bfrId));
        builder.setIngressNode(ingress);
        builder.setBierForwardingType(type);
        if (null != egressList && !egressList.isEmpty()) {
            builder.setEgressNode(egressList);
        }
        return builder.build();
    }

    private EgressNode constructEgressNode(int bfrId,String egress) {
        EgressNodeBuilder builder = new EgressNodeBuilder();
        builder.setEgressBfrId(new BfrId(bfrId));
        builder.setNodeId(egress);
        builder.setKey(new EgressNodeKey(egress));
        return builder.build();
    }

    private void assertChannelData(Channel expectChannel,Channel channelData) {
        if (null != expectChannel.getEgressNode() && null != channelData.getEgressNode()) {
            Assert.assertEquals(expectChannel.getEgressNode(), channelData.getEgressNode());
        } else if (null != expectChannel.getEgressNode() && null == channelData.getEgressNode()) {
            Assert.assertTrue(false);
        } else if (null == expectChannel.getEgressNode() && null != channelData.getEgressNode()) {
            Assert.assertTrue(false);
        }
        if (null != expectChannel.getIngressNode() && null != channelData.getIngressNode()) {
            Assert.assertEquals(expectChannel.getIngressNode(), channelData.getIngressNode());
        } else if (null != expectChannel.getIngressNode() && null == channelData.getIngressNode()) {
            Assert.assertTrue(false);
        } else if (null == expectChannel.getIngressNode() && null != channelData.getIngressNode()) {
            Assert.assertTrue(false);
        }
        Assert.assertEquals(expectChannel.getDomainId(),channelData.getDomainId());
        Assert.assertEquals(expectChannel.getDstGroup(),channelData.getDstGroup());
        Assert.assertEquals(expectChannel.getGroupWildcard(),channelData.getGroupWildcard());
        Assert.assertEquals(expectChannel.getSourceWildcard(),channelData.getSourceWildcard());
        Assert.assertEquals(expectChannel.getSrcIp(),channelData.getSrcIp());
        Assert.assertEquals(expectChannel.getSubDomainId(),channelData.getSubDomainId());
    }

    private Collection<DataTreeModification<Channel>> setChannelData(Channel before,
            Channel after, ModificationType type) {
        Collection<DataTreeModification<Channel>> collection = new ArrayList<>();
        DataTreeModificationMock mock = new DataTreeModificationMock();
        mock.setChannelData(before, after, type);
        collection.add(mock);
        return collection;
    }

}