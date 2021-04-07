package yuki.framework.verticle.common;

@FunctionalInterface
public interface ExecutionBlock {

    void apply() throws Throwable;
}
