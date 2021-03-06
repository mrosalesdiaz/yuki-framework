package yuki.plugin.gen.api.contracts;

import feign.Param;
import feign.RequestLine;

public interface DbFunctions {
	@RequestLine("GET /api/{schemaName}/functions")
	String functions(@Param("schemaName") String schemaName);
}
