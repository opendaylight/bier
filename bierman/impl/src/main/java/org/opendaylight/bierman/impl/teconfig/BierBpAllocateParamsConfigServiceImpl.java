/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bierman.impl.teconfig;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.opendaylight.bierman.impl.RpcUtil;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.AddSubdomainBslSiInput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.AddSubdomainBslSiOutput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.AddSubdomainBslSiOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.BierBpAllocateParams;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.BierBpAllocateParamsConfigService;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.DeleteSubdomainBslSiInput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.DeleteSubdomainBslSiOutput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.DeleteSubdomainBslSiOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.QuerySubdomainBslSiInput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.QuerySubdomainBslSiOutput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.QuerySubdomainBslSiOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.bier.bp.allocate.params.TopoBpAllocateParams;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.bier.bp.allocate.params.TopoBpAllocateParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.bier.bp.allocate.params.TopoBpAllocateParamsKey;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.query.subdomain.bsl.si.output.SiOfModel;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.query.subdomain.bsl.si.output.SiOfModelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.query.subdomain.bsl.si.output.SiOfModelKey;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.SubdomainBpAllocate;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.SubdomainBpAllocateBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.SubdomainBpAllocateKey;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.subdomain.bp.allocate.BslBpAllocate;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.subdomain.bp.allocate.BslBpAllocateBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.subdomain.bp.allocate.BslBpAllocateKey;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.subdomain.bp.allocate.bsl.bp.allocate.SiBpAllocate;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.subdomain.bp.allocate.bsl.bp.allocate.SiBpAllocateBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.subdomain.bsl.si.list.subdomain.bp.allocate.bsl.bp.allocate.SiBpAllocateKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BierBpAllocateParamsConfigServiceImpl implements BierBpAllocateParamsConfigService {
    private static final Logger LOG = LoggerFactory.getLogger(BierBpAllocateParamsConfigServiceImpl.class);
    private DataBroker dataBroker;

    public BierBpAllocateParamsConfigServiceImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public ListenableFuture<RpcResult<QuerySubdomainBslSiOutput>> querySubdomainBslSi(QuerySubdomainBslSiInput input) {
        LOG.info("QuerySubdomainBslSiInput" + input);
        if (null == input || null == input.getTopologyId() || null == input.getSubdomainValue()
                || null == input.getAllocateModel()) {
            LOG.info("Input params error!");
            return null;
        }
        SubdomainBpAllocate subdomainBpAllocate = readSubdomainBpAllocate(input.getTopologyId(),
                input.getSubdomainValue());
        if (null == subdomainBpAllocate) {
            return null;
        }
        List<SiOfModel> siOfModelList = new ArrayList<>();
        List<BslBpAllocate> bslBpAllocateList = subdomainBpAllocate.getBslBpAllocate();
        if (null != bslBpAllocateList) {
            for (BslBpAllocate bslBpAllocate : bslBpAllocateList) {
                if (bslBpAllocate.getBslValue().getIntValue() == input.getBslValue().getIntValue()) {
                    List<SiBpAllocate> siBpAllocateList = bslBpAllocate.getSiBpAllocate();
                    for (SiBpAllocate siBpAllocate : siBpAllocateList) {
                        if (siBpAllocate.getAllocateModel().getIntValue() == input.getAllocateModel().getIntValue()) {
                            SiOfModelBuilder siOfModelBuilder = new SiOfModelBuilder();
                            siOfModelBuilder.withKey(new SiOfModelKey(siBpAllocate.getSiValue()));
                            siOfModelBuilder.setSiValue(siBpAllocate.getSiValue());
                            siOfModelList.add(siOfModelBuilder.build());
                        }
                    }
                }
            }
        }
        QuerySubdomainBslSiOutputBuilder builder = new QuerySubdomainBslSiOutputBuilder();
        builder.setSiOfModel(siOfModelList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    private SubdomainBpAllocate readSubdomainBpAllocate(String topologyId, SubDomainId subdomainId) {
        InstanceIdentifier<SubdomainBpAllocate> subdomainIID = InstanceIdentifier.create(BierBpAllocateParams.class)
                .child(TopoBpAllocateParams.class, new TopoBpAllocateParamsKey(topologyId))
                .child(SubdomainBpAllocate.class, new SubdomainBpAllocateKey(subdomainId));
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        SubdomainBpAllocate subdomainBpAllocate;
        try {
            Optional<SubdomainBpAllocate> optinal = tx.read(LogicalDatastoreType.CONFIGURATION, subdomainIID)
                    .checkedGet();
            if (optinal.isPresent()) {
                subdomainBpAllocate = optinal.get();
            } else {
                SubdomainBpAllocateBuilder builder = new SubdomainBpAllocateBuilder();
                subdomainBpAllocate = builder.build();
            }
        } catch (ReadFailedException e) {
            LOG.info("Read subdomain failed!");
            return null;
        }

        return subdomainBpAllocate;
    }

    @Override
    public ListenableFuture<RpcResult<DeleteSubdomainBslSiOutput>> deleteSubdomainBslSi(DeleteSubdomainBslSiInput input) {
        LOG.info("DeleteSubdomainBslSiInput" + input);
        DeleteSubdomainBslSiOutputBuilder builder = new DeleteSubdomainBslSiOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getSubdomainValue()
                || null == input.getBslValue()) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false, "input  params error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        InstanceIdentifier<SubdomainBpAllocate> subdomainIID = InstanceIdentifier.create(BierBpAllocateParams.class)
                .child(TopoBpAllocateParams.class, new TopoBpAllocateParamsKey(input.getTopologyId()))
                .child(SubdomainBpAllocate.class, new SubdomainBpAllocateKey(input.getSubdomainValue()));
        InstanceIdentifier<BslBpAllocate> bslIID = subdomainIID.child(BslBpAllocate.class, new BslBpAllocateKey(
                input.getBslValue()));
        InstanceIdentifier<SiBpAllocate> siIID = null;
        if (null != input.getSiValue()) {
            siIID = bslIID.child(SiBpAllocate.class, new SiBpAllocateKey(input.getSiValue()));
        }
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        SubdomainBpAllocate subdomainBpAllocate = readSubdomainBpAllocate(input.getTopologyId(),
                input.getSubdomainValue());

        List<BslBpAllocate> bslBpAllocateList = subdomainBpAllocate.getBslBpAllocate();
        if (null != bslBpAllocateList) {
            for (BslBpAllocate bslBpAllocate : bslBpAllocateList) {
                if (bslBpAllocate.getBslValue().getIntValue() == input.getBslValue().getIntValue()) {
                    List<SiBpAllocate> siBpAllocateList = bslBpAllocate.getSiBpAllocate();
                    if (siBpAllocateList.size() != 1 && null != input.getSiValue()) {
                        tx.delete(LogicalDatastoreType.CONFIGURATION, siIID);
                    } else if (subdomainBpAllocate.getBslBpAllocate().size() != 1) {
                        tx.delete(LogicalDatastoreType.CONFIGURATION, bslIID);
                    } else {
                        tx.delete(LogicalDatastoreType.CONFIGURATION, subdomainIID);
                    }
                    try {
                        tx.submit().get();
                    } catch (InterruptedException | ExecutionException e) {
                        builder.setConfigureResult(RpcUtil.getConfigResult(false, "Delete from datastore failed!"));
                        return RpcResultBuilder.success(builder.build()).buildFuture();
                    }
                }
            }
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<AddSubdomainBslSiOutput>> addSubdomainBslSi(AddSubdomainBslSiInput input) {
        LOG.info("AddSubdomainBslSiInput" + input);
        AddSubdomainBslSiOutputBuilder builder = new AddSubdomainBslSiOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getSubdomainValue()
                || null == input.getBslValue() || null == input.getSiValue()) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false, "input  params error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        SiBpAllocateBuilder siBpAllocateBuilder = new SiBpAllocateBuilder();
        siBpAllocateBuilder.setSiValue(input.getSiValue());
        siBpAllocateBuilder.setAllocateModel(input.getAllocateModel());
        siBpAllocateBuilder.withKey(new SiBpAllocateKey(input.getSiValue()));
        List<SiBpAllocate> siBpAllocateList = new ArrayList<>();
        siBpAllocateList.add(siBpAllocateBuilder.build());

        BslBpAllocateBuilder bslBpAllocateBuilder = new BslBpAllocateBuilder();
        bslBpAllocateBuilder.setBslValue(input.getBslValue());
        bslBpAllocateBuilder.withKey(new BslBpAllocateKey(input.getBslValue()));
        bslBpAllocateBuilder.setSiBpAllocate(siBpAllocateList);
        List<BslBpAllocate> bslBpAllocateList = new ArrayList<>();
        bslBpAllocateList.add(bslBpAllocateBuilder.build());

        SubdomainBpAllocateBuilder subdomainBpAllocateBuilder = new SubdomainBpAllocateBuilder();
        subdomainBpAllocateBuilder.withKey(new SubdomainBpAllocateKey(input.getSubdomainValue()));
        subdomainBpAllocateBuilder.setSubdomainValue(input.getSubdomainValue());
        subdomainBpAllocateBuilder.setBslBpAllocate(bslBpAllocateList);
        List<SubdomainBpAllocate> subdomainBpAllocateList = new ArrayList<>();
        subdomainBpAllocateList.add(subdomainBpAllocateBuilder.build());

        TopoBpAllocateParamsBuilder topoBpAllocateParamsBuilder = new TopoBpAllocateParamsBuilder();
        topoBpAllocateParamsBuilder.withKey(new TopoBpAllocateParamsKey(input.getTopologyId()));
        topoBpAllocateParamsBuilder.setSubdomainBpAllocate(subdomainBpAllocateList);
        topoBpAllocateParamsBuilder.setTopologyId(input.getTopologyId());

        InstanceIdentifier<TopoBpAllocateParams> topoPath = InstanceIdentifier.create(BierBpAllocateParams.class)
                .child(TopoBpAllocateParams.class, new TopoBpAllocateParamsKey(input.getTopologyId()));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.merge(LogicalDatastoreType.CONFIGURATION, topoPath, topoBpAllocateParamsBuilder.build(), true);

        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false, "Write datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }
}
