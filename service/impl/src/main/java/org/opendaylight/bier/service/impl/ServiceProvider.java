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
import org.opendaylight.bier.adapter.api.BierTeBtaftWriter;
import org.opendaylight.bier.adapter.api.BierTeChannelWriter;
import org.opendaylight.bier.adapter.api.BierTeLabelRangeConfigWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.service.impl.activate.driver.ActivateNetconfConnetion;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceProvider.class);

    private final DataBroker dataBroker;
    private final RpcConsumerRegistry rpcConsumerRegistry;
    private final RpcProviderRegistry rpcProviderRegistry;
    private final NotificationPublishService notificationService;
    private final ChannelConfigWriter bierChannelWriter;
    private final BierTeChannelWriter teChannelWriter;
    private final BierConfigWriter bierConfigWriter;
    private final BierTeBiftWriter bierTeBiftWriter;
    private final BierTeBtaftWriter bierTeBtaftWriter;
    private final BierTeBitstringWriter bierTeBitstringWriter;
    private final BierTeLabelRangeConfigWriter bierTeLabelRangeConfigWriter;
    private final NotificationService registerService;
    private BierConfigReader bierConfigReader;
    private ServiceManager serviceManager;


    public ServiceProvider(final DataBroker dataBroker,
                           final RpcConsumerRegistry rpcConsumerRegistry,
                           final RpcProviderRegistry rpcProviderRegistry,
                           final NotificationPublishService notificationService,
                           final BierConfigWriter bierConfigWriter,
                           final ChannelConfigWriter channelConfigWriter,
                           final BierTeChannelWriter teChannelWriter,
                           final BierTeBiftWriter bierTeBiftWriter,
                           final BierTeBtaftWriter bierTeBtaftWriter,
                           final BierTeBitstringWriter bierTeBitstringWriter,
                           final BierTeLabelRangeConfigWriter bierTeLabelRangeConfigWriter,
                           final NotificationService registerService) {
        this.dataBroker = dataBroker;
        this.rpcConsumerRegistry = rpcConsumerRegistry;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.notificationService = notificationService;
        this.bierConfigWriter = bierConfigWriter;
        this.bierChannelWriter = channelConfigWriter;
        this.teChannelWriter = teChannelWriter;
        this.bierTeBiftWriter = bierTeBiftWriter;
        this.bierTeBtaftWriter = bierTeBtaftWriter;
        this.bierTeBitstringWriter = bierTeBitstringWriter;
        this.bierTeLabelRangeConfigWriter = bierTeLabelRangeConfigWriter;
        this.registerService = registerService;
    }

    public void setBierConfigReader(BierConfigReader bierConfigReader) {
        this.bierConfigReader = bierConfigReader;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("ServiceProvider Session Initiated");
        serviceManager = new ServiceManager(dataBroker, notificationService, rpcConsumerRegistry, rpcProviderRegistry,
                bierConfigWriter, bierChannelWriter, teChannelWriter, bierTeBiftWriter, bierTeBtaftWriter,
                bierTeBitstringWriter, bierTeLabelRangeConfigWriter, registerService);
        new ActivateNetconfConnetion(dataBroker,bierConfigReader);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("ServiceProvider Closed");
        serviceManager.close();
    }
}