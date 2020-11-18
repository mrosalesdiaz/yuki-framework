package yuki.plugin.gen.querydefinitions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import yuki.plugin.developmentserver.EndpointDefinition;
import yuki.plugin.enpoints.parser.PluginHelper;

public class EndpointDefinitionsCreator {

	public void createEndpoint(final EndpointDefinition endpoint, final Path folder)
			throws UnsupportedEncodingException, IOException {
		final var endPointClass = StaticJavaParser.parse(PluginHelper.getJavaTemplateForResources());

		endPointClass.findFirst(ClassOrInterfaceDeclaration.class)
				.get()
				.setName(endpoint.getClassName());
		endPointClass.findFirst(ClassOrInterfaceDeclaration.class)
				.get()
				.findAll(NormalAnnotationExpr.class)
				.get(0)
				.findAll(MemberValuePair.class)
				.forEach(e -> {
					if (e.getNameAsString()
							.equals("method")) {
						e.getValue()
								.asFieldAccessExpr()
								.setName(endpoint.getMethod());
					}

					if (e.getNameAsString()
							.equals("path")) {
						e.getValue()
								.asStringLiteralExpr()
								.setValue(endpoint.getPath());
					}
				});

		final var outputFile = folder.resolve(String.format("%s.java", endpoint.getClassName()));
		outputFile.getParent()
				.toFile()
				.mkdirs();
		Files.copy(new ByteArrayInputStream(endPointClass.toString()
				.getBytes("utf-8")), outputFile, StandardCopyOption.REPLACE_EXISTING);
	}
}
