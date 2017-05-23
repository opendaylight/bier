/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.pathcore;

import java.util.Map;
import java.util.TreeMap;

public class MetricStrategy<V, E> implements ICalcStrategy<V, E> {
    @Override
    public long getEdgeMeasure(ITransformer<E> edgeValues, E incomingEdge) {
        if (edgeValues == null) {
            return 1;
        }
        Number metric = edgeValues.transform(incomingEdge);
        if (metric == null) {
            return 1;
        }
        return metric.longValue();
    }

    @Override
    public boolean isCurNodeMoreOptimal(long curNodeMeasure, long incomingEdgeMeasure, long neighborNodeMeasure) {
        return (curNodeMeasure + incomingEdgeMeasure) < neighborNodeMeasure;
    }

    @Override
    public Map.Entry<V, Number> getOptimalNodeInTentMap(TreeMap<V, Number> tentMap) {
        return tentMap.firstEntry();
    }

    @Override
    public long transEdgeMeasure(long curNodeMeasure, long incomingEdgeMeasure) {
        return curNodeMeasure + incomingEdgeMeasure;
    }
}
