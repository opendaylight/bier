/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.teconfig;

import java.util.Collection;

import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BierNodeTeBpChangeListener implements DataTreeChangeListener<TeBp> {

    private static final Logger LOG = LoggerFactory.getLogger(BierNodeTeBpChangeListener.class);
    private BiftInfoProcess biftInfoProcess;
    private static final int TE_BP_ADD = 1;
    private static final int TE_BP_DELETE = 2;

    public BierNodeTeBpChangeListener(final DataBroker dataBroker, final RpcConsumerRegistry rpcConsumerRegistry,
                                      final BierTeBiftWriter bierTeBiftWriter,
                                      final BierTeBitstringWriter bierTeBitstringWriter) {
        biftInfoProcess = new BiftInfoProcess(dataBroker, rpcConsumerRegistry, bierTeBiftWriter, bierTeBitstringWriter);
    }

    private static final InstanceIdentifier<TeBp> TE_BP_IID = InstanceIdentifier.create(BierNetworkTopology.class)
            .child(BierTopology.class, new BierTopologyKey("example-linkstate-topology"))
            .child(BierNode.class).child(BierTeNodeParams.class)
            .child(TeDomain.class).child(TeSubDomain.class).child(TeBsl.class)
            .child(TeSi.class).child(TeBp.class);


    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<TeBp>> changes) {
        for (DataTreeModification<TeBp> change : changes) {
            DataObjectModification<TeBp> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    LOG.info("onDataTreeChanged - TeBp config with path {} was added or replaced: "
                            + "old TeBp: {}, new TeBp: {}", change.getRootPath().getRootIdentifier(),
                            rootNode.getDataBefore(), rootNode.getDataAfter());
                    processFwdInfo(change.getRootPath().getRootIdentifier(), rootNode.getDataAfter(), TE_BP_ADD);
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("onDataTreeChanged - TeBp config with path {} was modified: "
                            + "old TeBp: {}, new TeBp: {}", change.getRootPath().getRootIdentifier(),
                            rootNode.getDataBefore(), rootNode.getDataAfter());
                    biftInfoProcess.processModifiedFwdInfo(change.getRootPath().getRootIdentifier(),
                            rootNode.getDataAfter());
                    break;
                case DELETE:
                    LOG.info("onDataTreeChanged - TeBp config with path {} was deleted: old TeBp: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore());
                    processFwdInfo(change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(), TE_BP_DELETE);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type : {}"
                            + change.getRootNode().getModificationType());
            }
        }
    }


    public InstanceIdentifier<TeBp> getTeBpIid() {
        return TE_BP_IID;
    }

    private void processFwdInfo(InstanceIdentifier<TeBp> identifier, TeBp teBp, int type) {
        Si si = identifier.firstKeyOf(TeSi.class).getSi();
        Bsl bsl = identifier.firstIdentifierOf(TeSi.class).firstKeyOf(TeBsl.class).getBitstringlength();
        SubDomainId subDomainId = identifier.firstIdentifierOf(TeSi.class).firstIdentifierOf(TeBsl.class)
                .firstKeyOf(TeSubDomain.class).getSubDomainId();
        DomainId domainId = identifier.firstIdentifierOf(TeSi.class).firstIdentifierOf(TeBsl.class)
                .firstIdentifierOf(TeSubDomain.class).firstKeyOf(TeDomain.class).getDomainId();
        String homeNodeId = identifier.firstIdentifierOf(TeSi.class).firstIdentifierOf(TeBsl.class)
                .firstIdentifierOf(TeSubDomain.class).firstIdentifierOf(TeDomain.class)
                .firstIdentifierOf(BierTeNodeParams.class).firstKeyOf(BierNode.class).getNodeId();
        String homeNodeTpId = teBp.getTpId();
        Integer homeNodeBp = teBp.getBitposition();

        if (TE_BP_ADD == type) {
            biftInfoProcess.processAddedFwdInfo(homeNodeId, domainId, subDomainId, bsl, si, homeNodeTpId, homeNodeBp);
        } else if (TE_BP_DELETE == type) {
            biftInfoProcess.processDeletedFwdInfo(homeNodeId, domainId, subDomainId, bsl, si, homeNodeTpId, homeNodeBp);
        }
    }
}
