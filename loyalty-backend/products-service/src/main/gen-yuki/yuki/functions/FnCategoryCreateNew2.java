package yuki.functions;

import io.vertx.core.json.JsonArray;
import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;
import yuki.framework.dataaccess.utils.QueryDefinition;

@QueryDefinitionMetadata(sql = " SELECT * FROM fn_category_create_new( name := $1 ,ame := $2 ,ame3333 := $3 ) ", returnType = JsonArray.class)
public interface FnCategoryCreateNew2 extends QueryDefinition {

    void setName(String name);

    void setAme(String ame);

    void setAme3333(String ame3333);
}
