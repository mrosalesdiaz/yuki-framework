package yuki.plugin.gen.dtos;

public class EndpointDefinition {

	private String path;
	private String className;
	private String method;

	public String getPath() {
		return this.path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public String getClassName() {
		return this.className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(final String method) {
		this.method = method;
	}

}
