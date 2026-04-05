package com.environmental;

import com.environmental.models.EnvironmentalData;
import com.environmental.strategies.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RiskStrategyTest {

    @Test
    void testHeatwaveHighTemperature() {
        HeatwaveRiskStrategy strategy = new HeatwaveRiskStrategy();
        EnvironmentalData data = new EnvironmentalData(46, 100, 50, 10, 1013);
        assertEquals(100, strategy.calculateRisk(data), 0.01);
    }

    @Test
    void testHeatwaveNormalTemperature() {
        HeatwaveRiskStrategy strategy = new HeatwaveRiskStrategy();
        EnvironmentalData data = new EnvironmentalData(25, 100, 50, 10, 1013);
        double risk = strategy.calculateRisk(data);
        assertTrue(risk >= 0 && risk < 50);
    }

    @Test
    void testAQICritical() {
        AQIRiskStrategy strategy = new AQIRiskStrategy();
        EnvironmentalData data = new EnvironmentalData(30, 350, 50, 10, 1013);
        assertEquals(100, strategy.calculateRisk(data), 0.01);
    }

    @Test
    void testAQIGood() {
        AQIRiskStrategy strategy = new AQIRiskStrategy();
        EnvironmentalData data = new EnvironmentalData(30, 30, 50, 10, 1013);
        double risk = strategy.calculateRisk(data);
        assertTrue(risk < 20);
    }

    @Test
    void testDroughtLowHumidity() {
        DroughtRiskStrategy strategy = new DroughtRiskStrategy();
        EnvironmentalData data = new EnvironmentalData(42, 100, 10, 10, 1013);
        double risk = strategy.calculateRisk(data);
        assertTrue(risk > 80, "Drought risk should be high with very low humidity and high temp");
    }

    @Test
    void testSeaLevelLowPressure() {
        SeaLevelRiskStrategy strategy = new SeaLevelRiskStrategy();
        EnvironmentalData data = new EnvironmentalData(30, 100, 85, 25, 985);
        double risk = strategy.calculateRisk(data);
        assertTrue(risk > 60, "Sea level risk should be high with low pressure, high humidity, high wind");
    }

    @Test
    void testGlacierHighTemperature() {
        GlacierRiskStrategy strategy = new GlacierRiskStrategy();
        EnvironmentalData data = new EnvironmentalData(28, 100, 50, 10, 1013);
        double risk = strategy.calculateRisk(data);
        assertTrue(risk > 90, "Glacier melt risk should be very high at 28°C");
    }

    @Test
    void testCompositeRiskCombination() {
        CompositeRiskStrategy composite = new CompositeRiskStrategy();
        composite.addStrategy(new HeatwaveRiskStrategy(), 0.35);
        composite.addStrategy(new AQIRiskStrategy(), 0.45);
        composite.addStrategy(new HumidityRiskStrategy(), 0.20);

        EnvironmentalData data = new EnvironmentalData(42, 280, 85, 10, 1013);
        double risk = composite.calculateRisk(data);
        assertTrue(risk > 50 && risk <= 100, "Composite risk should be high: " + risk);
    }

    @Test
    void testCompositeRiskNeverExceeds100() {
        CompositeRiskStrategy composite = new CompositeRiskStrategy();
        composite.addStrategy(new HeatwaveRiskStrategy(), 0.5);
        composite.addStrategy(new AQIRiskStrategy(), 0.5);

        EnvironmentalData extreme = new EnvironmentalData(50, 500, 95, 30, 980);
        double risk = composite.calculateRisk(extreme);
        assertTrue(risk <= 100, "Risk must be capped at 100");
    }
}
