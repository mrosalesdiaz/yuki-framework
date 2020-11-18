package yuki.plugin.gen.queryclasses;

import feign.RequestLine;

public interface Endpoints {
	@RequestLine("GET /api/endpoints/")
	String endpoints();

}
