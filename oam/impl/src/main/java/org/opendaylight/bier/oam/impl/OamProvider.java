/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.oam.impl;

import org.opendaylight.bier.adapter.api.BierOamStartEchoRequestCheck;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.BierOamApiService;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.BierServiceApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OamProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OamProvider.class);
    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private final NotificationPublishService notificationService;
    private final BierOamStartEchoRequestCheck bierOamStartEchoRequestCheck;
    private OamImpl oamImpl;
    private BierServiceApiService bierService;
    private BindingAwareBroker.RpcRegistration<BierOamApiService> oamService;

    public OamProvider(final DataBroker dataBroker,final RpcProviderRegistry rpcProviderRegistry,
                       final NotificationPublishService notificationService,
                       final BierOamStartEchoRequestCheck bierOamStartEchoRequestCheck, final OamImpl oamImpl) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcProviderRegistry;
        this.notificationService = notificationService;
        this.bierOamStartEchoRequestCheck = bierOamStartEchoRequestCheck;
        this.oamImpl = oamImpl;
        this.bierService = rpcProviderRegistry.getRpcService(BierServiceApiService.class);
    }

    public void init() {
        LOG.info("OamProvider Session Initiated");
        DbProvider.getInstance().setDataBroker(dataBroker);
        NotificationProvider.getInstance().setNotificationService(notificationService);
        new ConfigureOam(bierOamStartEchoRequestCheck);
        //oamImpl = new OamImpl();
        oamService = rpcRegistry.addRpcImplementation(BierOamApiService.class,oamImpl);
        oamImpl.setBierService(bierService);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("OamProvider Closed");
        oamImpl.destroy();
        if (oamService != null) {
            oamService.close();
        }
    }
}
