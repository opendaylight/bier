/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.common;

import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.MulticastInformation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticast;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticastKey;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class IidBuilder {

    public static final java.lang.Long DEFAULT_VPN_ID = new java.lang.Long(0);

    public static InstanceIdentifier<PureMulticast> buildPureMulticastIId(Channel channel) {
        return InstanceIdentifier.create(MulticastInformation.class)
                .child(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                        .multicast.information.rev161028.multicast.information.PureMulticast.class)
                .child(PureMulticast.class,new PureMulticastKey(channel.getDstGroup(),
                        channel.getGroupWildcard(),
                        channel.getSrcIp(),
                        channel.getSourceWildcard(),
                        DEFAULT_VPN_ID));


    }
}
