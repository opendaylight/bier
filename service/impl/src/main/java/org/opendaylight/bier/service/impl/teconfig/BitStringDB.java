/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.teconfig;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class BitStringDB {

    private static final Logger LOG = LoggerFactory.getLogger(BitStringDB.class);
    private final DataBroker dataBroker;

    public BitStringDB(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public List<String> computBitStringList(Channel channel,int path_compute_type) {
        List<String> pathIdList = new ArrayList<>();
        return pathIdList;
    }

    public List<String> updateBitStringList(String channelName,List<String> pathIdList) {
        return pathIdList;
    }

    public List<String> getPathIdList(String channelName) {
        List<String> pathIdList = new ArrayList<>();
        return pathIdList;
    }

}
