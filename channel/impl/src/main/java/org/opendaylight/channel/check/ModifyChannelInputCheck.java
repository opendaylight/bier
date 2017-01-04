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
            return new CheckResult(true, CHANNEL_NOT_EXISTS);
        }
        if (hasChannelDeployed(modifyChannelInput.getName(),modifyChannelInput.getTopologyId())) {
            return new CheckResult(true, CHANNEL_DEPLOYED);
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
            Preconditions.checkNotNull(input, INPUT_IS_NULL);
            Preconditions.checkNotNull(input.getName(), CHANNEL_NAME_IS_NULL);
        } catch (NullPointerException e) {
            LOG.warn("NullPointerException: {}",e);
            return new CheckResult(true,e.getMessage());
        }
        return new CheckResult(false,"");
    }
}
