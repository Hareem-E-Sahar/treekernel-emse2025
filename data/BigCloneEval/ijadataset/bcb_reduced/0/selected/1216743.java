package at.fhj.itm.beans;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import at.fhj.itm.business.ServiceAssembler;
import at.fhj.itm.business.ServiceException;
import at.fhj.itm.business.ServiceTrip;
import at.fhj.itm.model.Location;
import at.fhj.itm.model.User;

/**
 * @author Gerald Reisinger, Patrick Winkelmayer
 */
@ManagedBean
@SessionScoped
public class InsertTrip {

    private ServiceTrip serviceTrip;

    private User user;

    private Calendar date = new GregorianCalendar();

    private String time;

    private int seats;

    private String fromCity;

    private int fromZip;

    private String toCity;

    private int toZip;

    private String stopCity;

    private int stopZip;

    public InsertTrip() {
        this.serviceTrip = ServiceAssembler.getInstance().createServiceTrip();
        setSeats(3);
        setDate(new Date());
        setTime(date.get(Calendar.HOUR_OF_DAY) + ":" + date.get(Calendar.MINUTE));
    }

    public String getStopCity() {
        return stopCity;
    }

    public void setStopCity(String stopCity) {
        this.stopCity = stopCity;
    }

    public int getStopZip() {
        return stopZip;
    }

    public void setStopZip(int stopZip) {
        this.stopZip = stopZip;
    }

    public Date getDate() {
        return date.getTime();
    }

    public void setDate(Date date) {
        this.date.setTime(date);
    }

    public String getFromCity() {
        return fromCity;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    public int getFromZip() {
        return fromZip;
    }

    public void setFromZip(int fromZip) {
        this.fromZip = fromZip;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getToCity() {
        return toCity;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }

    public int getToZip() {
        return toZip;
    }

    public void setToZip(int toZip) {
        this.toZip = toZip;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ServiceTrip getServiceTrip() {
        return serviceTrip;
    }

    public String insertTripButtonClicked() {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy");
        String depTime = dateTimeFormat.format(getDate().getTime()).concat(" ").concat(getTime());
        dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date departure;
        try {
            departure = dateTimeFormat.parse(depTime);
        } catch (ParseException e) {
            return "insertFailed";
        }
        Location fromLocation = new Location(fromZip, fromCity);
        Location toLocation = new Location(toZip, toCity);
        Location stopLocation = new Location(this.stopZip, this.stopCity);
        try {
            getServiceTrip().insertTrip(departure, getSeats(), fromLocation, toLocation, stopLocation);
        } catch (ServiceException e) {
            return "errorDatabase";
        }
        return "insertSuccessfull";
    }
}
