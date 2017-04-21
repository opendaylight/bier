/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.adapter.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.bier.adapter.api.ConfigurationResult;




public class ConfigurationResultTest {

    private ConfigurationResult configurationResult =
            new ConfigurationResult(ConfigurationResult.Result.FAILED,ConfigurationResult.READ_ROUTING_FAIL);

    @Test
    public void testSetCfgResult() throws Exception {
        configurationResult.setCfgResult(ConfigurationResult.Result.SUCCESSFUL);
        assertTrue(configurationResult.isSuccessful());
    }

    @Test
    public void testSetFailureReason() throws Exception {
        configurationResult.setFailureReason(ConfigurationResult.EGRESS_INFO_NULL);
        assertEquals(ConfigurationResult.EGRESS_INFO_NULL,configurationResult.getFailureReason());
    }

    @Test
    public void testIsSuccessful() throws Exception {
        assertFalse(configurationResult.isSuccessful());
    }

    @Test
    public void testGetFailureReason() throws Exception {
        configurationResult.setFailureReason(ConfigurationResult.IPV4_BSL_INVALID);
        assertEquals(ConfigurationResult.IPV4_BSL_INVALID,configurationResult.getFailureReason());
    }
}
