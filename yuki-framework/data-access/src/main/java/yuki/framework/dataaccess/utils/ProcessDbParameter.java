package yuki.framework.dataaccess.utils;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;

import yuki.framework.dataaccess.annotations.Parameter;

public class ProcessDbParameter {

    public static Boolean parse(Parameter parameterDef, Boolean val) {
        return val;
    }

    public static Integer parse(Parameter parameterDef, Integer val) {
        return val;
    }


    public static Double parse(Parameter parameterDef, Double val) {
        return val;
    }

    public static Temporal parse(Parameter parameterDef, Instant val) {
        if (val == null) {
            return null;
        }

        switch (parameterDef.value()) {
            case TIME:
                return LocalTime.from(val.atOffset(ZoneOffset.UTC));
            case DATE:
                return LocalDate.from(val.atOffset(ZoneOffset.UTC));
            case DATETIME:
                return LocalDateTime.from(val.atOffset(ZoneOffset.UTC));
            default:
                throw new ClassCastException(String.format("yuki: Wrong type: %s", parameterDef.value()));
        }

    }

    public static LocalTime parse(Parameter parameterDef, LocalTime val) {
        return val;
    }

    public static LocalDate parse(Parameter parameterDef, LocalDate val) {
        return val;
    }

    public static LocalDateTime parse(Parameter parameterDef, LocalDateTime val) {
        return val;
    }

    public static String parse(Parameter parameterDef, String val) {
        if (val == null) {
            return null;
        }

        if (parameterDef.length() == Integer.MIN_VALUE) {
            return val;
        }

        switch (parameterDef.value()) {
            case STRING:
                return val.substring(0, Math.min(parameterDef.length(), val.length() - 1));
            default:
                throw new ClassCastException(String.format("Wrong type: %s", parameterDef.value()));
        }
    }

    public static Byte[] parse(Parameter parameterDef, InputStream val) {
        throw new RuntimeException(new NoSuchMethodException(""));
    }
}
