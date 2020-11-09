package yuki.plugin.gen.endpoints;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import yuki.plugin.enpoints.parser.PluginHelper;

public class CreateClassesTasks {

	public void create(final String[][] strings, final Path folder) throws IOException {

		for (final String[] parameters : strings) {
			this.createEndpointClass(parameters[0], parameters[1], parameters[2], folder);
		}

	}

	private void createEndpointClass(final String path, final String name, final String method, final Path folder)
			throws IOException {

		final var endPointClass = StaticJavaParser.parse(PluginHelper.getJavaTemplateForResources());

		endPointClass.findFirst(ClassOrInterfaceDeclaration.class).get().setName(name);
		endPointClass.findFirst(ClassOrInterfaceDeclaration.class).get().findAll(NormalAnnotationExpr.class).get(0)
				.findAll(MemberValuePair.class).forEach(e -> {
					if (e.getNameAsString().equals("method")) {
						e.getValue().asFieldAccessExpr().setName(method);
					}

					if (e.getNameAsString().equals("path")) {
						e.getValue().asStringLiteralExpr().setValue(path);
					}
				});

		final var outputFile = folder.resolve(String.format("%s.java", name));
		outputFile.getParent().toFile().mkdirs();
		Files.copy(new ByteArrayInputStream(endPointClass.toString().getBytes("utf-8")), outputFile,
				StandardCopyOption.REPLACE_EXISTING);

		System.out.println();
	}

	public static void main(final String[] args) throws IOException {
		new CreateClassesTasks().createEndpointClass("/chanchito", "Chanchito", "GET",
				Paths.get("/Volumes/sdcard/yuki/yuki-gradle-plugin/endpoints-creator/files/"));
	}
}
