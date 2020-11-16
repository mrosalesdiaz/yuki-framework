package yuki.development.service.controllers;

import java.util.Iterator;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class RowProcessor {

	public Handler<AsyncResult<RowSet<Row>>> asJsonArray(final Handler<AsyncResult<JsonArray>> fnCallback) {
		return ar -> {
			if (ar.failed()) {
				fnCallback.handle(Future.failedFuture(ar.cause()));
			}

			final JsonArray jsonArray = new JsonArray();

			for (final Iterator<Row> iterator = ar.result()
					.iterator(); iterator.hasNext();) {
				final Row row = iterator.next();
				final JsonObject jsonObject = new JsonObject();

				for (int i = 0; i < row.size(); i++) {
					jsonObject.put(row.getColumnName(i), row.getValue(i));
				}

				jsonArray.add(jsonObject);
			}

			fnCallback.handle(Future.succeededFuture(jsonArray));
		};
	}

}
