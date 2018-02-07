/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.adapter.api;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.ContinuityCheckInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.ContinuityCheckOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.PathDiscoveryInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.PathDiscoveryOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public interface BierOamStartEchoRequestCheck {
    Future<RpcResult<ContinuityCheckOutput>> startBierContinuityCheck(
            String nodeId,ContinuityCheckInput continuityCheckInput);

    Future<RpcResult<PathDiscoveryOutput>> startBierPathDiscovery(
            String nodeId,PathDiscoveryInput pathDiscoveryInput);
}
