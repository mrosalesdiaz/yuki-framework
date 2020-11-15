package yuki.plugin.gen.querydefinitions;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.gradle.internal.impldep.com.google.common.io.ByteStreams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

import yuki.plugin.developmentserver.DbFunctionDefinition;
import yuki.plugin.gen.endpoints.YukiPluginExtension;

public class NewQueryDefinitionGeneratorTest {

	private static final String YUKI_GEN_QUERIES = "yuki/gen/queries";

	@Inject
	private NewQueryDefinitionGenerator newQueryDefinitionGenerator;

	@Before
	public void prepareDefaultInjector() {
		final var injector = Guice.createInjector(new AbstractModule() {

		});

		injector.injectMembers(this);
	}

	@Test
	public void Should_set_package_and_name_When_new_query_definition_is_generated() throws IOException {

		final var dbFunctionDefinition = Mockito.mock(DbFunctionDefinition.class);
		Mockito.doReturn("Chanchito")
				.when(dbFunctionDefinition)
				.getName();

		final var yukiExtensionParameters = Mockito.mock(YukiPluginExtension.class);

		final var returnValue = this.newQueryDefinitionGenerator
				.execute(NewQueryDefinitionGeneratorTest.YUKI_GEN_QUERIES, dbFunctionDefinition, yukiExtensionParameters);

		Assertions.assertThat(returnValue)
				.isNotNull();

		Assertions.assertThat(new String(ByteStreams.toByteArray(returnValue)))
				.contains("public interface Chanchito extends")
				.contains("package yuki.gen.queries;");

	}

	@Test
	public void Should_set_sql_string_with_function_execution_When_new_query_definition_without_parameter_is_generated()
			throws IOException {

		final var dbFunctionDefinition = Mockito.mock(DbFunctionDefinition.class);
		Mockito.doReturn("Chanchito")
				.when(dbFunctionDefinition)
				.getName();
		Mockito.doReturn("fn_chanchito_punto_com")
				.when(dbFunctionDefinition)
				.getFunctionName();

		final var yukiExtensionParameters = Mockito.mock(YukiPluginExtension.class);

		final var returnValue = this.newQueryDefinitionGenerator
				.execute(NewQueryDefinitionGeneratorTest.YUKI_GEN_QUERIES, dbFunctionDefinition, yukiExtensionParameters);

		Assertions.assertThat(returnValue)
				.isNotNull();

		Assertions.assertThat(new String(ByteStreams.toByteArray(returnValue)))
				.contains("sql = \" SELECT * FROM fn_chanchito_punto_com() \"");

	}

	@Test
	public void Should_set_sql_string_with_function_execution_When_new_query_definition_with_parameter_is_generated()
			throws IOException {

		final var dbFunctionDefinition = Mockito.mock(DbFunctionDefinition.class);
		Mockito.doReturn("Chanchito")
				.when(dbFunctionDefinition)
				.getName();
		Mockito.doReturn("fn_chanchito_punto_com")
				.when(dbFunctionDefinition)
				.getFunctionName();

		Mockito.doReturn(this.buildMapOfParameters("name=TEXT,name2=TEXT,name3=TEXT"))
				.when(dbFunctionDefinition)
				.getParameters();

		final var yukiExtensionParameters = Mockito.mock(YukiPluginExtension.class);

		final var returnValue = this.newQueryDefinitionGenerator
				.execute(NewQueryDefinitionGeneratorTest.YUKI_GEN_QUERIES, dbFunctionDefinition, yukiExtensionParameters);

		Assertions.assertThat(returnValue)
				.isNotNull();

		Assertions.assertThat(new String(ByteStreams.toByteArray(returnValue)))
				.contains("sql = \" SELECT * FROM fn_chanchito_punto_com( name := $1 ,name2 := $2 ,name3 := $3 ) \"");

	}

	@Test
	public void Should_create_functions_to_set_parameters_When_new_query_definition_with_parameter_text_is_generated()
			throws IOException {

		final var dbFunctionDefinition = Mockito.mock(DbFunctionDefinition.class);
		Mockito.doReturn("Chanchito")
				.when(dbFunctionDefinition)
				.getName();
		Mockito.doReturn("fn_chanchito_punto_com")
				.when(dbFunctionDefinition)
				.getFunctionName();

		Mockito.doReturn(this.buildMapOfParameters("name=TEXT,name2=TEXT,name3=TEXT"))
				.when(dbFunctionDefinition)
				.getParameters();

		final var yukiExtensionParameters = Mockito.mock(YukiPluginExtension.class);

		final var returnValue = this.newQueryDefinitionGenerator
				.execute(NewQueryDefinitionGeneratorTest.YUKI_GEN_QUERIES, dbFunctionDefinition, yukiExtensionParameters);

		Assertions.assertThat(returnValue)
				.isNotNull();

		Assertions.assertThat(new String(ByteStreams.toByteArray(returnValue)))
				.contains("void setName(String name);")
				.contains("void setName2(String name2);")
				.contains("void setName3(String name3);");

	}

	@Test
	public void Should_use_object_parameter_When_function_parameter_is_not_supported() throws IOException {

		final var dbFunctionDefinition = Mockito.mock(DbFunctionDefinition.class);
		Mockito.doReturn("Chanchito")
				.when(dbFunctionDefinition)
				.getName();
		Mockito.doReturn("fn_chanchito_punto_com")
				.when(dbFunctionDefinition)
				.getFunctionName();

		Mockito.doReturn(this.buildMapOfParameters("name=CHANCHITO,name2=TEXT,name3=TEXT"))
				.when(dbFunctionDefinition)
				.getParameters();

		final var yukiExtensionParameters = Mockito.mock(YukiPluginExtension.class);

		final var returnValue = this.newQueryDefinitionGenerator
				.execute(NewQueryDefinitionGeneratorTest.YUKI_GEN_QUERIES, dbFunctionDefinition, yukiExtensionParameters);

		Assertions.assertThat(returnValue)
				.isNotNull();

		Assertions.assertThat(new String(ByteStreams.toByteArray(returnValue)))
				.contains("void setName(Object name);")
				.contains("void setName2(String name2);")
				.contains("void setName3(String name3);");

	}

	private Map<String, Object> buildMapOfParameters(final String parameterLine) {
		return Stream.of(parameterLine.split(","))
				.map(e -> e.split("="))
				.collect(Collectors.toMap(e -> e[0], e -> e[1], (u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedHashMap<String, Object>::new));
	}

}
