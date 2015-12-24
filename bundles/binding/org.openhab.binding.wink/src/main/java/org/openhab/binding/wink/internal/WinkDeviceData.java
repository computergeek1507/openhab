/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.internal;

import java.io.IOException;
import java.util.HashMap;

import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class parses the JSON data and creates a HashMap of LightDevices
 * Openers
 * <ul>
 * <li>lightDevices: HashMap of Light Devices</li>
 * </ul>
 * 
 * @author Scott Hanson
 * @since 1.8.0
 */

public class WinkDeviceData {
	static final Logger logger = LoggerFactory.getLogger(WinkDeviceData.class);

	HashMap<String, LightDevice> lightDevices = new HashMap<String, LightDevice>();

	/**
	 * Constructor of the WinkDeviceData.
	 * 
	 * @param rootNode
	 *            The Json root node as it has been returned wink website.
	 */
	public WinkDeviceData(JsonNode rootNode) throws IOException {
		if (rootNode.has("data")) {
			JsonNode node = rootNode.get("data");
			if (node.isArray()) {
				logger.info("Wink Devices:");

				int arraysize = node.size();
				for (int i = 0; i < arraysize; i++) {
					JsonNode devicenode = node.get(i);
					if (devicenode.has("light_bulb_id")) {
						int deviceId = devicenode.get("light_bulb_id").asInt();
						String deviceName = devicenode.get("name").asText();
						String radioType = devicenode.get("radio_type")
								.asText();
						this.lightDevices.put(deviceName, new LightDevice(
								devicenode));
						logger.info("light_bulb_id: "
								+ Integer.toString(deviceId) + " DeviceName: "
								+ deviceName + " RadioType: " + radioType);
					} else if (devicenode.has("binary_switch_id")) {
						int deviceId = devicenode.get("binary_switch_id")
								.asInt();
						String deviceName = devicenode.get("name").asText();
						String radioType = devicenode.get("radio_type")
								.asText();
						this.lightDevices.put(deviceName, new LightDevice(
								devicenode));
						logger.info("binary_switch_id: "
								+ Integer.toString(deviceId) + " DeviceName: "
								+ deviceName + " RadioType: " + radioType);
					} else if (devicenode.has("lock_id")) {
						int deviceId = devicenode.get("lock_id")
								.asInt();
						String deviceName = devicenode.get("name").asText();
						this.lightDevices.put(deviceName, new LightDevice(
								devicenode));
						logger.info("binary_switch_id: "
								+ Integer.toString(deviceId) + " DeviceName: "
								+ deviceName);
					}
				}
			}
		}
	}

	public HashMap<String, LightDevice> getLightDevices() {
		return this.lightDevices;
	}

	public LightDevice getLightDevice(String name) {
		if (this.lightDevices.containsKey(name)) {
			return this.lightDevices.get(name);
		}
		return null;
	}
}

