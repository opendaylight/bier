/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.provider;



public class PceResult {
    private boolean calcFail = false;

    public static final PceResult nullPceResult = new PceResult();


    public void clearCalcPathResult() {
        calcFail = false;
    }

    public void setCalcFail(boolean calcFail) {
        this.calcFail = calcFail;
    }

    public boolean isCalcFail() {
        return this.calcFail;
    }

}
