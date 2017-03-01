/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.BierServiceApiService;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.ReportMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServiceManager implements BierServiceApiService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceManager.class);

    private BierNodeChangeListener bierNodeChangeListener;
    private ChannelChangeListener channelChangeListener;
    private NetconfStateChangeListener netconfStateChangeListener;

    public ServiceManager(final DataBroker dataBroker, final NotificationPublishService notificationService,
                          BierConfigWriter bierConfig, ChannelConfigWriter bierChannelWriter) {
        LOG.info("set notificationPublishService");
        NotificationProvider.getInstance().setNotificationService(notificationService);
        LOG.info("register bier-node listener");
        bierNodeChangeListener = new BierNodeChangeListener(bierConfig);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<BierNode>(
                LogicalDatastoreType.CONFIGURATION, bierNodeChangeListener.getBierNodeId()), bierNodeChangeListener);

        LOG.info("register bier-channel listener");
        channelChangeListener = new ChannelChangeListener(dataBroker,bierChannelWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<Channel>(
                LogicalDatastoreType.CONFIGURATION, channelChangeListener.getChannelId()), channelChangeListener);

        LOG.info("register netconfstate listener");
        netconfStateChangeListener = new NetconfStateChangeListener(dataBroker,bierConfig,bierChannelWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<Node>(
                LogicalDatastoreType.OPERATIONAL, netconfStateChangeListener.getNodeId()), netconfStateChangeListener);
    }

    public Future<RpcResult<Void>> testNotificationPublish() {
        NotificationProvider.getInstance().notify(new ReportMessageBuilder().setFailureReason("test111").build());
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

}
