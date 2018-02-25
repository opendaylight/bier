/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.service.impl.allocatebp;

public class ChannelNameBferNodeId {
    private String channelName;
    private String bferNodeId;

    public ChannelNameBferNodeId(String channelName, String bferNodeId) {
        this.channelName = channelName;
        this.bferNodeId = bferNodeId;
    }

    public String getChannelName() {
        return channelName;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        ChannelNameBferNodeId other = (ChannelNameBferNodeId)obj;
        return this.channelName.equals(other.channelName) && this.bferNodeId.equals(other.bferNodeId);
    }
}