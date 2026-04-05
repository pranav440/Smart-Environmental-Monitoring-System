package com.environmental.service;

import com.environmental.models.City;
import com.environmental.models.EnvironmentalData;

public interface EnvironmentalService {
    EnvironmentalData getCurrentData(City city);
    double calculateRisk(City city);
}