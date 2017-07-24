/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;

import com.google.common.base.Preconditions;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.AddChannelInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddChannelInputCheck extends ChannelInputCheck {
    private AddChannelInput addChannelInput;
    private static final Logger LOG = LoggerFactory.getLogger(AddChannelInputCheck.class);

    public AddChannelInputCheck(AddChannelInput input) {
        this.addChannelInput = input;
    }

    @Override
    public CheckResult check() {
        CheckResult result = checkParam();
        if (result.isInputIllegal()) {
            return result;
        }
        if (checkChannelExist(addChannelInput.getName(),addChannelInput.getTopologyId())) {
            return new CheckResult(true, CHANNEL_EXISTS);
        }
        return new CheckResult(false, "");
    }

    private CheckResult checkParam() {
        CheckResult result = checkInputNull(addChannelInput);
        if (result.isInputIllegal()) {
            return result;
        }
        return paramValidity(addChannelInput);
    }

    private CheckResult paramValidity(AddChannelInput input) {
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

    private CheckResult checkInputNull(AddChannelInput input) {
        try {
            Preconditions.checkNotNull(input, INPUT_IS_NULL);
            Preconditions.checkNotNull(input.getName(), CHANNEL_NAME_IS_NULL);
            Preconditions.checkNotNull(input.getSrcIp(), SRCIP_IS_NULL);
            Preconditions.checkNotNull(input.getDstGroup(), DEST_GROUP_IS_NULL);
            Preconditions.checkNotNull(input.getDomainId(), DOMAIN_ID_IS_NULL);
            Preconditions.checkNotNull(input.getSubDomainId(), SUB_DOMAIN_ID_IS_NULL);
            Preconditions.checkNotNull(input.getSourceWildcard(), SRC_WILDCARD_IS_NULL);
            Preconditions.checkNotNull(input.getGroupWildcard(), GROUP_WILDCARD_IS_NULL);
        } catch (NullPointerException e) {
            LOG.warn("NullPointerException: {}",e);
            return new CheckResult(true,e.getMessage());
        }
        return new CheckResult(false,"");
    }
}
