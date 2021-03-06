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

import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.jobgraph.IntermediateResultPartitionID;
import org.apache.flink.streaming.statistics.message.AbstractSerializableQosMessage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class implements an action to limit the buffer size of a particular
 * output channel.
 * 
 * @author warneke, Bjoern Lohrmann, Sascha Wolke
 */
public final class LimitBufferSizeAction extends AbstractSerializableQosMessage implements QosAction {

	private final ExecutionAttemptID attemptID;

	private final IntermediateResultPartitionID partitionID;

	private int consumedSubpartitionIndex;

	private int bufferSize;

	public LimitBufferSizeAction(ExecutionAttemptID attemptID,
			IntermediateResultPartitionID partitionID, int consumedSubpartitionIndex,
			int bufferSize) {

		super();

		checkNotNull(attemptID, "Argument attemptID must not be null");
		checkNotNull(partitionID, "Argument partitionID must not be null");
		checkArgument(consumedSubpartitionIndex >= 0, "Argument consumedSubpartitionIndex must be greater than or equal zero");
		checkArgument(bufferSize > 0, "Argument bufferSize must be greater than zero");

		this.attemptID = attemptID;
		this.partitionID = partitionID;
		this.consumedSubpartitionIndex = consumedSubpartitionIndex;
		this.bufferSize = bufferSize;
	}

	public LimitBufferSizeAction() {
		super();
		this.attemptID = new ExecutionAttemptID();
		this.partitionID = new IntermediateResultPartitionID();
		this.consumedSubpartitionIndex = -1;
		this.bufferSize = 0;
	}

	public ExecutionAttemptID getAttemptID() {
		return attemptID;
	}

	public IntermediateResultPartitionID getPartitionID() {
		return partitionID;
	}

	public int getConsumedSubpartitionIndex() {
		return consumedSubpartitionIndex;
	}

	public int getBufferSize() {
		return this.bufferSize;
	}
}
