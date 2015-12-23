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

/**
 * This Class holds the Garage Door Opener Device data.
 * <ul>
 * <li>DeviceName: DeviceName from Wink App</li>
 * <li>DeviceType: Device Type.</li>
 * <li>DeviceName: Serial number of device I think</li>
 * <li>TypeName: Name That appears in myQ App</li>
 * <li></li>
 * </ul>
 * 
 * @author Scott Hanson
 * @since 1.8.0
 */
class LightDevice {
	private String Name;
	private int DeviceId;// light id
	private String RadioType;
	private String DeviceType;

	// private HashMap<String, String> DesiredStateList;

	// private HashMap<String, Integer> intProperties = new HashMap<String,
	// Integer>();
	private HashMap<String, String> textProperties = new HashMap<String, String>();

	// private HashMap<String, Integer> intStates = new HashMap<String,
	// Integer>();
	private HashMap<String, Boolean> boolStates = new HashMap<String, Boolean>();
	private HashMap<String, Double> doubleStates = new HashMap<String, Double>();

	public LightDevice(JsonNode node) {
		if (node.has("data")) {
			node = node.get("data");
		}

		this.Name = node.get("name").asText();
		if (node.has("light_bulb_id")) {
			this.DeviceId = node.get("light_bulb_id").asInt();
			this.DeviceType = "light_bulbs";
		} else if (node.has("binary_switch_id")) {
			this.DeviceId = node.get("binary_switch_id").asInt();
			this.DeviceType = "binary_switches";
		}
		if (node.has("radio_type"))
			this.RadioType = node.get("radio_type").asText();

		if (node.has("hub_id"))
			this.textProperties.put("hub_id", node.get("hub_id").asText());
		if (node.has("local_id"))
			this.textProperties.put("local_id", node.get("local_id").asText());
		if (node.has("device_manufacturer"))
			this.textProperties.put("device_manufacturer",
					node.get("device_manufacturer").asText());
		if (node.has("model_name"))
			this.textProperties.put("model_name", node.get("model_name")
					.asText());
		if (node.has("manufacturer_device_model"))
			this.textProperties.put("manufacturer_device_model",
					node.get("manufacturer_device_model").asText());
		// this.DeviceManufacturer = node.get("device_manufacturer").asText();
		// this.ModelName = node.get("model_name").asText();
		// this.ManufacturerDeviceModel =
		// node.get("manufacturer_device_model").asText();

		JsonNode valueNode = node.get("last_reading");
		if (valueNode.has("connection"))
			this.boolStates.put("connection", valueNode.get("connection")
					.asBoolean());
		if (valueNode.has("powered"))
			this.boolStates
					.put("powered", valueNode.get("powered").asBoolean());
		// this.Powered = valueNode.get("powered").asBoolean();
		if (valueNode.has("brightness"))
			this.doubleStates.put("brightness", valueNode.get("brightness")
					.asDouble());
		// this.Brightness = valueNode.get("brightness").asDouble();
	}

	public String getName() {
		return this.Name;
	}

	public int getDeviceId() {
		return this.DeviceId;
	}

	public String getDeviceType() {
		return this.DeviceType;
	}

	public String getRadioType() {
		return this.RadioType;
	}

	// public boolean hasIntProperty(String PropertyName)
	// {
	// return this.intProperties.containsKey(PropertyName);
	// }

	// public int getIntProperty(String PropertyName)
	// {
	// return this.intProperties.get(PropertyName);
	// }

	public boolean hasTextProperty(String PropertyName) {
		return this.textProperties.containsKey(PropertyName);
	}

	public String getTextProperty(String PropertyName) {
		return this.textProperties.get(PropertyName);
	}

	public boolean hasBoolState(String StateName) {
		return this.boolStates.containsKey(StateName);
	}

	public Boolean getBoolState(String StateName) {
		return this.boolStates.get(StateName);
	}

	public boolean hasDoubleState(String StateName) {
		return this.doubleStates.containsKey(StateName);
	}

	public Double getDoubleState(String StateName) {
		return this.doubleStates.get(StateName);
	}

	public String toString() {
		return this.Name;
	}
}
