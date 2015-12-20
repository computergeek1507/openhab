/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.internal;

import org.openhab.core.binding.BindingConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a helper class holding binding specific configuration details
 * 
 * @author Scott Hanson
 * @since 1.8.0
 */
public class WinkBindingConfig implements BindingConfig
{
	static final Logger logger = LoggerFactory.getLogger(WinkBindingConfig.class);
	
	/**
	 * The binding type of the hue item.
	 * <ul>
	 * <li>Switch</li>
	 * <li>Brightness</li>
	 * <li>status</li>
	 * </ul>
	 */
	public enum BindingType
	{
		switching, brightness, status
	}
	
	/**
	 * The name under used by the wink app.
	 */
	private String deviceName;
	
	private String parameter;

	/**
	 * The binding type of the hue item.
	 */
	private final BindingType type;

	/**
	 * The optionally configurable step size that will be used when the bulb is
	 * dimmed up or down. Default is 25.
	 */
	private final int stepSize;
	
	/**
	 * On / Off Item State
	 */
	public OnOffType itemStateOnOffType;
	
	/**
	 * Percentage Item State
	 */
	public PercentType itemStatePercentType;
	
	public WinkBindingConfig(String name, String parameter, String stepSize)
			throws BindingConfigParseException
	{
		this.deviceName = name;
		this.parameter = parameter;

//		if (type != null)
//			this.type = parseBindingTypeConfigString(type);
//		else
			this.type = WinkBindingConfig.BindingType.brightness;		

		if (stepSize != null)
			this.stepSize = parseStepSizeConfigString(stepSize);
		else
			this.stepSize = 25;
	}
	
	public WinkBindingConfig(String name, String parameter)
			throws BindingConfigParseException
	{
		this.deviceName = name;
		this.parameter = parameter;

//		if (type != null)
//			this.type = parseBindingTypeConfigString(type);
//		else
			this.type = WinkBindingConfig.BindingType.switching;
			this.stepSize = 25;
	}
	
	/**
	 * Parses a step size string that has been found in the configuration.
	 * 
	 * @param configString
	 *            The step size as a string.
	 * @return The step size as an integer value.
	 * @throws BindingConfigParseException
	 */
	private int parseStepSizeConfigString(String configString)
			throws BindingConfigParseException 
	{
		try
		{
			return Integer.parseInt(configString);
		} catch (Exception e)
		{
			throw new BindingConfigParseException(
					"Error parsing step size.");
		}
	}
	

	

	/**
	 * @return The device Name that has been declared in the binding
	 *         configuration.
	 */
	public String getDeviceName()
	{
		return deviceName;
	}
	
	/**
	 * @return The Parameter that has been declared in the binding
	 *         configuration.
	 */
	public String getParameter()
	{
		return parameter;
	}

	/**
	 * @return The binding type as a {@link HueBindingConfig.BindingType} that
	 *         has been declared in the binding configuration.
	 */
	public BindingType getType()
	{
		return type;
	}

	/**
	 * @return The step size that has been declared in the binding
	 *         configuration. This is the amount of increase and decrease of
	 *         bulb values like hue or brightness.
	 */
	public int getStepSize()
	{
		return stepSize;
	}	
}
