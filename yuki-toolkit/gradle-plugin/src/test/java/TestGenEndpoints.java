import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;

import yuki.plugin.YukiGenPlugin;

public class TestGenEndpoints {
	@Test
	public void greeterPluginAddsGreetingTaskToProject() {

		final var project = ProjectBuilder.builder().build();

		project.getRepositories().gradlePluginPortal();
		// file://Volumes/sdcard/yuki/local_publish

		project.getRepositories().mavenLocal();
		project.getPluginManager().apply("yuki.gen-endpoints");

		Assert.assertTrue(project.getTasks().getByName("genEndpoints") instanceof YukiGenPlugin);
	}
}
