/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.service.impl.teconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.BierTeBtaftWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.service.impl.NotificationProvider;
import org.opendaylight.bier.service.impl.Util;
import org.opendaylight.bier.service.impl.allocatebp.AbstractBPAllocateStrategy;
import org.opendaylight.bier.service.impl.allocatebp.ChannelNameBferNodeId;
import org.opendaylight.bier.service.impl.allocatebp.SubdomainBslSi;
import org.opendaylight.bier.service.impl.allocatebp.TopoBasedBpAllocateStrategy;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierForwardingType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BpAssignmentStrategy;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BackupPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceService;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveTeFrrPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.TeFrrPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.TeFrrPathUpdate;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.backup.path.Path;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferKey;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.TeFrrKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.te.frr.key.ProtectedLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.FrrPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.ExcludingLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.NextHopPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.NextNextHopPath;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.TeInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.routing.BierTeConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.frr.Btaft;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.frr.BtaftBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.frr.btaft.Addbitmask;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.frr.btaft.AddbitmaskBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.te.si.TeFIndex;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.te.si.TeFIndexBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.te.subdomain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.items.Resetbitmask;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.items.ResetbitmaskBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddResetBitMaskProcess {

    private static final Logger LOG = LoggerFactory.getLogger(AddResetBitMaskProcess.class);
    private static final String TOPOLOGY_ID = "example-linkstate-topology";
    public static final int FRR_BP_ADD = 1;
    public static final int FRR_BP_DELETE = 2;
    private Map<FrrIndexAddBitMask,Integer> frrIndexAddBitMaskMap = new HashMap<>();
    private Set<SDBslSiBpFrrIndex> sdBslSiBpFrrIndexSet = new HashSet<>();

    private AbstractBPAllocateStrategy bpAllocateStrategy;
    private BitStringProcess bitStringProcess;
    private BiftInfoProcess biftInfoProcess;
    private BierTeBiftWriter bierTeBiftWriter;
    private BierTeBtaftWriter bierTeBtaftWriter;
    private RpcConsumerRegistry rpcConsumerRegistry;
    private Util util;
    private static AddResetBitMaskProcess instance = new AddResetBitMaskProcess();

    public AddResetBitMaskProcess() {

    }

    public AddResetBitMaskProcess(DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry,
                                  BierTeBitstringWriter bierTeBitstringWriter,
                                  BierTeBiftWriter bierTeBiftWriter, BierTeBtaftWriter bierTeBtaftWriter) {
        this.rpcConsumerRegistry = rpcConsumerRegistry;
        this.bierTeBiftWriter = bierTeBiftWriter;
        this.bierTeBtaftWriter = bierTeBtaftWriter;
        this.bpAllocateStrategy = TopoBasedBpAllocateStrategy.getInstance();
        this.bitStringProcess = new BitStringProcess(dataBroker, rpcConsumerRegistry,
                bierTeBitstringWriter, bierTeBiftWriter);
        this.biftInfoProcess = new BiftInfoProcess(dataBroker, bierTeBiftWriter);
        this.util = new Util(dataBroker);
    }

    public boolean processFrrInfo(BierLink bierLink, BpAssignmentStrategy bpAssignmentStrategy,
                                    TeDomain teDomain, TeSubDomain teSubDomain, TeBsl teBsl, TeSi teSi,
                                    TeBp teBp, FrrPath frrPath, int type) {
        TeBsl frrTeBsl = getBierInBierTeBsl(teBsl);
        TeSi frrTeSi = getBierInBierTeSi(teSi);

        Channel addBitMaskChannel = getAddBitMaskChannelByAdjacency(bierLink, bpAssignmentStrategy, teDomain,
                teSubDomain, frrTeBsl, frrTeSi);
        Channel resetBitMaskChannel = getResetBitMaskChannelByAdjacency(bierLink, bpAssignmentStrategy, teDomain,
                teSubDomain, teBsl, teSi);

        ConfigurationResult biftConfResult = null;
        ConfigurationResult btaftConfResult = null;

        String nodeId = bierLink.getLinkSource().getSourceNode();
        String tpId = bierLink.getLinkSource().getSourceTp();

        if (type == FRR_BP_DELETE) {
            LOG.info("Delete te frr protection!");
            SDBslSiBpFrrIndex sdBslSiBpFrrIndex = new SDBslSiBpFrrIndex(bierLink.getLinkId(), teDomain, teSubDomain,
                    teBsl, teSi, teBp, null, null);
            String linkId = sdBslSiBpFrrIndex.getLinkId();
            Integer linkIdFrrTimes = 0;
            Integer frrIndex = -1;
            for (SDBslSiBpFrrIndex stFrrIndex : sdBslSiBpFrrIndexSet) {
                if (stFrrIndex.linkId.equals(linkId)) {
                    linkIdFrrTimes ++;
                }
                if (sdBslSiBpFrrIndex.equals(stFrrIndex)) {
                    frrIndex = stFrrIndex.getFrrIndex();
                }
            }
            sdBslSiBpFrrIndexSet.remove(sdBslSiBpFrrIndex);
            if (linkIdFrrTimes.equals(1)) {
                LOG.info("Call remove te frr path.");
                removeTeFrrPath(teSubDomain, linkId);
            }
            FrrIndexAddBitMask frrIndexAddBitMask = new FrrIndexAddBitMask(frrIndex, null, null, null, null);
            Integer frrIndexAddBitMaskUsedTimes = frrIndexAddBitMaskMap.get(frrIndexAddBitMask);
            TeInfo frrTeInfoForModify;
            if (frrIndexAddBitMaskUsedTimes.equals(1)) {
                frrIndexAddBitMaskMap.remove(frrIndexAddBitMask);
                Btaft btaft = constructBtaft(null, null, null, frrIndex);
                btaftConfResult = bierTeBtaftWriter.writeBierTeBtaft(ConfigurationType.DELETE, nodeId,
                        teSubDomain.getSubDomainId(), btaft);
            } else {
                frrIndexAddBitMaskMap.put(frrIndexAddBitMask, --frrIndexAddBitMaskUsedTimes);
            }
            frrTeInfoForModify = constructFrrTeInfo(teSubDomain, teBsl, teSi, teBp, null, null, null);
            biftConfResult = bierTeBiftWriter.writeTeBift(ConfigurationType.MODIFY, nodeId, frrTeInfoForModify);
            if (bpAssignmentStrategy.equals(BpAssignmentStrategy.Automatic)) {
                if (!deleteAddResetChannelToStrategy(addBitMaskChannel, teSubDomain, frrTeBsl, frrTeSi)
                        || !deleteAddResetChannelToStrategy(resetBitMaskChannel, teSubDomain, teBsl, teSi)) {
                    LOG.info("Delete add and reset bitmask channel from strategy failed!");
                    return false;
                }
            }
        }

        if (type == FRR_BP_ADD) {
            if (bpAssignmentStrategy.equals(BpAssignmentStrategy.Automatic) && null == frrPath) {
                LOG.info("Add te frr protection!");
                if (!persistAddResetChannelToStrategy(addBitMaskChannel, teSubDomain, frrTeBsl, frrTeSi)
                        || !persistAddResetChannelToStrategy(resetBitMaskChannel, teSubDomain, teBsl, teSi)) {
                    LOG.info("Persist add and reset channel to strategy failed!");
                    return false;
                }
            }

            if (null == frrPath) {
                frrPath = calTeFrrPath(teSubDomain, bierLink).getFrrPath();
            }

            if (null != frrPath) {
                List<Bfer> addBferList = calAddBferListFromTeFrrPath(frrPath);
                List<TeInfo> addTeInfoList = bitStringProcess.getTeInfoFrombferList(addBitMaskChannel, addBferList);
                List<Integer> addBitMaskValue = calMaskValueFromTeInfoList(addTeInfoList);

                FrrIndexAddBitMask frrIndexAddBitMask = new FrrIndexAddBitMask(null, teSubDomain.getSubDomainId()
                        .getValue(), frrTeBsl.getBitstringlength().getIntValue(), frrTeSi.getSi()
                        .getValue(),addBitMaskValue);
                int frrIndex = checkAndGetOneFrrIndex(frrIndexAddBitMask);

                SDBslSiBpFrrIndex sdBslSiBpFrrIndex = new SDBslSiBpFrrIndex(bierLink.getLinkId(), teDomain,
                        teSubDomain, teBsl, teSi, teBp, bpAssignmentStrategy, frrIndex);
                if (sdBslSiBpFrrIndexSet.contains(sdBslSiBpFrrIndex)) {
                    sdBslSiBpFrrIndexSet.remove(sdBslSiBpFrrIndex);
                }
                sdBslSiBpFrrIndexSet.add(sdBslSiBpFrrIndex);

                frrIndexAddBitMask.setFrrIndex(frrIndex);
                Integer frrIndexAddBitMaskUsedTimes = frrIndexAddBitMaskMap.get(frrIndexAddBitMask);

                LOG.info("Add frr index to map.");
                if (null == frrIndexAddBitMaskUsedTimes) {
                    frrIndexAddBitMaskMap.put(frrIndexAddBitMask, 1);
                    Btaft btaft = constructBtaft(frrTeBsl, frrTeSi, addBitMaskValue, frrIndex);
                    btaftConfResult = bierTeBtaftWriter.writeBierTeBtaft(ConfigurationType.ADD, nodeId,
                            teSubDomain.getSubDomainId(), btaft);
                } else {
                    frrIndexAddBitMaskMap.put(frrIndexAddBitMask, ++frrIndexAddBitMaskUsedTimes);
                }
                List<Bfer> resetBferList = calResetBferListFromTeFrrPath(frrPath);
                List<TeInfo> resetTeInfoList = bitStringProcess.getTeInfoFrombferList(resetBitMaskChannel,
                        resetBferList);
                List<Integer> resetBitMaskValue = calMaskValueFromTeInfoList(resetTeInfoList);

                Long nodeInterface = biftInfoProcess.processGetInterface(nodeId, tpId);

                TeInfo frrTeInfo = constructFrrTeInfo(teSubDomain, teBsl, teSi, teBp, nodeInterface,
                        resetBitMaskValue, frrIndex);
                biftConfResult = bierTeBiftWriter.writeTeBift(ConfigurationType.ADD, nodeId, frrTeInfo);
            }
            if (null != biftConfResult && !biftConfResult.isSuccessful() || null != btaftConfResult
                    && !btaftConfResult.isSuccessful()) {
                LOG.info(type + " frr info to south bound failed!");
                NotificationProvider.getInstance().notifyFailureReason(type + " frr info to south bound failed!");
                return false;
            }
        }

        LOG.info(type + " frr te info success!");
        return true;
    }

    private int checkAndGetOneFrrIndex(FrrIndexAddBitMask noFrrIndexAddBitMask) {

        Set<Integer> frrIndexSet = new HashSet<>();

        for (FrrIndexAddBitMask frrIndexAddBitMask : frrIndexAddBitMaskMap.keySet()) {
            if (frrIndexAddBitMask.equals(noFrrIndexAddBitMask)) {
                return frrIndexAddBitMask.getFrrIndex();
            }
            frrIndexSet.add(frrIndexAddBitMask.getFrrIndex());
        }

        int frrIndex = 1;
        while (frrIndexSet.contains(frrIndex)) {
            frrIndex ++;
        }
        return frrIndex;
    }


    private Channel getAddBitMaskChannelByAdjacency(BierLink bierLink, BpAssignmentStrategy bpAssignmentStrategy,
                                                    TeDomain teDomain, TeSubDomain teSubDomain,
                                                    TeBsl teBsl, TeSi teSi) {
        Channel resetBitMaskChannel = getResetBitMaskChannelByAdjacency(bierLink, bpAssignmentStrategy, teDomain,
                teSubDomain, teBsl, teSi);
        List<EgressNode> egressNodeList = resetBitMaskChannel.getEgressNode();
        List<EgressNode> egressNodes = new ArrayList<>();
        for (EgressNode egressNode : egressNodeList) {
            egressNode = setLocalDecapTpIdForEgressNode(egressNode, teDomain, teSubDomain, teBsl, teSi);
            egressNodes.add(egressNode);
        }
        ChannelBuilder channelBuilder = new ChannelBuilder(resetBitMaskChannel);
        String channelName = "add:" + bierLink.getLinkDest().getDestNode() + ":" + bierLink.getLinkDest().getDestTp()
                + ":" + teSubDomain.getSubDomainId().getValue() + ":" + teBsl.getBitstringlength().getIntValue()
                + ":" + teSi.getSi().getValue();
        channelBuilder.setKey(new ChannelKey(channelName));
        channelBuilder.setEgressNode(egressNodes);
        return channelBuilder.build();
    }

    private Channel getResetBitMaskChannelByAdjacency(BierLink bierLink, BpAssignmentStrategy bpAssignmentStrategy,
                                                      TeDomain teDomain, TeSubDomain teSubDomain,
                                                      TeBsl teBsl, TeSi teSi) {
        ChannelBuilder channelBuilder = new ChannelBuilder();
        channelBuilder.setDomainId(teDomain.getDomainId());
        channelBuilder.setSubDomainId(teSubDomain.getSubDomainId());
        channelBuilder.setIngressNode(bierLink.getLinkSource().getSourceNode());
        channelBuilder.setBierForwardingType(BierForwardingType.BierTe);
        channelBuilder.setEgressNode(getNNHsByAdjacency(bierLink));
        channelBuilder.setBpAssignmentStrategy(bpAssignmentStrategy);
        String channelName = "reset:" + bierLink.getLinkDest().getDestNode() + ":" + bierLink.getLinkDest().getDestTp()
                + ":" + teSubDomain.getSubDomainId().getValue() + ":" + teBsl.getBitstringlength().getIntValue()
                + ":" + teSi.getSi().getValue();
        channelBuilder.setName(channelName);
        return channelBuilder.build();
    }

    public boolean processTeFrrPathUpdate(TeFrrPathUpdate teFrrPathUpdate) {
        BierLink bierLink = new BierLinkBuilder(teFrrPathUpdate.getTeFrrKey().getProtectedLink()).build();
        String linkId = bierLink.getLinkId();
        LOG.info("Update frr protection for bierlink: " + linkId);
        for (SDBslSiBpFrrIndex sdBslSiBpFrrIndex : sdBslSiBpFrrIndexSet) {
            if (sdBslSiBpFrrIndex.getLinkId().equals(linkId) && sdBslSiBpFrrIndex.getTeSubDomain().getSubDomainId()
                    .equals(teFrrPathUpdate.getTeFrrKey().getSubDomainId())) {
                TeDomain teDomain = sdBslSiBpFrrIndex.getTeDomain();
                TeSubDomain teSubDomain = sdBslSiBpFrrIndex.getTeSubDomain();
                TeBsl teBsl = sdBslSiBpFrrIndex.getTeBsl();
                TeSi teSi = sdBslSiBpFrrIndex.getTeSi();
                TeBp teBp = sdBslSiBpFrrIndex.getTeBp();
                BpAssignmentStrategy bpAssignmentStrategy = sdBslSiBpFrrIndex.getBpAssignmentStrategy();
                LOG.info("Configure new frr protection for <{},{},{},{}>",teSubDomain.getSubDomainId().getValue(),
                        teBsl.getBitstringlength(), teSi.getSi().getValue(), teBp.getBitposition());
                if (!processFrrInfo(bierLink, bpAssignmentStrategy, teDomain, teSubDomain, teBsl, teSi, teBp,
                        teFrrPathUpdate.getFrrPath(), FRR_BP_ADD)) {
                    LOG.info("Update te frr path failed!");
                    return false;
                }

                int frrIndex = sdBslSiBpFrrIndex.getFrrIndex();
                FrrIndexAddBitMask frrIndexAddBitMask = new FrrIndexAddBitMask(frrIndex, null, null, null, null);
                Integer frrIndexAddBitMaskUsedTimes = frrIndexAddBitMaskMap.get(frrIndexAddBitMask);
                if (frrIndexAddBitMaskUsedTimes.equals(1)) {
                    frrIndexAddBitMaskMap.remove(frrIndexAddBitMask);
                    Btaft btaft = constructBtaft(null, null, null, frrIndex);
                    bierTeBtaftWriter.writeBierTeBtaft(ConfigurationType.DELETE, bierLink
                            .getLinkSource().getSourceNode(),teFrrPathUpdate.getTeFrrKey().getSubDomainId(), btaft);

                } else {
                    frrIndexAddBitMaskMap.put(frrIndexAddBitMask, --frrIndexAddBitMaskUsedTimes);
                }
            }
        }
        return true;
    }

    private TeFrrPath calTeFrrPath(TeSubDomain teSubDomain, BierLink bierLink) {
        CreateTeFrrPathInputBuilder inputBuilder = new CreateTeFrrPathInputBuilder();
        TeFrrKeyBuilder teFrrKeyBuilder = new TeFrrKeyBuilder();
        teFrrKeyBuilder.setSubDomainId(teSubDomain.getSubDomainId());
        teFrrKeyBuilder.setProtectedLink(new ProtectedLinkBuilder(bierLink).build());
        inputBuilder.setTeFrrKey(teFrrKeyBuilder.build());
        Future<RpcResult<CreateTeFrrPathOutput>> future = rpcConsumerRegistry.getRpcService(BierPceService.class)
                .createTeFrrPath(inputBuilder.build());
        try {
            return future.get().getResult();
        } catch (InterruptedException | ExecutionException e) {
            LOG.info(Arrays.toString(e.getStackTrace()));
        }
        return null;
    }

    private void removeTeFrrPath(TeSubDomain teSubDomain, String linkId) {
        RemoveTeFrrPathInputBuilder inputBuilder = new RemoveTeFrrPathInputBuilder();
        TeFrrKeyBuilder teFrrKeyBuilder = new TeFrrKeyBuilder();
        teFrrKeyBuilder.setSubDomainId(teSubDomain.getSubDomainId());
        BierLink bierLink = util.getBierLinkByLinkId(TOPOLOGY_ID, linkId);
        teFrrKeyBuilder.setProtectedLink(new ProtectedLinkBuilder(bierLink).build());
        inputBuilder.setTeFrrKey(teFrrKeyBuilder.build());
        Future<RpcResult<Void>> future = rpcConsumerRegistry.getRpcService(BierPceService.class).removeTeFrrPath(
                inputBuilder.build());
        try {
            future.get().getResult();
        } catch (InterruptedException | ExecutionException e) {
            LOG.info(Arrays.toString(e.getStackTrace()));
        }
    }

    private TeBsl getBierInBierTeBsl(TeBsl teBsl) {
        //The strategy to determine BIER-in-BIER teBsl for addBitMask can be add here.
        org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain
                .TeBslBuilder teBslBuilder = new org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier
                .te.node.params.te.domain.te.sub.domain.TeBslBuilder();
        teBslBuilder.setBitstringlength(teBsl.getBitstringlength());
        return teBslBuilder.build();
    }

    private TeSi getBierInBierTeSi(TeSi teSi) {
        //The strategy to determine BIER-in-BIER teSi for addBitMask can be add here.
        org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain
                .te.bsl.TeSiBuilder teSiBuilder = new org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102
                .bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiBuilder();
        teSiBuilder.setSi(teSi.getSi());
        return teSiBuilder.build();
    }

    private List<Integer> calMaskValueFromTeInfoList(List<TeInfo> teInfoList) {
        List<Integer> bitMaskList = new ArrayList<>();
        if (null != teInfoList) {
            for (TeInfo teInfo : teInfoList) {
                LOG.info(teInfo.toString());
                TeSubdomain teSubdomain = teInfo.getTeSubdomain().get(0);
                org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.te.subdomain
                        .TeBsl teBsl = teSubdomain.getTeBsl().get(0);
                org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.TeSi teSi =
                        teBsl.getTeSi().get(0);
                int bitstring = teSi.getTeFIndex().get(0).getTeFIndex().getValue();
                bitMaskList.add(bitstring);
            }
        }
        return bitMaskList;
    }

    private TeInfo constructFrrTeInfo(TeSubDomain teSubDomain, TeBsl teBsl, TeSi teSi, TeBp teBp,
                                      Long nodeInterface, List<Integer> resetBitMaskValue, Integer frrIndex) {
        LOG.info("Process build frr te info.");
        TeFIndexBuilder teFIndexBuilder = new TeFIndexBuilder();
        teFIndexBuilder.setFrr(true);
        teFIndexBuilder.setTeFIndex(new BitString(teBp.getBitposition()));
        teFIndexBuilder.setFIntf(nodeInterface);
        if (null != resetBitMaskValue) {
            List<Resetbitmask> resetbitmaskList = new ArrayList<>();
            for (Integer bit : resetBitMaskValue) {
                ResetbitmaskBuilder resetbitmaskBuilder = new ResetbitmaskBuilder();
                resetbitmaskBuilder.setBitmask(new BitString(bit));
                resetbitmaskList.add(resetbitmaskBuilder.build());
            }
            teFIndexBuilder.setResetbitmask(resetbitmaskList);
            teFIndexBuilder.setFrrIndex(frrIndex);
        }
        List<TeFIndex> teFIndexList = new ArrayList<>();
        teFIndexList.add(teFIndexBuilder.build());

        TeSiBuilder teSiBuilder = new TeSiBuilder();
        teSiBuilder.setTeFIndex(teFIndexList);
        teSiBuilder.setSi(teSi.getSi());
        List<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.TeSi> teSiList
                = new ArrayList<>();
        teSiList.add(teSiBuilder.build());

        TeBslBuilder teBslBuilder = new TeBslBuilder();
        teBslBuilder.setTeSi(teSiList);
        teBslBuilder.setFwdBsl(teBsl.getBitstringlength().getIntValue());
        List<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.te.subdomain.TeBsl>
                teBslList = new ArrayList<>();
        teBslList.add(teBslBuilder.build());

        TeSubdomainBuilder subdomainBuilder = new TeSubdomainBuilder();
        subdomainBuilder.setTeBsl(teBslList);
        subdomainBuilder.setSubdomainId(teSubDomain.getSubDomainId());

        List<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomain>
                subdomainList = new ArrayList<>();
        subdomainList.add(subdomainBuilder.build());

        BierTeConfigBuilder builder = new BierTeConfigBuilder();
        builder.setTeSubdomain(subdomainList);

        return builder.build();
    }

    private Btaft constructBtaft(TeBsl teBslAddBitMask, TeSi teSiAddBitMask, List<Integer> addBitMaskValue,
                                 int frrIndex) {
        BtaftBuilder btaftBuilder = new BtaftBuilder();
        if (null != addBitMaskValue) {
            List<Addbitmask> addbitmaskList = new ArrayList<>();
            for (Integer bit : addBitMaskValue) {
                AddbitmaskBuilder addbitmaskBuilder = new AddbitmaskBuilder();
                addbitmaskBuilder.setBitmask(new BitString(bit));
                addbitmaskList.add(addbitmaskBuilder.build());
            }
            btaftBuilder.setAddbitmask(addbitmaskList);
            btaftBuilder.setFrrBsl(teBslAddBitMask.getBitstringlength().getIntValue());
            btaftBuilder.setFrrSi(teSiAddBitMask.getSi().getValue());
        }
        btaftBuilder.setFrrIndex(frrIndex);
        return btaftBuilder.build();
    }

    private boolean persistAddResetChannelToStrategy(Channel channel, TeSubDomain teSubDomain, TeBsl teBsl, TeSi teSi) {
        ChannelNameBferNodeId channelNameBferNodeId = new ChannelNameBferNodeId(channel.getName(),"nh-nnh");
        SubdomainBslSi subdomainBslSi = new SubdomainBslSi(teSubDomain.getSubDomainId().getValue(), teBsl, teSi);
        bpAllocateStrategy.getChannelSDBslSiMap().put(channelNameBferNodeId,subdomainBslSi);
        if (!bpAllocateStrategy.addDeleteChannelBferSubdomainBslSiMap(channel.getName(),"nh-nnh",teSubDomain
                .getSubDomainId().getValue(), teBsl, teSi, TopoBasedBpAllocateStrategy.ADD)) {
            LOG.info("Persist AddReset channel to strategy failed!");
            return false;
        }

        return true;
    }

    private boolean deleteAddResetChannelToStrategy(Channel channel, TeSubDomain teSubDomain, TeBsl teBsl, TeSi teSi) {
        ChannelNameBferNodeId channelNameBferNodeId = new ChannelNameBferNodeId(channel.getName(),"nh-nnh");
        bpAllocateStrategy.getChannelSDBslSiMap().remove(channelNameBferNodeId);
        if (!bpAllocateStrategy.addDeleteChannelBferSubdomainBslSiMap(channel.getName(),"nh-nnh",teSubDomain
                .getSubDomainId().getValue(), teBsl, teSi, TopoBasedBpAllocateStrategy.DELETE)) {
            LOG.info("Remove AddReset channel to strategy failed!");
            return false;
        }
        return true;
    }

    private List<Bfer> calAddBferListFromTeFrrPath(FrrPath frrPath) {
        List<Bfer> bferList = new ArrayList<>();
        NextHopPath nextHopPath = frrPath.getNextHopPath();
        bferList.add(calBferFromBackUpPath(nextHopPath));
        List<NextNextHopPath> nextNextHopPaths = frrPath.getNextNextHopPath();
        for (NextNextHopPath nextNextHopPath : nextNextHopPaths) {
            bferList.add(calBferFromBackUpPath(nextNextHopPath));
        }
        return bferList;
    }

    private List<Bfer> calResetBferListFromTeFrrPath(FrrPath frrPath) {
        List<Bfer> bferList = new ArrayList<>();
        List<ExcludingLink> excludingLink = frrPath.getExcludingLink();
        BierLink frrLink = new BierLinkBuilder(excludingLink.get(0)).build();
        PathLink frrPathLink = new PathLinkBuilder(frrLink).build();
        for (int i = 1;i < excludingLink.size();i++) {
            BierLink nnhLink = new BierLinkBuilder(excludingLink.get(i)).build();
            PathLink nnhPathLink = new PathLinkBuilder(nnhLink).build();
            List<PathLink> pathLinks = new ArrayList<>();
            pathLinks.add(frrPathLink);
            pathLinks.add(nnhPathLink);
            BferBuilder bferBuilder = new BferBuilder();
            BierPathBuilder bierPathBuilder = new BierPathBuilder();
            bierPathBuilder.setPathLink(pathLinks);
            bferBuilder.setBierPath(bierPathBuilder.build());
            bferBuilder.setBferNodeId(nnhLink.getLinkDest().getDestNode());
            bferBuilder.setKey(new BferKey(nnhLink.getLinkDest().getDestNode()));

            bferList.add(bferBuilder.build());
        }
        LOG.info("bfer list" + bferList.size());
        return bferList;
    }

    private Bfer calBferFromBackUpPath(BackupPath backupPath) {
        BferBuilder bferBuilder = new BferBuilder();
        BierPathBuilder bierPathBuilder = new BierPathBuilder();
        List<PathLink> pathLinks = new ArrayList<>();
        for (Path path : backupPath.getPath()) {
            PathLinkBuilder pathLinkBuilder = new PathLinkBuilder(path);
            pathLinks.add(pathLinkBuilder.build());
        }
        bierPathBuilder.setPathLink(pathLinks);
        bferBuilder.setBierPath(bierPathBuilder.build());
        bferBuilder.setBferNodeId(backupPath.getDestinationNode());
        bferBuilder.setKey(new BferKey(backupPath.getDestinationNode()));
        return bferBuilder.build();
    }

    private List<EgressNode> getNNHsByAdjacency(BierLink bierLink) {
        List<EgressNode> nnhNodeList = new ArrayList<>();
        List<BierLink> allBierLinks = util.getBierTopology(TOPOLOGY_ID).getBierLink();
        for (BierLink link : allBierLinks) {
            if (link.getLinkSource().getSourceNode().equals(bierLink.getLinkDest().getDestNode())
                    && !link.getLinkDest().getDestNode().equals(bierLink.getLinkSource().getSourceNode())) {
                EgressNodeBuilder builder = new EgressNodeBuilder();
                builder.setNodeId(link.getLinkDest().getDestNode());
                nnhNodeList.add(builder.build());
            }
        }
        EgressNodeBuilder nhEgressBuilder = new EgressNodeBuilder();
        nhEgressBuilder.setNodeId(bierLink.getLinkDest().getDestNode());
        nnhNodeList.add(nhEgressBuilder.build());
        return nnhNodeList;
    }

    private EgressNode setLocalDecapTpIdForEgressNode(EgressNode egressNode, TeDomain teDomain,
                                                      TeSubDomain teSubDomain, TeBsl teBsl, TeSi teSi) {
        String nodeId = egressNode.getNodeId();
        BierNode targetNode = null;
        List<BierNode> bierNodeList = util.getBierTopology(TOPOLOGY_ID).getBierNode();
        if (null != bierNodeList) {
            for (BierNode bierNode : bierNodeList) {
                if (nodeId.equals(bierNode.getNodeId())) {
                    targetNode = bierNode;
                    break;
                }
            }
        }
        if (null != targetNode) {
            TeSi targetTeSi = util.queryTeSi(TOPOLOGY_ID, targetNode.getNodeId(), teDomain.getDomainId(),
                    teSubDomain.getSubDomainId(), teBsl.getBitstringlength(), teSi.getSi());
            if (null != targetTeSi) {
                for (TeBp teBp : targetTeSi.getTeBp()) {
                    String tpId = teBp.getTpId();
                    if (null == util.getBierLinkByNodeIdAndTpId(TOPOLOGY_ID, targetNode.getNodeId(), tpId)) {
                        EgressNodeBuilder egressNodeBuilder = new EgressNodeBuilder(egressNode);
                        List<RcvTp> rcvTpList = new ArrayList<>();
                        RcvTpBuilder rcvTpBuilder = new RcvTpBuilder();
                        rcvTpBuilder.setTp(tpId);
                        rcvTpList.add(rcvTpBuilder.build());
                        egressNodeBuilder.setRcvTp(rcvTpList);
                        return egressNodeBuilder.build();
                    }
                }
            }
        }
        return egressNode;
    }

    public static AddResetBitMaskProcess getInstance() {
        return instance;
    }

    public void setBitStringProcess(BitStringProcess bitStringProcess) {
        this.bitStringProcess = bitStringProcess;
    }

    public void setBiftInfoProcess(BiftInfoProcess biftInfoProcess) {
        this.biftInfoProcess = biftInfoProcess;
    }

    public void setBierTeBiftWriter(BierTeBiftWriter bierTeBiftWriter) {
        this.bierTeBiftWriter = bierTeBiftWriter;
    }

    public void setBierTeBtaftWriter(BierTeBtaftWriter bierTeBtaftWriter) {
        this.bierTeBtaftWriter = bierTeBtaftWriter;
    }

    public void setRpcConsumerRegistry(RpcConsumerRegistry rpcConsumerRegistry) {
        this.rpcConsumerRegistry = rpcConsumerRegistry;
    }

    public void setUtil(Util util) {
        this.util = util;
    }

    public void setBpAllocateStrategy(AbstractBPAllocateStrategy bpAllocateStrategy) {
        this.bpAllocateStrategy = bpAllocateStrategy;
    }

    private class FrrIndexAddBitMask {
        private Integer frrIndex;
        private Integer subdomainValue;
        private Integer bslValue;
        private Integer siValue;
        private List<Integer> addBitMask;

        private FrrIndexAddBitMask(Integer frrIndex, Integer subdomainValue,
                                   Integer bslValue, Integer siValue, List<Integer> addBitMask) {
            this.frrIndex = frrIndex;
            this.subdomainValue = subdomainValue;
            this.bslValue = bslValue;
            this.siValue = siValue;
            this.addBitMask = addBitMask;
        }

        public Integer getFrrIndex() {
            return frrIndex;
        }

        public void setFrrIndex(Integer frrIndex) {
            this.frrIndex = frrIndex;
        }

        public Integer getBslValue() {
            return bslValue;
        }

        public Integer getSiValue() {
            return siValue;
        }

        public List<Integer> getAddBitMask() {
            return addBitMask;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            if (null == obj) {
                return false;
            }
            if (obj instanceof FrrIndexAddBitMask) {
                FrrIndexAddBitMask other = (FrrIndexAddBitMask)obj;
                if (null != other.getFrrIndex()) {
                    return this.frrIndex.equals(other.getFrrIndex());
                } else {
                    return this.subdomainValue.equals(other.subdomainValue)
                            && this.bslValue.equals(other.getBslValue())
                            && this.siValue.equals(other.getSiValue())
                            && this.addBitMask.containsAll(other.getAddBitMask())
                            && other.getAddBitMask().containsAll(this.addBitMask);
                }
            }
            return false;
        }
    }

    private class SDBslSiBpFrrIndex {
        private String linkId;
        private TeDomain teDomain;
        private TeSubDomain teSubDomain;
        private TeBsl teBsl;
        private TeSi teSi;
        private TeBp teBp;
        private BpAssignmentStrategy bpAssignmentStrategy;
        private Integer frrIndex;

        private SDBslSiBpFrrIndex(String linkId, TeDomain teDomain, TeSubDomain teSubDomain, TeBsl teBsl, TeSi teSi,
                                  TeBp teBp, BpAssignmentStrategy bpAssignmentStrategy, Integer frrIndex) {
            this.linkId = linkId;
            this.teDomain = teDomain;
            this.teSubDomain = teSubDomain;
            this.teBsl = teBsl;
            this.teSi = teSi;
            this.teBp = teBp;
            this.bpAssignmentStrategy = bpAssignmentStrategy;
            this.frrIndex = frrIndex;
        }

        public String getLinkId() {
            return linkId;
        }

        public TeDomain getTeDomain() {
            return teDomain;
        }

        public TeSubDomain getTeSubDomain() {
            return teSubDomain;
        }

        public TeBsl getTeBsl() {
            return teBsl;
        }

        public TeSi getTeSi() {
            return teSi;
        }

        public TeBp getTeBp() {
            return teBp;
        }

        public BpAssignmentStrategy getBpAssignmentStrategy() {
            return bpAssignmentStrategy;
        }

        public Integer getFrrIndex() {
            return frrIndex;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            if (null == obj) {
                return false;
            }
            if (obj instanceof SDBslSiBpFrrIndex) {
                SDBslSiBpFrrIndex other = (SDBslSiBpFrrIndex)obj;
                return this.linkId.equals(other.getLinkId())
                        && this.teSubDomain.equals(other.getTeSubDomain())
                        && this.teBsl.equals(other.getTeBsl())
                        && this.teSi.equals(other.getTeSi());
            }
            return false;
        }
    }
}
