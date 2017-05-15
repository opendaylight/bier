/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;

public interface BierDataOperation {

    void writeOperation(ReadWriteTransaction transaction);


    <T> ListenableFuture<T> readOperation(ReadWriteTransaction transaction);
}