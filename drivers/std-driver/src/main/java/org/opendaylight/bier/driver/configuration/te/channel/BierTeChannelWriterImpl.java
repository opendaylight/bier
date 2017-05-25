/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.te.channel;

import com.google.common.util.concurrent.CheckedFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.bier.adapter.api.BierTeChannelWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.common.util.DataWriter;
import org.opendaylight.bier.driver.common.util.IidBuilder;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticast;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticastBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.pure.multicast.MulticastTransportBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.transport.BierTeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.transport.bier.te.Path;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.transport.bier.te.PathBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class BierTeChannelWriterImpl implements BierTeChannelWriter {
    private static final Logger LOG = LoggerFactory.getLogger(BierTeChannelWriterImpl.class);
    private NetconfDataOperator netconfDataOperator ;

    public BierTeChannelWriterImpl(NetconfDataOperator netconfDataOperator) {
        this.netconfDataOperator = netconfDataOperator;
    }


    public CheckedFuture<Void, TransactionCommitFailedException> writeBierTeChannel(ConfigurationType type,
                                                                                      String nodeId,
                                                                                      Channel channel,
                                                                                      List<Long> pathId,
                                                                                      ConfigurationResult result) {

        InstanceIdentifier<PureMulticast> pureMulticastIId = IidBuilder.buildPureMulticastIId(channel);


        List<Path> pathList = new ArrayList<>();
        for (Long path : pathId) {
            pathList.add(new PathBuilder().setPathId(path).build());
        }


        PureMulticast pureMulticast = new PureMulticastBuilder()
                .setGroupAddress(channel.getDstGroup())
                .setGroupWildcard(channel.getGroupWildcard())
                .setSourceAddress(channel.getSrcIp())
                .setSourceWildcard(channel.getSourceWildcard())
                .setVpnId(IidBuilder.DEFAULT_VPN_ID)
                .setMulticastTransport(new MulticastTransportBuilder()
                        .setBierTe(new BierTeBuilder().setPath(pathList).build())
                        .build())
                .build();

        switch (type) {
            case DELETE :
                return netconfDataOperator.write(
                        DataWriter.OperateType.DELETE,
                        channel.getIngressNode(),
                        pureMulticastIId,
                        null,
                        result);
            case MODIFY : {
                netconfDataOperator.write(
                        DataWriter.OperateType.DELETE,
                        channel.getIngressNode(),
                        pureMulticastIId,
                        null,
                        result);
                return netconfDataOperator.write(
                        DataWriter.OperateType.MERGE,
                        channel.getIngressNode(),
                        pureMulticastIId,
                        pureMulticast,
                        result);
            }
            case ADD :
                return netconfDataOperator.write(
                        DataWriter.OperateType.MERGE,
                        channel.getIngressNode(),
                        pureMulticastIId,
                        pureMulticast,
                        result);

            default : {
                LOG.info("Invalid config type : {}",type);
                return null;
            }

        }
    }


    public ConfigurationResult writeBierTeChannel(ConfigurationType type, String nodeId,
                                                    Channel channel,List<Long> pathId) {
        LOG.info("Config bier te channel, type--{}, channel--{},path--{}",type,channel,pathId);
        ConfigurationResult result = new ConfigurationResult(ConfigurationResult.Result.FAILED);
        if ((pathId == null) || (pathId.isEmpty())) {
            result.setFailureReason(ConfigurationResult.PATH_NULL + channel.toString());
            LOG.info(ConfigurationResult.PATH_NULL + channel.toString());
            return result;
        }

        writeBierTeChannel(type,nodeId,channel,pathId,result);
        return result;

    }

}
