package yuki.plugin.gen.queryclasses;

import feign.Param;
import feign.RequestLine;

public interface DbFunctions {
	@RequestLine("GET /api/functions/{schema}")
	String functions(@Param("schema") String schema);
}
