/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.pilight.internal;

import org.openhab.binding.pilight.internal.communication.Status;

/**
 * Callback interface to signal any listeners that an update was received from pilight
 *
 * @author Jeroen Idserda
 * @since 1.0
 */
public interface IPilightMessageReceivedCallback {

    /**
     * Update for a device received.
     * 
     * @param connection The connection to pilight that received the update
     * @param status Object containing list of devices that were updated and their current state
     */
    public void messageReceived(PilightConnection connection, Status status);

}
