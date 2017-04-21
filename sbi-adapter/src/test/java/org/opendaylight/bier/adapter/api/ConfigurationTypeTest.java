/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.adapter.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.opendaylight.bier.adapter.api.ConfigurationType;


public class ConfigurationTypeTest {
    @Test
    public void testGetName() throws Exception {
        ConfigurationType type = ConfigurationType.ADD;
        assertEquals("add",type.getName());
        type = ConfigurationType.DELETE;
        assertEquals("delete",type.getName());
        type = ConfigurationType.MODIFY;
        assertEquals("modify",type.getName());
    }

    @Test
    public void testGetIntValue() throws Exception {
        ConfigurationType type = ConfigurationType.ADD;
        assertEquals(1,type.getIntValue());
        type = ConfigurationType.DELETE;
        assertEquals(3,type.getIntValue());
        type = ConfigurationType.MODIFY;
        assertEquals(2,type.getIntValue());
    }
}
