/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.statistics;

import org.apache.flink.core.io.IOReadableWritable;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.runtime.statistics.CustomStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SpecialStatistic extends CustomStatistic implements IOReadableWritable {
	private static final Logger LOG = LoggerFactory.getLogger(SpecialStatistic.class);
	private String test;

	public SpecialStatistic(String test) {
		this.test = test;
	}

	@Override
	public String toString() {
		return "special report = " + test;
	}

	@Override
	public void write(DataOutputView out) throws IOException {
		LOG.error("Writing object!");
		out.writeUTF(this.test);
	}

	@Override
	public void read(DataInputView in) throws IOException {
		LOG.error("Reading object!");
		this.test = in.readUTF();
	}
}
