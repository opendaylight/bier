/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver;

import org.opendaylight.bier.adapter.api.BierConfigReader;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigReader;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.driver.configuration.channel.ChannelConfigReaderImpl;
import org.opendaylight.bier.driver.configuration.channel.ChannelConfigWriterImpl;
import org.opendaylight.bier.driver.configuration.node.BierConfigReaderImpl;
import org.opendaylight.bier.driver.configuration.node.BierConfigWriterImpl;
import org.opendaylight.bier.driver.listener.NetconfNodesListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierNetconfDataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BierNetconfDataProvider.class);


    private final DataBroker dataBroker;

    private BindingAwareBroker bindingAwareBroker;
    private ServiceRegistration<BierConfigWriter> registrationBierConfig;
    private ServiceRegistration<BierConfigReader> registrationBierReader;
    private ServiceRegistration<ChannelConfigWriter> registrationChannelConfig;
    private ServiceRegistration<ChannelConfigReader> registrationChannelReader;

    private NetconfNodesListener netconfNodesListener;

    public BierNetconfDataProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setBindingRegistry(BindingAwareBroker bindingAwareBroker) {
        this.bindingAwareBroker = bindingAwareBroker;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("session init");
        NetconfDataOperator netconfDataOperator = new NetconfDataOperator();
        bindingAwareBroker.registerConsumer(netconfDataOperator);

        final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        BierConfigWriterImpl bierConfigImpl = new BierConfigWriterImpl(netconfDataOperator);
        registrationBierConfig = context.registerService(BierConfigWriter.class, bierConfigImpl, null);
        ChannelConfigWriterImpl channelConfigImpl = new ChannelConfigWriterImpl(netconfDataOperator);
        registrationChannelConfig = context.registerService(ChannelConfigWriter.class, channelConfigImpl, null);
        BierConfigReaderImpl bierReaderImpl = new BierConfigReaderImpl(netconfDataOperator);
        registrationBierReader = context.registerService(BierConfigReader.class, bierReaderImpl, null);
        ChannelConfigReaderImpl channelConfigReader = new ChannelConfigReaderImpl(netconfDataOperator);
        registrationChannelReader = context.registerService(ChannelConfigReader.class, channelConfigReader, null);

        netconfNodesListener = new NetconfNodesListener(dataBroker,netconfDataOperator);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {

        LOG.info("session closed");

        if (registrationBierConfig != null) {
            registrationBierConfig.unregister();
        }

        if (registrationChannelConfig != null) {
            registrationChannelConfig.unregister();
        }

        if (registrationBierReader != null) {
            registrationBierReader.unregister();
        }

        if (netconfNodesListener != null) {
            netconfNodesListener.unregisterListener();
        }



    }


}
