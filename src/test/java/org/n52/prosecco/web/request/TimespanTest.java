package org.n52.prosecco.web.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.Test;

public class TimespanTest {

	@Test
	public void given_betweenTimespan_when_toString_then_validOmPhenomenonTime() {
		Instant start = Instant.parse("2019-01-01T12:00:00Z");
		Instant end = Instant.parse("2019-01-10T12:00:00Z");
		Timespan actual = Timespan.between(start, end);
		String expected = start.toString() + "/" + end.toString();
		assertThat(actual.toString()).isEqualTo(expected);
	}
	
	@Test
	public void given_onTimespan_when_toString_then_validOmPhenomenonTime() {
		Timespan actual = Timespan.on(LocalDate.parse("2019-01-01"));
		String expected = Instant.parse("2019-01-01T00:00:00Z").toString();
		assertThat(actual.toString()).isEqualTo(expected);
	}
	
	@Test
	public void given_beforeTimespan_when_toString_then_validOmPhenomenonTime() {
		Timespan actual = Timespan.before(LocalDate.parse("2019-01-01"));
		String expected = "before," + Instant.parse("2019-01-01T00:00:00Z").toString();
		assertThat(actual.toString()).isEqualTo(expected);
	}
	
	@Test
	public void given_afterTimespan_when_toString_then_validOmPhenomenonTime() {
		Timespan actual = Timespan.after(LocalDate.parse("2019-01-01"));
		String expected = "after," + Instant.parse("2019-01-01T00:00:00Z").toString();
		assertThat(actual.toString()).isEqualTo(expected);
	}

    @Test
    public void given_timespanWithOpenStart_when_instantAfterEnd_then_timespanIsBefore() {
        Timespan timespan = Timespan.before(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2019-01-01T01:00:00Z");
        assertThat(timespan.isBefore(pointInTime)).isTrue();
    }
    
    @Test
    public void given_timespanWithOpenStart_when_instantAfterEnd_then_timespanIsNotAfter() {
        Timespan timespan = Timespan.before(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2019-01-01T01:00:00Z");
        assertThat(timespan.isAfter(pointInTime)).isFalse();
    }
    
    @Test
    public void given_timespanWithOpenStart_when_instantIsEnd_then_timespanNeitherBeforeNorAfter() {
        Timespan timespan = Timespan.before(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2019-01-01T00:00:00Z");
        assertThat(timespan.isBefore(pointInTime)).isFalse();
        assertThat(timespan.isAfter(pointInTime)).isFalse();
    }
    
    @Test
    public void given_timespanWithOpenEnd_when_instantIsStart_then_timespanNeitherBeforeNotAfter() {
        Timespan timespan = Timespan.after(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2019-01-01T00:00:00Z");
        assertThat(timespan.isBefore(pointInTime)).isFalse();
        assertThat(timespan.isAfter(pointInTime)).isFalse();
    }
    
	@Test
    public void given_timespanWithOpenEnd_when_instantBeforeStart_then_timespanIsAfter() {
        Timespan actual = Timespan.after(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2018-12-01T01:00:00Z");
        assertThat(actual.isAfter(pointInTime)).isTrue();
    }
	
	@Test
    public void given_timespanWithOpenEnd_when_instantBeforeStart_then_timespanIsNotBefore() {
        Timespan actual = Timespan.after(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2018-12-01T01:00:00Z");
        assertThat(actual.isBefore(pointInTime)).isFalse();
    }
	
	@Test
    public void given_timespanWithClosedInterval_when_instantBeforeStart_then_timespanIsAfter() {
        Timespan actual = createClosedTimespan("2019-01-01", "2019-02-01");
        Instant pointInTime = Instant.parse("2018-12-01T01:00:00Z");
        assertThat(actual.isAfter(pointInTime)).isTrue();
    }
	
	@Test
    public void given_timespanWithClosedInterval_when_instantAfterEnd_then_timespanIsBefore() {
	    Timespan actual = createClosedTimespan("2019-01-01", "2019-02-01");
        Instant pointInTime = Instant.parse("2019-02-01T01:00:00Z");
        assertThat(actual.isBefore(pointInTime)).isTrue();
    }

	@Test
    public void given_timespanWithClosedInterval_when_instantBeforeStart_then_timespanIsNotBefore() {
        Timespan actual = createClosedTimespan("2019-01-01", "2019-02-01");
        Instant pointInTime = Instant.parse("2018-12-01T01:00:00Z");
        assertThat(actual.isBefore(pointInTime)).isFalse();
    }
	
	@Test
    public void given_timespanWithClosedInterval_when_instantAfterEnd_then_timespanIsNotAfter() {
        Timespan actual = createClosedTimespan("2019-01-01", "2019-02-01");
        Instant pointInTime = Instant.parse("2019-02-01T01:00:00Z");
        assertThat(actual.isAfter(pointInTime)).isFalse();
    }
	
	@Test
    public void given_timespanPointInTime_when_instantIsBefore_then_timespanIsAfter() {
        Timespan actual = Timespan.on(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2018-12-01T01:00:00Z");
        assertThat(actual.isAfter(pointInTime)).isTrue();
    }
    
    @Test
    public void given_timespanPointInTime_when_instantIsAfter_then_timespanIsBefore() {
        Timespan actual = Timespan.on(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2019-02-01T01:00:00Z");
        assertThat(actual.isBefore(pointInTime)).isTrue();
    }
	
	@Test
    public void given_timespanPointInTime_when_instantIsAfter_then_timespanIsNotAfter() {
        Timespan actual = Timespan.on(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2019-02-01T01:00:00Z");
        assertThat(actual.isAfter(pointInTime)).isFalse();
    }
	
	@Test
    public void given_timespanPointInTime_when_instantIsBefore_then_timespanIsNotBefore() {
        Timespan actual = Timespan.on(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2018-12-01T01:00:00Z");
        assertThat(actual.isBefore(pointInTime)).isFalse();
    }
	

    @Test
    public void given_timespanPointInTime_when_instantIsBefore_then_notWithin() {
        Timespan actual = Timespan.on(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2018-12-01T01:00:00Z");
        assertThat(actual.isWithin(pointInTime)).isFalse();
    }
    
    @Test
    public void given_timespanPointInTime_when_instantIsAfter_then_notWithin() {
        Timespan actual = Timespan.on(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2019-02-01T01:00:00Z");
        assertThat(actual.isWithin(pointInTime)).isFalse();
    }
    
    @Test
    public void given_timespanPointInTime_when_instantIsSame_then_isWithin() {
        Timespan actual = Timespan.on(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2019-01-01T00:00:00Z");
        assertThat(actual.isWithin(pointInTime)).isTrue();
    }
    
    @Test
    public void given_timespanWithOpenStart_when_instantIsAfter_then_isNotWithin() {
        Timespan actual = Timespan.before(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2019-02-01T00:00:00Z");
        assertThat(actual.isWithin(pointInTime)).isFalse();
    }
    
    @Test
    public void given_timespanWithOpenStart_when_instantIsBeforeEnd_then_isWithin() {
        Timespan actual = Timespan.before(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2018-12-01T00:00:00Z");
        assertThat(actual.isWithin(pointInTime)).isTrue();
    }
    
    @Test
    public void given_timespanWithOpenStart_when_instantIsEnd_then_isWithin() {
        Timespan actual = Timespan.before(LocalDate.parse("2019-01-01"));
        Instant pointInTime = Instant.parse("2019-01-01T00:00:00Z");
        assertThat(actual.isWithin(pointInTime)).isTrue();
    }
    
    @Test
    public void given_timespanWithClosedInterval_when_otherOverlapStart_then_bothOverlap() {
        Timespan timespan = createClosedTimespan("2019-01-01", "2019-02-01");
        Timespan other = createClosedTimespan("2019-01-15", "2019-02-15");

        assertThat(timespan.isOverlapping(other)).isTrue();
        assertThat(other.isOverlapping(timespan)).isTrue();
    }
    
    @Test
    public void given_timespanWithClosedInterval_when_otherOverlapEnd_then_bothOverlap() {
        Timespan timespan = createClosedTimespan("2019-01-01", "2019-02-01");
        Timespan other = createClosedTimespan("2018-12-01", "2019-01-15");

        assertThat(timespan.isOverlapping(other)).isTrue();
        assertThat(other.isOverlapping(timespan)).isTrue();
    }
    
    private Timespan createClosedTimespan(String from, String to) {
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);
        return Timespan.between(start, end);
    }
    
    
	
}
