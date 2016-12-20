/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.bier.adapter.api.BierConfigResult;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.OptimisticLockFailedException;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev161020.Routing;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfDataOperator implements BindingAwareConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(NetconfDataOperator.class);
    private static MountPointService mountService = null;

    public static final InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier
                    .create(NetworkTopology.class)
                    .child(Topology.class,
                            new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));

    /*public static final InstanceIdentifier<BierGlobal> BIER_GLOBAL_IID =
            InstanceIdentifier.create(BierConfiguration.class)
                    .child(Bier.class).child(BierGlobal.class);
    public static final InstanceIdentifier<SubDomain> SUBDOMAIN_IID = BIER_GLOBAL_IID.child(SubDomain.class);
    */

    public enum OperateType {
        CREATE,
        REPLACE,
        MERGE,
        DELETE;

        OperateType() {
        }
    }

    public static final InstanceIdentifier<Routing> ROUTING_IID = InstanceIdentifier.create(Routing.class);


    public static final int RETRY_WRITE_MAX = 3;

    @Override
    public void onSessionInitialized(BindingAwareBroker.ConsumerContext session) {

        LOG.info("session initialized ");
        mountService = session.getSALService(MountPointService.class);

    }

    public static MountPoint getMountPoint(String nodeID) {
        if (mountService == null) {
            LOG.error("Mount service is null");
            return null;
        }
        Optional<MountPoint> nodeMountPoint = mountService.getMountPoint(NETCONF_TOPO_IID
                .child(Node.class, new NodeKey(new NodeId(nodeID))));


        if (!nodeMountPoint.isPresent()) {
            LOG.error("Mount point for node {} doesn't exist", nodeID);
            return null;
        }


        return nodeMountPoint.get();

    }

    public static <T extends DataObject> T read(DataBroker dataBroker,
                                                                  InstanceIdentifier<T> path) {
        T result = null;
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        Optional<T> optionalData;
        try {
            optionalData = transaction.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (optionalData.isPresent()) {
                result = optionalData.get();
            } else {
                LOG.debug("{}: Failed to read {}", Thread.currentThread().getStackTrace()[1], path);
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", path, e);
        }
        transaction.close();
        return result;

    }



    public static <T extends DataObject> BierConfigResult operate(OperateType type,
                                                       DataBroker dataBroker,
                                                       final int tries,
                                                       InstanceIdentifier<T> path, T data) {

        BierConfigResult ncResult = new BierConfigResult(BierConfigResult.ConfigurationResult.SUCCESSFUL);

        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        switch (type) {
            case CREATE:
            case REPLACE:
                writeTransaction.put(LogicalDatastoreType.CONFIGURATION,path,data,true);
                break;
            case MERGE:
                writeTransaction.merge(LogicalDatastoreType.CONFIGURATION,path,data,true);
                break;
            case DELETE:
                writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, path);
                break;
            default:
                break;

        }

        final CheckedFuture<Void, TransactionCommitFailedException> submitResult = writeTransaction.submit();
        Futures.addCallback(submitResult, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
            }

            @Override
            public void onFailure(final Throwable throwable) {
                if (throwable instanceof OptimisticLockFailedException) {
                    if ((tries - 1) > 0) {
                        LOG.info("Got OptimisticLockFailedException - trying again");
                        operate(type, dataBroker, tries - 1, path ,data);
                    } else {
                        ncResult.setFailureReason(BierConfigResult.NETCONF_LOCK_FAILUE);
                    }

                } else {
                    ncResult.setFailureReason(BierConfigResult.NETCONF_EDIT_FAILUE + throwable.getMessage());
                }

            }
        });

        return ncResult;

    }




    public static <T extends DataObject>  BierConfigResult write(OperateType type,
                                                                 String nodeId,
                                                                 InstanceIdentifier<T> path, T data) {

        BierConfigResult ncResult = new BierConfigResult(BierConfigResult.ConfigurationResult.SUCCESSFUL);

        if (mountService == null) {
            LOG.error(BierConfigResult.MOUNT_SERVICE_NULL);
            ncResult.setFailureReason(BierConfigResult.MOUNT_SERVICE_NULL);
            return ncResult;
        }
        Optional<MountPoint> nodeMountPoint = mountService.getMountPoint(NETCONF_TOPO_IID
                .child(Node.class, new NodeKey(new NodeId(nodeId))));


        if (!nodeMountPoint.isPresent()) {
            LOG.error(BierConfigResult.MOUNT_POINT_FAILUE);
            ncResult.setFailureReason(BierConfigResult.MOUNT_POINT_FAILUE);
            return ncResult;
        }



        final DataBroker nodeBroker = nodeMountPoint.get().getService(DataBroker.class).get();
        operate(type, nodeBroker, RETRY_WRITE_MAX, path ,data);

        return ncResult;
    }


}
