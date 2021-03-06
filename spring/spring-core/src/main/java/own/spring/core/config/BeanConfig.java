package own.spring.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import own.spring.core.model.cyclicDependency.Room;


@ComponentScans({
    @ComponentScan("own.spring.core.model.cyclicDependency"),
//    @ComponentScan("own.spring.core.model"),
//    @ComponentScan("own.spring.core.postProcessor"),
})
@Configuration
public class BeanConfig {

  // bean的name 默认 方法名称
//  @Bean
//  public MessageService messageService() {
//    return new MessageServiceImpl();
//  }

//  @Bean(value = "car", initMethod = "init", destroyMethod = "destroy")
//  public Car getCar() {
//    return new Car();
//  }

  @Bean
  public Room room() {
    Room room = new Room();
    room.setAirConditioner("Gree");
    room.setTelevision("Xiaomi");
    room.setRefrigerator("Haier");
    room.setWasher("Siemens");
    return room;
  }
}
