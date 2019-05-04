package org.n52.prosecco.filter;

import org.junit.Test;
import org.n52.prosecco.engine.filter.TimespanRelation;

import static org.assertj.core.api.Assertions.assertThat;

public class TimespanRelationTest {
    
    @Test
    public void having_relationStringMixedCase_when_findingRelation_then_gotFound() {
        assertThat(TimespanRelation.findValue("BeTweeN")).isEqualByComparingTo(TimespanRelation.BETWEEN);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void having_unknownRelationString_when_findingRelation_then_exception() {
        assertThat(TimespanRelation.findValue("unknown"));
    }
}
