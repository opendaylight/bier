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


public interface ICalcStrategy<V, E> {
    long getEdgeMeasure(ITransformer<E> edgeValues, E incomingEdge);

    boolean isCurNodeMoreOptimal(long curNodeMeasure, long incomingEdgeMeasure, long neighborNodeMeasure);

    Map.Entry<V, Number> getOptimalNodeInTentMap(TreeMap<V, Number> tentMap);

    long transEdgeMeasure(long curNodeMeasure, long incomingEdgeMeasure);
}


