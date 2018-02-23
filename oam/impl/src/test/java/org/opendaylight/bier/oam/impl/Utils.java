/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.oam.impl;

import com.google.common.util.concurrent.Futures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverNotifyBierEchoReply;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverNotifyBierEchoReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.ModeType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.start.echo.request.input.TargetNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.start.echo.request.input.TargetNodeIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringInput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringOutput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.BitstringInfo;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.BitstringInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.PathInfo;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.PathInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.ReturnCode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.ReturnInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.ReturnInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.address.info.BierBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.address.info.BierTeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.ReplyMode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.TargetBfers;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.bier.address.BfrsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.bier.address.bfrs.BierBfers;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.bier.address.bfrs.BierBfersBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.Bitstring;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.BitstringBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.FecStackType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.FecStackTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.bitstring.Bitpositions;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.bitstring.BitpositionsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.FecStackInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.FecStackInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.fec.stack.info.fec.stack.type.ConnectedBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.fec.stack.info.fec.stack.type.LocalDecapBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.continuity.check.input.destination.tp.tp.address.BierTeAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.ContinuityCheckInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.ContinuityCheckInputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.continuity.check.input.DestinationTpBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.rev170609.SessionType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;


public class Utils {
/*    public static ReceiveEchoReply buildPingTimeOutReply(SubDomainId subDomainId, String ingress,
                                                         List<String> egresses, List<TargetNodeIds> targetNodes) {
        return new ReceiveEchoReplyBuilder()
                .setSubdomainId(subDomainId)
                .setIngressNodeId(ingress)
                .setEgressNodeIds(trans(egresses))
                .setRespond(new PingBuilder().setPingTargetNodeIds(buildPingTargetTimeOut(targetNodes)).build())
                .build();
    }

    private static List<PingTargetNodeIds> buildPingTargetTimeOut(List<TargetNodeIds> targetNodes) {
        List<PingTargetNodeIds> pingTargetNodeIds = new ArrayList<>();
        for (TargetNodeIds targetNode : targetNodes) {
            pingTargetNodeIds.add(new PingTargetNodeIdsBuilder()
                    .setPingTargetNodeId(targetNode.getTargetNodeId())
                    .setPingResult("failed!Time out!")
                    .build());
        }
        return pingTargetNodeIds;
    }

    private static List<EgressNodeIds> trans(List<String> egresses) {
        List<EgressNodeIds> egressNodeIdList = new ArrayList<>();
        for (String egress : egresses) {
            egressNodeIdList.add(new EgressNodeIdsBuilder().setEgressNodeId(egress).build());
        }
        return egressNodeIdList;
    }
    */
    public static DriverNotifyBierEchoReply buildBierEchoReply(SubDomainId subDomainId, String channelName,
                                                                Integer bfirId, ReplyMode replyMode, ModeType modeType,
                                                                boolean isSuccess, TargetBfers targetBfer) {
        List<ReturnInfo> returnInfo = new ArrayList<>();
        switch (modeType) {
            case Ping:
                returnInfo.add(new ReturnInfoBuilder()
                        .setResponderBfr(BierBasicMockUtils.getIpAddress(targetBfer.getBierBfrid()))
                        .setReturnCode(isSuccess ? ReturnCode.ForwardSuccess : ReturnCode.NoEntryMatched)
                        .build());

            case Trace:
                for (Integer index = 1 ; index < 4 ; index ++) {
                    returnInfo.add(new ReturnInfoBuilder()
                            .setResponderBfr(buildResponderBfr(index,targetBfer))
                            .setReturnCode(isSuccess ? ReturnCode.ForwardSuccess : ReturnCode.NoEntryMatched)
                            .setIndex(index)
                            .setTtl(index)
                            .build());
                }
            default:
                break;
        }
        List<TargetBfers> targets = new ArrayList<>();
        targets.add(targetBfer);
        return new DriverNotifyBierEchoReplyBuilder()
                .setAddressInfo(new BierBuilder()
                        .setReplyMode(replyMode)
                        .setTargetBfers(targets)
                        .setBierAddress(new BfrsBuilder()
                                .setBierBfers(buildBierBfers(BierBasicMockUtils.getBfers(channelName)))
                                .setBierSubdomainid(subDomainId)
                                .setBfir(new BfrId(bfirId))
                                .setBierBsl(Bsl._256Bit)
                                .build())
                        .build())
                .setReturnInfo(returnInfo)
                .build();
    }


    public static List<DriverNotifyBierEchoReply> buildBierTeEchoReply(String channelName, SubDomainId subDomainId,
                                                                       String targetNode, ModeType modeType,
                                                                       boolean isSuccess,boolean isTimeOut,
                                                                       TargetBfers targetBfer,boolean isOrdered) {
        List<ReturnInfo> returnInfo = new ArrayList<>();
        List<DriverNotifyBierEchoReply> replies = new ArrayList<>();

        switch (modeType) {
            case Ping:
                returnInfo.add(new ReturnInfoBuilder()
                        .setResponderBfr(BierBasicMockUtils.getIpAddress(targetBfer.getBierBfrid()))
                        .setReturnCode(isSuccess ? ReturnCode.ForwardSuccess : ReturnCode.NoEntryMatched)
                        .build());

                replies.add(buildBierTeNotification(channelName, subDomainId, targetNode, returnInfo,isOrdered));
            case Trace:
                generateBierTeTraceReply(channelName, subDomainId, targetNode, isSuccess, isTimeOut, replies,isOrdered);
        }


        return replies;
    }

    private static void generateBierTeTraceReply(String channelName, SubDomainId subDomainId, String targetNode,
                                                 boolean isSuccess, boolean isTimeOut,
                                                 List<DriverNotifyBierEchoReply> replies,boolean isOrdered) {
        boolean timeOutFlag = isSuccess ? false : isTimeOut;

        List<ReturnInfo> returnInfo = new ArrayList<>();
        List<ReturnInfo> returnInfo1 = new ArrayList<>();
        if (channelName.equals("c5")) {

            if (targetNode.equals("node2")) {
                returnInfo.add(new ReturnInfoBuilder()
                        .setResponderBfr(BierBasicMockUtils.getIpAddress(new BfrId(33)))
                        .setReturnCode(ReturnCode.ForwardSuccess )
                        .build());
                replies.add(buildBierTeNotification(channelName,subDomainId,targetNode,returnInfo,isOrdered));

                if (!timeOutFlag) {
                    returnInfo1.add(new ReturnInfoBuilder()
                            .setResponderBfr(BierBasicMockUtils.getIpAddress(new BfrId(2)))
                            .setReturnCode(isSuccess ? ReturnCode.ForwardSuccess : ReturnCode.NoEntryMatched)
                            .build());
                    replies.add(buildBierTeNotification(channelName, subDomainId, targetNode, returnInfo1,isOrdered));
                }
            }
            if (targetNode.equals("node3")) {
                if (!timeOutFlag) {
                    returnInfo.add(new ReturnInfoBuilder()
                            .setResponderBfr(BierBasicMockUtils.getIpAddress(new BfrId(33)))
                            .setReturnCode(isSuccess ? ReturnCode.ForwardSuccess : ReturnCode.NoEntryMatched)
                            .build());
                    replies.add(buildBierTeNotification(channelName, subDomainId, targetNode, returnInfo,isOrdered));
                }
            }
        }
        if (channelName.equals("c6")) {
            if (targetNode.equals("node3")) {
                returnInfo.add(new ReturnInfoBuilder()
                        .setResponderBfr(BierBasicMockUtils.getIpAddress(new BfrId(2)))
                        .setReturnCode(ReturnCode.ForwardSuccess )
                        .build());
                replies.add(buildBierTeNotification(channelName,subDomainId,targetNode,returnInfo,isOrdered));
                if (!timeOutFlag) {
                    returnInfo1.add(new ReturnInfoBuilder()
                            .setResponderBfr(BierBasicMockUtils.getIpAddress(new BfrId(33)))
                            .setReturnCode(isSuccess ? ReturnCode.ForwardSuccess : ReturnCode.NoEntryMatched)
                            .build());
                    replies.add(buildBierTeNotification(channelName, subDomainId, targetNode, returnInfo1,isOrdered));
                }
            }
            if (targetNode.equals("node2")) {
                if (!timeOutFlag) {
                    returnInfo.add(new ReturnInfoBuilder()
                            .setResponderBfr(BierBasicMockUtils.getIpAddress(new BfrId(2)))
                            .setReturnCode(isSuccess ? ReturnCode.ForwardSuccess : ReturnCode.NoEntryMatched)
                            .build());
                    replies.add(buildBierTeNotification(channelName, subDomainId, targetNode, returnInfo,isOrdered));
                }
            }
            if (targetNode.equals("node5")) {
                returnInfo.add(new ReturnInfoBuilder()
                        .setResponderBfr(BierBasicMockUtils.getIpAddress(new BfrId(2)))
                        .setReturnCode(ReturnCode.ForwardSuccess )
                        .build());
                replies.add(buildBierTeNotification(channelName,subDomainId,targetNode,returnInfo,isOrdered));
                if (!timeOutFlag) {
                    returnInfo1.add(new ReturnInfoBuilder()
                            .setResponderBfr(BierBasicMockUtils.getIpAddress(new BfrId(55)))
                            .setReturnCode(isSuccess ? ReturnCode.ForwardSuccess : ReturnCode.NoEntryMatched)
                            .build());
                    replies.add(buildBierTeNotification(channelName, subDomainId, targetNode, returnInfo1,isOrdered));
                }
            }
        }
    }

    private static DriverNotifyBierEchoReply buildBierTeNotification(String channelName, SubDomainId subDomainId,
                                                                     String targetNode, List<ReturnInfo> returnInfo,
                                                                     boolean isOrdered) {
        return new DriverNotifyBierEchoReplyBuilder()
               .setAddressInfo(new BierTeBuilder()
                       .setReplyModeTe(ReplyMode.DoNotReply)
                       .setBierTeSubdomainid(subDomainId)
                       .setBierTeBpInfo(buildBierTeBpInfoList(channelName,
                               new TargetNodeIdsBuilder()
                                       .setTargetNodeId(targetNode)
                                       .build(),false,isOrdered))
                       .build())
               .setReturnInfo(returnInfo)
               .build();
    }

    private static IpAddress buildResponderBfr(Integer index, TargetBfers targetBfer) {
        if (index == 1) {
            return BierBasicMockUtils.getIpAddress(new BfrId(66));
        }
        if (index == 2) {
            return BierBasicMockUtils.getIpAddress(new BfrId(77));
        }
        if (index == 3) {
            return BierBasicMockUtils.getIpAddress(targetBfer.getBierBfrid());
        }
        return null;
    }

    private static List<BierBfers> buildBierBfers(List<BfrId> bfers) {
        List<BierBfers> bierBfersList = new ArrayList<>();
        for (BfrId bfer :bfers) {
            bierBfersList.add(new BierBfersBuilder().setBierBfrid(bfer).build());
        }
        return bierBfersList;
    }

    public static ContinuityCheckInput buildBierContinuityCheck(String channelName, String targetNode) {
        return new ContinuityCheckInputBuilder()
                .setTtl((short) 255)
                .setSessionTypeEnum(SessionType.SessionTypeEnum.OnDemand)
                .setCount(1L)
                .setDestinationTp(new DestinationTpBuilder()
                        .setTpAddress(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs
                                .rev170808.continuity.check.input.destination.tp.tp.address.BierTeAddressBuilder()
                                .setReplyModeTe(ReplyMode.DoNotReply)
                                .setBierTeSubdomainid(new SubDomainId(1))
                                .setBierTeBpInfo(buildBitstringInfo(channelName,
                                        new TargetNodeIdsBuilder()
                                                .setTargetNodeId(targetNode)
                                                .build())
                                        .getBierTeBpInfo())
                                .build())
                        .build())
                .build();
    }



    public static Future<RpcResult<GetTargetBitstringOutput>> buildGetTargetBitstringOutput(
            String channelName,List<TargetNodeIds> targetNodes) {
        List<org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring
                .output.TargetNodeIds> targetNodeList = new ArrayList<>();
        for (TargetNodeIds targetNode : targetNodes) {
            targetNodeList.add(new org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring
                    .output.TargetNodeIdsBuilder()
                    .setTargetNodeId(targetNode.getTargetNodeId())
                    .setPathInfo(buildPathInfo(channelName,targetNode.getTargetNodeId()))
                    .setBitstringInfo(buildBitstringInfo(channelName,targetNode))
                    .build());
        }

        GetTargetBitstringOutput output = new GetTargetBitstringOutputBuilder()
                .setTargetNodeIds(targetNodeList)
                .build();
        return Futures.immediateFuture(RpcResultBuilder.<GetTargetBitstringOutput>success(output).build());
    }

    private static BitstringInfo buildBitstringInfo(String channelName, TargetNodeIds targetNode) {
        BitstringInfoBuilder builder = new BitstringInfoBuilder();
        builder.setBierTeSubdomainid(new SubDomainId(1));
        builder.setReplyModeTe(ReplyMode.DoNotReply);
        List<BierTeBpInfo> bpInfoList = buildBierTeBpInfoList(channelName, targetNode,true,true);
        builder.setBierTeBpInfo(bpInfoList).build();

        return builder.build();
    }

    private static List<BierTeBpInfo> buildBierTeBpInfoList(String channelName, TargetNodeIds targetNode,
                                                            boolean hasFec,boolean isOrdered) {
        List<BierTeBpInfo> bpInfoList = new ArrayList<>();

        bpInfoList.add(new BierTeBpInfoBuilder()
                .setBierTeBsl(Bsl._64Bit)
                .setBitstring(buildBitstringList(channelName,targetNode,isOrdered))
                .setFecStackType(hasFec ? buildFecStackList(channelName,targetNode): null)
                .build());
        return bpInfoList;
    }

    private static List<FecStackType> buildFecStackList(String channelName, TargetNodeIds targetNode) {
        List<FecStackType> fecStackTypes = new ArrayList<>();
        fecStackTypes.add(new FecStackTypeBuilder()
                .setSi(new Si(1))
                .setFecStackInfo(buildFecStackInfos(channelName,targetNode))
                .build());
        return fecStackTypes;
    }

    private static List<FecStackInfo> buildFecStackInfos(String channelName, TargetNodeIds targetNode) {
        List<FecStackInfo> fecStackInfos = new ArrayList<>();
        if (channelName.equals("c5")) {
            if (targetNode.getTargetNodeId().equals("node3")) {
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(43))
                        .setFecStackType(new ConnectedBuilder()
                                .setLocalBfr(new IpAddress(new Ipv4Address("3.3.3.3")))
                                .setLocalInterface("tp1")
                                .build())
                        .build());
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(3))
                        .setFecStackType(new LocalDecapBuilder()
                                .setBfer(new IpAddress(new Ipv4Address("3.3.3.3")))
                                .build())
                        .build());
            }
            if (targetNode.getTargetNodeId().equals("node2")) {
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(43))
                        .setFecStackType(new ConnectedBuilder()
                                .setLocalBfr(new IpAddress(new Ipv4Address("3.3.3.3")))
                                .setLocalInterface("tp1")
                                .build())
                        .build());
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(32))
                        .setFecStackType(new ConnectedBuilder()
                                .setLocalBfr(new IpAddress(new Ipv4Address("2.2.2.2")))
                                .setLocalInterface("tp2")
                                .build())
                        .build());
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(2))
                        .setFecStackType(new LocalDecapBuilder()
                                .setBfer(new IpAddress(new Ipv4Address("2.2.2.2")))
                                .build())
                        .build());
            }
        }
        if (channelName.equals("c6")) {
            if (targetNode.getTargetNodeId().equals("node2")) {
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(12))
                        .setFecStackType(new ConnectedBuilder()
                                .setLocalBfr(new IpAddress(new Ipv4Address("2.2.2.2")))
                                .setLocalInterface("tp1")
                                .build())
                        .build());
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(2))
                        .setFecStackType(new LocalDecapBuilder()
                                .setBfer(new IpAddress(new Ipv4Address("2.2.2.2")))
                                .build())
                        .build());
            }
            if (targetNode.getTargetNodeId().equals("node3")) {
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(12))
                        .setFecStackType(new ConnectedBuilder()
                                .setLocalBfr(new IpAddress(new Ipv4Address("2.2.2.2")))
                                .setLocalInterface("tp1")
                                .build())
                        .build());
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(23))
                        .setFecStackType(new ConnectedBuilder()
                                .setLocalBfr(new IpAddress(new Ipv4Address("3.3.3.3")))
                                .setLocalInterface("tp2")
                                .build())
                        .build());
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(3))
                        .setFecStackType(new LocalDecapBuilder()
                                .setBfer(new IpAddress(new Ipv4Address("3.3.3.3")))
                                .build())
                        .build());
            }
            if (targetNode.getTargetNodeId().equals("node5")) {
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(12))
                        .setFecStackType(new ConnectedBuilder()
                                .setLocalBfr(new IpAddress(new Ipv4Address("2.2.2.2")))
                                .setLocalInterface("tp1")
                                .build())
                        .build());
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(25))
                        .setFecStackType(new ConnectedBuilder()
                                .setLocalBfr(new IpAddress(new Ipv4Address("5.5.5.5")))
                                .setLocalInterface("tp3")
                                .build())
                        .build());
                fecStackInfos.add(new FecStackInfoBuilder()
                        .setBitposition(new BitString(5))
                        .setFecStackType(new LocalDecapBuilder()
                                .setBfer(new IpAddress(new Ipv4Address("5.5.5.5")))
                                .build())
                        .build());
            }
        }
        return fecStackInfos;
    }

    private static List<Bitstring> buildBitstringList(String channelName, TargetNodeIds targetNode, boolean isOrdered) {
        List<Bitstring> bitstringList = new ArrayList<>();
        bitstringList.add(new BitstringBuilder()
                .setSi(new Si(1))
                .setBitpositions(buildbpList(channelName,targetNode,isOrdered))
                .build());
        return bitstringList;
    }

    private static List<Bitpositions> buildbpList(String channelName, TargetNodeIds targetNode, boolean isOrdered) {
        List<Bitpositions> bitpositionList = new ArrayList<>();
        if (channelName.equals("c5")) {
            if (targetNode.getTargetNodeId().equals("node3")){
                bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(43)).build());
                bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(3)).build());
            }
            if (targetNode.getTargetNodeId().equals("node2")) {
                if (isOrdered) {
                    bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(43)).build());
                    bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(32)).build());
                    bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(2)).build());
                } else {
                    bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(32)).build());
                    bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(43)).build());
                    bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(2)).build());
                }
            }
        }
        if (channelName.equals("c6")) {
            if (targetNode.getTargetNodeId().equals("node2")){
                if (isOrdered) {
                    bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(12)).build());
                    bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(2)).build());
                } else {
                    bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(2)).build());
                    bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(12)).build());
                }
            }
            if (targetNode.getTargetNodeId().equals("node3")) {
                bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(12)).build());
                bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(23)).build());
                bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(3)).build());
            }
            if (targetNode.getTargetNodeId().equals("node5")) {
                bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(12)).build());
                bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(25)).build());
                bitpositionList.add(new BitpositionsBuilder().setBitposition(new BitString(5)).build());
            }
        }
        return bitpositionList;
    }

    /*
 *  R1--------R2------R5
 *  |         |      /
 *  |         |    /
 *  |         |  /
 *  R4--------R3
 *
 */

    private static PathInfo buildPathInfo(String channelName, String targetNodeId) {
        PathInfoBuilder pathInfoBuilder = new PathInfoBuilder();
        List<PathLink> pathLinks = new ArrayList<>();
        if (channelName.equals("c5")) {
            if (targetNodeId.equals("node2")) {
                pathLinks.add(new PathLinkBuilder(BierBasicMockUtils.buildLink("node4","link43","link34","node3",10))
                        .build());
                pathLinks.add(new PathLinkBuilder(BierBasicMockUtils.buildLink("node3","link32","link23","node2",10))
                        .build());
                pathInfoBuilder.setPathLink(pathLinks).setPathMetric(20L).build();
            }
            if (targetNodeId.equals("node3")) {
                pathLinks.add(new PathLinkBuilder(BierBasicMockUtils.buildLink("node4","link43","link34","node3",10))
                        .build());
                pathInfoBuilder.setPathLink(pathLinks).setPathMetric(10L).build();
            }
        }
        if (channelName.equals("c6")) {
            if (targetNodeId.equals("node3")) {
                pathLinks.add(new PathLinkBuilder(BierBasicMockUtils.buildLink("node1","link12","link21","node2",10))
                        .build());
                pathLinks.add(new PathLinkBuilder(BierBasicMockUtils.buildLink("node2","link23","link32","node3",10))
                        .build());
                pathInfoBuilder.setPathLink(pathLinks).setPathMetric(20L).build();
            }
            if (targetNodeId.equals("node2")) {
                pathLinks.add(new PathLinkBuilder(BierBasicMockUtils.buildLink("node1","link12","link21","node2",10))
                        .build());
                pathInfoBuilder.setPathLink(pathLinks).setPathMetric(10L).build();
            }
            if (targetNodeId.equals("node5")) {
                pathLinks.add(new PathLinkBuilder(BierBasicMockUtils.buildLink("node1","link12","link21","node2",10))
                        .build());
                pathLinks.add(new PathLinkBuilder(BierBasicMockUtils.buildLink("node2","link25","link52","node5",10))
                        .build());
                pathInfoBuilder.setPathLink(pathLinks).setPathMetric(20L).build();
            }
        }
        return pathInfoBuilder.build();
    }

    public static GetTargetBitstringInput buildGetTargetBitstringInput(String channelName, List<TargetNodeIds> targetNodes) {
        List<org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring
                .input.TargetNodeIds> targetNodeList = new ArrayList<>();
        for (TargetNodeIds target : targetNodes) {
            targetNodeList.add(new org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring
                    .input.TargetNodeIdsBuilder().setTargetNodeId(target.getTargetNodeId()).build());
        }
        return new GetTargetBitstringInputBuilder()
                .setChannelName(channelName)
                .setTopologyId(BierBasicMockUtils.DEFAULT_TOPO)
                .setTargetNodeIds(targetNodeList)
                .build();
    }

}
