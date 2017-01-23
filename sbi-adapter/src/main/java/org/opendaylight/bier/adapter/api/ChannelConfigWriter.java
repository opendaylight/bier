/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.adapter.api;


import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;



public interface ChannelConfigWriter {

    ConfigurationResult writeChannel(ConfigurationType type, Channel channel);

    ConfigurationResult writeChannelEgressNode(ConfigurationType type, Channel channel);
}
