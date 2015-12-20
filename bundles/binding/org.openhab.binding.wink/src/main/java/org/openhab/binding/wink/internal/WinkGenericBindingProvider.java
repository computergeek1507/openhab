/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.internal;

import java.util.ArrayList;
import java.util.List;



import org.openhab.binding.wink.internal.WinkBindingConfig;
import org.openhab.binding.wink.internal.WinkBindingConfig.BindingType;
import org.openhab.binding.wink.WinkBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Scott Hanson
 * @since 1.8.0
 */
public class WinkGenericBindingProvider extends AbstractGenericBindingProvider implements WinkBindingProvider
{
	static final Logger logger = LoggerFactory.getLogger(WinkGenericBindingProvider.class);
	
	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() 
	{
		return "wink";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException
	{
		if (!(item instanceof SwitchItem || item instanceof DimmerItem))
		{
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException
	{
		super.processBindingConfiguration(context, item, bindingConfig);
		try 
		{
			if (bindingConfig != null)
			{
				String[] configParts = bindingConfig.split(":");

				if (item instanceof DimmerItem)
				{
					BindingConfig winkBindingConfig = (BindingConfig) new WinkBindingConfig(
						configParts[0], configParts[1], configParts.length < 3 ? null : configParts[2]);
					addBindingConfig(item, winkBindingConfig);
				} 
				else if (item instanceof SwitchItem)
				{
					BindingConfig winkBindingConfig = (BindingConfig) new WinkBindingConfig(
							configParts[0], configParts[1]);
					addBindingConfig(item, winkBindingConfig);
				}

			}
			else
			{
				logger.warn("bindingConfig is NULL (item=" + item
						+ ") -> processing bindingConfig aborted!");
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			logger.warn("bindingConfig is invalid (item=" + item
					+ ") -> processing bindingConfig aborted!");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public WinkBindingConfig getItemConfig(String itemName) 
	{
		return (WinkBindingConfig) bindingConfigs.get(itemName);
	}
	
	public List<String> getInBindingItemNames()
	{
		List<String> inBindings = new ArrayList<String>();
		for (String itemName : bindingConfigs.keySet())		
			inBindings.add(itemName);		
		return inBindings;
	}
}