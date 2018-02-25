/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.teconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.service.impl.NotificationProvider;
import org.opendaylight.bier.service.impl.allocatebp.BPAllocateStrategy;
import org.opendaylight.bier.service.impl.allocatebp.TopoBasedBpAllocateStrategy;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BpAssignmentStrategy;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPathUpdate;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceService;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.TePath;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.PathKey;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.te.path.Bitstring;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.te.path.BitstringBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.te.path.BitstringKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.TeInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.TeSi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.te.subdomain.TeBsl;
import org.opendaylight.yangtools.yang.common.RpcResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitStringProcess {

    private static final Logger LOG = LoggerFactory.getLogger(BitStringProcess.class);
    private final RpcConsumerRegistry rpcConsumerRegistry;
    private NotificationProvider notificationProvider;
    private BiftInfoProcess biftInfoProcess;
    private BierTeBitstringWriter bierTeBitstringWriter;
    private BitStringDB bitStringDB;
    private BPAllocateStrategy bpAllocateStrategy;

    private long pathIdLocked = 0;

    public static final int PATH_ADD = 1;
    public static final int PATH_REMOVE = 2;
    public static final int PATH_REMOVE_ALL = 3;

    public BitStringProcess(final DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry,
                            BierTeBitstringWriter bierTeBitstringWriter,
                            BierTeBiftWriter bierTeBiftWriter) {
        this.notificationProvider = new NotificationProvider();
        this.rpcConsumerRegistry = rpcConsumerRegistry;
        this.bierTeBitstringWriter = bierTeBitstringWriter;
        this.biftInfoProcess = new BiftInfoProcess(dataBroker, bierTeBiftWriter);
        this.bitStringDB = new BitStringDB(dataBroker);
        this.bpAllocateStrategy = TopoBasedBpAllocateStrategy.getInstance();
//        this.bpAllocateStrategy = PathMonopolyBPAllocateStrategy.getInstance();
    }

    public boolean bierTeBitStringProcess(Channel channel,Channel channelCurrent,int pathComputeType) {
        if (!checkPathComputeInput(channel)) {
            LOG.error("Path computation input error!");
            return false;
        }
        if (PATH_REMOVE_ALL == pathComputeType) {
            LOG.info("Remove the channel!");
            RemoveBierPathOutput output = removeBierPathRpc(channel,PATH_REMOVE_ALL);
            if (null != output && null == output.getBfer()) {
                List<TePath> tePathList = bitStringDB.getTePathFromChannel(channel.getName());
                if (tePathList != null && !tePathList.isEmpty()) {
                    LOG.info("Recycle all bps of the channel");
                    List<Bfer> bferList = bpAllocateStrategy.getBferListOfChannel(channel.getName());

                    if (channel.getBpAssignmentStrategy().equals(BpAssignmentStrategy.Automatic)) {
                        if (!bpAllocateStrategy.recycleBPs(channel,bferList)) {
                            webSocketToApp("Recycle bp failed!");
                            return false;
                        }
                        bpAllocateStrategy.removeBferListToChannel(channel.getName());
                    }

                    for (TePath tePath:tePathList) {
                        setBitStringConfigToSouthbound(channel.getIngressNode(), ConfigurationType.DELETE, tePath);
                    }

                    return bitStringDB.delBitStringToDataStore(channel.getName());
                }
            } else {
                LOG.error("Remove the channel computation failed!");
            }
        }
        LOG.info("Process calculate bier te path!");
        List<Bfer> bferList = computePathByPce(channel,pathComputeType);

        if (channel.getBpAssignmentStrategy().equals(BpAssignmentStrategy.Automatic)) {
            List<Bfer> oldBferList = bpAllocateStrategy.getBferListOfChannel(channel.getName());
            if (!processChannelPathChange(channel, channelCurrent, bferList, oldBferList)) {
                return false;
            }
        }

        return processBierTePath(channelCurrent,bferList);
    }

    private List<Bfer> deepCopy(List<Bfer> sourceList) {
        List<Bfer> bfers = new ArrayList<>();
        for (Bfer bfer:sourceList) {
            org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder bferBuilder =
                    new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder(bfer);
            bfers.add(bferBuilder.build());
        }
        return bfers;
    }

    public List<Bfer> notChangeBferList(List<Bfer> bferList,List<Bfer> oldBferList) {
        List<Bfer> bfers = new ArrayList<>();
        if (null != oldBferList) {
            for (Bfer bfer:bferList) {
                if (oldBferList.contains(bfer)) {
                    bfers.add(bfer);
                }
            }
        }
        return bfers;
    }

    public boolean updateBitStringList(Channel channel, BierPathUpdate bierPath) {
        if (null == bierPath) {
            return false;
        }
        if (!bitStringDB.checkChannelPathExisted(channel.getName())) {
            LOG.error("No such channel!");
            return false;
        }

        List<Bfer> bferList = bierPath.getBfer();

        if (channel.getBpAssignmentStrategy().equals(BpAssignmentStrategy.Automatic)) {
            List<Bfer> oldBferList = bpAllocateStrategy.getBferListOfChannel(channel.getName());
            if (!processChannelPathChange(channel, channel, bferList, oldBferList)) {
                return false;
            }
        }

        return processBierTePath(channel,bferList);
    }

    private boolean processChannelPathChange(Channel channel, Channel currentChannel, List<Bfer> bferList,
                                             List<Bfer> oldBferList) {
        List<Bfer> tmpBferList = deepCopy(bferList);
        List<Bfer> notChangeBferList = notChangeBferList(tmpBferList,oldBferList);
        if (null != oldBferList) {
            LOG.info("Process remove odl bfer list of channel");
            oldBferList.removeAll(notChangeBferList);
            if (!bpAllocateStrategy.recycleBPs(channel,oldBferList)) {
                webSocketToApp("Recycle bp failed!");
                return false;
            }
        }
        tmpBferList.removeAll(notChangeBferList);
        LOG.info("Process add new bfer list of channel");
        if (!bpAllocateStrategy.allocateBPs(currentChannel,tmpBferList)) {
            webSocketToApp("Allocate bp failed!");
            return false;
        }
        LOG.info("Process bit string!" + bferList);
        bpAllocateStrategy.setBferListToChannel(channel.getName(), bferList);
        return true;
    }

    private boolean processBierTePath(Channel channelCurrent,List<
            org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer>  bferList) {
        if (null == bferList || bferList.isEmpty()) {
            LOG.info("Path computation result is null");
            return false;
        }
        List<TeInfo> teInfoList = getTeInfoFrombferList(channelCurrent,bferList);
        if (null == teInfoList || teInfoList.isEmpty()) {
            LOG.info("Get TeInfo from BiftInfoProcess failed!");
            return false;
        }

        List<TePath> tePathList = getTePathFromTeInfo(teInfoList);
        LOG.info("tePath: " + tePathList);
        boolean result = false;
        LOG.info("channel: " + channelCurrent.getName());
        if (bitStringDB.checkChannelPathExisted(channelCurrent.getName())) {
            LOG.info("Modify!");
            for (TePath tePath : tePathList) {
                result = setBitStringConfigToSouthbound(channelCurrent.getIngressNode(),
                        ConfigurationType.MODIFY,tePath);
            }
        } else {
            LOG.info("Add!");
            for (TePath tePath : tePathList) {
                result = setBitStringConfigToSouthbound(channelCurrent.getIngressNode(),
                        ConfigurationType.ADD,tePath);
            }
        }
        if (true == result) {
            LOG.info("Set bitString to data store");
            return bitStringDB.setBitStringToDataStore(channelCurrent,tePathList);
        }
        return false;
    }


    public List<Long> getPathIdList(String channelName) {
        List<Long> pathIdList = new ArrayList<>();
        List<TePath> tePathList = bitStringDB.getTePathFromChannel(channelName);
        if (null != tePathList && !tePathList.isEmpty()) {
            for (TePath tePath : tePathList) {
                pathIdList.add(tePath.getPathId());
            }
        }
        return pathIdList;
    }

    private List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer> computePathByPce(
            Channel channel,int pathComputeType) {
        if (pathComputeType == PATH_ADD) {
            LOG.info("Add path");
            return createBierPathRpc(channel).getBfer();
        } else if (pathComputeType == PATH_REMOVE) {
            LOG.info("Remove path");
            return removeBierPathRpc(channel,PATH_REMOVE).getBfer();
        }
        return null;
    }

    private List<TePath> getTePathFromTeInfo(List<TeInfo> teInfoList) {
        if (null == teInfoList || teInfoList.isEmpty()) {
            return null;
        }
        Map<TeSubdomainBslSiKey,List<BitString>> tePathBitstringMap = new HashMap<>();
        for (TeInfo teInfo : teInfoList) {
            TeSubdomain teSubdomain = teInfo.getTeSubdomain().get(0);
            TeBsl teBsl = teSubdomain.getTeBsl().get(0);
            TeSi teSi = teBsl.getTeSi().get(0);
            BitString bitstring = teSi.getTeFIndex().get(0).getTeFIndex();
            TeSubdomainBslSiKey teSubdomainBslSiKey = new TeSubdomainBslSiKey(teSubdomain,teBsl,teSi);

            List<BitString> bitStringList = tePathBitstringMap.get(teSubdomainBslSiKey);
            if (null == bitStringList) {
                bitStringList = new ArrayList<>();
                tePathBitstringMap.put(teSubdomainBslSiKey,bitStringList);
            }
            bitStringList.add(bitstring);

        }

        List<TePath> tePathList = new ArrayList<>();
        if (!tePathBitstringMap.isEmpty()) {
            Set<TeSubdomainBslSiKey> teSubdomainBslSiKeySet = tePathBitstringMap.keySet();
            for (TeSubdomainBslSiKey teSubdomainBslSiKey : teSubdomainBslSiKeySet) {
                List<BitString> bitStringList = tePathBitstringMap.get(teSubdomainBslSiKey);
                TePath tePath = constructTePath(teSubdomainBslSiKey.getTeSubDomain().getSubdomainId(),
                        teSubdomainBslSiKey.getTeBsl().getFwdBsl(),teSubdomainBslSiKey.getTeSi().getSi(),bitStringList);
                tePathList.add(tePath);
            }
        }

        return tePathList;
    }

    private class TeSubdomainBslSiKey {
        private TeSubdomain teSubDomain;
        private TeBsl teBsl;
        private TeSi teSi;

        TeSubdomainBslSiKey(TeSubdomain teSubDomain, TeBsl teBsl, TeSi teSi) {
            this.teSubDomain = teSubDomain;
            this.teBsl = teBsl;
            this.teSi = teSi;
        }

        public TeSubdomain getTeSubDomain() {
            return teSubDomain;
        }

        public TeBsl getTeBsl() {
            return teBsl;
        }

        public TeSi getTeSi() {
            return teSi;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            TeSubdomainBslSiKey other = (TeSubdomainBslSiKey)obj;
            boolean subdomainEquals = this.getTeSubDomain().getKey().equals(other.getTeSubDomain().getKey());
            boolean bslEquals = this.getTeBsl().getKey().equals(other.getTeBsl().getKey());
            boolean siEquals = this.getTeSi().getKey().equals(other.getTeSi().getKey());
            return subdomainEquals && bslEquals && siEquals;
        }
    }

    private TePath constructTePath(SubDomainId subDomainId, Integer bslLen, Si si, List<BitString> bitStringList) {

        PathBuilder pathBuilder = new PathBuilder();
        Long pathId = createPathId();
        LOG.info("pathId is " + pathId);
        pathBuilder.setPathId(pathId);
        pathBuilder.setKey(new PathKey(pathId));
        pathBuilder.setSubdomainId(subDomainId);
        LOG.info("bsl: " + bslLen.intValue());
        pathBuilder.setBitstringlength(queryBslByInteger(bslLen));
        LOG.info("bsl: " + queryBslByInteger(bslLen));
        pathBuilder.setSi(si);
        List<Bitstring> bitstringList = new ArrayList<>();
        for (BitString bitString : bitStringList) {
            BitstringBuilder bitstringBuilder =  new BitstringBuilder();
            bitstringBuilder.setBitposition(bitString);
            bitstringBuilder.setKey(new BitstringKey(bitString));
            bitstringList.add(bitstringBuilder.build());
        }
        pathBuilder.setBitstring(bitstringList);

        return pathBuilder.build();
    }

    private Bsl queryBslByInteger(Integer bslLen) {
        for (Bsl enumItem : Bsl.values()) {
            String[] bslName = enumItem.getName().split("-");
            if (bslName[0].equals(bslLen.toString())) {
                return enumItem;
            }
        }
        return null;
    }

    private Long createPathId() {
        long ltime = System.currentTimeMillis() / 1000;
        if (ltime > pathIdLocked) {
            pathIdLocked = ltime;
        } else {
            pathIdLocked = pathIdLocked + 1;
            ltime = pathIdLocked;
        }
        return new Long(ltime);
    }

    private boolean setBitStringConfigToSouthbound(String bfir,ConfigurationType type,TePath tePath) {
        if (null == bfir || null == tePath) {
            return false;
        }
        LOG.info("Process send BitString to southbound");
        ConfigurationResult result = bierTeBitstringWriter.writeBierTeBitstring(type,bfir,tePath);
        if (!result.isSuccessful()) {
            webSocketToApp(result.getFailureReason());
            return false;
        }
        return true;
    }

    public CreateBierPathOutput createBierPathRpc(Channel channel) {

        CreateBierPathInputBuilder createBierPathInputBuilder = new CreateBierPathInputBuilder();
        createBierPathInputBuilder.setBfirNodeId(channel.getIngressNode());
        List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer> bferList
                = getBferListFromEgressNode(channel.getEgressNode());
        if (null != bferList && !bferList.isEmpty()) {
            createBierPathInputBuilder.setBfer(bferList);
        }
        createBierPathInputBuilder.setChannelName(channel.getName());
        createBierPathInputBuilder.setSubDomainId(channel.getSubDomainId());

        LOG.info("Rpc create bier path");
        Future<RpcResult<CreateBierPathOutput>> future = rpcConsumerRegistry.getRpcService(BierPceService.class)
                .createBierPath(createBierPathInputBuilder.build());
        try {
            LOG.info("Add path computation!");
            CreateBierPathOutput output = future.get().getResult();
            if (null != output) {
                LOG.info("Path computation succeed! " + output);
                return output;
            }
            LOG.error("Path computation failed!");
            return null;

        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Path computation is Interrupted by", e);
        }
        return null;
    }

    private RemoveBierPathOutput removeBierPathRpc(Channel channel, int type) {

        RemoveBierPathInputBuilder removeBierPathInputBuilder = new RemoveBierPathInputBuilder();
        removeBierPathInputBuilder.setChannelName(channel.getName());
        removeBierPathInputBuilder.setSubDomainId(channel.getSubDomainId());
        removeBierPathInputBuilder.setBfirNodeId(channel.getIngressNode());
        if (PATH_REMOVE == type) {
            List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input.Bfer> bferList =
                    getBferListFromEgressNodeRemove(channel.getEgressNode());
            if (null != bferList && !bferList.isEmpty()) {
                removeBierPathInputBuilder.setBfer(bferList);
            }
        }
        Future<RpcResult<RemoveBierPathOutput>>  future = rpcConsumerRegistry.getRpcService(BierPceService.class)
                .removeBierPath(removeBierPathInputBuilder.build());
        try {
            LOG.info("Remove path computation!");
            RemoveBierPathOutput output = future.get().getResult();
            if (null != output) {
                LOG.info("Path computation succeed!");
                return output;
            }
            LOG.info("Path computation failed or remove all!");
            return null;

        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Path computation is Interrupted by", e);
        }
        return null;
    }

    private List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input.Bfer>
        getBferListFromEgressNodeRemove(List<EgressNode> egressNodeList) {

        List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input.Bfer> bferList =
                new ArrayList<>();
        for (EgressNode egressNode : egressNodeList) {
            org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input.BferBuilder bferBuilder =
                    new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input.BferBuilder();
            bferBuilder.setBferNodeId(egressNode.getNodeId());
            bferList.add(bferBuilder.build());
        }
        return bferList;
    }

    private List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer>
        getBferListFromEgressNode(List<EgressNode> egressNodeList) {

        List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer> bferList =
                new ArrayList<>();
        for (EgressNode egressNode : egressNodeList) {
            BferBuilder bferBuilder = new BferBuilder();
            bferBuilder.setBferNodeId(egressNode.getNodeId());
            bferList.add(bferBuilder.build());
        }
        return bferList;
    }

    public List<TeInfo> getTeInfoFrombferList(Channel channelCurrent, List<Bfer> bferList) {
        List<String> tpList = new ArrayList<>();
        List<String> nodeList = new ArrayList<>();
        for (Bfer bfer : bferList) {
            List<PathLink> pathLinkList = bfer.getBierPath().getPathLink();
            for (PathLink pathLink : pathLinkList) {
                if (checkNodeTpExisted(tpList,nodeList,pathLink.getLinkDest().getDestNode(),pathLink.getLinkDest()
                        .getDestTp())) {
                    continue;
                }
                nodeList.add(pathLink.getLinkDest().getDestNode());
                tpList.add(pathLink.getLinkDest().getDestTp());
            }
        }
        for (EgressNode egressNode :channelCurrent.getEgressNode()) {
            if (null != egressNode.getRcvTp()) {
                for (RcvTp rcvTp : egressNode.getRcvTp()) {
                    nodeList.add(egressNode.getNodeId());
                    tpList.add(rcvTp.getTp());
                }
            }
        }
        return getTeInfoFromNodeTp(channelCurrent,nodeList,tpList);
    }

    private List<TeInfo>  getTeInfoFromNodeTp(Channel channel,List<String> nodeIdList,List<String> tpList) {
        if (null == nodeIdList || nodeIdList.isEmpty()
                || null == tpList || tpList.isEmpty() || nodeIdList.size() != tpList.size()) {
            return null;
        }
        LOG.info("node list: " + nodeIdList + ", tp list: " + tpList);
        List<TeInfo> teInfoList = new ArrayList<>();
        for (int i = 0;i < tpList.size(); i++) {
            BierNode node = biftInfoProcess.getBierNodeById(nodeIdList.get(i));
            List<TeInfo> teInfo = biftInfoProcess.getBiftTeInfoFromInput(bpAllocateStrategy, channel,
                    node, tpList.get(i));
            if (null != teInfo && !teInfo.isEmpty()) {
                for (TeInfo teinfo : teInfo) {
                    teInfoList.add(teinfo);
                }
            }
        }
        return teInfoList;
    }

    private boolean checkNodeTpExisted(List<String> tpList,List<String> nodeList,String nodeId,String tpId) {
        if (null != tpList && !tpList.isEmpty() && null != nodeList && !nodeList.isEmpty()) {
            for (int i = 0;i < tpList.size(); i++) {
                if (tpList.get(i).equals(tpId) && nodeList.get(i).equals(nodeId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkPathComputeInput(Channel channel) {
        if (null == channel) {
            return false;
        }
        if (null == channel.getIngressNode()) {
            return false;
        }
        if (null == channel.getEgressNode() || channel.getEgressNode().isEmpty()) {
            return false;
        }
        return true;
    }

    private void webSocketToApp(String failureReason) {
        notificationProvider.notifyFailureReason(failureReason);
    }

}
