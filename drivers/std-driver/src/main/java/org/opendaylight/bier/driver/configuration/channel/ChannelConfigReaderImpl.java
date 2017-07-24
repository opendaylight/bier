/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.channel;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;
import org.opendaylight.bier.adapter.api.ChannelConfigReader;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.common.util.IidBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticast;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelConfigReaderImpl implements ChannelConfigReader {
    private static final Logger LOG = LoggerFactory.getLogger(ChannelConfigReaderImpl.class);
    private NetconfDataOperator netconfDataOperator ;

    public ChannelConfigReaderImpl(NetconfDataOperator netconfDataOperator) {
        this.netconfDataOperator = netconfDataOperator;
    }

    public List<BfrId> readChannel(Channel channel) {
        InstanceIdentifier<PureMulticast> pureMulticastIid = IidBuilder.buildPureMulticastIId(channel);
        PureMulticast pureMulticast = netconfDataOperator.read(channel.getIngressNode(),pureMulticastIid);
        if (pureMulticast != null) {
            if ((pureMulticast.getMulticastOverlay() != null)
                    && (pureMulticast.getMulticastOverlay().getBierInformation() != null)) {
                List<EgressNodes> egressNodesList =
                        pureMulticast.getMulticastOverlay().getBierInformation().getEgressNodes();
                if ((egressNodesList != null) && (!egressNodesList.isEmpty())) {
                    return Lists.transform(egressNodesList,
                            new Function<EgressNodes, BfrId>() {
                                @java.lang.Override
                                public BfrId apply(EgressNodes input) {
                                    return new BfrId(input.getEgressNode().getValue());
                                }
                            });
                }

            }
        }
        return null;
    }
}
