/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.internal;

import org.openhab.binding.wink.internal.WinkLoginData;
import org.openhab.binding.wink.internal.WinkDeviceData;
import org.openhab.binding.wink.internal.InvalidLoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import static org.openhab.io.net.http.HttpUtil.executeUrl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * This Class handles the Chamberlain Wink http connection.
 * 
 * @method Login()
 * 
 *         <ul>
 *         <li>userName: Login Username</li>
 *         <li>password: Login Password</li>
 *         <li>accessToken: accessToken for API requests</li>
 *         <li>refreshToken: refreshToken for API requests</li>
 *         <li>WEBSITE: url of API</li>
 *         <li>clientId: clientId for API requests</li>
 *         <li>clientSecret: clientSecret for API requests</li>
 *         </ul>
 * 
 * @author Scott Hanson
 * @since 1.8.0
 */
public class WinkData {
	static final Logger logger = LoggerFactory.getLogger(WinkData.class);

	private static final String WEBSITE = "https://winkapi.quirky.com";
	public static final String DEFAULT_CLIENT_ID = "9156a2c35bc52e9977eda916506a7d16";
	public static final String DEFAULT_CLIENT_SECRET = "a06e5da1224afe4f244f39078f7502ba";
	public static final int DEFAUALT_TIMEOUT = 5000;

	private String username;
	private String password;
	private String clientId;
	private String clientSecret;
	private int timeout;

	private String accessToken;
	private String refreshToken;

	/**
	 * Constructor For Wink http connection
	 * 
	 * @param username
	 *            Wink UserName
	 * 
	 * @param password
	 *            Wink password
	 * 
	 * @param clientId
	 *            Wink Developer Client ID, defaults to DEFAULT_CLIENT_ID if
	 *            null
	 * 
	 * @param clientSecret
	 *            Wink Developer Client Secret, defaults to
	 *            DEFAULT_CLIENT_SECRET if null
	 * 
	 * @param timeout
	 *            HTTP timeout in milliseconds, defaults to DEFAUALT_TIMEOUT if
	 *            not > 0
	 */
	public WinkData(String username, String password, String clientId,
			String clientSecret, int timeout) {
		this.username = username;
		this.password = password;

		if (clientId != null) {
			this.clientId = clientId;
		} else {
			this.clientId = DEFAULT_CLIENT_ID;
		}

		if (clientSecret != null) {
			this.clientSecret = clientSecret;
		} else {
			this.clientSecret = DEFAULT_CLIENT_SECRET;
		}

		if (timeout > 0) {
			this.timeout = timeout;
		} else {
			this.timeout = DEFAUALT_TIMEOUT;
		}
	}

	/**
	 * Gets Wink Data in WinkData object format
	 */
	public WinkDeviceData getWinkData() throws InvalidLoginException,
			IOException {

		logger.debug("Retreiveing wink device data");
		if (null == this.accessToken)
			login();
		String url = String.format("%s/users/me/wink_devices", WEBSITE);
		Properties header = new Properties();
		header.put("Accept", "application/json");
		header.put("authorization", "Bearer " + this.accessToken);
		JsonNode data = request("GET", header, url, null, null, true);

		return new WinkDeviceData(data);
	}

	public LightDevice updateDeviceState(LightDevice device, String parareter,
			String value) throws InvalidLoginException, IOException {
		String url = String.format("%s/%s/%s", WEBSITE, device.getDeviceType(),
				device.getDeviceId());

		String message = String.format(
				"{\n    \"desired_state\":{\"%s\":%s}\n}", parareter, value);

		Properties header = new Properties();
		header.put("Accept", "application/json");
		header.put("authorization", "Bearer " + this.accessToken);

		JsonNode data = request("PUT", header, url, message,
				"application/json", true);
		LightDevice updateLight = new LightDevice(data);
		return updateLight;
	}

	/**
	 * Validates Username and Password then saved accessToken and refreshToken
	 * to variables throws if return code from API is not correct or connection
	 * fails
	 */
	private void login() throws InvalidLoginException, IOException {
		logger.debug("attempting to login");
		String url = String.format("%s/oauth2/token", WEBSITE);

		String message = String
				.format("{\"client_id\":\"%s\",\"client_secret\":\"%s\",\"username\":\"%s\",\"password\":\"%s\",\"grant_type\":\"password\"}",
						this.clientId, this.clientSecret, this.username,
						this.password);

		Properties header = new Properties();
		header.put("Accept", "application/json");

		JsonNode data = request("POST", header, url, message,
				"application/json", true);
		WinkLoginData login = new WinkLoginData(data);
		this.accessToken = login.getAccessToken();
		this.refreshToken = login.getRefreshToken();
	}

	/**
	 * Make a request to the server, optionally retry the call if there is a
	 * login issue. Will throw a InvalidLoginExcpetion if the account is
	 * invalid, locked or soon to be locked.
	 * 
	 * @param method
	 *            The Http Method Type (GET,PUT)
	 * @param url
	 *            The request URL
	 * @param payload
	 *            Payload string for put operations
	 * @param payloadType
	 *            Payload content type for put operations
	 * @param retry
	 *            Retry the attempt if our session key is not valid
	 * @return The JsonNode representing the response data
	 * @throws IOException
	 * @throws InvalidLoginException
	 */
	private synchronized JsonNode request(String method, Properties header,
			String url, String payload, String payloadType, boolean retry)
			throws IOException, InvalidLoginException {

		logger.debug("Requsting URL {}", url);

		String dataString = executeUrl(method, url, header,
				payload == null ? null : IOUtils.toInputStream(payload),
				payloadType, 5000);

		logger.debug("Received Wink JSON: {}", dataString);

		if (dataString == null) {
			throw new IOException("Null response from Wink server");
		}

		try {
			if (dataString.length() == 1)
				if (retry) {
					login();
					return request(method, header, url, payload, payloadType,
							false);
				}
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(dataString);

			return rootNode;
			// int returnCode = rootNode.get("ReturnCode").asInt();
			// logger.debug("wink ReturnCode: {}", returnCode);

			// WinkResponseCode rc = WinkResponseCode.fromCode(returnCode);
			/*
			 * switch (rc) { case OK: { return rootNode; } case ACCOUNT_INVALID:
			 * case ACCOUNT_NOT_FOUND: case ACCOUNT_LOCKED: case
			 * ACCOUNT_LOCKED_PENDING: // these are bad, we do not want to
			 * continue to log in and // lock an account throw new
			 * InvalidLoginException(rc.getDesc()); case LOGIN_ERROR: // Our
			 * session key has expired, request a new one if (retry) { login();
			 * return request(method, header, url, payload, payloadType, false);
			 * } // fall through to default default: throw new
			 * IOException("Request Failed: " + rc.getDesc()); }
			 */
		} catch (JsonProcessingException e) {
			throw new IOException("Could not parse response", e);
		}
	}
}