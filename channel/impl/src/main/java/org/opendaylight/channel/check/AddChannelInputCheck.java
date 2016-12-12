/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;


import org.opendaylight.channel.util.ChannelDBUtil;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.AddChannelInput;

public class AddChannelInputCheck implements InputCheck {
    private AddChannelInput addChannelInput;
    private ChannelDBUtil channelDBUtil = ChannelDBUtil.getInstance();

    public AddChannelInputCheck(AddChannelInput input) {
        this.addChannelInput = input;
    }

    @Override
    public CheckResult check() {
        CheckResult result = checkParam();
        if (result.isInputIllegal()) {
            return result;
        }
        if (checkChannelExist()) {
            return new CheckResult(true, "The Channel already exists!");
        }
        return new CheckResult(false, "");
    }

    private boolean checkChannelExist() {
        return channelDBUtil.isChannelExists(addChannelInput.getName(), addChannelInput.getTopologyId());
    }

    private CheckResult checkParam() {
        return null;
    }
}
