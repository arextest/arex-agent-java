package io.arex.foundation.serializer;

import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class TimeTestInfo {
    private LocalDateTime localDateTime = LocalDateTime.now();
    private LocalDate localDate = LocalDate.now();
    private LocalTime localTime = LocalTime.now();

    private org.joda.time.LocalDateTime jodaLocalDateTime = org.joda.time.LocalDateTime.now();
    private org.joda.time.LocalDate jodaLocalDate = org.joda.time.LocalDate.now();
    private org.joda.time.LocalTime jodaLocalTime = org.joda.time.LocalTime.now();
    private DateTime dateTime = new DateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-01:00")));

    private GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT-01:00"));
    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-01:00"));
    private XMLGregorianCalendar xmlGregorianCalendar;
    private OffsetDateTime offsetDateTime = OffsetDateTime.now(ZoneId.of("GMT-01:00"));

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    {
        try {
            xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
    }

    private Date date = new Date();
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    private Instant instant = Instant.now();

    private Range range;

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public TimeTestInfo(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }


    public TimeTestInfo() throws DatatypeConfigurationException {
    }

    public XMLGregorianCalendar getXmlGregorianCalendar() {
        return xmlGregorianCalendar;
    }

    public void setXmlGregorianCalendar(XMLGregorianCalendar xmlGregorianCalendar) {
        this.xmlGregorianCalendar = xmlGregorianCalendar;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    public GregorianCalendar getGregorianCalendar() {
        return gregorianCalendar;
    }

    public void setGregorianCalendar(GregorianCalendar gregorianCalendar) {
        this.gregorianCalendar = gregorianCalendar;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public org.joda.time.LocalDateTime getJodaLocalDateTime() {
        return jodaLocalDateTime;
    }

    public void setJodaLocalDateTime(org.joda.time.LocalDateTime jodaLocalDateTime) {
        this.jodaLocalDateTime = jodaLocalDateTime;
    }

    public org.joda.time.LocalDate getJodaLocalDate() {
        return jodaLocalDate;
    }

    public void setJodaLocalDate(org.joda.time.LocalDate jodaLocalDate) {
        this.jodaLocalDate = jodaLocalDate;
    }

    public org.joda.time.LocalTime getJodaLocalTime() {
        return jodaLocalTime;
    }

    public void setJodaLocalTime(org.joda.time.LocalTime jodaLocalTime) {
        this.jodaLocalTime = jodaLocalTime;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }
}
