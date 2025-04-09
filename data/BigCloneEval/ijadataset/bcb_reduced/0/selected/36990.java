package com.shimari.fxtp;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.io.Serializable;

/**
 * A Bean representing a claim on FX
 */
public final class Claim extends Book implements Serializable {

    private final String my_symbol;

    private final String my_description;

    private final int my_bid;

    private final int my_ask;

    private final int my_last;

    private final int my_status;

    private final int my_pairs;

    private final int my_people;

    private static final int ACTIVE = -2;

    private static final int PROPOSED = -1;

    private static final int PENDING = -3;

    /**
     * Construct new claim
     */
    Claim(String symbol, String description, int bid, int ask, int last, int people, int pairs, int status, List bids, List asks) {
        super(bids, asks);
        this.my_symbol = symbol;
        this.my_description = description;
        this.my_bid = bid;
        this.my_ask = ask;
        this.my_last = last;
        this.my_status = status;
        this.my_people = people;
        this.my_pairs = pairs;
    }

    /**
     * Return whether it is currently being traded or not
     */
    public boolean isActive() {
        return my_status == ACTIVE;
    }

    /**
     * Return symbol
     */
    public String getSymbol() {
        return my_symbol;
    }

    /**
     * Return short description
     */
    public String getDescription() {
        return my_description;
    }

    /**
     * Return highest bid
     */
    public int getBid() {
        return my_bid;
    }

    /**
     * Return lowest ask
     */
    public int getAsk() {
        return my_ask;
    }

    /**
     * Return last trading price
     */
    public int getLast() {
        return my_last;
    }

    /**
     * Return number of pairs being traded
     */
    public int getPairs() {
        return my_pairs;
    }

    /**
     * Return number of UIDs who hold the symbol
     */
    public int getPeople() {
        return my_people;
    }

    /**
     * Return average bid, excluding bids from specified user, given a 
     * purchase of cost dollars.
     */
    public double getAverageBid(UserId exclude, double cost) {
        return calcAveragePrice(getBids(), exclude, cost);
    }

    /**
     * Return the average ask, excluding asks from specified user, given a
     * purchase of cost dollars
     */
    public double getAverageAsk(UserId exclude, double cost) {
        return 100 - calcAveragePrice(getAsks(), exclude, cost);
    }

    /**
     * Return the average of getAverageBid() and getAverageCost()
     */
    public double getAveragePrice(UserId exclude, double cost) {
        double ab = getAverageBid(exclude, cost);
        double aa = getAverageAsk(exclude, cost);
        double ap = (ab + aa) / 2;
        return ap;
    }

    /**
     * Return the difference of getAverageBid() and getAverageCost()
     */
    public double getSpread(UserId exclude, double cost) {
        double aa = getAverageAsk(exclude, cost);
        double ab = getAverageBid(exclude, cost);
        return aa - ab;
    }

    private static double calcAveragePrice(Collection c, UserId exclude, double cash) {
        double cashLeft = cash;
        double total = 0;
        Integer excludeId = (exclude != null) ? exclude.getId() : null;
        Iterator i = c.iterator();
        while (i.hasNext() && cashLeft > 0.01) {
            Order o = (Order) i.next();
            if (excludeId != null && o.getId().equals(excludeId)) {
                continue;
            }
            double quantity = o.getQuantity();
            double cost = o.getCost();
            if (cost < cashLeft) {
                total += quantity;
                cashLeft -= cost;
            } else {
                double num = (quantity * cashLeft) / cost;
                total += num;
                cashLeft = 0;
            }
        }
        if (total == 0) {
            return 0;
        }
        double avgPrice = (100 * (cash - cashLeft)) / total;
        return avgPrice;
    }

    /**
     * Return the current status: ACTIVE, PROPOSED, PENDING, or CLOSED
     */
    public String getStatus() {
        switch(my_status) {
            case ACTIVE:
                return "ACTIVE";
            case PROPOSED:
                return "PROPOSED";
            case PENDING:
                return "PENDING";
            default:
                return "CLOSED";
        }
    }

    /**
     * Return debugging string
     */
    public String toString() {
        return my_symbol + " - " + my_description + " bid=" + my_bid + " ask=" + my_ask + " last=" + my_last + " pairs=" + my_pairs + ": " + getStatus();
    }

    /**
     * Check semantic equality
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        try {
            Claim fo = (Claim) o;
            return (fo.my_symbol.equals(my_symbol));
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Hashcode is symbol hashcode
     */
    public int hashCode() {
        return my_symbol.hashCode();
    }

    /**
     * Return a string listing the current asks
     */
    public String getAskQueue(int size) {
        return getQueue(getAsks().iterator(), size);
    }

    /**
     * Return a string listing the current bids
     */
    public String getBidQueue(int size) {
        return getQueue(getBids().iterator(), size);
    }

    private String getQueue(Iterator i, int size) {
        StringBuffer buf = new StringBuffer();
        int n = 0;
        while (n < size && i.hasNext()) {
            Order o = (Order) i.next();
            if (n != 0) {
                buf.append(" ");
            }
            buf.append(o.toMediumString());
            n++;
        }
        return buf.toString();
    }
}
