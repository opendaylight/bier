/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;

import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.RemoveChannelInput;

public class RemoveChannelInputCheck implements InputCheck {
    private RemoveChannelInput removeChannelInput;

    public RemoveChannelInputCheck(RemoveChannelInput input) {
        this.removeChannelInput = input;
    }

    @Override
    public CheckResult check() {
        CheckResult result = checkParam();
        if (result.isInputIllegal()) {
            return result;
        }
        return new CheckResult(false, "");
    }

    private CheckResult checkParam() {
        return null;
    }
}
