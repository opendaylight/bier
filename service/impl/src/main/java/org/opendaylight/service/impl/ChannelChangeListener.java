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
//import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
//import org.opendaylight.service.impl.netconf.configuration.ConfigurationNetconfAPI;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
//import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChannelChangeListener implements DataTreeChangeListener<Channel> {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelChangeListener.class);

    private static final InstanceIdentifier<Channel> CHANNEL_IID = InstanceIdentifier.create(BierNetworkChannel.class)
            .child(BierChannel.class, new BierChannelKey("flow:1")).child(Channel.class);

    private final DataBroker dataProvider;

    public ChannelChangeListener(final DataBroker dataBroker) {
        this.dataProvider = dataBroker;
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Channel>> changes) {
        for (DataTreeModification<Channel> change: changes) {
            DataObjectModification<Channel> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    LOG.info("onDataTreeChanged - BierChannel config with path {} was added or replaced: "
                                    + "old BierChannel: {}, new BierChannel: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                            rootNode.getDataAfter());
                    processAddedChannel(rootNode.getDataAfter());
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("onDataTreeChanged - BierChannel config with path {} was modified: "
                                    + "old BierChannel: {}, new BierChannel: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                            rootNode.getDataAfter());
                    processModifiedChannel(rootNode.getDataBefore(),rootNode.getDataAfter());
                    break;
                case DELETE:
                    LOG.info("onDataTreeChanged - BierChannel config with path {} was deleted: "
                                    + "old BierChannel: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore());
                    processAddedChannel(rootNode.getDataBefore());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type : {}"
                            + change.getRootNode().getModificationType());
            }
        }
    }

    public InstanceIdentifier<Channel> getChannelIid() {
        return CHANNEL_IID;
    }

    private void processAddedChannel(Channel channel) {
        if (null == channel) {
            return;
        }
        //Future<RpcResult<Void>> writedChannelResult = ConfigurationNetconfAPI.writeMultiCastInfo(
        //        ConfigurationNetconfAPI.ConfigurationType.CREATE, channel);
        //if (!writedChannelResult.getResult()){
        //    reportToApp(writedChannelResult);
        //}
    }


    private void processDeletedChannel(Channel channel) {
        if (null == channel) {
            return;
        }
        //Future<RpcResult<Void>> deleteChannelResult = ConfigurationNetconfAPI.writeMultiCastInfo(
        //        ConfigurationNetconfAPI.ConfigurationType.DELETE, channel);
        //if (!deleteChannelResult.getResult()){
        //    reportToApp(deleteChannelResult);
        //}
    }


    public void processModifiedChannel(Channel before, Channel after) {
        if (null == before || null == after) {
            return;
        }
        boolean isChannelParaChange = checkChannelParaChange(before, after);
        if (isChannelParaChange) {
            //Future<RpcResult<Void>> updateChannelResult = ConfigurationNetconfAPI.writeMultiCastInfo(
            //        ConfigurationNetconfAPI.ConfigurationType.UPDATE, channel);
            //if (!updateChannelResult.getResult()) {
            //    reportToApp(updateChannelResult);
            //    return;
            //}
        }
        List<EgressNode> nodeBefore = before.getEgressNode();
        List<EgressNode> nodeAfter = after.getEgressNode();
        List<EgressNode> egressNodeChange =  getEgressNodeListChange(nodeBefore,nodeAfter);
        if (null != nodeBefore && null == nodeAfter) {
            processDeletedEgressNode(before.getName(),egressNodeChange);
        }
        if (null != nodeBefore && null != nodeAfter
                && nodeBefore.size() > nodeAfter.size()) {
            processDeletedEgressNode(before.getName(),egressNodeChange);
        }
    }

    private void processDeletedEgressNode(String name,  List<EgressNode> egressNodeList) {
        if (null == name || null == egressNodeList || 0 == egressNodeList.size()) {
            return;
        }
        for (EgressNode egressNode : egressNodeList) {
            //Future<RpcResult<Void>> deleteEgressNodeResult = ConfigurationNetconfAPI.deleteEgressNode(
            //        ConfigurationNetconfAPI.ConfigurationType.DELETE, egressNode);
            //if (!deleteEgressNodeResult.getResult()) {
            //    reportToApp(deleteEgressNodeResult);
            //    return;
            //}
        }
    }

    private List<EgressNode>  getEgressNodeListChange(List<EgressNode> nodeBefore,List<EgressNode> nodeAfter) {
        if (null == nodeBefore &&  null == nodeAfter) {
            return null;
        }

        if (null == nodeAfter && 0 == nodeAfter.size()) {
            return nodeBefore;
        }
        List<EgressNode> nodeChange = new ArrayList<>();
        if (nodeBefore.size() > nodeAfter.size()) {
            for (EgressNode egressNode : nodeBefore) {
                if (null == getNodeById(egressNode.getNodeId(),nodeAfter)) {
                    nodeChange.add(egressNode);
                }
            }
        }
        return nodeChange;
    }

    private EgressNode getNodeById(String nodeId, List<EgressNode> nodeAfter) {
        if (null == nodeId || null == nodeAfter || 0 == nodeAfter.size()) {
            return null;
        }
        for (EgressNode node  : nodeAfter) {
            if (node.getNodeId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    private boolean checkChannelParaChange(Channel before, Channel after) {
        if (null == before || null == after) {
            return false;
        }
        if (!before.getName().equals(after.getName())
                || !before.getDomainId().equals(after.getDomainId())
                || !before.getIngressNode().equals(after.getIngressNode())
                || !before.getKey().equals(after.getKey())
                || !before.getGroupWildcard().equals(after.getGroupWildcard())
                || !before.getSourceWildcard().equals(after.getSourceWildcard())
                || !before.getSubDomainId().equals(after.getSubDomainId())
                || !before.getDstGroup().equals(after.getDstGroup())
                || !before.getSrcIp().equals(after.getSrcIp())) {
            return true;
        }
        return false;

    }



}
