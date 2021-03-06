/**
 * Copyright (C) 2014-2015 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.pinot.common.client.request.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import com.linkedin.pinot.common.request.FilterOperator;
import com.linkedin.pinot.common.utils.request.FilterQueryTree;


/**
 * Nov 21, 2014
 */

public class RangeFilterQueryTreeConstructor extends FilterQueryTreeConstructor {
  public static final String FILTER_TYPE = "range";

  @Override
  protected FilterQueryTree doConstructFilter(Object obj) throws Exception {
    final JSONObject json = (JSONObject) obj;

    final Iterator<String> iter = json.keys();
    if (!iter.hasNext()) {
      return null;
    }

    final String field = iter.next();

    final JSONObject jsonObj = json.getJSONObject(field);

    final String from, to;
    final boolean include_lower, include_upper;
    final boolean noOptimize = jsonObj.optBoolean(NOOPTIMIZE_PARAM, false);
    final String type;
    final String dateFormat;

    final String gt = jsonObj.optString(GT_PARAM, null);
    final String gte = jsonObj.optString(GTE_PARAM, null);
    final String lt = jsonObj.optString(LT_PARAM, null);
    final String lte = jsonObj.optString(LTE_PARAM, null);

    type = jsonObj.optString(RANGE_FIELD_TYPE, null);
    dateFormat = jsonObj.optString(RANGE_DATE_FORMAT, null);

    if (gt != null && gt.length() != 0) {
      from = gt;
      include_lower = false;
    } else if (gte != null && gte.length() != 0) {
      from = gte;
      include_lower = true;
    } else {
      from = jsonObj.optString(FROM_PARAM, null);
      include_lower = jsonObj.optBoolean(INCLUDE_LOWER_PARAM, true);
    }

    if (lt != null && lt.length() != 0) {
      to = lt;
      include_upper = false;
    } else if (lte != null && lte.length() != 0) {
      to = lte;
      include_upper = true;
    } else {
      to = jsonObj.optString(TO_PARAM, null);
      include_upper = jsonObj.optBoolean(INCLUDE_UPPER_PARAM, true);
    }

    final StringBuilder sb = new StringBuilder();
    if (include_lower && from != null && from.length() != 0) {
      sb.append("[");
    } else {
      sb.append("(");
    }

    if (from == null || from.length() == 0) {
      sb.append("*");
    } else {
      sb.append(from);
    }
    sb.append("\t\t");
    if (to == null || to.length() == 0) {
      sb.append("*");
    } else {
      sb.append(to);
    }

    if (include_upper && to != null && to.length() != 0) {
      sb.append("]");
    } else {
      sb.append(")");
    }

    final List<String> vals = new ArrayList<String>();
    vals.add(sb.toString());

    return new FilterQueryTree(field, vals, FilterOperator.RANGE, null);
  }

}
