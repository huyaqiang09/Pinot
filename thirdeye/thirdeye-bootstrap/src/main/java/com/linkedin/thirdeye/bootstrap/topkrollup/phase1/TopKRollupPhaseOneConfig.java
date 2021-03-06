package com.linkedin.thirdeye.bootstrap.topkrollup.phase1;

import com.linkedin.thirdeye.api.DimensionSpec;
import com.linkedin.thirdeye.api.MetricSpec;
import com.linkedin.thirdeye.api.MetricType;
import com.linkedin.thirdeye.api.StarTreeConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopKRollupPhaseOneConfig {
  private List<String> dimensionNames;
  private List<String> metricNames;
  private List<MetricType> metricTypes;
  private Map<String,Double> metricThresholds;
  /**
   *
   */
  public TopKRollupPhaseOneConfig(){

  }
/**
 *
 * @param dimensionNames
 * @param metricNames
 * @param metricTypes
 * @param metricThresholds
 */
  public TopKRollupPhaseOneConfig(List<String> dimensionNames,
      List<String> metricNames, List<MetricType> metricTypes,
      Map<String, Double> metricThresholds) {
    super();
    this.dimensionNames = dimensionNames;
    this.metricNames = metricNames;
    this.metricTypes = metricTypes;
    this.metricThresholds = metricThresholds;
  }

  public List<String> getDimensionNames() {
    return dimensionNames;
  }

  public List<String> getMetricNames() {
    return metricNames;
  }

  public List<MetricType> getMetricTypes() {
    return metricTypes;
  }

  public Map<String, Double> getMetricThresholds() {
    return metricThresholds;
  }

  public static TopKRollupPhaseOneConfig fromStarTreeConfig(StarTreeConfig config)
  {

    List<String> metricNames = new ArrayList<String>(config.getMetrics().size());
    List<MetricType> metricTypes = new ArrayList<MetricType>(config.getMetrics().size());
    for (MetricSpec spec : config.getMetrics())
    {
      metricNames.add(spec.getName());
      metricTypes.add(spec.getType());
    }

    List<String> dimensionNames = new ArrayList<String>(config.getDimensions().size());
    for (DimensionSpec dimensionSpec : config.getDimensions())
    {
      dimensionNames.add(dimensionSpec.getName());
    }

    Map<String, Double> metricThresholds = new HashMap<>();
    if (config.getTopKRollup() != null && config.getTopKRollup().getThreshold() != null) {
      metricThresholds = config.getTopKRollup().getThreshold();
    }
    for (String metric : metricNames) {
      if (metricThresholds.get(metric) == null) {
        metricThresholds.put(metric, 0.0);
      }
    }

    return new TopKRollupPhaseOneConfig(dimensionNames,
                                    metricNames,
                                    metricTypes,
                                    metricThresholds);
  }
}
