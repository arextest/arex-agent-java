package io.arex.foundation.serializer;

import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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

public class ZeroSecondTimeTestInfo {
    private LocalDateTime localDateTime;
    private LocalDate localDate;
    private LocalTime localTime;

    private org.joda.time.LocalDateTime jodaLocalDateTime;
    private org.joda.time.LocalDate jodaLocalDate;
    private org.joda.time.LocalTime jodaLocalTime;
    private DateTime dateTime;

    private GregorianCalendar gregorianCalendar;
    private Calendar calendar;
    private XMLGregorianCalendar xmlGregorianCalendar;
    private OffsetDateTime offsetDateTime;
    private TimeZone timeZone1;

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

//    {
//        try {
//            xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
//        } catch (DatatypeConfigurationException e) {
//            e.printStackTrace();
//        }
//    }

    private Date date;
    private Timestamp timestamp;

//    private Instant instant;

    private Range range;

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

//    public Instant getInstant() {
//        return instant;
//    }
//
//    public void setInstant(Instant instant) {
//        this.instant = instant;
//    }

    public ZeroSecondTimeTestInfo(TimeTestInfo timeTestInfo) {
        // zero second timeTestInfo
        this.localDateTime = timeTestInfo.getLocalDateTime().withSecond(0).withNano(0);
        this.localDate = timeTestInfo.getLocalDate();
        this.localTime = timeTestInfo.getLocalTime().withSecond(0).withNano(0);
        this.jodaLocalDateTime = timeTestInfo.getJodaLocalDateTime().withSecondOfMinute(0).withMillisOfSecond(0);
        this.jodaLocalDate = timeTestInfo.getJodaLocalDate();
        this.jodaLocalTime = timeTestInfo.getJodaLocalTime().withSecondOfMinute(0).withMillisOfSecond(0);
        this.dateTime = timeTestInfo.getDateTime().withSecondOfMinute(0).withMillisOfSecond(0);
        this.gregorianCalendar = timeTestInfo.getGregorianCalendar();
        this.gregorianCalendar.set(Calendar.SECOND, 0);
        this.gregorianCalendar.set(Calendar.MILLISECOND, 0);
        this.calendar = timeTestInfo.getCalendar();
        this.calendar.set(Calendar.SECOND, 0);
        this.calendar.set(Calendar.MILLISECOND, 0);
        this.xmlGregorianCalendar = timeTestInfo.getXmlGregorianCalendar();
        this.xmlGregorianCalendar.setSecond(0);
        this.xmlGregorianCalendar.setMillisecond(0);

        // zero second date
        Calendar cal = Calendar.getInstance();
        cal.setTime(timeTestInfo.getDate());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.date = cal.getTime();


        this.offsetDateTime = timeTestInfo.getOffsetDateTime().withSecond(0).withNano(0);
        this.timeZone1 = timeTestInfo.getTimeZone1();

        // zero second timestamp
        this.timestamp = timeTestInfo.getTimestamp();
        this.timestamp.setSeconds(0);
        this.timestamp.setNanos(0);

        // zero second instant
//        LocalDateTime localDateTime = LocalDateTime.ofInstant(timeTestInfo.getInstant(), ZoneId.systemDefault());
//        localDateTime = localDateTime.withSecond(0).withNano(0);
//        this.instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }


    public ZeroSecondTimeTestInfo() throws DatatypeConfigurationException {
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

    public TimeZone getTimeZone1() {
        return timeZone1;
    }

    public void setTimeZone1(TimeZone timeZone1) {
        this.timeZone1 = timeZone1;
    }
}
