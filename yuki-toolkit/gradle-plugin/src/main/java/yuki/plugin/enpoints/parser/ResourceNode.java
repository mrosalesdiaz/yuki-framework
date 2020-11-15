package yuki.plugin.enpoints.parser;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceNode {
	Set<ResourceNode> children = new LinkedHashSet<>();
	String name;
	List<String> paths = new LinkedList<>();
	Map<String, String> props = new HashMap<>();
	String method;
}
