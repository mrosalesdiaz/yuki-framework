package yuki.views;

import yuki.common.dto.Get;
import yuki.common.dto.JsonDto;

public interface VData extends JsonDto {

  @Get("param_name")
  String getParamName();

}
