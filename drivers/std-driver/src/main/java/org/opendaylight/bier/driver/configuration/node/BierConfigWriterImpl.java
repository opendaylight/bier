/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.node;


import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;

import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;


import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.Af;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;


import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;



import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierConfigWriterImpl implements BierConfigWriter {

    private static final Logger LOG = LoggerFactory.getLogger(BierConfigWriterImpl.class);
    private static final ConfigurationResult RESULT_SUCCESS =
            new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);

    private NetconfDataOperator netconfDataOperator ;

    public BierConfigWriterImpl(NetconfDataOperator netconfDataOperator) {
        this.netconfDataOperator = netconfDataOperator;
    }


    public ConfigurationResult writeDomain(ConfigurationType type, String nodeId, Domain domain) {
        LOG.info("configurations write to node {}: bier configuration - bier global domain {}",
                nodeId,domain);


        if (type == ConfigurationType.DELETE) {
            LOG.info("configurations delete  bier configuration - nodeid {}", nodeId);
            return netconfDataOperator.write(
                    NetconfDataOperator.OperateType.DELETE,
                    nodeId,
                    NetconfDataOperator.BIER_GLOBAL_IID,
                    null);
        }
        // Now ietf-bier.yang doesn't support multiple domains,
        // domain configuration will be replaced
        LOG.info("configurations write  bier global - nodeid {}, {}", nodeId,domain);
        return netconfDataOperator.write(
                NetconfDataOperator.OperateType.REPLACE,
                nodeId,
                NetconfDataOperator.BIER_GLOBAL_IID,
                domain.getBierGlobal());


    }

    public InstanceIdentifier<SubDomain> getSubDomainIId(SubDomainId subDomainId) {
        return NetconfDataOperator.BIER_GLOBAL_IID.child(SubDomain.class,
                new SubDomainKey(subDomainId));

    }

    public ConfigurationResult writeSubdomain(ConfigurationType type, String nodeId,
                                           DomainId domainId, SubDomain subDomain) {
        //Subdomain info is related to NodeID and DomainID.
        // Now domain id is ignored for ietf-bier.yang doesn't support multiple domains.


        if ( type == ConfigurationType.DELETE ) {

            LOG.info("configurations write to node {}: delete - sub-domain {}",
                    nodeId,subDomain);


            return netconfDataOperator.write(
                    NetconfDataOperator.OperateType.DELETE,
                    nodeId,
                    getSubDomainIId(subDomain.getSubDomainId()),
                    null);

        }
        LOG.info("configurations write to node {}: - sub-domain {}",
                nodeId,subDomain);
        return netconfDataOperator.write(
                NetconfDataOperator.OperateType.MERGE,
                nodeId,
                getSubDomainIId(subDomain.getSubDomainId()),
                subDomain);

    }


    public ConfigurationResult writeSubdomainIpv4(ConfigurationType type, String nodeId,
                                               DomainId domainId, SubDomainId subDomainId,
                                               Ipv4 ipv4) {
        LOG.info("delete  : sub-domain-id {} ipv4 {} node {}",
                subDomainId,ipv4,nodeId);
        InstanceIdentifier<Ipv4> ipv4IId = getSubDomainIId(subDomainId)
                .child(Af.class)
                .child(Ipv4.class, ipv4.getKey());
        if (type == ConfigurationType.DELETE ) {

            return netconfDataOperator.write(
                    NetconfDataOperator.OperateType.DELETE,
                    nodeId,
                    ipv4IId,
                    null);

        }

        LOG.info("configurations write to node {}: sub-domain {} ipv4 {}",
                nodeId,subDomainId,ipv4);
        return netconfDataOperator.write(
                NetconfDataOperator.OperateType.MERGE,
                nodeId,
                ipv4IId,
                ipv4);



    }

    public ConfigurationResult writeSubdomainIpv6(ConfigurationType type, String nodeId,
                                               DomainId domainId, SubDomainId subDomainId,
                                               Ipv6 ipv6) {
        LOG.info("delete  : sub-domain-id {} ipv6 {} node {}", subDomainId,ipv6,nodeId);
        InstanceIdentifier<Ipv6> ipv6IId = getSubDomainIId(subDomainId)
                .child(Af.class)
                .child(Ipv6.class, ipv6.getKey());
        if (type == ConfigurationType.DELETE) {

            return netconfDataOperator.write(
                    NetconfDataOperator.OperateType.DELETE,
                    nodeId,
                    ipv6IId,
                    null);

        }

        LOG.info("configurations write to node {}: sub-domain {} ipv6 {}",
                nodeId, subDomainId, ipv6);
        return netconfDataOperator.write(
                NetconfDataOperator.OperateType.MERGE,
                nodeId,
                ipv6IId,
                ipv6);
    }


}
