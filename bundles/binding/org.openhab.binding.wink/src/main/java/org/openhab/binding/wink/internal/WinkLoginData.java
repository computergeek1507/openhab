package org.openhab.binding.wink.internal;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.openhab.binding.wink.internal.InvalidDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinkLoginData
{
	static final Logger logger = LoggerFactory.getLogger(WinkLoginData.class);

	String accessToken;
	String refreshToken;
	
	/**
	 * Constructor of the LoginData.
	 * 
	 * @param loginData
	 *            The Json string as it has been returned Wink website.
	 */
	public WinkLoginData(JsonNode root) throws IOException, InvalidDataException {
		ObjectMapper mapper = new ObjectMapper();
		//JsonNode rootNode = mapper.readTree(loginData);
		//Map<String, Object> treeData = mapper.readValue(rootNode, Map.class);
		JsonNode dataNode = root.get("data");
		this.accessToken = dataNode.get("access_token").asText();
		this.refreshToken = dataNode.get("refresh_token").asText();
		logger.debug("Wink accessToken: " + this.accessToken);
		logger.debug("Wink refreshToken: " + this.refreshToken);
	}

	/**
	 * @return Login AccessToken
	 */
	public String getAccessToken()
	{
		return this.accessToken;
	}
	
	/**
	 * @return Login RefreshToken
	 */
	public String getRefreshToken()
	{
		return this.refreshToken;
	}
	
	
}
