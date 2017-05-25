/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.teconfig;

import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.BierTeChannelWriter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPathUpdate;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceListener;


public class BierPathUpdateProcess implements BierPceListener {

    private BierTeChannelProcess bierTeChannelProcess;

    public BierPathUpdateProcess(DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry,
                                 BierTeChannelWriter teChannelWriter, BierTeBiftWriter bierTeBiftWriter,
                                 BierTeBitstringWriter bierTeBitstringWriter) {
        this.bierTeChannelProcess = new BierTeChannelProcess(dataBroker, rpcConsumerRegistry,
                teChannelWriter, bierTeBiftWriter,bierTeBitstringWriter);
    }


    @Override
    public void onBierPathUpdate(BierPathUpdate updatePath) {
        bierTeChannelProcess.processUpdateTeChannel(updatePath);
    }
}
