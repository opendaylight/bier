/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;

import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4Key;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierParametersConfigProcess {

    private static final Logger LOG = LoggerFactory.getLogger(BierParametersConfigProcess.class);

    private BierConfigWriter bierConfigWriter;
    private NotificationProvider notificationProvider;

    public BierParametersConfigProcess(BierConfigWriter bierConfig) {
        bierConfigWriter = bierConfig;
        notificationProvider = new NotificationProvider();
    }

    public void processDomain(String nodeId, List<Domain> domainBefore,List<Domain> domainAfter) {
        if (null == nodeId || (null == domainBefore && null == domainAfter)) {
            return;
        }
        List<Domain> domainChange = getChangeDomainList(domainBefore, domainAfter);
        if (checkAddedDomain(domainBefore,domainAfter)) {
            LOG.info("Add Domain Parameters!");
            processAddedDomain(nodeId, domainChange);
        } else if (checkDeletedDomain(domainBefore,domainAfter)) {
            LOG.info("Del Domain Parameters!");
            processDeletedDomain(nodeId, domainChange);
        } else if (checkModifiedDomain(domainBefore,domainAfter)) {
            LOG.info("Modify Domain Parameters!");
            processModifiedDomain(nodeId, domainBefore,domainAfter);
        }
    }

    public void processAddedDomain(String nodeId, List<Domain> domainList) {
        if (null == nodeId || null == domainList || domainList.isEmpty()) {
            return;
        }
        for (Domain domain : domainList) {
            if (!processAddedSingleDomain(nodeId,domain)) {
                LOG.error("Add Domain to SBI failed!");
                return;
            }
        }
    }

    public void processDeletedDomain(String nodeId, List<Domain> domainList) {
        if (null == nodeId || null == domainList || domainList.isEmpty()) {
            return;
        }
        for (Domain domain : domainList) {
            if (!processDeletedSingleDomain(nodeId,domain)) {
                LOG.error("Del Domain to SBI failed!");
                return;
            }
        }
    }

    private void processModifiedDomain(String nodeId, List<Domain> domainBefore, List<Domain> domainAfter) {
        if (null == nodeId || null == domainBefore || domainBefore.isEmpty()
            || null == domainAfter || domainAfter.isEmpty()) {
            return;
        }
        for (int i = 0; i < domainAfter.size(); i++) {
            Domain before = getDomainById(domainAfter.get(i).getDomainId(), domainBefore);
            if (null == before) {
                LOG.error("Can not find that Domain!");
                return;
            }
            boolean isDomainParaChange = checkLeafOfDomainChange(before, domainAfter.get(i));
            if (isDomainParaChange) {
                if (!processModifiedSingleDomain(nodeId, domainAfter.get(i))) {
                    LOG.error("Modify Domain to SBI failed!");
                    return;
                }
            }
            processSubDomain(nodeId, before.getDomainId(), isDomainParaChange,before.getBierGlobal().getSubDomain(),
                domainAfter.get(i).getBierGlobal().getSubDomain());
        }
    }

    private boolean processAddedSingleDomain(String nodeId, Domain domain) {
        if (null == nodeId || null == domain) {
            return false;
        }
        ConfigurationResult writedDomainResult = bierConfigWriter.writeDomain(
                ConfigurationType.ADD, nodeId, domain);
        if (!writedDomainResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(writedDomainResult.getFailureReason());
            return false;
        }
        return true;
    }

    private boolean processDeletedSingleDomain(String nodeId, Domain domain) {
        if (null == nodeId || null == domain) {
            return false;
        }
        ConfigurationResult writedDomainResult = bierConfigWriter.writeDomain(
                ConfigurationType.DELETE, nodeId, domain);
        if (!writedDomainResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(writedDomainResult.getFailureReason());
            return false;
        }
        return true;
    }

    private boolean processModifiedSingleDomain(String nodeId, Domain domain) {
        if (null == nodeId || null == domain) {
            return false;
        }
        ConfigurationResult writedDomainResult = bierConfigWriter.writeDomain(
                ConfigurationType.MODIFY, nodeId, domain);
        if (!writedDomainResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(writedDomainResult.getFailureReason());
            return false;
        }
        return true;
    }

    private boolean processSubDomain(String nodeId, DomainId domainId, boolean isDomainParaChange,
                 List<SubDomain> subDomainlistBefore, List<SubDomain> subDomainlistAfter) {
        if (null == nodeId || null == domainId) {
            return false;
        }
        List<SubDomain> subDomainChange = getSubDomainChangeList(subDomainlistBefore, subDomainlistAfter);
        if (checkAddedSubDomain(subDomainlistBefore,subDomainlistAfter,isDomainParaChange)) {
            LOG.info("Add SubDomain Parameters!");
            return processAddedSubDomain(nodeId,domainId, subDomainChange);
        } else if (checkDeletedSubDomain(subDomainlistBefore,subDomainlistAfter)) {
            LOG.info("Del SubDomain Parameters!");
            return processDeletedSubDomain(nodeId, domainId,subDomainChange);
        } else if (checkModifiedSubDomain(subDomainlistBefore,subDomainlistAfter,isDomainParaChange)) {
            LOG.info("Modify SubDomain Parameters!");
            return processModifiedSubDomain(nodeId, domainId,subDomainlistBefore,subDomainlistAfter);
        }
        return true;
    }

    private boolean processAddedSubDomain(String nodeId, DomainId domainId, List<SubDomain> subDomainList) {
        if (null == nodeId || null == domainId
                || null == subDomainList || subDomainList.isEmpty()) {
            return false;
        }
        for (SubDomain subDomain : subDomainList) {
            if (!processAddedSingleSubDomain(nodeId,domainId,subDomain)) {
                LOG.error("Add SubDomain to SBI failed!");
                return false;
            }
        }
        return true;
    }

    private boolean processDeletedSubDomain(String nodeId, DomainId domainId, List<SubDomain> subDomainList) {
        if (null == nodeId || null == domainId
                || null == subDomainList || subDomainList.isEmpty()) {
            return false;
        }
        for (SubDomain subDomain : subDomainList) {
            if (!processDeletedSingleSubDomain(nodeId,domainId,subDomain)) {
                LOG.error("Del SubDomain to SBI failed!");
                return false;
            }
        }
        return true;
    }

    private boolean processModifiedSubDomain(String nodeId, DomainId domainId,
            List<SubDomain> subDomainlistBefore, List<SubDomain> subDomainlistAfter) {
        if (null == nodeId || null == domainId
            || null == subDomainlistBefore || subDomainlistBefore.isEmpty()
            || null == subDomainlistAfter || subDomainlistAfter.isEmpty()) {
            return false;
        }
        for (int i = 0; i < subDomainlistAfter.size(); i++) {
            SubDomain before = getSubDomainById(subDomainlistAfter.get(i).getSubDomainId(), subDomainlistBefore);
            if (null == before) {
                LOG.error("Can not find that SubDomain!");
                return false;
            }
            if (checkSubDomainChange(before, subDomainlistAfter.get(i))) {
                LOG.info("process modify single subdomain");
                if (!processModifiedSingleSubDomain(nodeId, domainId, subDomainlistAfter.get(i))) {
                    return false;
                }
            }
            if (null != before.getAf() && null != subDomainlistAfter.get(i).getAf()) {
                LOG.info("process delete ipv4ipv6");
                if (!processDeletedIpv4Ipv6(nodeId, domainId,
                        before, subDomainlistAfter.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean processAddedSingleSubDomain(String nodeId, DomainId domainId, SubDomain subDomain) {
        if (null == nodeId || null == domainId || null == subDomain) {
            return false;
        }
        ConfigurationResult subDomainResult = bierConfigWriter
                .writeSubdomain(ConfigurationType.ADD,nodeId,domainId,subDomain);
        if (!subDomainResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(subDomainResult.getFailureReason());
            return false;
        }
        return true;
    }

    private boolean processDeletedSingleSubDomain(String nodeId, DomainId domainId, SubDomain subDomain) {
        if (null == nodeId || null == domainId || null == subDomain) {
            return false;
        }
        ConfigurationResult subDomainResult = bierConfigWriter
                .writeSubdomain(ConfigurationType.DELETE,nodeId,domainId,subDomain);
        if (!subDomainResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(subDomainResult.getFailureReason());
            return false;
        }
        return true;
    }

    private boolean processModifiedSingleSubDomain(String nodeId, DomainId domainId, SubDomain subDomain) {
        if (null == nodeId || null == domainId || null == subDomain) {
            return false;
        }
        LOG.info("send modify subdomain to netconf");
        ConfigurationResult subDomainResult = bierConfigWriter
                .writeSubdomain(ConfigurationType.MODIFY,nodeId,domainId,subDomain);
        if (!subDomainResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(subDomainResult.getFailureReason());
            return false;
        }
        return true;
    }

    private boolean processDeletedIpv4Ipv6(String nodeId, DomainId domainId,
                SubDomain subDomainlistBefore, SubDomain subDomainlistAfter) {
        List<Ipv4> ipv4Before = subDomainlistBefore.getAf().getIpv4();
        List<Ipv4> ipv4After = subDomainlistAfter.getAf().getIpv4();
        List<Ipv4> ipv4Change = getIpv4DeletedChangeList(ipv4Before,ipv4After);
        if (checkDeletedIpv4(ipv4Before,ipv4After)) {
            LOG.info("Del Ipv4 Parameters!");
            if (!processDeletedIpv4(nodeId, domainId, subDomainlistBefore.getSubDomainId(), ipv4Change)) {
                LOG.error("Del Ipv4 to SBI failed!");
                return false;
            }
        }
        List<Ipv6> ipv6Before = subDomainlistBefore.getAf().getIpv6();
        List<Ipv6> ipv6After = subDomainlistAfter.getAf().getIpv6();
        List<Ipv6> ipv6Change = getIpv6DeletedChangeList(ipv6Before,ipv6After);
        if (checkDeletedIpv6(ipv6Before,ipv6After)) {
            LOG.info("Del Ipv6 Parameters!");
            if (!processDeletedIpv6(nodeId, domainId, subDomainlistBefore.getSubDomainId(), ipv6Change)) {
                LOG.error("Del Ipv6 to SBI failed!");
                return false;
            }
        }
        return true;
    }


    private boolean processDeletedIpv4(String nodeId, DomainId domainId,
            SubDomainId subDomainId, List<Ipv4> ipv4Change) {
        if (null == nodeId || null == domainId || null == subDomainId
                || null == ipv4Change || ipv4Change.isEmpty()) {
            return false;
        }
        for (Ipv4 ipv4 : ipv4Change) {
            ConfigurationResult ipv4Result = bierConfigWriter
                    .writeSubdomainIpv4(ConfigurationType.DELETE, nodeId, domainId,
                    subDomainId,ipv4);
            if (!ipv4Result.isSuccessful()) {
                notificationProvider.notifyFailureReason(ipv4Result.getFailureReason());
                return false;
            }
        }
        return true;
    }

    private boolean processDeletedIpv6(String nodeId, DomainId domainId,
          SubDomainId subDomainId, List<Ipv6> ipv6Change) {
        if (null == nodeId || null == domainId || null == subDomainId
                || null == ipv6Change || ipv6Change.isEmpty()) {
            return false;
        }
        for (Ipv6 ipv6 : ipv6Change) {
            ConfigurationResult ipv6Result = bierConfigWriter
                    .writeSubdomainIpv6(ConfigurationType.DELETE, nodeId, domainId,
                        subDomainId,ipv6);
            if (!ipv6Result.isSuccessful()) {
                notificationProvider.notifyFailureReason(ipv6Result.getFailureReason());
                return false;
            }
        }
        return true;
    }

    private List<Domain> getChangeDomainList(List<Domain> domainBefore, List<Domain> domainAfter) {
        if (null == domainBefore && null == domainAfter) {
            return null;
        }
        if (null == domainBefore && null != domainAfter) {
            return domainAfter;
        } else if (null != domainBefore && null == domainAfter) {
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


    private List<SubDomain> getSubDomainChangeList(List<SubDomain> subdomainBefore, List<SubDomain> subdomainAfter) {
        if (null == subdomainBefore && null == subdomainAfter) {
            return null;
        }
        if (null == subdomainBefore && null != subdomainAfter) {
            return subdomainAfter;
        } else if (null != subdomainBefore && null == subdomainAfter) {
            return subdomainBefore;
        }
        List<SubDomain> subdomainChange = new ArrayList<>();
        if (subdomainBefore.size() > subdomainAfter.size()) {
            for (SubDomain subDomain : subdomainBefore) {
                if (null == getSubDomainById(subDomain.getSubDomainId(), subdomainAfter)) {
                    subdomainChange.add(subDomain);
                }
            }
        } else if (subdomainBefore.size() < subdomainAfter.size()) {
            for (SubDomain subDomain : subdomainAfter) {
                if (null == getSubDomainById(subDomain.getSubDomainId(), subdomainBefore)) {
                    subdomainChange.add(subDomain);
                }
            }
        }
        return subdomainChange;
    }

    private List<Ipv4> getIpv4DeletedChangeList(List<Ipv4> ipv4Before, List<Ipv4> ipv4After) {
        if ((null == ipv4Before && null == ipv4After) || (null == ipv4Before && null != ipv4After)) {
            return null;
        }
        if (null != ipv4Before && null == ipv4After) {
            return ipv4Before;
        }
        List<Ipv4> ipv4Change = new ArrayList<>();
        if (ipv4Before.size() > ipv4After.size()) {
            for (Ipv4 ipv4 : ipv4Before) {
                if (null == getIpv4ById(ipv4.getKey(), ipv4After)) {
                    ipv4Change.add(ipv4);
                }
            }
        }
        return ipv4Change;
    }

    private List<Ipv6> getIpv6DeletedChangeList(List<Ipv6> ipv6Before, List<Ipv6> ipv6After) {
        if ((null == ipv6Before && null == ipv6After) || (null == ipv6Before && null != ipv6After)) {
            return null;
        }
        if (null != ipv6Before && null == ipv6After) {
            return ipv6Before;
        }
        List<Ipv6> ipv6Change = new ArrayList<>();
        if (ipv6Before.size() > ipv6After.size()) {
            for (Ipv6 ipv6 : ipv6Before) {
                if (null == getIpv6ById(ipv6.getKey(), ipv6After)) {
                    ipv6Change.add(ipv6);
                }
            }
        }
        return ipv6Change;
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

    private Ipv4 getIpv4ById(Ipv4Key key, List<Ipv4> ipv4List) {
        if (null == key || null == ipv4List || ipv4List.isEmpty()) {
            return null;
        }
        for (Ipv4 ipv4 : ipv4List) {
            if (ipv4.getKey().equals(key)) {
                return ipv4;
            }
        }
        return null;
    }

    private Ipv6 getIpv6ById(Ipv6Key key, List<Ipv6> ipv6List) {
        if (null == key || null == ipv6List || ipv6List.isEmpty()) {
            return null;
        }
        for (Ipv6 ipv6 : ipv6List) {
            if (ipv6.getKey().equals(key)) {
                return ipv6;
            }
        }
        return null;
    }

    private boolean checkLeafOfDomainChange(Domain before, Domain after) {
        if (checkDomainIdChange(before, after) || checkEncapsulationType(before, after)
                || checkBitStringLengthChange(before, after) || checkBfrIdChange(before, after)
                || checkIpv4BfrPrefixChange(before, after) || checkIpv6BfrPrefixChange(before, after)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkSubDomainChange(SubDomain before, SubDomain after) {

        if (checkSubDomainIdChange(before, after) || checkIgpTypeChange(before, after)
                || checkMtIdChange(before, after) || checkSubDomainBfrIdChange(before, after)
                || checkSubDomainBitStringLengthChange(before, after)) {
            return true;
        }
        if (null != before.getAf() && null != after.getAf()) {
            if ((null == before.getAf().getIpv4() && null != after.getAf().getIpv4())
                    || (null == before.getAf().getIpv6() && null != after.getAf().getIpv6())) {
                return true;
            }
            if (null != before.getAf().getIpv4() && null != after.getAf().getIpv4()) {
                if (before.getAf().getIpv4().size() < after.getAf().getIpv4().size()) {
                    return true;
                }
                if (before.getAf().getIpv4().size() == after.getAf().getIpv4().size()) {
                    for (int i = 0; i < before.getAf().getIpv4().size(); i++) {
                        if (!before.getAf().getIpv4().get(i).equals(after.getAf().getIpv4().get(i))) {
                            return true;
                        }
                    }
                }
            }
            if (null != before.getAf().getIpv6() && null != after.getAf().getIpv6()) {
                if (before.getAf().getIpv6().size() < after.getAf().getIpv6().size()) {
                    return true;
                }
                if (before.getAf().getIpv6().size() == after.getAf().getIpv6().size()) {
                    for (int i = 0; i < before.getAf().getIpv6().size(); i++) {
                        if (!before.getAf().getIpv6().get(i).equals(after.getAf().getIpv6().get(i))) {
                            return true;
                        }
                    }
                }
            }
        } else if (null == before.getAf() || null == after.getAf()) {
            if (null == before.getAf() && null != after.getAf()) {
                LOG.info("add af");
                return true;
            } else if (null != before.getAf() && null == after.getAf()) {
                LOG.info("delete af");
                return true;
            } else {
                LOG.info("no content in af,only contain af change");
                return true;
            }
        }
        LOG.info("no change");
        return false;
    }

    private boolean checkAddedDomain(List<Domain> domainBefore, List<Domain> domainAfter) {
        if (null == domainBefore && null != domainAfter) {
            return true;
        }
        if (null != domainBefore && null != domainAfter && domainBefore.size() < domainAfter.size()) {
            return true;
        }
        return false;
    }

    private boolean checkDeletedDomain(List<Domain> domainBefore, List<Domain> domainAfter) {
        if (null != domainBefore && null == domainAfter) {
            return true;
        }
        if (null != domainBefore && null != domainAfter && domainBefore.size() > domainAfter.size()) {
            return true;
        }
        return false;
    }

    private boolean checkModifiedDomain(List<Domain> domainBefore, List<Domain> domainAfter) {
        if (null != domainBefore && null != domainAfter && domainBefore.size() == domainAfter.size()) {
            return true;
        }
        return false;
    }

    private boolean checkDomainIdChange(Domain before, Domain after) {
        if (after.getDomainId().equals(before.getDomainId())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkEncapsulationType(Domain before, Domain after) {
        if (null != before.getBierGlobal().getEncapsulationType()
                && null != after.getBierGlobal().getEncapsulationType()) {
            if (after.getBierGlobal().getEncapsulationType().equals(before.getBierGlobal().getEncapsulationType())) {
                return false;
            } else {
                return true;
            }
        } else if ((null == before.getBierGlobal().getEncapsulationType())
                && (null == after.getBierGlobal().getEncapsulationType())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkBitStringLengthChange(Domain before, Domain after) {
        if (null != before.getBierGlobal().getBitstringlength() && null != after.getBierGlobal().getBitstringlength()) {
            if (after.getBierGlobal().getBitstringlength().equals(before.getBierGlobal().getBitstringlength())) {
                return false;
            } else {
                return true;
            }
        } else if ((null == before.getBierGlobal().getBitstringlength())
                && (null == after.getBierGlobal().getBitstringlength())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkBfrIdChange(Domain before, Domain after) {
        if (null != before.getBierGlobal().getBfrId() && null != after.getBierGlobal().getBfrId()) {
            if (after.getBierGlobal().getBfrId().equals(before.getBierGlobal().getBfrId())) {
                return false;
            } else {
                return true;
            }
        } else if ((null == before.getBierGlobal().getBfrId()) && (null == after.getBierGlobal().getBfrId())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkIpv4BfrPrefixChange(Domain before, Domain after) {
        if (null != before.getBierGlobal().getIpv4BfrPrefix() && null != after.getBierGlobal().getIpv4BfrPrefix()) {
            if (after.getBierGlobal().getIpv4BfrPrefix().equals(before.getBierGlobal().getIpv4BfrPrefix())) {
                return false;
            } else {
                return true;
            }
        } else if ((null == before.getBierGlobal().getIpv4BfrPrefix())
                && (null == after.getBierGlobal().getIpv4BfrPrefix())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkIpv6BfrPrefixChange(Domain before, Domain after) {
        if (null != before.getBierGlobal().getIpv6BfrPrefix() && null != after.getBierGlobal().getIpv6BfrPrefix()) {
            if (after.getBierGlobal().getIpv6BfrPrefix().equals(before.getBierGlobal().getIpv6BfrPrefix())) {
                return false;
            } else {
                return true;
            }
        } else if ((null == before.getBierGlobal().getIpv6BfrPrefix())
                && (null == after.getBierGlobal().getIpv6BfrPrefix())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkSubDomainIdChange(SubDomain before, SubDomain after) {
        if (after.getSubDomainId().equals(before.getSubDomainId())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkIgpTypeChange(SubDomain before, SubDomain after) {
        if (null != before.getIgpType() && null != after.getIgpType()) {
            if (after.getIgpType().equals(before.getIgpType())) {
                return false;
            } else {
                return true;
            }
        } else if ((null == before.getIgpType())
                && (null == after.getIgpType())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkMtIdChange(SubDomain before, SubDomain after) {
        if (null != before.getMtId() && null != after.getMtId()) {
            if (after.getMtId().equals(before.getMtId())) {
                return false;
            } else {
                return true;
            }
        } else if ((null == before.getMtId())
                && (null == after.getMtId())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkSubDomainBfrIdChange(SubDomain before, SubDomain after) {
        if (null != before.getBfrId() && null != after.getBfrId()) {
            if (after.getBfrId().equals(before.getBfrId())) {
                return false;
            } else {
                return true;
            }
        } else if ((null == before.getBfrId())
                && (null == after.getBfrId())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkSubDomainBitStringLengthChange(SubDomain before, SubDomain after) {
        if (null != before.getBitstringlength() && null != after.getBitstringlength()) {
            if (after.getBitstringlength().equals(before.getBitstringlength())) {
                return false;
            } else {
                return true;
            }
        } else if ((null == before.getBitstringlength())
                && (null == after.getBitstringlength())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkAddedSubDomain(List<SubDomain> subDomainlistBefore,
        List<SubDomain> subDomainlistAfter,boolean isDomainParaChange) {
        if (null == subDomainlistBefore && null != subDomainlistAfter && !isDomainParaChange) {
            return true;
        }
        if (null != subDomainlistBefore && null != subDomainlistAfter
               && subDomainlistBefore.size() < subDomainlistAfter.size() && !isDomainParaChange) {
            return true;
        }
        return false;
    }

    private boolean checkDeletedSubDomain(List<SubDomain> subDomainlistBefore, List<SubDomain> subDomainlistAfter) {
        if (null != subDomainlistBefore && null == subDomainlistAfter) {
            return true;
        }
        if (null != subDomainlistBefore && null != subDomainlistAfter
             && subDomainlistBefore.size() > subDomainlistAfter.size()) {
            return true;
        }
        return false;
    }

    private boolean checkModifiedSubDomain(List<SubDomain> subDomainlistBefore,
                 List<SubDomain> subDomainlistAfter,boolean isDomainParaChange) {
        if (null != subDomainlistBefore && null != subDomainlistAfter
              && subDomainlistBefore.size() == subDomainlistAfter.size() && !isDomainParaChange) {
            return true;
        }
        return false;
    }

    private boolean checkDeletedIpv4(List<Ipv4> ipv4Before, List<Ipv4> ipv4After) {
        if (null != ipv4Before && null == ipv4After) {
            return true;
        }
        if (null != ipv4Before && null != ipv4After
                && ipv4Before.size() > ipv4After.size()) {
            return true;
        }
        return false;
    }

    private boolean checkDeletedIpv6(List<Ipv6> ipv6Before, List<Ipv6> ipv6After) {
        if (null != ipv6Before && null == ipv6After) {
            return true;
        }
        if (null != ipv6Before && null != ipv6After
                && ipv6Before.size() > ipv6After.size()) {
            return true;
        }
        return false;
    }

}
