/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.allocatebp;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;


public interface BPAllocateStrategy {
    boolean allocateBPs(Channel channel, List<Bfer> bferList);

    boolean recycleBPs(Channel channel, List<Bfer> bferList);

    boolean setBferListToChannel(String channelName, List<Bfer> bferList);

    List<Bfer> getBferListOfChannel(String channelName);

    boolean removeBferListToChannel(String channelName);

    List<SubdomainBslSi> getSubdomainBslSiAllocatedToChannel(Channel channel);

    List<SubdomainBslSi> getAllAllocatedSubdomainBslSi();

}
