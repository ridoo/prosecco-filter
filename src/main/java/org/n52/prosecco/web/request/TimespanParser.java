
package org.n52.prosecco.web.request;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TimespanParser {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder().appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)
                                                                                               .appendOptional(DateTimeFormatter.ISO_DATE_TIME)
                                                                                               .appendOptional(DateTimeFormatter.ISO_INSTANT)
                                                                                               .appendOptional(DateTimeFormatter.ISO_DATE)
                                                                                               .toFormatter();
    private final String prefix;
    
    public TimespanParser() {
        this(null);
    }
    
    public TimespanParser(String prefix) {
        this.prefix = prefix != null
                ? prefix
                : "";
    }

    /**
     * @param temporalFilter
     * @return
     * @throws IllegalArgumentException
     * @throws DateTimeException
     */
    public Timespan parsePhenomenonTime(String temporalFilter) {
        if (temporalFilter == null) {
            return null;
        }
        
        if (temporalFilter.toLowerCase().startsWith(prefix)) {
            String[] parts = temporalFilter.split(",");
            String[] partsWithoutPrefix = Arrays.copyOfRange(parts, 1, parts.length);
            return parseTimespan(partsWithoutPrefix);
        }
        
        throw new IllegalArgumentException("Missing om:phenomenonTime prefix");
    }

    public Timespan parseTimeRestriction(String restrictionValue) {
        Objects.requireNonNull(restrictionValue, "restrictionValue is null");
        String[] parts = restrictionValue.split(",");
        String timeRelation = parts[0];
        if ("floating".equalsIgnoreCase(timeRelation)) {
            Instant now = Instant.now();
            TemporalAmount period = Period.parse(parts[1]);
            Instant upperTimeRestriction = now.minus(period);
            return Timespan.after(upperTimeRestriction);
        } else {
            return parseTimespan(parts);
        }
    }

    private Timespan parseTimespan(String[] parts) {
        Objects.requireNonNull(parts, "filter parts are null");
        if (parts.length != 1 && parts.length != 2) {
            throw new IllegalArgumentException("Unknown filter format! Expected either \n"
                    + "\t 1) <time instant>/<time instant>\n "
                    + "\t 2) before,<time instant>\n"
                    + "\t 3) after,<time instant>\n"
                    + "\t 4) <date>\n"
                    + "but was: "
                    + Arrays.stream(parts).collect(Collectors.joining(",")));
        } 
        
        if (parts.length == 1) {
            String value = parts[0];
            if (value.contains("/")) {
                // case <time instant>/<time instant>"
                return parseTimespanWindow(value);
            } else {
                // case <date>
                return Timespan.on(parseTemporalAccessor(value));
            }
        } else {
            // parse {before,after},<date>
            return parseIndeterminedTimespan(parts);
        }
    }

    private Timespan parseTimespanWindow(String value) {
        String[] range = value.split("/");
        TemporalAccessor start = parseTemporalAccessor(range[0]);
        TemporalAccessor end = parseTemporalAccessor(range[1]);
        return Timespan.between(start, end);
    }

    private Timespan parseIndeterminedTimespan(String[] parts) {
        String timeRelation = parts[0];
        TemporalAccessor date = parseTemporalAccessor(parts[1]);
        TimespanRelation relation = TimespanRelation.findValue(timeRelation);
        if (relation == TimespanRelation.AFTER) {
            return Timespan.after(date);
        } else if (relation == TimespanRelation.BEFORE) {
            return Timespan.before(date);
        } else {
            throw new IllegalArgumentException("Expected time relation: 'after' or 'before'");
        }
    }

    private TemporalAccessor parseTemporalAccessor(String timestring) {
        return DATE_TIME_FORMATTER.parseBest(timestring, Instant::from, ZonedDateTime::from, LocalDate::from);
    }

}
