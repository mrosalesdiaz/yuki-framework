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
		final QueryProxyInvoker proxyHandler = this.injector.getInstance(QueryProxyInvoker.class);

		proxyHandler.configure(queryDefinitionMetadata.sql(), new HashMap<>());

		return queryClass
				.cast(Proxy.newProxyInstance(queryClass.getClassLoader(), new Class<?>[] { queryClass }, proxyHandler));

	}

}
