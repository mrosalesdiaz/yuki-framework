package services.products.verticle.daos;

import io.vertx.core.json.JsonArray;
import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;
import yuki.framework.dataaccess.utils.QueryDefinition;

@QueryDefinitionMetadata(sql = " select * from fn_category_create_new( name := 'chanchito' ) ", returnType = JsonArray.class)
public interface CreateNewCategory extends QueryDefinition {

	void setName(String name);

}
