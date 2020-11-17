package yuki.plugin.gen.queryclasses;

import feign.RequestLine;

public interface YukiDevelopment {
	@RequestLine("HEAD /")
	String checkStatus();
}
