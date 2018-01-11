/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;

import com.google.common.base.Preconditions;

import java.util.List;

import org.opendaylight.channel.util.ChannelDBUtil;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.deploy.channel.input.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.deploy.channel.input.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierForwardingType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BpAssignmentStrategy;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployChannelInputCheck extends ChannelInputCheck {
    private DeployChannelInput deployChannelInput;
    private Channel channel;
    private static final Logger LOG = LoggerFactory.getLogger(DeployChannelInputCheck.class);

    public DeployChannelInputCheck(DeployChannelInput input) {
        this.deployChannelInput = input;
    }

    @Override
    public CheckResult check() {
        CheckResult result = checkParam();
        if (result.isInputIllegal()) {
            return result;
        }
        channel = ChannelDBUtil.getInstance().getChannelInfo(deployChannelInput.getTopologyId(),
                deployChannelInput.getChannelName()
                );
        if (!checkChannelExist(channel)) {
            return new CheckResult(true, CHANNEL_NOT_EXISTS);
        }
        if (bierForwardingTypeConflict(channel,deployChannelInput)) {
            return new CheckResult(true, FORWARDING_TYPE_CONFLICT);
        }
        if (bierBpStrategyConflict(channel,deployChannelInput)) {
            return new CheckResult(true, STRATEGY_CONFLICT);
        }
        BpAssignmentStrategy bpAssignmentStrategy = getBpStrategy(channel.getBpAssignmentStrategy(),
                deployChannelInput.getBpAssignmentStrategy());
        result = checkNodesInSubdomain(bpAssignmentStrategy);
        if (result.isInputIllegal()) {
            return result;
        }
        result = checkIngressAndEgressNodes();
        if (result.isInputIllegal()) {
            return result;
        }
        return new CheckResult(false, "");
    }

    private BpAssignmentStrategy getBpStrategy(BpAssignmentStrategy oldBpStrategy, BpAssignmentStrategy newBpStrategy) {
        return oldBpStrategy == null ? newBpStrategy : oldBpStrategy;
    }

    private boolean bierBpStrategyConflict(Channel channel, DeployChannelInput deployChannelInput) {
        if (channel.getBpAssignmentStrategy() != null && deployChannelInput.getBpAssignmentStrategy() != null) {
            return !channel.getBpAssignmentStrategy().equals(deployChannelInput.getBpAssignmentStrategy());
        }
        return false;
    }

    private boolean bierForwardingTypeConflict(Channel channel, DeployChannelInput deployChannelInput) {
        if (channel.getBierForwardingType() != null) {
            return !channel.getBierForwardingType().equals(deployChannelInput.getBierForwardingType());
        }
        return false;
    }

    private CheckResult checkIngressAndEgressNodes() {
        String ingressNode = deployChannelInput.getIngressNode();
        List<EgressNode> egressNodeList = deployChannelInput.getEgressNode();
        for (EgressNode egressNode : egressNodeList) {
            if (egressNode.getNodeId().equals(ingressNode)) {
                return new CheckResult(true, INGRESS_EGRESS_CONFLICT);
            }
        }
        return new CheckResult(false, "");
    }

    private CheckResult checkNodesInSubdomain(BpAssignmentStrategy bpAssignmentStrategy) {
        if (!nodeInSubdomain(deployChannelInput.getTopologyId(),deployChannelInput.getIngressNode(),
                channel.getDomainId(),channel.getSubDomainId(),deployChannelInput.getBierForwardingType())) {
            return new CheckResult(true, INGRESS_NOT_IN_SUBDOMIN);
        }
        if (!egressNodesInSubdomain(deployChannelInput.getTopologyId(),deployChannelInput.getEgressNode(),
                channel.getDomainId(),channel.getSubDomainId(),deployChannelInput.getBierForwardingType())) {
            return new CheckResult(true,EGRESS_NOT_IN_SUBDOMIN);
        }
        if (deployChannelInput.getBierForwardingType().equals(BierForwardingType.BierTe)
                && bpAssignmentStrategy == BpAssignmentStrategy.Manual) {
            if (!srcTpInSubdomain(deployChannelInput.getTopologyId(), deployChannelInput.getIngressNode(),
                    channel.getDomainId(), channel.getSubDomainId(),deployChannelInput.getSrcTp())) {
                return new CheckResult(true, SRCTP_NOT_IN_SUBDOMIN);
            }
            if (!rcvTpsInSubdomain(deployChannelInput.getTopologyId(),deployChannelInput.getEgressNode(),
                    channel.getDomainId(),channel.getSubDomainId())) {
                return new CheckResult(true,RCVTP_NOT_IN_SUBDOMIN);
            }
        }
        return new CheckResult(false,"");
    }

    private boolean srcTpInSubdomain(String topologyId, String ingressNode, DomainId domainId,
                                     SubDomainId subDomainId, String tp) {
        return tpInTeSubdomain(topologyId,ingressNode,domainId,subDomainId,tp);
    }

    private boolean rcvTpsInSubdomain(String topologyId, List<EgressNode> egressNodeList, DomainId domainId,
                                     SubDomainId subDomainId) {
        for (EgressNode egressNode : egressNodeList) {
            for (RcvTp rcvTp : egressNode.getRcvTp()) {
                if (!tpInTeSubdomain(topologyId,egressNode.getNodeId(),domainId,subDomainId,rcvTp.getTp())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean egressNodesInSubdomain(String topologyId, List<EgressNode> egressNodeList,
                                           DomainId domainId, SubDomainId subDomainId, BierForwardingType type) {
        for (EgressNode egressNode : egressNodeList) {
            if (!nodeInSubdomain(topologyId,egressNode.getNodeId(),domainId,subDomainId,type)) {
                return false;
            }
        }
        return true;
    }

    private CheckResult checkParam() {
        return checkInputNull(deployChannelInput);
    }

    private CheckResult checkInputNull(DeployChannelInput input) {
        try {
            Preconditions.checkNotNull(input, INPUT_IS_NULL);
            Preconditions.checkNotNull(input.getChannelName(), CHANNEL_NAME_IS_NULL);
            Preconditions.checkNotNull(input.getBierForwardingType(),BIER_FORWARDING_TYPE_IS_NULL);
            Preconditions.checkNotNull(input.getIngressNode(), INGRESS_IS_NULL);
            if (input.getBierForwardingType().equals(BierForwardingType.BierTe)) {
                Preconditions.checkNotNull(input.getSrcTp(), SRC_TP_IS_NULL);
            }
            Preconditions.checkNotNull(input.getEgressNode(), EGRESS_IS_NULL);
            if (input.getEgressNode().isEmpty()) {
                return new CheckResult(true,EGRESS_IS_NULL);
            }
            for (EgressNode egressNode : input.getEgressNode()) {
                Preconditions.checkNotNull(egressNode.getNodeId(),EGRESS_NODE_ID_IS_NULL);
                if (input.getBierForwardingType().equals(BierForwardingType.BierTe)) {
                    Preconditions.checkNotNull(egressNode.getRcvTp(), RCV_TP_IS_NULL);
                }
            }
        } catch (NullPointerException e) {
            LOG.warn("NullPointerException: {}",e);
            return new CheckResult(true,e.getMessage());
        }
        return new CheckResult(false,"");
    }
}
