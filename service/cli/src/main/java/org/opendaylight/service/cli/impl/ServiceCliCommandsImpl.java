/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.service.cli.api.ServiceCliCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceCliCommandsImpl implements ServiceCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public ServiceCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("ServiceCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}