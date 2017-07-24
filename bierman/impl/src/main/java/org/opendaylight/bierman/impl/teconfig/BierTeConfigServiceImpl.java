/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl.teconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.bierman.impl.BierDataManager;
import org.opendaylight.bierman.impl.RpcUtil;
import org.opendaylight.bierman.impl.bierconfig.BierConfigServiceImpl;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.BierTeConfigApiService;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeLabelInput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeLabelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeLabelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeBpInput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeBpOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeBpOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeBslInput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeBslOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeBslOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeLabelInput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeLabelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeLabelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeSiInput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeSiOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeSiOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRangeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTeConfigServiceImpl implements BierTeConfigApiService {
    private static final Logger LOG = LoggerFactory.getLogger(BierConfigServiceImpl.class);

    private BierDataManager topoManager;

    public BierTeConfigServiceImpl(BierDataManager topoManager) {
        this.topoManager = topoManager;
    }

    public Future<RpcResult<ConfigureTeNodeOutput>> configureTeNode(ConfigureTeNodeInput input) {
        LOG.info("ConfigureTeNodeInput" + input);
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

        if (!topoManager.checkTpId(topologyId,nodeBuilder.build())) {
            LOG.info("checkTpId........false");
            builder.setConfigureResult(RpcUtil.getConfigResult(false,
                    "node tp-id or bitposition is exist in same si!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!topoManager.checkFtLabel(topologyId,nodeBuilder.build())) {
            LOG.info("Si not exisit");
            if (!topoManager.checkLabelExist(node)) {
                builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Label is not exist!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
            LOG.info("build label");
            Long ftLabelLong = topoManager.buildFtLabel(topologyId,nodeBuilder.build());
            if (ftLabelLong == -1L) {
                builder.setConfigureResult(RpcUtil.getConfigResult(false,
                        "LabelRange for generate ft-label is use up!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
            MplsLabel ftLabel = new MplsLabel(ftLabelLong);
            Si si = input.getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0).getSi();

            TeSiBuilder teSiBuilder = new TeSiBuilder();
            teSiBuilder.setTeBp(input.getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0)
                    .getTeSi().get(0).getTeBp());
            teSiBuilder.setKey(new TeSiKey(si));
            teSiBuilder.setSi(si);
            teSiBuilder.setFtLabel(ftLabel);
            List<TeSi> teSiList = new ArrayList<TeSi>();
            teSiList.add(teSiBuilder.build());

            Bsl teBsl = input.getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getBitstringlength();
            TeBslBuilder teBslBuilder = new TeBslBuilder();
            teBslBuilder.setTeSi(teSiList);
            teBslBuilder.setBitstringlength(teBsl);
            teBslBuilder.setKey(new TeBslKey(teBsl));
            List<TeBsl> teBslList = new ArrayList<TeBsl>();
            teBslList.add(teBslBuilder.build());

            SubDomainId teSubDomainId = input.getTeDomain().get(0).getTeSubDomain().get(0).getSubDomainId();
            TeSubDomainBuilder teSubDomainBuilder = new TeSubDomainBuilder();
            teSubDomainBuilder.setTeBsl(teBslList);
            teSubDomainBuilder.setSubDomainId(teSubDomainId);
            teSubDomainBuilder.setKey(new TeSubDomainKey(teSubDomainId));
            List<TeSubDomain> teSubDomainList = new ArrayList<TeSubDomain>();
            teSubDomainList.add(teSubDomainBuilder.build());

            DomainId teDomainId = input.getTeDomain().get(0).getDomainId();
            TeDomainBuilder teDomainBuilder = new TeDomainBuilder();
            teDomainBuilder.setTeSubDomain(teSubDomainList);
            teDomainBuilder.setDomainId(teDomainId);
            teDomainBuilder.setKey(new TeDomainKey(teDomainId));
            List<TeDomain> teDomainList = new ArrayList<TeDomain>();
            teDomainList.add(teDomainBuilder.build());

            BierTeNodeParamsBuilder bierTeNodeParamsBuilder = new BierTeNodeParamsBuilder();
            bierTeNodeParamsBuilder.setTeDomain(teDomainList);
            //BierNodeBuilder nodeBuilder = new BierNodeBuilder(node);
            nodeBuilder.setBierTeNodeParams(bierTeNodeParamsBuilder.build());
            LOG.info("Build TeNode" + nodeBuilder.build());
        }

        if (!topoManager.setNodeData(topologyId, nodeBuilder.build())) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"write node to datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();

    }

    public Future<RpcResult<DeleteTeBslOutput>> deleteTeBsl(DeleteTeBslInput input) {
        DeleteTeBslOutputBuilder builder = new DeleteTeBslOutputBuilder();

        String bslErrorCause = checkTeNode(input,input.getTopologyId(),input.getDomainId(),
                input.getSubDomainId(), input.getNodeId());
        if (!bslErrorCause.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,bslErrorCause));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode nodeForBsl = topoManager.getNodeData(input.getTopologyId(),input.getNodeId());
        if (!topoManager.checkBitstringlengthExist(input.getTopologyId(),input.getDomainId(),
                input.getSubDomainId(), input.getBitstringlength(),nodeForBsl)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Bsl is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.delTeBslFromNode(input.getTopologyId(),input.getDomainId(),
                input.getSubDomainId(), input.getBitstringlength(),nodeForBsl)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,
                    "delete Te-Bsl form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteTeSiOutput>> deleteTeSi(DeleteTeSiInput input) {
        DeleteTeSiOutputBuilder builder = new DeleteTeSiOutputBuilder();

        String siErrorCause = checkTeNode(input,input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getNodeId());
        if (!siErrorCause.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,siErrorCause));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode nodeForSi = topoManager.getNodeData(input.getTopologyId(),input.getNodeId());
        if (!topoManager.checkBitstringlengthExist(input.getTopologyId(),
                input.getDomainId(),input.getSubDomainId(), input.getBitstringlength(),nodeForSi)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,
                    "Te-Bsl is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.checkSiExist(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),input.getSi(),nodeForSi)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Si is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.delTeSiFromNode(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),input.getSi(),nodeForSi)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"delete Te-Si form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteTeBpOutput>> deleteTeBp(DeleteTeBpInput input) {
        DeleteTeBpOutputBuilder builder = new DeleteTeBpOutputBuilder();

        String inputError = checkTeNode(input,input.getTopologyId(),
                input.getDomainId(),input.getSubDomainId(), input.getNodeId());
        if (!inputError.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,inputError));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode nodeForBp = topoManager.getNodeData(input.getTopologyId(),input.getNodeId());
        if (!topoManager.checkBitstringlengthExist(input.getTopologyId(),
                input.getDomainId(),input.getSubDomainId(), input.getBitstringlength(),nodeForBp)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Bsl is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.checkSiExist(input.getTopologyId(),input.getDomainId(),
                input.getSubDomainId(), input.getBitstringlength(),input.getSi(),nodeForBp)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,
                    "Te-Si is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.checkTpIdExist(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),input.getSi(),input.getTpId(),nodeForBp)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Bp is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (!topoManager.delTeBpFromNode(input.getTopologyId(),input.getDomainId(),input.getSubDomainId(),
                input.getBitstringlength(),input.getSi(),input.getTpId(),nodeForBp)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"delete Te-Bp form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<ConfigureTeLabelOutput>> configureTeLabel(ConfigureTeLabelInput input) {
        ConfigureTeLabelOutputBuilder builder = new ConfigureTeLabelOutputBuilder();

        if (null == input) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String nodeId = input.getNodeId();
        String topologyId = input.getTopologyId();

        if (nodeId == null || nodeId.equals("") || topologyId == null || topologyId.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode bierNode = topoManager.getNodeData(topologyId, nodeId);
        if (bierNode == null) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"node is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNodeBuilder nodeBuilder = new BierNodeBuilder(bierNode);
        BierTeLableRangeBuilder teLableRangeBuilder = new BierTeLableRangeBuilder();
        teLableRangeBuilder.setLabelBase(input.getLabelBase());
        teLableRangeBuilder.setLabelRangeSize(input.getLabelRangeSize());
        nodeBuilder.setBierTeLableRange(teLableRangeBuilder.build());

        String errorMsg = topoManager.checkLableRangeParams(teLableRangeBuilder);
        if (!errorMsg.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,errorMsg));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!topoManager.setNodeData(topologyId, nodeBuilder.build())) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"write node to datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();

    }

    public Future<RpcResult<DeleteTeLabelOutput>> deleteTeLabel(DeleteTeLabelInput input) {
        DeleteTeLabelOutputBuilder builder = new DeleteTeLabelOutputBuilder();
        if (input == null) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        String topoId = input.getTopologyId();
        String nodeId = input.getNodeId();
        if (topoId == null || topoId.equals("") || nodeId == null || nodeId.equals("")) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode node = topoManager.getNodeData(input.getTopologyId(),input.getNodeId());
        if (!topoManager.checkLabelExist(node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"Te-Label is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!topoManager.deleteTeLabel(topoId,node)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"delete Te Label form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public <T> String checkTeNode(T input, String topologyId, DomainId domainId,
                                  SubDomainId subDomainId, String nodeId) {
        if (null == input) {
            return ("input is null!");
        }

        if (topologyId == null || topologyId.equals("") || domainId == null || subDomainId == null
                || nodeId == null || nodeId.equals("")) {
            return ("input param is error!");
        }

        BierNode teNode = topoManager.getNodeData(topologyId, nodeId);
        if (teNode == null) {
            return ("node is not exist!");
        }

        if (!topoManager.checkNodeBelongToTeDomain(domainId,subDomainId,teNode)) {
            return ("node is not belong to domain or subdomain!");
        }

        return "";
    }

}