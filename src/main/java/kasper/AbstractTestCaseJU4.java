/**
 * Kasper-kernel - v6 - Simple Java Framework
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidi�re - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kasper;

import kasper.kernel.Home;
import kasper.kernel.Starter;
import kasper.kernel.di.container.Container;
import kasper.kernel.di.injector.Injector;
import kasper.kernel.lang.Option;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Charge l'environnement de test par defaut.
 * @author pchretien
 */
public abstract class AbstractTestCaseJU4 {
	protected final void nop(final Object o) {
		//rien
	}

	@Rule
	public ContainerResource resource = new ContainerResource(this);

	@Before
	public final void setUp() throws Exception {
		doSetUp();
	}

	@After
	public final void tearDown() throws Exception {
		doTearDown();
		doAfterTearDown();
	}

	/**
	 * Initialisation du test pour impl� sp�cifique.
	 * @throws Exception Erreur
	 */
	protected void doSetUp() throws Exception {
		// pour impl� sp�cifique 
	}

	/**
	 * Finalisation du test pour impl� sp�cifique.
	 * @throws Exception Erreur
	 */
	protected void doTearDown() throws Exception {
		// pour impl� sp�cifique 
	}

	/**
	 * Finalisation du test pour impl� sp�cifique.
	 * @throws Exception Erreur
	 */
	protected void doAfterTearDown() throws Exception {
		// pour impl� sp�cifique 
	}

	/**
	 * @return fichier managers.xml (par defaut managers-test.xml)
	 */
	protected String getManagersXmlFileName() {
		return "./managers-test.xml";
	}

	/**
	 * @return fichier properties de param�trage des managers (par defaut Option.none())
	 */
	protected Option<String> getPropertiesFileName() {
		return Option.none(); //par d�faut pas de properties
	}

	/**
	 * JUnitRule repr�sentant la resource de Container.
	 * @author npiedeloup
	 * @version $Id: $
	 */
	static class ContainerResource implements TestRule {

		private final AbstractTestCaseJU4 testCaseInstance;

		/**
		 * Constructeur.
		 * @param testCaseInstance instance du testCase
		 */
		ContainerResource(final AbstractTestCaseJU4 testCaseInstance) {
			this.testCaseInstance = testCaseInstance;
		}

		/** {@inheritDoc} */
		@Override
		public Statement apply(final Statement base, final Description description) {
			return new ContainerStatement(base, testCaseInstance);
		}

		/**
		 * Statement de la resource ContainerResource.
		 * @author npiedeloup
		 * @version $Id: $
		 */
		static class ContainerStatement extends Statement {
			private final AbstractTestCaseJU4 testCaseInstance;
			private final Injector injector = new Injector();
			private Starter starter;

			private final Statement base;

			/**
			 * @param base evaluation du test
			 * @param testCaseInstance instance du testCase
			 */
			public ContainerStatement(final Statement base, final AbstractTestCaseJU4 testCaseInstance) {
				this.base = base;
				this.testCaseInstance = testCaseInstance;
			}

			/** {@inheritDoc} */
			@Override
			public void evaluate() throws Throwable {
				starter = new Starter(testCaseInstance.getManagersXmlFileName(), testCaseInstance.getPropertiesFileName(), testCaseInstance.getClass(), 0L);
				starter.start();

				//On injecte les managers sur la classe de test.
				injector.injectMembers(testCaseInstance, getContainer());
				try {
					base.evaluate();
				} finally {
					starter.stop();
				}
			}

			/**
			 * Fournit le container utilis� pour l'injection.
			 * @return Container de l'injection
			 */
			private Container getContainer() {
				return Home.getContainer().getRootContainer();
			}
		}
	}
}
