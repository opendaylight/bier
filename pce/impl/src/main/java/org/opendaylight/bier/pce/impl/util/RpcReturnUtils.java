/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.util;

import java.util.concurrent.Future;

import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import com.google.common.util.concurrent.Futures;



public class RpcReturnUtils {
    private RpcReturnUtils() {

    }

    public static <T> Future<RpcResult<T>> returnErr(String errMsg) {
        return Futures.immediateFuture(RpcResultBuilder
                .<T>failed()
                .withError(ErrorType.APPLICATION, errMsg).build());
    }

    public static <T> Future<RpcResult<T>> returnOk() {
        return Futures.immediateFuture(RpcResultBuilder
                .<T>success().build());
    }
}
