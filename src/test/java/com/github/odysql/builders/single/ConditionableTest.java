package com.github.odysql.builders.single;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.odysql.models.SQLCondition;
import com.github.odysql.models.SQLParameter;

class ConditionableTest {
    private List<SQLParameter> initialized;
    private List<SQLParameter> toBeAdded;

    private List<SQLParameter> result;

    @BeforeEach
    void init() {
        initialized = new ArrayList<>();
        initialized.add(SQLParameter.of(123));

        toBeAdded = new ArrayList<>();
        toBeAdded.add(SQLParameter.of("abc"));
        toBeAdded.add(SQLParameter.of(456.79));

        result = new ArrayList<>();
        result.add(SQLParameter.of(123));
        result.add(SQLParameter.of("abc"));
        result.add(SQLParameter.of(456.79));
    }

    private static class ConditionableTestImpl implements Conditionable<ConditionableTestImpl> {
        List<SQLParameter> params;

        public ConditionableTestImpl(List<SQLParameter> initialized) {
            this.params = new ArrayList<>(initialized);
        }

        @Override
        public ConditionableTestImpl where(SQLCondition cond) {
            throw new UnsupportedOperationException("Unimplemented method 'where'");
        }

        @Override
        public ConditionableTestImpl param(SQLParameter... arguments) {
            for (SQLParameter arg : arguments) {
                this.params.add(arg);
            }
            return this;
        }
    }

    @Test
    void testFunc() {
        ConditionableTestImpl impl = new ConditionableTestImpl(initialized).param(toBeAdded);
        assertIterableEquals(result, impl.params);

        ConditionableTestImpl impl2 = new ConditionableTestImpl(initialized).param(toBeAdded);
        assertIterableEquals(result, impl2.params);
    }
}
