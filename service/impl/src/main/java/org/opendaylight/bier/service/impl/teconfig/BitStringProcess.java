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
import org.opendaylight.bier.service.impl.NotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPathUpdate;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceService;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
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

    public static final int PATH_ADD = 1;
    public static final int PATH_REMOVE = 2;

    public BitStringProcess(final DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry,
                            BierTeBitstringWriter bierTeBitstringWriter,
                            BierTeBiftWriter bierTeBiftWriter) {
        this.dataBroker = dataBroker;
        this.notificationProvider = new NotificationProvider();
        this.rpcConsumerRegistry = rpcConsumerRegistry;
        this.bierTeBitstringWriter = bierTeBitstringWriter;
        this.biftInfoProcess = new BiftInfoProcess(dataBroker,bierTeBiftWriter);
    }

    public boolean bierTeBitStringProcess(Channel channel,int path_compute_type) {
        if (!checkPathComputeInput(channel)) {
            LOG.error("Path compute input error!");
            return false;
        }
        List<String> tpList = computePathByPce(channel,path_compute_type);
        if (null != tpList && !tpList.isEmpty()) {
            List<String> bitStringList = computeBitStringList(tpList);
            return setBitStringConfigToSouthbound(bitStringList);
        }
        return true;
    }

    public boolean updateBitStringList(String channelName, BierPathUpdate bierPath) {
        return true;
    }

    public List<Long> getPathIdList(String channelName) {
        List<Long> pathIdList = new ArrayList<>();
        return pathIdList;
    }

    private List<String> computePathByPce(Channel channel,int path_compute_type) {
        List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer> bferList = new ArrayList<>();
        if (path_compute_type == BitStringProcess.PATH_ADD) {
            CreateBierPathOutput output = createBierPathRpc(channel);
            if (null != output) {
                bferList = output.getBfer();
            } else {
                LOG.error("CreateBierPath Rpc failed");
            }
        } else if (path_compute_type == BitStringProcess.PATH_REMOVE) {
        }
        return getTpListFromOutput(bferList);
    }

    private List<String> removeBierTePaths(Channel channel) {
        return null;
    }

    private List<String> computeBitStringList(List<String> tpList) {
        List<TeBp> bpList = new ArrayList<>();
        return null;
    }

    private boolean setBitStringConfigToSouthbound(List<String> bitStringList) {
        return true;
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

    private CreateBierPathOutput createBierPathRpc(Channel channel) {

        CreateBierPathInputBuilder createBierPathInputBuilder = new CreateBierPathInputBuilder();
        createBierPathInputBuilder.setBfirNodeId(channel.getIngressNode());
        List<Bfer> bferList = getBferListFromEgressNode(channel.getEgressNode());
        if (null != bferList && !bferList.isEmpty()) {
            createBierPathInputBuilder.setBfer(bferList);
        }
        createBierPathInputBuilder.setChannelName(channel.getName());

        Future<RpcResult<CreateBierPathOutput>>  future = rpcConsumerRegistry.getRpcService(BierPceService.class)
                .createBierPath(createBierPathInputBuilder.build());
        try {
            LOG.info("Add path computation!");
            CreateBierPathOutput output = future.get().getResult();
            if (null != output) {
                LOG.info("Path computation succeed!");
                return output;
            }
            LOG.error("Path computation failer!");
            return null;

        } catch (InterruptedException e) {
            LOG.error("ZTE:Set Optical Node is Interrupted by", e);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
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

    private List<String> getTpListFromOutput(List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer>
                                                     bferList) {
        if (null != bferList && !bferList.isEmpty()) {
            return null;
        }
        List<String> tpList = new ArrayList<>();
        for (org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer bfer : bferList) {
            List<PathLink> pathLinkList = bfer.getBierPath().getPathLink();
            for (PathLink pathLink : pathLinkList) {
                tpList.add(pathLink.getLinkSource().getSourceTp());
                tpList.add(pathLink.getLinkDest().getDestTp());
            }
        }
        return tpList;
    }


}
