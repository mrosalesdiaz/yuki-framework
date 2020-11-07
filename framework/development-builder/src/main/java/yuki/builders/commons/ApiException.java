package yuki.builders.commons;

public class ApiException extends RuntimeException {

	private static final long serialVersionUID = 901694703782847592L;

	public ApiException() {
		super();
	}

	public ApiException(final String message) {
		super(message);
	}

	public ApiException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ApiException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ApiException(final Throwable cause) {
		super(cause);
	}

}
