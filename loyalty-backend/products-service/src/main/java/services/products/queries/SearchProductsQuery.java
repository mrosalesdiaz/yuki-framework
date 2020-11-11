package services.products.queries;

import io.vertx.core.json.JsonArray;
import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;
import yuki.framework.dataaccess.utils.QueryDefinition;

@QueryDefinitionMetadata(sql = " SELECT * from fn_product_search_index( title:='', keywords:='' ) ", returnType = JsonArray.class)
public interface SearchProductsQuery extends QueryDefinition {
	void setName(String name);
}
