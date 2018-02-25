/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.service.impl;

import java.util.Collection;

import org.opendaylight.bier.service.impl.teconfig.AddResetBitMaskProcess;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BpAssignmentStrategy;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.TeFrrConfigure;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.TopologyTeFrr;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.LinkTeFrr;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.TeFrrDomain;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.TeFrrSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.te.frr.sub.domain.TeFrrBsl;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.te.frr.sub.domain.te.frr.bsl.TeFrrSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeFrrBpChangeListener implements DataTreeChangeListener<TeFrrSi> {

    private static final Logger LOG = LoggerFactory.getLogger(TeFrrBpChangeListener.class);
    private static final String TOPOLOGY_ID = "example-linkstate-topology";
    private AddResetBitMaskProcess addResetBitMaskProcess;
    private Util util;

    public TeFrrBpChangeListener(DataBroker dataBroker) {
        this.util = new Util(dataBroker);
        this.addResetBitMaskProcess = AddResetBitMaskProcess.getInstance();
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<TeFrrSi>> changes) {
        for (DataTreeModification<TeFrrSi> change : changes) {
            DataObjectModification<TeFrrSi> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    LOG.info("Frr info add!");
                    processFrrInfo(change.getRootPath().getRootIdentifier(), rootNode.getDataAfter(),
                            AddResetBitMaskProcess.FRR_BP_ADD);
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("Do nothing!");
                    break;
                case DELETE:
                    LOG.info("Frr info delete!");
                    processFrrInfo(change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                            AddResetBitMaskProcess.FRR_BP_DELETE);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type : {}"
                            + change.getRootNode().getModificationType());
            }
        }
    }

    private void processFrrInfo(InstanceIdentifier<TeFrrSi> identifier, TeFrrSi teFrrSi, int type) {
        TeBpBuilder teBpBuilder = new TeBpBuilder();
        teBpBuilder.setBitposition(teFrrSi.getTeFrrBp().getValue());
        TeSiBuilder teSiBuilder = new TeSiBuilder();
        teSiBuilder.setSi(teFrrSi.getSi());
        TeBslBuilder teBslBuilder = new TeBslBuilder();
        Bsl bsl = identifier.firstKeyOf(TeFrrBsl.class).getBitstringlength();
        teBslBuilder.setBitstringlength(bsl);
        TeSubDomainBuilder teSubDomainBuilder = new TeSubDomainBuilder();
        SubDomainId subdomainId = identifier.firstIdentifierOf(TeFrrBsl.class).firstKeyOf(TeFrrSubDomain.class)
                .getSubDomainId();
        teSubDomainBuilder.setSubDomainId(subdomainId);
        TeDomainBuilder teDomainBuilder = new TeDomainBuilder();
        DomainId domainId = identifier.firstIdentifierOf(TeFrrBsl.class).firstIdentifierOf(TeFrrSubDomain.class)
                .firstKeyOf(TeFrrDomain.class).getDomainId();
        teDomainBuilder.setDomainId(domainId);
        String linkId = identifier.firstIdentifierOf(TeFrrBsl.class).firstIdentifierOf(TeFrrSubDomain.class)
                .firstIdentifierOf(TeFrrDomain.class).firstKeyOf(LinkTeFrr.class).getLinkId();
        BierLink bierLink = util.getBierLinkByLinkId(TOPOLOGY_ID, linkId);
        BpAssignmentStrategy bpAssignmentStrategy = teFrrSi.getAssignmentStrategy();
        addResetBitMaskProcess.processFrrInfo(bierLink, bpAssignmentStrategy, teDomainBuilder.build(),
                teSubDomainBuilder.build(), teBslBuilder.build(), teSiBuilder.build(), teBpBuilder.build(), null, type);
    }

    private static final InstanceIdentifier<TeFrrSi> TE_FRR_SI_IID = InstanceIdentifier.create(TeFrrConfigure.class)
            .child(TopologyTeFrr.class)
            .child(LinkTeFrr.class)
            .child(TeFrrDomain.class)
            .child(TeFrrSubDomain.class)
            .child(TeFrrBsl.class)
            .child(TeFrrSi.class);

    public InstanceIdentifier<TeFrrSi> getTeFrrSiIid() {
        return TE_FRR_SI_IID;
    }
}
