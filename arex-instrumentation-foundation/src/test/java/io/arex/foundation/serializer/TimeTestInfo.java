package io.arex.foundation.serializer;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT-01:00"));
    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-01:00"));
    private XMLGregorianCalendar xmlGregorianCalendar;

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

}
