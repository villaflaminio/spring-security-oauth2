package com.flaminiovilla.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

/**
 * Configuration class for Freemarker template engine.
 */
@Configuration
public class EmailConfiguration {
    /**
     * Create a bean for Freemarker configuration to setup the path where the templates will be saved.
     *
     * @return the Freemarker configuration bean.
     */
    @Primary
    @Bean
    public FreeMarkerConfigurationFactoryBean factoryBean(){
        FreeMarkerConfigurationFactoryBean bean = new FreeMarkerConfigurationFactoryBean();
        bean.setTemplateLoaderPath("classpath:/mail-templates");
        return bean;
    }
}
