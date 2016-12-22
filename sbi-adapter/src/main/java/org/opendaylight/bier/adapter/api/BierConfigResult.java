/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.adapter.api;

public class BierConfigResult {
    public  enum ConfigurationResult {
        SUCCESSFUL,
        FAILED;
        ConfigurationResult(){
        }
    }

    public static final String MOUNT_SERVICE_NULL = "Netconf mount service unavailable";
    public static final String MOUNT_POINT_FAILUE = "No netconf connetion to the device";
    public static final String NETCONF_LOCK_FAILUE = "Device is busy,try again later";
    public static final String NETCONF_EDIT_FAILUE = "Netconf edit config failed with error message:";
    public static final String READ_ROUTING_FAIL = "Netconf read routing node fail";
    public static final String SUBDOMAINID_NULL = "Subdomain without subdomain id ,node id:";
    public static final String IPV4_BSL_NULL = "Subdomain's ipv4 mpls without bsl ,node id:";
    public static final String IPV4_BSL_INVALID = "Subdomain's ipv4 mpls with invalid bsl ,node id:";

    private ConfigurationResult cfgResult;
    private String failureReason;

    public BierConfigResult(ConfigurationResult cfgResult, String failureReason) {
        this.cfgResult = cfgResult;
        this.failureReason = failureReason;
    }

    public BierConfigResult(ConfigurationResult cfgResult) {
        this.cfgResult = cfgResult;
        this.failureReason = null;
    }

    public void setCfgResult(ConfigurationResult cfgResult) {
        this.cfgResult = cfgResult;
    }

    public void setFailureReason(String failureReason) {

        this.failureReason = failureReason;
    }


    public boolean isSuccessful() {
        return (cfgResult == ConfigurationResult.SUCCESSFUL);
    }

    public String getFailureReason() {
        return failureReason;
    }


}
