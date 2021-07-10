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
import javax.inject.Inject;

public class FunctionsDao {

  @Inject
  private Ds ds;

  public JsonArray readFunctions(String schemaName) throws SQLException, IOException {
    JsonArray result = new JsonArray();

    try (Connection connection = ds.getConnection()) {

      try (PreparedStatement preparedStatement = connection
          .prepareStatement(getQueryToLoadFunctions())) {
        preparedStatement.setString(1, schemaName);

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          while (resultSet.next()) {
            JsonObject functionDefinition = new JsonObject();

            functionDefinition.put("id", resultSet.getInt("id"));
            functionDefinition.put("schemaName", resultSet.getString("schemaName"));
            functionDefinition.put("functionName", resultSet.getString("functionName"));
            functionDefinition.put("hasMany", resultSet.getBoolean("hasMany"));
            functionDefinition.put("functionArguments", resultSet.getString("functionArguments"));
            functionDefinition.put("functionReturn", resultSet.getString("functionReturn"));

            result.add(functionDefinition);
          }
          return result;
        }
      }
    }
  }

  private String getQueryToLoadFunctions() throws IOException {
    return Resources.toString(
        Resources.getResource(
            FunctionsDao.class,
            String.format("/sql/%s.sql",
                "query-select-functions-by-schema"
            )
        )
        , StandardCharsets.UTF_8
    );
  }

}
