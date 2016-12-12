/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.util;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;

import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;


public class RpcReturnUtil {
    private RpcReturnUtil() {

    }

    public static <T> Future<RpcResult<T>> returnErr(String errMsg) {
        return Futures.immediateFuture(RpcResultBuilder.<T>failed().withError(RpcError.ErrorType.APPLICATION, errMsg)
                .build());
    }

    public static <T> Future<RpcResult<T>> returnRpcResult(T out) {
        return Futures.immediateFuture(RpcResultBuilder.<T>success(out).build());
    }

    public static <T> RpcResult<T> returnResultOk() {
        return RpcResultBuilder.<T>success().build();
    }

    public static <T> RpcResult<T> returnResultErr(String errMsg) {
        return RpcResultBuilder.<T>failed().withError(RpcError.ErrorType.APPLICATION, errMsg)
                .build();
    }
}
