/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.oam.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.CheckType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.ModeType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.NetworkType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.ReceiveEchoReply;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.ReceiveEchoReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.receive.echo.reply.EgressNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.receive.echo.reply.EgressNodeIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.receive.echo.reply.respond.PingBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.receive.echo.reply.respond.TraceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.receive.echo.reply.respond.ping.PingTargetNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.receive.echo.reply.respond.ping.PingTargetNodeIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.receive.echo.reply.respond.trace.TraceTargetNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.receive.echo.reply.respond.trace.TraceTargetNodeIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.receive.echo.reply.respond.trace.trace.target.node.ids.TraceResponderNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.receive.echo.reply.respond.trace.trace.target.node.ids.TraceResponderNodeIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.TargetNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.ReturnInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobal;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.ReplyMode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OamInstance {
    private static final Logger LOG = LoggerFactory.getLogger(OamInstance.class);
    private OamInstanceKey oamInstanceKey;
    private Set<SingleOamRequest> oamRequestSet = new HashSet<>(); //
    private Timer timer;
    private static final long DELAY = 30000;
    private List<String> waitingRespondTargets;
    private List<TraceResponderNodeIds> traceResponderNodes = new ArrayList<>();
    private List<TraceTargetNodeIds> traceTargetNodeIds = new ArrayList<>();
    private List<PingTargetNodeIds> pingTargetNodeIds = new ArrayList<>();
    private List<TargetNodeIds> targetNodeTeInfo = new ArrayList<>();


    public OamInstance(OamInstanceKey key) {
        this.oamInstanceKey = key;
        this.waitingRespondTargets = new ArrayList<>(key.getTargetNodes());
    }

    public Set<SingleOamRequest> getOamRequestSet() {
        return this.oamRequestSet;
    }

    public void removeFromWaitingTargets(String nodeId) {
        waitingRespondTargets.remove(nodeId);
    }

    public ConfigurationResult processOamRequest() {
        String errMsg = "";
        for (String targetNode : oamInstanceKey.getTargetNodes()) {
            SingleOamRequestKey key = new SingleOamRequestKey(oamInstanceKey.getSubDomainId(),
                    oamInstanceKey.getIngressNode(),oamInstanceKey.getBfirId(),oamInstanceKey.getEgressNodes(),
                    oamInstanceKey.getEgressBfrs(), targetNode,oamInstanceKey.getNetworkType(),
                    oamInstanceKey.getCheckType(), oamInstanceKey.getModeType(),oamInstanceKey.getReplyMode());
            SingleOamRequest oamRequest = OamImpl.getInstance().getSingleRequest(key);
            if (oamRequest == null) {
                oamRequest = new SingleOamRequest(key,oamInstanceKey.getMaxTtl());
            }
            if (oamInstanceKey.getNetworkType().equals(NetworkType.BierTe)) {
                Integer index = oamInstanceKey.getTargetNodes().indexOf(targetNode);
                if (index != -1 && !targetNodeTeInfo.isEmpty()) {
                    oamRequest.setBitstringInfo(targetNodeTeInfo.get(index).getBitstringInfo());
                    oamRequest.setPathInfo(targetNodeTeInfo.get(index).getPathInfo());
                    oamRequest.setWaitRespondLinks(targetNodeTeInfo.get(index).getPathInfo().getPathLink());
                }
            }
            ConfigurationResult result = oamRequest.sendOamRequest();
            if (result.isSuccessful()) {
                oamRequestSet.add(oamRequest);
                oamRequest.putToSingleRequestMap();
                oamRequest.putToBierTeAddressMap();
                oamRequest.putToRequestInstanceMap(this);
                oamRequest.startTimer(oamRequest);
            } else {
                errMsg += result.getFailureReason() + " ; ";
                oamRequest.stopTimer();
                oamRequest.destroy();
            }
        }
        if (errMsg != "") {
            destroy();
            return new ConfigurationResult(ConfigurationResult.Result.FAILED,errMsg);
        }
        startTimer(this);
        return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
    }

    private void startTimer(final OamInstance oamInstance) {
        if (oamInstanceKey.getCheckType() == CheckType.OnDemand) {
            if (timer == null) {
                timer = new Timer();
            } else {
                timer.cancel();
                timer = new Timer();
            }
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    LOG.info("%%%%%%  oam instance time out %%%%%%%");
                    oamInstance.notifyEchoReply();
                    oamInstance.destroy();
                }
            }, DELAY);
        }
    }

    public void notifyEchoReply() {
        LOG.info("notify echo reply!");
        ReceiveEchoReply echoReply = buildEchoReply();
        NotificationProvider.getInstance().notify(echoReply);
        LOG.info("echo reply info: " + echoReply);
    }

    private ReceiveEchoReply buildEchoReply() {

        ReceiveEchoReplyBuilder builder = new ReceiveEchoReplyBuilder();
        builder.setSubdomainId(oamInstanceKey.getSubDomainId());
        builder.setIngressNodeId(oamInstanceKey.getIngressNode());
        builder.setEgressNodeIds(transformEgressNodes(oamInstanceKey.getEgressNodes()));
        switch (oamInstanceKey.getModeType()) {
            case Ping:
                pingEchoReply(builder);
                break;
            case Trace:
                traceEchoReply(builder);
                break;
            default:
                break;
        }
        return builder.build();
    }

    private ReceiveEchoReplyBuilder traceEchoReply(ReceiveEchoReplyBuilder builder) {
        return builder.setRespond(new TraceBuilder()
                .setTraceTargetNodeIds(buildTraceTargetNodeIds())
                .build());
    }

    private ReceiveEchoReplyBuilder pingEchoReply(ReceiveEchoReplyBuilder builder) {
        return builder.setRespond(new PingBuilder()
                .setPingTargetNodeIds(buildPingTargetNodeIds())
                .build());
    }

    private List<PingTargetNodeIds> buildPingTargetNodeIds() {
        if (!waitingRespondTargets.isEmpty()) {
            for (String targetNode : waitingRespondTargets) {
                pingTargetNodeIds.add(new PingTargetNodeIdsBuilder()
                        .setPingResult("failed!Time out!")
                        .setPingTargetNodeId(targetNode)
                        .build());
            }
        }
        return pingTargetNodeIds;
    }

    private List<TraceTargetNodeIds> buildTraceTargetNodeIds() {
        if (!waitingRespondTargets.isEmpty()) {
            for (String targetNode : waitingRespondTargets) {
                traceTargetNodeIds.add(new TraceTargetNodeIdsBuilder()
                        .setTraceTargetNodeId(targetNode)
                        .setTraceResult("failed!Time out!(instance)")
                        .build());
            }
        }
        return traceTargetNodeIds;
    }

    private List<EgressNodeIds> transformEgressNodes(List<String> egressNodes) {
        List<EgressNodeIds> egressNodeIdsList = new ArrayList<>();
        for (String nodeId : egressNodes) {
            egressNodeIdsList.add(new EgressNodeIdsBuilder().setEgressNodeId(nodeId).build());
        }
        return egressNodeIdsList;
    }

    public void destroy() {
        for (SingleOamRequest oamRequest : oamRequestSet) {
            OamImpl.getInstance().removeFromRequestInstanceMap(oamRequest.getSingleRequestKey(),this);
        }
        oamRequestSet.clear();
        OamImpl.getInstance().removeFromOamInstanceMap(this.oamInstanceKey);
    }

    public void removeSingleRequest(SingleOamRequest oamRequest) {
        oamRequestSet.remove(oamRequest);
    }


    public void processOamReply(SingleOamRequest oamRequest, List<ReturnInfo> returnInfo) {
        switch (oamRequest.getSingleRequestKey().getModeType()) {
            case Ping:
                LOG.info("processReplyPing");
                processReplyPing(oamRequest,returnInfo);
                break;
            case Trace:
                LOG.info("processReplyTrace");
                processReplyTrace(oamRequest,returnInfo);
                break;
            default:
                break;
        }
    }

    private void processReplyTrace(SingleOamRequest oamRequest, List<ReturnInfo> returnInfo) {
        if (oamRequest.getSingleRequestKey().getReplyMode() == ReplyMode.ReplyViaIPv4IPv6UDPPacket
                && oamRequest.getSingleRequestKey().getNetworkType().equals(NetworkType.Bier)) {
            processUdpReplyTrace(oamRequest,returnInfo);
        }
        if (oamRequest.getSingleRequestKey().getReplyMode() == ReplyMode.DoNotReply
                && oamRequest.getSingleRequestKey().getNetworkType().equals(NetworkType.BierTe)) {
            processBierTeReplyTrace(oamRequest,returnInfo);
        }
    }

    private void processBierTeReplyTrace(SingleOamRequest oamRequest, List<ReturnInfo> returnInfo) {
        String traceInfo = "success";
        boolean targetNodeProcessed = false;
        oamRequest.removePathLink(getNodeIdByIp(returnInfo.get(0).getResponderBfr()));
        if (!returnCodeSuccess(returnInfo.get(0))) {
            buildRespondNodes(oamRequest,getNodeIdByIp(returnInfo.get(0).getResponderBfr())) ;
            traceInfo = returnInfo.get(0).getReturnCode().getName();
            targetNodeProcessed = true;
        } else {
            if (oamRequest.isWaitRespondLinksEmpty()) {
                buildRespondNodes(oamRequest,null);
                targetNodeProcessed = true;
            }
        }
        if (targetNodeProcessed) {
            addToTraceTargetNodes(oamRequest.getSingleRequestKey().getTargetNode(),traceInfo);
            removeFromWaitingTargets(oamRequest.getSingleRequestKey().getTargetNode());
            removeSingleRequest(oamRequest);
            oamRequest.stopTimer();
            oamRequest.destroy();
            if (waitingRespondTargets.isEmpty()) {
                notifyEchoReply();
                stopTimer();
                destroy();
            }
        }
    }

    private void processUdpReplyTrace(SingleOamRequest oamRequest, List<ReturnInfo> returnInfo) {
        String traceInfo = "success";
        for (ReturnInfo info : returnInfo) {
            if (!returnCodeSuccess(info)) {
                traceInfo = info.getReturnCode().getName();
            }
            addToRespondNodes(ModeType.Trace,null,null,info);
        }
        addToTraceTargetNodes(oamRequest.getSingleRequestKey().getTargetNode(),traceInfo);
        removeFromWaitingTargets(oamRequest.getSingleRequestKey().getTargetNode());
        removeSingleRequest(oamRequest);
        if (waitingRespondTargets.isEmpty()) {
            notifyEchoReply();
            stopTimer();
            destroy();
        }
    }

    private void processReplyPing(SingleOamRequest oamRequest, List<ReturnInfo> returnInfo) {
        if (returnCodeSuccess(returnInfo.get(0))) {
            addToRespondNodes(ModeType.Ping,oamRequest.getSingleRequestKey().getTargetNode(),
                    "success",returnInfo.get(0));
        } else {
            addToRespondNodes(ModeType.Ping,oamRequest.getSingleRequestKey().getTargetNode(),null,
                    returnInfo.get(0));
        }
        removeFromWaitingTargets(oamRequest.getSingleRequestKey().getTargetNode());
        removeSingleRequest(oamRequest);
        if (waitingRespondTargets.isEmpty()) {
            notifyEchoReply();
            stopTimer();
            destroy();
        }
    }

    private boolean returnCodeSuccess(ReturnInfo returnInfo) {
        if (returnInfo.getReturnCode().getIntValue() >= 3 && returnInfo.getReturnCode().getIntValue() <= 5) {
            return true;
        }
        return false;
    }

    public void buildRespondNodes(SingleOamRequest oamRequest, String respondBfr) {
        Integer index = 1;
        for (BierLink link : oamRequest.getPathInfo().getPathLink()) {
            traceResponderNodes.add(new TraceResponderNodeIdsBuilder()
                    .setIndex(index++)
                    .setTraceResponderNodeId(link.getLinkDest().getDestNode())
                    .build());
            if (respondBfr != null
                    && link.getLinkDest().getDestNode().equals(respondBfr)) {
                break;
            }
        }
    }

    private void addToRespondNodes(ModeType modeType, String targetNode, String result, ReturnInfo info) {
        switch (modeType) {
            case Ping:
                pingTargetNodeIds.add(new PingTargetNodeIdsBuilder()
                        .setPingTargetNodeId(targetNode)
                        .setPingResult(result != null ? result : info.getReturnCode().getName())
                        .build());
                break;
            case Trace:
                traceResponderNodes.add(new TraceResponderNodeIdsBuilder()
                        .setIndex(info.getTtl())
                        .setTraceResponderNodeId(getNodeIdByIp(info.getResponderBfr()))
                        .build());
                break;
            default:
                break;
        }

    }

    public void  addToTraceTargetNodes(String targetNode, String result) {
        List<TraceResponderNodeIds> traceResponderNodeIdsList = new ArrayList<>(traceResponderNodes);
        traceTargetNodeIds.add(new TraceTargetNodeIdsBuilder()
                .setTraceTargetNodeId(targetNode)
                .setTraceResponderNodeIds(traceResponderNodeIdsList)
                .setTraceResult(result)
                .build());
        traceResponderNodes.clear();
    }

    private String getNodeIdByIp(IpAddress bfrIp) {
        BierTopology bierGlobalCfg = DbProvider.getInstance().readBierTopology();
        if (bierGlobalCfg != null) {
            for (BierNode bierNode : bierGlobalCfg.getBierNode()) {
                BierGlobal bierGlobal = bierNode.getBierNodeParams().getDomain().get(0).getBierGlobal();
                Ipv4Prefix ipv4Prefix = bierGlobal.getIpv4BfrPrefix();
                Ipv6Prefix ipv6Prefix = bierGlobal.getIpv6BfrPrefix();
                Ipv4Address ipv4Add = bfrIp.getIpv4Address();
                Ipv6Address ipv6Add = bfrIp.getIpv6Address();
                if (ipv4Add != null && ipv4Prefix != null) {
                    if (ipv4Prefix.getValue().contains(ipv4Add.getValue() + "/")) {
                        return bierNode.getNodeId();
                    }
                }
                if (ipv6Add != null && ipv6Prefix != null) {
                    if (ipv6Prefix.getValue().contains(ipv6Add.getValue() + "/")) {
                        return bierNode.getNodeId();
                    }
                }
            }
        }
        return null;
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public boolean isTimerActive() {
        if (timer != null) {
            return true;
        }
        return false;
    }

    public void setTargetNodeTeInfo(List<TargetNodeIds> targetNodeTeInfo) {
        List<TargetNodeIds> tempTarget = new ArrayList<>();
        for (String targetNode : oamInstanceKey.getTargetNodes()) {
            for (TargetNodeIds targetNodeTe : targetNodeTeInfo) {
                if (targetNodeTe.getTargetNodeId().equals(targetNode)) {
                    tempTarget.add(targetNodeTe);
                }
            }
        }
        this.targetNodeTeInfo = tempTarget;
    }
}
