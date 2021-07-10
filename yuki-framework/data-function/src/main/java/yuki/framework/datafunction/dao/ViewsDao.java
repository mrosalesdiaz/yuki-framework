package yuki.framework.datafunction.dao;

import com.google.common.io.Resources;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;
import javax.inject.Inject;
import yuki.common.stream.JsonCollector;

public class ViewsDao {

  @Inject
  private Ds ds;

  public JsonArray readViews(String schemaName) throws SQLException, IOException {
    JsonArray columnDefinitions = new JsonArray();

    try (Connection connection = ds.getConnection()) {
      try (PreparedStatement preparedStatement = connection
          .prepareStatement(getQueryToReadViewColumnDefinitions())) {
        preparedStatement.setString(1, schemaName);

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          while (resultSet.next()) {
            JsonObject item = new JsonObject();

            item.put("schemaName", resultSet.getString("table_schema"));
            item.put("viewName", resultSet.getString("table_name"));
            item.put("columnName", resultSet.getString("column_name"));
            item.put("databaseType", resultSet.getString("data_type"));

            columnDefinitions.add(item);
          }
        }
      }
    }

    return columnDefinitions.stream()
        .map(JsonObject.class::cast)
        .map(i -> i.getString("viewName"))
        .distinct()
        .map(tableName -> {
          JsonObject viewDefinition = new JsonObject();

          viewDefinition.put("viewName", tableName);
          viewDefinition.put("schemaName", schemaName);

          viewDefinition.put(
              "columns",
              columnDefinitions.stream()
                  .map(JsonObject.class::cast)
                  .filter(i -> i.getString("viewName").equalsIgnoreCase(tableName))
                  .map(i -> String
                      .format("%s %s", i.getString("columnName"), i.getString("databaseType")))
                  .collect(Collectors.joining(", "))
          );

          return viewDefinition;
        })
        .collect(JsonCollector.toJsonArray());
  }

  private String getQueryToReadViewColumnDefinitions() throws IOException {
    return Resources.toString(
        Resources.getResource(
            ViewsDao.class,
            String.format("/sql/%s.sql",
                "query-select-views-definition"
            )
        )
        , StandardCharsets.UTF_8
    );
  }

}
