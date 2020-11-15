package yuki.plugin.gen.querydefinitions;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import yuki.plugin.developmentserver.DbFunctionDefinition;

public final class JavaClassHelper {
	private JavaClassHelper() {
	}

	public static String getJavaFileName(final DbFunctionDefinition dbFunctionDefinition) {
		return String.format("%1$s.java", dbFunctionDefinition.getName());
	}

	public static String getJavaTemplateForQueryDefinition() throws IOException {
		return Resources.toString(Resources
				.getResource(JavaClassHelper.class, "/templates/java-template-query-definition.txt"), Charsets.UTF_8);
	}
}
