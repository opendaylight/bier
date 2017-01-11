/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.listener;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.common.IidConstants;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;


import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.NotificationsService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.IetfBierListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class NetconfNodesListener implements DataTreeChangeListener<Node> {
    private static final Logger LOG = LoggerFactory.getLogger(NetconfNodesListener.class);
    private ListenerRegistration<NetconfNodesListener> listenerRegistration;
    private Map<NodeId, ListenerRegistration<IetfBierListener>> mapNodeListenerReg = Maps.newHashMap();
    private NetconfDataOperator netconfDataOperator ;


    public NetconfNodesListener(final DataBroker dataBroker,final NetconfDataOperator netconfDataOperator) {
        listenerRegistration =
                dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<Node>(LogicalDatastoreType.OPERATIONAL,
                        IidConstants.NETCONF_TOPO_IID.child(Node.class)),this);
        this.netconfDataOperator = netconfDataOperator;
        LOG.info("Begin to listen to the changes of netconf nodes!");

    }

    public void unregisterListener() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }

        Iterator<Map.Entry<NodeId, ListenerRegistration<IetfBierListener>>> entries =
                mapNodeListenerReg.entrySet().iterator();

        while (entries.hasNext()) {

            Map.Entry<NodeId, ListenerRegistration<IetfBierListener>> entry = entries.next();

            if (entry.getValue() != null ) {
                entry.getValue().close();
                LOG.info("unregisterListener {}",entry.getKey());

            }

        }

    }

    private void registerNotificationListener(final NodeId nodeId) {

        MountPoint mountPoint = netconfDataOperator.getMountPoint(nodeId.getValue());
        if (null == mountPoint) {
            return;
        }
        final IetfBierListener listener;
        listener = new BierNotificationListener();
        final Optional<NotificationService> notificationService = mountPoint.getService(NotificationService.class);
        final ListenerRegistration<IetfBierListener> listenerRegistration =
                notificationService.get().registerNotificationListener(listener);
        mapNodeListenerReg.put(nodeId,listenerRegistration);

        final Optional<RpcConsumerRegistry> service = mountPoint.getService(RpcConsumerRegistry.class);
        final NotificationsService rpcService = service.get().getRpcService(NotificationsService.class);
        final CreateSubscriptionInputBuilder createSubscriptionInputBuilder = new CreateSubscriptionInputBuilder();
        rpcService.createSubscription(createSubscriptionInputBuilder.build());
    }





    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Node>> changes) {
        LOG.info("Netconf nodes change!");
        for (DataTreeModification<Node> change:changes) {
            DataObjectModification<Node> rootNode = change.getRootNode();
            if (rootNode.getDataAfter().getNodeId().getValue().equals("controller-config")) {

                LOG.info("Netconf node {} ignored",rootNode.getDataAfter().getNodeId().getValue());
                break;
            }
            switch (rootNode.getModificationType()) {
                case WRITE:
                    LOG.info("Netconf node {} was created",rootNode.getDataAfter().getNodeId().getValue());
                    NetconfNode ncNode = rootNode.getDataAfter().getAugmentation(NetconfNode.class);
                    if (ncNode.getConnectionStatus() == NetconfNodeConnectionStatus.ConnectionStatus.Connected) {
                        registerNotificationListener(rootNode.getDataAfter().getNodeId());
                    }
                    break;
                case SUBTREE_MODIFIED:
                    NetconfNode ncNodeNew = rootNode.getDataAfter().getAugmentation(NetconfNode.class);
                    NetconfNode ncNodeOld = rootNode.getDataBefore().getAugmentation(NetconfNode.class);
                    if ((ncNodeNew.getConnectionStatus() == NetconfNodeConnectionStatus.ConnectionStatus.Connected)
                            &&
                            (ncNodeOld.getConnectionStatus() != NetconfNodeConnectionStatus.ConnectionStatus.Connected)
                    ) {
                        registerNotificationListener(rootNode.getDataAfter().getNodeId());
                    }

                    break;
                case DELETE:
                    LOG.info("Netconf node {} was deleted",rootNode.getDataAfter().getNodeId().getValue());
                    break;
                default:
                    break;

            }

        }
    }
}

