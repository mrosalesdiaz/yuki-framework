package yuki.functions;

import yuki.common.dto.Get;
import yuki.common.dto.JsonDto;
import yuki.framework.dataaccess.annotations.Parameter;
import yuki.framework.dataaccess.annotations.ParameterType;
import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;
import yuki.framework.dataaccess.utils.QueryDefinition;

@QueryDefinitionMetadata(sql = " SELECT * FROM fn_category_create_new( name := $1 ) ")
public interface FnCategoryCreateNew extends QueryDefinition {

  @Parameter(ParameterType.VARCHAR)
  void setParam(String param);

}
