/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opendaylight.bier.adapter.api.BierConfigResult;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierNodeChangeListener implements DataTreeChangeListener<BierNode> {

    private static final Logger LOG = LoggerFactory.getLogger(BierNodeChangeListener.class);

    private static final InstanceIdentifier<BierNode> BIER_NODE_IID = InstanceIdentifier
            .create(BierNetworkTopology.class)
            .child(BierTopology.class, new BierTopologyKey("flow:1")).child(BierNode.class);

    private final DataBroker dataProvider;
    private BierConfigWriter bierConfigWriter;

    public BierNodeChangeListener(final DataBroker dataBroker, BierConfigWriter bierConfig) {
        dataProvider = dataBroker;
        bierConfigWriter = bierConfig;
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<BierNode>> changes) {
        for (DataTreeModification<BierNode> change : changes) {
            DataObjectModification<BierNode> rootNode = change.getRootNode();
            if (!bierInfoCheck(rootNode.getDataBefore(), rootNode.getDataAfter())) {
                LOG.info("Does not configuration bier info, do nothing");
            } else {
                switch (rootNode.getModificationType()) {
                    case WRITE:
                        LOG.info("onDataTreeChanged - BierNode config with path {} was added or replaced: "
                                        + "old BierNode: {}, new BierNode: {}",
                                change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                                rootNode.getDataAfter());
                        processAddedNode(rootNode.getDataAfter());
                        break;
                    case SUBTREE_MODIFIED:
                        LOG.info("onDataTreeChanged - BierNode config with path {} was modified: "
                                        + "old BierNode: {}, new BierNode: {}",
                                change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                                rootNode.getDataAfter());
                        processModifiedNode(rootNode.getDataBefore(), rootNode.getDataAfter());
                        break;
                    case DELETE:
                        LOG.info("onDataTreeChanged - BierNode config with path {} was deleted: old BierNode: {}",
                                change.getRootPath().getRootIdentifier(), rootNode.getDataBefore());
                        processDeletedNode(rootNode.getDataBefore());
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled modification type : {}"
                                + change.getRootNode().getModificationType());
                }
            }
        }
    }

    public InstanceIdentifier<BierNode> getBierNodeIid() {
        return BIER_NODE_IID;
    }


    public boolean bierInfoCheck(BierNode before, BierNode after) {
        if ((before.getBierNodeParams().getDomain() == null || 0 == before.getBierNodeParams().getDomain().size())
                && (after.getBierNodeParams().getDomain() == null || 0 == after.getBierNodeParams().getDomain()
                .size())) {
            return false;
        } else {
            return true;
        }
    }

    public void processAddedNode(BierNode afterNode) {
        if (null == afterNode) {
            return;
        }
        String nodeId = afterNode.getNodeId();
        processAddedDomain(nodeId, afterNode.getBierNodeParams().getDomain());
    }

    public void processDeletedNode(BierNode beforeNode) {
        if (null == beforeNode) {
            return;
        }
        String nodeId = beforeNode.getNodeId();
        processDeletedDomain(nodeId, beforeNode.getBierNodeParams().getDomain());
    }

    public void processModifiedNode(BierNode beforeNode, BierNode afterNode) {
        if (null == beforeNode || null == afterNode) {
            return;
        }
        String nodeId = beforeNode.getNodeId();
        List<Domain> domainBefore = beforeNode.getBierNodeParams().getDomain();
        List<Domain> domainAfter = afterNode.getBierNodeParams().getDomain();
        List<Domain> domainChange = getChangeDomainList(domainBefore, domainAfter);

        if (null == beforeNode.getBierNodeParams().getDomain()
                && null != afterNode.getBierNodeParams().getDomain()) {
            processAddedDomain(nodeId, domainChange);
        } else if (null == afterNode.getBierNodeParams().getDomain()
                && null != beforeNode.getBierNodeParams().getDomain()) {
            processAddedDomain(nodeId, domainChange);
        } else if (null == afterNode.getBierNodeParams().getDomain()
                && null == beforeNode.getBierNodeParams().getDomain()) {
            return;
        }

        if (afterNode.getBierNodeParams().getDomain().size()
                > beforeNode.getBierNodeParams().getDomain().size()) {
            processAddedDomain(nodeId, domainChange);
        } else if (afterNode.getBierNodeParams().getDomain().size()
                < beforeNode.getBierNodeParams().getDomain().size()) {
            processDeletedDomain(nodeId, domainChange);
        } else {
            for (int i = 0; i < domainAfter.size(); i++) {

                Domain before = getDomainById(domainAfter.get(i).getDomainId(), domainBefore);
                if (null == before) {
                    return;
                }

                boolean isDomainParaChange = checkLeafOfDomainChange(before, domainAfter.get(i));
                if (isDomainParaChange) {
                    boolean resultDomain = processModifiedDomain(nodeId, domainAfter.get(i));
                    if (!resultDomain) {
                        return;
                    }
                }

                List<SubDomain> subDomainlistBefore = before.getBierGlobal().getSubDomain();
                List<SubDomain> subDomainlistAfter = domainAfter.get(i).getBierGlobal().getSubDomain();
                boolean resultSubDomain = processSubDomain(nodeId, before.getDomainId(), isDomainParaChange,
                        subDomainlistBefore, subDomainlistAfter);
                if (!resultSubDomain) {
                    return;
                }
            }
        }
    }


    public boolean processAddedDomain(String nodeId, List<Domain> domainList) {
        if (null == nodeId || null == domainList || 0 == domainList.size()) {
            return false;
        }
        for (Domain domain : domainList) {
            BierConfigResult writedDomainResult = bierConfigWriter.writeDomain(
                    BierConfigWriter.ConfigurationType.ADD, nodeId, domain);
            if (!writedDomainResult.isSuccessful()) {
                //reportToApp(writedDomainResult.getFailureReason());
                return false;
            }
        }
        return true;
    }

    public boolean processDeletedDomain(String nodeId, List<Domain> domainList) {
        if (null == nodeId || null == domainList || 0 == domainList.size()) {
            return false;
        }
        for (Domain domain : domainList) {
            BierConfigResult writedDomainResult = bierConfigWriter.writeDomain(
                    BierConfigWriter.ConfigurationType.DELETE, nodeId, domain);
            if (!writedDomainResult.isSuccessful()) {
                //reportToApp(writedDomainResult.getFailureReason());
                return false;
            }
        }
        return true;
    }

    public boolean processModifiedDomain(String nodeId, Domain domain) {
        if (null == nodeId || null == domain) {
            return false;
        }
        BierConfigResult writedDomainResult = bierConfigWriter.writeDomain(
                BierConfigWriter.ConfigurationType.MODIFY, nodeId, domain);
        if (!writedDomainResult.isSuccessful()) {
            //reportToApp(writedDomainResult.getFailureReason());
            return false;
        }
        return true;
    }

    public boolean processSubDomain(String nodeId, DomainId domainId, boolean isDomainParaChange,
                                    List<SubDomain> subDomainlistBefore, List<SubDomain> subDomainlistAfter) {
        if (null == nodeId || null == domainId || null == subDomainlistBefore || null == subDomainlistAfter
                || 0 == subDomainlistBefore.size() || 0 == subDomainlistAfter.size()) {
            return false;
        }
        boolean result = true;
        List<SubDomain> subDomainChange = getSubDomainChangeList(subDomainlistBefore, subDomainlistAfter);
        if (null == subDomainlistBefore && null != subDomainlistAfter) {
            processAddedSubDomain(nodeId,domainId, subDomainChange);
        } else if (null == subDomainlistBefore && null != subDomainlistAfter) {
            processDeletedSubDomain(nodeId, domainId,subDomainChange);
        } else if (null == subDomainlistBefore && null == subDomainlistAfter) {
            return true;
        }
        if (subDomainlistBefore.size() > subDomainlistAfter.size() && !isDomainParaChange) {
            result = processAddedSubDomain(nodeId, domainId, subDomainChange);
        } else if (subDomainlistBefore.size() < subDomainlistAfter.size()) {
            result = processDeletedSubDomain(nodeId, domainId, subDomainChange);
        } else if (subDomainlistBefore.size() == subDomainlistAfter.size() && !isDomainParaChange) {

            for (int i = 0; i < subDomainlistAfter.size(); i++) {

                SubDomain before = getSubDomainById(subDomainlistAfter.get(i).getSubDomainId(), subDomainlistBefore);
                if (null == before) {
                    return false;
                }

                boolean isSubDomainParaChange = checkSubDomainChange(before, subDomainlistAfter.get(i));
                if (isSubDomainParaChange) {
                    result = processModifiedSubDomain(nodeId, domainId, subDomainlistAfter.get(i));
                    if (!result) {
                        return result;
                    }
                }
                List<Ipv4> ipv4Before = before.getAf().getIpv4();
                List<Ipv4> ipv4After = subDomainlistAfter.get(i).getAf().getIpv4();
                if (ipv4Before.size() > ipv4After.size()) {
                    result = processDeletedIpv4(nodeId, domainId, before.getSubDomainId(), ipv4Before, ipv4After);
                    if (!result) {
                        return result;
                    }
                }

                List<Ipv6> ipv6Before = before.getAf().getIpv6();
                List<Ipv6> ipv6After = subDomainlistAfter.get(i).getAf().getIpv6();
                if (ipv6Before.size() > ipv6After.size()) {
                    result = processDeletedIpv6(nodeId, domainId, before.getSubDomainId(), ipv6Before, ipv6After);
                    if (!result) {
                        return result;
                    }
                }
            }
        }
        return result;
    }


    public boolean processAddedSubDomain(String nodeId, DomainId domainId, List<SubDomain> subDomainList) {
        if (null == nodeId || null == domainId
                || null == subDomainList || 0 == subDomainList.size()) {
            return false;
        }
        for (SubDomain subDomain : subDomainList) {
            BierConfigResult subDomainResult = bierConfigWriter
                    .writeSubdomain(BierConfigWriter.ConfigurationType.ADD,nodeId,domainId,subDomain);
            if (!subDomainResult.isSuccessful()) {
                //reportToApp(SubDomainResult.getFailureReason());
                return false;
            }
        }
        return true;
    }

    public boolean processDeletedSubDomain(String nodeId, DomainId domainId, List<SubDomain> subDomainList) {
        if (null == nodeId || null == domainId
                || null == subDomainList || 0 == subDomainList.size()) {
            return false;
        }
        for (SubDomain subDomain : subDomainList) {
            BierConfigResult subDomainResult = bierConfigWriter
                    .writeSubdomain(BierConfigWriter.ConfigurationType.DELETE,nodeId,domainId,subDomain);
            if (!subDomainResult.isSuccessful()) {
                //reportToApp(SubDomainResult.getFailureReason());
                return false;
            }
        }
        return true;
    }

    public boolean processModifiedSubDomain(String nodeId, DomainId domainId, SubDomain subDomain) {
        if (null == nodeId || null == domainId || null == subDomain) {
            return false;
        }
        BierConfigResult subDomainResult = bierConfigWriter
                .writeSubdomain(BierConfigWriter.ConfigurationType.MODIFY,nodeId,domainId,subDomain);
        if (!subDomainResult.isSuccessful()) {
            //reportToApp(SubDomainResult.getFailureReason());
            return false;
        }
        return true;
    }


    public boolean processDeletedIpv4(String nodeId, DomainId domainId, SubDomainId subDomainId, List<Ipv4> before,
                                      List<Ipv4> after) {
        if (null == nodeId || null == domainId || null == subDomainId
                || null == before || 0 == before.size()) {
            return false;
        }
        for (int i = before.size(); i > after.size(); i--) {
            BierConfigResult ipv4Result = bierConfigWriter
                    .writeSubdomainIpv4(BierConfigWriter.ConfigurationType.DELETE, nodeId, domainId, subDomainId,
                            before.get(i - 1));
            if (!ipv4Result.isSuccessful()) {
                //reportToApp(ipv4Result.getFailureReason());
                return false;
            }
        }
        return true;
    }

    public boolean processDeletedIpv6(String nodeId, DomainId domainId, SubDomainId subDomainId, List<Ipv6> before,
                                      List<Ipv6> after) {
        if (null == nodeId || null == domainId || null == subDomainId
                || null == before || 0 == before.size()) {
            return false;
        }
        for (int i = before.size(); i > after.size(); i--) {
            BierConfigResult ipv6Result = bierConfigWriter
                    .writeSubdomainIpv6(BierConfigWriter.ConfigurationType.DELETE, nodeId, domainId, subDomainId,
                            before.get(i - 1));
            if (!ipv6Result.isSuccessful()) {
                //reportToApp(ipv6Result.getFailureReason());
                return false;
            }
        }
        return true;
    }

    private List<Domain> getChangeDomainList(List<Domain> domainBefore, List<Domain> domainAfter) {
        if (null == domainBefore && null == domainAfter) {
            return null;
        }
        if (null == domainBefore && 0 == domainBefore.size()) {
            return domainAfter;
        } else if (null == domainAfter && 0 == domainAfter.size()) {
            return domainBefore;
        }
        List<Domain> domainChange = new ArrayList<>();
        if (domainBefore.size() > domainAfter.size()) {
            for (Domain domain : domainBefore) {
                if (null == getDomainById(domain.getDomainId(), domainAfter)) {
                    domainChange.add(domain);
                }
            }
        } else if (domainBefore.size() < domainAfter.size()) {
            for (Domain domain : domainAfter) {
                if (null == getDomainById(domain.getDomainId(), domainBefore)) {
                    domainChange.add(domain);
                }
            }
        }
        return domainChange;
    }


    private List<SubDomain> getSubDomainChangeList(List<SubDomain> domainBefore, List<SubDomain> domainAfter) {
        if (null == domainBefore && null == domainAfter) {
            return null;
        }
        if (null == domainBefore && 0 == domainBefore.size()) {
            return domainAfter;
        } else if (null == domainAfter && 0 == domainAfter.size()) {
            return domainBefore;
        }
        List<SubDomain> domainChange = new ArrayList<>();
        if (domainBefore.size() > domainAfter.size()) {
            for (SubDomain subDomain : domainBefore) {
                if (null == getSubDomainById(subDomain.getSubDomainId(), domainAfter)) {
                    domainChange.add(subDomain);
                }
            }
        } else if (domainBefore.size() < domainAfter.size()) {
            for (SubDomain subDomain : domainAfter) {
                if (null == getSubDomainById(subDomain.getSubDomainId(), domainBefore)) {
                    domainChange.add(subDomain);
                }
            }
        }
        return domainChange;
    }

    private Domain getDomainById(DomainId domainId, List<Domain> domainList) {
        if (null == domainId || null == domainList || 0 == domainList.size()) {
            return null;
        }
        for (Domain domain : domainList) {
            if (domain.getDomainId().equals(domainId)) {
                return domain;
            }
        }
        return null;
    }

    private SubDomain getSubDomainById(SubDomainId subDomainId, List<SubDomain> subDomainList) {
        if (null == subDomainId || null == subDomainList || 0 == subDomainList.size()) {
            return null;
        }
        for (SubDomain subDomain : subDomainList) {
            if (subDomain.getSubDomainId().equals(subDomainId)) {
                return subDomain;
            }
        }
        return null;
    }


    public boolean checkLeafOfDomainChange(Domain before, Domain after) {
        if (after.getDomainId().equals(before.getDomainId()) && after.getBierGlobal()
                .getEncapsulationType().equals(before.getBierGlobal().getEncapsulationType())
                && after.getBierGlobal().getBitstringlength().equals(before.getBierGlobal()
                .getBitstringlength()) && after.getBierGlobal().getBfrId().equals(before
                .getBierGlobal().getBfrId()) && after.getBierGlobal().getIpv4BfrPrefix()
                .equals(before.getBierGlobal().getIpv4BfrPrefix()) && after.getBierGlobal()
                .getIpv6BfrPrefix().equals(before.getBierGlobal().getIpv6BfrPrefix())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkSubDomainChange(SubDomain before, SubDomain after) {

        if (!after.getSubDomainId().equals(before.getSubDomainId()) || !after.getIgpType().equals(before.getIgpType())
                || !after.getMtId().equals(before.getMtId()) || !after.getBfrId().equals(before.getBfrId()) || !after
                .getBitstringlength().equals(before.getBitstringlength())) {
            return true;
        }
        if (before.getAf().getIpv4().size() != after.getAf().getIpv4().size()
                || before.getAf().getIpv6().size() != after.getAf().getIpv6().size()) {
            return true;
        }
        for (int i = 0; i < before.getAf().getIpv4().size(); i++) {
            if (before.getAf().getIpv4().get(i).equals(after.getAf().getIpv4().get(i))) {
                return true;
            }
        }
        for (int i = 0; i < before.getAf().getIpv6().size(); i++) {
            if (before.getAf().getIpv6().get(i).equals(after.getAf().getIpv6().get(i))) {
                return true;
            }
        }
        return false;
    }


}
