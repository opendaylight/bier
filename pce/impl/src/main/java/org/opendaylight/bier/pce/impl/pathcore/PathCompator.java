/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.pathcore;

import java.util.LinkedList;

import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDest;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSource;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;


public class PathCompator {

    private PathCompator() {

    }

    public static boolean isPathEqual(LinkedList<BierLink> path1,
                                      LinkedList<BierLink> path2) {
        if ((path1 == null && path2 == null)
                || (path1 == path2)) {
            return true;
        }

        if (path1 == null || path2 == null) {
            return false;
        }

        if (path1.size() != path2.size()) {
            return false;
        }

        return isValidPathEqual(path1, path2);
    }

    private static boolean isValidPathEqual(LinkedList<BierLink> path1, LinkedList<BierLink> path2) {
        for (int i = 0; i < path1.size(); ++i) {
            if (!isLinkEqual(path1.get(i), path2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLinkEqual(BierLink link1, BierLink link2) {
        if (sourceAreEqual(link1.getLinkSource(), link2.getLinkSource())
                && destinationAreEqual(link1.getLinkDest(), link2.getLinkDest())) {
            return true;
        } else if (sourceEqual2Destination(link1.getLinkSource(), link2.getLinkDest())
                && sourceEqual2Destination(link2.getLinkSource(), link1.getLinkDest())) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean sourceEqual2Destination(LinkSource source,
                                                   LinkDest destination) {
        return source.getSourceNode().equals(destination.getDestNode())
                && source.getSourceTp().equals(destination.getDestTp());
    }

    private static boolean destinationAreEqual(LinkDest destination1,
                                               LinkDest destination2) {
        return destination1.getDestNode().equals(destination2.getDestNode())
                && destination1.getDestTp().equals(destination2.getDestTp());
    }

    private static boolean sourceAreEqual(LinkSource source1, LinkSource source2) {
        return source1.getSourceNode().equals(source2.getSourceNode())
                && source1.getSourceTp().equals(source2.getSourceTp());
    }
}
