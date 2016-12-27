/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.channel;


import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;


import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticast;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticastBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.pure.multicast.MulticastOverlayBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.overlay.BierInformationBuilder;



public class ChannelDataBuilder {

    public static final java.lang.Long DEFAULT_VPN_ID = new java.lang.Long(0);

    public PureMulticast build(Channel channel) {

        PureMulticastBuilder pureMulticastBuilder = new PureMulticastBuilder();

        BierInformationBuilder bierInformationBuilder = new BierInformationBuilder();

        if (channel.getSubDomainId() != null ) {
            bierInformationBuilder.setSubDomain(new SubDomainId(channel.getSubDomainId().getValue()) );
        }

        if (channel.getIngressBfrId() != null ) {

            bierInformationBuilder.setIngressNode(new BfrId(channel.getIngressBfrId().getValue()) );
        }
        if (channel.getEgressNode() != null) {
            bierInformationBuilder.setEgressNodes(Lists.transform(channel.getEgressNode(),
                    new Function<EgressNode,EgressNodes>() {
                        @Override
                        public EgressNodes apply(EgressNode input) {
                            Preconditions.checkNotNull(input.getEgressBfrId(),"channel egress node invalid");
                            return new EgressNodesBuilder()
                                    .setEgressNode(new BfrId(input.getEgressBfrId().getValue()))
                                    .build();
                        }

                    }
            ));
        }


        return  new PureMulticastBuilder()
                .setGroupAddress(channel.getDstGroup())
                .setGroupWildcard(channel.getGroupWildcard())
                .setSourceAddress(channel.getSrcIp())
                .setSourceWildcard(channel.getSourceWildcard())
                .setVpnId(DEFAULT_VPN_ID)
                .setMulticastOverlay(new MulticastOverlayBuilder()
                        .setBierInformation(bierInformationBuilder.build())
                        .build())
                .build();



    }




}
