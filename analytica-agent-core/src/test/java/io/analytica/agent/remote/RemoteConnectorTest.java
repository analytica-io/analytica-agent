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
package io.analytica.agent.remote;

import io.analytica.agent.AbstractProcessCollectorTest;

import java.io.IOException;

/**
 * Cas de Test JUNIT de l'API Analytics.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerTest.java,v 1.3 2012/06/14 13:51:57 npiedeloup Exp $
 */
public final class RemoteConnectorTest extends AbstractProcessCollectorTest {

	/**
	 * Initialisation du test pour impl� sp�cifique.
	 * @throws IOException Erreur
	 */
	@Override
	protected void afterSetUp() throws IOException {
		startServer();
	}
}
