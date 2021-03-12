package test.dataaccess;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import yuki.framework.dataaccess.utils.QueryExecutor;


@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class Test_SetterAnnotations {


    private Injector rootInjector;

    @BeforeEach
    void initialzie() {
        this.rootInjector = Guice.createInjector(new AbstractModule() {
        });
    }

    @Test
    void Should___When_() {
        FnEcho instance = rootInjector.getInstance(QueryExecutor.class).create(FnEcho.class);

        instance.setV_boolean(true);
    }


}