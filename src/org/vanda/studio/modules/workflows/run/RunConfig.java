package org.vanda.studio.modules.workflows.run;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import org.vanda.workflows.hyper.Job;

/**
 * Stores all information that is relevant for the execution system. 
 * @author kgebhardt
 *
 */
public class RunConfig {
	private final String path;
	private final Date date;
	private final Map<String, Integer> jobPriorities;
	
	public RunConfig(String path, Map<String, Integer> jobPriorities) {
		this.path = path;
		this.date = new Date();
		this.jobPriorities = jobPriorities;
	}
	
	public String getPath() {
		return path;
	}
	
	public Date getDate() {
		return date;
	}
	
	public Map<String, Integer> getJobPriorities() {
		return jobPriorities;
	}
	
	/**
	 * @return a Comperator that is used by the TopSorter to implement Job priorities
	 */
	public Comparator<Job> generateComperator() {
		return new Comparator<Job>() {

			@Override
			public int compare(Job arg0, Job arg1) {
				if (arg0.getId() == null && arg1.getId() == null) {
					return arg0.hashCode() - arg1.hashCode();
				} else if (arg0.getId() == null) {
					return -1;
				} else if (arg1.getId() == null) {
					return 1;
				} else {
					int diff = jobPriorities.get(arg0.getId()) - jobPriorities.get(arg1.getId());
					if (diff != 0)
						return diff;
					else return arg0.hashCode() - arg1.hashCode();
				}
			}
		};
		
	}
}
