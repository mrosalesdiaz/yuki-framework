package yuki.plugin;

import org.gradle.api.provider.Property;

public abstract class YukiPluginExtension {

	abstract public Property<String> getServiceUrl();

	abstract public Property<String> getSchema();

	abstract public Property<String> getEndpointsModel();

}
