package yuki.plugin.gen.api.contracts;

import feign.RequestLine;

public interface YukiDevelopment {
	@RequestLine("HEAD /")
	String checkStatus();
}
