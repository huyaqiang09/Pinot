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
package com.linkedin.pinot.core.realtime.converter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.linkedin.pinot.common.data.Schema;
import com.linkedin.pinot.core.data.GenericRow;
import com.linkedin.pinot.core.data.readers.RecordReader;
import com.linkedin.pinot.core.realtime.impl.RealtimeSegmentImpl;


public class RealtimeSegmentRecordReader implements RecordReader {

  private RealtimeSegmentImpl realtimeSegment;
  private Schema dataSchema;
  private List<String> columns;
  int counter = 0;
  private final Iterator<Integer> docIdIterator;

  public RealtimeSegmentRecordReader(RealtimeSegmentImpl rtSegment, Schema schema) {
    this.realtimeSegment = rtSegment;
    this.dataSchema = schema;
    columns = new ArrayList<String>();
    this.docIdIterator = null;
  }

  public RealtimeSegmentRecordReader(RealtimeSegmentImpl rtSegment, Schema schema, String sortedColumn) {
    this.realtimeSegment = rtSegment;
    this.dataSchema = schema;
    columns = new ArrayList<String>();
    this.docIdIterator = realtimeSegment.getSortedDocIdIteratorOnColumn(sortedColumn);
  }

  @Override
  public void init() throws Exception {
    columns.addAll(dataSchema.getDimensionNames());
    columns.addAll(dataSchema.getMetricNames());
    columns.add(dataSchema.getTimeFieldSpec().getOutGoingTimeColumnName());
  }

  @Override
  public void rewind() throws Exception {
    counter = 0;
  }

  @Override
  public boolean hasNext() {
    if (docIdIterator == null) {
      return counter < realtimeSegment.getAggregateDocumentCount();
    }
    return docIdIterator.hasNext();
  }

  @Override
  public Schema getSchema() {
    return dataSchema;
  }

  @Override
  public GenericRow next() {
    if (docIdIterator == null) {
      GenericRow row = realtimeSegment.getRawValueRowAt(counter);
      counter++;
      return row;
    }
    int docId = docIdIterator.next();
    return realtimeSegment.getRawValueRowAt(docId);
  }

  @Override
  public void close() throws Exception {
    realtimeSegment = null;
  }

}
