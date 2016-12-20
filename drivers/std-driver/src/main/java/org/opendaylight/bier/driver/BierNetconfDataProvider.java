/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver;

import  org.opendaylight.bier.adapter.api.BierConfigWriter;
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
    private ServiceRegistration<BierConfigWriter> registration;

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
        final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        BierConfigWriterImpl impl = new BierConfigWriterImpl();
        registration = context.registerService(BierConfigWriter.class, impl, null);
        NetconfDataOperator netconfWrtier = new NetconfDataOperator();
        bindingAwareBroker.registerConsumer(netconfWrtier);
        NetconfNodesListener netconfNodesListener = new NetconfNodesListener(dataBroker);

    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {

        LOG.info("session closed");
        registration.unregister();
    }


}
