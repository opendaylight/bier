/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.util;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.AddChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierForwardingType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobal;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChannelDBUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ChannelDBUtil.class);
    private ChannelDBContext context;
    private static ChannelDBUtil instance = new ChannelDBUtil();
    public static final String DEFAULT_TOPO_ID = "example-linkstate-topology";

    ChannelDBUtil() {
    }


    public void setContext(ChannelDBContext context) {
        this.context = context;
    }

    public static ChannelDBUtil getInstance() {
        return instance;
    }

    public boolean isChannelExists(String name, String topologyId) {
        Optional<Channel> channel = readChannel(name,buildTopoId(topologyId));

        if (channel == null || !channel.isPresent()) {
            return false;
        }
        return true;

    }

    public Optional<Channel> readChannel(String name, String topologyId) {
        ReadOnlyTransaction rtx = context.newReadOnlyTransaction();
        try {
            return rtx.read(LogicalDatastoreType.CONFIGURATION,
                    buildChannelPath(name,buildTopoId(topologyId))).get();

        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Channel:occur exception when read databroker {}", e);
            return null;

        }
    }

    public Channel getChannelInfo(String topologyId, String channelName) {
        Optional<Channel> channel = readChannel(channelName,topologyId);
        if (channel == null || !channel.isPresent()) {
            return null;
        }
        return channel.get();
    }

    public Optional<BierChannel> readBierChannel(String topologyId) {
        ReadOnlyTransaction rtx = context.newReadOnlyTransaction();
        try {
            return rtx.read(LogicalDatastoreType.CONFIGURATION,
                    buildBierChannelPath(topologyId)).get();

        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Channel:occur exception when read databroker {}", e);
            return null;

        }
    }

    private Optional<BierNetworkChannel> readBierNetworkChannel() {
        ReadOnlyTransaction rtx = context.newReadOnlyTransaction();
        try {
            return rtx.read(LogicalDatastoreType.CONFIGURATION,
                    buildBierNetworkChannelPath()).get();

        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Channel:occur exception when read databroker {}", e);
            return null;

        }
    }

    private Optional<SubDomain> readBierNodeSubDomain(String topologyId, String node, DomainId domainId,
                                                      SubDomainId subDomainId) {
        ReadOnlyTransaction rtx = context.newReadOnlyTransaction();
        try {
            return rtx.read(LogicalDatastoreType.CONFIGURATION,
                    buildBierNodeSubDomainPath(topologyId,node,domainId,subDomainId)).get();
        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Channel:occur exception when read databroker {}", e);
            return null;
        }
    }

    private Optional<TeSubDomain> readBierTeNodeSubDomain(String topologyId, String node, DomainId domainId,
                                                      SubDomainId subDomainId) {
        ReadOnlyTransaction rtx = context.newReadOnlyTransaction();
        try {
            return rtx.read(LogicalDatastoreType.CONFIGURATION,
                    buildBierTeNodeSubDomainPath(topologyId,node,domainId,subDomainId)).get();
        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Channel:occur exception when read databroker {}", e);
            return null;
        }
    }

    private InstanceIdentifier<SubDomain> buildBierNodeSubDomainPath(String topologyId, String node,
                                                                      DomainId domainId, SubDomainId subDomainId) {
        return InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId))
                .child(BierNode.class, new BierNodeKey(node))
                .child(BierNodeParams.class)
                .child(Domain.class, new DomainKey(domainId))
                .child(BierGlobal.class)
                .child(SubDomain.class, new SubDomainKey(subDomainId));
    }

    private InstanceIdentifier<TeSubDomain> buildBierTeNodeSubDomainPath(String topologyId, String node,
                                                                     DomainId domainId, SubDomainId subDomainId) {
        return InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId))
                .child(BierNode.class, new BierNodeKey(node))
                .child(BierTeNodeParams.class)
                .child(TeDomain.class, new TeDomainKey(domainId))
                .child(TeSubDomain.class, new TeSubDomainKey(subDomainId));
    }

    private InstanceIdentifier<Channel> buildChannelPath(String name, String topologyId) {
        return InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class, new BierChannelKey(topologyId))
                .child(Channel.class, new ChannelKey(name));
    }


    private InstanceIdentifier<BierNetworkChannel> buildBierNetworkChannelPath() {
        return InstanceIdentifier.create(BierNetworkChannel.class);
    }

    public boolean writeChannelToDB(AddChannelInput input) {
        WriteTransaction wtx = context.newWriteOnlyTransaction();

        Channel channel = new ChannelBuilder(input).build();
        wtx.put(LogicalDatastoreType.CONFIGURATION,
                buildChannelPath(input.getName(), buildTopoId(input.getTopologyId())), channel, true);
        return submitTransaction(wtx);

    }


    public boolean writeDeployChannelToDB(DeployChannelInput input) {
        WriteTransaction wtx = context.newWriteOnlyTransaction();
        Channel oldChannel = getChannelInfo(input.getTopologyId(),input.getChannelName());
        Channel channelInfo = buildDeployChannelInfo(input,oldChannel);
        wtx.put(LogicalDatastoreType.CONFIGURATION,
                buildChannelPath(input.getChannelName(),buildTopoId(input.getTopologyId())),channelInfo);
        return submitTransaction(wtx);

    }


    private Channel buildDeployChannelInfo(DeployChannelInput input, Channel channel) {
        List<EgressNode> egressNodeList = new ArrayList<>();
        for (org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.deploy.channel.input.EgressNode egressNode
                : input.getEgressNode()) {
            List<RcvTp> rcvTpList = new ArrayList<>();
            if (egressNode.getRcvTp() != null) {
                for (org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.deploy.channel.input.egress.node.RcvTp
                         rcvTp : egressNode.getRcvTp()) {
                    rcvTpList.add(new RcvTpBuilder().setTp(rcvTp.getTp()).build());
                }
            }
            egressNodeList.add(new EgressNodeBuilder()
                    .setNodeId(egressNode.getNodeId())
                    .setEgressBfrId(getNodeBfrId(input.getTopologyId(),egressNode.getNodeId(),
                            channel.getDomainId(),channel.getSubDomainId(),input.getBierForwardingType()))
                    .setRcvTp(rcvTpList.isEmpty() ? null : rcvTpList)
                    .build());
        }
        return new ChannelBuilder(channel)
                .setBierForwardingType(input.getBierForwardingType())
                .setIngressNode(input.getIngressNode())
                .setIngressBfrId(getNodeBfrId(input.getTopologyId(),input.getIngressNode(),
                        channel.getDomainId(),channel.getSubDomainId(),input.getBierForwardingType()))
                .setSrcTp(input.getSrcTp())
                .setEgressNode(egressNodeList).build();
    }

    private BfrId getNodeBfrId(String topologyId, String nodeId, DomainId domainId, SubDomainId subDomainId,
                               BierForwardingType type) {
        if (type.equals(BierForwardingType.BierTe)) {
            return null;
        }
        BierGlobal bierGlobal = readBierGlobal(buildTopoId(topologyId),nodeId,domainId);
        BfrId globalBfrId = bierGlobal.getBfrId();
        if (bierGlobal.getSubDomain() != null) {
            for (SubDomain subDomain : bierGlobal.getSubDomain()) {
                if (subDomain.getSubDomainId().equals(subDomainId) && subDomain.getBfrId() != null
                        && subDomain.getBfrId().getValue() != 0) {
                    return subDomain.getBfrId();
                }
            }
        }
        return globalBfrId;
    }

    private BierGlobal readBierGlobal(String topologyId, String nodeId, DomainId domainId) {
        ReadOnlyTransaction rtx = context.newReadOnlyTransaction();
        try {
            return rtx.read(LogicalDatastoreType.CONFIGURATION,
                    buildBierGlobalPath(topologyId,nodeId,domainId)).get().get();
        } catch (ExecutionException | InterruptedException | IllegalStateException e) {
            LOG.warn("Channel:occur exception when read databroker {}", e);
            return null;
        }
    }

    private InstanceIdentifier<BierGlobal> buildBierGlobalPath(String topologyId, String nodeId, DomainId domainId) {
        return InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class,new BierTopologyKey(topologyId))
                .child(BierNode.class,new BierNodeKey(nodeId))
                .child(BierNodeParams.class)
                .child(Domain.class, new DomainKey(domainId))
                .child(BierGlobal.class);
    }


    private InstanceIdentifier<BierChannel> buildBierChannelPath(String topologyId) {
        return InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class, new BierChannelKey(topologyId));
    }

    private boolean submitTransaction(WriteTransaction writeTransaction) {
        try {
            writeTransaction.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Channel:write DB fail!", e);
            return false;
        }
        return true;
    }

    public boolean deleteChannelFromDB(RemoveChannelInput input) {
        if (!isChannelExists(input.getChannelName(),input.getTopologyId())) {
            return true;
        }
        WriteTransaction wtx = context.newWriteOnlyTransaction();

        wtx.delete(LogicalDatastoreType.CONFIGURATION, buildChannelPath(input.getChannelName(),
                buildTopoId(input.getTopologyId())));
        return submitTransaction(wtx);

    }

    public boolean modifyChannelToDB(ModifyChannelInput input) {
        Channel odlChannel = readChannel(input.getName(),buildTopoId(input.getTopologyId())).get();
        ChannelBuilder channelBuilder = new ChannelBuilder(odlChannel);
        if (input.getDomainId() != null) {
            channelBuilder.setDomainId(input.getDomainId());
        }
        if (input.getSubDomainId() != null) {
            channelBuilder.setSubDomainId(input.getSubDomainId());
        }
        if (input.getSrcIp() != null) {
            channelBuilder.setSrcIp(input.getSrcIp());
        }
        if (input.getDstGroup() != null) {
            channelBuilder.setDstGroup(input.getDstGroup());
        }
        if (input.getSourceWildcard() != null) {
            channelBuilder.setSourceWildcard(input.getSourceWildcard());
        }
        if (input.getGroupWildcard() != null) {
            channelBuilder.setGroupWildcard(input.getGroupWildcard());
        }
        WriteTransaction wtx = context.newWriteOnlyTransaction();
        wtx.merge(LogicalDatastoreType.CONFIGURATION,
                buildChannelPath(input.getName(),buildTopoId(input.getTopologyId())),channelBuilder.build(),true);
        return submitTransaction(wtx);
    }

    public List<Channel> queryChannels(QueryChannelInput input) {
        List<Channel> channels = new ArrayList<>();
        for (String channelName : input.getChannelName()) {
            Optional<Channel> channel = readChannel(channelName, buildTopoId(input.getTopologyId()));
            if (channel != null && channel.isPresent()) {
                channels.add(channel.get());
            }
        }
        return channels;
    }

    public List<String> getChannels(String topoId) {
        List<String> channelNames = new ArrayList<>();
        Optional<BierChannel> bierChannel = readBierChannel(buildTopoId(topoId));
        if (bierChannel != null && bierChannel.isPresent()) {
            for (Channel channel : bierChannel.get().getChannel()) {
                channelNames.add(channel.getName());
            }
        }
        return channelNames;
    }

    private String buildTopoId(String topoId) {
        return topoId != null ? topoId : DEFAULT_TOPO_ID;
    }

    public void initDB() {
        WriteTransaction wtx = context.newWriteOnlyTransaction();

        Optional<BierNetworkChannel> bierChannels = readBierNetworkChannel();
        if (bierChannels == null || !bierChannels.isPresent()) {
            wtx.put(LogicalDatastoreType.CONFIGURATION, buildBierNetworkChannelPath(),
                    new BierNetworkChannelBuilder().build());
        }
        submitTransaction(wtx);
    }

    public boolean hasChannelDeplyed(String name, String topologyId) {
        Optional<Channel> channel = readChannel(name,buildTopoId(topologyId));

        if (channel != null && channel.isPresent() && channel.get().getIngressNode() != null) {
            return true;
        }
        return false;
    }

    public boolean isBierNodeInSubDomain(String topologyId, String node, DomainId domainId, SubDomainId subDomainId,
                                         BierForwardingType type) {
        if (type.equals(BierForwardingType.Bier)) {
            Optional<SubDomain> bierNodeSubDomain = readBierNodeSubDomain(buildTopoId(topologyId),
                    node, domainId, subDomainId);
            if (bierNodeSubDomain == null || !bierNodeSubDomain.isPresent()) {
                return false;
            }
        }
        if (type.equals(BierForwardingType.BierTe)) {
            Optional<TeSubDomain> bierTeNodeSubDomain = readBierTeNodeSubDomain(buildTopoId(topologyId),
                    node, domainId, subDomainId);
            if (bierTeNodeSubDomain == null || !bierTeNodeSubDomain.isPresent()) {
                return false;
            }
        }
        return true;
    }

    public boolean isTpInTeSubdomain(String topologyId, String node, DomainId domainId, SubDomainId subDomainId,
                                     String tpId) {
        Optional<TeSubDomain> bierTeNodeSubDomain = readBierTeNodeSubDomain(buildTopoId(topologyId),
                node, domainId, subDomainId);
        if (bierTeNodeSubDomain != null && bierTeNodeSubDomain.isPresent()) {
            for (TeBsl teBsl : bierTeNodeSubDomain.get().getTeBsl()) {
                for (TeSi teSi : teBsl.getTeSi()) {
                    for (TeBp teBp : teSi.getTeBp()) {
                        if (teBp.getTpId().equals(tpId)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


}
