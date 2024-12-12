package io.arex.agent.compare.handler.log.filterrules;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class TimePrecisionFilter {
    private static AbstractDataProcessor dataProcessor;

    private static DateTimeFormatter parseFormat1 = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart().appendLiteral(' ').optionalEnd()
            .appendOptional(DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS"))
            .appendOptional(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
            .toFormatter();
    private static DateTimeFormatter parseFormat2 = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS"))
            .appendOptional(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
            .toFormatter();
    private static DateTimeFormatter parseFormat3 = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart().appendLiteral('T').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .appendOptional(DateTimeFormatter.ofPattern("HH:mm:ss.SSSXXX"))
            .appendOptional(DateTimeFormatter.ofPattern("HH:mm:ss.SSSZ"))
            .toFormatter();

    static {
        parseFormat1 = parseFormat1.withZone(ZoneId.of("UTC"));
        dataProcessor = new ProcessorChainBuilder()
                .addProcessor(new FirstDataProcessor())
                .addProcessor(new SecondDataProcessor())
                .addProcessor(new ThirdDataProcessor())
                .build();
    }

    public Instant identifyTime(String data) {
        if (data == null || data.length() < 12 || data.length() > 29) {
            return null;
        }

        if ((data.startsWith("0") || data.startsWith("1") || data.startsWith("2"))) {
            Instant baseTime = dataProcessor.process(data);
            if (baseTime == null) {
                return null;
            }
            return baseTime;
        }
        return null;
    }

    private static abstract class AbstractDataProcessor {

        private AbstractDataProcessor nextProcessor;

        public void setNextProcessor(AbstractDataProcessor nextProcessor) {
            this.nextProcessor = nextProcessor;
        }

        public Instant process(String data) {
            Instant date = processData(data);
            if (date != null) {
                return date;
            }
            if (this.nextProcessor == null) {
                return null;
            }
            return this.nextProcessor.process(data);
        }

        protected abstract Instant processData(String data);
    }

    private static class FirstDataProcessor extends AbstractDataProcessor {

        @Override
        protected Instant processData(String data) {

            Instant instant = null;
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(data, parseFormat1);
                instant = zdt.toInstant();
            } catch (Exception e) {
            }
            return instant;
        }
    }

    private static class SecondDataProcessor extends AbstractDataProcessor {

        @Override
        protected Instant processData(String data) {
            Instant instant = null;
            try {
                LocalTime time = LocalTime.parse(data, parseFormat2);
                LocalDate date = LocalDate.ofEpochDay(0);
                LocalDateTime dateTime = LocalDateTime.of(date, time);
                instant = dateTime.toInstant(ZoneOffset.UTC);
            } catch (Exception e) {
            }
            return instant;

        }
    }

    private static class ThirdDataProcessor extends AbstractDataProcessor {

        @Override
        protected Instant processData(String data) {
            Instant instant = null;
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(data, parseFormat3);
                instant = zdt.toInstant();
            } catch (Exception e) {
            }
            return instant;
        }
    }

    private static class ProcessorChainBuilder {

        private AbstractDataProcessor firstProcessor;
        private AbstractDataProcessor lastProcessor;

        public ProcessorChainBuilder addProcessor(AbstractDataProcessor processor) {
            if (firstProcessor == null) {
                firstProcessor = processor;
            } else {
                lastProcessor.setNextProcessor(processor);
            }
            lastProcessor = processor;
            return this;
        }

        public AbstractDataProcessor build() {
            return firstProcessor;
        }
    }
}
