package com.linkedin.pinot.core.plan.maker;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.linkedin.pinot.common.request.BrokerRequest;
import com.linkedin.pinot.core.indexsegment.IndexSegment;
import com.linkedin.pinot.core.indexsegment.columnar.ColumnarSegment;
import com.linkedin.pinot.core.plan.AggregationGroupByOperatorPlanNode;
import com.linkedin.pinot.core.plan.AggregationGroupByOperatorPlanNode.AggregationGroupByImplementationType;
import com.linkedin.pinot.core.plan.AggregationPlanNode;
import com.linkedin.pinot.core.plan.CombinePlanNode;
import com.linkedin.pinot.core.plan.GlobalPlanImplV0;
import com.linkedin.pinot.core.plan.InstanceResponsePlanNode;
import com.linkedin.pinot.core.plan.Plan;
import com.linkedin.pinot.core.plan.PlanNode;
import com.linkedin.pinot.core.plan.SelectionPlanNode;
import com.linkedin.pinot.core.query.aggregation.groupby.BitHacks;


/**
 * Make the huge plan, root is always ResultPlanNode, the child of it is a huge
 * plan node which will take the segment and query, then do everything.
 * 
 * @author xiafu
 *
 */
public class InstancePlanMakerImplV2 implements PlanMaker {
  private final long _timeOutMs;

  public InstancePlanMakerImplV2() {
    _timeOutMs = 150000;
  }

  public InstancePlanMakerImplV2(long timeOutMs) {
    _timeOutMs = timeOutMs;
  }

  @Override
  public PlanNode makeInnerSegmentPlan(IndexSegment indexSegment, BrokerRequest brokerRequest) {

    if (brokerRequest.isSetAggregationsInfo()) {
      if (!brokerRequest.isSetGroupBy()) {
        // Only Aggregation
        PlanNode aggregationPlanNode = new AggregationPlanNode(indexSegment, brokerRequest);
        return aggregationPlanNode;
      } else {
        // Aggregation GroupBy
        PlanNode aggregationGroupByPlanNode;
        if (indexSegment instanceof ColumnarSegment) {
          if (isGroupKeyFitForLong(indexSegment, brokerRequest)) {
            aggregationGroupByPlanNode =
                new AggregationGroupByOperatorPlanNode(indexSegment, brokerRequest,
                    AggregationGroupByImplementationType.Dictionary);
          } else {
            aggregationGroupByPlanNode =
                new AggregationGroupByOperatorPlanNode(indexSegment, brokerRequest,
                    AggregationGroupByImplementationType.DictionaryAndTrie);
          }
        } else {
          aggregationGroupByPlanNode =
              new AggregationGroupByOperatorPlanNode(indexSegment, brokerRequest,
                  AggregationGroupByImplementationType.NoDictionary);
        }
        return aggregationGroupByPlanNode;
      }
    }
    // Only Selection
    if (brokerRequest.isSetSelections()) {
      PlanNode selectionPlanNode = new SelectionPlanNode(indexSegment, brokerRequest);
      return selectionPlanNode;
    }
    throw new UnsupportedOperationException("The query contains no aggregation or selection!");
  }

  @Override
  public Plan makeInterSegmentPlan(List<IndexSegment> indexSegmentList, BrokerRequest brokerRequest,
      ExecutorService executorService) {
    InstanceResponsePlanNode rootNode = new InstanceResponsePlanNode();
    CombinePlanNode combinePlanNode = new CombinePlanNode(brokerRequest, executorService, _timeOutMs);
    rootNode.setPlanNode(combinePlanNode);
    for (IndexSegment indexSegment : indexSegmentList) {
      combinePlanNode.addPlanNode(makeInnerSegmentPlan(indexSegment, brokerRequest));
    }
    return new GlobalPlanImplV0(rootNode);
  }

  private boolean isGroupKeyFitForLong(IndexSegment indexSegment, BrokerRequest brokerRequest) {
    ColumnarSegment columnarSegment = (ColumnarSegment) indexSegment;
    int totalBitSet = 0;
    for (String column : brokerRequest.getGroupBy().getColumns()) {
      totalBitSet += BitHacks.findLogBase2(columnarSegment.getDictionaryFor(column).size()) + 1;
    }
    if (totalBitSet > 64) {
      return false;
    }
    return true;
  }
}
