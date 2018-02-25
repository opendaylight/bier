/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.allocatebp;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.bier.service.impl.NotificationProvider;
import org.opendaylight.bierman.impl.BierDataManager;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.AddSubdomainBslSiInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.AddSubdomainBslSiOutput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.BierBpAllocateParams;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.BierBpAllocateParamsConfigService;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.BpAllocateModel;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.DeleteSubdomainBslSiInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.DeleteSubdomainBslSiOutput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.QuerySubdomainBslSiInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.QuerySubdomainBslSiOutput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.bier.bp.allocate.params.TopoBpAllocateParams;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.bier.bp.allocate.params.TopoBpAllocateParamsKey;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.bier.bp.allocate.params.topo.bp.allocate.params.RecommendBsl;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.query.subdomain.bsl.si.output.SiOfModel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.StrategyDataPersistence;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.StrategyDataPersistenceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.BferListOfChannel;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.BferListOfChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.BpUsedInSubdomainBslSi;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.BpUsedInSubdomainBslSiKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.BslUsedInSubdomain;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.BslUsedInSubdomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.ChannelBferSubdomainBslSiMap;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.ChannelBferSubdomainBslSiMapBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.ChannelBferSubdomainBslSiMapKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.SiUsedInSubdomainBsl;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.SiUsedInSubdomainBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.bfer.list.of.channel.BferOfChannel;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.bfer.list.of.channel.BferOfChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.bfer.list.of.channel.BferOfChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.bfer.list.of.channel.bfer.of.channel.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.bp.used.in.subdomain.bsl.si.BpOfSubdomainBslSi;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.bp.used.in.subdomain.bsl.si.BpOfSubdomainBslSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.bp.used.in.subdomain.bsl.si.BpOfSubdomainBslSiKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.bsl.used.in.subdomain.BslOfSubdomain;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.bsl.used.in.subdomain.BslOfSubdomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.bsl.used.in.subdomain.BslOfSubdomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.si.used.in.subdomain.bsl.SiOfSubdomainBsl;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.si.used.in.subdomain.bsl.SiOfSubdomainBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.si.used.in.subdomain.bsl.SiOfSubdomainBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBPAllocateStrategy implements BPAllocateStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBPAllocateStrategy.class);
    protected static final int ALLOCATE_BPS = 1;
    protected static final int RECYCLE_BPS = 2;
    public static final int ADD = 3;
    public static final int DELETE = 4;
    protected static final String TOPOLOGY_ID = "example-linkstate-topology";
    protected Map<Integer,List<Integer>> bslUsedInSubdomain = new HashMap<>();
    protected Map<SubdomainBsl,List<Integer>> siUsedInSubdomainBsl = new HashMap<>();
    protected Map<SubdomainBslSi,List<Integer>> bpUsedInSubdomainBslSi = new HashMap<>();
    protected Map<String,List<Bfer>> bferListOfChannel = new HashMap<>();
    protected Map<ChannelNameBferNodeId,SubdomainBslSi> channelBferSubdomainBslSiMap = new HashMap<>();
    protected Map<Integer,Bsl> bslValueMap = new HashMap<>();
    protected DataBroker dataBroker;
    protected BierDataManager topoManager;
    protected RpcConsumerRegistry rpcConsumerRegistry;

    public AbstractBPAllocateStrategy() {
        initBslValueMap();
    }

    public AbstractBPAllocateStrategy(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        topoManager = new BierDataManager(dataBroker);
        initBslValueMap();
    }

    protected abstract TeBsl checkAndGetOneBsl(Integer subdomainId);

    protected abstract TeSi checkAndGetOneSi(Integer subdomainId, TeBsl teBsl);

    protected abstract TeBp checkAndGetOneBP(Integer subdomainId, TeBsl teBsl, TeSi teSi);

    protected abstract boolean recycleBslSiBp(Integer subdomainId, TeBsl teBsl, TeSi teSi, TeBp teBp);

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.topoManager = new BierDataManager(dataBroker);
    }

    public void setRpcConsumerRegistry(RpcConsumerRegistry rpcConsumerRegistry) {
        this.rpcConsumerRegistry = rpcConsumerRegistry;
    }

    protected boolean configureOneBPToNode(String topologyId, String nodeId, TeDomain teDomain, TeSubDomain teSubDomain,
                                        TeBsl teBsl, TeSi teSi, TeBp teBp) {
        if (null == teDomain || null == teSubDomain || null == teBsl || null == teSi || null == teBp) {
            LOG.info("Configure node input error!");
            return false;
        }
        LOG.info("Allocate one BP to node {} tpId {}.", nodeId, teBp.getTpId());
        BierNode bierNode = getBierNodeById(TOPOLOGY_ID,nodeId);
        int teSiIndex = topoManager.getTeSiIndex(teDomain.getDomainId(), teSubDomain.getSubDomainId(),
                teBsl.getBitstringlength(), teSi.getSi(), bierNode);
        if (-1 == teSiIndex) {
            MplsLabel mplsLabel = getMplsLabel(topologyId, bierNode);
            LOG.info("Mpls label for node {} si {} is {}.", bierNode.getNodeId(), teSi.getSi(), mplsLabel.getValue());
            TeSiBuilder teSiBuilder = new TeSiBuilder(teSi);
            teSiBuilder.setFtLabel(mplsLabel);
            teSi = teSiBuilder.build();
        }
        bierNode = constructBierNode(topologyId, nodeId, teDomain, teSubDomain, teBsl, teSi, teBp);
        return mergeBierNode(topologyId, bierNode);
    }

    protected  boolean deleteOneBpFromNode(String topologyId, DomainId domainId, SubDomainId subDomainId,
                                           Bsl bitstringlength, Si si, String nodeId, String tpId) {
        TeBsl teBsl = queryTeBsl(topologyId, nodeId, domainId, subDomainId, bitstringlength);
        List<TeSi> teSiList = teBsl.getTeSi();
        if (null == teSiList || teSiList.isEmpty()) {
            LOG.info("Delete bp failed, teSi list is null or empty.");
            return false;
        }

        TeSi teSi = null;
        for (TeSi teSi1:teSiList) {
            if (teSi1.getSi().equals(si)) {
                teSi = teSi1;
            }
        }
        if (null == teSi) {
            LOG.info("Delete bp failed, no matched input teSi in teSi list.");
            return false;
        }
        List<TeBp> teBpList = teSi.getTeBp();
        if (null == teBpList || teBpList.isEmpty()) {
            LOG.info("Delete bp failed, teBp list is null or empty.");
            return false;
        }

        if (teBpList.size() > 1) {
            return deleteTeBP(topologyId, nodeId, domainId, subDomainId, bitstringlength, si, tpId);
        } else if (teSiList.size() > 1) {
            return deleteTeSi(topologyId, nodeId, domainId, subDomainId, bitstringlength, si);
        } else {
            return deleteTeBsl(topologyId, nodeId, domainId, subDomainId, bitstringlength);
        }
    }

    private BierNode constructBierNode(String topologyId, String nodeId, TeDomain teDomain, TeSubDomain teSubDomain,
                                       TeBsl teBsl, TeSi teSi, TeBp teBp) {
        List<TeBp> teBpList = new ArrayList<>();
        teBpList.add(teBp);
        TeSiBuilder teSiBuilder = new TeSiBuilder(teSi);
        teSiBuilder.setTeBp(teBpList);
        List<TeSi> teSiList = new ArrayList<>();
        teSiList.add(teSiBuilder.build());
        TeBslBuilder teBslBuilder = new TeBslBuilder(teBsl);
        teBslBuilder.setTeSi(teSiList);
        List<TeBsl> bslList = new ArrayList<>();
        bslList.add(teBslBuilder.build());
        TeSubDomainBuilder teSubDomainBuilder = new TeSubDomainBuilder(teSubDomain);
        teSubDomainBuilder.setTeBsl(bslList);
        List<TeSubDomain> teSubDomainList = new ArrayList<>();
        teSubDomainList.add(teSubDomainBuilder.build());
        TeDomainBuilder teDomainBuilder = new TeDomainBuilder(teDomain);
        teDomainBuilder.setTeSubDomain(teSubDomainList);
        List<TeDomain> teDomainList = new ArrayList<>();
        teDomainList.add(teDomainBuilder.build());

        BierTeNodeParamsBuilder builder = new BierTeNodeParamsBuilder();
        builder.setTeDomain(teDomainList);
        BierNodeBuilder bierNodeBuilder = new BierNodeBuilder(getBierNodeById(topologyId, nodeId));
        bierNodeBuilder.setBierTeNodeParams(builder.build());
        return bierNodeBuilder.build();
    }

    private MplsLabel getMplsLabel(String topologyId, BierNode bierNode) {
        if (!topoManager.checkLabelExist(bierNode)) {
            LOG.info("Te label range of node {} is not configured！", bierNode.getNodeId());
        }
        Long ftLabelLong = topoManager.buildFtLabel(topologyId,bierNode);
        if (ftLabelLong == -1L) {
            LOG.info("LabelRange for node {] ft-label is use up!", bierNode);
            return null;
        }
        MplsLabel ftLabel = new MplsLabel(ftLabelLong);
        return ftLabel;
    }

    protected  List<Integer> getUsedSiInSubdomainBsl(String topologyId, int subdomainValue, int bslValue) {
        List<Integer> siList = new ArrayList<>();
        QuerySubdomainBslSiInputBuilder builder = new QuerySubdomainBslSiInputBuilder();
        builder.setTopologyId(topologyId);
        Bsl bsl = bslValueMap.get(bslValue);
        builder.setBslValue(bsl);
        SubDomainId subdomainId = new SubDomainId(subdomainValue);
        builder.setSubdomainValue(subdomainId);
        builder.setAllocateModel(BpAllocateModel.AllocateModel.MANUAL);
        Future<RpcResult<QuerySubdomainBslSiOutput>> output = rpcConsumerRegistry.getRpcService(
                BierBpAllocateParamsConfigService.class).querySubdomainBslSi(builder.build());
        if (null == output) {
            LOG.info("Read <Subdomain, Bsl, Si> from datastore failed!");
            return null;
        }
        try {
            List<SiOfModel> siOfModelList = output.get().getResult().getSiOfModel();

            for (SiOfModel siOfModel:siOfModelList) {
                siList.add(siOfModel.getSiValue().getValue());
            }
            return siList;
        } catch (InterruptedException | ExecutionException e) {
            LOG.info(e.getStackTrace().toString());
            return null;
        }
    }

    protected boolean addUsedSubdomainBslSi(String topologyId, int subdomainValue, int bslValue, int siValue) {
        AddSubdomainBslSiInputBuilder builder = new AddSubdomainBslSiInputBuilder();
        builder.setTopologyId(topologyId);
        SubDomainId subdomainId = new SubDomainId(subdomainValue);
        builder.setSubdomainValue(subdomainId);
        Bsl bsl = bslValueMap.get(bslValue);
        builder.setBslValue(bsl);
        Si si = new Si(siValue);
        builder.setSiValue(si);
        builder.setAllocateModel(BpAllocateModel.AllocateModel.AUTO);
        Future<RpcResult<AddSubdomainBslSiOutput>> output = rpcConsumerRegistry.getRpcService(
                BierBpAllocateParamsConfigService.class).addSubdomainBslSi(builder.build());
        try {
            if (output.get().getResult().getConfigureResult().getResult().equals(ConfigureResult.Result.SUCCESS)) {
                return true;
            } else {
                LOG.info("Add <Subdomain Bsl Si > to datastore failed!");
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.info(e.getStackTrace().toString());
        }

        return false;
    }

    protected boolean deleteUsedSubdomainBslSi(String topologyId, int subdomainValue, int bslValue, int siValue) {
        DeleteSubdomainBslSiInputBuilder builder = new DeleteSubdomainBslSiInputBuilder();
        builder.setTopologyId(topologyId);
        SubDomainId subdomainId = new SubDomainId(subdomainValue);
        Bsl bsl = bslValueMap.get(bslValue);
        Si si = new Si(siValue);
        builder.setSubdomainValue(subdomainId);
        builder.setBslValue(bsl);
        builder.setSiValue(si);
        builder.setAllocateModel(BpAllocateModel.AllocateModel.AUTO);
        Future<RpcResult<DeleteSubdomainBslSiOutput>> output = rpcConsumerRegistry.getRpcService(
                BierBpAllocateParamsConfigService.class).deleteSubdomainBslSi(builder.build());
        try {
            if (output.get().getResult().getConfigureResult().getResult().equals(ConfigureResult.Result.SUCCESS)) {
                return true;
            } else {
                LOG.info("Delete <Subdomain Bsl Si > from datastore failed!");
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.info(e.getStackTrace().toString());
        }
        return false;
    }

    @Override
    public boolean setBferListToChannel(String channelName, List<Bfer> bferList) {
        bferListOfChannel.put(channelName,bferList);
        //Persistence to datastore
        if (!addDeleteBferListOfChannel(channelName,null,DELETE)) {
            return false;
        }
        for (Bfer bfer:bferList) {
            if (!addDeleteBferListOfChannel(channelName, bfer, ADD)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeBferListToChannel(String channelName) {
        bferListOfChannel.remove(channelName);
        //Persistence to datastore
        if (!addDeleteBferListOfChannel(channelName,null,DELETE)) {
            return false;
        }
        return true;
    }

    @Override
    public List<Bfer> getBferListOfChannel(String channelName) {
        return bferListOfChannel.get(channelName);
    }


    protected TeDomain getTeDomainFromChannel(Channel channel) {
        DomainId domainId = channel.getDomainId();
        TeDomainBuilder teDomainBuilder = new TeDomainBuilder();
        teDomainBuilder.setDomainId(domainId);
        teDomainBuilder.setKey(new TeDomainKey(domainId));
        return teDomainBuilder.build();
    }

    protected TeSubDomain getTeSubDomainFromChannel(Channel channel) {
        SubDomainId subDomainId = channel.getSubDomainId();
        TeSubDomainBuilder teSubDomainBuilder = new TeSubDomainBuilder();
        teSubDomainBuilder.setSubDomainId(subDomainId);
        teSubDomainBuilder.setKey(new TeSubDomainKey(subDomainId));
        return teSubDomainBuilder.build();
    }

    @Override
    public List<SubdomainBslSi> getSubdomainBslSiAllocatedToChannel(Channel channel) {
        List<SubdomainBslSi> subdomainBslSiList = new ArrayList<>();
        for (ChannelNameBferNodeId key : channelBferSubdomainBslSiMap.keySet()) {
            if (key.getChannelName().equals(channel.getName())) {
                SubdomainBslSi subdomainBslSi = channelBferSubdomainBslSiMap.get(key);
                subdomainBslSiList.add(subdomainBslSi);
            }
        }
        return subdomainBslSiList;
    }

    @Override
    public List<SubdomainBslSi> getAllAllocatedSubdomainBslSi() {
        List<SubdomainBslSi> subdomainBslSiList = new ArrayList<>();
        for (ChannelNameBferNodeId key : channelBferSubdomainBslSiMap.keySet()) {
            SubdomainBslSi subdomainBslSi = channelBferSubdomainBslSiMap.get(key);
            subdomainBslSiList.add(subdomainBslSi);
        }
        return subdomainBslSiList;
    }

    public BierNode getBierNodeById(String topologyId, String nodeId) {
        InstanceIdentifier<BierNode> path = getTopoPath(topologyId).child(BierNode.class, new BierNodeKey(nodeId));
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        try {
            return tx.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet().get();
        } catch (ReadFailedException e) {
            LOG.info(e.getStackTrace().toString());
        }
        return null;
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

    private InstanceIdentifier<BierTopology> getTopoPath(String topologyId) {
        return InstanceIdentifier.create(BierNetworkTopology.class).child(BierTopology.class,
                new BierTopologyKey(topologyId));
    }

    protected Integer readRecommendBsl(String topologyId) {
        InstanceIdentifier<RecommendBsl> bslPath = InstanceIdentifier.create(BierBpAllocateParams.class)
                .child(TopoBpAllocateParams.class, new TopoBpAllocateParamsKey(topologyId)).child(RecommendBsl.class);
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        try {
            Optional<RecommendBsl> optional = tx.read(LogicalDatastoreType.CONFIGURATION, bslPath).checkedGet();
            if (optional.isPresent()) {
                int recommendBsl = optional.get().getRecommendBsl().getIntValue();
                recommendBsl = recommendBsl << (recommendBsl + 5);
                return recommendBsl;
            } else {
                NotificationProvider.getInstance().notifyFailureReason("Recommend bsl has not been configured!");
                LOG.info("Recommend bsl has not been configured!");
                return -1;
            }

        } catch (ReadFailedException e) {
            LOG.info("Read recommend bsl from datastore failed!");
        }
        return null;

    }

    protected TeBp getTeBp(String topologyId, String nodeId, DomainId domainId, SubDomainId subDomainId, Bsl bsl,
                           Si si, String tpId) {
        InstanceIdentifier<TeBp> teBpPath = getTeBpPath(topologyId, nodeId, domainId, subDomainId, bsl, si, tpId);
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        try {
            Optional<TeBp> optional = tx.read(LogicalDatastoreType.CONFIGURATION, teBpPath).checkedGet();
            if (optional.isPresent()) {
                return optional.get();
            } else {
                LOG.info("TeBp of tpId {} of <{} {} {} >is not in datastore.", tpId, subDomainId.getValue(),
                        bsl.getIntValue(), si.getValue());
                return null;
            }
        } catch (ReadFailedException e) {
            LOG.info("Read teBp from datastore failed!");
        }
        return null;
    }

    private InstanceIdentifier<TeSubDomain> getTeSubdomainPath(String topologyId,String nodeId,DomainId domainId,
                                                               SubDomainId subDomainId) {
        InstanceIdentifier<TeSubDomain> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId))
                .child(BierNode.class, new BierNodeKey(nodeId))
                .child(BierTeNodeParams.class).child(TeDomain.class, new TeDomainKey(domainId))
                .child(TeSubDomain.class, new TeSubDomainKey(subDomainId));
        return path;
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

    private InstanceIdentifier<TeBp> getTeBpPath(String topologyId,String nodeId,DomainId domainId,
                                                 SubDomainId subDomainId,Bsl bitstringlength,Si si,String tpId) {
        InstanceIdentifier<TeBp> path = getTeSiPath(topologyId, nodeId, domainId, subDomainId, bitstringlength, si)
                .child(TeBp.class, new TeBpKey(tpId));
        return path;
    }

    protected  boolean isSudomainHasDeployedChannel(int subdomainId) {
        for (ChannelNameBferNodeId channelNameBferNodeId:channelBferSubdomainBslSiMap.keySet()) {
            SubdomainBslSi subdomainBslSi = channelBferSubdomainBslSiMap.get(channelNameBferNodeId);
            if (subdomainBslSi.getSubdomainValue() == subdomainId) {
                return true;
            }
        }
        return false;
    }

    public BierLink queryLinkByNodeIdAndTpId(String topologyId, String nodeId, String tpId) {
        BierTopology bierTopology = getBierTopology(topologyId);
        List<BierLink> bierLinkList = bierTopology.getBierLink();
        if (null == bierTopology.getBierLink() || bierTopology.getBierLink().isEmpty()) {
            LOG.info("Bierlink list is null or empty");
        } else {
            for (BierLink bierLink : bierLinkList) {
                if (bierLink.getLinkSource().getSourceNode().equals(nodeId)
                        && bierLink.getLinkSource().getSourceTp().equals(tpId)) {
                    return bierLink;
                }
            }
        }
        return null;
    }

    public BierLink queryTeSubdomainLinkByNodeIdAndTpId(String topologyId, String nodeId, String tpId,
                                                        SubDomainId subDomainId) {
        BierTopology bierTopology = getBierTopology(topologyId);
        List<BierLink> bierLinkList = queryTeSubdomainLink(subDomainId, bierTopology);
        if (null == bierTopology || null == bierTopology.getBierLink() || bierTopology.getBierLink().isEmpty()) {
            LOG.info("Bierlink list is null or empty.");
        } else {
            for (BierLink bierLink : bierLinkList) {
                if (bierLink.getLinkSource().getSourceNode().equals(nodeId)
                        && bierLink.getLinkSource().getSourceTp().equals(tpId)) {
                    return bierLink;
                }
            }
        }
        return null;
    }



    public boolean mergeBierNode(String topologyId, BierNode bierNode) {
        InstanceIdentifier<BierNode> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId))
                .child(BierNode.class, new BierNodeKey(bierNode.getKey()));
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.merge(LogicalDatastoreType.CONFIGURATION, path, bierNode, true);
        try {
            tx.submit().checkedGet();
            return true;
        } catch (TransactionCommitFailedException e) {
            LOG.info("Merge biernode to datastore failed!");
        }
        return false;
    }


    public List<BierNode> queryTeSubdomainNode(SubDomainId subDomainId, BierTopology bierTopology) {
        List<BierNode> allBierNodes = bierTopology.getBierNode();
        List<BierNode> subdomainNodes = new ArrayList<>();
        for (BierNode bierNode:allBierNodes) {
            BierTeNodeParams bierTeNodeParams = bierNode.getBierTeNodeParams();
            if (null != bierTeNodeParams && null != bierTeNodeParams.getTeDomain() && !bierTeNodeParams
                    .getTeDomain().isEmpty()) {
                for (TeSubDomain teSubDomain:bierTeNodeParams.getTeDomain().get(0).getTeSubDomain()) {
                    if (teSubDomain.getSubDomainId().equals(subDomainId)) {
                        subdomainNodes.add(bierNode);
                    }
                }
            }
        }
        return subdomainNodes;
    }

    public List<BierLink> queryTeSubdomainLink(SubDomainId subDomainId, BierTopology bierTopology) {
        List<BierNode> subdomainNodes = queryTeSubdomainNode(subDomainId, bierTopology);
        List<BierLink> allBierLinks = bierTopology.getBierLink();
        List<String> nodeIdList = new ArrayList<>();
        for (BierNode bierNode:subdomainNodes) {
            nodeIdList.add(bierNode.getNodeId());
        }
        List<BierLink> subdomainLinks = new ArrayList<>();
        for (BierLink bierLink:allBierLinks) {
            if (nodeIdList.contains(bierLink.getLinkSource().getSourceNode()) && nodeIdList.contains(
                    bierLink.getLinkDest().getDestNode())) {
                subdomainLinks.add(bierLink);
            }
        }
        return subdomainLinks;
    }

    protected TeBsl queryTeBsl(String topologyId,String nodeId,DomainId domainId,
                             SubDomainId subDomainId,Bsl bitstringlength) {
        InstanceIdentifier<TeBsl> path = getTeBslPath(topologyId, nodeId, domainId, subDomainId, bitstringlength);
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();

        try {
            Optional<TeBsl> optional = tx.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
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

    private boolean deleteTeBP(String topologyId,String nodeId,DomainId domainId,
                               SubDomainId subDomainId,Bsl bitstringlength,Si si,String tpId) {
        InstanceIdentifier<TeBp> path = getTeBpPath(topologyId, nodeId, domainId, subDomainId, bitstringlength,
                si, tpId);
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.delete(LogicalDatastoreType.CONFIGURATION, path);
        try {
            tx.submit().checkedGet();
            return true;
        } catch (TransactionCommitFailedException e) {
            LOG.info("Delete teBp from datastore failed!");
        }
        return false;
    }

    private boolean deleteTeSi(String topologyId,String nodeId,DomainId domainId,
                               SubDomainId subDomainId,Bsl bitstringlength,Si si) {
        InstanceIdentifier<TeSi> path = getTeSiPath(topologyId, nodeId, domainId, subDomainId, bitstringlength, si);
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.delete(LogicalDatastoreType.CONFIGURATION, path);
        try {
            tx.submit().checkedGet();
            return true;
        } catch (TransactionCommitFailedException e) {
            LOG.info("Delete teSi from datastore failed!");
        }
        return false;
    }

    private boolean deleteTeBsl(String topologyId,String nodeId,DomainId domainId,
                                SubDomainId subDomainId,Bsl bitstringlength) {
        InstanceIdentifier<TeBsl> path = getTeBslPath(topologyId, nodeId, domainId, subDomainId, bitstringlength);
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.delete(LogicalDatastoreType.CONFIGURATION, path);
        try {
            tx.submit().checkedGet();
            return true;
        } catch (TransactionCommitFailedException e) {
            LOG.info("Delete teBsl from datastore failed!");
        }
        return false;
    }

    public boolean deleteTeSubdomain(String topologyId,String nodeId,DomainId domainId,
                                     SubDomainId subDomainId) {
        InstanceIdentifier<TeSubDomain> path = getTeSubdomainPath(topologyId, nodeId, domainId, subDomainId);
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.delete(LogicalDatastoreType.CONFIGURATION, path);
        try {
            tx.submit().checkedGet();
            return true;
        } catch (TransactionCommitFailedException e) {
            LOG.info("Delete teSubdomain from data store failed!");
        }
        return false;
    }

    public Map<Integer,List<Integer>> getBslUsedInSubdomain() {
        return bslUsedInSubdomain;
    }

    private void initBslValueMap() {
        bslValueMap.put(1,Bsl._64Bit);
        bslValueMap.put(2,Bsl._128Bit);
        bslValueMap.put(3,Bsl._256Bit);
        bslValueMap.put(4,Bsl._512Bit);
        bslValueMap.put(5,Bsl._1024Bit);
        bslValueMap.put(6,Bsl._2048Bit);
        bslValueMap.put(7,Bsl._4096Bit);
    }

    public Map<Integer, Bsl> getBslValueMap() {
        return bslValueMap;
    }

    protected InstanceIdentifier<StrategyDataPersistence> getStrategyPersisPath() {
        return InstanceIdentifier.create(StrategyDataPersistence.class);
    }

    protected  boolean addDeleteBslUsedInSubdomain(int subdomainId, int bslValue, int type) {
        InstanceIdentifier<BslUsedInSubdomain> bslUsedInSubdomainIID = getStrategyPersisPath().child(BslUsedInSubdomain
                .class, new BslUsedInSubdomainKey(subdomainId));
        InstanceIdentifier<BslOfSubdomain> bslOfSubdomainIID = bslUsedInSubdomainIID.child(BslOfSubdomain.class,
                new BslOfSubdomainKey(bslValue));
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        switch (type) {
            case ADD:
                BslOfSubdomainBuilder bslOfSubdomainBuilder = new BslOfSubdomainBuilder();
                bslOfSubdomainBuilder.setKey(new BslOfSubdomainKey(bslValue));
                bslOfSubdomainBuilder.setBslValue(bslValue);
                tx.put(LogicalDatastoreType.CONFIGURATION, bslOfSubdomainIID, bslOfSubdomainBuilder.build(), true);
                if (!txSubmit(tx, "Add bsl used in subdomain failed!")) {
                    return false;
                }
                break;

            case DELETE:
                tx.delete(LogicalDatastoreType.CONFIGURATION, bslOfSubdomainIID);
                if (!txSubmit(tx, "Delete bsl used in subdomain failed!")) {
                    return false;
                }
                break;
            default:
                LOG.info("Bsl used in subdomain : type error");
                return false;
        }

        return true;
    }

    protected boolean addDeleteSiUsedInSubdomainBsl(int subdomainId, TeBsl teBsl, int siValue, int type) {
        InstanceIdentifier<SiOfSubdomainBsl> siOfSubdomainBslIID = getStrategyPersisPath().child(
                SiUsedInSubdomainBsl.class, new SiUsedInSubdomainBslKey(teBsl.getBitstringlength(),
                        subdomainId)).child(SiOfSubdomainBsl.class, new SiOfSubdomainBslKey(siValue));
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        switch (type) {
            case ADD:
                SiOfSubdomainBslBuilder builder = new SiOfSubdomainBslBuilder();
                builder.setKey(new SiOfSubdomainBslKey(siValue));
                builder.setSiValue(siValue);
                tx.put(LogicalDatastoreType.CONFIGURATION, siOfSubdomainBslIID, builder.build(), true);
                if (!txSubmit(tx, "Add si used in <subdomain bsl> failed!")) {
                    return false;
                }
                break;
            case DELETE:
                tx.delete(LogicalDatastoreType.CONFIGURATION, siOfSubdomainBslIID);
                if (!txSubmit(tx, "Delete si used in <subdomain bsl> failed!")) {
                    return  false;
                }
                break;
            default:
                LOG.info("Si used in <subdomain bsl> : type error");
                return false;
        }
        return true;
    }

    protected boolean addDeleteBpUsedInSubdomainBslSi(int subdomainId, TeBsl teBsl, TeSi teSi,
                                                              int bpValue, int type) {
        InstanceIdentifier<BpOfSubdomainBslSi> bpOfSubdomainBslSiIID = getStrategyPersisPath().child(
                BpUsedInSubdomainBslSi.class, new BpUsedInSubdomainBslSiKey(teBsl.getBitstringlength(), teSi.getSi(),
                        subdomainId)).child(BpOfSubdomainBslSi.class, new BpOfSubdomainBslSiKey(bpValue));
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        switch (type) {
            case ADD:
                BpOfSubdomainBslSiBuilder builder = new BpOfSubdomainBslSiBuilder();
                builder.setBpValue(bpValue);
                builder.setKey(new BpOfSubdomainBslSiKey(bpValue));
                tx.put(LogicalDatastoreType.CONFIGURATION, bpOfSubdomainBslSiIID, builder.build(), true);
                if (!txSubmit(tx, "Add bp used in <subdomain bsl si> failed!")) {
                    return false;
                }
                break;
            case DELETE:
                tx.delete(LogicalDatastoreType.CONFIGURATION, bpOfSubdomainBslSiIID);
                if (!txSubmit(tx, "Delete bp used in <subdomain bsl si> failed!")) {
                    return false;
                }
                break;
            default:
                LOG.info("Bp used in <subdomain bsl si> : type error");
                return false;
        }
        return true;
    }

    protected boolean addDeleteBferListOfChannel(String channelName, Bfer bfer, int type) {
        InstanceIdentifier<BferListOfChannel> bferListOfChannelIID = getStrategyPersisPath()
                .child(BferListOfChannel.class, new BferListOfChannelKey(channelName));
        InstanceIdentifier<BferOfChannel> bferOfChannelIID = null;
        if (null != bfer) {
            bferOfChannelIID = bferListOfChannelIID.child(BferOfChannel.class, new BferOfChannelKey(
                    bfer.getBferNodeId()));
        }
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        switch (type) {
            case ADD:
                BferOfChannelBuilder builder = new BferOfChannelBuilder();
                builder.setBferNodeId(bfer.getBferNodeId());
                builder.setKey(new BferOfChannelKey(bfer.getBferNodeId()));

                BierPathBuilder bierPathBuilder = new BierPathBuilder();
                bierPathBuilder.setPathLink(bfer.getBierPath().getPathLink());
                bierPathBuilder.setPathMetric(bfer.getBierPath().getPathMetric());
                builder.setBierPath(bierPathBuilder.build());

                tx.put(LogicalDatastoreType.CONFIGURATION, bferOfChannelIID, builder.build(), true);
                if (!txSubmit(tx, "Add bfer list of channel failed!")) {
                    return false;
                }
                break;
            case DELETE:
                try {
                    Optional<BferListOfChannel> optional = tx.read(LogicalDatastoreType.CONFIGURATION,
                            bferListOfChannelIID).checkedGet();
                    if (optional.isPresent()) {
                        tx.delete(LogicalDatastoreType.CONFIGURATION, bferListOfChannelIID);
                        if (!txSubmit(tx, "Delete bfer list of channel failed!")) {
                            return false;
                        }
                        break;
                    }
                } catch (ReadFailedException e) {
                    LOG.info(e.getStackTrace().toString());
                }
                break;
            default:
                LOG.info("Bfer list of channel : type error");
                return false;
        }
        return true;
    }

    public boolean addDeleteChannelBferSubdomainBslSiMap(String channelName, String bferNodeId, Integer subdomainId,
                                                            TeBsl teBsl, TeSi teSi, int type) {
        InstanceIdentifier<ChannelBferSubdomainBslSiMap> channelBferSubdomainBslSiIID = getStrategyPersisPath().child(
                ChannelBferSubdomainBslSiMap.class, new ChannelBferSubdomainBslSiMapKey(bferNodeId, channelName));
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        switch (type) {
            case ADD:
                ChannelBferSubdomainBslSiMapBuilder builder = new ChannelBferSubdomainBslSiMapBuilder();
                builder.setBferNodeId(bferNodeId);
                builder.setBsl(teBsl.getBitstringlength());
                builder.setChannelName(channelName);
                builder.setKey(new ChannelBferSubdomainBslSiMapKey(bferNodeId,channelName));
                builder.setSi(teSi.getSi());
                builder.setSubdomainId(subdomainId);
                tx.put(LogicalDatastoreType.CONFIGURATION, channelBferSubdomainBslSiIID, builder.build(), true);
                if (!txSubmit(tx, "Add channel bfer <subdomain bsl si> map failed!")) {
                    return false;
                }
                break;
            case DELETE:
                tx.delete(LogicalDatastoreType.CONFIGURATION, channelBferSubdomainBslSiIID);
                if (!txSubmit(tx, "Delete channel bfer <subdomain bsl si> map failed!")) {
                    return false;
                }
                break;
            default:
                LOG.info("Add channel bfer <subdomain bsl si> map : type error!");
                return false;
        }
        return true;
    }

    protected boolean txSubmit(ReadWriteTransaction tx, String errMsg) {
        try {
            tx.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.info(errMsg);
            return false;
        }
        return true;
    }

    protected StrategyDataPersistence queryStrategyData() {
        InstanceIdentifier<StrategyDataPersistence> strategyDataPersistenceIID = getStrategyPersisPath();
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        try {
            Optional<StrategyDataPersistence> optional = tx.read(LogicalDatastoreType
                    .CONFIGURATION, strategyDataPersistenceIID).checkedGet();
            if (optional.isPresent()) {
                return optional.get();
            } else {
                StrategyDataPersistenceBuilder builder = new StrategyDataPersistenceBuilder();
                return builder.build();
            }

        } catch (ReadFailedException e) {
            LOG.info("Query strategy persistence datastore failed!");
        }
        return null;
    }

    public boolean resumeAbstractDataStructures() {
        StrategyDataPersistence strategyDataPersistence = queryStrategyData();
        if (null == strategyDataPersistence) {
            return false;
        }
        if (null == strategyDataPersistence.getSubdomainDeployedChannel()) {
            LOG.info("First time start up controller!");
            return true;
        }
        LOG.info("Resume bsl used in subdomain!");
        List<BslUsedInSubdomain> bslUsedInSubdomains = strategyDataPersistence.getBslUsedInSubdomain();
        if (null == bslUsedInSubdomains) {
            LOG.info("No bsl used in subdomain data found!");
        } else {
            for (BslUsedInSubdomain bslUsedInSD:bslUsedInSubdomains) {
                int subdomainId = bslUsedInSD.getSubdomainId();
                List<Integer> bslUsed = new ArrayList<>();
                for (BslOfSubdomain bslOfSubdomain:bslUsedInSD.getBslOfSubdomain()) {
                    bslUsed.add(bslOfSubdomain.getBslValue());
                }
                bslUsedInSubdomain.put(subdomainId, bslUsed);
            }
        }

        LOG.info("Resume si used in <subdomain bsl>!");
        List<SiUsedInSubdomainBsl> siUsedInSubdomainBsls = strategyDataPersistence.getSiUsedInSubdomainBsl();
        if (null == siUsedInSubdomainBsls) {
            LOG.info("No si used in <subdomain bsl> data found!");
        } else {
            for (SiUsedInSubdomainBsl siUsedInSDBsl:siUsedInSubdomainBsls) {
                int subdomainId = siUsedInSDBsl.getSubdomainId();
                Bsl bsl = siUsedInSDBsl.getBsl();
                TeBslBuilder teBslBuilder = new TeBslBuilder();
                teBslBuilder.setBitstringlength(bsl);
                teBslBuilder.setKey(new TeBslKey(bsl));
                SubdomainBsl subdomainBsl = new SubdomainBsl(subdomainId, teBslBuilder.build());
                List<Integer> siUsed = new ArrayList<>();
                for (SiOfSubdomainBsl siOfSubdomainBsl:siUsedInSDBsl.getSiOfSubdomainBsl()) {
                    siUsed.add(siOfSubdomainBsl.getSiValue());
                }
                siUsedInSubdomainBsl.put(subdomainBsl, siUsed);
            }
        }

        LOG.info("Resume bp used in <subdomain bsl si>!");
        List<BpUsedInSubdomainBslSi> bpUsedInSubdomainBslSis = strategyDataPersistence.getBpUsedInSubdomainBslSi();
        if (null == bpUsedInSubdomainBslSis) {
            LOG.info("No bp used in <subdomain bsl si> data found!");
        } else {
            for (BpUsedInSubdomainBslSi bpUsedInSDBslSi:bpUsedInSubdomainBslSis) {
                Bsl bsl = bpUsedInSDBslSi.getBsl();
                TeBslBuilder teBslBuilder = new TeBslBuilder();
                teBslBuilder.setKey(new TeBslKey(bsl));
                teBslBuilder.setBitstringlength(bsl);
                Si si = bpUsedInSDBslSi.getSi();
                TeSiBuilder teSiBuilder = new TeSiBuilder();
                teSiBuilder.setSi(si);
                teSiBuilder.setKey(new TeSiKey(si));
                int subdomainid = bpUsedInSDBslSi.getSubdomainId();
                SubdomainBslSi subdomainBslSi = new SubdomainBslSi(subdomainid, teBslBuilder.build(),
                        teSiBuilder.build());
                List<Integer> bpUsed = new ArrayList<>();
                for (BpOfSubdomainBslSi bpOfSubdomainBslSi:bpUsedInSDBslSi.getBpOfSubdomainBslSi()) {
                    bpUsed.add(bpOfSubdomainBslSi.getBpValue());
                }
                bpUsedInSubdomainBslSi.put(subdomainBslSi, bpUsed);
            }
        }

        LOG.info("Resume bferlist of channel!");
        List<BferListOfChannel> bferListOfChannels = strategyDataPersistence.getBferListOfChannel();
        if (null == bferListOfChannels) {
            LOG.info("No bferlist of channel data found!");
        } else {
            for (BferListOfChannel bferListChannel:bferListOfChannels) {
                String channelName = bferListChannel.getChannelName();
                List<BferOfChannel> bferOfChannels = bferListChannel.getBferOfChannel();
                List<Bfer> bferList = new ArrayList<>();
                for (BferOfChannel bferOfChannel:bferOfChannels) {
                    BferBuilder bferBuilder = new BferBuilder();
                    bferBuilder.setBferNodeId(bferOfChannel.getBferNodeId());
                    bferBuilder.setKey(new BferKey(bferOfChannel.getBferNodeId()));
                    org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPathBuilder bierPathBuilder
                            = new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPathBuilder();
                    bierPathBuilder.setPathLink(bferOfChannel.getBierPath().getPathLink());
                    bierPathBuilder.setPathMetric(bferOfChannel.getBierPath().getPathMetric());
                    bferBuilder.setBierPath(bierPathBuilder.build());
                    bferList.add(bferBuilder.build());
                }
                bferListOfChannel.put(channelName, bferList);
            }
        }

        LOG.info("Resume channelBfer <subdomain bsl si> map!");
        List<ChannelBferSubdomainBslSiMap> channelBferSubdomainBslSiMaps = strategyDataPersistence
                .getChannelBferSubdomainBslSiMap();
        if (null == channelBferSubdomainBslSiMaps) {
            LOG.info("No channelBfer <subdomain bsl si> data found!");
        } else {
            for (ChannelBferSubdomainBslSiMap channelBferSDBslSi:channelBferSubdomainBslSiMaps) {
                Bsl bsl = channelBferSDBslSi.getBsl();
                TeBslBuilder teBslBuilder = new TeBslBuilder();
                teBslBuilder.setKey(new TeBslKey(bsl));
                teBslBuilder.setBitstringlength(bsl);
                Si si = channelBferSDBslSi.getSi();
                TeSiBuilder teSiBuilder = new TeSiBuilder();
                teSiBuilder.setSi(si);
                teSiBuilder.setKey(new TeSiKey(si));
                int subdomainId = channelBferSDBslSi.getSubdomainId();
                String channelName = channelBferSDBslSi.getChannelName();
                String bferNodeId = channelBferSDBslSi.getBferNodeId();
                ChannelNameBferNodeId channelNameBferNodeId = new ChannelNameBferNodeId(channelName, bferNodeId);
                SubdomainBslSi subdomainBslSi = new SubdomainBslSi(subdomainId, teBslBuilder.build(),
                        teSiBuilder.build());
                channelBferSubdomainBslSiMap.put(channelNameBferNodeId, subdomainBslSi);
            }
        }
        return true;
    }

    public Map<ChannelNameBferNodeId,SubdomainBslSi> getChannelSDBslSiMap() {
        return channelBferSubdomainBslSiMap;
    }
}
