/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.allocatebp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PathMonopolyBPAllocateStrategy extends AbstractBPAllocateStrategy {
    private static PathMonopolyBPAllocateStrategy instance = new PathMonopolyBPAllocateStrategy();

    private static final Logger LOG = LoggerFactory.getLogger(PathMonopolyBPAllocateStrategy.class);
    private Map<String, String> channelBfirNodeId = new HashMap<>();
    private Map<String, String> channelBfirTpId = new HashMap<>();
    private Map<ChannelNameBferNodeId, List<String>> bferDstTpListMap = new HashMap<>();

    private PathMonopolyBPAllocateStrategy() {

    }

    @Override
    public boolean allocateBPs(Channel channel, List<Bfer> bferList) {
        LOG.info("Allocate bps for bferlist {} of channel {}.", bferList, channel);
        if (null == bferList || bferList.isEmpty()) {
            return true;
        }
        return allocateOrRecycleBPs(channel, bferList, ALLOCATE_BPS);
    }

    @Override
    public boolean recycleBPs(Channel channel, List<Bfer> bferList) {
        LOG.info("Recycle bps for bferlist {} of channel {}.", bferList, channel);
        if (null == bferList || bferList.isEmpty()) {
            return true;
        }
        return allocateOrRecycleBPs(channel, bferList, RECYCLE_BPS);
    }

    private boolean allocateOrRecycleBPs(Channel channel, List<Bfer> bferList, int type) {
        List<String> nodeIdList = new ArrayList<>();
        List<String> tpIdList = new ArrayList<>();
        for (int i = 0;i < bferList.size();i++) {
            nodeIdList.clear();
            tpIdList.clear();
            Bfer bfer = bferList.get(i);

            if (type == ALLOCATE_BPS) {
                nodeIdList.add(channel.getIngressNode());
                tpIdList.add(channel.getSrcTp());
                channelBfirNodeId.put(channel.getName(),channel.getIngressNode());
                channelBfirTpId.put(channel.getName(),channel.getSrcTp());
            } else {
                nodeIdList.add(channelBfirNodeId.get(channel.getName()));
                tpIdList.add(channelBfirTpId.get(channel.getName()));
            }

            List<PathLink> pathLinkList = bfer.getBierPath().getPathLink();
            for (PathLink pathLink : pathLinkList) {
                nodeIdList.add(pathLink.getLinkSource().getSourceNode());
                tpIdList.add(pathLink.getLinkSource().getSourceTp());
                nodeIdList.add(pathLink.getLinkDest().getDestNode());
                tpIdList.add(pathLink.getLinkDest().getDestTp());
            }

            String egressNodeId = nodeIdList.get(nodeIdList.size() - 1);
            ChannelNameBferNodeId nameBfer = new ChannelNameBferNodeId(channel.getName(),egressNodeId);

            if (type == ALLOCATE_BPS) {
                for (EgressNode egressNode : channel.getEgressNode()) {
                    List<String> rcvTpList = new ArrayList<>();
                    if (egressNode.getNodeId().equals(egressNodeId)) {
                        for (RcvTp rcvTp : egressNode.getRcvTp()) {
                            nodeIdList.add(egressNode.getNodeId());
                            tpIdList.add(rcvTp.getTp());
                            rcvTpList.add(rcvTp.getTp());
                        }
                        bferDstTpListMap.put(nameBfer,rcvTpList);
                    }
                }
            } else {
                for (String rcvTp:bferDstTpListMap.get(nameBfer)) {
                    nodeIdList.add(egressNodeId);
                    tpIdList.add(rcvTp);
                }
                bferDstTpListMap.remove(nameBfer);
            }

            ChannelNameBferNodeId channelNameBferNodeId = new ChannelNameBferNodeId(channel.getName(),
                    egressNodeId);

            if (type == ALLOCATE_BPS) {
                TeBsl teBsl = checkAndGetOneBsl(channel.getSubDomainId().getValue());
                TeSi teSi = checkAndGetOneSi(channel.getSubDomainId().getValue(), teBsl);
                if (null == teSi) {
                    return false;
                }
                LOG.info("Set channel-bfer to si map");
                SubdomainBslSi subdomainBslSi = new SubdomainBslSi(channel.getSubDomainId().getValue(), teBsl, teSi);
                channelBferSubdomainBslSiMap.put(channelNameBferNodeId, subdomainBslSi);
                LOG.info("Get and allocate bp for each tpId");
                if (!getAndAllocateBpForEachTpId(channel,teBsl,teSi,nodeIdList,tpIdList)) {
                    return false;
                }
            } else if (type == RECYCLE_BPS) {
                SubdomainBslSi subdomainBslSi = channelBferSubdomainBslSiMap.get(channelNameBferNodeId);
                TeBsl teBsl = subdomainBslSi.getTeBsl();
                LOG.info("Recycle si and bp");
                if (!recycleBslSiBp(channel.getSubDomainId().getValue(), teBsl, subdomainBslSi.getTeSi(), null)) {
                    return false;
                }
                LOG.info("Delete channel-bfer to si map");
                channelBferSubdomainBslSiMap.remove(channelNameBferNodeId);
                LOG.info("Get and remove bp for each tpId");
                if (!removeBpForEachTpId(channel, teBsl, subdomainBslSi.getTeSi(), nodeIdList, tpIdList)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean getAndAllocateBpForEachTpId(Channel channel,TeBsl teBsl,TeSi teSi,List<String> nodeIdList,
                                             List<String> tpIdList) {
        TeDomain teDomain = getTeDomainFromChannel(channel);
        TeSubDomain teSubDomain = getTeSubDomainFromChannel(channel);
        for (int i = 0;i < nodeIdList.size();i++) {
            String tpId = tpIdList.get(i);
            TeBp teBp = checkAndGetOneBP(channel.getSubDomainId().getValue(), teBsl,teSi);
            TeBpBuilder teBpBuilder = new TeBpBuilder(teBp);
            teBpBuilder.setKey(new TeBpKey(tpId));
            teBpBuilder.setTpId(tpId);
            teBp = teBpBuilder.build();
            String nodeId = nodeIdList.get(i);
            boolean flag = configureOneBPToNode(TOPOLOGY_ID,nodeId,teDomain,teSubDomain,teBsl,teSi,teBp);
            if (!flag) {
                LOG.info("Auto allocate bp write bier node params failed");
                return false;
            }
        }
        return true;
    }

    private boolean removeBpForEachTpId(Channel channel, TeBsl teBsl,TeSi teSi,List<String> nodeIdList,
                                     List<String> tpIdList) {
        DomainId domainId = getTeDomainFromChannel(channel).getDomainId();
        SubDomainId subDomainId = getTeSubDomainFromChannel(channel).getSubDomainId();
        Bsl bsl = teBsl.getBitstringlength();
        Si si = teSi.getSi();
        for (int i = 0;i < nodeIdList.size();i++) {
            String nodeId = nodeIdList.get(i);
            String tpId = tpIdList.get(i);
            BierNode bierNode = getBierNodeById(TOPOLOGY_ID, nodeId);
            if (null != bierNode) {
                boolean flag = deleteOneBpFromNode(TOPOLOGY_ID,domainId,subDomainId,bsl,si,nodeId,tpId);
                if (!flag) {
                    LOG.info("Auto delete bp failed");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public TeBsl checkAndGetOneBsl(Integer subdomainId) {
        TeBslBuilder teBslBuilder = new TeBslBuilder();
        teBslBuilder.setKey(new TeBslKey(Bsl._64Bit));
        teBslBuilder.setBitstringlength(Bsl._64Bit);
        if (null == bslUsedInSubdomain.get(subdomainId)) {
            List<Integer> bslList = new ArrayList<>();
            bslList.add(64);
            bslUsedInSubdomain.put(subdomainId,bslList);
        }
        return teBslBuilder.build();
    }

    @Override
    public TeSi checkAndGetOneSi(Integer subdomainId, TeBsl teBsl) {
        int si = 1;
        List<Integer> siUsedByManual = getUsedSiInSubdomainBsl(TOPOLOGY_ID, subdomainId,
                teBsl.getBitstringlength().getIntValue());
        if (null == siUsedByManual) {
            return null;
        }
        SubdomainBsl subdomainBsl = new SubdomainBsl(subdomainId, teBsl);
        List<Integer> usedSiList = siUsedInSubdomainBsl.get(subdomainBsl);
        if (null == usedSiList) {
            usedSiList = new ArrayList<>();
            siUsedInSubdomainBsl.put(subdomainBsl,usedSiList);
        }

        while (siUsedByManual.contains(si) || usedSiList.contains(si)) {
            si++;
        }

        int bslValue = subdomainBsl.getTeBsl().getBitstringlength().getIntValue() * 64;

        if (si > 65535 / bslValue) {
            LOG.info("Si in Bsl:" + bslValue + "has been used up");
            return null;
        }
        if (!addUsedSubdomainBslSi(TOPOLOGY_ID, subdomainId, teBsl.getBitstringlength().getIntValue(), si)) {
            return null;
        }
        usedSiList.add(si);
        TeSiBuilder teSiBuilder = new TeSiBuilder();
        teSiBuilder.setKey(new TeSiKey(new Si(si)));
        teSiBuilder.setSi(new Si(si));
        return teSiBuilder.build();
    }

    @Override
    public TeBp checkAndGetOneBP(Integer subdomainId, TeBsl teBsl, TeSi teSi) {
        int bp;
        SubdomainBslSi subdomainBslSi = new SubdomainBslSi(subdomainId,teBsl,teSi);
        List<Integer> bpListInBslSi = bpUsedInSubdomainBslSi.get(subdomainBslSi);
        if (null == bpListInBslSi) {
            bpListInBslSi = new ArrayList<>();
            bpUsedInSubdomainBslSi.put(subdomainBslSi,bpListInBslSi);
        }
        if (bpListInBslSi.size() + 1 > 64) {
            LOG.info("Length of single tePath overflow");
            return null;
        }
        bp = bpListInBslSi.size() + 1;
        bpListInBslSi.add(bp);

        TeBpBuilder teBpBuilder = new TeBpBuilder();
        teBpBuilder.setBitposition(bp);
        return teBpBuilder.build();
    }

    @Override
    public boolean recycleBslSiBp(Integer subdomainId, TeBsl teBsl, TeSi teSi, TeBp teBp) {
        SubdomainBsl subdomainBsl = new SubdomainBsl(subdomainId, teBsl);
        Integer bslValue = teBsl.getBitstringlength().getIntValue();
        Integer siValue = teSi.getSi().getValue();
        SubdomainBslSi subdomainBslSi = new SubdomainBslSi(subdomainId, teBsl, teSi);
        bpUsedInSubdomainBslSi.remove(subdomainBslSi);
        List<Integer> siUsedList = siUsedInSubdomainBsl.get(subdomainBsl);
        siUsedList.remove(siValue);
        return deleteUsedSubdomainBslSi(TOPOLOGY_ID, subdomainId, bslValue, siValue);
    }

    public static PathMonopolyBPAllocateStrategy getInstance() {
        return instance;
    }
}
