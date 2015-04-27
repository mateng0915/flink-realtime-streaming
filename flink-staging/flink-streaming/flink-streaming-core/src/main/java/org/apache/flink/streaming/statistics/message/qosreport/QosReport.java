/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.statistics.message.qosreport;

import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.streaming.statistics.message.AbstractSerializableQosMessage;
import org.apache.flink.streaming.statistics.taskmanager.qosmodel.QosReporterID;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Holds Qos report data to be shipped to a specific Qos manager. Instead of
 * sending each {@link AbstractQosReportRecord} individually, they are sent in
 * batch. Most internal fields of this class are initialized in a lazy fashion,
 * thus (empty) instances of this class have a small memory footprint.
 *
 * @author Bjoern Lohrmann
 */
public class QosReport extends AbstractSerializableQosMessage {

	private HashMap<QosReporterID.Edge, EdgeLatency> edgeLatencies;

	private HashMap<QosReporterID.Edge, EdgeStatistics> edgeStatistics;

	private HashMap<QosReporterID.Vertex, VertexStatistics> vertexStatistics;

	/**
	 * Creates and initializes QosReport object to be used for
	 * receiving/deserialization.
	 */
	public QosReport() {
		super();
	}

	private HashMap<QosReporterID.Edge, EdgeLatency> getOrCreateEdgeLatencyMap() {
		if (this.edgeLatencies == null) {
			this.edgeLatencies = new HashMap<QosReporterID.Edge, EdgeLatency>();
		}
		return this.edgeLatencies;
	}

	private HashMap<QosReporterID.Edge, EdgeStatistics> getOrCreateEdgeStatisticsMap() {
		if (this.edgeStatistics == null) {
			this.edgeStatistics = new HashMap<QosReporterID.Edge, EdgeStatistics>();
		}
		return this.edgeStatistics;
	}

	private HashMap<QosReporterID.Vertex, VertexStatistics> getOrCreateVertexStatisticsMap() {
		if (this.vertexStatistics == null) {
			this.vertexStatistics = new HashMap<QosReporterID.Vertex, VertexStatistics>();
		}
		return this.vertexStatistics;
	}

	public void addEdgeLatency(EdgeLatency edgeLatency) {
		QosReporterID.Edge reporterID = edgeLatency.getReporterID();

		EdgeLatency existing = this.getOrCreateEdgeLatencyMap().get(reporterID);
		if (existing == null) {
			this.getOrCreateEdgeLatencyMap().put(reporterID, edgeLatency);
		} else {
			existing.add(edgeLatency);
		}
	}

	public Collection<EdgeLatency> getEdgeLatencies() {
		if (this.edgeLatencies == null) {
			return Collections.emptyList();
		}
		return this.edgeLatencies.values();
	}

	public void addEdgeStatistics(EdgeStatistics edgeStats) {

		QosReporterID.Edge reporterID = edgeStats.getReporterID();

		EdgeStatistics existing = this.getOrCreateEdgeStatisticsMap().get(reporterID);
		if (existing == null) {
			this.getOrCreateEdgeStatisticsMap().put(reporterID, edgeStats);
		} else {
			this.getOrCreateEdgeStatisticsMap().put(reporterID, existing.fuseWith(edgeStats));
		}
	}

	public Collection<EdgeStatistics> getEdgeStatistics() {
		if (this.edgeStatistics == null) {
			return Collections.emptyList();
		}
		return this.edgeStatistics.values();
	}

	public void addVertexStatistics(VertexStatistics vertexStats) {
		QosReporterID.Vertex reporterID = vertexStats.getReporterID();
		VertexStatistics existing = this.getOrCreateVertexStatisticsMap().get(
				reporterID);
		if (existing == null) {
			this.getOrCreateVertexStatisticsMap().put(reporterID, vertexStats);
		} else {
			this.getOrCreateVertexStatisticsMap().put(reporterID,
					existing.fuseWith(vertexStats));
		}
	}

	public Collection<VertexStatistics> getVertexStatistics() {
		if (this.vertexStatistics == null) {
			return Collections.emptyList();
		}
		return this.vertexStatistics.values();
	}

	@Override
	public void write(final DataOutputView out) throws IOException {
		this.writeEdgeLatencies(out);
		this.writeEdgeStatistics(out);
		this.writeVertexLatencies(out);
	}

	private void writeEdgeLatencies(DataOutputView out) throws IOException {
		if (this.edgeLatencies != null) {
			out.writeInt(this.edgeLatencies.size());
			for (Entry<QosReporterID.Edge, EdgeLatency> entry : this.edgeLatencies
					.entrySet()) {
				entry.getKey().write(out);
				out.writeDouble(entry.getValue().getEdgeLatency());
			}
		} else {
			out.writeInt(0);
		}
	}

	private void writeEdgeStatistics(DataOutputView out) throws IOException {
		if (this.edgeStatistics != null) {
			out.writeInt(this.edgeStatistics.size());
			for (Entry<QosReporterID.Edge, EdgeStatistics> entry : this.edgeStatistics
					.entrySet()) {
				entry.getKey().write(out);
				out.writeDouble(entry.getValue().getThroughput());
				out.writeDouble(entry.getValue().getOutputBufferLifetime());
				out.writeDouble(entry.getValue().getRecordsPerBuffer());
				out.writeDouble(entry.getValue().getRecordsPerSecond());
			}
		} else {
			out.writeInt(0);
		}
	}

	private void writeVertexLatencies(DataOutputView out) throws IOException {
		if (this.vertexStatistics != null) {
			out.writeInt(this.vertexStatistics.size());
			for (VertexStatistics vertexStat : this.vertexStatistics.values()) {
				vertexStat.write(out);
			}
		} else {
			out.writeInt(0);
		}
	}

	@Override
	public void read(final DataInputView in) throws IOException {
		this.readEdgeLatencies(in);
		this.readOutputEdgeStatistics(in);
		this.readVertexStatistics(in);
	}

	private void readEdgeLatencies(DataInputView in) throws IOException {
		int toRead = in.readInt();
		for (int i = 0; i < toRead; i++) {
			QosReporterID.Edge reporterID = new QosReporterID.Edge();
			reporterID.read(in);

			EdgeLatency edgeLatency = new EdgeLatency(reporterID,
					in.readDouble());
			this.getOrCreateEdgeLatencyMap().put(reporterID, edgeLatency);
		}
	}

	private void readOutputEdgeStatistics(DataInputView in) throws IOException {
		int toRead = in.readInt();
		for (int i = 0; i < toRead; i++) {
			QosReporterID.Edge reporterID = new QosReporterID.Edge();
			reporterID.read(in);

			EdgeStatistics edgeStats = new EdgeStatistics(reporterID,
					in.readDouble(), in.readDouble(), in.readDouble(),
					in.readDouble());
			this.getOrCreateEdgeStatisticsMap().put(reporterID, edgeStats);
		}
	}

	private void readVertexStatistics(DataInputView in) throws IOException {
		int toRead = in.readInt();
		for (int i = 0; i < toRead; i++) {
			VertexStatistics vertexStat = new VertexStatistics();
			vertexStat.read(in);
			this.getOrCreateVertexStatisticsMap().put(
					vertexStat.getReporterID(), vertexStat);
		}
	}

	public boolean isEmpty() {
		return this.edgeLatencies == null
				&& this.edgeStatistics == null
				&& this.vertexStatistics == null;
	}
}
