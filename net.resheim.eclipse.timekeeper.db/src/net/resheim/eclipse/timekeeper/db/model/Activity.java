/*******************************************************************************
 * Copyright © 2016-2020 Torkild U. Resheim
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package net.resheim.eclipse.timekeeper.db.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.eclipse.persistence.annotations.UuidGenerator;

import net.resheim.eclipse.timekeeper.db.converters.LocalDateTimeAttributeConverter;

/**
 * The {@link Activity} type represents a period of work on a task. It holds the
 * start time, and the stop time. An activity can stretch over several days,
 * however that should typically not be the case. Multiple activities can be
 * assign to the same {@link TrackedTask}, on the same day.
 * 
 * TODO: Consolidate activities so they never span dates. It will make several
 * operations much easier.
 * 
 * @author Torkild U. Resheim
 */
@Entity(name = "ACTIVITY")
@UuidGenerator(name = "uuid")
public class Activity implements Comparable<Activity>, Serializable {

	private static final long serialVersionUID = 7770745026684660897L;

	@Id
	@GeneratedValue(generator = "uuid")
	@Column(name = "ID")
	private String id;

	/** The time the activity was started */
	@Column(name = "START_TIME")
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime start = null;

	/** The time the activity was stopped */
	@Column(name = "END_TIME")
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime end = null;

	/** Whether or not activity properties have been manually adjusted or created */
	@Column(name = "ADJUSTED")
	private boolean manual = false;

	/** The task the activity is associated with */
	@ManyToOne
	private TrackedTask trackedtask;

	/** The project this activity is associated with, if not associated with a tracked task */
	@ManyToOne
	@JoinColumn(name = "PROJECT")
	private Project project;

	/** A short summary of the activity */
	@Column
	private String summary;

	public Activity() {
	}

	public Activity(TrackedTask trackedtask, LocalDateTime start) {
		this.trackedtask = trackedtask;
		this.start = start;
		StringBuilder sb = new StringBuilder();
		sb.append("Activity started on ");
		sb.append(getStart().format(DateTimeFormatter.ISO_LOCAL_DATE));
		sb.append(" at ");
		sb.append(getStart().format(DateTimeFormatter.ofPattern("HH:mm")));
		summary = sb.toString();
	}

	/**
	 * Returns the total duration of work.
	 * 
	 * @return the duration of work
	 */
	public Duration getDuration() {
		return Duration.between(getStart(), getEnd());
	}

	/**
	 * Returns the duration of work on the given date if any.
	 * 
	 * @param date the date to calculate for
	 * @return the amount of work occuring on the given date
	 */
	public Duration getDuration(LocalDate date) {
		return getDuration(date, date.plusDays(1));
	}

	/**
	 * Returns the duration of the activity between the given dates.
	 * 
	 * @param start the start date
	 * @param end   the end date
	 * @return the duration of work between the two days
	 */
	public Duration getDuration(LocalDate start, LocalDate end) {
		LocalDateTime min = LocalDateTime.of(start, LocalTime.MIN);
		LocalDateTime max = LocalDateTime.of(end, LocalTime.MIN);

		LocalDateTime s = getStart();
		LocalDateTime e = getEnd();

		// end time has not been specified so the task must be currently
		// active, using current time as the end date.
		if (e == null) {
			e = LocalDateTime.now();
		}

		if (s.isAfter(max) || e.isBefore(min)) {
			return Duration.ZERO;
		}

		Duration d = Duration.between(s, e);
		if (s.isBefore(min)) {
			d = d.minus(Duration.between(s, min));
		}
		if (e.isAfter(max)) {
			d = d.minus(Duration.between(max, e));
		}
		return d;
	}

	/**
	 * Specifies the duration of the activity by modifying the end time. Also sets
	 * the <i>manual</i> flag indicating that this activity was manually created or
	 * edited.
	 * 
	 * @param duration the duration
	 */
	public void setDuration(Duration duration) {
		end = start.plus(duration);
		manual = true;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setEnd(LocalDateTime end) {
		this.end = end;
	}

	public void setStart(LocalDateTime start) {
		this.start = start;
	}

	public boolean isEdited() {
		return manual;
	}

	public TrackedTask getTrackedTask() {
		return trackedtask;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Override
	public int compareTo(Activity o) {
		return this.getStart().compareTo(o.getStart());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (manual ? 1231 : 1237);
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((summary == null) ? 0 : summary.hashCode());
		result = prime * result + ((trackedtask == null) ? 0 : trackedtask.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Activity other = (Activity) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (manual != other.manual)
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (summary == null) {
			if (other.summary != null)
				return false;
		} else if (!summary.equals(other.summary))
			return false;
		if (trackedtask == null) {
			if (other.trackedtask != null)
				return false;
		} else if (!trackedtask.equals(other.trackedtask))
			return false;
		return true;
	}
	
}
