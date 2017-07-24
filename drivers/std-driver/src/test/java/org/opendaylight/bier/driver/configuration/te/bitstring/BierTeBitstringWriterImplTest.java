/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.configuration.te.bitstring;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.configuration.te.bitstring.BierTeBitstringWriterImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.TePath;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.Path;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.te.path.Bitstring;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.te.path.BitstringBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class BierTeBitstringWriterImplTest  extends AbstractConcurrentDataBrokerTest {



    private MountPoint mountPoint;
    private DataBroker dataBroker ;
    private MountPointService mountPointService;
    // Optionals
    private Optional<MountPoint> optionalMountPointObject;
    private Optional<DataBroker> optionalDataBrokerObject;
    private BierTeBitstringWriterImpl bierTeBitstringWriter ;
    private NetconfDataOperator netconfDataOperator;

    private ConfigurationResult result =
            new ConfigurationResult(ConfigurationResult.Result.FAILED);

    private static final String NODE_ID = "nodeId";
    private static final Integer BP1 = 1;
    private static final Integer BP2 = 2;
    private static final Integer BP3 = 3;
    private static final Integer BP4 = 4;
    private static final Integer BP5 = 5;
    private static final Long PATH_ID = 1L;
    private static final SubDomainId SUB_DOMAIN_ID = new SubDomainId(100);
    private static final Bsl BSL = Bsl._64Bit;
    private static final Si SI = new Si(0);


    @Before
    public void before() throws Exception {
        dataBroker = getDataBroker();
    }

    private void buildMock() {
        mountPoint = mock(MountPoint.class);
        mountPointService = mock(MountPointService.class);
        optionalMountPointObject = mock(Optional.class);
        optionalDataBrokerObject = mock(Optional.class);

        when(mountPointService.getMountPoint(any(InstanceIdentifier.class))).thenReturn(optionalMountPointObject);
        when(mountPoint.getService(eq(DataBroker.class))).thenReturn(optionalDataBrokerObject);
        // Mock getting mountpoint
        when(optionalMountPointObject.isPresent()).thenReturn(true);
        // OptionalGetWithoutIsPresent
        when(optionalMountPointObject.get()).thenReturn(mountPoint);
        when(optionalDataBrokerObject.isPresent()).thenReturn(true);
        // OptionalGetWithoutIsPresent
        when(optionalDataBrokerObject.get()).thenReturn(dataBroker);

    }

    private void buildInstance() {
        netconfDataOperator = new NetconfDataOperator(mountPointService);
        bierTeBitstringWriter = new BierTeBitstringWriterImpl(netconfDataOperator);

    }

    private Bitstring buildBitstring(Integer bp) {
        return new BitstringBuilder()
                .setBitposition(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml
                        .ns.yang.ietf.bier.te.rev161013.BitString(BP1))
                .build();

    }

    private List<Integer> getBpList(TePath tePath) {
        ArrayList<Integer> bpList = new ArrayList<>();
        List<Bitstring> bitstringList = tePath.getBitstring();
        for (Bitstring bitstring : bitstringList) {
            bpList.add(bitstring.getBitposition().getValue());
        }
        return bpList;


    }

    private Path buildPath(Integer lastBp) {

        ArrayList<Bitstring> bitstringArrayList = new ArrayList<>();
        bitstringArrayList.add(buildBitstring(BP1));
        bitstringArrayList.add(buildBitstring(BP2));
        bitstringArrayList.add(buildBitstring(BP3));
        bitstringArrayList.add(buildBitstring(lastBp));

        return new PathBuilder()
                .setBitstring(bitstringArrayList)
                .setPathId(PATH_ID)
                .setSubdomainId(SUB_DOMAIN_ID)
                .setBitstringlength(BSL)
                .setSi(SI)
                .build();

    }




    @Test
    public void testWriteTeBitstringAdd() throws Exception {
        buildMock();
        buildInstance();
        Path path = buildPath(BP4);
        List<Integer> bpListExpected = getBpList(path);
        bierTeBitstringWriter.writeBierTeBitstring(ConfigurationType.ADD, NODE_ID,
                path,result).checkedGet();
        assertTrue(result.isSuccessful());
        TePath tePathActucal = netconfDataOperator.read(dataBroker,
                bierTeBitstringWriter.getTePathIid(path));
        List<Integer> bpListActual = getBpList(tePathActucal);

        assertTrue(bpListActual.containsAll(bpListExpected));
        assertTrue(bpListExpected.containsAll(bpListActual));
    }

    @Test
    public void testWriteTeBitstringModify() throws Exception {
        buildMock();
        buildInstance();
        Path path = buildPath(BP4);


        bierTeBitstringWriter.writeBierTeBitstring(ConfigurationType.ADD, NODE_ID,
                path,result).checkedGet();
        assertTrue(result.isSuccessful());

        path = buildPath(BP5);
        TePath tePathExpected = path;
        bierTeBitstringWriter.writeBierTeBitstring(ConfigurationType.ADD, NODE_ID,
                tePathExpected,result).checkedGet();
        List<Integer> bpListExpected = getBpList(tePathExpected);

        TePath tePathActucal = netconfDataOperator.read(dataBroker,
                bierTeBitstringWriter.getTePathIid(tePathExpected));
        List<Integer> bpListActual = getBpList(tePathActucal);

        assertTrue(bpListActual.containsAll(bpListExpected));
        assertTrue(bpListExpected.containsAll(bpListActual));
    }

    @Test
    public void testWriteTeBitstringDelete() throws Exception {
        buildMock();
        buildInstance();
        Path path = buildPath(BP4);

        bierTeBitstringWriter.writeBierTeBitstring(ConfigurationType.ADD, NODE_ID,
                path,result).checkedGet();
        assertTrue(result.isSuccessful());

        bierTeBitstringWriter.writeBierTeBitstring(ConfigurationType.DELETE, NODE_ID,
                path,result).checkedGet();


        TePath tePathActucal = netconfDataOperator.read(dataBroker,
                bierTeBitstringWriter.getTePathIid(path));
        assertNull(tePathActucal);
    }

    @Test
    public void testWriteTeBitstring() throws Exception {
        buildMock();
        buildInstance();
        Path path = buildPath(BP4);
        ConfigurationResult writeResult = bierTeBitstringWriter.writeBierTeBitstring(ConfigurationType.ADD,
                NODE_ID, path);
        assertTrue(writeResult.isSuccessful());
    }


}