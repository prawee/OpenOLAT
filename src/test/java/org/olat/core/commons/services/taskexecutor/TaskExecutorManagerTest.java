/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.taskexecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskExecutorManagerTest extends OlatTestCase {
	
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	
	@Test
	public void testRunTask() {
		final CountDownLatch finishCount = new CountDownLatch(1);
		taskExecutorManager.execute(new DummyTask(finishCount));
		
		try {
			boolean zero = finishCount.await(10, TimeUnit.SECONDS);
			Assert.assertTrue(zero);
		} catch (InterruptedException e) {
			Assert.fail("Takes too long (more than 10sec)");
		}
	}
	
	public static class DummyTask implements Runnable {
		
		private final CountDownLatch finishCount;
		
		public DummyTask(CountDownLatch finishCount) {
			this.finishCount = finishCount;
		}
		
		@Override
		public void run() {
			finishCount.countDown();
		}
	}
}
