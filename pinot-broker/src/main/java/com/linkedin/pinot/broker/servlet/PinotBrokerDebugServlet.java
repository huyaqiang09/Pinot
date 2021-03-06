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
package com.linkedin.pinot.broker.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedin.pinot.common.metrics.BrokerMeter;
import com.linkedin.pinot.common.metrics.BrokerMetrics;
import com.linkedin.pinot.requestHandler.BrokerRequestHandler;

public class PinotBrokerDebugServlet extends HttpServlet {
  // for serde
  private static final long serialVersionUID = -3516093545255816357L;
  
  private static final Logger LOGGER = LoggerFactory.getLogger(PinotClientRequestServlet.class);

  private BrokerRequestHandler broker;
  private BrokerMetrics brokerMetrics;
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    broker = (BrokerRequestHandler) config.getServletContext().getAttribute(BrokerRequestHandler.class.toString());
    brokerMetrics = (BrokerMetrics) config.getServletContext().getAttribute(BrokerMetrics.class.toString());
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      System.out.println("********************************* : debug");
      resp.setContentType("application/json");
      resp.getOutputStream().print(broker.getDebugInfo());
      resp.getOutputStream().flush();
      resp.getOutputStream().close();
    } catch (final Exception e) {
      resp.getOutputStream().print(e.getMessage());
      resp.getOutputStream().flush();
      resp.getOutputStream().close();
      LOGGER.error("Caught exception while processing GET request", e);
      brokerMetrics.addMeteredValue(null, BrokerMeter.UNCAUGHT_GET_EXCEPTIONS, 1);
    }
  }
  
}
