/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl.teconfig;

import org.opendaylight.bierman.impl.BierDataManager;
import org.opendaylight.bierman.impl.RpcUtil;
import org.opendaylight.bierman.impl.bierconfig.BierConfigServiceImpl;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.*;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class BierTeConfigServiceImpl implements BierTeConfigApiService {
    private static final Logger LOG = LoggerFactory.getLogger(BierConfigServiceImpl.class);

    private BierDataManager topoManager;

    public BierTeConfigServiceImpl(BierDataManager topoManager) {
        this.topoManager = topoManager;
    }

    public Future<RpcResult<ConfigureTeNodeOutput>> configureTeNode(ConfigureTeNodeInput input){
        ConfigureTeNodeOutputBuilder builder = new ConfigureTeNodeOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        String nodeId = input.getNodeId();
        if (topologyId == null || topologyId.equals("") || nodeId == null || nodeId.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode node = topoManager.getNodeData(topologyId, nodeId);
        if (node == null) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"node is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNodeBuilder nodeBuilder = new BierNodeBuilder(node);
        BierTeNodeParamsBuilder teNodeParamsBuilder = new BierTeNodeParamsBuilder();
        teNodeParamsBuilder.setTeDomain(input.getTeDomain());
        nodeBuilder.setBierTeNodeParams(teNodeParamsBuilder.build());

        String errorMsg = topoManager.checkBierTeNodeParams(node,teNodeParamsBuilder);
        if (!errorMsg.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,errorMsg));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!topoManager.checkTeDomainExist(topologyId,input.getTeDomain())) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"domain or subdomain is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }




        if (!topoManager.setNodeData(topologyId, nodeBuilder.build())) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"write node to datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();

    }

    public Future<RpcResult<DeleteTeBslOutput>> deleteTeBsl(DeleteTeBslInput input){
        DeleteTeBslOutputBuilder builder = new DeleteTeBslOutputBuilder();

        String errorCause = checkNode(input,input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getNodeId());
        if (!errorCause.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,errorCause));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode node = topoManager.getNodeData(input.getTopologyId(),input.getNodeId());
        if (!topoManager.checkBitstringlengthExist(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Bsl is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.delTeBslFromNode(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),input.getBitstringlength(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"delete Te-Bsl form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteTeSiOutput>> deleteTeSi(DeleteTeSiInput input){
        DeleteTeSiOutputBuilder builder = new DeleteTeSiOutputBuilder();

        String errorCause = checkNode(input,input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getNodeId());
        if (!errorCause.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,errorCause));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode node = topoManager.getNodeData(input.getTopologyId(),input.getNodeId());
        if (!topoManager.checkBitstringlengthExist(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Bsl is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.checkSiExist(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),input.getSi(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Si is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.delTeSiFromNode(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),input.getSi(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"delete Te-Si form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteTeBpOutput>> deleteTeBp(DeleteTeBpInput input){
        DeleteTeBpOutputBuilder builder = new DeleteTeBpOutputBuilder();

        String errorCause = checkNode(input,input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getNodeId());
        if (!errorCause.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,errorCause));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode node = topoManager.getNodeData(input.getTopologyId(),input.getNodeId());
        if (!topoManager.checkBitstringlengthExist(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Bsl is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.checkSiExist(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),input.getSi(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Si is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.checkTpIdExist(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),input.getSi(),input.getTpId(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Bp is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.delTeBpFromNode(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),input.getSi(),input.getTpId(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"delete Te-Bp form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<ConfigureTeLabelOutput>> configureTeLabel(ConfigureTeLabelInput input) {
        ConfigureTeLabelOutputBuilder builder = new ConfigureTeLabelOutputBuilder();

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();

    }

    public <T> String checkNode(T input, String topologyId, DomainId domainId, SubDomainId subDomainId, String nodeId) {
        if (null == input) {
            return ("input is null!");
        }

        if (topologyId == null || topologyId.equals("") || domainId == null || subDomainId == null
                || nodeId == null || nodeId.equals("")) {
            return ("input param is error!");
        }

        BierNode node = topoManager.getNodeData(topologyId, nodeId);
        if (node == null) {
            return ("node is not exist!");
        }

        if (!topoManager.checkNodeBelongToTeDomain(domainId,subDomainId,node)) {
            return ("node is not belong to domain or subdomain!");
        }

        return "";
    }

}