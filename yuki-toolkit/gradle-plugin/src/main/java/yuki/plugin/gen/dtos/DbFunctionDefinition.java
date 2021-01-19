package yuki.plugin.gen.dtos;

import java.util.Map;

public class DbFunctionDefinition {

	private String name;

	private String functionName;
	
	private String schemaName;

	private Map<String, String> returnParameters;

	private Map<String, String> parameters;

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Map<String, String> getReturnParameters() {
		return this.returnParameters;
	}

	public void setReturnParameters(final Map<String, String> returnParameters) {
		this.returnParameters = returnParameters;
	}

	public Map<String, String> getParameters() {
		return this.parameters;
	}

	public void setParameters(final Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getFunctionName() {
		return this.functionName;
	}

	public void setFunctionName(final String functionName) {
		this.functionName = functionName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

}
