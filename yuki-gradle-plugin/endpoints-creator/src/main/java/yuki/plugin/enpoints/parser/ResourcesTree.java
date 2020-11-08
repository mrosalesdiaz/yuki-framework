package yuki.plugin.enpoints.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

public class ResourcesTree {

	private JsonObject cachedStarUmlContent;
	private final List<ResourceNode> nodes = new ArrayList<>();
	private String pathToRootObject;

	public List<ResourceNode> getNodes() {
		return Collections.unmodifiableList(this.nodes);
	}

	public void loadStarUml(final Path starUmlFile, final String pathToRootObject) throws IOException {

		this.pathToRootObject = pathToRootObject;
		this.cachedStarUmlContent = this.parseToJsonObject(starUmlFile);

		this.processNodes(this.cachedStarUmlContent);
	}

	private JsonObject parseToJsonObject(final Path starUmlFile) throws IOException {
		return new JsonObject(Files.readString(starUmlFile));
	}

	private void processNodes(final JsonObject rootJsonObject) {
		this.nodes.clear();
		System.out.println(this.pathToRootObject);

		new ArrayList<ResourceNode>();

		final var allObject = new Stack<ResourceNode>();
		for (final JsonObject umlObject : JsonTraverseHelper.getUMLObjects(this.cachedStarUmlContent)) {

			final var node = new ResourceNode();
			node.name = umlObject.getString("name", "<noname>");
			node.method = this.getClassifier(umlObject);
			node.props.put("_id", umlObject.getString("_id", "<noname>"));

			allObject.add(node);
		}

		final var links = new Stack<JsonObject>();

		JsonTraverseHelper.getUMLLinks(this.cachedStarUmlContent).forEach(links::add);

		while (links.size() > 0) {
			final var link = links.pop();

			final var parent = allObject.stream()
					.filter(u -> Objects.equals(u.props.get("_id"), link.getJsonObject("_parent").getString("$ref")))
					.findFirst().get();
			final var children = allObject.stream().filter(u -> Objects.equals(u.props.get("_id"),
					link.getJsonObject("end2").getJsonObject("reference").getString("$ref"))).findFirst().get();

			System.out.println("Parent: " + parent.name);
			System.out.println("Link: " + link.getString("name"));
			System.out.println("Children: " + children.name);
			System.out.println("==========");

			parent.children.add(children);
			parent.paths.add(link.getString("name", "<nopath>"));

		}

		allObject.stream().filter(e -> Objects.equals(e.name, "RootContext")).forEach(this.nodes::add);
		this.populateCachedList(this.nodes.get(0), this.nodes::add);
		System.out.println("ResourcesTree.processNodes()");

	}

	private void populateCachedList(final ResourceNode resourceNode, final Consumer<ResourceNode> process) {
		for (final ResourceNode item : resourceNode.children) {
			process.accept(item);
			this.populateCachedList(item, process);
		}
	}

	private String getClassifier(final JsonObject umlObject) {
		final var classifier = umlObject.getValue("classifier");
		if (classifier == null) {
			return "<nomethod>";
		}
		if (classifier instanceof JsonObject) {
			return JsonTraverseHelper.getInterfaces(this.cachedStarUmlContent).stream()
					.filter(e -> Objects.equals(((JsonObject) classifier).getString("$ref"), e.getString("_id")))
					.map(e -> e.getString("name")).findFirst().orElse("<nomethod>");
		}
		return classifier.toString();
	}

	public String[][] getResourceList() {
		final var rootNode = this.nodes.get(0);

		final var stringBuilder = new StringBuilder();

		this.traverse(rootNode, stringBuilder, "");

		return Stream.of(stringBuilder.toString().split("\n")).map(l -> l.split("\\|")).collect(Collectors.toList())
				.toArray(new String[0][0]);
	}

	private void traverse(final ResourceNode rootNode, final StringBuilder stringBuilder, final String path) {

		for (var i = 0; i < rootNode.children.size(); i++) {
			final var node = rootNode.children.toArray(new ResourceNode[0])[i];

			this.traverse(node, stringBuilder, path + rootNode.paths.get(i));
		}
		if (!Objects.equals(rootNode.name, "RootContext")) {
			stringBuilder.append(String.format("%3$s|%1$s|%2$s\n", rootNode.name, rootNode.method, path));
		}
	}

}
