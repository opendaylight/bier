/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl.teconfig;

import com.google.common.base.Optional;
import org.opendaylight.bierman.impl.BierDataManager;
import org.opendaylight.bierman.impl.RpcUtil;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.BierBpAllocateParams;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.BpAllocateModel;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.bier.bp.allocate.params.TopoBpAllocateParams;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.bier.bp.allocate.params.TopoBpAllocateParamsKey;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.SubdomainBpAllocate;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.SubdomainBpAllocateKey;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.subdomain.bp.allocate.BslBpAllocate;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.subdomain.bp.allocate.BslBpAllocateKey;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.subdomain.bp.allocate.bsl.bp.allocate.SiBpAllocate;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.subdomain.bp.allocate.bsl.bp.allocate.SiBpAllocateBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.subdomain.bp.allocate.bsl.bp.allocate.SiBpAllocateKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BpAssignmentStrategy;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.BierTeFrrConfigApiService;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.ConfigureTeFrrOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.ConfigureTeFrrInput;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.ConfigureTeFrrOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.DeleteTeFrrOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.DeleteTeFrrInput;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.DeleteTeFrrOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.QueryLinkTeInfoInput;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.QueryLinkTeInfoOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.QueryLinkTeInfoOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.TeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.TeDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.te.domain.TeSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.te.domain.TeSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.te.domain.te.sub.domain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.te.domain.te.sub.domain.TeBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.te.domain.te.sub.domain.te.bsl.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.link.te.info.te.domain.te.sub.domain.te.bsl.TeSiKey;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.TeFrrConfigure;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.TopologyTeFrr;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.TopologyTeFrrKey;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.LinkTeFrr;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.LinkTeFrrBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.LinkTeFrrKey;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.TeFrrDomain;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.TeFrrDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.TeFrrDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.TeFrrSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.TeFrrSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.TeFrrSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.te.frr.sub.domain.TeFrrBsl;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.te.frr.sub.domain.TeFrrBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.te.frr.sub.domain.TeFrrBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.te.frr.sub.domain.te.frr.bsl.TeFrrSi;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.te.frr.sub.domain.te.frr.bsl.TeFrrSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.te.frr.sub.domain.te.frr.bsl.TeFrrSiKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDest;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSource;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BierTeFrrConfigServiceImpl implements BierTeFrrConfigApiService {
    private static final Logger LOG = LoggerFactory.getLogger(BierTeFrrConfigServiceImpl.class);
    private DataBroker dataBroker;
    private BierDataManager topoManager;

    public BierTeFrrConfigServiceImpl(DataBroker dataBroker, BierDataManager topoManager) {
        this.dataBroker = dataBroker;
        this.topoManager = topoManager;
    }

    public Future<RpcResult<ConfigureTeFrrOutput>> configureTeFrr(ConfigureTeFrrInput input) {
        LOG.info("ConfigureTeFrrInput" + input);
        ConfigureTeFrrOutputBuilder builder = new ConfigureTeFrrOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false, "input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        String linkId = input.getLinkId();
        DomainId domainId = input.getDomain();
        SubDomainId subDomainId = input.getSubDomain();
        Bsl bsl = input.getBsl();
        Si si = input.getSi();
        BitString bitString = input.getTeBitposition();
        String frrErrorCause = checkTeFrrConfigInput(topologyId, linkId, domainId, subDomainId, bsl, si, bitString,
                input.getLinkSource(), input.getLinkDest());
        if (!frrErrorCause.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false, frrErrorCause));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        boolean result = setBierTeFrrSiData(topologyId, linkId, domainId, subDomainId, bsl, si, bitString);
        if (!result) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false, "Set frr data failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();

    }

    public Future<RpcResult<DeleteTeFrrOutput>> deleteTeFrr(DeleteTeFrrInput input) {
        LOG.info("DeleteTeFrrInput" + input);
        DeleteTeFrrOutputBuilder builder = new DeleteTeFrrOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false, "input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        String linkId = input.getLinkId();
        DomainId domainId = input.getDomain();
        SubDomainId subDomainId = input.getSubDomain();
        Bsl bsl = input.getBsl();
        Si si = input.getSi();
        BitString bitString = input.getTeBitposition();
        String frrErrorCause = checkTeFrrConfigInput(topologyId, linkId, domainId, subDomainId, bsl, si, bitString,
                input.getLinkSource(), input.getLinkDest());
        if (!frrErrorCause.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false, frrErrorCause));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!isBierTeFrrInfoExisted(topologyId, linkId, domainId, subDomainId, bsl, si, bitString)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false, "Frr data is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!delBierTeFrrSiData(topologyId, linkId, domainId, subDomainId, bsl, si)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false, "Del frr data failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<QueryLinkTeInfoOutput>> queryLinkTeInfo(QueryLinkTeInfoInput input) {
        LOG.info("queryLinkTeInfo" + input);
        QueryLinkTeInfoOutputBuilder builder = new QueryLinkTeInfoOutputBuilder();
        if (null == input) {
            LOG.info("input is null!");
            return null;
        }
        String topologyId = input.getTopologyId();
        String linkId = input.getLinkId();
        LinkSource linkSource = input.getLinkSource();
        LinkDest linkDest = input.getLinkDest();
        String frrErrorCause = checkQueryTeFrrInput(topologyId, linkId, linkSource, linkDest);
        if (!frrErrorCause.equals("")) {
            LOG.info("Input params error!");
            return null;
        }

        List<TeDomain> teDomainList = getBierTeDomainList(topologyId, linkId, linkDest);
        if (null == teDomainList || teDomainList.isEmpty()) {
            LOG.info("Te domain data is not exist!");
            return null;
        }
        builder.setTeDomain(teDomainList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }


    private <T> String checkTeFrrConfigInput(String topologyId, String linkId, DomainId domainId,
                                             SubDomainId subDomainId, Bsl bsl, Si si, BitString bitString,
                                             LinkSource linkSource, LinkDest linkDest) {

        if (null == topologyId || topologyId.equals("") || null == linkId || linkId.equals("")
                || null == domainId || null == subDomainId || null == bsl
                || null == si || null == bitString) {
            return ("input param is error!");
        }

        BierLink bierLink = topoManager.getLinkData(topologyId, linkId);
        if (null == bierLink) {
            return ("Link is not exist!");
        }

        if (!bierLink.getLinkSource().equals(linkSource) || !bierLink.getLinkDest().equals(linkDest)) {
            return ("Link info is error!");
        }

        BierDomain bierDomain = topoManager.getDomainData(topologyId, domainId);
        if (null == bierDomain) {
            return ("Domain is not exist!");
        }

        BierNode srcNode = topoManager.getNodeData(topologyId, linkSource.getSourceNode());
        if (null == srcNode) {
            return ("linkSource is not exist!");
        }

        BierNode dstNode = topoManager.getNodeData(topologyId, linkDest.getDestNode());
        if (null == dstNode) {
            return ("linkDest is not exist!");
        }

        if (!topoManager.checkNodeBelongToTeDomain(domainId, subDomainId, dstNode)) {
            return ("linkDest is not belong to domain and subDomain!");
        }

        if (!topoManager.checkBitstringlengthExist(topologyId, domainId, subDomainId, bsl, dstNode)) {
            return ("bsl is not belong to domain and subDomain!");
        }

        if (!topoManager.checkSiExist(topologyId, domainId, subDomainId, bsl, si, dstNode)) {
            return ("si is not belong to domain and subDomain!");
        }

        return "";
    }

    private <T> String checkQueryTeFrrInput(String topologyId, String linkId, LinkSource linkSource, LinkDest linkDest) {


        if (topologyId == null || topologyId.equals("") || null == linkId || linkId.equals("")
                || null == linkSource || null == linkDest) {
            return ("input param is error!");
        }

        BierLink bierLink = topoManager.getLinkData(topologyId, linkId);
        if (null == bierLink) {
            return ("Link is not exist!");
        }

        if (!bierLink.getLinkSource().equals(linkSource) || !bierLink.getLinkDest().equals(linkDest)) {
            return ("Link info is error!");
        }

        return "";
    }

    private boolean setBierTeFrrSiData(String topologyId, String linkId, DomainId domainId,
                                           SubDomainId subDomainId, Bsl bsl, Si si, BitString bitString) {

        InstanceIdentifier<TeFrrSi> teFrrPath = InstanceIdentifier.create(TeFrrConfigure.class)
                .child(TopologyTeFrr.class, new TopologyTeFrrKey(topologyId))
                .child(LinkTeFrr.class, new LinkTeFrrKey(linkId))
                .child(TeFrrDomain.class, new TeFrrDomainKey(domainId))
                .child(TeFrrSubDomain.class, new TeFrrSubDomainKey(subDomainId))
                .child(TeFrrBsl.class, new TeFrrBslKey(bsl))
                .child(TeFrrSi.class, new TeFrrSiKey(si));

        TeFrrSiBuilder teFrrSiBuilder = new TeFrrSiBuilder();
        teFrrSiBuilder.setSi(si);
        teFrrSiBuilder.withKey(new TeFrrSiKey(si));
        teFrrSiBuilder.setTeFrrBp(bitString);
        BpAssignmentStrategy bpAssignmentStrategy = getBpAssignmentStrategy(topologyId, subDomainId, bsl, si);
        if (null != bpAssignmentStrategy) {
            teFrrSiBuilder.setAssignmentStrategy(bpAssignmentStrategy);
        }

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.put(LogicalDatastoreType.CONFIGURATION, teFrrPath, teFrrSiBuilder.build(), true);

        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Write datastore failed!");
            return false;
        }
        return true;
    }

    private boolean isBierTeFrrInfoExisted(String topologyId, String linkId, DomainId domainId,
                                           SubDomainId subDomainId, Bsl bsl, Si si, BitString bitString) {
        InstanceIdentifier<TeFrrSi> teFrrPath = InstanceIdentifier.create(TeFrrConfigure.class)
                .child(TopologyTeFrr.class, new TopologyTeFrrKey(topologyId))
                .child(LinkTeFrr.class, new LinkTeFrrKey(linkId))
                .child(TeFrrDomain.class, new TeFrrDomainKey(domainId))
                .child(TeFrrSubDomain.class, new TeFrrSubDomainKey(subDomainId))
                .child(TeFrrBsl.class, new TeFrrBslKey(bsl))
                .child(TeFrrSi.class, new TeFrrSiKey(si));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();

        TeFrrSi teFrrSi;

        try {
            Optional<TeFrrSi> optinal = tx.read(LogicalDatastoreType.CONFIGURATION, teFrrPath)
                    .checkedGet();
            if (optinal.isPresent()) {
                teFrrSi = optinal.get();
            } else {
                TeFrrSiBuilder builder = new TeFrrSiBuilder();
                teFrrSi = builder.build();
            }
        } catch (ReadFailedException e) {
            LOG.info("Read frr data failed!");
            return false;
        }
        if (null == teFrrSi) {
            LOG.info("Read frr data failed!");
            return false;
        }
        if (null == teFrrSi.getTeFrrBp()) {
            LOG.info("Bp is not existed!");
            return false;
        }
        if (!teFrrSi.getTeFrrBp().equals(bitString)) {
            LOG.info("Bp is not existed!");
            return false;
        }
        return true;
    }

    private boolean delBierTeFrrSiData(String topologyId, String linkId, DomainId domainId,
                                           SubDomainId subDomainId, Bsl bsl, Si si) {
        InstanceIdentifier<TeFrrSi> teFrrPath = InstanceIdentifier.create(TeFrrConfigure.class)
                .child(TopologyTeFrr.class, new TopologyTeFrrKey(topologyId))
                .child(LinkTeFrr.class, new LinkTeFrrKey(linkId))
                .child(TeFrrDomain.class, new TeFrrDomainKey(domainId))
                .child(TeFrrSubDomain.class, new TeFrrSubDomainKey(subDomainId))
                .child(TeFrrBsl.class, new TeFrrBslKey(bsl))
                .child(TeFrrSi.class, new TeFrrSiKey(si));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.delete(LogicalDatastoreType.CONFIGURATION, teFrrPath);

        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Delete Si Frr datastore failed!");
            return false;
        }
        if (!isBierTeFrrSiExisted(topologyId,linkId,domainId,subDomainId,bsl)) {
            if (!delBierTeFrrBslData(topologyId,linkId,domainId,subDomainId,bsl)) {
                LOG.error("Delete Bsl Frr datastore failed!");
                return false;
            }
        }
        return true;
    }

    private boolean delBierTeFrrBslData(String topologyId, String linkId, DomainId domainId,
                                                           SubDomainId subDomainId, Bsl bsl) {
        InstanceIdentifier<TeFrrBsl> teFrrPath = InstanceIdentifier.create(TeFrrConfigure.class)
                .child(TopologyTeFrr.class, new TopologyTeFrrKey(topologyId))
                .child(LinkTeFrr.class, new LinkTeFrrKey(linkId))
                .child(TeFrrDomain.class, new TeFrrDomainKey(domainId))
                .child(TeFrrSubDomain.class, new TeFrrSubDomainKey(subDomainId))
                .child(TeFrrBsl.class, new TeFrrBslKey(bsl));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.delete(LogicalDatastoreType.CONFIGURATION, teFrrPath);

        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Delete Bsl Frr datastore failed!");
            return false;
        }

        if (!isBierTeFrrBslExisted(topologyId,linkId,domainId,subDomainId)) {
            if (!delBierTeFrrSubDomainData(topologyId,linkId,domainId,subDomainId)) {
                LOG.error("Delete SubDomain Frr datastore failed!");
                return false;
            }
        }
        return true;
    }

    private boolean delBierTeFrrSubDomainData(String topologyId, String linkId, DomainId domainId,
                                        SubDomainId subDomainId) {
        InstanceIdentifier<TeFrrSubDomain> teFrrPath = InstanceIdentifier.create(TeFrrConfigure.class)
                .child(TopologyTeFrr.class, new TopologyTeFrrKey(topologyId))
                .child(LinkTeFrr.class, new LinkTeFrrKey(linkId))
                .child(TeFrrDomain.class, new TeFrrDomainKey(domainId))
                .child(TeFrrSubDomain.class, new TeFrrSubDomainKey(subDomainId));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.delete(LogicalDatastoreType.CONFIGURATION, teFrrPath);

        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Delete SubDomain Frr datastore failed!");
            return false;
        }

        if (!isBierTeFrrSubDomainExisted(topologyId,linkId,domainId)) {
            if (!delBierTeFrrDomainData(topologyId,linkId,domainId)) {
                LOG.error("Delete Domain Frr datastore failed!");
                return false;
            }
        }
        return true;
    }

    private boolean delBierTeFrrDomainData(String topologyId, String linkId, DomainId domainId) {
        InstanceIdentifier<TeFrrDomain> teFrrPath = InstanceIdentifier.create(TeFrrConfigure.class)
                .child(TopologyTeFrr.class, new TopologyTeFrrKey(topologyId))
                .child(LinkTeFrr.class, new LinkTeFrrKey(linkId))
                .child(TeFrrDomain.class, new TeFrrDomainKey(domainId));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.delete(LogicalDatastoreType.CONFIGURATION, teFrrPath);

        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Delete Domain Frr datastore failed!");
            return false;
        }
        if (!isBierTeFrrDomainExisted(topologyId,linkId)) {
            if (!delBierTeFrrLinkData(topologyId,linkId)) {
                LOG.error("Delete Link Frr datastore failed!");
                return false;
            }
        }
        return true;
    }

    private boolean delBierTeFrrLinkData(String topologyId, String linkId) {
        InstanceIdentifier<LinkTeFrr> teFrrPath = InstanceIdentifier.create(TeFrrConfigure.class)
                .child(TopologyTeFrr.class, new TopologyTeFrrKey(topologyId))
                .child(LinkTeFrr.class, new LinkTeFrrKey(linkId));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.delete(LogicalDatastoreType.CONFIGURATION, teFrrPath);

        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Delete Bsl Frr datastore failed!");
            return false;
        }
        return true;
    }



    private boolean isBierTeFrrSiExisted(String topologyId, String linkId, DomainId domainId,
                                         SubDomainId subDomainId, Bsl bsl) {
        InstanceIdentifier<TeFrrBsl> teFrrPath = InstanceIdentifier.create(TeFrrConfigure.class)
                .child(TopologyTeFrr.class, new TopologyTeFrrKey(topologyId))
                .child(LinkTeFrr.class, new LinkTeFrrKey(linkId))
                .child(TeFrrDomain.class, new TeFrrDomainKey(domainId))
                .child(TeFrrSubDomain.class, new TeFrrSubDomainKey(subDomainId))
                .child(TeFrrBsl.class, new TeFrrBslKey(bsl));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();

        TeFrrBsl teFrr;

        try {
            Optional<TeFrrBsl> optinal = tx.read(LogicalDatastoreType.CONFIGURATION, teFrrPath)
                    .checkedGet();
            if (optinal.isPresent()) {
                teFrr = optinal.get();
            } else {
                TeFrrBslBuilder builder = new TeFrrBslBuilder();
                teFrr = builder.build();
            }
        } catch (ReadFailedException e) {
            LOG.info("Read frr data failed!");
            return false;
        }
        if (null == teFrr) {
            LOG.info("Read frr data failed!");
            return false;
        }
        if (null == teFrr.getTeFrrSi() || teFrr.getTeFrrSi().isEmpty()) {
            LOG.info("Si is not existed!");
            return false;
        }
        return true;
    }

    private boolean isBierTeFrrBslExisted(String topologyId, String linkId, DomainId domainId,SubDomainId subDomainId) {
        InstanceIdentifier<TeFrrSubDomain> teFrrPath = InstanceIdentifier.create(TeFrrConfigure.class)
                .child(TopologyTeFrr.class, new TopologyTeFrrKey(topologyId))
                .child(LinkTeFrr.class, new LinkTeFrrKey(linkId))
                .child(TeFrrDomain.class, new TeFrrDomainKey(domainId))
                .child(TeFrrSubDomain.class, new TeFrrSubDomainKey(subDomainId));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();

        TeFrrSubDomain teFrr;

        try {
            Optional<TeFrrSubDomain> optinal = tx.read(LogicalDatastoreType.CONFIGURATION, teFrrPath)
                    .checkedGet();
            if (optinal.isPresent()) {
                teFrr = optinal.get();
            } else {
                TeFrrSubDomainBuilder builder = new TeFrrSubDomainBuilder();
                teFrr = builder.build();
            }
        } catch (ReadFailedException e) {
            LOG.info("Read frr data failed!");
            return false;
        }
        if (null == teFrr) {
            LOG.info("Read frr data failed!");
            return false;
        }
        if (null == teFrr.getTeFrrBsl() || teFrr.getTeFrrBsl().isEmpty()) {
            LOG.info("Bsl is not existed!");
            return false;
        }
        return true;
    }

    private boolean isBierTeFrrSubDomainExisted(String topologyId, String linkId, DomainId domainId) {
        InstanceIdentifier<TeFrrDomain> teFrrPath = InstanceIdentifier.create(TeFrrConfigure.class)
                .child(TopologyTeFrr.class, new TopologyTeFrrKey(topologyId))
                .child(LinkTeFrr.class, new LinkTeFrrKey(linkId))
                .child(TeFrrDomain.class, new TeFrrDomainKey(domainId));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();

        TeFrrDomain teFrr;

        try {
            Optional<TeFrrDomain> optinal = tx.read(LogicalDatastoreType.CONFIGURATION, teFrrPath)
                    .checkedGet();
            if (optinal.isPresent()) {
                teFrr = optinal.get();
            } else {
                TeFrrDomainBuilder builder = new TeFrrDomainBuilder();
                teFrr = builder.build();
            }
        } catch (ReadFailedException e) {
            LOG.info("Read frr data failed!");
            return false;
        }
        if (null == teFrr) {
            LOG.info("Read frr data failed!");
            return false;
        }
        if (null == teFrr.getTeFrrSubDomain() || teFrr.getTeFrrSubDomain().isEmpty()) {
            LOG.info("Subdomain is not existed!");
            return false;
        }
        return true;
    }

    private boolean isBierTeFrrDomainExisted(String topologyId, String linkId) {
        InstanceIdentifier<LinkTeFrr> teFrrPath = InstanceIdentifier.create(TeFrrConfigure.class)
                .child(TopologyTeFrr.class, new TopologyTeFrrKey(topologyId))
                .child(LinkTeFrr.class, new LinkTeFrrKey(linkId));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();

        LinkTeFrr teFrr;

        try {
            Optional<LinkTeFrr> optinal = tx.read(LogicalDatastoreType.CONFIGURATION, teFrrPath)
                    .checkedGet();
            if (optinal.isPresent()) {
                teFrr = optinal.get();
            } else {
                LinkTeFrrBuilder builder = new LinkTeFrrBuilder();
                teFrr = builder.build();
            }
        } catch (ReadFailedException e) {
            LOG.info("Read frr data failed!");
            return false;
        }
        if (null == teFrr) {
            LOG.info("Read frr data failed!");
            return false;
        }
        if (null == teFrr.getTeFrrDomain() || teFrr.getTeFrrDomain().isEmpty()) {
            LOG.info("Domain is not existed!");
            return false;
        }
        return true;
    }



    private List<TeDomain> getBierTeDomainList(String topologyId, String linkId, LinkDest linkDest) {

        BierNode dstNode = topoManager.getNodeData(topologyId, linkDest.getDestNode());
        if (null == dstNode) {
            LOG.error("dstNode is not exist!");
            return null;
        }

        List<org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain>
                teDomainListTopo = dstNode.getBierTeNodeParams().getTeDomain();
        if (null == teDomainListTopo || teDomainListTopo.isEmpty()) {
            LOG.error("teDomainListTopo is not exist!");
            return null;
        }

        List<TeDomain> teDomainList = new ArrayList<>();
        for (org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain
                teDomainTopo : teDomainListTopo) {

            TeDomainBuilder teDomainBuilder = new TeDomainBuilder();
            teDomainBuilder.setDomainId(teDomainTopo.getDomainId());
            teDomainBuilder.withKey(new TeDomainKey(teDomainTopo.getDomainId()));

            List<TeSubDomain> teSubDomainList = new ArrayList<>();
            for (org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain
                    teSubDomainTopo : teDomainTopo.getTeSubDomain()) {
                TeSubDomainBuilder teSubDomainBuilder = new TeSubDomainBuilder();
                teSubDomainBuilder.setSubDomainId(teSubDomainTopo.getSubDomainId());
                teSubDomainBuilder.withKey(new TeSubDomainKey(teSubDomainTopo.getSubDomainId()));

                List<TeBsl> bslList = new ArrayList<>();
                for (org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl
                        teBslTopo : teSubDomainTopo.getTeBsl()) {
                    TeBslBuilder teBslBuilder = new TeBslBuilder();
                    teBslBuilder.setBitstringlength(teBslTopo.getBitstringlength());
                    teBslBuilder.withKey(new TeBslKey(teBslTopo.getBitstringlength()));

                    List<TeSi> teSiList = constructTeSi(topologyId,linkId,linkDest,teDomainTopo,teSubDomainTopo,teBslTopo);
                    if (null != teSiList && !teSiList.isEmpty()) {
                        teBslBuilder.setTeSi(teSiList);
                    }
                    bslList.add(teBslBuilder.build());
                }

                teSubDomainBuilder.setTeBsl(bslList);
                teSubDomainList.add(teSubDomainBuilder.build());
            }
            teDomainBuilder.setTeSubDomain(teSubDomainList);
            teDomainList.add(teDomainBuilder.build());
        }
        return teDomainList;
    }

    private BpAssignmentStrategy getBpAssignmentStrategy(String topologyId, SubDomainId subDomainId, Bsl bsl, Si si) {

        InstanceIdentifier<SiBpAllocate> siBpAllocatePath = InstanceIdentifier.create(BierBpAllocateParams.class)
                .child(TopoBpAllocateParams.class, new TopoBpAllocateParamsKey(topologyId))
                .child(SubdomainBpAllocate.class, new SubdomainBpAllocateKey(subDomainId))
                .child(BslBpAllocate.class, new BslBpAllocateKey(bsl))
                .child(SiBpAllocate.class, new SiBpAllocateKey(si));
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        SiBpAllocate siBpAllocate;
        try {
            Optional<SiBpAllocate> optinal = tx.read(LogicalDatastoreType.CONFIGURATION, siBpAllocatePath)
                    .checkedGet();
            if (optinal.isPresent()) {
                siBpAllocate = optinal.get();
            } else {
                SiBpAllocateBuilder builder = new SiBpAllocateBuilder();
                siBpAllocate = builder.build();
            }
        } catch (ReadFailedException e) {
            LOG.info("Read siBpAllocate failed!");
            return null;
        }

        if (null == siBpAllocate) {
            LOG.info("Read siBpAllocate failed!");
            return null;
        }

        if (siBpAllocate.getAllocateModel().equals(BpAllocateModel.AllocateModel.AUTO)) {
            return BpAssignmentStrategy.Automatic;
        } else if (siBpAllocate.getAllocateModel().equals(BpAllocateModel.AllocateModel.MANUAL)) {
            return BpAssignmentStrategy.Manual;
        }
        return null;
    }

    private List<TeSi> constructTeSi(String topologyId, String linkId, LinkDest linkDest,
                                     org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain teDomainTopo,
                                     org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain teSubDomainTopo,
                                     org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl teBslTopo) {
        List<TeSi> teSiList = new ArrayList<>();
        for (org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi
                teSiTopo : teBslTopo.getTeSi()) {
            TeSiBuilder teSiBuilder = new TeSiBuilder();
            teSiBuilder.setSi(teSiTopo.getSi());
            teSiBuilder.withKey(new TeSiKey(teSiTopo.getSi()));
            if (null == teSiTopo.getTeBp()) {
                LOG.info("teBpTopo is not existed!");
                return null;
            }
            for (org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp
                    teBpTopo : teSiTopo.getTeBp()) {
                if (teBpTopo.getTpId().equals(linkDest.getDestTp())) {
                    teSiBuilder.setTeBitposition(new BitString(teBpTopo.getBitposition()));
                    if (isBierTeFrrInfoExisted(topologyId, linkId, teDomainTopo.getDomainId(),
                            teSubDomainTopo.getSubDomainId(), teBslTopo.getBitstringlength(),
                            teSiTopo.getSi(), new BitString(teBpTopo.getBitposition()))) {
                        teSiBuilder.setTeFrr(Boolean.TRUE);
                    } else {
                        teSiBuilder.setTeFrr(Boolean.FALSE);
                    }
                }
            }
            teSiList.add(teSiBuilder.build());
        }
        return teSiList;
    }






}
