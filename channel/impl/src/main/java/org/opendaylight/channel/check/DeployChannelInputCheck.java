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

import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.deploy.channel.input.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployChannelInputCheck extends ChannelInputCheck {
    private DeployChannelInput deployChannelInput;
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
        if (!checkChannelExist(deployChannelInput.getChannelName(),deployChannelInput.getTopologyId())) {
            return new CheckResult(true, CHANNEL_NOT_EXISTS);
        }
        result = checkNodesInSubdomain();
        if (result.isInputIllegal()) {
            return result;
        }
        return new CheckResult(false, "");
    }

    private CheckResult checkNodesInSubdomain() {
        Channel channel = getChannel(deployChannelInput.getTopologyId(),deployChannelInput.getChannelName());
        if (!nodeInSubdomain(deployChannelInput.getTopologyId(),deployChannelInput.getIngressNode(),
                channel.getDomainId(),channel.getSubDomainId())) {
            return new CheckResult(true, INGRESS_NOT_IN_SUBDOMIN);
        }
        if (!egressNodesInSubdomain(deployChannelInput.getTopologyId(),deployChannelInput.getEgressNode(),
                channel.getDomainId(),channel.getSubDomainId())) {
            return new CheckResult(true,EGRESS_NOT_IN_SUBDOMIN);
        }
        return new CheckResult(false,"");
    }

    private boolean egressNodesInSubdomain(String topologyId, List<EgressNode> egressNodeList,
                                           DomainId domainId, SubDomainId subDomainId) {
        for (EgressNode egressNode : egressNodeList) {
            if (!nodeInSubdomain(topologyId,egressNode.getNodeId(),domainId,subDomainId)) {
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
            Preconditions.checkNotNull(input.getIngressNode(), INGRESS_IS_NULL);
            Preconditions.checkNotNull(input.getEgressNode(), EGRESS_IS_NULL);
        } catch (NullPointerException e) {
            LOG.warn("NullPointerException: {}",e);
            return new CheckResult(true,e.getMessage());
        }
        return new CheckResult(false,"");
    }
}
