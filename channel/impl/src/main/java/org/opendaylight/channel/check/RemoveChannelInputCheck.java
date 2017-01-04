/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;

import com.google.common.base.Preconditions;

import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RemoveChannelInputCheck extends ChannelInputCheck {
    private RemoveChannelInput removeChannelInput;
    private static final Logger LOG = LoggerFactory.getLogger(RemoveChannelInputCheck.class);

    public RemoveChannelInputCheck(RemoveChannelInput input) {
        this.removeChannelInput = input;
    }

    @Override
    public CheckResult check() {
        return checkParam();
    }

    private CheckResult checkParam() {
        return checkInputNull(removeChannelInput);
    }

    private CheckResult checkInputNull(RemoveChannelInput input) {
        try {
            Preconditions.checkNotNull(input, INPUT_IS_NULL);
            Preconditions.checkNotNull(input.getChannelName(), CHANNEL_NAME_IS_NULL);
        } catch (NullPointerException e) {
            LOG.warn("NullPointerException: {}",e);
            return new CheckResult(true,e.getMessage());
        }
        return new CheckResult(false,"");
    }
}
