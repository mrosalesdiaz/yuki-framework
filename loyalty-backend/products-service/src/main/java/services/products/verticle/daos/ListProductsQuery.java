package services.products.verticle.daos;

import io.vertx.core.json.JsonArray;
import yuki.framework.dataaccess.QueryDef;
import yuki.framework.dataaccess.QueryDefinitionMetadata;

@QueryDefinitionMetadata(sql = " SELECT * from fn_get_products2(name:='') ", returnType = JsonArray.class)
public interface ListProductsQuery extends QueryDef {
	ListProductsQuery setName(String name);
}
