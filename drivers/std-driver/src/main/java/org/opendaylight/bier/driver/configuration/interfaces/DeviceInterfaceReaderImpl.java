/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.driver.configuration.interfaces;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import org.opendaylight.bier.adapter.api.DeviceInterfaceReader;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.Interfaces;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DeviceInterfaceReaderImpl implements DeviceInterfaceReader {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceInterfaceReaderImpl.class);
    private NetconfDataOperator netconfDataOperator;
    public static final InstanceIdentifier<Interfaces> INTERFACES_IID =
            InstanceIdentifier.create(Interfaces.class);

    public DeviceInterfaceReaderImpl(NetconfDataOperator netconfDataOperator) {
        this.netconfDataOperator = netconfDataOperator;
    }




    public List<BierTerminationPoint> readDeviceInterface(String nodeId) {

        Interfaces interfaces = netconfDataOperator.read(nodeId,INTERFACES_IID);
        if ((interfaces == null) || (interfaces.getInterface() == null)) {
            LOG.info("Interface info is null ,node : {} !!",nodeId);
            return null;
        }
        return Lists.transform(interfaces.getInterface(), new Function<Interface, BierTerminationPoint>() {
            @Nullable
            @Override
            public BierTerminationPoint apply(Interface input) {
                return new BierTerminationPointBuilder()
                        .setIfName(input.getName())
                        .build();
            }
        });

    }


}
