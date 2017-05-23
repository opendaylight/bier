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

public interface ISourceData<V, E> {

    V moveOptimalTentNode2PathList();

    void add2TentList(V localNode, V neighborNode, E outEdge);

    boolean isNodeAlreadyInPathList(V node);

    long getDistance(V node);

    Map<V, Number> getDistance();

    Map<V, List<E>> getIncomingEdgeMap();

    LinkedList<V> getDistanceOrderList();

    void setDestNodeDescendFlag(List<E> incomingEdges);

}
