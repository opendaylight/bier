/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.cli.impl;

import org.opendaylight.bier.cli.api.PceCliCommands;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PceCliCommandsImpl implements PceCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(PceCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public PceCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("PceCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}