/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.teconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.service.impl.NotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPathUpdate;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceService;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.te.subdomain.TeBsl;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BitStringProcess {

    private static final Logger LOG = LoggerFactory.getLogger(BitStringProcess.class);
    private final DataBroker dataBroker;
    private final RpcConsumerRegistry rpcConsumerRegistry;
    private NotificationProvider notificationProvider;
    private BiftInfoProcess biftInfoProcess;
    private BierTeBitstringWriter bierTeBitstringWriter;
    private BitStringDB bitStringDB;
    private long pathIdLocked = 0;

    public static final int PATH_ADD = 1;
    public static final int PATH_REMOVE = 2;
    public static final int PATH_REMOVE_ALL = 3;

    public BitStringProcess(final DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry,
                            BierTeBitstringWriter bierTeBitstringWriter,
                            BierTeBiftWriter bierTeBiftWriter) {
        this.dataBroker = dataBroker;
        this.notificationProvider = new NotificationProvider();
        this.rpcConsumerRegistry = rpcConsumerRegistry;
        this.bierTeBitstringWriter = bierTeBitstringWriter;
        this.biftInfoProcess = new BiftInfoProcess(dataBroker,rpcConsumerRegistry,
                bierTeBiftWriter, bierTeBitstringWriter);
        this.bitStringDB = new BitStringDB(dataBroker);
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
                    if (setBitStringConfigToSouthbound(channel.getIngressNode(), ConfigurationType.DELETE,
                            tePathList.get(0))) {
                        return bitStringDB.delBitStringToDataStore(channel.getName());
                    }
                }
            } else {
                LOG.error("Remove the channel computation failed!");
            }
        }
        LOG.info("Process create bier te path!");
        List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer>  bferList = computePathByPce(
                channel,pathComputeType);
        LOG.info("Process bit string!" + bferList);
        return processBierTePath(channelCurrent,bferList);
    }

    public boolean updateBitStringList(Channel channel, BierPathUpdate bierPath) {
        if (null == bierPath) {
            return false;
        }
        if (!bitStringDB.checkChannelPathExisted(channel.getName())) {
            LOG.error("No such channel!");
            return false;
        }

        List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer> bferList = bierPath.getBfer();
        return processBierTePath(channel,bferList);
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
        TePath tePath = getTePathFromTeInfo(teInfoList);
        LOG.info("tePath: " + tePath);
        boolean result = false;
        LOG.info("channel: " + channelCurrent.getName());
        if (bitStringDB.checkChannelPathExisted(channelCurrent.getName())) {
            LOG.info("Modify!");
            result = setBitStringConfigToSouthbound(channelCurrent.getIngressNode(),ConfigurationType.MODIFY,tePath);
        } else {
            LOG.info("Add!");
            LOG.info("tePath: " + tePath);
            result = setBitStringConfigToSouthbound(channelCurrent.getIngressNode(),ConfigurationType.ADD,tePath);
        }
        if (true == result) {
            LOG.info("Set bitString to data store");
            return bitStringDB.setBitStringToDataStore(channelCurrent,tePath);
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

    private TePath getTePathFromTeInfo(List<TeInfo> teInfoList) {
        if (null == teInfoList || teInfoList.isEmpty()) {
            return null;
        }
        List<BitString> bitStringList = new ArrayList<>();
        for (TeInfo teInfo : teInfoList) {
            BitString bitString = teInfo.getTeSubdomain().get(0).getTeBsl().get(0).getTeSi().get(0).getTeFIndex()
                    .get(0).getTeFIndex();
            bitStringList.add(bitString);
        }
        if (null != bitStringList && !bitStringList.isEmpty()) {
            TeBsl teBsl = teInfoList.get(0).getTeSubdomain().get(0).getTeBsl().get(0);
            TeSi teSi = teBsl.getTeSi().get(0);
            LOG.info("fwd bsl is: " + teBsl.getFwdBsl());
            return constructTePath(teInfoList.get(0).getTeSubdomain().get(0).getSubdomainId(),
                    teBsl.getFwdBsl(),teSi.getSi(),bitStringList);
        }
        return null;
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

    private CreateBierPathOutput createBierPathRpc(Channel channel) {

        CreateBierPathInputBuilder createBierPathInputBuilder = new CreateBierPathInputBuilder();
        createBierPathInputBuilder.setBfirNodeId(channel.getIngressNode());
        List<Bfer> bferList = getBferListFromEgressNode(channel.getEgressNode());
        if (null != bferList && !bferList.isEmpty()) {
            createBierPathInputBuilder.setBfer(bferList);
        }
        createBierPathInputBuilder.setChannelName(channel.getName());

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

    private List<Bfer> getBferListFromEgressNode(List<EgressNode> egressNodeList) {

        List<Bfer> bferList = new ArrayList<>();
        for (EgressNode egressNode : egressNodeList) {
            BferBuilder bferBuilder = new BferBuilder();
            bferBuilder.setBferNodeId(egressNode.getNodeId());
            bferList.add(bferBuilder.build());
        }
        return bferList;
    }

    private List<TeInfo> getTeInfoFrombferList(Channel channelCurrent,
                         List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer> bferList) {
        List<String> tpList = new ArrayList<>();
        List<String> nodeList = new ArrayList<>();
        for (org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer bfer : bferList) {
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
            for (RcvTp rcvTp : egressNode.getRcvTp()) {
                nodeList.add(egressNode.getNodeId());
                tpList.add(rcvTp.getTp());
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
            List<TeInfo> teInfo = biftInfoProcess.getBiftTeInfoFromInput(channel.getDomainId(),channel.getSubDomainId(),
                    node,tpList.get(i));
            if (null != teInfo && !teInfo.isEmpty()) {
                for (TeInfo teinfo : teInfo) {
                    teInfoList.add(teinfo);
                }
            }
        }
        return teInfoList;
    }

    private boolean checkNodeTpExisted(List<String> tpList,List<String> nodeList,String tpId,String nodeId) {
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
