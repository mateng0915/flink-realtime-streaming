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

package org.apache.flink.streaming.statistics.message.action;

import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.runtime.jobgraph.IntermediateDataSetID;

import java.io.IOException;

/**
 * Describes a Qos reporter role for an edge.
 *
 * @author Bjoern Lohrmann, Sascha Wolke
 */
public class EdgeQosReporterConfig implements QosReporterConfig {

//	private IntermediateResultPartitionID intermediateResultPartitionID;

//	private int consumedSubpartitionIndex;

	private IntermediateDataSetID intermediateDataSetID;

	private int outputGateIndex; // optional (based on intermediate result)

	private int inputGateIndex; // optional (based on intermediate result)

	public enum Side {
		SOURCE,
		TARGET
	}

	/** Source and target reporter configs differs only in this field. */
	private Side deploymentSide;

	private String name;

	public EdgeQosReporterConfig() {
	}

	/**
	 * Initializes EdgeQosReporterConfig.
	 */
	public EdgeQosReporterConfig(IntermediateDataSetID intermediateDataSetID,
			int outputGateIndex, int inputGateIndex,
			Side deploymentSide, String name) {

		this.intermediateDataSetID = intermediateDataSetID;
		this.outputGateIndex = outputGateIndex;
		this.inputGateIndex = inputGateIndex;
		this.deploymentSide = deploymentSide;
		this.name = name;
	}

	public IntermediateDataSetID getIntermediateDataSetID() {
		return intermediateDataSetID;
	}

	public int getOutputGateIndex() {
		return this.outputGateIndex;
	}

	public int getInputGateIndex() {
		return this.inputGateIndex;
	}

	public boolean isSourceTaskConfig() {
		return this.deploymentSide == Side.SOURCE;
	}

	public boolean isTargetTaskConfig() {
		return this.deploymentSide == Side.TARGET;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public void write(DataOutputView out) throws IOException {
		this.intermediateDataSetID.write(out);
		out.writeInt(this.outputGateIndex);
		out.writeInt(this.inputGateIndex);
		out.writeUTF(this.name);
	}

	@Override
	public void read(DataInputView in) throws IOException {
		this.intermediateDataSetID = new IntermediateDataSetID();
		this.intermediateDataSetID.read(in);
		this.outputGateIndex = in.readInt();
		this.inputGateIndex = in.readInt();
		this.name = in.readUTF();
	}

	@Override
	public String toString() {
		return "EdgeQosReporterConfig("
				+ this.outputGateIndex + " -> " + this.name + " -> " + this.inputGateIndex
				+ ", dataSet: " + this.intermediateDataSetID + ")";
	}

	public static EdgeQosReporterConfig sourceTaskConfig(
			IntermediateDataSetID intermediateDataSetID,
			int outputGateIndex, int inputGateIndex, String name) {

		return new EdgeQosReporterConfig(intermediateDataSetID,
				outputGateIndex, inputGateIndex, Side.SOURCE, name);
	}

	public static EdgeQosReporterConfig targetTaskConfig(
			IntermediateDataSetID intermediateDataSetID,
			int outputGateIndex, int inputGateIndex, String name) {

		return new EdgeQosReporterConfig(intermediateDataSetID,
				outputGateIndex, inputGateIndex, Side.TARGET, name);
	}
}
