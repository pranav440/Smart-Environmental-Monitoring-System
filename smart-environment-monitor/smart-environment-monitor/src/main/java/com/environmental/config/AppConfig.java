package com.environmental.config;

import com.environmental.strategies.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AppConfig {

    @Bean
    public CompositeRiskStrategy riskStrategy() {
        CompositeRiskStrategy strategy = new CompositeRiskStrategy();
        strategy.addStrategy(new HeatwaveRiskStrategy(), 0.30);
        strategy.addStrategy(new AQIRiskStrategy(), 0.35);
        strategy.addStrategy(new HumidityRiskStrategy(), 0.15);
        strategy.addStrategy(new DroughtRiskStrategy(), 0.10);
        strategy.addStrategy(new SeaLevelRiskStrategy(), 0.05);
        strategy.addStrategy(new GlacierRiskStrategy(), 0.05);
        return strategy;
    }
}