/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl;


import org.opendaylight.bier.adapter.api.BierConfigReader;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.BierTeChannelWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.service.impl.activate.driver.ActivateNetconfConnetion;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceProvider.class);

    private final DataBroker dataBroker;
    private final RpcConsumerRegistry rpcConsumerRegistry;
    private final NotificationPublishService notificationService;
    private final ChannelConfigWriter bierChannelWriter;
    private final BierTeChannelWriter teChannelWriter;
    private final BierConfigWriter bierConfigWriter;
    private final BierTeBiftWriter bierTeBiftWriter;
    private final BierTeBitstringWriter bierTeBitstringWriter;
    private BierConfigReader bierConfigReader;

    private ServiceManager serviceManager;

    public ServiceProvider(final DataBroker dataBroker,
                           final RpcConsumerRegistry rpcConsumerRegistry,
                           final NotificationPublishService notificationService,
                           final BierConfigWriter bierConfigWriter, final ChannelConfigWriter channelConfigWriter,
                           final BierTeChannelWriter teChannelWriter,
                           final BierTeBiftWriter bierTeBiftWriter,
                           final BierTeBitstringWriter bierTeBitstringWriter) {
        this.dataBroker = dataBroker;
        this.rpcConsumerRegistry = rpcConsumerRegistry;
        this.notificationService = notificationService;
        this.bierConfigWriter = bierConfigWriter;
        this.bierChannelWriter = channelConfigWriter;
        this.teChannelWriter = teChannelWriter;
        this.bierTeBiftWriter = bierTeBiftWriter;
        this.bierTeBitstringWriter = bierTeBitstringWriter;
    }

    public void setBierConfigReader(BierConfigReader bierConfigReader) {
        this.bierConfigReader = bierConfigReader;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("ServiceProvider Session Initiated");
        serviceManager = new ServiceManager(dataBroker, notificationService, rpcConsumerRegistry, bierConfigWriter,
                bierChannelWriter, teChannelWriter, bierTeBiftWriter,bierTeBitstringWriter);
        ActivateNetconfConnetion activateNetconfConnetion = new ActivateNetconfConnetion(dataBroker,bierConfigReader);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("ServiceProvider Closed");
    }
}