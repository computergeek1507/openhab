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
import java.util.Map;

import org.openhab.binding.wink.internal.InvalidLoginException;
import org.openhab.binding.wink.internal.WinkData;
import org.openhab.binding.wink.WinkBindingProvider;
import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement this class if you are going create an actively polling service like
 * querying a Website/Device.
 * 
 * @author Scott Hanson
 * @since 1.8.0
 */
public class WinkBinding extends AbstractActiveBinding<WinkBindingProvider> {
	private static final Logger logger = LoggerFactory
			.getLogger(WinkBinding.class);

	/**
	 * The BundleContext. This is only valid when the bundle is ACTIVE. It is
	 * set in the activate() method and must not be accessed anymore once the
	 * deactivate() method was called or before activate() was called.
	 */
	private BundleContext bundleContext;

	/**
	 * the refresh interval which is used to poll values from the wink server
	 * (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;

	/**
	 * The WinkData. This object stores the connection data and makes API
	 * requests
	 */
	private WinkData winkOnlineData = null;

	/**
	 * The WinkDeviceData. This object stores the garage door opener status
	 */
	// private WinkDeviceData deviceStatus = null;

	/**
	 * If our login credentials are invalid then we will stop api requests until
	 * our configuration is changed
	 */
	private boolean invalidCredentials;

	public WinkBinding() {

	}

	/**
	 * Called by the SCR to activate the component with its configuration read
	 * from CAS
	 * 
	 * @param bundleContext
	 *            BundleContext of the Bundle that defines this component
	 * @param configuration
	 *            Configuration properties for this component obtained from the
	 *            ConfigAdmin service
	 */
	public void activate(final BundleContext bundleContext,
			final Map<String, Object> configuration) {
		this.bundleContext = bundleContext;
		modified(configuration);
	}

	/**
	 * Called by the SCR when the configuration of a binding has been changed
	 * through the ConfigAdmin service.
	 * 
	 * @param configuration
	 *            Updated configuration properties
	 */
	public void modified(final Map<String, Object> configuration) {
		// to override the default refresh interval one has to add a
		// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
		String refreshIntervalString = (String) configuration.get("refresh");
		if (StringUtils.isNotBlank(refreshIntervalString))
			refreshInterval = Long.parseLong(refreshIntervalString);

		String usernameString = (String) configuration.get("username");
		String passwordString = (String) configuration.get("password");

		String clientId = (String) configuration.get("clientId");
		if (StringUtils.isBlank(clientId)) {
			clientId = WinkData.DEFAULT_CLIENT_ID;
		}

		String clientSecret = (String) configuration.get("clientSecret");
		if (StringUtils.isBlank(clientSecret)) {
			clientSecret = WinkData.DEFAULT_CLIENT_SECRET;
		}

		int timeout = WinkData.DEFAUALT_TIMEOUT;
		String timeoutString = (String) configuration.get("timeout");
		if (StringUtils.isNotBlank(timeoutString)) {
			timeout = Integer.parseInt(timeoutString);
		}

		// read further config parameters here ...
		if (StringUtils.isNotBlank(usernameString)
				&& StringUtils.isNotBlank(passwordString)) {
			winkOnlineData = new WinkData(usernameString, passwordString,
					clientId, clientSecret, timeout);

			invalidCredentials = false;
			setProperlyConfigured(true);
		}
	}

	/**
	 * Called by the SCR to deactivate the component when either the
	 * configuration is removed or mandatory references are no longer satisfied
	 * or the component has simply been stopped.
	 * 
	 * @param reason
	 *            Reason code for the deactivation:<br>
	 *            <ul>
	 *            <li>0 – Unspecified
	 *            <li>1 – The component was disabled
	 *            <li>2 – A reference became unsatisfied
	 *            <li>3 – A configuration was changed
	 *            <li>4 – A configuration was deleted
	 *            <li>5 – The component was disposed
	 *            <li>6 – The bundle was stopped
	 *            </ul>
	 */
	public void deactivate(final int reason) {
		this.bundleContext = null;
		// deallocate resources here that are no longer needed and
		// should be reset when activating this binding again
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected String getName() {
		return "wink Refresh Service";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void execute() {
		// the frequently executed code (polling) goes here ...
		if (invalidCredentials || this.winkOnlineData == null) {
			logger.debug("Invalid Account Credentials");
			return;
		}
		try {
			WinkDeviceData deviceStatus = winkOnlineData.getWinkData();

			for (WinkBindingProvider provider : this.providers) {
				for (String winkItemName : provider.getInBindingItemNames()) {
					WinkBindingConfig deviceConfig = getConfigForItemName(winkItemName);

					if (deviceConfig != null) {
						if (deviceStatus.getLightDevices().containsKey(
								deviceConfig.getDeviceName())) {
							LightDevice lightBulb = deviceStatus
									.getLightDevices().get(
											deviceConfig.getDeviceName());
							if (deviceConfig.getType() == WinkBindingConfig.BindingType.switching) {
								if (lightBulb.hasBoolState(deviceConfig
										.getParameter())) {
									eventPublisher
											.postUpdate(
													winkItemName,
													lightBulb
															.getBoolState(deviceConfig
																	.getParameter()) ? OnOffType.ON
															: OnOffType.OFF);
								}
							} else if (deviceConfig.getType() == WinkBindingConfig.BindingType.brightness) {
								if (lightBulb.hasDoubleState(deviceConfig
										.getParameter())) {
									PercentType newPercent = new PercentType(
											(int) Math.round(lightBulb
													.getDoubleState(deviceConfig
															.getParameter()) * 100.0));
									eventPublisher.postUpdate(winkItemName,
											newPercent);
								}
							} else if (deviceConfig.getType() == WinkBindingConfig.BindingType.status) {
								if (lightBulb.hasTextProperty(deviceConfig
										.getParameter())) {
									eventPublisher
											.postUpdate(
													winkItemName,
													new StringType(
															lightBulb
																	.getTextProperty(deviceConfig
																			.getParameter())));

								}
							}
						}
					}
				}
			}
		} catch (InvalidLoginException e) {
			logger.error("Could not log in, please check your credentials.", e);
			invalidCredentials = true;
		} catch (IOException e) {
			logger.error("Could not connect to Wink service", e);
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void internalReceiveCommand(String itemName, Command command) {
		super.internalReceiveCommand(itemName, command);

		logger.debug("wink binding received command '" + command
				+ "' for item '" + itemName + "'");

		if (this.winkOnlineData != null) {
			computeCommandForItem(command, itemName);
		} else {
			logger.warn("Command '{}' for item '{}' not sent", command,
					itemName);
		}
	}

	/**
	 * Checks whether the command is value and if the deviceID exists then get
	 * status of Garage Door Opener and send command to change it's state
	 * opposite of its current state
	 * 
	 * @param command
	 *            The command from the openHAB bus.
	 * @param itemName
	 *            The name of the targeted item.
	 */
	private void computeCommandForItem(Command command, String itemName) {
		WinkBindingConfig deviceConfig = getConfigForItemName(itemName);
		if (invalidCredentials || deviceConfig == null) {
			return;
		}
		try {
			// only switch type is valid
			if (deviceConfig.getType() != WinkBindingConfig.BindingType.switching
					&& deviceConfig.getType() != WinkBindingConfig.BindingType.brightness) {
				return;
			}
			// only send command if switch
			if (command instanceof OnOffType) {
				WinkDeviceData deviceStatus = winkOnlineData.getWinkData();

				if (deviceStatus.getLightDevices().containsKey(
						deviceConfig.getDeviceName())) {
					LightDevice lightBulb = deviceStatus.getLightDevices().get(
							deviceConfig.getDeviceName());

					winkOnlineData.updateDeviceState(lightBulb, deviceConfig
							.getParameter(), Boolean
							.toString((((OnOffType) command)
									.equals(OnOffType.ON))));
					eventPublisher.postUpdate(itemName, (OnOffType) command);
					// Thread.sleep(1000);
					// get status again
					// this.execute();
				} else {
					logger.warn("no wink device found with this name: "
							+ deviceConfig.getDeviceName());
				}
			}
			if (command instanceof PercentType) {
				if (deviceConfig.getType().equals(
						WinkBindingConfig.BindingType.brightness)) {
					WinkDeviceData deviceStatus = winkOnlineData.getWinkData();
					if (deviceStatus.getLightDevices().containsKey(
							deviceConfig.getDeviceName())) {
						LightDevice lightBulb = deviceStatus.getLightDevices()
								.get(deviceConfig.getDeviceName());

						winkOnlineData
								.updateDeviceState(
										lightBulb,
										deviceConfig.getParameter(),
										Double.toString(((double) ((PercentType) command)
												.intValue()) / 100.0));
						eventPublisher.postUpdate(itemName,
								(PercentType) command);
					}
				}
			}
		} catch (InvalidLoginException e) {
			logger.error("Could not log in, please check your credentials.", e);
			invalidCredentials = true;
		} catch (IOException e) {
			logger.error("Could not connect to Wink service", e);
		}
	}

	private WinkBindingConfig getConfigForItemName(String itemName) {
		for (WinkBindingProvider provider : this.providers) {
			if (provider.getItemConfig(itemName) != null)
				return provider.getItemConfig(itemName);
		}
		return null;
	}

}
