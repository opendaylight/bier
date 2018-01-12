/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.oam.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResultBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverFailure;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverNotifyBierEchoReply;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverReporterListener;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.BierOamApiService;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.CheckType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.ModeType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.NetworkType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.StartEchoRequestInput;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.StartEchoRequestOutput;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.StartEchoRequestOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.start.echo.request.input.TargetNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.BierServiceApiService;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringInput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringOutput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.input.TargetNodeIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.AddressInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.address.info.BierTe;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.BierAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.ReplyMode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.bier.address.Bfrs;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.bier.address.bfrs.BierBfers;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.Bitstring;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.bitstring.Bitpositions;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.rmi.runtime.Log;


public class OamImpl implements BierOamApiService, DriverReporterListener {
    private static final Logger LOG = LoggerFactory.getLogger(OamImpl.class);
    private DbProvider dbProvider = DbProvider.getInstance();
    protected ConcurrentHashMap<OamInstanceKey, OamInstance> oamInstanceMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<SingleOamRequestKey, Set<OamInstance>> requestInstanceMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<SingleOamRequestKey,SingleOamRequest> singleRequestMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<AddressInfo,SingleOamRequest> bierTeAddressMap = new ConcurrentHashMap<>();
    protected ExecutorService executor = Executors.newFixedThreadPool(1);
    private static OamImpl instance ;
    protected BierServiceApiService bierService;

    public OamImpl() {
        instance = this;
    }

    public static OamImpl getInstance() {
        return instance;
    }

    public void setBierService(BierServiceApiService bierService) {
        this.bierService = bierService;
    }

    @Override
    public Future<RpcResult<StartEchoRequestOutput>> startEchoRequest(StartEchoRequestInput input) {
        LOG.info("start echo request :" + input);
        StartEchoRequestOutputBuilder output = new StartEchoRequestOutputBuilder();
        output.setConfigureResult(buildConfigResult(ConfigureResult.Result.SUCCESS,""));
        String checkResult = checkInput(input);
        if (checkResult != null) {
            output.setConfigureResult(buildConfigResult(ConfigureResult.Result.FAILURE,checkResult));
        }
        OamInstance oamInstance = getNodeInstance(input);
        ConfigurationResult result = oamInstance.processOamRequest();
        if (!result.isSuccessful()) {
            output.setConfigureResult(buildConfigResult(ConfigureResult.Result.FAILURE,result.getFailureReason()));
        }
        return Futures.immediateFuture(RpcResultBuilder.<StartEchoRequestOutput>success(output.build()).build());
    }

    public void removeFromOamInstanceMap(OamInstanceKey oamInstanceKey) {
        oamInstanceMap.remove(oamInstanceKey);
    }

    public void removeFromSingleRequestMap(SingleOamRequestKey key) {
        singleRequestMap.remove(key);
    }

    public void removeFromBierTeAddressMap(SingleOamRequest oamRequest) {
        for (Map.Entry<AddressInfo,SingleOamRequest> entry : bierTeAddressMap.entrySet()) {
            if (entry.getValue().equals(oamRequest)) {
                bierTeAddressMap.remove(entry.getKey());
                break;
            }
        }

    }

    public SingleOamRequest getSingleRequest(SingleOamRequestKey key) {
        if (key != null) {
            return singleRequestMap.get(key);
        }
        return null;
    }

    public SingleOamRequest getSingleRequest(AddressInfo addressInfo) {
        //return bierTeAddressMap.get(addressInfo);
        for (Map.Entry<AddressInfo,SingleOamRequest> entry : bierTeAddressMap.entrySet()) {
            if (entry.getKey() instanceof BierTe && addressInfo instanceof BierTe) {
                LOG.info("bierTeAddressMap key display:" + entry.getKey());
                if (((BierTe) entry.getKey()).getReplyModeTe().equals(((BierTe) addressInfo).getReplyModeTe())
                        && ((BierTe) entry.getKey()).getBierTeSubdomainid()
                        .equals(((BierTe) addressInfo).getBierTeSubdomainid())
                        && compareBierTeBpInfo(((BierTe) entry.getKey()).getBierTeBpInfo(),
                        ((BierTe) addressInfo).getBierTeBpInfo())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private boolean compareBierTeBpInfo(List<BierTeBpInfo> bierTeBpInfo, List<BierTeBpInfo> bierTeBpInfo1) {
        //Integer result = 0;
        LOG.info("compareBierTeBpInfo inMap: " + bierTeBpInfo + ", in notification: " + bierTeBpInfo1);
        if (bierTeBpInfo.size() == bierTeBpInfo1.size()) {
            if (bierTeBpInfo.get(0).getBierTeBsl().equals(bierTeBpInfo1.get(0).getBierTeBsl())) {
                if (bierTeBpInfo.get(0).getBitstring().get(0).getSi()
                        .equals(bierTeBpInfo1.get(0).getBitstring().get(0).getSi())) {
                    List<Bitpositions> bitPositions = bierTeBpInfo.get(0).getBitstring().get(0).getBitpositions();
                    List<Bitpositions> bitpositions1 = bierTeBpInfo1.get(0).getBitstring().get(0).getBitpositions();
                    return (bitPositions.size() == bitpositions1.size()) && (bitpositions1.containsAll(bitPositions));
                }
            }
/*            for (BierTeBpInfo teBpInfo : bierTeBpInfo) {
                for (BierTeBpInfo teBpInfo1 :bierTeBpInfo1) {
                    if (teBpInfo.getBierTeBsl().equals(teBpInfo1.getBierTeBsl())
                            && teBpInfo.getBitstring().size() == teBpInfo1.getBitstring().size()) {
                        for (Bitstring bitstring : teBpInfo.getBitstring()) {
                            if (teBpInfo1.getBitstring().contains(bitstring)) {
                                result++;
                            }
                        }
                        if (result == teBpInfo.getBitstring().size()) {
                            LOG.info("BIerTeBpInfo compare success!");
                            return true;
                        }
                    }
                }
            }*/
        }
        return false;
    }

    public ConcurrentHashMap<SingleOamRequestKey, Set<OamInstance>> getRequestInstanceMap() {
        return this.requestInstanceMap;
    }

    public ConcurrentHashMap<AddressInfo,SingleOamRequest> getBierTeAddressMap() {
        return this.bierTeAddressMap;
    }

    private OamInstance getNodeInstance(StartEchoRequestInput input) {
        Channel channel = dbProvider.readChannel(input.getChannelName(),input.getTopologyId());
        SubDomainId subdomain = channel.getSubDomainId();
        String ingressNode = channel.getIngressNode();
        BfrId bfir = channel.getIngressBfrId();
        List<BfrId> egressBfrs = getEgressBfrIds(channel);
        List<String> egressNodes = getEgressNodes(channel);
        NetworkType networkType = NetworkType.forValue(channel.getBierForwardingType().getIntValue() - 1);
        Integer maxTtl = input.getMaxTtl() == null ? 255 : input.getMaxTtl();
        OamInstanceKey key = new OamInstanceKey(subdomain,ingressNode,bfir,egressNodes,egressBfrs,
                input.getTargetNodeIds(),networkType,input.getCheckType(),input.getModeType(),input.getReplyMode(),
                maxTtl);
        OamInstance oamInstance = oamInstanceMap.get(key);
        if (oamInstance == null) {
            oamInstance = new OamInstance(key);
            oamInstanceMap.put(key,oamInstance);
        }
        if (networkType.equals(NetworkType.BierTe)) {
            GetTargetBitstringInput getInput = buildGetBitStringInput(input);
            Future<RpcResult<GetTargetBitstringOutput>> output = bierService.getTargetBitstring(getInput);
            List<org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring
                    .output.TargetNodeIds> targetNodeTeInfoList = null;
            try {
                if (output.get().isSuccessful()) {
                    targetNodeTeInfoList = output.get().getResult().getTargetNodeIds();
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("get targetNodes bit-string and path info failed!", e);
            }
            if (targetNodeTeInfoList != null && !targetNodeTeInfoList.isEmpty()) {
                oamInstance.setTargetNodeTeInfo(targetNodeTeInfoList);
            }
        }
        return oamInstance;
    }

    private GetTargetBitstringInput buildGetBitStringInput(StartEchoRequestInput input) {
        return new GetTargetBitstringInputBuilder()
                .setTopologyId(input.getTopologyId())
                .setChannelName(input.getChannelName())
                .setTargetNodeIds(buildTargetNodes(input.getTargetNodeIds()))
                .build();
    }

    private List<org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.input
            .TargetNodeIds> buildTargetNodes(List<TargetNodeIds> targetNodeIds) {
        List<org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.input
                .TargetNodeIds> targetNodeList = new ArrayList<>();
        for (TargetNodeIds targetNode : targetNodeIds) {
            targetNodeList.add(new TargetNodeIdsBuilder().setTargetNodeId(targetNode.getTargetNodeId()).build());
        }
        return targetNodeList;
    }

    public Set<OamInstance> getRequestInstanceSet(SingleOamRequestKey oamRequest) {
        return requestInstanceMap.get(oamRequest);
    }

    public OamInstance getOamInstance(OamInstanceKey key) {
        return oamInstanceMap.get(key);
    }

    public void putToRequestInstanceMap(SingleOamRequestKey key, Set<OamInstance> oamInstances) {
        requestInstanceMap.put(key,oamInstances);
    }

    public void removeFromRequestInstanceMap(SingleOamRequestKey key) {
        requestInstanceMap.remove(key);
    }

    public void removeFromRequestInstanceMap(SingleOamRequestKey oamRequestKey, OamInstance oamInstance) {
        Set<OamInstance> oamInstanceSet = requestInstanceMap.get(oamRequestKey);
        if (oamInstanceSet != null) {
            oamInstanceSet.remove(oamInstance);
            if (oamInstanceSet.isEmpty()) {
                requestInstanceMap.remove(oamRequestKey);
            }
        }
    }

    private String checkInput(StartEchoRequestInput input) {
        try {
            Preconditions.checkNotNull(input, "input is null!");
            Preconditions.checkNotNull(input.getTopologyId(), "topo-id is null!");
            Preconditions.checkNotNull(input.getChannelName(), "channel-name is null!");
            Preconditions.checkNotNull(input.getCheckType(), "check-type is null!");
            Preconditions.checkNotNull(input.getModeType(), "mode-type is null!");
            Preconditions.checkNotNull(input.getReplyMode(), "reply-mode is null!");
            Preconditions.checkNotNull(input.getTargetNodeIds(), "target-node-list is null!");
            Preconditions.checkState(isChannelExist(input.getTopologyId(),input.getChannelName()),
                    "channel is not exists!");
            Preconditions.checkState(!input.getTargetNodeIds().isEmpty(), "target-node-list is empty!");
            Preconditions.checkState(checkTargetNodes(input.getTopologyId(),input.getChannelName(),
                    input.getTargetNodeIds()), "channel-name and target-nodes do not match!");
        } catch (NullPointerException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    private boolean checkTargetNodes(String topoId, String channelName, List<TargetNodeIds> targetNodeIds) {
        return dbProvider.checkTargetNodes(topoId,channelName,targetNodeIds);
    }

    private boolean isChannelExist(String topoId, String channelName) {
        return dbProvider.isChannelExist(topoId,channelName);
    }

    public ConfigureResult buildConfigResult(ConfigureResult.Result result, String errorMsg) {
        return  new ConfigureResultBuilder().setResult(result).setErrorCause(errorMsg).build();
    }


    public List<BfrId> getEgressBfrIds(Channel channel) {
        List<BfrId> egressNodes = new ArrayList<>();
        for (EgressNode egressNode : channel.getEgressNode()) {
            egressNodes.add(egressNode.getEgressBfrId());
        }
        return egressNodes;
    }

    public List<String> getEgressNodes(Channel channel) {
        List<String> egressNodes = new ArrayList<>();
        for (EgressNode egressNode : channel.getEgressNode()) {
            egressNodes.add(egressNode.getNodeId());
        }
        return egressNodes;
    }

    @Override
    public void onDriverFailure(DriverFailure notification) {
    }

    @Override
    public void onDriverNotifyBierEchoReply(DriverNotifyBierEchoReply notification) {
        LOG.info("receive echo reply:" + notification);
        executor.execute(new ProcessEchoReplyTask(notification));
    }

    public void destroy() {
        oamInstanceMap.clear();
        requestInstanceMap.clear();
        singleRequestMap.clear();
        bierTeAddressMap.clear();
    }


    public void putToSingleRequestMap(SingleOamRequestKey singleRequestKey, SingleOamRequest oamRequest) {
        singleRequestMap.put(singleRequestKey,oamRequest);
    }

    @VisibleForTesting
    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void putToBierTeAddressMap(AddressInfo addressInfo, SingleOamRequest singleOamRequest) {
        bierTeAddressMap.put(addressInfo,singleOamRequest);
    }

    private class ProcessEchoReplyTask implements Runnable {
        private DriverNotifyBierEchoReply notifyBierEchoReply;

        private ProcessEchoReplyTask(DriverNotifyBierEchoReply notification) {
            this.notifyBierEchoReply = notification;
        }

        @Override
        public void run() {
            processReplyNotify(notifyBierEchoReply,ModeType.Ping);
            processReplyNotify(notifyBierEchoReply,ModeType.Trace);
        }


        private void processReplyNotify(DriverNotifyBierEchoReply notification, ModeType modeType) {
            SingleOamRequestKey oamRequestKey = generateOamRequestKey(notification, modeType);
            SingleOamRequest oamRequest;
            if (oamRequestKey == null) {
                oamRequest = getSingleRequest(notification,modeType);
                LOG.info("get single-request by notification,single-request:" + oamRequest);

            } else {
                oamRequest = OamImpl.getInstance().getSingleRequest(oamRequestKey);
            }
            if (oamRequest != null) {
                Set<OamInstance> oamInstanceSet = OamImpl.getInstance().getRequestInstanceMap()
                        .get(oamRequest.getSingleRequestKey());
                LOG.info("get oamInstances by single-request,oamInstanceSet:" + oamInstanceSet);
                if (oamInstanceSet != null) {
                    for (OamInstance oamInstance : oamInstanceSet) {
                        oamInstance.processOamReply(oamRequest,notification.getReturnInfo());
                    }
                    if (!oamRequest.getSingleRequestKey().getNetworkType().equals(NetworkType.BierTe)
                            || !oamRequest.getSingleRequestKey().getModeType().equals(ModeType.Trace)) {
                        oamRequest.stopTimer();
                        oamRequest.destroy();
                    }
                }
            }
        }

        private SingleOamRequest getSingleRequest(DriverNotifyBierEchoReply notification, ModeType modeType) {
            if (notification.getAddressInfo() instanceof BierTe) {
                AddressInfo addressInfo = notification.getAddressInfo();
                SingleOamRequest singleOamRequest = OamImpl.getInstance().getSingleRequest(addressInfo);
                LOG.info("get single-request by addressInfo,single-request:" + singleOamRequest);
                ReplyMode replyMode = ((BierTe) notification.getAddressInfo()).getReplyModeTe();
                if (singleOamRequest != null && singleOamRequest.getSingleRequestKey().getModeType().equals(modeType)
                        && singleOamRequest.getSingleRequestKey().getReplyMode().equals(replyMode)) {
                    return singleOamRequest;
                }
            }
            return null;
        }

        private SingleOamRequestKey generateOamRequestKey(DriverNotifyBierEchoReply notification, ModeType modeType) {
            /*
            if (notification != null && notification.getBierAddress() != null
                    && notification.getBierAddress() instanceof Bfrs) {
                SubDomainId subDomainId = ((Bfrs) notification.getBierAddress()).getBierSubdomainid();
                BfrId bfirId = ((Bfrs) notification.getBierAddress()).getBfir();
                String ingressNode = getNodeIdByBfirId(subDomainId,bfirId);
                List<BfrId> bfers = trans(((Bfrs) notification.getBierAddress()).getBierBfers());
                List<String> egressNodes = getNodeIdByBferId(subDomainId,bfers);
                List<BfrId> targetBfrs = new ArrayList<>();
                String targetNode = null;
                if (egressNodes != null && !egressNodes.isEmpty() && notification.getTargetBfers() != null
                        && !notification.getTargetBfers().isEmpty()) {
                    targetBfrs.add(notification.getTargetBfers().get(0).getBierBfrid());
                    targetNode = egressNodes.get(bfers.indexOf(notification.getTargetBfers().get(0).getBierBfrid()));
                }

                ReplyMode replyMode = notification.getReplyMode();
                if (subDomainId != null && ingressNode != null && egressNodes != null && !egressNodes.isEmpty()
                        && bfers != null && !bfers.isEmpty() && targetNode != null && replyMode != null) {
                    return new SingleOamRequestKey(subDomainId, ingressNode, bfirId,egressNodes, bfers, targetNode,
                            NetworkType.Bier, CheckType.OnDemand, modeType, replyMode);
                }
            }*/
            if (notification != null && notification.getAddressInfo() != null) {
                if (notification.getAddressInfo() instanceof BierAddress) {
                    if (((BierAddress) notification.getAddressInfo()).getBierAddress() instanceof Bfrs) {
                        SubDomainId subDomainId = ((Bfrs) ((BierAddress) notification.getAddressInfo())
                                .getBierAddress()).getBierSubdomainid();
                        BfrId bfirId = ((Bfrs) ((BierAddress) notification.getAddressInfo())
                                .getBierAddress()).getBfir();
                        String ingressNode = getNodeIdByBfirId(subDomainId, bfirId);
                        List<BfrId> bfers = trans(((Bfrs) ((BierAddress) notification.getAddressInfo())
                                .getBierAddress()).getBierBfers());
                        List<String> egressNodes = getNodeIdByBferId(subDomainId, bfers);
                        List<BfrId> targetBfrs = new ArrayList<>();
                        String targetNode = null;
                        if (egressNodes != null && !egressNodes.isEmpty()
                                && ((BierAddress) notification.getAddressInfo()).getTargetBfers() != null
                                && !((BierAddress) notification.getAddressInfo()).getTargetBfers().isEmpty()) {
                            targetBfrs.add(((BierAddress) notification.getAddressInfo())
                                    .getTargetBfers().get(0).getBierBfrid());
                            targetNode = egressNodes.get(bfers.indexOf(((BierAddress) notification.getAddressInfo())
                                    .getTargetBfers().get(0).getBierBfrid()));
                        }

                        ReplyMode replyMode = ((BierAddress) notification.getAddressInfo()).getReplyMode();
                        if (subDomainId != null && ingressNode != null && egressNodes != null && !egressNodes.isEmpty()
                                && bfers != null && !bfers.isEmpty() && targetNode != null && replyMode != null) {
                            return new SingleOamRequestKey(subDomainId, ingressNode, bfirId, egressNodes, bfers,
                                    targetNode, NetworkType.Bier, CheckType.OnDemand, modeType, replyMode);
                        }
                    }
                }
            }
            return null;
        }

        private List<String> getNodeIdByBferId(SubDomainId subDomainId, List<BfrId> bfers) {
            if (subDomainId != null && bfers != null && !bfers.isEmpty()) {
                return dbProvider.getNodeIdByBferId(subDomainId, bfers);
            }
            return null;
        }

        private String getNodeIdByBfirId(SubDomainId subDomainId, BfrId bfirId) {
            if (subDomainId != null && bfirId != null) {
                return dbProvider.getNodeIdByBfirId(subDomainId, bfirId);
            }
            return null;
        }

        private List<BfrId> trans(List<BierBfers> bierBfers) {
            if (bierBfers != null && !bierBfers.isEmpty()) {
                List<BfrId> bfrIds = new ArrayList<>();
                for (BierBfers bfer : bierBfers) {
                    bfrIds.add(bfer.getBierBfrid());
                }
                return bfrIds;
            }
            return null;
        }
    }

}