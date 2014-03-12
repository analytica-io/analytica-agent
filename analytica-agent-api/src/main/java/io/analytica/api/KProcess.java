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
package io.analytica.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A process is an event with
 * - a category defined by 
 * 		--a type 
 * 		--an array of subTypes	
 * - a start date
 * - a list of sub processes
 * - a duration (cf.measures)
 * - a list of measures  with a DURATION  measure 
 * - a list of metadatas
 * 
 * @author pchretien, npiedeloup
 * @version $Id: KProcess.java,v 1.8 2012/10/16 17:18:26 pchretien Exp $
 */
public final class KProcess {
	/**
	 * Mesure de type dur�e.  
	 */
	public static final String DURATION = "duration";
	/**
	 * Mesure de type dur�e.  
	 */
	public static final String SUB_DURATION = "sub-duration";
	/**
	 * REGEX d�crivant les r�gles du type de process. (exemples : SQL, MAIL, REQUEST)
	 */
	public static final Pattern TYPE_REGEX = Pattern.compile("[A-Z][A-Z0-9_]*");

	private final String type;
	private String[] subTypes = new String[0];
	private final Date startDate;

	private Map<String, Double> measures = new HashMap<String, Double>();
	private Map<String, String> metaDatas = new HashMap<String, String>();
	private List<KProcess> subProcesses = new ArrayList<KProcess>();

	/*
	 * Le constructeur est package car il faut passer par le builder.
	 */
	KProcess(final String type, final String[] subTypes, final Date startDate, final Map<String, Double> measures, final Map<String, String> metaDatas, final List<KProcess> subProcesses) {
		if (type == null) {
			throw new NullPointerException("type of process is required");
		}
		if (subTypes == null) {
			throw new NullPointerException("subTypes of process are required");
		}
		if (!TYPE_REGEX.matcher(type).matches()) {
			throw new IllegalArgumentException("process type must match regex :" + TYPE_REGEX);
		}
		if (!measures.containsKey(DURATION)) {
			throw new IllegalArgumentException("measures must contain DURATION");
		}
		if (measures.containsKey(SUB_DURATION) && measures.get(SUB_DURATION) > measures.get(DURATION)) {
			throw new IllegalArgumentException("measures SUB-DURATION must be lower than DURATION (duration:" + measures.get(DURATION) + " < sub-duration:" + measures.get(SUB_DURATION) + ")");
		}
		//---------------------------------------------------------------------
		this.type = type;
		this.subTypes = subTypes;
		this.startDate = startDate;
		this.measures = Collections.unmodifiableMap(new HashMap<String, Double>(measures));
		this.metaDatas = Collections.unmodifiableMap(new HashMap<String, String>(metaDatas));
		this.subProcesses = subProcesses;
	}

	/**
	 * @return Type du processus
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return Sous-types du processus
	 */
	public String[] getSubTypes() {
		return subTypes!=null?subTypes:new String[0]; //TODO force subTypes notNull
	}

	/**@return Process duration */
	public double getDuration() {
		return measures.get(DURATION);
	}

	public Date getStartDate() {
		return startDate;
	}

	public Map<String, Double> getMeasures() {
		return measures;
	}

	public Map<String, String> getMetaDatas() {
		return metaDatas;
	}

	public List<KProcess> getSubProcesses() {
		return subProcesses;
	}

	@Override
	public String toString() {
		return "process:{category:{ type:" + type + ", subTypes:" + Arrays.asList(getSubTypes()) + "}; startDate:" + startDate + "}";
	}
}
