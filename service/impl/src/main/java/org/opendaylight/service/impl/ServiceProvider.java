/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.service.impl.netconf.configuration.ConfigurationNetconfAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceProvider.class);

    private final DataBroker dataBroker;

    private BindingAwareBroker bindingAwareBroker = null;
    private ConfigurationNetconfAPI api;

    public ServiceProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setBindingRegistry(BindingAwareBroker bindingAwareBroker) {
        this.bindingAwareBroker = bindingAwareBroker;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("ServiceProvider Session Initiated");
        api = new ConfigurationNetconfAPI();
        bindingAwareBroker.registerConsumer(api);
        ServiceManager serviceManager = new ServiceManager(dataBroker);

    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("ServiceProvider Closed");
    }
}