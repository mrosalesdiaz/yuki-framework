package yuki.framework.dataaccess.utils;

/**
 * Exception for errors trying to set parameter value
 *
 * @author mrosalesdiaz
 *
 */
public class ParameterValueException extends RuntimeException {

	private static final long serialVersionUID = 8764172439148619558L;

	public ParameterValueException() {
		super();
	}

	public ParameterValueException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ParameterValueException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ParameterValueException(final String message) {
		super(message);
	}

	public ParameterValueException(final Throwable cause) {
		super(cause);
	}

}
