/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.pathcore;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface ISpt<V, E> {
    Map<V, List<E>> getIncomingEdgeMap();

    Map<V, Number> getDistanceMap();

    LinkedList<V> getDistanceOrderList();
}
