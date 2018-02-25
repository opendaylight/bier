/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.service.impl.allocatebp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierNodeTeSubdomainChangeListener implements DataTreeChangeListener<TeSubDomain> {
    private static final Logger LOG = LoggerFactory.getLogger(BierNodeTeSubdomainChangeListener.class);
    private static final String TOPOLOGY_ID = "example-linkstate-topology";
    private TopoBasedBpAllocateStrategy bpAllocateStrategy = TopoBasedBpAllocateStrategy.getInstance();
    private static int ADD_NODE_SUBDOMAIN = 1;
    private static int DELETE_NODE_SUBDOMAIN = 2;

    private  InstanceIdentifier<TeSubDomain>  subdomainIID = InstanceIdentifier.create(BierNetworkTopology.class)
            .child(BierTopology.class, new BierTopologyKey(TOPOLOGY_ID))
            .child(BierNode.class).child(BierTeNodeParams.class)
            .child(TeDomain.class).child(TeSubDomain.class);


    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<TeSubDomain>> changes) {
        for (DataTreeModification<TeSubDomain> change : changes) {
            DataObjectModification<TeSubDomain> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    SubDomainId subDomainId = rootNode.getDataAfter().getSubDomainId();
                    processNodeChanged(change, subDomainId, ADD_NODE_SUBDOMAIN);
                    break;
                case SUBTREE_MODIFIED:
                    break;

                case DELETE:
                    subDomainId = rootNode.getDataBefore().getSubDomainId();
                    processNodeChanged(change, subDomainId, DELETE_NODE_SUBDOMAIN);
                    break;

                default:
                    throw new IllegalArgumentException("Unhandled modification type : {}"
                            + change.getRootNode().getModificationType());
            }
        }
    }

    public InstanceIdentifier<TeSubDomain> getTeSubdomainId() {
        return subdomainIID;
    }



    private void processNodeChanged(DataTreeModification<TeSubDomain> change, SubDomainId subDomainId, int type) {
        DomainId domainId = change.getRootPath().getRootIdentifier().firstKeyOf(TeDomain.class)
                .getDomainId();

        String nodeId = change.getRootPath().getRootIdentifier().firstIdentifierOf(TeDomain.class)
                .firstIdentifierOf(BierTeNodeParams.class).firstKeyOf(BierNode.class).getNodeId();
        TeDomain teDomain = getDomainById(domainId);
        TeSubDomain teSubDomain = getTeSubdomainById(subDomainId);
        LOG.info("New subdomain {} of node {} added.", subDomainId, nodeId);
        if (bpAllocateStrategy.isSudomainHasDeployedChannel(subDomainId.getValue())) {

            if (type == ADD_NODE_SUBDOMAIN && null != bpAllocateStrategy.nodeLocalDecapBpMap.get(
                    subDomainId.getValue()).get(nodeId)) {
                LOG.info("TeSubdomainListener: Controller starting up!");
                return;
            }

            TeBsl teBsl = bpAllocateStrategy.checkAndGetOneBsl(subDomainId.getValue());
            TeSi teSi = bpAllocateStrategy.checkAndGetOneSi(subDomainId.getValue(), teBsl);

            BierNode bierNode = bpAllocateStrategy.getBierNodeById(TOPOLOGY_ID, nodeId);
            List<BierLink> linkList = getBierLinks(bierNode, subDomainId);
            LOG.info("Bierlinks size of node {} is {}.", bierNode.getNodeId(), linkList.size());
            List<BierNode> nodeList = new ArrayList<>();
            nodeList.add(bierNode);
            if (type == ADD_NODE_SUBDOMAIN && !bpAllocateStrategy.allocateBPToBierTopology(TOPOLOGY_ID, teDomain,
                    teSubDomain, teBsl, teSi, linkList, null, nodeList)) {
                LOG.info("Allocate bp for new subdomain failed!");
            } else if (type == DELETE_NODE_SUBDOMAIN && !bpAllocateStrategy.allocateBPToBierTopology(TOPOLOGY_ID,
                    teDomain, teSubDomain, teBsl, teSi, null, linkList, null)) {
                LOG.info("Delete bp of deleted subdomain failed!");
            }
        }
    }

    private List<BierLink> getBierLinks(BierNode bierNode, SubDomainId subdomainId) {
        List<BierLink> bierLinkList = new ArrayList<>();
        for (TerminationPoint terminationPoint:bierNode.getBierTerminationPoint()) {
            String tpId = terminationPoint.getTpId();
            BierLink bierLink = bpAllocateStrategy.queryLinkByNodeIdAndTpId(TOPOLOGY_ID, bierNode.getNodeId(), tpId);
            if (null == bierLink) {
                continue;
            }
            String dstNode = bierLink.getLinkDest().getDestNode();
            BierNode dstBierNode = bpAllocateStrategy.getBierNodeById(TOPOLOGY_ID, dstNode);
            List<SubDomainId> subdomainInDstBierNode = new ArrayList<>();
            if (null != dstBierNode.getBierTeNodeParams() && null != dstBierNode.getBierTeNodeParams().getTeDomain()
                    && !dstBierNode.getBierTeNodeParams().getTeDomain().isEmpty()) {
                for (TeSubDomain teSubDomain:dstBierNode.getBierTeNodeParams().getTeDomain().get(0).getTeSubDomain()) {
                    subdomainInDstBierNode.add(teSubDomain.getSubDomainId());
                }
                if (subdomainInDstBierNode.contains(subdomainId)) {
                    bierLinkList.add(bierLink);
                }
            }
        }
        return bierLinkList;
    }

    private TeDomain getDomainById(DomainId domainId) {
        TeDomainBuilder teDomainBuilder = new TeDomainBuilder();
        teDomainBuilder.setDomainId(domainId);
        teDomainBuilder.setKey(new TeDomainKey(domainId));
        return teDomainBuilder.build();
    }

    private TeSubDomain getTeSubdomainById(SubDomainId subDomainId) {
        TeSubDomainBuilder teSubDomainBuilder = new TeSubDomainBuilder();
        teSubDomainBuilder.setKey(new TeSubDomainKey(subDomainId));
        teSubDomainBuilder.setSubDomainId(subDomainId);
        return teSubDomainBuilder.build();
    }

}
