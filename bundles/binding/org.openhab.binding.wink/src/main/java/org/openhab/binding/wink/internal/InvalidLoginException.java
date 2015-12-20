package org.openhab.binding.wink.internal;

/**
 * Exception type used when a login attempt fails against the wink API.
 * @author Dan Cunningham
 *
 */
public class InvalidLoginException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidLoginException(String message) {
		super(message);
	}
}
