/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.common.util;


public class LabelUtil {

    private static final Long MIN_MPLS_LABEL = 16L;
    private static final Long MAX_MPLS_LABEL = 1048575L;

    public static boolean checkLabelRangeValid(final Long label) {
        if (label >= MIN_MPLS_LABEL && label <= MAX_MPLS_LABEL) {
            return true;
        }
        return false;
    }
}
