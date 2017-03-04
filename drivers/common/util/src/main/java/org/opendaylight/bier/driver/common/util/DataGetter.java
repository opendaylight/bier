/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.common.util;

import com.google.common.base.Optional;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataGetter {

    private static final Logger LOG = LoggerFactory.getLogger(DataGetter.class);


    public static MountPoint getMountPoint(String nodeId, ConfigurationResult result,MountPointService mountService) {
        if (mountService == null) {
            LOG.error(result.MOUNT_SERVICE_NULL);
            result.setCfgResult(ConfigurationResult.Result.FAILED);
            result.setFailureReason(result.MOUNT_SERVICE_NULL);
            return null;
        }
        Optional<MountPoint> nodeMountPoint = mountService.getMountPoint(IidConstants.NETCONF_TOPO_IID
                .child(Node.class, new NodeKey(new NodeId(nodeId))));


        if (!nodeMountPoint.isPresent()) {
            LOG.error(ConfigurationResult.MOUNT_POINT_FAILUE + nodeId);
            result.setCfgResult(ConfigurationResult.Result.FAILED);
            result.setFailureReason(ConfigurationResult.MOUNT_POINT_FAILUE + nodeId);
            return null;
        }

        result.setCfgResult(ConfigurationResult.Result.SUCCESSFUL);
        return nodeMountPoint.get();

    }



    public static DataBroker getDataBroker(String nodeId, ConfigurationResult result,MountPointService mountService) {

        MountPoint mountPoint = getMountPoint(nodeId,result,mountService);

        if (mountPoint == null) {
            return null;
        }


        Optional<DataBroker> nodeBroker =  mountPoint.getService(DataBroker.class);

        if (!nodeBroker.isPresent()) {
            LOG.error(ConfigurationResult.DATA_BROKER_FAILUE + nodeId);
            result.setCfgResult(ConfigurationResult.Result.FAILED);
            result.setFailureReason(ConfigurationResult.DATA_BROKER_FAILUE + nodeId);
            return null;

        }
        result.setCfgResult(ConfigurationResult.Result.SUCCESSFUL);
        return nodeBroker.get();

    }



    public static <T extends DataObject> T readData(DataBroker dataBroker,
                                                    InstanceIdentifier<T> path) {
        T data = null;
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        Optional<T> optionalData;
        try {
            optionalData = transaction.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (optionalData.isPresent()) {
                data = optionalData.get();
            } else {
                LOG.debug("{}: Failed to read {}", Thread.currentThread().getStackTrace()[1], path);
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", path, e);
        }
        transaction.close();
        return data;
    }

    public static <T extends DataObject> T readData(String nodeId,
                                             InstanceIdentifier<T> path,MountPointService mountService) {




        ConfigurationResult ncResult = new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        final DataBroker nodeBroker = DataGetter.getDataBroker(nodeId, ncResult, mountService);
        if (nodeBroker == null) {
            return null;
        }
        return readData(nodeBroker,path);

    }


}
