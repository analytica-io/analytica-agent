/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you permission to link this library
 * with independent modules to produce an executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the license of that module.
 * An independent module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version of the library,
 * but you are not obliged to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
package io.analytica.api;

import io.analytica.agent.api.KProcessCollector;
import io.analytica.agent.api.KProcessConnector;

import org.junit.Assert;
import org.junit.Test;

public final class KProcessCollectorTest {
	private static KProcessCollector createProcessCollector(final boolean containsSubProcess) {
		final String location = "mexico";
		return new KProcessCollector("my-pretty-app", location, new KProcessConnector() {
			public void add(final KProcess process) {
				checkProcess(process);
				if (containsSubProcess) {
					checkSubProcess(process);
				}
				//on vérifie que l'encodage json fonctionne correctement
				final String sprocess = KProcessUtil.toJson(process);
				final KProcess process2 = KProcessUtil.fromJson(sprocess);
				checkProcess(process2);
				if (containsSubProcess) {
					checkSubProcess(process2);
				}
			}

			public void start() {
				// TODO Auto-generated method stub

			}

			public void stop() {
				// TODO Auto-generated method stub

			}
		});
	}

	@Test
	public void testSimpleProcess() {
		createProcessCollector(false)
				.startProcess("pages", "search/items")
				.incMeasure("beats", 10)
				.incMeasure("beats", 5)
				.incMeasure("beats", 1)
				.setMeasure("mails", 22)
				.addMetaData("tags", "fast")
				.stopProcess();
	}

	@Test
	public void testComplexProcess() {
		createProcessCollector(true)
				.startProcess("pages", "search/items")
				.incMeasure("beats", 10)
				.incMeasure("beats", 5)
				.incMeasure("beats", 1)
				.startProcess("services", "getItems")
				.incMeasure("rows", 55)//subprocess
				.stopProcess()
				.setMeasure("mails", 22)
				.addMetaData("tags", "fast")
				.stopProcess();
	}

	private static void checkSubProcess(final KProcess process) {
		final KProcess subProcess = process.getSubProcesses().get(0);
		Assert.assertEquals("my-pretty-app", subProcess.getAppName());
		Assert.assertEquals("services", subProcess.getType());
		Assert.assertEquals(Double.valueOf(55d), subProcess.getMeasures().get("rows"));
	}

	private static void checkProcess(final KProcess process) {
		Assert.assertEquals("my-pretty-app", process.getAppName());
		Assert.assertEquals("pages", process.getType());
		Assert.assertEquals("search/items", process.getCategory());
		Assert.assertEquals("mexico", process.getLocation());
		Assert.assertEquals(Double.valueOf(16d), process.getMeasures().get("beats"));
		Assert.assertEquals("fast", process.getMetaDatas().get("tags"));
	}

}
