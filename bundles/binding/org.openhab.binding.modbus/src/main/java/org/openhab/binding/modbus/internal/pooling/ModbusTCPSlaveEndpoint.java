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
package org.openhab.binding.modbus.internal.pooling;

import net.wimpi.modbus.net.ModbusSlaveConnection;

public class ModbusTCPSlaveEndpoint extends ModbusIPSlaveEndpoint {

    public ModbusTCPSlaveEndpoint(String address, int port) {
        super(address, port);
    }

    @Override
    public <R> R accept(ModbusSlaveEndpointVisitor<R> factory) {
        return factory.visit(this);
    }

    @Override
    public ModbusSlaveConnection create(ModbusSlaveConnectionFactory factory) {
        return accept(factory);
    }

}
