package org.openlmis.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class CustomWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {

  @Value("${service.url}")
  private String serviceUrl;

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/notification/docs")
        .setViewName("redirect:" + serviceUrl + "/notification/docs/");
    registry.addViewController("/notification/docs/")
        .setViewName("forward:/notification/docs/index.html");
    super.addViewControllers(registry);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/notification/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
    super.addResourceHandlers(registry);
  }
}
