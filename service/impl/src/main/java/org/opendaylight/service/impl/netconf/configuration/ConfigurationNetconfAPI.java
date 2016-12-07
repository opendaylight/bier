/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl.netconf.configuration;


import com.google.common.base.Function;
import com.google.common.base.Optional;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;

import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;

import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ConfigurationNetconfAPI implements BindingAwareConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationNetconfAPI.class);
    private static MountPointService mountService = null;
    private static final RpcResult<Void> RPC_SUCCESS = RpcResultBuilder.<Void>success().build();
    private static final RpcResult<Void> RPC_FAILED = RpcResultBuilder.<Void>failed().build();

    public  enum ConfigurationType {
        CREATE,
        UPDATE,
        DELETE;
        ConfigurationType(){
        }
    }

    public static final InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier
                    .create(NetworkTopology.class)
                    .child(Topology.class,
                            new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));


    public ConfigurationNetconfAPI() {

        LOG.info("create instance of ConfigurationNetconfAPI");

    }

    @Override
    public void onSessionInitialized(ConsumerContext session) {

        LOG.info("session initialized ");
        mountService = session.getSALService(MountPointService.class);

    }


    public static MountPoint getMountPoin(String nodeID) {
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

    public static Future<RpcResult<Void>> writeBierNode(ConfigurationType type,BierNode bierNode) {
        LOG.info("configurations write to node : bier configuration");
        MountPoint node = getMountPoin(bierNode.getNodeId());
        if (null == getMountPoin(bierNode.getNodeId())) {

            return Futures.immediateFuture(RPC_FAILED);
        }

        final DataBroker nodeBroker = node.getService(DataBroker.class).get();
        final WriteTransaction writeTransaction = nodeBroker.newWriteOnlyTransaction();
        switch (type) {
            case CREATE:
                break;
            case UPDATE:
                break;
            case DELETE:
                break;
            default:
                break;

        }
        final CheckedFuture<Void, TransactionCommitFailedException> submit = writeTransaction.submit();

        return Futures.transform(submit, new Function<Void, RpcResult<Void>>() {
            @Override
            public RpcResult<Void> apply(final Void result) {
                LOG.info(" BFR info written to BFR");
                return RPC_SUCCESS;
            }
        });
    }


    public static Future<RpcResult<Void>> writeMultiCastInfo(ConfigurationType type,Channel channel) {
        LOG.info("configurations write to node : multicast info");
        MountPoint node = getMountPoin(channel.getNodeId());
        if (null == getMountPoin(channel.getNodeId())) {

            return Futures.immediateFuture(RPC_FAILED);
        }
        final DataBroker nodeBroker = node.getService(DataBroker.class).get();
        final WriteTransaction writeTransaction = nodeBroker.newWriteOnlyTransaction();

        switch (type) {
            case CREATE:
                break;
            case UPDATE:
                break;
            case DELETE:
                break;
            default:
                break;

        }
        final CheckedFuture<Void, TransactionCommitFailedException> submit = writeTransaction.submit();

        return Futures.transform(submit, new Function<Void, RpcResult<Void>>() {
            @Override
            public RpcResult<Void> apply(final Void result) {
                LOG.info(" BFR info written to BFR");
                return RPC_SUCCESS;
            }
        });


    }



}
