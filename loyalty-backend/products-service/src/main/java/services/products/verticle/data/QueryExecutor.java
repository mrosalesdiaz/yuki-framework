package services.products.verticle.data;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.google.inject.Injector;

import yuki.framework.dataaccess.QueryDefinitionMetadata;

public class QueryExecutor {

	@Inject
	private Injector injector;

	public <T> T execute(final Class<T> queryClass) {

		final var query = queryClass.getAnnotation(QueryDefinitionMetadata.class).sql();
		final Class<?> returnType = queryClass.getAnnotation(QueryDefinitionMetadata.class).returnType();

		final Map<String, Object> parameters = new HashMap<>();

		final var proxyHandler = this.injector.getInstance(QueryInvocator.class);
		proxyHandler.configure(query, returnType, parameters);

		final var instance = Proxy.newProxyInstance(queryClass.getClassLoader(), new Class<?>[] { queryClass },
				proxyHandler);

		this.injector.injectMembers(instance);

		return (T) instance;

	}

}
