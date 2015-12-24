/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.internal;

import java.util.List;

import org.openhab.core.binding.BindingConfig;
import org.openhab.core.types.State;

/**
 * This is a helper class holding binding specific configuration details
 * 
 * @author Scott Hanson
 * @since 1.8.0
 */
public class WinkBindingConfig implements BindingConfig
{
	/**
	 * The name under used by the wink app.
	 */
	String deviceName;	
	String parameter;	
	List<Class<? extends State>> acceptedDataTypes;	
}
