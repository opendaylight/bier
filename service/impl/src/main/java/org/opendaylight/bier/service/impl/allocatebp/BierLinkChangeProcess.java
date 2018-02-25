/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.service.impl.allocatebp;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.BierTopologyApiListener;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkAdd;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkChange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkRemove;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.TopoChange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.link.add.AddLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.link.remove.RemoveLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BierLinkChangeProcess implements BierTopologyApiListener {

    private static final Logger LOG = LoggerFactory.getLogger(BierLinkChangeProcess.class);
    private static final String TOPOLOGY_ID = "example-linkstate-topology";
    private TopoBasedBpAllocateStrategy bpAllocateStrategy = TopoBasedBpAllocateStrategy.getInstance();
    private static final int ADD_LINK = 1;
    private static final int REMOVE_LINK = 2;

    @Override
    public void onTopoChange(TopoChange notification) {

    }

    @Override
    public void onLinkAdd(LinkAdd notification) {
        AddLink addLink = notification.getAddLink();
        LOG.info("LinkAdd: srcNode {} dstNode {}.", addLink.getLinkSource().getSourceNode(), addLink.getLinkDest()
                .getDestNode());
        BierLinkBuilder bierLinkBuilder = new BierLinkBuilder();
        bierLinkBuilder.setKey(new BierLinkKey(addLink.getLinkId()));
        bierLinkBuilder.setLinkId(addLink.getLinkId());
        bierLinkBuilder.setLinkSource(addLink.getLinkSource());
        bierLinkBuilder.setLinkDest(addLink.getLinkDest());
        BierLink bierLink = bierLinkBuilder.build();
        addOrRemoveLink(bierLink, bierLink.getLinkSource().getSourceNode(),
                bierLink.getLinkDest().getDestNode(), ADD_LINK);
    }

    @Override
    public void onLinkRemove(LinkRemove notification) {
        RemoveLink removeLink = notification.getRemoveLink();
        LOG.info("LinkRemove: srcNode {} dstNode {}.", removeLink.getLinkSource().getSourceNode(), removeLink
                .getLinkDest().getDestNode());
        BierLinkBuilder bierLinkBuilder = new BierLinkBuilder();
        bierLinkBuilder.setKey(new BierLinkKey(removeLink.getLinkId()));
        bierLinkBuilder.setLinkId(removeLink.getLinkId());
        bierLinkBuilder.setLinkSource(removeLink.getLinkSource());
        bierLinkBuilder.setLinkDest(removeLink.getLinkDest());
        BierLink bierLink = bierLinkBuilder.build();
        addOrRemoveLink(bierLink, bierLink.getLinkSource().getSourceNode(),
                bierLink.getLinkDest().getDestNode(), REMOVE_LINK);
    }

    @Override
    public void onLinkChange(LinkChange notification) {

    }

    private void addOrRemoveLink(BierLink bierLink, String srcNode, String dstNode, int type) {

        BierNode srcBierNode = bpAllocateStrategy.getBierNodeById(TOPOLOGY_ID, srcNode);
        BierNode dstBierNode = bpAllocateStrategy.getBierNodeById(TOPOLOGY_ID, dstNode);

        BierTeNodeParams srcBierTeNodeParams = srcBierNode.getBierTeNodeParams();
        BierTeNodeParams dstBierTeNodeParams = dstBierNode.getBierTeNodeParams();

        if (null == srcBierTeNodeParams || null == srcBierTeNodeParams.getTeDomain() || srcBierTeNodeParams
                .getTeDomain().isEmpty() || null == dstBierTeNodeParams || null == dstBierTeNodeParams
                .getTeDomain() || dstBierTeNodeParams.getTeDomain().isEmpty()) {
            return;
        }

        List<TeSubDomain> srcSubdomainList = srcBierTeNodeParams.getTeDomain().get(0).getTeSubDomain();
        List<TeSubDomain> dstSubdomainList = dstBierTeNodeParams.getTeDomain().get(0).getTeSubDomain();

        List<SubDomainId> dstSubdomainIdList = new ArrayList<>();
        for (TeSubDomain dstSubdomain:dstSubdomainList) {
            dstSubdomainIdList.add(dstSubdomain.getSubDomainId());
        }

        for (TeSubDomain teSubDomain:srcSubdomainList) {
            if (dstSubdomainIdList.contains(teSubDomain.getSubDomainId()) && bpAllocateStrategy
                    .isSudomainHasDeployedChannel(teSubDomain.getSubDomainId().getValue())) {
                LOG.info("Channel has been deployed in the subdomain {}.", teSubDomain.getSubDomainId());

                List<BierLink> tmpBierLinkChanged;
                if (type == ADD_LINK) {
                    tmpBierLinkChanged = bpAllocateStrategy.tmpBierLinkAdd.get(teSubDomain.getSubDomainId().getValue());
                } else {
                    tmpBierLinkChanged = bpAllocateStrategy.tmpBierLinkDelete.get(teSubDomain
                            .getSubDomainId().getValue());
                }
                if (!isReverseLinkExist(bierLink, tmpBierLinkChanged)) {
                    tmpBierLinkChanged.add(bierLink);
                    LOG.info("The reverse direction bierlink is not online.");
                    continue;
                }

                List<BierLink> linkList = new ArrayList<>();
                linkList.add(bierLink);
                TeDomain teDomain = srcBierNode.getBierTeNodeParams().getTeDomain().get(0);
                TeBsl teBsl = bpAllocateStrategy.checkAndGetOneBsl(teSubDomain.getSubDomainId().getValue());
                TeSi teSi = bpAllocateStrategy.checkAndGetOneSi(teSubDomain.getSubDomainId().getValue(),teBsl);

                if (type == ADD_LINK && isBpOfConnectedLinkAdded(bierLink, teDomain, teSubDomain, teBsl, teSi)) {
                    LOG.info("BinLinkChangeProcess: Controller starting up!");
                    return;
                }

                if (type == ADD_LINK && !bpAllocateStrategy.allocateBPToBierTopology(TOPOLOGY_ID, teDomain,
                        teSubDomain, teBsl, teSi, linkList, null, null)) {
                    LOG.info("Allocate bp to new bierlink failed!");
                    return;

                } else if (type == REMOVE_LINK && !bpAllocateStrategy.allocateBPToBierTopology(TOPOLOGY_ID, teDomain,
                        teSubDomain, teBsl, teSi, null, linkList, null)) {
                    LOG.info("Recycle bp of removed bierlink failed!");
                    return;
                }
            }
        }
    }

    private boolean isReverseLinkExist(BierLink changedLink, List<BierLink> tmpBierLinkChanged) {
        for (BierLink bierLink:tmpBierLinkChanged) {
            if (changedLink.getLinkDest().getDestNode().equals(bierLink.getLinkSource().getSourceNode())
                    && changedLink.getLinkSource().getSourceNode().equals(bierLink.getLinkDest().getDestNode())) {
                tmpBierLinkChanged.remove(bierLink);
                LOG.info("Reverse bierlink exist.");
                return true;
            }
        }
        return false;
    }

    private boolean isBpOfConnectedLinkAdded(BierLink bierLink, TeDomain teDomain, TeSubDomain teSubDomain,
                                             TeBsl teBsl, TeSi teSi) {
        BierTopology bierTopology = bpAllocateStrategy.getBierTopology(TOPOLOGY_ID);
        if (null == bierTopology || null == bierTopology.getBierNode()) {
            return false;
        }
        List<BierNode> bierNodeList = bierTopology.getBierNode();
        TeBp srcTeBp = null;
        TeBp dstTeBp = null;
        for (BierNode bierNode:bierNodeList) {
            if (bierNode.getNodeId().equals(bierLink.getLinkSource().getSourceNode())) {
                srcTeBp = bpAllocateStrategy.getTeBp(TOPOLOGY_ID, bierNode.getNodeId(), teDomain.getDomainId(),
                        teSubDomain.getSubDomainId(), teBsl.getBitstringlength(), teSi.getSi(),
                        bierLink.getLinkSource().getSourceTp());
            }
            if (bierNode.getNodeId().equals(bierLink.getLinkDest().getDestNode())) {
                dstTeBp = bpAllocateStrategy.getTeBp(TOPOLOGY_ID, bierNode.getNodeId(), teDomain.getDomainId(),
                        teSubDomain.getSubDomainId(), teBsl.getBitstringlength(), teSi.getSi(),
                        bierLink.getLinkDest().getDestTp());
            }
        }
        if (null != srcTeBp && null != dstTeBp && srcTeBp.getBitposition().equals(dstTeBp.getBitposition())) {
            return true;
        }
        return false;
    }


}
