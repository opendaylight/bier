/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl;

import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.BierTeChannelWriter;
import org.opendaylight.bier.adapter.api.BierTeLabelRangeConfigWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.service.impl.bierconfig.BierNodeChangeListener;
import org.opendaylight.bier.service.impl.teconfig.BierNodeTeBpChangeListener;
import org.opendaylight.bier.service.impl.teconfig.BierTeLabelRangeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceManager.class);

    private BierNodeChangeListener bierNodeChangeListener;
    private ChannelChangeListener channelChangeListener;
    private NetconfStateChangeListener netconfStateChangeListener;
    private BierNodeTeBpChangeListener bierNodeTeBpChangeListener;
    private BierTeLabelRangeChangeListener bierTeLabelRangeChangeListener;

    public ServiceManager(final DataBroker dataBroker, final NotificationPublishService notificationService,
                          final RpcConsumerRegistry rpcConsumerRegistry, BierConfigWriter bierConfig,
                          ChannelConfigWriter channelConfigWriter, BierTeChannelWriter teChannelWriter,
                          BierTeBiftWriter bierTeBiftWriter, BierTeBitstringWriter bierTeBitstringWriter,
                          BierTeLabelRangeConfigWriter bierTeLabelRangeConfigWriter) {
        LOG.info("set notificationPublishService");
        NotificationProvider.getInstance().setNotificationService(notificationService);
        LOG.info("register bier-node listener");
        bierNodeChangeListener = new BierNodeChangeListener(bierConfig);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<BierNodeParams>(
                LogicalDatastoreType.CONFIGURATION, bierNodeChangeListener.getBierNodeId()), bierNodeChangeListener);

        LOG.info("register bier-channel listener");
        channelChangeListener = new ChannelChangeListener(dataBroker, rpcConsumerRegistry, channelConfigWriter,
                teChannelWriter, bierTeBiftWriter,bierTeBitstringWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<Channel>(
                LogicalDatastoreType.CONFIGURATION, channelChangeListener.getChannelId()), channelChangeListener);

        LOG.info("register netconfstate listener");
        netconfStateChangeListener = new NetconfStateChangeListener(dataBroker,bierConfig,channelConfigWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<Node>(
                LogicalDatastoreType.OPERATIONAL, netconfStateChangeListener.getNodeId()), netconfStateChangeListener);

        LOG.info("register bier-node-tebp listener");
        bierNodeTeBpChangeListener = new BierNodeTeBpChangeListener(dataBroker, rpcConsumerRegistry, bierTeBiftWriter,
                bierTeBitstringWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<TeBp>(
                        LogicalDatastoreType.CONFIGURATION, bierNodeTeBpChangeListener.getTeBpIid()),
                bierNodeTeBpChangeListener);
        LOG.info("register bier-te-lable-range");
        bierTeLabelRangeChangeListener = new BierTeLabelRangeChangeListener(bierTeLabelRangeConfigWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<BierTeLableRange>(
                LogicalDatastoreType.CONFIGURATION, bierTeLabelRangeChangeListener.getBierTeLableRangeIid()),
                bierTeLabelRangeChangeListener);
    }

}
