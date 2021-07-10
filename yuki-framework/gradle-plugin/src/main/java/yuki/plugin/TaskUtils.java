package yuki.plugin;

import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;

import feign.Feign;
import feign.codec.StringDecoder;
import yuki.plugin.gen.api.contracts.YukiDevelopment;

public class TaskUtils {
	public static void checkDevelopmentServer(final String name, final Property<String> property) {
		if (!property.isPresent()) {
			throw new GradleException(String
					.format("Error executing task: %s. Configuration for URL is not present. check yuki.serviceUrl property.", name));
		}

		TaskUtils.checkHttpConnection(name, property.get());

	}

	public static void checkHttpConnection(final String taskName, final String stringUrl) {
		try {
			Feign.builder()
					.decoder(new StringDecoder())
					.target(YukiDevelopment.class, stringUrl)
					.checkStatus();
		} catch (final Exception e) {
			throw new GradleException(
					String.format("Development server(%2$s) is down for task: %1$s ", taskName, stringUrl), e);
		}
	}
}
