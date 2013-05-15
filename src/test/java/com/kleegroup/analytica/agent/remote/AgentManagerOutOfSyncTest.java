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
package com.kleegroup.analytica.agent.remote;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;

import com.kleegroup.analytica.agent.AbstractAgentManagerTest;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * Cas de Test JUNIT de l'API Analytics.
 * Dans le cas ou le serveur n'est pas toujours joignable.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerOutOfSyncTest.java,v 1.2 2012/06/14 13:52:26 npiedeloup Exp $
 */
public final class AgentManagerOutOfSyncTest extends AbstractAgentManagerTest {
	/** Logger. */
	private final Logger log = Logger.getLogger(getClass());

	private HttpServer httpServer;

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(9998).build();
	}

	public static final URI BASE_URI = getBaseURI();

	protected static HttpServer startServer() throws IOException {
		System.out.println("Starting grizzly...");
		final ResourceConfig rc = new PackagesResourceConfig("com.kleegroup.analyticaimpl.server.plugins.api.rest");
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
	}

	/**
		 * Initialisation du test pour impl� sp�cifique.
		 * @throws Exception Erreur
		 */
	@Override
	protected void doSetUp() throws Exception {
		//on ne d�marre pas le serveur
	}

	/**
	 * Finalisation du test pour impl� sp�cifique.
	 * @throws Exception Erreur
	 */
	@Override
	protected void doTearDown() throws Exception {
		if (httpServer != null) { //!=null pour les tests qui n'ont pas fait de flushAgentToServer
			httpServer.stop();
		}

	}

	/** {@inheritDoc} */
	@Override
	protected void flushAgentToServer() {
		try {
			Thread.sleep(5000);//on attend 5s que le process soit conserv� cot� client.
			try {
				httpServer = startServer();
				System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl", BASE_URI));
			} catch (final IOException e1) {
				throw new RuntimeException("Impossible de lancer le server jersey");
			}
			Thread.sleep(2000);//on attend 2s que le process soit envoy� au serveur.
		} catch (final InterruptedException e) {
			//rien on stop juste l'attente
		}
	}
}
