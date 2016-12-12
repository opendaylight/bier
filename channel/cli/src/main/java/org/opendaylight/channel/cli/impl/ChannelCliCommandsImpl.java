/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.cli.impl;

import org.opendaylight.channel.cli.api.ChannelCliCommands;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelCliCommandsImpl implements ChannelCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public ChannelCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("ChannelCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}