package yuki.plugin.gen.querydefinitions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.gradle.api.GradleException;

import com.google.common.io.ByteStreams;

import yuki.plugin.developmentserver.DbFunctionDefinition;
import yuki.plugin.gen.endpoints.YukiPluginExtension;

public class QueryDefinitionsCreator {
	private static final String YUKI_GEN_QUERIES = "yuki/gen/queries";
	@Inject
	private NewQueryDefinitionGenerator newQueryDefinitionGenerator;

	public void updateJavaClassDefinition(final DbFunctionDefinition f, final YukiPluginExtension parameters,
			final File genYukiFolder) throws IOException {
		if (!parameters.getOutput()
				.isPresent()) {
			throw new GradleException("The output parameter is missed");
		}
		final File finalJavaFilePath = genYukiFolder.toPath()
				.resolve(QueryDefinitionsCreator.YUKI_GEN_QUERIES)
				.toFile();

		this.prepareFolderStructure(genYukiFolder, QueryDefinitionsCreator.YUKI_GEN_QUERIES);

		try (var resultInputStream = this.newQueryDefinitionGenerator
				.execute(QueryDefinitionsCreator.YUKI_GEN_QUERIES, f, parameters)) {
			try (FileOutputStream javaFileOutputStream = new FileOutputStream(finalJavaFilePath)) {
				ByteStreams.copy(resultInputStream, javaFileOutputStream);
			}
		}

	}

	private void prepareFolderStructure(final File genYukiFolder, final String yukiGenQueriesRealtiveFolder) {
		Paths.get(genYukiFolder.getAbsolutePath(), yukiGenQueriesRealtiveFolder)
				.toFile()
				.mkdirs();

	}

}
