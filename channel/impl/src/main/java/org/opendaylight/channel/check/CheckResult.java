/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;


public class CheckResult {
    private boolean isIllegal;
    private String errorCause;

    public CheckResult(boolean isIllegal, String errorCause) {
        this.isIllegal = isIllegal;
        this.errorCause = errorCause;
    }

    public boolean isInputIllegal() {
        return isIllegal;
    }

    public String getErrorCause() {
        return errorCause;
    }
}
