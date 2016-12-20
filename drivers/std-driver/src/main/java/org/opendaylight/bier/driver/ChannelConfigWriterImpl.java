/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver;

import org.opendaylight.bier.adapter.api.BierConfigResult;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelConfigWriterImpl implements ChannelConfigWriter {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelConfigWriterImpl.class);
    private static final BierConfigResult RESULT_SUCCESS =
            new BierConfigResult(BierConfigResult.ConfigurationResult.SUCCESSFUL);

    public BierConfigResult writeChannel(ConfigurationType type, Channel channel) {
        return RESULT_SUCCESS;
    }

    public BierConfigResult writeChannelEgressNode(ConfigurationType type, Channel channel) {
        return RESULT_SUCCESS;
    }

}

