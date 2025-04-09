package managers;

import exception.InvalidOperationException;
import entity.Book;
import entity.Loan;
import entity.Reservation;
import entity.User;
import entity.enumeration.BookState;
import entity.enumeration.LoanState;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

/**
 * Session Bean implementation class LibrarienBean
 *
 * mění stavy knih a vypůjček
 * představuje knihovnici, která do systému zadává informace, že daná kniha je teď vypůjčena/vrácena
 *
 * @author Jindřich Březina
 */
@Stateless
public class LibrarienBean implements LibrarienRemote {

    @EJB
    private CustomerManagerRemote customer;

    @Resource
    private UserTransaction utx;

    @PersistenceContext(unitName = "eLib-ejbPU2")
    private EntityManager entityManager;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

    /**
     * Constant for default count of days for loan length
     */
    public final int BORROW_DEFAULT_TIME = 14;

    /**
     * Default constructor.
     */
    public LibrarienBean() {
    }

    /**
     * Support method to test if date wasn't reach yet
     * @param to date to be reached
     * @return true if it was reached, false otherwise
     */
    private boolean wasDateReached(Date to) {
        try {
            return to.before(this.dateFormatter.parse(this.dateFormatter.format(new Date())));
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }
    }

    public void proccessReservation(Reservation reservation) {
        try {
            Date now = this.dateFormatter.parse(this.dateFormatter.format(new Date()));
            if (wasDateReached(reservation.getDateTo())) {
                throw new RuntimeException("Outdated reservation");
            }
            Book reservationBook = this.entityManager.merge(reservation.getBook());
            User reservationUser = this.entityManager.merge(reservation.getUser());
            Loan newLoan = new Loan();
            newLoan.setBook(reservationBook);
            newLoan.setUser(reservationUser);
            newLoan.setDateFrom(now);
            Calendar c = Calendar.getInstance();
            c.setTime(now);
            c.add(Calendar.DATE, this.BORROW_DEFAULT_TIME);
            newLoan.setDateTo(c.getTime());
            newLoan.setloanState(LoanState.WAITING);
            this.entityManager.persist(newLoan);
            this.entityManager.remove(reservation);
            reservationBook.setBookState(BookState.WAITING_FOR_BORROWER);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Reservation getAnotherReservationWaitingForBook(Book book) {
        if (book.getBookState() == BookState.NOT_FOR_BORROWING) {
            throw new InvalidOperationException("Cannot process reservation over book with state NOT_FOR_BORROWING");
        }
        Query q = this.entityManager.createNamedQuery("getReservationsOfBook");
        List results = q.getResultList();
        for (Object o : results) {
            if (o instanceof Reservation) {
                Reservation r = (Reservation) o;
                if (!this.wasDateReached(r.getDateTo())) {
                    return r;
                } else {
                    Reservation er = this.entityManager.merge(r);
                    this.entityManager.remove(er);
                }
            }
        }
        return null;
    }

    /**
     * It returns the first following reservation of book
     * @param book book
     * @return reservation if exists or null if not
     */
    public Reservation getFollowingReservation(Book book) {
        List<Reservation> list = null;
        list = customer.getReservationsOfBook(book.getId());
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public void discardBook(Book book) {
        Book persisted = this.entityManager.merge(book);
        persisted.setBookState(BookState.NOT_FOR_BORROWING);
        this.entityManager.flush();
    }

    /**
     * It changes a state of the given book
     * @param bookId book's ID
     * @param bookState new bookstate
     */
    public void changeBookState(Long bookId, BookState bookState) {
        Book book = entityManager.find(Book.class, bookId);
        book.setBookState(bookState);
        entityManager.merge(book);
    }

    /**
     * It reurns a list of loans of the given user and loan state. So it is possible to find out
     * which books are borrowed by the given user
     * @param userId
     * @param loanState
     * @return
     */
    public List<Loan> getAllLoansOfUserWithLoanState(Long userId, LoanState loanState) {
        return this.entityManager.createNamedQuery("getAllLoansOfUserWithLoanState").setParameter(1, userId).setParameter(2, loanState).getResultList();
    }

    /**
     * It gets all books (via loans) which have been waiting for borrower and haven't been picked for a specific period (until a specific date).
     * @param date a specific date
     * @return All loans which weren't picked up till a specific date
     */
    public List<Loan> getAllWaitingLoansOlderThen(Date date) {
        return this.entityManager.createNamedQuery("getAllWaitingLoansOlderThen").setParameter(1, date).getResultList();
    }

    /**
     * It gets all finished loans connected to the given book
     * @param bookId book's ID
     * @return all finished loans
     */
    public List<Loan> getHistoryOfLoansAboutBook(Long bookId) {
        return this.entityManager.createNamedQuery("getHistoryOfLoansAboutBook").setParameter(1, bookId).getResultList();
    }

    /**
     * It gets all loans which are about to expire in the given day
     * @param date the given day
     * @return all loans which are about to expire
     */
    public List<Loan> getAllLoansFinishingIn(Date date) {
        return this.entityManager.createNamedQuery("getAllLoansFinishingIn").setParameter(1, date).getResultList();
    }

    public void loanBook(long bookID, User user) {
        Query q = this.entityManager.createNamedQuery("getBookWithID");
        Object result = q.getSingleResult();
        if (result == null) {
            throw new InvalidOperationException("Loan for book with id " + String.valueOf(bookID) + " cannot be made because book wasn't found");
        }
        if (result instanceof Book) {
            Book b = (Book) result;
            Loan l = new Loan();
            l.setBook(b);
            l.setUser(user);
            l.setloanState(LoanState.BORROWED);
            try {
                l.setDateFrom(this.dateFormatter.parse(this.dateFormatter.format(new Date())));
                Calendar c = Calendar.getInstance();
                c.setTime(l.getDateFrom());
                c.add(Calendar.DAY_OF_YEAR, this.BORROW_DEFAULT_TIME);
                l.setDateTo(c.getTime());
                this.entityManager.persist(l);
            } catch (ParseException ex) {
                Logger.getLogger(LibrarienBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            throw new InvalidOperationException("Loan for book with id " + String.valueOf(bookID) + " cannot be made because book wasn't found");
        }
    }

    public void finishLoan(long loanID) {
        Query q = this.entityManager.createNamedQuery("getLoanWithID");
        Object result = q.getSingleResult();
        if (result == null) {
            throw new InvalidOperationException("Loan with id " + String.valueOf(loanID) + " cannot be found");
        }
        if (result instanceof Loan) {
            Loan l = (Loan) result;
            l = this.entityManager.merge(l);
            l.setloanState(LoanState.FINISHED);
            this.entityManager.flush();
        } else {
            throw new InvalidOperationException("Loan with id " + String.valueOf(loanID) + " cannot be found");
        }
    }

    public boolean prolongLoan(long loanID) {
        Query q = this.entityManager.createNamedQuery("getLoanWithID");
        Object result = q.getSingleResult();
        if (result == null) {
            return false;
        }
        if (result instanceof Loan) {
            Loan l = (Loan) result;
            l = this.entityManager.merge(l);
            if (l.getloanState() != LoanState.BORROWED) {
                return false;
            }
            if (this.getAnotherReservationWaitingForBook(l.getBook()) != null) {
                return false;
            }
            if (l.getProlongation() == null) {
                l.setProlongation(1);
            } else {
                l.setProlongation(l.getProlongation() + 1);
            }
            this.entityManager.flush();
            return true;
        } else {
            return false;
        }
    }
}
