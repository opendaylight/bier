/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.pathcore;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.bier.pce.impl.util.ComUtility;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;


public class MetricTransformer implements ITransformer<BierLink> {
    protected static final long LINK_METRIC_STEP = 0x1;
    List<BierLink> contrainedLinkList = new ArrayList<>();

    public MetricTransformer(List<BierLink> contrainedLinks) {
        if (contrainedLinks != null && !contrainedLinks.isEmpty()) {
            this.contrainedLinkList.addAll(contrainedLinks);
        }
    }



    @Override
    public Double transform(BierLink link) {
        double metric = ComUtility.getLinkMetric(link);

        for (BierLink containedLink : contrainedLinkList) {
            if (containedLink.equals(link)) {
                metric -= metric / 2;
            }
        }
        return metric < 1 ? 1 : metric;
    }
}
