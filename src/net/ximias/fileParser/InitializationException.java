package net.ximias.fileParser;

/**
 * Tells the Initializer to skip initializing the current object and try again later
 * Throw when Initialization of an object is going to fail, but could be recovered in a later pass.
 * Ex. If an object references another not-yet-initialized object,
 * it can't initialize before that other object has been initialized first.
 */
public class InitializationException extends RuntimeException{
	public InitializationException(String message) {
		super(message);
	}
}
