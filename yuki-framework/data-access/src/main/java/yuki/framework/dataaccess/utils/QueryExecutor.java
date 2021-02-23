package yuki.framework.dataaccess.utils;

import java.lang.reflect.Proxy;
import java.util.HashMap;

import javax.inject.Inject;

import com.google.inject.Injector;

import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;

public class QueryExecutor {

	@Inject
	private Injector injector;

	public <T> T create(final Class<T> queryClass) {
		final QueryDefinitionMetadata queryDefinitionMetadata = queryClass.getAnnotation(QueryDefinitionMetadata.class);
		final QueryProxyInvocator proxyHandler = this.injector.getInstance(QueryProxyInvocator.class);

		proxyHandler.configure(queryDefinitionMetadata.sql(), queryDefinitionMetadata.returnType(), new HashMap<>());

		return queryClass
				.cast(Proxy.newProxyInstance(queryClass.getClassLoader(), new Class<?>[] { queryClass }, proxyHandler));

	}

}
