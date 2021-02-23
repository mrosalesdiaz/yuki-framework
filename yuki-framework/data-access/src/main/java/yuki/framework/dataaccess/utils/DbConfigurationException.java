package yuki.framework.dataaccess.utils;

/**
 * Thrown when any errors such as:<br>
 * - Wrong jdbc url<br>
 * - Connection database<br>
 * - Testing dummy query<br>
 *
 * @author mrosalesdiaz
 *
 */
public class DbConfigurationException extends RuntimeException {

	private static final long serialVersionUID = -1186332579427300490L;

	public DbConfigurationException() {
		super();
	}

	public DbConfigurationException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DbConfigurationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public DbConfigurationException(final String message) {
		super(message);
	}

	public DbConfigurationException(final Throwable cause) {
		super(cause);
	}

}
