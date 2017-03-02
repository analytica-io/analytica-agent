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
package io.analytica.museum;

import io.analytica.api.Assertion;
import io.analytica.api.AProcess;
import io.analytica.api.KProcessBuilder;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author statchum
 */
public final class Museum {
	public static final String APP_NAME = "museum";
	private static final String QOS = "qos";
	private static final String HEALTH = "health";
	public static final String APP_LOCATION = "cloudNine";
	private final PageListener pageListener;

	private long pages = 0;

	public Museum(final PageListener pageListener) {
		Assertion.checkNotNull(pageListener);
		// ---------------------------------------------------------------------
		this.pageListener = pageListener;
	}

	public void load(final int days, final int visitsByDay) {
		Assertion.checkArgument(days >= 0, "days must be >= 0");
		//---------------------------------------------------------------------
		//Toutes les visites sur 3h, 100visites par heures
		final Calendar today = new GregorianCalendar();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		final Date startDate = new DateBuilder(today.getTime()).build();
		System.out.println("=============");
		System.out.println("=====days :" + days);
		System.out.println("=====visitsByDay :" + visitsByDay);
		System.out.println("=============");

		final long start = System.currentTimeMillis();
		for (int day = 0; day < days; day++) {
			final Date visitDate = new DateBuilder(startDate).addDays(-day).build();
			final Calendar calendar = new GregorianCalendar();
			calendar.setTime(visitDate);
			try {
				loadVisitors(visitDate, StatsUtil.random(visitsByDay, Activity.getCoefPerDay(calendar.get(Calendar.DAY_OF_WEEK))));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println();
		System.out.println("data loaded in " + (System.currentTimeMillis() - start) / 1000 + "seconds");
		System.out.println("=============");

	}

	public void constantLoad(final double visitsByDay) throws InterruptedException{
		while (true){
			Date currentDate = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDate);
			cal.add(Calendar.HOUR, -1);
			Date oneHourBack = cal.getTime();
			loadVisitors( currentDate,visitsByDay, true);
		}
		
		
	}
	
	private void loadVisitors(final Date startDate, final double visitsByDay) throws InterruptedException{
		loadVisitors(startDate,visitsByDay,false);
	}
	
	private void loadVisitors(final Date startDate, final double visitsByDay, final boolean sleep) throws InterruptedException {
		System.out.println("\n===== add " + visitsByDay + " at " + startDate);
		double visitRatioSum = 0;
		final double[] visitRatioPerHour = new double[24];
		for (int h = 0; h < 24; h++) {
			visitRatioPerHour[h] = Math.round(StatsUtil.random(visitsByDay, Activity.getCoefPerHour(h)));
			visitRatioSum += visitRatioPerHour[h];
		}
		final double visitPerHourRatio = visitsByDay / visitRatioSum;

		for (int h = 0; h < 24; h++) {
			loadVisitorPerHour(h, visitPerHourRatio, visitRatioPerHour, startDate,sleep);	
		}
	}
	
		private void loadVisitorPerHour(final int h,final double visitPerHourRatio, final double []visitRatioPerHour, final Date startDate, final boolean sleep) throws InterruptedException{
			int index = h;
			if (sleep){
				Date date = new Date();   // given date
				Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
				calendar.setTime(date);   
				index =calendar.get(Calendar.HOUR_OF_DAY);
			}
			final int nbHourVisit = (int) Math.round(visitRatioPerHour[index] * visitPerHourRatio);
			System.out.print(h + "h:" + nbHourVisit + ", ");
			final long timeToSleep = 60*60*1000L /nbHourVisit;
			final Date dateHour = new DateBuilder(startDate).addHours(h).toDateTime();
			for (int i = 0; i < nbHourVisit; i++) {
				final int defaultMinute = (int) Math.round((i + 1) * 60d / (nbHourVisit + 1));
				final int randomMinute = (int) (defaultMinute + (4 * 5 - StatsUtil.random(4 * 5, 1))); //default +/- 5 minutes (on joue sur le battement de 20%)
				final Date startVisit = new DateBuilder(dateHour).addMinutes(randomMinute).toDateTime();
				addVisitorScenario(startVisit);
				Thread.sleep(timeToSleep);
			}
			loadHealthInfos(dateHour, nbHourVisit);
			loadQOS(dateHour, nbHourVisit);
		}
	
	private void addVisitorScenario(final Date startVisit) {
		//System.out.println("scenario [" + startVisit.getDay() + ", " + startVisit.getHours() + "] >>" + startVisit);
		//On ne CODE pas un scenario, on le d�clare.
		final AProcess visiteur = new KProcessBuilder(APP_NAME, "session", startVisit, 0)
				.withLocation(Museum.APP_LOCATION).withCategory("health")
				.setMeasure("sessionHttp", 1) //1 sessio
				.build();
		//On notifie le listener
		pageListener.onPage(visiteur);

		addPages(startVisit,
				Pages.HOME,
				Pages.ARTIST_SEARCH,
				Pages.ARTIST,
				Pages.IMAGE_ARTIST,
				Pages.ARTIST,
				Pages.IMAGE_ARTIST,
				Pages.ARTIST,
				Pages.IMAGE_ARTIST,
				Pages.OEUVRE,
				Pages.IMAGE_OEUVRE,
				Pages.ARTIST,
				Pages.IMAGE_ARTIST,
				Pages.EXPOSITION,
				Pages.EXPOSITION,
				Pages.OEUVRE,
				Pages.IMAGE_OEUVRE,
				Pages.OEUVRE,
				Pages.IMAGE_OEUVRE,
				Pages.OEUVRE,
				Pages.IMAGE_OEUVRE,
				Pages.OEUVRE,
				Pages.IMAGE_OEUVRE,
				Pages.ARTIST_SEARCH,
				Pages.ARTIST,
				Pages.IMAGE_ARTIST,
				Pages.ARTIST,
				Pages.IMAGE_ARTIST,
				Pages.OEUVRE,
				Pages.IMAGE_OEUVRE,
				Pages.ARTIST,
				Pages.IMAGE_ARTIST,
				Pages.OEUVRE,
				Pages.IMAGE_OEUVRE,
				Pages.OEUVRE_SEARCH,
				Pages.OEUVRE,
				Pages.IMAGE_OEUVRE,
				Pages.OEUVRE,
				Pages.IMAGE_OEUVRE,
				Pages.OEUVRE,
				Pages.IMAGE_OEUVRE,
				Pages.OEUVRE,
				Pages.IMAGE_OEUVRE,
				Pages.EXPOSITION,
				Pages.ARTIST,
				Pages.IMAGE_ARTIST);
	}

	private void addPages(final Date startVisit, final PageBuilder... pageBuilders) {
		Date startDate = startVisit;
		int pagesVue = 0;
		final int pagesAVoir = (int) StatsUtil.random(pageBuilders.length, 1); //en moyen on voie le max mais 20% voient moins
		for (final PageBuilder pageBuilder : pageBuilders) {
			startDate = addPage(pageBuilder, startDate);
			if (++pagesVue > pagesAVoir) {
				break;
			}
		}
	}

	private Date addPage(final PageBuilder pageBuilder, final Date startDate) {
		final AProcess page = pageBuilder.createPage(startDate);
		//On notifie le listener
		pageListener.onPage(page);
		pages++;
		return addWaitTime(startDate, pageBuilder);
	}

	private static Date addWaitTime(final Date startVisit, final PageBuilder pageBuilder) {
		final long waitTime;
		if (pageBuilder == Pages.IMAGE_ARTIST || pageBuilder == Pages.IMAGE_OEUVRE) {
			waitTime = 100;
		} else {
			waitTime = 30 * 1000;//30s
		}
		return new Date(startVisit.getTime() + StatsUtil.random(waitTime, 1));
	}

	private void loadQOS(final Date dateHour, final double nbVisitsHour) {
		final double activity = Math.min(100, StatsUtil.random(100, nbVisitsHour / 50));
		final double perfs = Math.min(100, StatsUtil.random(100, 1.4 - nbVisitsHour / 50));
		final double health = Math.min(100, StatsUtil.random(100, 1.5 - nbVisitsHour / 50));

		final AProcess qosProcess = new KProcessBuilder(APP_NAME, QOS, dateHour, 0)
		.withLocation(Museum.APP_LOCATION).withCategory(QOS)
				.setMeasure("activity", activity)
				.setMeasure("activityMax", 100)
				.setMeasure("performance", perfs)
				.setMeasure("performanceMax", 100)
				.setMeasure("health", health)
				.setMeasure("healthMax", 100)
				.build();
		pageListener.onPage(qosProcess);
	}

	private void loadHealthInfos(final Date dateHour, final double nbVisitsHour) {
		for (int min = 0; min < 60; min ++) {
			final Date dateMinute = new DateBuilder(dateHour).addMinutes(min).toDateTime();
			final AProcess healthProcess = new KProcessBuilder(APP_NAME, HEALTH, dateMinute, 0)
					.withLocation(Museum.APP_LOCATION).withCategory("physical")
			.setMeasure("cpu", Math.min(100, 5 + (nbVisitsHour > 0 ? StatsUtil.random(nbVisitsHour, 1) : 0)))
			.setMeasure("ram", Math.min(3096, 250 + (nbVisitsHour > 0 ? StatsUtil.random(nbVisitsHour, 10) : 0)))
			.setMeasure("io", 10 + (nbVisitsHour > 0 ? StatsUtil.random(nbVisitsHour, 5) : 0))
			.build();
			pageListener.onPage(healthProcess);
		}
	}
}
