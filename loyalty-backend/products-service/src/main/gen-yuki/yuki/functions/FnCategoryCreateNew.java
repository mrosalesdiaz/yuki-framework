package yuki.functions;

import io.vertx.core.json.JsonArray;
import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;
import yuki.framework.dataaccess.utils.QueryDefinition;

@QueryDefinitionMetadata(sql = " SELECT * FROM fn_category_create_new( name := $1 ) ", returnType = JsonArray.class)
public interface FnCategoryCreateNew extends QueryDefinition {

    void setName(String name);
}
