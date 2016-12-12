/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;

import org.opendaylight.channel.util.ChannelDBUtil;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.DeployChannelInput;

public class DeployChannelInputCheck implements InputCheck {
    private DeployChannelInput deployChannelInput;
    private ChannelDBUtil channelDBUtil = ChannelDBUtil.getInstance();

    public DeployChannelInputCheck(DeployChannelInput input) {
        this.deployChannelInput = input;
    }

    @Override
    public CheckResult check() {
        CheckResult result = checkParam();
        if (result.isInputIllegal()) {
            return result;
        }
        if (!checkChannelExist()) {
            return new CheckResult(true, "The Channel does not exists!");
        }
        return new CheckResult(false, "");
    }

    private boolean checkChannelExist() {
        return channelDBUtil.isChannelExists(deployChannelInput.getChannelName(), deployChannelInput.getTopologyId());

    }

    private CheckResult checkParam() {
        return null;
    }
}
