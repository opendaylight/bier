/*
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
//import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTopologyProcess<T> implements Callable<ListenableFuture<T>>,  TransactionChainListener {
    private static final Logger LOG =  LoggerFactory.getLogger(BierTopologyProcess.class);
    protected final DataBroker mcDataBroker;

    protected static final int MAX_TRANSACTION_OPERATIONS = 100;
    protected static final int OPERATION_QUEUE_DEPTH = 500;
    public static final int  FLAG_READ             =  0;
    public static final int  FLAG_WRITE            =  1;

    protected final BlockingQueue<BierTopologyOperation> mqueTopoOperation =
           new LinkedBlockingQueue<>(OPERATION_QUEUE_DEPTH);
    //protected BindingTransactionChain m_cTxChain = null;
    protected final int        miProcessFlag;
    public final T             mtT;


    public BierTopologyProcess(final DataBroker dataBroker, final int processFlag, final T tt) {
        this.mcDataBroker = Preconditions.checkNotNull(dataBroker);
        //m_cTxChain = this.m_cDataBroker.createTransactionChain(this);
        miProcessFlag = processFlag;
        mtT = tt;
    }


    @Override
    public ListenableFuture<T> call() throws Exception {
        T object = mtT;
        //try
        {
            BierTopologyOperation op = mqueTopoOperation.take();
            final ReadWriteTransaction tx = this.mcDataBroker.newReadWriteTransaction();
            int ops = 0;

            do {
                if (0 == miProcessFlag) {
                    ListenableFuture<Optional<T>> readResult = op.readOperation(tx);
                    //try
                    {
                        object = readResult.get().get();
                    }
                    /*catch (Exception e) {
                        LOG.error("Read Data failed", e);
                    }*/
                } else {
                    op.writeOperation(tx);

                    //try
                    {
                        tx.submit().checkedGet();
                    }
                    /*catch (final TransactionCommitFailedException e) {
                        tObject = null;
                        LOG.warn("Stat DataStoreOperation unexpected State!", e);
                        //m_cTxChain.close();
                        //m_cTxChain = m_cDataBroker.createTransactionChain(this);
                    }*/
                }

                ops++;
                if (ops < MAX_TRANSACTION_OPERATIONS) {
                    op = mqueTopoOperation.poll();
                } else {
                    op = null;
                }

            } while (op != null);

        }
        /*catch (final Exception e) {
            LOG.warn("Stat DataStore Operation executor fail!", e);
        }*/

        cleanDataStoreOperQueue();

        return Futures.immediateFuture(object);
    }


    public void enqueueOperation(final BierTopologyOperation task) {
        try {
            mqueTopoOperation.put(task);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while submitting task {}", task, e);
        }
    }


    protected void cleanDataStoreOperQueue() {
        while (!mqueTopoOperation.isEmpty()) {
            mqueTopoOperation.poll();
        }
    }


    @Override
    public void onTransactionChainFailed(TransactionChain<?, ?> chain, AsyncTransaction<?, ?> transaction,
            Throwable cause) {
        //m_cTxChain.close();

        //m_cTxChain = m_cDataBroker.createTransactionChain(this);

        cleanDataStoreOperQueue();
    }

    @Override
    public void onTransactionChainSuccessful(TransactionChain<?, ?> chain) {
    }
}
