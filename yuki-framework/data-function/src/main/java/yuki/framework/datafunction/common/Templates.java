package yuki.framework.datafunction.common;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public enum Templates {

  JAVA_FN_DEFINITION("/templates/template-database-function.java"),
  JAVA_VIEW_DEFINITION("/templates/template-database-view.java");

  private String template;

  Templates(String resourcePath) {
    try {
      this.template = Resources
          .toString(Resources.getResource(Templates.class, resourcePath), StandardCharsets.UTF_8);
    } catch (IOException e) {
      this.template = String.format("## ERROR LOADING TEMPLATE: %s ##", e.getMessage());
    }
  }

  public String template() {
    return template;
  }
}
