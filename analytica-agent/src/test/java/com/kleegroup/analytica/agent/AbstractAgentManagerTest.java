/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidi�re - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
 */
package com.kleegroup.analytica.agent;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import kasper.AbstractTestCaseJU4;
import kasper.kernel.util.Assertion;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.kleegroup.analytica.hcube.cube.HCube;
import com.kleegroup.analytica.hcube.cube.HMetric;
import com.kleegroup.analytica.hcube.cube.HMetricKey;
import com.kleegroup.analytica.hcube.dimension.HCategory;
import com.kleegroup.analytica.hcube.dimension.HTimeDimension;
import com.kleegroup.analytica.hcube.query.HQuery;
import com.kleegroup.analytica.hcube.query.HQueryBuilder;
import com.kleegroup.analytica.server.ServerManager;

/**
 * Cas de Test JUNIT de l'API Analytics.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerTest.java,v 1.3 2012/03/29 08:48:19 npiedeloup Exp $
 */
public abstract class AbstractAgentManagerTest extends AbstractTestCaseJU4 {

	/** Base de donn�es g�rant les articles envoy�s dans une commande. */
	private static final String PROCESS1_TYPE = "ARTICLE";
	private static final String PROCESS2_TYPE = "COMMANDE";

	/** Logger. */
	private final Logger log = Logger.getLogger(getClass());

	@Inject
	private AgentManager agentManager;
	@Inject
	private ServerManager serverManager;

	//-------------------------------------------------------------------------

	/**
	 * Test simple avec un compteur. 
	 * Test sur l'envoi d'un process 
	 * Chaque article coute 10�.
	 */
	@Test
	public void testOneProcess() {
		agentManager.startProcess(PROCESS1_TYPE, "1 Article 25 Kg");
		agentManager.setMeasure("POIDS", 25);
		agentManager.incMeasure("MONTANT", 10);
		agentManager.stopProcess();
		flushAgentToServer();
		checkMetricCount("MONTANT", 1, PROCESS1_TYPE);
	}

	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10�.
	 */
	@Test
	public void test1000Articles() {
		doNArticles(1000);
		flushAgentToServer();
		checkMetricCount("MONTANT", 1000, PROCESS1_TYPE);
	}

	/**
	 * Test pour v�rifier que l'on peut se passer des processus si et seulement si le mode Analytics est d�sactiv�.
	 */
	@Test
	public void testNoProcess() {
		try {
			agentManager.setMeasure("POIDS", 25);
			Assert.fail();
		} catch (final Exception e) {
			// Ce cas de test est r�ussi s'il remonte une exception
			// OK
			Assert.assertTrue(true);
		}
	}

	/**
	 * Test de r�cursivit�. 
	 * Test sur l'envoi de 500 commandes contenant chacune 500 articles d'un poids de 25 kg. 
	 * Chaque article coute 10�. 
	 * Les frais d'envoi sont de 5�.
	 */
	@Test
	public void test500Commandes() {
		final long start = System.currentTimeMillis();
		doNCommande(5, 15);
		log.trace("elapsed = " + (System.currentTimeMillis() - start));
		flushAgentToServer();
		checkMetricCount("MONTANT", 5, PROCESS2_TYPE); //nombre de commande
		checkMetricCount("MONTANT", 5 * 15, PROCESS1_TYPE); //nombre d'article
	}

	/**
	 * Test de parall�lisme. 
	 * Test sur l'envoi de 500 commandes contenant chacune 1000 articles d'un poids de 25 kg.
	 * L'envoi est simuler avec 20 clients (thread).
	 * Chaque article coute 10�. 
	 * Les frais d'envoi sont de 5�.
	 * @throws InterruptedException Interruption
	 */
	@Test
	public void testMultiThread() throws InterruptedException {
		final long start = System.currentTimeMillis();
		final ExecutorService workersPool = Executors.newFixedThreadPool(20);
		final long nbCommandes = 200;
		for (int i = 0; i < nbCommandes; i++) {
			workersPool.execute(new CommandeTask(String.valueOf(i), 5));
		}
		workersPool.shutdown();
		workersPool.awaitTermination(2, TimeUnit.MINUTES); //On laisse 2 minute pour vider la pile   
		Assertion.invariant(workersPool.isTerminated(), "Les threads ne sont pas tous stopp�s");

		log.trace("elapsed = " + (System.currentTimeMillis() - start));

		flushAgentToServer();
		checkMetricCount("MONTANT", nbCommandes, PROCESS2_TYPE); //nombre de commande
		checkMetricCount("MONTANT", nbCommandes * 5, PROCESS1_TYPE); //nombre d'article
	}

	/**
	 * Flush des donn�es conserv�es par l'agent vers le server.
	 */
	protected abstract void flushAgentToServer();

	private HMetric getMetricInTodayCube(final String metricName, final String type, final String... subTypes) {
		final HQuery query = new HQueryBuilder() //
				.on(HTimeDimension.Day).from(new Date()).to(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) //
				.with(type, subTypes) //
				.build();
		final HCategory category = new HCategory(type, subTypes);
		final List<HCube> cubes = serverManager.execute(query).getSerie(category).getCubes();
		Assert.assertFalse("Le cube [" + category + "] n'apparait pas dans les cubes", cubes.isEmpty());
		final HCube firstCube = cubes.get(0);
		final HMetricKey metricKey = new HMetricKey(metricName, false);
		Assert.assertTrue("Le cube [" + firstCube + "] ne contient pas la metric: " + metricName, firstCube.getMetric(metricKey) != null);
		return firstCube.getMetric(metricKey);
	}

	private void checkMetricCount(final String metricName, final long countExpected, final String type, final String... subTypes) {
		final HCategory category = new HCategory(type, subTypes);
		final HMetric metric = getMetricInTodayCube(metricName, type, subTypes);
		Assert.assertEquals("Le cube [" + category + "] n'est pas peupl� correctement", countExpected, metric.getCount(), 0);
	}

	//	private void checkMetricMean(final String metricName, final double meanExpected, final String type, final String... subTypes) {
	//		final HCategory category = new HCategory(type, subTypes);
	//		final HMetric metric = getMetricInTodayCube(metricName, type, subTypes);
	//		Assert.assertEquals("Le cube [" + category + "] n'est pas peupl� correctement", meanExpected, metric.getMean(), 0);
	//	}

	/**
	 * Passe N commandes.
	 * @param nbCommandes Numero de la commande
	 * @param nbArticles Nombre d'article
	 */
	void doNCommande(final int nbCommandes, final int nbArticles) {
		agentManager.startProcess(PROCESS2_TYPE, nbCommandes + " Commandes");
		for (int i = 0; i < nbCommandes; i++) {
			doOneCommande(String.valueOf(i), nbArticles);
		}
		agentManager.stopProcess();
	}

	/**
	 * Passe une commande.
	 * @param numCommande Numero de la commande
	 * @param nbArticles Nombre d'article
	 */
	void doOneCommande(final String numCommande, final int nbArticles) {
		agentManager.startProcess(PROCESS2_TYPE, "1 Commande");
		agentManager.incMeasure("MONTANT", 5);
		agentManager.addMetaData("NUMERO", numCommande);
		doNArticles(nbArticles);
		agentManager.stopProcess();
	}

	/**
	 * Ajoute N articles.
	 * @param nbArticles Nombre d'article
	 */
	void doNArticles(final int nbArticles) {
		agentManager.startProcess(PROCESS1_TYPE, nbArticles + " Articles 25 Kg");
		for (int i = 0; i < nbArticles; i++) {
			agentManager.startProcess(PROCESS1_TYPE, "1 Article 25 Kg");
			agentManager.setMeasure("POIDS", 25);
			agentManager.incMeasure("MONTANT", 10);
			agentManager.stopProcess();
		}
		agentManager.stopProcess();
	}

	private final class CommandeTask implements Runnable {
		private final String numCommande;
		private final int nbArticles;

		public CommandeTask(final String numCommande, final int nbArticles) {
			this.numCommande = numCommande;
			this.nbArticles = nbArticles;
		}

		public void run() {
			doOneCommande(numCommande, nbArticles);
			System.out.println("Finish commande n�" + numCommande);
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				//rien
			}
		}
	}
}