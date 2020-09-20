package own.pattern.factory.demo.dependencyInjection;

import java.io.InputStream;
import java.util.List;

public interface BeanConfigParser {

    List<BeanDefinition> parse(InputStream in);

    List<BeanDefinition> parse(String configContent);
}
