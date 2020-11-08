package com.baeldung;

public class GreetingPluginExtension {
	private String greeter = "Baeldung";
	private String message = "Message from Plugin!";

	public String getGreeter() {
		return this.greeter;
	}

	public String getMessage() {
		return this.message;
	}

	public void setGreeter(final String greeter) {
		this.greeter = greeter;
	}

	public void setMessage(final String message) {
		this.message = message;
	}
}