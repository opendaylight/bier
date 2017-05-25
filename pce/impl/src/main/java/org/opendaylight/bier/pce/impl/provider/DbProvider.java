/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.provider;

import com.google.common.base.Optional;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DbProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DbProvider.class);
    private static DbProvider instance = null;
    private DataBroker dataBroker;

    private DbProvider() {
    }

    public static DbProvider getInstance() {

        if (instance == null) {
            instance = new DbProvider();
        }
        return instance;
    }

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }


    public DataBroker getDataBroker() {
        return dataBroker;
    }

    public void deleteData(final LogicalDatastoreType type, InstanceIdentifier<?> path) {
        DataObject data = readData(type,path);
        if (data == null) {
            return;
        }
        final WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.delete(type, path);
        try {
            tx.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("", e);
        }
    }

    public <T extends DataObject> void mergeData(final LogicalDatastoreType type, InstanceIdentifier<T> path,
                                                            T data) {
        final WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.merge(type, path, data, true);
        try {
            tx.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("", e);
        }
    }

    public <T extends DataObject> T readData(final LogicalDatastoreType type, InstanceIdentifier<T> path) {
        if (dataBroker == null) {
            LOG.error("readOperationalData error, dataBroker null!");
            return null;
        }
        ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            Optional<T> optional = tx.read(type, path).checkedGet();
            if (optional.isPresent()) {
                return optional.get();
            } else {
                return null;
            }
        } catch (ReadFailedException | IllegalStateException e) {
            LOG.warn("PCE DB warring", e);
            return null;
        }
    }
}
