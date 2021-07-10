package yuki.gradleplugin;

import org.gradle.api.provider.Property;

public abstract class YukiPluginExtension {

	abstract public Property<String> getSchema();

	abstract public Property<String> getJdbcUrl();

}
