package yuki.plugin.developmentserver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.GradleException;

import feign.Feign;
import feign.codec.StringDecoder;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import yuki.plugin.gen.endpoints.YukiPluginExtension;
import yuki.plugin.gen.queryclasses.DbFunctions;

public class DevelopmentService {

	public List<DbFunctionDefinition> getDbFunctionDefinitions(final YukiPluginExtension parameters) {
		if (!parameters.getSchema()
				.isPresent()) {
			throw new GradleException("Parameter schema is missed");
		}

		if (!parameters.getServiceUrl()
				.isPresent()) {
			throw new GradleException("Parameter serverUrl is missed");
		}

		final DbFunctions dbFunctions = Feign.builder()
				.decoder(new StringDecoder())
				.target(DbFunctions.class, parameters.getServiceUrl()
						.get());

		final var functions = new JsonArray(dbFunctions.functions(parameters.getSchema()
				.get()));

		return functions.stream()
				.map(JsonObject.class::cast)
				.map(this::parseJsonObjectToDbFunctionDefinition)
				.collect(Collectors.toList());
	}

	private DbFunctionDefinition parseJsonObjectToDbFunctionDefinition(final JsonObject jsonObject) {
		final var instance = new DbFunctionDefinition();

		instance.setName(jsonObject.getString("name"));
		// TODO: pending to implement to read the list values for parameters and return
		instance.setParameters(new LinkedHashMap<String, String>());
		instance.setReturnParameters(new LinkedHashMap<String, String>());

		return instance;
	}

}
