
package org.n52.prosecco.web.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import org.junit.Test;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.request.Timespan;
import org.n52.prosecco.web.request.TimespanParser;
import org.n52.prosecco.web.request.TimespanRelation;

public class TimespanParserTest {

	@Test
	public void given_temporalFilterWithValidInstantRange_when_parsingFilterContext_then_correctTimespan()
			throws FilterException {
	    String temporalFilter = "om:phenomenonTime,2012-11-19T13:00:00Z/2012-11-19T14:15:00+01:00";
        TimespanParser timespanParser = new TimespanParser("om:phenomenontime");
        Timespan timespan = timespanParser.parsePhenomenonTime(temporalFilter);

		assertThat(timespan.getStart()).isEqualTo(Instant.parse("2012-11-19T13:00:00Z"));
		assertThat(timespan.getEnd()).isEqualTo(Instant.parse("2012-11-19T13:15:00Z"));
	}
	
	@Test
	public void given_temporalFilterWithLocalOn_when_parsingFilterContext_then_correctTimespan()
			throws FilterException {
	    String temporalFilter = "om:phenomenonTime,2012-11-19";
	    TimespanParser timespanParser = new TimespanParser("om:phenomenontime");
        Timespan timespan = timespanParser.parsePhenomenonTime(temporalFilter);

		assertThat(timespan.getStart()).isEqualTo(LocalDate.parse("2012-11-19"));
		assertThat(timespan.getEnd()).isEqualTo(LocalDate.parse("2012-11-19"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void given_tempFilterWithInvalidRelation_when_parsingFilterContext_then_exception()
			throws FilterException {
	    String temporalFilter = "om:phenomenonTime,unknown,2012-11-19";
	    TimespanParser timespanParser = new TimespanParser("om:phenomenontime");
        timespanParser.parsePhenomenonTime(temporalFilter);
	}
	
	@Test
	public void given_tempFilterWithValidBefore_when_parsingFilterContext_then_exception()
			throws FilterException {
		String temporalFilter = "om:phenomenonTime,before,2012-11-19";
		TimespanParser timespanParser = new TimespanParser("om:phenomenontime");
        Timespan timespan = timespanParser.parsePhenomenonTime(temporalFilter);
		
		assertThat(timespan.getRelation()).isEqualTo(TimespanRelation.BEFORE);
		assertThat(timespan.getEnd()).isEqualTo(LocalDate.parse("2012-11-19"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void given_queryWithoutOMPhenomenonTimePrefix_when_parsingFilterContext_then_exception()
			throws FilterException {
	    TimespanParser timespanParser = new TimespanParser();
        timespanParser.parsePhenomenonTime("2019-10-01/2019-11-01");
	}

    @Test
    public void given_beforeWithPeriod_when_parsingTimeString_then_beforeNowMinusPeriod() {
        String period = "P7D";
        Instant now = Instant.now();
        Instant end = now.minus(Period.parse(period));
        String restrictionValue = "floating," + period;
        
        Timespan actual = new TimespanParser().parseTimeRestriction(restrictionValue);
        assertThat(actual.getRelation()).isEqualTo(TimespanRelation.AFTER);
        assertThat(Instant.from(actual.getStart())).satisfies(e -> {
            assertThat(e).isNotEqualTo(now);
            assertThat(e).isBefore(end.plus(1, ChronoUnit.SECONDS));
        });
    }
    
    @Test
    public void given_beforeWithInstant_when_parsingTimeString_then_beforeInstant() {
        String date = "2019-10-10T12:00:00Z";
        String restrictionValue = "before," + date;
        
        Timespan actual = new TimespanParser().parseTimeRestriction(restrictionValue);
        assertThat(actual.getRelation()).isEqualTo(TimespanRelation.BEFORE);
        assertThat(Instant.from(actual.getEnd())).satisfies(e -> {
            assertThat(e).isEqualTo(Instant.parse(date));
        });
    }
}
