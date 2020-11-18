package yuki.functions;

import io.vertx.core.json.JsonArray;
import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;
import yuki.framework.dataaccess.utils.QueryDefinition;

@QueryDefinitionMetadata(sql = " SELECT * FROM fn_category_create_new( name := $1 ,ame3333 := $2 ) ", returnType = JsonArray.class)
public interface FnCategoryCreateNew1 extends QueryDefinition {

    void setName(String name);

    void setAme3333(String ame3333);
}
