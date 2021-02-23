package yuki.framework.dataaccess.utils;

/**
 * Any database related error
 *
 * @author mrosalesdiaz
 *
 */
public class DatabaseExecutionException extends Throwable {

	private static final long serialVersionUID = -3628837669918375867L;

	public DatabaseExecutionException() {
		super();
	}

	public DatabaseExecutionException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DatabaseExecutionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public DatabaseExecutionException(final String message) {
		super(message);
	}

	public DatabaseExecutionException(final Throwable cause) {
		super(cause);
	}

}
