package yuki.plugin.developmentserver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.GradleException;

import feign.Feign;
import feign.codec.StringDecoder;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import yuki.plugin.YukiPluginExtension;
import yuki.plugin.gen.api.contracts.DbFunctions;
import yuki.plugin.gen.api.contracts.Endpoints;
import yuki.plugin.gen.dtos.DbFunctionDefinition;
import yuki.plugin.gen.dtos.EndpointDefinition;

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

		final JsonArray functions = new JsonArray(dbFunctions.functions(parameters.getSchema()
				.get()));

		return functions.stream()
				.map(JsonObject.class::cast)
				.map(this::parseJsonObjectToDbFunctionDefinition)
				.collect(Collectors.toList());
	}

	private DbFunctionDefinition parseJsonObjectToDbFunctionDefinition(final JsonObject jsonObject) {
		final DbFunctionDefinition instance = new DbFunctionDefinition();

		instance.setName(jsonObject.getString("className"));
		instance.setFunctionName(jsonObject.getString("functionName"));
		// TODO: pending to implement to read the list values for parameters and return
		instance.setParameters(new LinkedHashMap<String, String>());
		instance.setReturnParameters(new LinkedHashMap<String, String>());

		jsonObject.getJsonArray("functionArguments")
				.stream()
				.map(JsonObject.class::cast)
				.forEach(o -> {
					instance.getParameters()
							.put(o.getString("name"), o.getString("type"));
				});

		return instance;
	}

	private EndpointDefinition parseJsonObjectToEndpoint(final JsonObject jsonObject) {
		final EndpointDefinition instance = new EndpointDefinition();

		instance.setPath(jsonObject.getString("path"));
		instance.setClassName(jsonObject.getString("className"));
		instance.setMethod(jsonObject.getString("method"));

		return instance;
	}

	public List<EndpointDefinition> getEndpointsDefinitions(final YukiPluginExtension parameters) {

		if (!parameters.getServiceUrl()
				.isPresent()) {
			throw new GradleException("Parameter serverUrl is missed");
		}

		final Endpoints endpointsResponse = Feign.builder()
				.decoder(new StringDecoder())
				.target(Endpoints.class, parameters.getServiceUrl()
						.get());

		final JsonArray endpoints = new JsonArray(endpointsResponse.endpoints(parameters.getEndpointsModel()
				.getOrElse("")));

		return endpoints.stream()
				.map(JsonObject.class::cast)
				.map(this::parseJsonObjectToEndpoint)
				.collect(Collectors.toList());
	}

}
