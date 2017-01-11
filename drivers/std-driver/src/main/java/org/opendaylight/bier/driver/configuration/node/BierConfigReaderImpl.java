/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.node;

import org.opendaylight.bier.adapter.api.BierConfigReader;

import org.opendaylight.bier.driver.NetconfDataOperator;


import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierConfigReaderImpl implements BierConfigReader {
    private static final Logger LOG = LoggerFactory.getLogger(BierConfigReaderImpl.class);
    private NetconfDataOperator netconfDataOperator ;

    public BierConfigReaderImpl(NetconfDataOperator netconfDataOperator) {
        this.netconfDataOperator = netconfDataOperator;
    }

    public BierGlobal readBierGlobal(String nodeId) {
        return netconfDataOperator.read(nodeId,NetconfDataOperator.BIER_GLOBAL_IID);
    }
}
