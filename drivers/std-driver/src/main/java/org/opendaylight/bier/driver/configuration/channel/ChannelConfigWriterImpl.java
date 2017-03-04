/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.channel;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.List;


import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;

import org.opendaylight.bier.driver.common.util.DataWriter;
import org.opendaylight.bier.driver.common.util.IidBuilder;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;


import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.MulticastInformation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodesKey;


import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticast;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticastKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.pure.multicast.MulticastOverlay;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.overlay.BierInformation;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelConfigWriterImpl implements ChannelConfigWriter {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelConfigWriterImpl.class);

    private NetconfDataOperator netconfDataOperator ;
    private ChannelDataBuilder channelDataBuilder = new ChannelDataBuilder();

    public ChannelConfigWriterImpl(NetconfDataOperator netconfDataOperator) {
        this.netconfDataOperator = netconfDataOperator;
    }

    public InstanceIdentifier<PureMulticast> buildPureMulticastIId(Channel channel) {
        return InstanceIdentifier.create(MulticastInformation.class)
                .child(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                        .multicast.information.rev161028.multicast.information.PureMulticast.class)
                .child(PureMulticast.class,new PureMulticastKey(channel.getDstGroup(),
                        channel.getGroupWildcard(),
                        channel.getSrcIp(),
                        channel.getSourceWildcard(),
                        IidBuilder.DEFAULT_VPN_ID));


    }




    public CheckedFuture<Void, TransactionCommitFailedException>
        writeChannel(ConfigurationType type,Channel channel,ConfigurationResult result) {

        if (type == ConfigurationType.DELETE) {
            LOG.info("delete channel {} in node {} ",channel,channel.getIngressNode());
            return netconfDataOperator.write(
                    DataWriter.OperateType.DELETE,
                    channel.getIngressNode(),
                    buildPureMulticastIId(channel),
                    null,
                    result);
        }
        LOG.info("channel config to node {} :: {}",channel.getIngressNode(),channel);
        return netconfDataOperator.write(
                DataWriter.OperateType.MERGE,
                channel.getIngressNode(),
                buildPureMulticastIId(channel),
                channelDataBuilder.build(channel),
                result
        );
    }

    public ConfigurationResult writeChannel(ConfigurationType type, Channel channel) {
        ConfigurationResult result =
                new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        writeChannel(type,channel,result);
        return result;

    }

    public CheckedFuture<Void, TransactionCommitFailedException>
        writeChannelEgressNode(ConfigurationType type,Channel channel,ConfigurationResult result) {
        List<EgressNode> egressNodeList = channel.getEgressNode();
        if ((egressNodeList == null) || egressNodeList.isEmpty()) {
            result.setCfgResult(ConfigurationResult.Result.FAILED);
            result.setFailureReason(ConfigurationResult.EGRESS_INFO_NULL);
            return null;
        }
        CheckedFuture<Void, TransactionCommitFailedException> checkedFuture = null;
        for (EgressNode egressNode:egressNodeList) {
            Preconditions.checkNotNull(egressNode.getEgressBfrId(),"channel egress node invalid");
            BfrId bfrId = new BfrId(egressNode.getEgressBfrId().getValue());
            InstanceIdentifier<EgressNodes> egressNodesIId =
                    buildPureMulticastIId(channel)
                            .child(MulticastOverlay.class)
                            .child(BierInformation.class)
                            .child(EgressNodes.class,
                                    new EgressNodesKey(bfrId));


            if (type == ConfigurationType.DELETE) {
                LOG.info("delete  {} of channel {} in node {} ",
                        egressNode,channel.getKey(),channel.getIngressNode());
                checkedFuture = netconfDataOperator.write(
                        DataWriter.OperateType.DELETE,
                        channel.getIngressNode(),
                        egressNodesIId,
                        null,
                        result);
            } else {
                LOG.info("config  {} of channel {} to node {} ",
                        egressNode,channel.getKey(),channel.getIngressNode());
                checkedFuture = netconfDataOperator.write(
                        DataWriter.OperateType.MERGE,
                        channel.getIngressNode(),
                        egressNodesIId,
                        new EgressNodesBuilder().setEgressNode(bfrId).build(),
                        result);
            }
            if (!result.isSuccessful()) {
                return checkedFuture;
            }
        }
        return checkedFuture;
    }




    public ConfigurationResult writeChannelEgressNode(ConfigurationType type, Channel channel) {
        ConfigurationResult result =
                new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        writeChannelEgressNode(type, channel, result);
        return result;
    }


}

