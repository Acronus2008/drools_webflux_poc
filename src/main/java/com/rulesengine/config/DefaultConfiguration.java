package com.rulesengine.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.rulesengine.service.impl")
public class DefaultConfiguration {

}
