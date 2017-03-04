/*
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.test.driver;

import com.google.common.base.Function;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;


import org.opendaylight.bier.adapter.api.BierConfigReader;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigReader;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;


import org.opendaylight.bier.driver.common.util.DataGetter;
import org.opendaylight.bier.driver.common.util.IidConstants;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResultBuilder;

import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.CheckBierGlobalInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.CheckBierGlobalOutput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.CheckBierGlobalOutputBuilder;

import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.CheckChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.CheckChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.CheckChannelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.ConfigType;

import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.ReadBierGlobalInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.ReadBierGlobalOutput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.ReadBierGlobalOutputBuilder;

import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetChannelOutputBuilder;

import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetDomainConfigInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetDomainConfigOutput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetDomainConfigOutputBuilder;

import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetEgressNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetEgressNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetEgressNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetIpv4ConfigInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetIpv4ConfigOutput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetIpv4ConfigOutputBuilder;

import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetSubdomainConfigInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetSubdomainConfigOutput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetSubdomainConfigOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.TestDriverService;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobal;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.AfBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4Builder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestDriverProvider implements TestDriverService {
    private static final Logger LOG = LoggerFactory.getLogger(TestDriverProvider.class);
    private BierConfigWriter bierConfigWriter;
    private BierConfigReader bierConfigReader;
    private RpcProviderRegistry rpcRegistry;
    private final DataBroker dataBroker;
    private BindingAwareBroker.RpcRegistration<TestDriverService> rpcReg;
    private ChannelConfigWriter channelConfigWrite;
    private ChannelConfigReader channelConfigReader;



    public TestDriverProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;

    }

    public void setBierConfigWriter(BierConfigWriter bierConfigWriter) {
        this.bierConfigWriter = bierConfigWriter;
    }

    public void setBierConfigReader(BierConfigReader bierConfigReader) {
        this.bierConfigReader = bierConfigReader;
    }

    public void setRpcRegistry(RpcProviderRegistry rpcRegistry) {
        this.rpcRegistry = rpcRegistry;
    }

    public void setChannelConfigWriter(ChannelConfigWriter channelConfigWriter) {
        this.channelConfigWrite = channelConfigWriter;
    }

    public void setChannelConfigReader(ChannelConfigReader channelConfigReader) {
        this.channelConfigReader = channelConfigReader;
    }



    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("Session Initiated");
        rpcReg = rpcRegistry.addRpcImplementation(TestDriverService.class, this);

    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info(" Closed");
        rpcReg.close();

    }

    public ConfigurationType getConfigurationType(ConfigType type) {
        switch (type) {
            case ADD:
                return ConfigurationType.ADD;
            case MODIFY:
                return ConfigurationType.MODIFY;
            case DELETE:
                return ConfigurationType.DELETE;
            default:
                return null;
        }

    }

    public ConfigureResult buildResult(ConfigurationResult result) {
        ConfigureResultBuilder resultBuilder = new ConfigureResultBuilder();
        if (result.isSuccessful()) {
            return new ConfigureResultBuilder().setResult(ConfigureResult.Result.SUCCESS).build();
        } else {
            return new ConfigureResultBuilder()
                    .setResult(ConfigureResult.Result.FAILURE)
                    .setErrorCause(result.getFailureReason())
                    .build();
        }

    }

    @Override
    public Future<RpcResult<SetDomainConfigOutput>> setDomainConfig(SetDomainConfigInput input) {
        ConfigurationType type = getConfigurationType(input.getWriteType());
        ConfigurationResult result = bierConfigWriter.writeDomain(type,input.getNodeName(),
                new DomainBuilder().setBierGlobal(input.getBierGlobal()).build());
        SetDomainConfigOutput output =
                new SetDomainConfigOutputBuilder().setConfigureResult(buildResult(result)).build();
        return RpcResultBuilder.success(output).buildFuture();
    }

    @Override
    public Future<RpcResult<SetSubdomainConfigOutput>> setSubdomainConfig(SetSubdomainConfigInput input) {
        ConfigurationType type = getConfigurationType(input.getWriteType());
        SubDomain subDomain = new SubDomainBuilder()
                .setSubDomainId(input.getSubDomainId())
                .setAf(input.getAf())
                .setBfrId(input.getBfrId())
                .setBitstringlength(input.getBitstringlength())
                .setIgpType(input.getIgpType())
                .setMtId(input.getMtId())
                .build();
        ConfigurationResult result = bierConfigWriter.writeSubdomain(type,
                input.getNodeName(),
                new DomainId(input.getDomainId()),
                subDomain);
        SetSubdomainConfigOutput output =
                new SetSubdomainConfigOutputBuilder().setConfigureResult(buildResult(result)).build();
        return RpcResultBuilder.success(output).buildFuture();


    }

    @Override
    public Future<RpcResult<SetIpv4ConfigOutput>> setIpv4Config(SetIpv4ConfigInput input) {
        ConfigurationType type = getConfigurationType(input.getWriteType());
        Ipv4 ipv4 = new Ipv4Builder()
                .setBitstringlength(input.getBitstringlength())
                .setBierMplsLabelBase(input.getBierMplsLabelBase())
                .setBierMplsLabelRangeSize(input.getBierMplsLabelRangeSize())
                .build();
        ConfigurationResult result = bierConfigWriter.writeSubdomainIpv4(type,
                input.getNodeName(),
                new DomainId(input.getDomainId()),
                new SubDomainId(input.getSubDomainId()),
                ipv4);
        SetIpv4ConfigOutput output =
                new SetIpv4ConfigOutputBuilder().setConfigureResult(buildResult(result)).build();
        return RpcResultBuilder.success(output).buildFuture();

    }

    @Override
    public Future<RpcResult<ReadBierGlobalOutput>> readBierGlobal(ReadBierGlobalInput input) {

        BierGlobal bierGlobal = bierConfigReader.readBierGlobal(input.getNodeName());
        LOG.info("readBierGlobal {}",bierGlobal);
        ReadBierGlobalOutputBuilder outputBuilder = new ReadBierGlobalOutputBuilder().setBierGlobal(bierGlobal);

        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();


    }

    public SubDomain bulid(SubDomain subDomain) {
        if (subDomain.getAf() == null) {
            return null;
        }
        List<Ipv4> ipv4List =  subDomain.getAf().getIpv4();
        ArrayList<Ipv4> ipv4ArrayList = new ArrayList<Ipv4>();
        if ((ipv4List != null) && (!ipv4List.isEmpty())) {
            for (Ipv4 ipv4 : ipv4List) {
                ipv4ArrayList.add(new Ipv4Builder(ipv4).build());
            }
        }
        return new SubDomainBuilder()
                .setAf(new AfBuilder().setIpv4(ipv4ArrayList).build())
                .setBitstringlength(subDomain.getBitstringlength())
                .setIgpType(subDomain.getIgpType())
                .setBfrId(subDomain.getBfrId())
                .setMtId(subDomain.getMtId())
                .setSubDomainId(subDomain.getSubDomainId())
                .build();



    }

    public BierGlobal bulid(BierGlobal bierGlobal) {
        List<SubDomain> subDomainList =  bierGlobal.getSubDomain();
        ArrayList<SubDomain> subDomainArrayList = new ArrayList<SubDomain>();
        if ((subDomainList != null) && (!subDomainList.isEmpty())) {

            for (SubDomain subDomain:subDomainList) {
                subDomainArrayList.add(bulid(subDomain));
            }

        }
        return new BierGlobalBuilder()
                .setIpv4BfrPrefix(bierGlobal.getIpv4BfrPrefix())
                .setSubDomain(subDomainArrayList)
                .setBfrId(bierGlobal.getBfrId())
                .setBitstringlength(bierGlobal.getBitstringlength())
                .setEncapsulationType(bierGlobal.getEncapsulationType())
                .build();

    }

    public BierGlobal readBierGlobalFromController(String nodeId) {

        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(nodeId));

        BierNode bierNode = DataGetter.readData(dataBroker,bierNodeIId);
        if ((bierNode != null) && (bierNode.getBierNodeParams() != null)) {
            List<Domain> domainList = bierNode.getBierNodeParams().getDomain();
            if ((domainList == null) || domainList.isEmpty()) {
                LOG.info("readBierGlobalFromController domainList is null");
                return null;
            }
            for (Domain domain : domainList) {
                LOG.info("readBierGlobalFromController {} {}",domain.getBierGlobal(),nodeId);
                return bulid(domain.getBierGlobal());
            }

        }
        return null;




    }


    static String buildDetailInfo( Object controller, Object device) {

        String controllerString = String.valueOf(controller);
        String deviceString = String.valueOf(device);

        return  "\n" + "controller: \n"  + controllerString  + "\n"
                + " device : \n"  + deviceString  + "\n";

    }

    public String compareBierGlobal(BierGlobal bierGlobalFromController,BierGlobal bierGlobalFromDevice) {

        String detail = "";

        LOG.info("compareBierGlobal controller:{}, device:{}",bierGlobalFromController,bierGlobalFromDevice);

        if (!Objects.equals(bierGlobalFromController.getBitstringlength(),
                bierGlobalFromDevice.getBitstringlength())) {
            detail = detail + buildDetailInfo(bierGlobalFromController.getBitstringlength(),
                    bierGlobalFromDevice.getBitstringlength());
        }

        if (!Objects.equals(bierGlobalFromController.getEncapsulationType(),
                bierGlobalFromDevice.getEncapsulationType())) {
            detail = detail + buildDetailInfo(bierGlobalFromController.getEncapsulationType(),
                    bierGlobalFromDevice.getEncapsulationType());
        }

        if (!Objects.equals(bierGlobalFromController.getBfrId(),
                bierGlobalFromDevice.getBfrId())) {
            detail = detail + buildDetailInfo(bierGlobalFromController.getBfrId(),
                    bierGlobalFromDevice.getBfrId());
        }

        if (!Objects.equals(bierGlobalFromController.getIpv4BfrPrefix(),
                bierGlobalFromDevice.getIpv4BfrPrefix())) {
            detail = detail + buildDetailInfo(bierGlobalFromController.getIpv4BfrPrefix(),
                    bierGlobalFromDevice.getIpv4BfrPrefix());
        }

        if (!Objects.equals(bierGlobalFromController.getIpv6BfrPrefix(),
                bierGlobalFromDevice.getIpv6BfrPrefix())) {
            detail = detail + buildDetailInfo(bierGlobalFromController.getIpv6BfrPrefix(),
                    bierGlobalFromDevice.getIpv6BfrPrefix());
        }

        if (!Objects.equals(bierGlobalFromController.getSubDomain(),
                bierGlobalFromDevice.getSubDomain())) {
            LOG.info("compareBierGlobal subdomain controller:{}, device:{}",bierGlobalFromController.getSubDomain(),
                    bierGlobalFromDevice.getSubDomain());
            detail = detail + buildDetailInfo(bierGlobalFromController.getSubDomain(),
                    bierGlobalFromDevice.getSubDomain());
        }

        return detail;
    }

    @Override
    public Future<RpcResult<CheckBierGlobalOutput>> checkBierGlobal(CheckBierGlobalInput input) {
        BierGlobal bierGlobalFromDevice = bierConfigReader.readBierGlobal(input.getNodeName());
        BierGlobal bierGlobalFromController = readBierGlobalFromController(input.getNodeName());
        String detail = "";

        boolean isEqual = false;

        if ((bierGlobalFromController == null) && (bierGlobalFromDevice != null)) {
            isEqual = false;
            detail = "bierGlobalFromController is null while bierGlobalFromDevice is not null.";
        } else if ((bierGlobalFromController == null ) && (bierGlobalFromDevice == null)) {
            isEqual = true;
            detail = "Both bierGlobalFromController and bierGlobalFromDevice are null";
        } else if ((bierGlobalFromController != null) && (bierGlobalFromDevice == null)) {
            isEqual = false;
            detail = "bierGlobalFromController is not null while bierGlobalFromDevice is  null.";
        } else {
            isEqual = bierGlobalFromController.equals(bierGlobalFromDevice);
            if (!isEqual) {
                detail = compareBierGlobal(bierGlobalFromController,bierGlobalFromDevice);
            }
        }

        CheckBierGlobalOutput output = new CheckBierGlobalOutputBuilder()
                .setConfigureResult(new ConfigureResultBuilder()
                        .setResult(isEqual ? ConfigureResult.Result.SUCCESS : ConfigureResult.Result.FAILURE)
                        .setErrorCause(detail)
                        .build())
                .build();
        return RpcResultBuilder.success(output).buildFuture();
    }


    @Override
    public Future<RpcResult<SetChannelOutput>> setChannel(SetChannelInput input) {


        ConfigurationType type =
                getConfigurationType(input.getWriteType());


        ConfigurationResult result = channelConfigWrite.writeChannel(type,
                    new ChannelBuilder()
                            .setDstGroup(input.getDstGroup())
                            .setEgressNode(Lists.transform(input.getEgressNode(), new Function<
                                        org.opendaylight.yang.gen.v1.urn.bier.test.driver
                                                .rev161219.set.channel.input.EgressNode,
                                        EgressNode
                                        >() {
                                    @java.lang.Override
                                    public EgressNode apply(org.opendaylight.yang.gen.v1.urn
                                                                    .bier.test.driver.rev161219
                                                                    .set.channel.input
                                                                    .EgressNode enode) {
                                        return new EgressNodeBuilder()
                                                .setEgressBfrId(new BfrId(enode.getEgressBfrId().getValue()))
                                                .setNodeId(enode.getNodeId())
                                                .build();
                                    }
                                }

                            ))

                            .setGroupWildcard(input.getGroupWildcard())
                            .setIngressBfrId(input.getIngressBfrId())
                            .setIngressNode(input.getIngressNode())
                            .setSourceWildcard(input.getSourceWildcard())
                            .setSrcIp(input.getSrcIp())
                            .setSubDomainId(input.getSubDomainId()).build()


            );

        SetChannelOutput output =
                new SetChannelOutputBuilder().setConfigureResult(buildResult(result)).build();
        return RpcResultBuilder.success(output).buildFuture();

    }

    @Override
    public Future<RpcResult<SetEgressNodeOutput>> setEgressNode(SetEgressNodeInput input) {
        ConfigurationType type =
                getConfigurationType(input.getWriteType());

        ConfigurationResult result = channelConfigWrite.writeChannelEgressNode(type,
                new ChannelBuilder()
                        .setDstGroup(input.getDstGroup())
                        .setEgressNode(Collections.singletonList(
                                new EgressNodeBuilder()
                                        .setEgressBfrId(input.getEgressBfrId())
                                        .setNodeId(input.getEgressNode())
                                        .build()))
                        .setGroupWildcard(input.getGroupWildcard())
                        .setIngressBfrId(input.getIngressBfrId())
                        .setIngressNode(input.getIngressNode())
                        .setSourceWildcard(input.getSourceWildcard())
                        .setSrcIp(input.getSrcIp())
                        .setSubDomainId(input.getSubDomainId()).build()

        );

        SetEgressNodeOutput output =
                        new SetEgressNodeOutputBuilder().setConfigureResult(buildResult(result)).build();
        return RpcResultBuilder.success(output).buildFuture();

    }


    private Channel getChannelByName(String channelName) {

        InstanceIdentifier<Channel> multicastInfoIid =
                IidConstants.BIER_CHANNEL_IID.child(Channel.class, new ChannelKey(channelName));
        return DataGetter.readData(dataBroker, multicastInfoIid);
    }

    private List<BfrId> getContollerBrfIdList(Channel channel) {

        List<EgressNode> egressNodeList = channel.getEgressNode();
        if ((egressNodeList == null) || (egressNodeList.isEmpty())) {
            return null;
        }
        return Lists.transform(egressNodeList,
                new Function<EgressNode, BfrId>() {
                    @java.lang.Override
                    public BfrId apply(EgressNode input) {
                        return input.getEgressBfrId();
                    }

                });

    }

    private List<BfrId> getDeviceBrfIdList(Channel channel) {
        return channelConfigReader.readChannel(channel);
    }

    @Override
    public Future<RpcResult<CheckChannelOutput>> checkChannel(CheckChannelInput input) {
        Channel channel = getChannelByName(input.getChannelName());
        List<BfrId> controllerEgress = getContollerBrfIdList(channel);
        List<BfrId> deviceEgress = getDeviceBrfIdList(channel);
        boolean isEqual = false;
        String detail = "";

        if ((controllerEgress == null) && (deviceEgress != null)) {
            isEqual = false;
            detail = "controllerEgress is null while deviceEgress is not null.";
        } else if ((controllerEgress == null ) && (deviceEgress == null)) {
            isEqual = true;
            detail = "Both controllerEgress and deviceEgress are null";
        } else if ((controllerEgress != null) && (deviceEgress == null)) {
            isEqual = false;
            detail = "controllerEgress is not null while deviceEgress is  null.";
        } else {
            isEqual = controllerEgress.containsAll(deviceEgress) & (deviceEgress.containsAll(controllerEgress));
            if (!isEqual) {
                detail = "\n controller egress : " + controllerEgress.toString()
                        + "\n device egress : " + deviceEgress.toString();
            }
        }

        CheckChannelOutput output = new CheckChannelOutputBuilder()
                .setConfigureResult(new ConfigureResultBuilder()
                        .setResult(isEqual ? ConfigureResult.Result.SUCCESS : ConfigureResult.Result.FAILURE)
                        .setErrorCause(detail)
                        .build())
                .build();
        return RpcResultBuilder.success(output).buildFuture();


    }



}
