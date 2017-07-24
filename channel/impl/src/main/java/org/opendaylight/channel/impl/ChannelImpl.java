/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.impl;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.channel.check.AddChannelInputCheck;
import org.opendaylight.channel.check.CheckResult;
import org.opendaylight.channel.check.DeployChannelInputCheck;
import org.opendaylight.channel.check.ModifyChannelInputCheck;
import org.opendaylight.channel.check.RemoveChannelInputCheck;
import org.opendaylight.channel.util.ChannelDBContext;
import org.opendaylight.channel.util.ChannelDBUtil;
import org.opendaylight.channel.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.AddChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.AddChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.AddChannelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.BierChannelApiService;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.GetChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.GetChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.GetChannelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelWithPortInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelWithPortOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelWithPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.get.channel.output.ChannelName;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.get.channel.output.ChannelNameBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel.egress.node.RcvTpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.with.port.output.QueryChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.with.port.output.QueryChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResultBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelImpl implements BierChannelApiService {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelImpl.class);
    private ChannelDBUtil channelDBUtil = ChannelDBUtil.getInstance();


    ChannelImpl(ChannelDBContext context) {
        channelDBUtil.setContext(context);
    }

    @Override
    public Future<RpcResult<GetChannelOutput>> getChannel(GetChannelInput input) {
        LOG.info("Channel:get-channel {}", input);
        GetChannelOutputBuilder output = new GetChannelOutputBuilder();
        if (input == null) {
            return RpcReturnUtil.returnErr("input is null!");
        }
        List<String> channels = channelDBUtil.getChannels(input.getTopologyId());
        output.setChannelName(buildOutputChannelName(channels));
        return RpcReturnUtil.returnRpcResult(output.build());
    }

    private List<ChannelName> buildOutputChannelName(List<String> channels) {
        List<ChannelName> channelNameList = new ArrayList<>();
        for (String channelName :channels) {
            channelNameList.add(new ChannelNameBuilder().setName(channelName).build());
        }
        return channelNameList;
    }

    @Override
    public Future<RpcResult<ModifyChannelOutput>> modifyChannel(ModifyChannelInput input) {
        LOG.info("Channel:modify-channel {}", input);
        ModifyChannelOutputBuilder output = new ModifyChannelOutputBuilder();
        ModifyChannelInputCheck modifyCheck = new ModifyChannelInputCheck(input);
        CheckResult checkInputResult = modifyCheck.check();
        if (checkInputResult.isInputIllegal()) {
            output.setConfigureResult(
                    buildConfigResult(ConfigureResult.Result.FAILURE,checkInputResult.getErrorCause()));
            return RpcReturnUtil.returnRpcResult(output.build());
        }

        if (!channelDBUtil.modifyChannelToDB(input)) {
            output.setConfigureResult(
                    buildConfigResult(ConfigureResult.Result.FAILURE,"modify Channel to DB fail!"));
            return RpcReturnUtil.returnRpcResult(output.build());
        }
        output.setConfigureResult(buildConfigResult(ConfigureResult.Result.SUCCESS,""));
        return RpcReturnUtil.returnRpcResult(output.build());
    }

    @Override
    public Future<RpcResult<QueryChannelWithPortOutput>> queryChannelWithPort(QueryChannelWithPortInput input) {
        if (input == null || input.getDomainId() == null || input.getSubDomainId() == null
                || input.getNodeId() == null || input.getTpId() == null) {
            return RpcReturnUtil.returnErr("input is null, or domain, sub-domain, node-id, tp-id is null!");
        }
        Optional<BierChannel> bierChannel = channelDBUtil.readBierChannel(channelDBUtil.DEFAULT_TOPO_ID);
        List<QueryChannel> outputChannels = new ArrayList<>();
        if (bierChannel != null && bierChannel.isPresent()) {
            List<Channel> channels = bierChannel.get().getChannel();
            if (!channels.isEmpty()) {
                for (Channel  channel : channels) {
                    if (channelDBUtil.hasChannelDeplyed(channel.getName(),null)) {
                        findTpFromChannel(input, channel, outputChannels);
                    }
                }
            }
        }
        QueryChannelWithPortOutput output = new QueryChannelWithPortOutputBuilder()
                .setQueryChannel(outputChannels).build();
        return RpcReturnUtil.returnRpcResult(output);
    }

    private void findTpFromChannel(QueryChannelWithPortInput input, Channel channel,
                                   List<QueryChannel> outputChannels) {
        if (channel.getDomainId().equals(input.getDomainId())
                && channel.getSubDomainId().equals(input.getSubDomainId())) {
            if (isSrcTp(input, channel)) {
                addChannelInfo(channel,outputChannels,false);
            } else {
                for (EgressNode egressNode : channel.getEgressNode()) {
                    if (isRcvTp(input, egressNode)) {
                        addChannelInfo(channel, outputChannels,true);
                        break;
                    }
                }
            }
        }
    }

    private boolean isRcvTp(QueryChannelWithPortInput input, EgressNode egressNode) {
        return egressNode.getNodeId().equals(input.getNodeId())
                && isTpInRcvTpList(input.getTpId(),egressNode.getRcvTp());
    }

    private boolean isTpInRcvTpList(String tpId, List<RcvTp> rcvTps) {
        if (rcvTps != null) {
            for (RcvTp rcvTp : rcvTps) {
                if (rcvTp.getTp().equals(tpId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addChannelInfo(Channel channel,
                                List<QueryChannel> outputChannels, boolean isRcvTp) {
        outputChannels.add(new QueryChannelBuilder()
                .setChannelName(channel.getName())
                .setBfir(channel.getIngressNode())
                .setIsRcvTp(isRcvTp).build());
    }

    private boolean isSrcTp(QueryChannelWithPortInput input, Channel channel) {
        return channel.getIngressNode().equals(input.getNodeId()) && channel.getSrcTp().equals(input.getTpId());
    }

    public ConfigureResult buildConfigResult(ConfigureResult.Result result, String errorMsg) {
        return  new ConfigureResultBuilder().setResult(result).setErrorCause(errorMsg).build();
    }

    @Override
    public Future<RpcResult<DeployChannelOutput>> deployChannel(DeployChannelInput input) {
        LOG.info("Channel:deploy-channel {}", input);

        DeployChannelInputCheck deployCheck = new DeployChannelInputCheck(input);
        DeployChannelOutputBuilder output = new DeployChannelOutputBuilder();
        CheckResult checkInputResult = deployCheck.check();
        if (checkInputResult.isInputIllegal()) {
            output.setConfigureResult(
                    buildConfigResult(ConfigureResult.Result.FAILURE,checkInputResult.getErrorCause()));
            return RpcReturnUtil.returnRpcResult(output.build());
        }
        if (!channelDBUtil.writeDeployChannelToDB(input)) {
            output.setConfigureResult(
                    buildConfigResult(ConfigureResult.Result.FAILURE,"Write Deploy-Channel to DB fail!"));
            return RpcReturnUtil.returnRpcResult(output.build());
        }
        output.setConfigureResult(buildConfigResult(ConfigureResult.Result.SUCCESS,""));
        return RpcReturnUtil.returnRpcResult(output.build());
    }

    @Override
    public Future<RpcResult<RemoveChannelOutput>> removeChannel(RemoveChannelInput input) {
        LOG.info("Channel:remove-channel {}", input);
        RemoveChannelOutputBuilder output = new RemoveChannelOutputBuilder();
        RemoveChannelInputCheck removeCheck = new RemoveChannelInputCheck(input);
        CheckResult checkInputResult = removeCheck.check();
        if (checkInputResult.isInputIllegal()) {
            output.setConfigureResult(
                    buildConfigResult(ConfigureResult.Result.FAILURE,checkInputResult.getErrorCause()));
            return RpcReturnUtil.returnRpcResult(output.build());
        }
        if (!channelDBUtil.deleteChannelFromDB(input)) {
            output.setConfigureResult(buildConfigResult(ConfigureResult.Result.FAILURE,"Delete Channel from DB fail!"));
            return RpcReturnUtil.returnRpcResult(output.build());
        }
        output.setConfigureResult(buildConfigResult(ConfigureResult.Result.SUCCESS,""));
        return RpcReturnUtil.returnRpcResult(output.build());
    }

    @Override
    public Future<RpcResult<QueryChannelOutput>> queryChannel(QueryChannelInput input) {
        LOG.info("Channel:Query-channel {}", input);
        QueryChannelOutputBuilder output = new QueryChannelOutputBuilder();
        if (input == null || input.getChannelName() == null || input.getChannelName().isEmpty()) {
            return RpcReturnUtil.returnErr("input or channel-name is null!");
        }
        List<Channel> channels = channelDBUtil.queryChannels(input);
        output.setChannel(buildOutputChannels(channels));
        return RpcReturnUtil.returnRpcResult(output.build());
    }

    private List<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102
            .query.channel.output.Channel> buildOutputChannels(List<Channel> channels) {
        List<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.Channel>
                channelList = new ArrayList<>();
        for (Channel channel: channels) {
            org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.ChannelBuilder
                    channelBuilder = new ChannelBuilder(channel);
            channelBuilder.setIngressNode(channel.getIngressNode());
            channelBuilder.setSrcTp(channel.getSrcTp());
            channelBuilder.setBierForwardingType(channel.getBierForwardingType());
            channelBuilder.setEgressNode(buildEgressNodes(channel.getEgressNode()));
            channelList.add(channelBuilder.build());
        }
        return channelList;
    }

    private List<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel
            .EgressNode> buildEgressNodes(List<EgressNode> egressNode) {
        List<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel.EgressNode>
                egressNodeList = new ArrayList<>();

        if (egressNode != null && !egressNode.isEmpty()) {
            for (EgressNode node : egressNode) {
                List<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel
                        .egress.node.RcvTp> rcvTpList = new ArrayList<>();
                if (node.getRcvTp() != null) {
                    for (RcvTp rcvTp : node.getRcvTp()) {
                        rcvTpList.add(new RcvTpBuilder().setTp(rcvTp.getTp()).build());
                    }
                }
                egressNodeList.add(new EgressNodeBuilder()
                        .setNodeId(node.getNodeId())
                        .setRcvTp(rcvTpList.isEmpty() ? null : rcvTpList)
                        .build());
            }
        }
        return egressNodeList;
    }

    @Override
    public Future<RpcResult<AddChannelOutput>> addChannel(AddChannelInput input) {
        LOG.info("Channel:add-channel {}", input);
        AddChannelOutputBuilder output = new AddChannelOutputBuilder();
        AddChannelInputCheck addCheck = new AddChannelInputCheck(input);
        CheckResult checkInputResult = addCheck.check();
        if (checkInputResult.isInputIllegal()) {
            output.setConfigureResult(
                    buildConfigResult(ConfigureResult.Result.FAILURE,checkInputResult.getErrorCause()));
            return RpcReturnUtil.returnRpcResult(output.build());
        }

        if (!channelDBUtil.writeChannelToDB(input)) {
            output.setConfigureResult(buildConfigResult(ConfigureResult.Result.FAILURE,"Write Channel to DB fail!"));
            return RpcReturnUtil.returnRpcResult(output.build());
        }
        output.setConfigureResult(buildConfigResult(ConfigureResult.Result.SUCCESS,""));
        return RpcReturnUtil.returnRpcResult(output.build());
    }

    public void start() {
        channelDBUtil.initDB();
    }
}
