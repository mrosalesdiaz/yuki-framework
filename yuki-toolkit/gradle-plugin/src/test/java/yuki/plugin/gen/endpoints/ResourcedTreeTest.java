package yuki.plugin.gen.endpoints;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import yuki.plugin.enpoints.parser.ResourcesTree;

public class ResourcedTreeTest {
	private final Path starUmlFile = Paths
			.get("/Volumes/sdcard/yuki/yuki-gradle-plugin/endpoints-creator/files/uml-sample.mdj");

	@Test
	public void Should_return_a_simple_tree_When_passed_and_simple_model() throws IOException {
		final var pathToRootObject = "Rest Endpoint/RootContext";
		final var resourcesTree = new ResourcesTree();

		resourcesTree.loadStarUml(this.starUmlFile, pathToRootObject);

		Assert.assertEquals(10, resourcesTree.getNodes().size());
	}

	@Test
	public void Should_return_a_endpoints_and_paths_When_passed_and_simple_model() throws IOException {
		final var pathToRootObject = "Rest Endpoint/RootContext";
		final var resourcesTree = new ResourcesTree();

		resourcesTree.loadStarUml(this.starUmlFile, pathToRootObject);

		final var resourceList = resourcesTree.getResourceList();
		Assert.assertArrayEquals(new String[][] { { "/patients/:patientId", "GetPatient", "GET" } }, resourceList);

		Assert.assertEquals(10, resourcesTree.getNodes().size());
	}

}
