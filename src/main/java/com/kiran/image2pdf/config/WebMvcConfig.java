package com.kiran.image2pdf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer  {
	
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/**","/images/**")
                .addResourceLocations("classpath:/static/","file:/home/kiran/app-configs/ic/images/")
                .setCachePeriod(0);
        /*
        registry
        .addResourceHandler("/**")
        .addResourceLocations("classpath:/static/")
        .setCachePeriod(0);
        */
        
    }

}
