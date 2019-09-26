package own.spring.core.model.inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
public class Tea {

  private String name;

  private Water water;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


}
