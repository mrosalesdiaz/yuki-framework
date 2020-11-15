package yuki.plugin.gen.endpoints;

import org.gradle.api.provider.Property;

public abstract class YukiPluginExtension {

	abstract public Property<String> getServiceUrl();

	abstract public Property<String> getSchema();

	// TODO: evaluate to change to file for validation purpouses
	abstract public Property<String> getOutput();
}
