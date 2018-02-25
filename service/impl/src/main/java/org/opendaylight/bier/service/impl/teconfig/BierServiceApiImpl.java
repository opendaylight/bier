/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.service.impl.teconfig;

import com.google.common.util.concurrent.Futures;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.bier.service.impl.Util;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.BierServiceApiService;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringInput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringOutput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.TargetNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.TargetNodeIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.TargetNodeIdsKey;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.BitstringInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.PathInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfoKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.Bitstring;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.BitstringBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.BitstringKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.FecStackType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.FecStackTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.FecStackTypeKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.bitstring.Bitpositions;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.bitstring.BitpositionsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.FecStackInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.FecStackInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.fec.stack.info.fec.stack.type.ConnectedBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.fec.stack.info.fec.stack.type.LocalDecapBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.TeInfo;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierServiceApiImpl implements BierServiceApiService {

    private static final Logger LOG = LoggerFactory.getLogger(BierServiceApiImpl.class);
    private static final String TOPOLOGY_ID = "example-linkstate-topology";
    private BitStringProcess bitStringProcess;
    private Util util;

    public BierServiceApiImpl(DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry) {
        this.bitStringProcess = new BitStringProcess(dataBroker, rpcConsumerRegistry, null, null);
        this.util = new Util(dataBroker);
    }

    @Override
    public Future<RpcResult<GetTargetBitstringOutput>> getTargetBitstring(GetTargetBitstringInput input) {
        GetTargetBitstringOutputBuilder outputBuilder = new GetTargetBitstringOutputBuilder();

        CreateBierPathOutput createBierPathOutput = transferInputToPath(input);
        if (null == createBierPathOutput) {
            return Futures.immediateFuture(RpcResultBuilder
                    .<GetTargetBitstringOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "Create bier path return null.").build());
        }
        List<Bfer> bferList = createBierPathOutput.getBfer();
        List<TargetNodeIds> targetNodeIdsList = new ArrayList<>();
        for (Bfer bfer : bferList) {
            Channel channel = util.getChannelByName(TOPOLOGY_ID, createBierPathOutput.getChannelName());
            ChannelBuilder channelBuilder = new ChannelBuilder(channel);
            List<EgressNode> egressNodeList = new LinkedList<>();
            for (EgressNode egressNode : channel.getEgressNode()) {
                if (egressNode.getNodeId().equals(bfer.getBferNodeId())) {
                    egressNodeList.add(egressNode);
                    break;
                }
            }
            channelBuilder.setEgressNode(egressNodeList);
            TargetNodeIds targetNodeIds = transferBferToTargetNodeIds(channelBuilder.build(), bfer);
            targetNodeIdsList.add(targetNodeIds);
        }
        outputBuilder.setTargetNodeIds(targetNodeIdsList);
        LOG.info(outputBuilder.toString());
        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
    }

    private CreateBierPathOutput transferInputToPath(GetTargetBitstringInput input) {
        LOG.info("Transfer input to path.");
        Channel channel = util.getChannelByName(input.getTopologyId(), input.getChannelName());
        if (null != channel) {
            List<EgressNode> egressNodeList = channel.getEgressNode();
            List<EgressNode> targetEgressNodeList = new ArrayList<>();
            List<String> targetNodeIdList = new ArrayList<>();
            for (org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.input.TargetNodeIds
                    targetNodeIds : input.getTargetNodeIds()) {
                targetNodeIdList.add(targetNodeIds.getTargetNodeId());
            }
            for (EgressNode egressNode : egressNodeList) {
                if (targetNodeIdList.contains(egressNode.getNodeId())) {
                    targetEgressNodeList.add(egressNode);
                }
            }
            ChannelBuilder channelBuilder = new ChannelBuilder(channel);
            channelBuilder.setEgressNode(targetEgressNodeList);
            Channel targetChannel = channelBuilder.build();
            return bitStringProcess.createBierPathRpc(targetChannel);
        }
        return null;
    }

    private TargetNodeIds transferBferToTargetNodeIds(Channel channel, Bfer bfer) {
        List<Bfer> bferList = new ArrayList<>();
        bferList.add(bfer);

        List<TeInfo> teInfoList = bitStringProcess.getTeInfoFrombferList(channel, bferList);
        LOG.info("Teinfo list size of target node {} is {}.", bfer.getBferNodeId(), teInfoList.size());
        SubDomainId subdomainId = teInfoList.get(0).getTeSubdomain().get(0).getSubdomainId();
        Bsl bsl = Bsl.forValue(teInfoList.get(0).getTeSubdomain().get(0).getTeBsl().get(0).getFwdBsl() / 64);
        Si si = new Si(teInfoList.get(0).getTeSubdomain().get(0).getTeBsl().get(0).getTeSi().get(0).getSi().getValue());

        List<Bitstring> bitStringList = getBitstrings(teInfoList, si);

        BitstringInfoBuilder bitstringInfoBuilder = new BitstringInfoBuilder();
        bitstringInfoBuilder.setBierTeSubdomainid(subdomainId);

        BierTeBpInfoBuilder bierTeBpInfoBuilder = new BierTeBpInfoBuilder();
        bierTeBpInfoBuilder.setBierTeBsl(bsl);
        bierTeBpInfoBuilder.setBitstring(bitStringList);
        List<FecStackType> fecStackTypeList = getFecStackTypeList(bfer, teInfoList, si);
        bierTeBpInfoBuilder.setFecStackType(fecStackTypeList);
        bierTeBpInfoBuilder.setKey(new BierTeBpInfoKey(bsl));
        List<BierTeBpInfo> bierTeBpInfoList = new ArrayList<>();
        bierTeBpInfoList.add(bierTeBpInfoBuilder.build());
        bitstringInfoBuilder.setBierTeBpInfo(bierTeBpInfoList);

        PathInfoBuilder pathInfoBuilder = new PathInfoBuilder();
        pathInfoBuilder.setPathLink(bfer.getBierPath().getPathLink());
        pathInfoBuilder.setPathMetric(bfer.getBierPath().getPathMetric());

        TargetNodeIdsBuilder targetNodeIdsBuilder = new TargetNodeIdsBuilder();
        targetNodeIdsBuilder.setBitstringInfo(bitstringInfoBuilder.build());
        targetNodeIdsBuilder.setTargetNodeId(bfer.getBferNodeId());
        targetNodeIdsBuilder.setPathInfo(pathInfoBuilder.build());
        targetNodeIdsBuilder.setKey(new TargetNodeIdsKey(bfer.getBferNodeId()));

        return targetNodeIdsBuilder.build();

    }

    private List<Bitstring> getBitstrings(List<TeInfo> teInfoList, Si si) {
        BitstringBuilder bitstringBuilder = new BitstringBuilder();
        List<Bitpositions> bitpositionsList = new ArrayList<>();
        for (TeInfo teInfo : teInfoList) {
            BitpositionsBuilder bitpositionsBuilder = new BitpositionsBuilder();
            BitString bitString = teInfo.getTeSubdomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                    .getTeFIndex().get(0).getTeFIndex();
            bitpositionsBuilder.setBitposition(bitString);
            bitpositionsList.add(bitpositionsBuilder.build());
        }
        bitstringBuilder.setSi(si);
        bitstringBuilder.setKey(new BitstringKey(si));
        bitstringBuilder.setBitpositions(bitpositionsList);
        List<Bitstring> bitStringList = new ArrayList<>();
        bitStringList.add(bitstringBuilder.build());
        return bitStringList;
    }


    private List<FecStackType> getFecStackTypeList(Bfer bfer, List<TeInfo> teInfoList, Si si) {
        FecStackTypeBuilder fecStackTypeBuilder = new FecStackTypeBuilder();
        fecStackTypeBuilder.setSi(si);
        fecStackTypeBuilder.setKey(new FecStackTypeKey(si));
        List<FecStackInfo> fecStackInfoList = new ArrayList<>();
        for (int i = 0;i < teInfoList.size() - 1;i++) {
            TeInfo teInfo = teInfoList.get(i);
            FecStackInfoBuilder fecStackInfoBuilder = new FecStackInfoBuilder();
            BitString bitString = teInfo.getTeSubdomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                    .getTeFIndex().get(0).getTeFIndex();

            fecStackInfoBuilder.setBitposition(bitString);
            PathLink pathLink = bfer.getBierPath().getPathLink().get(i);
            String nodeId = pathLink.getLinkDest().getDestNode();
            String tpId = pathLink.getLinkDest().getDestTp();
            List<BierTerminationPoint> terminationPointList = util.getBierNodeByNodeId(TOPOLOGY_ID, nodeId)
                    .getBierTerminationPoint();
            for (BierTerminationPoint terminationPoint : terminationPointList) {
                if (terminationPoint.getTpId().equals(tpId)) {
                    ConnectedBuilder connectedBuilder = new ConnectedBuilder();
                    connectedBuilder.setLocalBfr(util.getIpAddressByNodeId(TOPOLOGY_ID, nodeId));
                    connectedBuilder.setLocalInterface(terminationPoint.getIfName());
                    fecStackInfoBuilder.setFecStackType(connectedBuilder.build());
                    break;
                }
            }
            fecStackInfoList.add(fecStackInfoBuilder.build());
        }
        TeInfo teInfo = teInfoList.get(teInfoList.size() - 1);
        FecStackInfoBuilder fecStackInfoBuilder = new FecStackInfoBuilder();
        BitString bitString = teInfo.getTeSubdomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeFIndex().get(0).getTeFIndex();

        fecStackInfoBuilder.setBitposition(bitString);
        String nodeId = bfer.getBferNodeId();
        LocalDecapBuilder localDecapBuilder = new LocalDecapBuilder();
        localDecapBuilder.setBfer(util.getIpAddressByNodeId(TOPOLOGY_ID, nodeId));
        fecStackInfoBuilder.setFecStackType(localDecapBuilder.build());
        fecStackInfoList.add(fecStackInfoBuilder.build());
        fecStackTypeBuilder.setFecStackInfo(fecStackInfoList);
        List<FecStackType> fecStackTypeList = new ArrayList<>();
        fecStackTypeList.add(fecStackTypeBuilder.build());
        return fecStackTypeList;
    }
}
