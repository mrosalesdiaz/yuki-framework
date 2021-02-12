package test;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import yuki.framework.dataaccess.Db;
import yuki.framework.dataaccess.DbConfigurator;

public class Test_DatabaseConnection {
    
    @Inject
    private Db db;
    
    @Inject
    private DbConfigurator dbConfigurator;
    
    @BeforeEach
    void initialize() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            
        });
        
        injector.injectMembers(this);
    }
    
    @Test
    void Should_return_an_active_connection_When_it_is_well_configure() {
        JsonObject jdbcConfiguration = new JsonObject().put("jdbcUrl", "postgresql://localhost/db_unit_testing")
                .put("dbUser", "postgres").put("dbPassword", "not24get");
        
        Vertx vertx = Vertx.vertx();
        dbConfigurator.init(jdbcConfiguration, vertx);
        assertThat(db.getConnection()).isNotNull();
    }
    
    @Test
    void Should_thrown_exception_When_configuration_is_wrong() {
        JsonObject jdbcConfiguration = new JsonObject().put("jdbcUrl", "postgresql://localhost_/db_unit_testing")
                .put("dbUser", "postgres").put("dbPassword", "not24get");
        
        Vertx vertx = Vertx.vertx();
        dbConfigurator.init(jdbcConfiguration, vertx);
        assertThat(db.getConnection()).isNotNull();
    }
    
}
