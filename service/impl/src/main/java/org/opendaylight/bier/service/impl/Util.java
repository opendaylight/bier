/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.service.impl;

import com.google.common.base.Optional;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDest;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSource;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private DataBroker dataBroker;

    public Util(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    private InstanceIdentifier<TeBsl> getTeBslPath(String topologyId,String nodeId,DomainId domainId,
                                                   SubDomainId subDomainId,Bsl bitstringlength) {
        InstanceIdentifier<TeBsl> path = getTopoPath(topologyId).child(BierNode.class, new BierNodeKey(nodeId))
                .child(BierTeNodeParams.class).child(TeDomain.class, new TeDomainKey(domainId))
                .child(TeSubDomain.class, new TeSubDomainKey(subDomainId)).child(TeBsl.class,
                        new TeBslKey(bitstringlength));
        return path;
    }

    private InstanceIdentifier<TeSi> getTeSiPath(String topologyId,String nodeId,DomainId domainId,
                                                 SubDomainId subDomainId,Bsl bitstringlength,Si si) {
        InstanceIdentifier<TeSi> path = getTeBslPath(topologyId, nodeId, domainId, subDomainId, bitstringlength)
                .child(TeSi.class, new TeSiKey(si));
        return path;
    }


    private InstanceIdentifier<BierTopology> getTopoPath(String topologyId) {
        return InstanceIdentifier.create(BierNetworkTopology.class).child(BierTopology.class,
                new BierTopologyKey(topologyId));
    }

    public BierTopology getBierTopology(String topologyId) {
        InstanceIdentifier<BierTopology> topoPath = getTopoPath(topologyId);
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        try {
            return tx.read(LogicalDatastoreType.CONFIGURATION, topoPath).checkedGet().get();
        } catch (ReadFailedException e) {
            LOG.info(e.getStackTrace().toString());
        }
        return null;
    }


    public TeSi queryTeSi(String topologyId,String nodeId,DomainId domainId,
                            SubDomainId subDomainId,Bsl bitstringlength,Si si) {
        InstanceIdentifier<TeSi> path = getTeSiPath(topologyId, nodeId, domainId, subDomainId, bitstringlength, si);
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();

        try {
            Optional<TeSi> optional = tx.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (optional.isPresent()) {
                return optional.get();
            } else {
                return null;
            }
        } catch (ReadFailedException e) {
            LOG.info("Read teBsl from datastore failed!");
        }
        return null;
    }

    public BierLink getBierLinkByNodeIdAndTpId(String topologyId, String nodeId, String tpId) {
        for (BierLink bierLink : getBierTopology(topologyId).getBierLink()) {
            LinkSource linkSource = bierLink.getLinkSource();
            LinkDest linkDest = bierLink.getLinkDest();
            if (linkDest.getDestNode().equals(nodeId) && linkDest.getDestTp().equals(tpId)
                    || linkSource.getSourceNode().equals(nodeId) && linkSource.getSourceTp().equals(tpId)) {
                return bierLink;
            }
        }
        return null;
    }

    public BierLink getBierLinkByLinkId(String topologyId, String linkId) {
        for (BierLink bierLink : getBierTopology(topologyId).getBierLink()) {
            if (bierLink.getLinkId().equals(linkId)) {
                return bierLink;
            }
        }
        return null;
    }

    public Channel getChannelByName(String topologyId, String channelName) {
        InstanceIdentifier<Channel> identifier = InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class, new BierChannelKey(topologyId))
                .child(Channel.class, new ChannelKey(channelName));
        ReadWriteTransaction transaction = dataBroker.newReadWriteTransaction();
        try {
            Optional<Channel> optional = transaction.read(LogicalDatastoreType.CONFIGURATION, identifier).checkedGet();
            if (optional.isPresent()) {
                return optional.get();
            } else {
                return null;
            }
        } catch (ReadFailedException e) {
            LOG.info("Read channel from datastore failed!");
        }
        return null;
    }

    public BierNode getBierNodeByNodeId(String topologyId, String nodeId) {
        for (BierNode bierNode : getBierTopology(topologyId).getBierNode()) {
            if (bierNode.getNodeId().equals(nodeId)) {
                return bierNode;
            }
        }
        return null;
    }

    public IpAddress getIpAddressByNodeId(String topologyId, String nodeId) {
        BierNode bierNode = getBierNodeByNodeId(topologyId, nodeId);
        if (null != bierNode) {
            Domain domain = bierNode.getBierNodeParams().getDomain().get(0);
            String ipv4 = domain.getBierGlobal().getIpv4BfrPrefix().getValue();
            ipv4 = ipv4.substring(0,ipv4.indexOf("/"));
            Ipv4Address ipv4Address = new Ipv4Address(ipv4);

            if (null != ipv4Address) {
                return new IpAddress(ipv4Address);
            } else {
                String ipv6 = domain.getBierGlobal().getIpv6BfrPrefix().getValue();
                ipv6 = ipv6.substring(0,ipv6.indexOf("/"));
                Ipv6Address ipv6Address = new Ipv6Address(ipv6);
                return new IpAddress(ipv6Address);
            }
        }

        return null;
    }
}
