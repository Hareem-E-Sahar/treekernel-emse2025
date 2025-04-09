package org.ibit.avanthotel.end.web.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @created 7 de octubre de 2003
 */
public class DateUtil {

    private Date data;

    private int dia;

    private int mes;

    private int any;

    private Calendar calendari;

    /**Construeix una nova instancia de DateUtil */
    public DateUtil() {
        calendari = new GregorianCalendar();
    }

    /**
    *Construeix una nova instancia de DateUtil
    *
    * @param data
    */
    public DateUtil(Date data) {
        this();
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        try {
            this.data = df.parse(df.format(data));
        } catch (ParseException e) {
        }
        calendari.setTime(data);
        this.dia = calendari.get(Calendar.DAY_OF_MONTH);
        this.mes = calendari.get(Calendar.MONTH) + 1;
        this.any = calendari.get(Calendar.YEAR);
    }

    /**
    *Construeix una nova instancia de DateUtil
    *
    * @param dia
    * @param mes
    * @param any
    */
    public DateUtil(int dia, int mes, int any) {
        this.data = new GregorianCalendar(any, mes - 1, dia).getTime();
        this.dia = dia;
        this.mes = mes;
        this.any = any;
    }

    /**
    *Construeix una nova instancia de DateUtil
    *
    * @param dia
    * @param mes
    * @param any
    */
    public DateUtil(String dia, String mes, String any) {
        this(Integer.parseInt(dia), Integer.parseInt(mes), Integer.parseInt(any));
    }

    /**
    * retorna el valor per la propietat data
    *
    * @return valor de data
    */
    public Date getData() {
        return data;
    }

    /**
    * retorna el valor per la propietat dia
    *
    * @return valor de dia
    */
    public int getDia() {
        return dia;
    }

    /**
    * retorna el valor per la propietat mes
    *
    * @return valor de mes
    */
    public int getMes() {
        return mes;
    }

    /**
    * retorna el valor per la propietat any
    *
    * @return valor de any
    */
    public int getAny() {
        return any;
    }
}
