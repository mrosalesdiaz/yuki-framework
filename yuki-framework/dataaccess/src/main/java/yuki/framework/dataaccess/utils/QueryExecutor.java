package yuki.framework.dataaccess.utils;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.google.inject.Injector;

import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;

public class QueryExecutor {

	@Inject
	private Injector injector;

	public <T> T create(final Class<T> queryClass) {

		final Map<String, Object> internalParameters = new HashMap<>();
		final var queryDefinitionMetadata = queryClass.getAnnotation(QueryDefinitionMetadata.class);

		final var proxyHandler = this.injector.getInstance(QueryProxyInvocator.class);
		proxyHandler.configure(queryDefinitionMetadata.sql(), queryDefinitionMetadata.returnType(), internalParameters);

		final T instance = queryClass
				.cast(Proxy.newProxyInstance(queryClass.getClassLoader(), new Class<?>[] { queryClass }, proxyHandler));

		this.injector.injectMembers(instance);

		return instance;

	}

}
