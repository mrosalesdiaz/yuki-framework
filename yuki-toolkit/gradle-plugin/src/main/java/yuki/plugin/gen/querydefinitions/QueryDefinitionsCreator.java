package yuki.plugin.gen.querydefinitions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.inject.Inject;

import com.google.common.io.ByteStreams;

import yuki.plugin.YukiPluginExtension;
import yuki.plugin.gen.dtos.DbFunctionDefinition;

public class QueryDefinitionsCreator {
	private static final String YUKI_GEN_QUERIES = "yuki/functions";
	@Inject
	private NewQueryDefinitionGenerator newQueryDefinitionGenerator;

	public void updateJavaClassDefinition(final DbFunctionDefinition f, final YukiPluginExtension parameters,
			final File genYukiFolder) throws IOException {

		final File finalJavaFilePath = genYukiFolder.toPath()
				.resolve(QueryDefinitionsCreator.YUKI_GEN_QUERIES)
				.resolve(JavaClassHelper.getJavaFileName(f))
				.toFile();

		this.prepareFolderStructure(genYukiFolder, QueryDefinitionsCreator.YUKI_GEN_QUERIES);

		try (InputStream resultInputStream = this.newQueryDefinitionGenerator
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
