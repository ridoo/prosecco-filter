
package org.n52.prosecco.engine.filter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

public final class Timespan {

    // TODO use Instants only and implement Comparable<Timespan>

    public static Timespan before(TemporalAccessor instant) {
        return new Timespan(TimespanRelation.BEFORE, null, instant);
    }

    public static Timespan after(TemporalAccessor instant) {
        return new Timespan(TimespanRelation.AFTER, instant, null);
    }

    public static Timespan on(TemporalAccessor instant) {
        return new Timespan(TimespanRelation.ON, instant, instant);
    }

    public static Timespan between(TemporalAccessor start, TemporalAccessor end) {
        return new Timespan(TimespanRelation.BETWEEN, start, end);
    }

    private final TimespanRelation relation;

    private final TemporalAccessor start;

    private TemporalAccessor end;

    /**
     * Creates a timespan from start and end. Depending on the {@code relation} either {@code start} or
     * {@code end} may be {@code null}.
     * 
     * @param relation
     *        relation of given time instant(s)
     * @param start
     *        the start time instant (may be {@code null})
     * @param end
     *        the end time instant (may be {@code null})
     */
    private Timespan(TimespanRelation relation, TemporalAccessor start, TemporalAccessor end) {
        Objects.requireNonNull(relation, "relation is null");
        this.relation = relation;
        this.start = start;
        this.end = end;
    }

    public TimespanRelation getRelation() {
        return relation;
    }

    public TemporalAccessor getStart() {
        return start;
    }

    public TemporalAccessor getEnd() {
        return end;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("om:phenomenonTime,");
        switch (relation) {
        case ON:
            sb.append(start.toString());
            return sb.toString();
        case BETWEEN:
            sb.append(start.toString())
              .append("/")
              .append(end.toString());
            return sb.toString();
        case BEFORE:
            sb.append("before,")
              .append(end.toString());
            return sb.toString();
        case AFTER:
            sb.append("after,")
              .append(start.toString());
            return sb.toString();
        default:
            return "";
        }
    }

    public boolean isBefore(TemporalAccessor pointInTime) {
        Instant pit = asInstant(pointInTime);
        if (relation == TimespanRelation.BETWEEN) {
            return asInstant(end).isBefore(pit);
        }
        if (relation == TimespanRelation.ON) {
            return asInstant(start).isBefore(pit);
        }
        if (relation == TimespanRelation.BEFORE) {
            return asInstant(end).isBefore(pit);
        }

        // open end
        return false;
    }

    public boolean isAfter(TemporalAccessor pointInTime) {
        Instant pit = asInstant(pointInTime);
        if (relation == TimespanRelation.BETWEEN) {
            return asInstant(start).isAfter(pit);
        }
        if (relation == TimespanRelation.ON) {
            return asInstant(start).isAfter(pit);
        }
        if (relation == TimespanRelation.AFTER) {
            return asInstant(start).isAfter(pit);
        }

        // open start
        return false;
    }
    
    public boolean isWithin(TemporalAccessor pointInTime) {
        Instant pit = asInstant(pointInTime);
        if (relation == TimespanRelation.BETWEEN) {
            return asInstant(start).isBefore(pit)
                    && asInstant(end).isAfter(pit);
        }
        if (relation == TimespanRelation.ON) {
            return asInstant(start).equals(pit);
        }
        if (relation == TimespanRelation.AFTER) {
            Instant intervalStart = asInstant(start);
            return intervalStart.isBefore(pit)
                    || intervalStart.equals(pit);
        }
        if (relation == TimespanRelation.BEFORE) {
            Instant intervalEnd = asInstant(end);
            return intervalEnd.isAfter(pit)
                    || intervalEnd.equals(pit);
        }

        throw new IllegalStateException("Unknown time relation: " + relation);
    }
    
    public boolean isOverlapping(Timespan other) {
        if (relation == TimespanRelation.BETWEEN) {
            return other.isWithin(asInstant(start))
                    || other.isWithin(asInstant(end));
        }
        if (relation == TimespanRelation.ON) {
            return other.isWithin(asInstant(start));
        }
        if (relation == TimespanRelation.AFTER) {
            Instant intervalStart = asInstant(start);
            Instant othersEnd = asInstant(other.getEnd());
            return intervalStart.isBefore(othersEnd);
        }
        if (relation == TimespanRelation.BEFORE) {
            Instant intervalEnd = asInstant(end);
            Instant othersStart = asInstant(other.getStart());
            return intervalEnd.isAfter(othersStart);
        }

        throw new IllegalStateException("Unknown time relation: " + relation);
    }

    private Instant asInstant(TemporalAccessor temporalAccessor) {
        if (temporalAccessor instanceof Instant) {
            return Instant.from(temporalAccessor);
        } else if (temporalAccessor instanceof LocalDate) {
            return LocalDate.from(temporalAccessor)
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC);
        } else if (temporalAccessor instanceof ZonedDateTime) {
            return ZonedDateTime.from(temporalAccessor)
                                .toInstant();
        }

        throw new IllegalArgumentException("No conversion from " + temporalAccessor + " to Instant.");
    }

}
