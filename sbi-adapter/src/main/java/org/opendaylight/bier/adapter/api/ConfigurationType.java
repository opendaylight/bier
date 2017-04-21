/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.adapter.api;

public enum ConfigurationType {

    ADD(1,"add"),
    MODIFY(2,"modify"),
    DELETE(3,"delete");

    java.lang.String name;
    int value;

    ConfigurationType(int value, java.lang.String name) {
        this.value = value;
        this.name = name;
    }

    public java.lang.String getName() {
        return name;
    }

    public int getIntValue() {
        return value;
    }
}


