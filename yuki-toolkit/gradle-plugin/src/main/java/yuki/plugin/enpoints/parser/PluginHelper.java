package yuki.plugin.enpoints.parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.gradle.internal.impldep.org.apache.commons.io.IOUtils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public final class PluginHelper {

	private PluginHelper() {
	}

	public static List<JsonObject> getUMLLinks(final JsonObject rootJsonObject) {

		final List<JsonObject> returnList = new ArrayList<>();

		PluginHelper.traverse(rootJsonObject, (o) -> {
			PluginHelper.isObject(o, n -> n.getString("_type", "").equals("UMLLink")).accept(returnList::add);
		});

		return returnList;

	}

	public static List<JsonObject> getUMLObjects(final JsonObject rootJsonObject) {

		final List<JsonObject> returnList = new ArrayList<>();

		PluginHelper.traverse(rootJsonObject, (o) -> {
			PluginHelper.isObject(o, n -> n.getString("_type", "").equals("UMLObject")).accept(returnList::add);
		});

		return returnList;

	}

	private static Consumer<Consumer<JsonObject>> isObject(final Object node, final Predicate<JsonObject> predicate) {
		return (n) -> {
			if ((node instanceof JsonObject) && predicate.test((JsonObject) node)) {
				n.accept((JsonObject) node);
			}
		};
	}

	public static void traverse(final JsonArray node, final Consumer<Object> fn) {
		node.forEach(e -> {
			fn.accept(e);
			if (e == null) {
				return;
			} else if (e instanceof JsonObject) {
				PluginHelper.traverse((JsonObject) e, fn);
			} else if (e instanceof JsonArray) {
				PluginHelper.traverse((JsonArray) e, fn);
			}
		});
	}

	private static void traverse(final JsonObject node, final Consumer<Object> fn) {
		node.forEach(e -> {
			fn.accept(e.getValue());
			if (e.getValue() == null) {
				return;
			} else if (e.getValue() instanceof JsonObject) {
				PluginHelper.traverse((JsonObject) e.getValue(), fn);
			} else if (e.getValue() instanceof JsonArray) {
				PluginHelper.traverse((JsonArray) e.getValue(), fn);
			}
		});

	}

	public static List<JsonObject> getInterfaces(final JsonObject rootJsonObject) {
		final List<JsonObject> returnList = new ArrayList<>();

		PluginHelper.traverse(rootJsonObject, (o) -> {
			PluginHelper.isObject(o, n -> n.getString("_type", "").equals("UMLInterface"))
					.accept(returnList::add);
		});

		return returnList;
	}

	public static String getJavaTemplateForResources() throws IOException {
		return IOUtils.resourceToString("/java-template-resource.txt", Charset.forName("utf-8"),
				PluginHelper.class.getClassLoader());
	}

}
