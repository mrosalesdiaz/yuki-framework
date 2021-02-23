package yuki.framework.dataaccess.utils;

/**
 * When error while processing data returned from data base
 *
 *
 * @author mrosalesdiaz
 *
 */
public class ProcessDataException extends Throwable {
	private static final long serialVersionUID = -6991106079182976309L;

	public ProcessDataException() {
		super();
	}

	public ProcessDataException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ProcessDataException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ProcessDataException(final String message) {
		super(message);
	}

	public ProcessDataException(final Throwable cause) {
		super(cause);
	}

}
