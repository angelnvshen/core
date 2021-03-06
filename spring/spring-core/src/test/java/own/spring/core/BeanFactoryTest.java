package own.spring.core;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.TypeDescriptor;
import own.spring.core.config.BeanConfig;
import own.spring.core.config.BeanFactoryConfig;
import own.spring.core.model.cyclicDependency.Room;
import own.spring.core.model.lazy.LazyComputer;
import own.spring.core.model.factory.Phone;
import own.spring.core.config.InjectBeanConfig;
import own.spring.core.model.inject.Cup;
import own.spring.core.reveal.DowJonesNewsListener;
import own.spring.core.reveal.DowJonesNewsPersister;
import own.spring.core.reveal.FXNewsProvider;

public class BeanFactoryTest {

  @Test
  public void test() {

    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    bindViaCode(beanFactory);
    FXNewsProvider fxNewsProvider = beanFactory.getBean(FXNewsProvider.class);
    fxNewsProvider.getAndPersistNews();
  }

  public static BeanFactory bindViaCode(BeanDefinitionRegistry registry) {
    AbstractBeanDefinition newsProvider = new RootBeanDefinition(FXNewsProvider.class);
    AbstractBeanDefinition newsListener = new RootBeanDefinition(DowJonesNewsListener.class);
    AbstractBeanDefinition newsPersister = new RootBeanDefinition(DowJonesNewsPersister.class);

    // 将bean定义注册到容器中
    registry.registerBeanDefinition("djNewsProvider", newsProvider);
    registry.registerBeanDefinition("djListener", newsListener);
    registry.registerBeanDefinition("djPersister", newsPersister);

    // 指定依赖关系
    // 1：通过构造方法注入
    ConstructorArgumentValues argumentValues = new ConstructorArgumentValues();
//    argumentValues.addIndexedArgumentValue(0, newsListener);
//    argumentValues.addIndexedArgumentValue(1, newsPersister);
    argumentValues.addGenericArgumentValue(new ValueHolder(newsListener, "own.spring.core.reveal.IFXNewsListener"));
    argumentValues.addGenericArgumentValue(new ValueHolder(newsPersister, "own.spring.core.reveal.IFXNewsPersister"));

    newsProvider.setConstructorArgumentValues(argumentValues);

    // 2：通过set方法注入
//    MutablePropertyValues propertyValues = new MutablePropertyValues();
//    propertyValues.addPropertyValue("newsListener", newsListener);
//    propertyValues.addPropertyValue("newPersistener", newsPersister);
//    newsProvider.setPropertyValues(propertyValues);

    return (BeanFactory) registry;
  }

  @Test
  public void testMethodReplacer() {
    ApplicationContext context = new AnnotationConfigApplicationContext(BeanFactoryConfig.class);
    FXNewsProvider fxNewsProvider = context.getBean(FXNewsProvider.class);
    fxNewsProvider.getAndPersistNews();
  }

  @Test
  public void testBeanFactory() {
    ApplicationContext context = new AnnotationConfigApplicationContext(BeanFactoryConfig.class);
    Phone phone = context.getBean(Phone.class);
    System.out.println(phone.getClass());
    System.out.println(phone.getScreen());
    System.out.println(phone.getScreen());
  }

  @Test
  public void testInject() {

    ApplicationContext context = new AnnotationConfigApplicationContext(InjectBeanConfig.class);
    Cup cup = context.getBean(Cup.class);
    System.out.println(cup.getWater());
    System.out.println(cup.getWater());
    System.out.println(cup.getTea());
    System.out.println(cup.getTea());
    System.out.println(cup.getJuice());
    System.out.println(cup.getJuice());
    System.out.println(cup);
  }

  @Test
  public void testBeanWrapper() throws Exception {

    Object provider = Class.forName("own.spring.core.reveal.FXNewsProvider").newInstance();
    Object listener = Class.forName("own.spring.core.reveal.DowJonesNewsListener").newInstance();
    Object persister = Class.forName("own.spring.core.reveal.DowJonesNewsPersister").newInstance();

    BeanWrapper beanWrapper = new BeanWrapperImpl(provider);
    beanWrapper.setPropertyValue("newsListener", listener);
    beanWrapper.setPropertyValue("newPersistener", persister);

    assertTrue(beanWrapper.getWrappedInstance() instanceof FXNewsProvider);
    assertSame(provider, beanWrapper.getWrappedInstance());
    assertSame(listener, beanWrapper.getPropertyValue("newsListener"));
    assertSame(persister, beanWrapper.getPropertyValue("newPersistener"));

    TypeDescriptor newPersistener = beanWrapper.getPropertyTypeDescriptor("newPersistener");
    System.out.println(newPersistener);

  }

  /**
   * applicationContext 会立即实例化singleton对象，@Lazy注解 会延迟到使用时实例化
   */
  @Test
  public void testLazyInit() {
    ApplicationContext applicationContext = new AnnotationConfigApplicationContext("own.spring.core.model.lazy");
    LazyComputer lazyComputer = applicationContext.getBean(LazyComputer.class);
    System.out.println(lazyComputer.getName());

  }

  @Test
  public void testIocA() {

    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(BeanConfig.class);
    Room bean = applicationContext.getBean(Room.class);
    System.out.println(bean);
  }
}