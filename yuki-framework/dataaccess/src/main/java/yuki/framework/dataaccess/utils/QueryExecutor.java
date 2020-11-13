package yuki.framework.dataaccess.utils;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.common.base.Stopwatch;
import com.google.inject.Injector;

import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;

public class QueryExecutor {

	@Inject
	private Injector injector;

	public <T> T create(final Class<T> queryClass) {
		var creationExecutor = Stopwatch.createStarted();
		final Map<String, Object> internalParameters = new HashMap<>();
		final var queryDefinitionMetadata = queryClass.getAnnotation(QueryDefinitionMetadata.class);

		System.out.println("Getting annotation: " + creationExecutor.elapsed(TimeUnit.MILLISECONDS));
		creationExecutor = Stopwatch.createStarted();
		final var proxyHandler = this.injector.getInstance(QueryProxyInvocator.class);
		proxyHandler.configure(queryDefinitionMetadata.sql(), queryDefinitionMetadata.returnType(), internalParameters);
		System.out.println("Initialize configuration: " + creationExecutor.elapsed(TimeUnit.MILLISECONDS));
		creationExecutor = Stopwatch.createStarted();
		final T instance = queryClass
				.cast(Proxy.newProxyInstance(queryClass.getClassLoader(), new Class<?>[] { queryClass }, proxyHandler));

		System.out.println("INjectec insatnce: " + creationExecutor.elapsed(TimeUnit.MILLISECONDS));
		creationExecutor = Stopwatch.createStarted();
		return instance;

	}

}
