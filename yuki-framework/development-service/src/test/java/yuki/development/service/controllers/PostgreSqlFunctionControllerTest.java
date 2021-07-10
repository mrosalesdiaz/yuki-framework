package yuki.development.service.controllers;

import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import yuki.development.service.dataaccess.Db;

@ExtendWith(MockitoExtension.class)
public class PostgreSqlFunctionControllerTest {

	@Spy
	private final Db db = Mockito.mock(Db.class);

	@Spy
	private final RowProcessor rowProcessor = Mockito.mock(RowProcessor.class);

	@InjectMocks
	private PostgreSqlFunctionController postgreSqlFunctionController;

	@Captor
	private ArgumentCaptor<Handler<AsyncResult<JsonArray>>> callbackCaptor;

	@Captor
	private ArgumentCaptor<Consumer<Row>> callbackCaptorForEach;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void Should_obtain_one_function_definition_When_it_called_with_schema_parameter() throws Throwable {

		final PgPool pgClient = Mockito.mock(PgPool.class);
		Mockito.doReturn(pgClient)
				.when(this.db)
				.getConnection();

		final Query<RowSet<Row>> returnOfMethodQuery = Mockito.spy(Query.class);
		Mockito.doReturn(returnOfMethodQuery)
				.when(pgClient)
				.query(ArgumentMatchers.any());

		final RoutingContext parameterRc = Mockito.mock(RoutingContext.class);
		final HttpServerResponse responseObject = Mockito.mock(HttpServerResponse.class);

		Mockito.doReturn(responseObject)
				.when(parameterRc)
				.response();

		Mockito.doReturn(responseObject)
				.when(responseObject)
				.setStatusCode(ArgumentMatchers.anyInt());

		Mockito.doReturn(responseObject)
				.when(responseObject)
				.putHeader(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

		this.postgreSqlFunctionController.getPostgreSqlFunctions(parameterRc);

		Mockito.verify(returnOfMethodQuery)
				.execute(ArgumentMatchers.any());

		Mockito.verify(this.rowProcessor)
				.asJsonArray(this.callbackCaptor.capture());

		System.out.println(this.callbackCaptor.getValue());

		final Handler<AsyncResult<JsonArray>> callbackForExecute = this.callbackCaptor.getValue();

		final AsyncResult<JsonArray> asynResult = Mockito.mock(AsyncResult.class);

		Mockito.doReturn(new JsonArray().add(new JsonObject().put("name", "chanchito")))
				.when(asynResult)
				.result();

		callbackForExecute.handle(asynResult);

		Mockito.verify(responseObject)
				.end(ArgumentMatchers.eq("[{\"name\":\"chanchito\"}]"));
		//

	}

	public static <T> void mockIterable(final Iterable<T> iterable, final T... values) {
		final RowIterator<T> mockIterator = Mockito.mock(RowIterator.class);
		Mockito.when(iterable.iterator())
				.thenReturn(mockIterator);

		if (values.length == 0) {
			Mockito.when(mockIterator.hasNext())
					.thenReturn(false);
			return;
		} else if (values.length == 1) {
			Mockito.when(mockIterator.hasNext())
					.thenReturn(true, false);
			Mockito.when(mockIterator.next())
					.thenReturn(values[0]);
		} else {
			// build boolean array for hasNext()
			final Boolean[] hasNextResponses = new Boolean[values.length];
			for (int i = 0; i < (hasNextResponses.length - 1); i++) {
				hasNextResponses[i] = true;
			}
			hasNextResponses[hasNextResponses.length - 1] = false;
			Mockito.when(mockIterator.hasNext())
					.thenReturn(true, hasNextResponses);
			final T[] valuesMinusTheFirst = Arrays.copyOfRange(values, 1, values.length);
			Mockito.when(mockIterator.next())
					.thenReturn(values[0], valuesMinusTheFirst);
		}
	}
}
