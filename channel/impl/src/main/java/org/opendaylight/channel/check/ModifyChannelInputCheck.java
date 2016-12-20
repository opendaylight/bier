/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;

import com.google.common.base.Preconditions;

import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModifyChannelInputCheck extends ChannelInputCheck {
    private ModifyChannelInput modifyChannelInput;
    private static final Logger LOG = LoggerFactory.getLogger(ModifyChannelInputCheck.class);

    public ModifyChannelInputCheck(ModifyChannelInput input) {
        this.modifyChannelInput = input;
    }

    @Override
    public CheckResult check() {

        CheckResult result = checkParam();
        if (result.isInputIllegal()) {
            return result;
        }
        if (!checkChannelExist(modifyChannelInput.getName(),modifyChannelInput.getTopologyId())) {
            return new CheckResult(true, "The Channel does not exists!");
        }
        if (hasChannelDeployed(modifyChannelInput.getName(),modifyChannelInput.getTopologyId())) {
            return new CheckResult(true, "The Channel has deployed,can not modify");
        }

        return new CheckResult(false, "");
    }



    private CheckResult checkParam() {
        CheckResult result = checkInputNull(modifyChannelInput);
        if (result.isInputIllegal()) {
            return result;
        }
        return paramValidity(modifyChannelInput);
    }

    private CheckResult paramValidity(ModifyChannelInput input) {
        CheckResult result = checkIpRange("src-ip",input.getSrcIp(),false);
        if (result.isInputIllegal()) {
            return result;
        }
        result = checkIpRange("dest-group",input.getDstGroup(),true);
        if (result.isInputIllegal()) {
            return result;
        }
        return checkWildCard(input.getGroupWildcard(),input.getSourceWildcard());
    }

    private CheckResult checkInputNull(ModifyChannelInput input) {
        try {
            Preconditions.checkNotNull(input, "Input is null!");
            Preconditions.checkNotNull(input.getName(), "channel-name is null!");
            Preconditions.checkNotNull(input.getSrcIp(), "src-ip is null!");
            Preconditions.checkNotNull(input.getDstGroup(), "dest-group is null!");
            Preconditions.checkNotNull(input.getDomainId(), "domain-id is null!");
            Preconditions.checkNotNull(input.getSubDomainId(), "sub-domain-id is null!");
            Preconditions.checkNotNull(input.getSourceWildcard(), "src-wildcard is null!");
            Preconditions.checkNotNull(input.getGroupWildcard(), "group-wildcard is null!");
        } catch (NullPointerException e) {
            LOG.warn("NullPointerException: {}",e);
            return new CheckResult(true,e.getMessage());
        }
        return new CheckResult(false,"");
    }
}
