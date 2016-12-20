/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver;

import  org.opendaylight.bier.adapter.api.BierConfigResult;
import  org.opendaylight.bier.adapter.api.BierConfigResult.ConfigurationResult;
import  org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.driver.configuration.BierConfigDataBuilder;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierConfiguration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;


import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev161020.Routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierConfigWriterImpl implements BierConfigWriter {

    private static final Logger LOG = LoggerFactory.getLogger(BierConfigWriterImpl.class);
    private static final BierConfigResult RESULT_SUCCESS = new BierConfigResult(ConfigurationResult.SUCCESSFUL);


    public BierConfigResult writeDomain(ConfigurationType type, String nodeId, Domain domain) {
        LOG.info("configurations write to node {}: bier configuration - bier global domain {}",
                nodeId,domain);

        // Now ietf-bier.yang doesn't support multiple domains,
        // domain configuration will be replaced
        return NetconfDataOperator.write(NetconfDataOperator.OperateType.REPLACE, nodeId,
                NetconfDataOperator.ROUTING_IID,
                BierConfigDataBuilder.build(type,  domain));


    }

    public DataBroker getDataBroker(String nodeId,BierConfigResult ncResult) {

        MountPoint mountPoint = NetconfDataOperator.getMountPoint(nodeId);
        if ( mountPoint == null ) {

            LOG.error(BierConfigResult.MOUNT_POINT_FAILUE);
            ncResult.setFailureReason(BierConfigResult.MOUNT_POINT_FAILUE);
            return null;
        }
        return mountPoint.getService(DataBroker.class).get();

    }

    public BierConfiguration getBierConfiguration(String nodeId,BierConfigResult ncResult,DataBroker dataBroker) {

        Routing routing = NetconfDataOperator.read(dataBroker,
                NetconfDataOperator.ROUTING_IID);
        if (routing == null) {
            LOG.info(BierConfigResult.READ_ROUTING_FAIL);
            ncResult.setFailureReason(BierConfigResult.READ_ROUTING_FAIL);
            return null;
        }

        return routing.getAugmentation(BierConfiguration.class);
    }

    public BierConfigResult writeSubdomain(ConfigurationType type, String nodeId,
                                           DomainId domainId, SubDomain subDomain) {
        //Subdomain info is related to NodeID and DomainID.
        // Now domain id is ignored for ietf-bier.yang doesn't support multiple domains.


        if ( type == ConfigurationType.DELETE ) {
            BierConfigResult ncResult = new BierConfigResult(ConfigurationResult.SUCCESSFUL);
            DataBroker dataBroker = getDataBroker(nodeId,ncResult);
            if (dataBroker == null) {
                return ncResult;
            }

            LOG.info("configurations write to node {}: delete - sub-domain {}",
                    nodeId,subDomain);
            BierConfiguration bierConfiguration = getBierConfiguration(nodeId,ncResult,dataBroker);
            if (bierConfiguration == null) {
                return ncResult;
            }
            //replace BierGlobal--subdomain
            return NetconfDataOperator.operate(NetconfDataOperator.OperateType.REPLACE,
                    dataBroker,NetconfDataOperator.RETRY_WRITE_MAX,
                    NetconfDataOperator.ROUTING_IID,
                    BierConfigDataBuilder.build(type,subDomain,bierConfiguration));

        }
        LOG.info("configurations write to node {}: - sub-domain {}",
                nodeId,subDomain);
        return NetconfDataOperator.write(NetconfDataOperator.OperateType.MERGE, nodeId,
                NetconfDataOperator.ROUTING_IID,
                BierConfigDataBuilder.build(type,subDomain));

    }


    public BierConfigResult writeSubdomainIpv4(ConfigurationType type, String nodeId,
                                               DomainId domainId, SubDomainId subDomainId,
                                               Ipv4 ipv4) {
        if (type == ConfigurationType.DELETE ) {
            BierConfigResult ncResult = new BierConfigResult(ConfigurationResult.SUCCESSFUL);
            DataBroker dataBroker = getDataBroker(nodeId,ncResult);
            if (dataBroker == null) {
                return ncResult;
            }
            LOG.info("configurations write to node {}: delete - sub-domain {} ipv4 {}",
                    nodeId,subDomainId,ipv4);
            BierConfiguration bierConfiguration = getBierConfiguration(nodeId,ncResult,dataBroker);
            if (bierConfiguration == null) {
                return ncResult;
            }
            return NetconfDataOperator.operate(NetconfDataOperator.OperateType.REPLACE,
                    dataBroker,NetconfDataOperator.RETRY_WRITE_MAX,
                    NetconfDataOperator.ROUTING_IID,
                    BierConfigDataBuilder.build(type,subDomainId,ipv4,bierConfiguration));

        }

        LOG.info("configurations write to node {}: sub-domain {} ipv4 {}",
                nodeId,subDomainId,ipv4);
        return NetconfDataOperator.write(NetconfDataOperator.OperateType.MERGE,
                nodeId,
                NetconfDataOperator.ROUTING_IID,
                BierConfigDataBuilder.build(type,subDomainId,ipv4));



    }

    public BierConfigResult writeSubdomainIpv6(ConfigurationType type, String nodeId,
                                               DomainId domainId, SubDomainId subDomainId,
                                               Ipv6 ipv6) {


        if (type == ConfigurationType.DELETE ) {
            BierConfigResult ncResult = new BierConfigResult(ConfigurationResult.SUCCESSFUL);
            DataBroker dataBroker = getDataBroker(nodeId,ncResult);
            if (dataBroker == null) {
                return ncResult;
            }

            LOG.info("configurations write to node {}: delete - sub-domain {} ipv6 {}",
                    nodeId,subDomainId,ipv6);
            BierConfiguration bierConfiguration = getBierConfiguration(nodeId, ncResult,dataBroker);
            if (bierConfiguration == null) {
                return ncResult;
            }

            return NetconfDataOperator.operate(NetconfDataOperator.OperateType.REPLACE,
                    dataBroker, NetconfDataOperator.RETRY_WRITE_MAX,
                    NetconfDataOperator.ROUTING_IID,
                    BierConfigDataBuilder.build(type, subDomainId, ipv6, bierConfiguration));

        }
        LOG.info("configurations write to node {}: sub-domain {} ipv6 {}",
                nodeId,subDomainId,ipv6);
        return NetconfDataOperator.write(NetconfDataOperator.OperateType.MERGE,
                nodeId,
                NetconfDataOperator.ROUTING_IID,
                BierConfigDataBuilder.build(type,subDomainId,ipv6));

    }


}
