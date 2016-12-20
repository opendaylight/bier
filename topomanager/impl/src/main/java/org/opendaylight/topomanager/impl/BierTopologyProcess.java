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
    private static final Logger LOG =  LoggerFactory.getLogger(BierTopologyProcess.class);    // 日志记录
    protected final DataBroker mcDataBroker;

    protected static final int MAX_TRANSACTION_OPERATIONS = 100;
    protected static final int OPERATION_QUEUE_DEPTH = 500;
    public static final int  FLAG_READ             =  0;           // 数据操作读标志
    public static final int  FLAG_WRITE            =  1;           // 数据操作写标志

    protected final BlockingQueue<BierTopologyOperation> mqueTopoOperation =
           new LinkedBlockingQueue<>(OPERATION_QUEUE_DEPTH);
    //protected BindingTransactionChain m_cTxChain = null;
    protected final int        miProcessFlag;
    public final T             mtT;


    public BierTopologyProcess(final DataBroker dataBroker, final int processFlag, final T tt) {
        this.mcDataBroker = Preconditions.checkNotNull(dataBroker);                  // 检查数据是否为空
        //m_cTxChain = this.m_cDataBroker.createTransactionChain(this);           // 创建事务
        miProcessFlag = processFlag;
        mtT = tt;
    }


    @Override
    public ListenableFuture<T> call() throws Exception {
        T object = mtT;
        //try
        {
            BierTopologyOperation op = mqueTopoOperation.take();                     // 获取操作队列中的对象，进行操作
            final ReadWriteTransaction tx = this.mcDataBroker.newReadWriteTransaction(); // 获取事务
            int ops = 0;                                                          // 记录操作执行条数

            do {
                // 读取结果，这里采用接口中的ReadOpeartion函数，所以构造操作对象时需重载该函数，实现对应的功能
                if ( 0 == miProcessFlag ) {
                    ListenableFuture<Optional<T>> readResult = op.readOperation(tx);
                    //try
                    {
                        object = readResult.get().get();          // 获取信息
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
                    op = mqueTopoOperation.poll();                                  // 取出队列首元素
                } else {
                    op = null;                                          // 操作次数过多，不执行
                }

            } while (op != null);

        }
        /*catch (final Exception e) {
            LOG.warn("Stat DataStore Operation executor fail!", e);     // 只记录失败日志，分支暂不处理
        }*/

        // 清除所有数据
        cleanDataStoreOperQueue();

        // 返回执行结果
        return Futures.immediateFuture(object);
    }


    public void enqueueOperation(final BierTopologyOperation task) {
        try {
            mqueTopoOperation.put(task);                                             // 存入队列
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while submitting task {}", task, e);               // 失败记录日志
        }
    }


    protected void cleanDataStoreOperQueue() {

        // 清除事务表
        while (!mqueTopoOperation.isEmpty()) {
            mqueTopoOperation.poll();
        }
    }


    @Override
    public void onTransactionChainFailed(TransactionChain<?, ?> chain, AsyncTransaction<?, ?> transaction,
            Throwable cause) {
        // 关闭事务
        //m_cTxChain.close();

        // 创建一个新的事务
        //m_cTxChain = m_cDataBroker.createTransactionChain(this);

        // 清除事务
        cleanDataStoreOperQueue();
    }

    @Override
    public void onTransactionChainSuccessful(TransactionChain<?, ?> chain) {
    }
}
