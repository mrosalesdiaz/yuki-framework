package yuki.plugin.gen.api.contracts;

import feign.RequestLine;

public interface Endpoints {
	@RequestLine("GET /api/endpoints/")
	String endpoints();

}
