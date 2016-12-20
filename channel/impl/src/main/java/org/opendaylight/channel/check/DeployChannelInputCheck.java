/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;

import com.google.common.base.Preconditions;

import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelInput;
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
            return new CheckResult(true, "The Channel does not exists!");
        }
        return new CheckResult(false, "");
    }

    private CheckResult checkParam() {
        return checkInputNull(deployChannelInput);
    }

    private CheckResult checkInputNull(DeployChannelInput input) {
        try {
            Preconditions.checkNotNull(input, "Input is null!");
            Preconditions.checkNotNull(input.getChannelName(), "channel-name is null!");
            Preconditions.checkNotNull(input.getIngressNode(), "ingress-node is null!");
            Preconditions.checkNotNull(input.getEgressNode(), "egress-node is null!");
        } catch (NullPointerException e) {
            LOG.warn("NullPointerException: {}",e);
            return new CheckResult(true,e.getMessage());
        }
        return new CheckResult(false,"");
    }
}
