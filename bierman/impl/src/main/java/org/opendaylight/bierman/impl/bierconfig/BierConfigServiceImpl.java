/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl.bierconfig;

import java.util.concurrent.Future;

import org.opendaylight.bierman.impl.BierDataManager;
import org.opendaylight.bierman.impl.RpcUtil;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.BierConfigApiService;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.ConfigureNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.ConfigureNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.ConfigureNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteIpv4Input;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteIpv4Output;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteIpv4OutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteIpv6Input;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteIpv6Output;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteIpv6OutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.delete.ipv4.input.Ipv4;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.delete.ipv6.input.Ipv6;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierConfigServiceImpl implements BierConfigApiService {
    private static final Logger LOG = LoggerFactory.getLogger(BierConfigServiceImpl.class);

    private BierDataManager topoManager;

    public BierConfigServiceImpl(BierDataManager topoManager) {
        this.topoManager = topoManager;
    }

    public Future<RpcResult<ConfigureNodeOutput>> configureNode(ConfigureNodeInput input) {
        ConfigureNodeOutputBuilder configureBuilder = new ConfigureNodeOutputBuilder();
        if (null == input) {
            configureBuilder.setConfigureResult(RpcUtil.getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(configureBuilder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        String nodeId = input.getNodeId();
        if (topologyId == null || topologyId.equals("") || nodeId == null || nodeId.equals("")) {
            configureBuilder.setConfigureResult(RpcUtil.getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(configureBuilder.build()).buildFuture();
        }

        BierNode node = topoManager.getNodeData(topologyId, nodeId);
        if (node == null) {
            configureBuilder.setConfigureResult(RpcUtil.getConfigResult(false,"node is not exist!"));
            return RpcResultBuilder.success(configureBuilder.build()).buildFuture();
        }

        BierNodeBuilder nodeBuilder = new BierNodeBuilder(node);
        BierNodeParamsBuilder nodeParamsBuilder = new BierNodeParamsBuilder();
        nodeParamsBuilder.setDomain(input.getDomain());
        nodeBuilder.setBierNodeParams(nodeParamsBuilder.build());

        String errorMsg = topoManager.checkBierNodeParams(node,nodeParamsBuilder);
        if (!errorMsg.equals("")) {
            configureBuilder.setConfigureResult(RpcUtil.getConfigResult(false,errorMsg));
            return RpcResultBuilder.success(configureBuilder.build()).buildFuture();
        }

        if (!topoManager.checkDomainExist(topologyId,input.getDomain())) {
            configureBuilder.setConfigureResult(RpcUtil.getConfigResult(false,"domain or subdomain is not exist!"));
            return RpcResultBuilder.success(configureBuilder.build()).buildFuture();
        }

        if (!topoManager.checkNodeBfrId(topologyId,nodeBuilder.build())) {
            configureBuilder.setConfigureResult(RpcUtil.getConfigResult(false,
                    "node bfrId is exist in same subdomain!"));
            return RpcResultBuilder.success(configureBuilder.build()).buildFuture();
        }

        if (!topoManager.checkNodeLabel(node,nodeBuilder.build())) {
            configureBuilder.setConfigureResult(RpcUtil.getConfigResult(false,"node label range is overlapped!"));
            return RpcResultBuilder.success(configureBuilder.build()).buildFuture();
        }

        if (!topoManager.setNodeData(topologyId, nodeBuilder.build())) {
            configureBuilder.setConfigureResult(RpcUtil.getConfigResult(false,"write node to datastore failed!"));
            return RpcResultBuilder.success(configureBuilder.build()).buildFuture();
        }

        configureBuilder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(configureBuilder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteNodeOutput>> deleteNode(DeleteNodeInput input) {
        DeleteNodeOutputBuilder builder = new DeleteNodeOutputBuilder();

        String errorCause = checkNode(input,input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getNodeId());
        if (!errorCause.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,errorCause));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode node = topoManager.getNodeData(input.getTopologyId(), input.getNodeId());
        if (!topoManager.delNodeFromDomain(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"delete node form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteIpv4Output>> deleteIpv4(DeleteIpv4Input input)  {
        DeleteIpv4OutputBuilder builder = new  DeleteIpv4OutputBuilder();

        String errorCause = checkNode(input,input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getNodeId());
        if (!errorCause.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,errorCause));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        Ipv4 ipv4 = input.getIpv4();
        BierNode node = topoManager.getNodeData(input.getTopologyId(),input.getNodeId());
        if (!topoManager.checkIpv4Exist(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                ipv4.getBitstringlength(), ipv4.getBierMplsLabelBase(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"ipv4 is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!topoManager.delIpv4FromNode(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                ipv4.getBitstringlength(), ipv4.getBierMplsLabelBase(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"delete ipv4 from datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();

    }

    public Future<RpcResult<DeleteIpv6Output>> deleteIpv6(DeleteIpv6Input input) {
        DeleteIpv6OutputBuilder builder = new  DeleteIpv6OutputBuilder();

        String errorCause = checkNode(input,input.getTopologyId(),input.getDomainId(),
                input.getSubDomainId(),input.getNodeId());
        if (!errorCause.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,errorCause));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        Ipv6 ipv6 = input.getIpv6();
        BierNode node = topoManager.getNodeData(input.getTopologyId(), input.getNodeId());
        if (!topoManager.checkIpv6Exist(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                ipv6.getBitstringlength(), ipv6.getBierMplsLabelBase(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"ipv6 is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!topoManager.delIpv6FromNode(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                ipv6.getBitstringlength(), ipv6.getBierMplsLabelBase(),node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"delete ipv6 from datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public <T> String checkNode(T input,String topologyId,DomainId domainId,SubDomainId subDomainId,String nodeId) {
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

        if (!topoManager.checkNodeBelongToDomain(domainId,subDomainId,node)) {
            return ("node is not belong to domain or subdomain!");
        }

        return "";
    }
}