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
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.bier.driver.common.util.DataGetter;
import org.opendaylight.bier.driver.common.util.IidBuilder;
import org.opendaylight.bier.driver.common.util.IidConstants;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.mstatic.rev171107.multicast.information.pure.multicast.pure.multicast.multicast.overlay.OutgoingInterfaces;
import org.opendaylight.yang.gen.v1.urn.bier.mstatic.rev171107.multicast.information.pure.multicast.pure.multicast.multicast.overlay.OutgoingInterfacesBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.mstatic.rev171107.multicast.information.pure.multicast.pure.multicast.multicast.overlay.outgoing.interfaces.OutgoingInterfacesIndexes;
import org.opendaylight.yang.gen.v1.urn.bier.mstatic.rev171107.multicast.information.pure.multicast.pure.multicast.multicast.overlay.outgoing.interfaces.OutgoingInterfacesIndexesBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticast;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticastBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.pure.multicast.MulticastOverlayBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.overlay.BierInformationBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelDataBuilder {

    private DataBroker dataBroker;
    private static final Logger LOG = LoggerFactory.getLogger(ChannelDataBuilder.class);

    public ChannelDataBuilder(DataBroker dataBroker) {
        this.dataBroker = dataBroker;

    }



    public Long getIfIndexByTpId(String tpId,String nodeId) {

        LOG.info("Get interface index by tp-id {}, node id : {}",tpId,nodeId);

        BierTerminationPoint bierTerminationPoint =
                DataGetter.readData(dataBroker,
                        IidConstants.BIER_TOPO_IID
                                .child(BierNode.class, new BierNodeKey(nodeId))
                                .child(BierTerminationPoint.class, new BierTerminationPointKey(tpId)),
                        LogicalDatastoreType.CONFIGURATION);
        if (bierTerminationPoint == null) {
            LOG.error("Get interface index failed  by tp id = {} failed, node id = {}",tpId,nodeId);
        }
        return bierTerminationPoint.getTpIndex();
    }

    public OutgoingInterfaces buildOifInfo(String nodeId,List<RcvTp> recvTpList) {
        ArrayList<OutgoingInterfacesIndexes> oifList = new ArrayList<>();

        for (RcvTp recvTp : recvTpList) {

            oifList.add(new OutgoingInterfacesIndexesBuilder()
                    .setInterfaceIndex(getIfIndexByTpId(recvTp.getTp(),nodeId))
                    .build());


        }

        return new OutgoingInterfacesBuilder()
                .setOutgoingInterfacesIndexes(oifList)
                .build();
    }





    public PureMulticast buildKey(Channel channel) {
        return  new PureMulticastBuilder()
                .setGroupAddress(channel.getDstGroup())
                .setGroupWildcard(channel.getGroupWildcard())
                .setSourceAddress(channel.getSrcIp())
                .setSourceWildcard(channel.getSourceWildcard())
                .setVpnId(IidBuilder.DEFAULT_VPN_ID)
                .build();
    }


    public PureMulticast build(Channel channel) {

        BierInformationBuilder bierInformationBuilder = new BierInformationBuilder();

        if (channel.getSubDomainId() != null) {
            bierInformationBuilder.setSubDomain(new SubDomainId(channel.getSubDomainId().getValue()));
        }

        if (channel.getIngressBfrId() != null) {

            bierInformationBuilder
                    .setIngressNode(new BfrId(channel.getIngressBfrId().getValue()));
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
                .setVpnId(IidBuilder.DEFAULT_VPN_ID)
                .setMulticastOverlay(new MulticastOverlayBuilder()
                        .setBierInformation(bierInformationBuilder.build())
                        .build())
                .build();



    }




}
