/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.oam.impl;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.start.echo.request.input.TargetNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.start.echo.request.input.TargetNodeIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DbProvider.class);
    private static DbProvider instance = null;
    private DataBroker dataBroker;
    public static final String DEFAULT_TOPO_ID = "example-linkstate-topology";

    private DbProvider() {
    }

    public static DbProvider getInstance() {
        if (instance == null) {
            instance = new DbProvider();
        }
        return instance;
    }

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }


    public DataBroker getDataBroker() {
        return dataBroker;
    }

    public void deleteData(final LogicalDatastoreType type, InstanceIdentifier<?> path) {
        DataObject data = readData(type,path);
        if (data == null) {
            return;
        }
        final WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.delete(type, path);
        try {
            tx.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("", e);
        }
    }

    public <T extends DataObject> void mergeData(final LogicalDatastoreType type, InstanceIdentifier<T> path,
                                                            T data) {
        final WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.merge(type, path, data, true);
        try {
            tx.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("", e);
        }
    }

    public <T extends DataObject> T readData(final LogicalDatastoreType type, InstanceIdentifier<T> path) {
        if (dataBroker == null) {
            LOG.error("readOperationalData error, dataBroker null!");
            return null;
        }
        ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            Optional<T> optional = tx.read(type, path).checkedGet();
            if (optional.isPresent()) {
                return optional.get();
            } else {
                return null;
            }
        } catch (ReadFailedException | IllegalStateException e) {
            LOG.warn("read Db failed!", e);
            return null;
        }
    }

    public boolean isChannelExist(String topoId, String channelName) {
        Channel channel = readChannel(channelName,topoId);
        if (channel == null) {
            return false;
        }
        return true;
    }


    public Channel readChannel(String name, String topologyId) {
        return readData(LogicalDatastoreType.CONFIGURATION,buildChannelPath(name,topologyId));
    }

    private InstanceIdentifier<Channel> buildChannelPath(String name, String topologyId) {
        return InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class, new BierChannelKey(topologyId))
                .child(Channel.class, new ChannelKey(name));
    }

    public boolean checkTargetNodes(String topoId, String channelName, List<TargetNodeIds> targetNodeIds) {
        Channel channel = readChannel(channelName,topoId);
        if (channel == null || channel.getIngressNode() == null || channel.getEgressNode() == null
                || channel.getEgressNode().isEmpty()) {
            return false;
        }
        if (!targetNodeInEgressNodes(targetNodeIds,channel.getEgressNode())) {
            return false;
        }
        return true;
    }

    private boolean targetNodeInEgressNodes(List<TargetNodeIds> targetNodes, List<EgressNode> egressNodes) {
        if (targetNodes.size() > egressNodes.size()) {
            return false;
        }
        List<TargetNodeIds> egressList = new ArrayList<>();
        for (EgressNode egressNode : egressNodes) {
            egressList.add(new TargetNodeIdsBuilder().setTargetNodeId(egressNode.getNodeId()).build());
        }
        return egressList.containsAll(targetNodes);
    }

    public String getNodeIdByBfirId(SubDomainId subDomainId, BfrId bfirId) {
        BierChannel bierChannel = readData(LogicalDatastoreType.CONFIGURATION,bierChannelPath(DEFAULT_TOPO_ID));
        if (bierChannel != null) {
            for (Channel channel : bierChannel.getChannel()) {
                if (channel.getIngressBfrId() != null) {
                    if (channel.getSubDomainId().equals(subDomainId) && channel.getIngressBfrId().equals(bfirId)) {
                        return channel.getIngressNode();
                    }
                }
            }
        }
        return null;
    }

    private InstanceIdentifier<BierChannel> bierChannelPath(String topoId) {
        return InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class, new BierChannelKey(topoId));
    }

    public List<String> getNodeIdByBferId(SubDomainId subDomainId, List<BfrId> bfers) {
        List<String> egressNodes = new ArrayList<>();
        BierChannel bierChannel = readData(LogicalDatastoreType.CONFIGURATION,bierChannelPath(DEFAULT_TOPO_ID));
        if (bierChannel != null) {
            for (Channel channel : bierChannel.getChannel()) {
                List<BfrId> bfrIds = new ArrayList<>();
                if (channel.getEgressNode() != null) {
                    for (EgressNode egressNode : channel.getEgressNode()) {
                        bfrIds.add(egressNode.getEgressBfrId());
                        egressNodes.add(egressNode.getNodeId());
                    }
                    if (channel.getSubDomainId().equals(subDomainId) && bfrIds.containsAll(bfers)) {
                        return egressNodes;
                    }
                    egressNodes.clear();
                }
            }
        }
        return null;
    }

    public BierTopology readBierTopology() {
        InstanceIdentifier<BierTopology> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class,new BierTopologyKey(DEFAULT_TOPO_ID));
        return readData(LogicalDatastoreType.CONFIGURATION,path);
    }
}
