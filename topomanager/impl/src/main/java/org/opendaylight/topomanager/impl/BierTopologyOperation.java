/*
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;

import com.google.common.util.concurrent.ListenableFuture;


public interface BierTopologyOperation {

    
    void writeOperation(ReadWriteTransaction transaction);

    
    <T> ListenableFuture<T> ReadOperation(ReadWriteTransaction transaction);
}