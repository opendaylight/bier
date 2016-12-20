/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;

import org.opendaylight.channel.util.ChannelDBUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;

public class ChannelInputCheck implements InputCheck {

    private ChannelDBUtil channelDBUtil = ChannelDBUtil.getInstance();

    @Override
    public CheckResult check() {
        return new CheckResult(false,"");
    }

    public CheckResult checkIpRange(String key, IpAddress ipAddress, boolean isMulticastIp) {
        String[] sections = ipAddress.getIpv4Address().getValue().toString().split("\\.");
        Integer value = Integer.parseInt(sections[0]);
        if (isMulticastIp) {
            if (value < 224 || value > 239) {
                return new CheckResult(true, key + " is not multicast ipaddress!");
            }
        } else {
            if (value > 223 || value < 1) {
                return new CheckResult(true, key + " is illegal!");
            }
        }
        return new CheckResult(false,"");
    }

    public CheckResult checkWildCard(Short groupWildcard, Short sourceWildcard) {
        if (groupWildcard > 32 || groupWildcard < 1 || sourceWildcard > 32 || sourceWildcard < 1) {
            return new CheckResult(true,"wildcard is invalid!");
        }
        return new CheckResult(false,"");
    }

    public boolean checkChannelExist(String channelName, String topoId) {
        return channelDBUtil.isChannelExists(channelName, topoId);
    }

    public boolean hasChannelDeployed(String name, String topologyId) {
        return channelDBUtil.hasChannelDeplyed(name,topologyId);
    }
}
