package net.sourceforge.jasa.market.rules;

import java.util.Iterator;
import net.sourceforge.jasa.market.Account;
import net.sourceforge.jasa.market.Order;
import net.sourceforge.jasa.market.OrderBook;
import net.sourceforge.jasa.market.ZeroCreditAccount;
import net.sourceforge.jasa.market.auctioneer.AbstractAuctioneer;

/**
 * An implementation of the mechanism described in
 * 
 * "A Dominant Strategy Double Auction" R. Preston McAfee Journal of Economic
 * Theory Vol 56 pages 434-450 1992
 * 
 * @author Steve Phelps
 * @version $Revision: 1.1 $
 */
public class McAfeeClearingPolicy implements ClearingPolicy {

    protected ZeroCreditAccount account;

    protected AbstractAuctioneer auctioneer;

    public McAfeeClearingPolicy(AbstractAuctioneer auctioneer) {
        account = new ZeroCreditAccount(this);
    }

    public void clear() {
        boolean efficientClearing;
        double a0 = -1, a1 = -1;
        double b0 = -1, b1 = -1;
        double p0 = -1;
        OrderBook orderBook = auctioneer.getOrderBook();
        if (orderBook.getLowestMatchedBid() == null) {
            return;
        }
        if (orderBook.getHighestUnmatchedBid() == null || orderBook.getLowestUnmatchedAsk() == null) {
            efficientClearing = false;
        } else {
            a0 = orderBook.getHighestUnmatchedBid().getPrice();
            b0 = orderBook.getLowestUnmatchedAsk().getPrice();
            p0 = (a0 + b0) / 2;
            efficientClearing = orderBook.getHighestMatchedAsk().getPrice() <= p0 && p0 <= orderBook.getLowestMatchedBid().getPrice();
        }
        if (!efficientClearing) {
            a1 = orderBook.getLowestMatchedBid().getPrice();
            b1 = orderBook.getHighestMatchedAsk().getPrice();
        }
        Iterator<Order> matchedShouts = orderBook.matchOrders().iterator();
        while (matchedShouts.hasNext()) {
            Order bid = matchedShouts.next();
            Order ask = matchedShouts.next();
            if (efficientClearing) {
                auctioneer.clear(ask, bid, p0);
            } else {
                if (bid.getPrice() > a1) {
                    auctioneer.clear(ask, bid, a1, b1, ask.getQuantity());
                }
            }
        }
    }

    public Account getAccount() {
        return account;
    }

    public void reset() {
        account.setFunds(0);
    }
}
