package yuki.plugin.gen.api.contracts;

import feign.Param;
import feign.RequestLine;

public interface Endpoints {
	@RequestLine("GET /api/endpoints/?endpointsModel={endpointsModel}")
	String endpoints(@Param("endpointsModel") String endpointsModel);

}
