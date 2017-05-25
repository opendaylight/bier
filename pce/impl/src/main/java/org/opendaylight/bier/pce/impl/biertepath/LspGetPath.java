/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.biertepath;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;

public class LspGetPath {

    private LspGetPath(){

    }

    public static LinkedList<BierLink> getPath(
            Map<String, List<BierLink>> incomingLinkMap, String srcNode,
            String destNode) {

        LinkedList<BierLink> path = new LinkedList<>();

        if (incomingLinkMap == null
                || incomingLinkMap.isEmpty()
                || incomingLinkMap.get(destNode) == null) {
            return path;
        }

        String current = destNode;

        while (!current.equals(srcNode)) {
            BierLink incoming;

            incoming = incomingLinkMap.get(current).get(0);

            addIncomingEdge2Path(path, incoming);

            current = incoming.getLinkSource().getSourceNode();
        }

        return path;
    }

    private static void addIncomingEdge2Path(LinkedList<BierLink> path,
                                             BierLink incoming) {
        path.addFirst(incoming);
    }

}
