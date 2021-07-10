package yuki.gradleplugin.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.zaxxer.hikari.HikariConfig;
import java.net.URL;
import org.gradle.api.Project;
import org.slf4j.Logger;
import yuki.gradleplugin.YukiPluginExtension;

public class GuiceModule implements Module {

  private final YukiPluginExtension yukiPluginParameters;

  public GuiceModule(YukiPluginExtension yukiPluginParameters) {
    this.yukiPluginParameters = yukiPluginParameters;
  }

  @Override
  public void configure(final Binder binder) {
    binder.bind(HikariConfig.class).toProvider(this::newDatabaseConfiguration);
  }

  private HikariConfig newDatabaseConfiguration() {
    HikariConfig config = new HikariConfig();

    config.setJdbcUrl(yukiPluginParameters.getJdbcUrl().get());

    config.setSchema(yukiPluginParameters.getSchema().get());
    config.setDriverClassName(org.postgresql.Driver.class.getName());
    config.addDataSourceProperty("cachePrepStmts", "false");

    return config;
  }

}
