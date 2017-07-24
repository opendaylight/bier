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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.common.util.IidConstants;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainIdCollision;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.AvailableCapabilities;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class NetconfNodesListener implements DataTreeChangeListener<Node> {


    private static final Logger LOG = LoggerFactory.getLogger(NetconfNodesListener.class);
    private ListenerRegistration<NetconfNodesListener> listenerRegistration;

    public Map<NodeId, ListenerRegistration<IetfBierListener>> getMapNodeListenerReg() {
        return mapNodeListenerReg;
    }

    private Map<NodeId, ListenerRegistration<IetfBierListener>> mapNodeListenerReg = Maps.newHashMap();
    private final DataBroker dataBroker;
    private final NetconfDataOperator netconfDataOperator ;


    public NetconfNodesListener(final DataBroker dataBroker,final NetconfDataOperator netconfDataOperator) {
        this.dataBroker = dataBroker;
        this.netconfDataOperator = netconfDataOperator;
    }

    public void init() {
        listenerRegistration =
                dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<Node>(LogicalDatastoreType.OPERATIONAL,
                        IidConstants.NETCONF_TOPO_IID.child(Node.class)),this);
        LOG.info("Begin to listen to the changes of netconf nodes!");
    }

    public void close() {
        unregisterListener();
    }

    public void unregisterListener() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }

        Iterator<Map.Entry<NodeId, ListenerRegistration<IetfBierListener>>> entries =
                mapNodeListenerReg.entrySet().iterator();

        while (entries.hasNext()) {

            Map.Entry<NodeId, ListenerRegistration<IetfBierListener>> entry = entries.next();
            unRegisterNotificationListener(entry.getKey());
        }

    }


    private boolean hasNotification(AvailableCapabilities availableCapabilities) {

        List<String> capabilities =
                availableCapabilities.getAvailableCapability().stream().map(cp ->
                        cp.getCapability()).collect(Collectors.toList());
        LOG.info("Capabilities: {}", capabilities);

        if (capabilities.contains(QName.create(SubDomainIdCollision.QNAME, "ietf-bier").toString())) {
            LOG.info("capabitlities contain notifications {}",
                    QName.create(SubDomainIdCollision.QNAME, "ietf-bier").toString());
            return true;
        }
        LOG.info("capabitlities do not contain notifications");
        return false;

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

    public void unRegisterNotificationListener(final NodeId nodeId) {
        final ListenerRegistration<IetfBierListener> listenerRegistration = mapNodeListenerReg.get(nodeId);
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }
        mapNodeListenerReg.remove(nodeId);
        LOG.info("unregisterListener {}",nodeId);
    }



    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Node>> changes) {
        LOG.info("Netconf nodes change!");
        Node nodeBefore = null;
        Node nodeAfter = null;
        Node nodeInfo = null;
        for (DataTreeModification<Node> change:changes) {
            DataObjectModification<Node> rootNode = change.getRootNode();
            nodeBefore = rootNode.getDataBefore();
            nodeAfter = rootNode.getDataAfter();
            if (nodeBefore != null) {
                nodeInfo  = nodeBefore;
            } else {
                nodeInfo = nodeAfter;
            }
            if (nodeInfo == null) {
                LOG.info("Netconf node info null");
                continue;
            }
            if (nodeInfo.getNodeId().getValue().equals("controller-config")) {
                LOG.info("Netconf node {} ignored",rootNode.getDataAfter().getNodeId().getValue());
                continue;
            }
            switch (rootNode.getModificationType()) {
                case WRITE:
                    LOG.info("Netconf node {} was created", nodeAfter.getNodeId().getValue());
                    NetconfNode ncNode = nodeAfter.getAugmentation(NetconfNode.class);
                    if (ncNode.getConnectionStatus() == NetconfNodeConnectionStatus.ConnectionStatus.Connected) {
                        if (hasNotification(ncNode.getAvailableCapabilities())) {
                            registerNotificationListener(nodeAfter.getNodeId());
                        }

                    }
                    break;
                case SUBTREE_MODIFIED:
                    NetconfNode ncNodeNew = nodeAfter.getAugmentation(NetconfNode.class);
                    NetconfNode ncNodeOld = nodeBefore.getAugmentation(NetconfNode.class);
                    if ((ncNodeNew.getConnectionStatus() == NetconfNodeConnectionStatus.ConnectionStatus.Connected)
                            &&
                            (ncNodeOld.getConnectionStatus() != NetconfNodeConnectionStatus.ConnectionStatus.Connected)
                    ) {
                        if (hasNotification(ncNodeNew.getAvailableCapabilities())) {
                            registerNotificationListener(nodeAfter.getNodeId());
                        }
                    }

                    break;
                case DELETE:
                    LOG.info("Netconf node {} was deleted", nodeBefore.getNodeId().getValue());
                    unRegisterNotificationListener(nodeBefore.getNodeId());
                    break;
                default:
                    break;

            }

        }
    }
}

