package org.jbudget.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.jbudget.Core.Account;
import org.jbudget.Core.AutomaticTransaction;
import org.jbudget.Core.Budget;
import org.jbudget.Core.ExpenseAllocation;
import org.jbudget.Core.ExpenseCategory;
import org.jbudget.Core.MoneyPit;
import org.jbudget.Core.Month;
import org.jbudget.Core.Transaction;

/**Unit tests for testing functionality of the Month object.
 *
 * @author petrov
 */
public class TestMonth {

    final Budget emptyBudget = new Budget();

    Budget budget1;

    Budget budget2;

    Budget budget3;

    final ExpenseCategory category1 = new ExpenseCategory("test category1");

    final ExpenseCategory category2 = new ExpenseCategory("test category2");

    final ExpenseCategory category3 = new ExpenseCategory("test category3");

    final ExpenseCategory category4 = new ExpenseCategory("test category4");

    final ExpenseCategory category5 = new ExpenseCategory("test category5");

    final ExpenseCategory category6 = new ExpenseCategory("test category6");

    final ExpenseCategory category7 = new ExpenseCategory("test category7");

    final ExpenseCategory category8 = new ExpenseCategory("test category8");

    final ExpenseCategory category9 = new ExpenseCategory("test category9");

    final ExpenseCategory category10 = new ExpenseCategory("test category10");

    final Account cash = new Account("Cash");

    Calendar calendar;

    int firstDay;

    int lastDay;

    /** Creates a new instance of TestMonth */
    public TestMonth() {
    }

    /** Recreating budgets before every test */
    @Before
    public void setUp() {
        calendar = new GregorianCalendar();
        firstDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        budget1 = new Budget();
        budget2 = new Budget();
        budget3 = new Budget();
        budget1.addRootExpenseCategory(category1, false);
        budget1.addRootExpenseCategory(category2, false);
        budget2.addRootExpenseCategory(category1, false);
        budget2.addRootExpenseCategory(category2, false);
        budget2.addSubcategory(category1, category3, false);
        budget2.addSubcategory(category1, category4, false);
        budget3.addRootExpenseCategory(category2, false);
        budget3.addRootExpenseCategory(category5, false);
        budget3.addSubcategory(category2, category6, false);
        budget3.addSubcategory(category6, category7, false);
        budget3.addSubcategory(category5, category8, false);
        budget3.addSubcategory(category5, category9, false);
        budget3.addSubcategory(category9, category10, false);
    }

    /** Test that a Month with empty budget works predictably */
    @Test
    public void emptyTest() {
        Month month = new Month(emptyBudget);
        assertEquals(month.getYear(), calendar.get(Calendar.YEAR));
        assertEquals(month.getMonth(), calendar.get(Calendar.MONTH));
        calendar.setTime(month.getFirstDay());
        assertEquals(month.getYear(), calendar.get(Calendar.YEAR));
        assertEquals(month.getMonth(), calendar.get(Calendar.MONTH));
        assertEquals(calendar.getActualMinimum(Calendar.DAY_OF_MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        for (int day = firstDay; day <= lastDay; day++) assertTrue(month.getTransactions(day).isEmpty());
        month.generateTransactions(lastDay);
        for (int day = firstDay; day <= lastDay; day++) assertTrue(month.getTransactions(day).isEmpty());
    }

    /** Tests that {@link org.petrov.dmitri.jbudget.core.Budget#isACopy isACopy}
   * method works as advertised
   */
    @Test
    public void testCopy() {
        Month month1 = new Month(budget3);
        Month month2 = new Month(budget3);
        assertTrue(month1.isACopy(month2));
        ExpenseCategory[] categories = { category2, category5, category6, category7, category8, category9, category10 };
        Random rnd = new Random(System.currentTimeMillis());
        for (int i = 0; i < categories.length; i++) {
            long balance = rnd.nextLong() % 1000000L;
            month1.setStartingAllocation(categories[i], balance);
            month2.setStartingAllocation(categories[i], balance);
        }
        assertTrue(month1.isACopy(month2));
        assertTrue(month2.isACopy(month1));
        for (int i = 0; i < 100; i++) {
            int day = firstDay + (int) (rnd.nextFloat() * (lastDay - firstDay));
            calendar.set(Calendar.DAY_OF_MONTH, day);
            ExpenseCategory category = categories[(int) (rnd.nextFloat() * categories.length)];
            long amount = rnd.nextLong() % 1000000L;
            Transaction t = new Transaction(amount, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category, month1.getBudget()));
            month1.addTransaction(t);
            t = new Transaction(amount, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category, month2.getBudget()));
            month2.addTransaction(t);
        }
        assertTrue(month1.isACopy(month2));
        assertTrue(month2.isACopy(month1));
        Transaction t = new Transaction(100L, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category2, month1.getBudget()));
        month1.addTransaction(t);
        assertTrue(!month1.isACopy(month2));
        assertTrue(!month2.isACopy(month1));
    }

    /** Should be impossible to add a transaction to a month with
    * an empty budget
    */
    @Test(expected = IllegalArgumentException.class)
    public void test1() {
        Month month = new Month(emptyBudget);
        Calendar calendar = new GregorianCalendar();
        Transaction transaction = new Transaction(1L, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category1, month.getBudget()));
        month.addTransaction(transaction);
    }

    /** Should be impossible get balance for any category with an empty budget.
    */
    @Test(expected = IllegalArgumentException.class)
    public void test2() {
        Month month = new Month(emptyBudget);
        month.getAllocatedBalance(category5, 1);
    }

    /** Should be impossible get balance for any category with an empty budget.
    */
    @Test(expected = IllegalArgumentException.class)
    public void test3() {
        Month month = new Month(emptyBudget);
        month.getStartingAllocation(category1);
    }

    /** Should be impossible get transactions for any category with 
    * an empty budget.
    */
    @Test(expected = IllegalArgumentException.class)
    public void test4() {
        Month month = new Month(emptyBudget);
        month.getAllocations(1, category3);
    }

    /** Should be impossible get transactions for any category with 
    * an empty budget.
    */
    @Test(expected = IllegalArgumentException.class)
    public void test5() {
        Month month = new Month(emptyBudget);
        month.getAllocations(category5);
    }

    /** Should be impossible to remove a transaction from a month with
    * an empty budget
    */
    @Test(expected = IllegalArgumentException.class)
    public void test6() {
        Month month = new Month(emptyBudget);
        Transaction transaction = new Transaction(1L, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category1, month.getBudget()));
        month.removeTransaction(transaction);
    }

    /** Make sure that setting the initial balance works as predicted */
    @Test
    public void test7() {
        Month month = new Month(budget1);
        long balance1 = 200L;
        long balance2 = -500L;
        month.setStartingAllocation(category1, balance1);
        month.setStartingAllocation(category2, balance2);
        assertEquals(balance1, month.getFinalAllocation(category1));
        assertEquals(balance2, month.getFinalAllocation(category2));
        balance1 = 1300L;
        month.setStartingAllocation(category1, balance1);
        assertEquals(balance1, month.getFinalAllocation(category1));
    }

    /** Make sure that simple operations with transactions and allocations 
   * work. 
   */
    @Test
    public void test8() {
        Month month = new Month(budget1);
        long amount1 = 100L;
        long amount2 = 200L;
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        Transaction transaction1 = new Transaction(amount1, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category1, month.getBudget()));
        month.addAllocation(transaction1);
        assertEquals(0L, month.getAllocatedBalance(category1, 1));
        assertEquals(amount1, month.getAllocatedBalance(category1, 5));
        assertEquals(amount1, month.getFinalAllocation(category1));
        List<Transaction> t = month.getAllocations(category1);
        assertEquals(1, t.size());
        assertEquals(transaction1, t.get(0));
        t = month.getAllocations(category2);
        assertTrue(t.isEmpty());
        for (int day = firstDay; day <= lastDay; day++) {
            t = month.getAllocations(day, category1);
            if (day != 5) assertEquals(0, t.size()); else assertEquals(1, t.size());
            t = month.getAllocations(day, category2);
            assertTrue(t.isEmpty());
            t = month.getAllocations(day);
            if (day != 5) assertEquals(0, t.size()); else assertEquals(1, t.size());
        }
        calendar.set(Calendar.DAY_OF_MONTH, 10);
        Transaction transaction2 = new Transaction(amount2, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category1, month.getBudget()));
        month.addTransaction(transaction2);
        assertEquals(amount1, month.getAllocatedBalance(category1, 7));
        assertEquals(amount1 - amount2, month.getAllocatedBalance(category1, 15));
        assertEquals(amount1 - amount2, month.getFinalAllocation(category1));
        t = month.getAllocations(category1);
        assertEquals(1, t.size());
        assertTrue(t.contains(transaction1));
        assertTrue(!t.contains(transaction2));
        t = month.getTransactions(category1);
        assertEquals(1, t.size());
        assertTrue(!t.contains(transaction1));
        assertTrue(t.contains(transaction2));
        for (int day = firstDay; day <= lastDay; day++) {
            t = month.getAllocations(day, category1);
            if (day != 5) assertEquals(0, t.size()); else assertEquals(1, t.size());
            t = month.getTransactions(day, category1);
            if (day != 10) assertEquals(0, t.size()); else assertEquals(1, t.size());
            t = month.getAllocations(day, category2);
            assertTrue(t.isEmpty());
            t = month.getTransactions(day, category2);
            assertTrue(t.isEmpty());
            t = month.getAllocations(day);
            if (day != 5) assertEquals(0, t.size()); else assertEquals(1, t.size());
            t = month.getTransactions(day);
            if (day != 10) assertEquals(0, t.size()); else assertEquals(1, t.size());
        }
        month.removeAllocation(transaction1);
        t = month.getAllocations(category1);
        assertTrue(t.isEmpty());
        t = month.getTransactions(category1);
        assertEquals(1, t.size());
        assertEquals(transaction2, t.get(0));
        t = month.getAllocations(category2);
        assertTrue(t.isEmpty());
        t = month.getTransactions(category2);
        assertTrue(t.isEmpty());
        for (int day = firstDay; day <= lastDay; day++) {
            t = month.getAllocations(day, category1);
            assertTrue(t.isEmpty());
            t = month.getTransactions(day, category1);
            if (day != 10) assertEquals(0, t.size()); else assertEquals(1, t.size());
            t = month.getAllocations(day, category2);
            assertTrue(t.isEmpty());
            t = month.getAllocations(day);
            assertTrue(t.isEmpty());
            t = month.getTransactions(day);
            if (day != 10) assertEquals(0, t.size()); else assertEquals(1, t.size());
        }
        month.removeTransaction(transaction2);
        for (int day = firstDay; day <= lastDay; day++) {
            assertTrue(month.getAllocations(day).isEmpty());
            assertTrue(month.getTransactions(day).isEmpty());
        }
        assertEquals(0L, month.getFinalAllocation(category1));
        assertEquals(0L, month.getFinalAllocation(category2));
    }

    /** Make sure that initial balance and transactions work well together. */
    @Test
    public void test9() {
        Month month = new Month(budget1);
        Random rnd = new Random(System.currentTimeMillis());
        int middle = (firstDay + lastDay) / 2;
        long c1StartAmount = (long) (rnd.nextFloat() * 10000);
        long c2StartAmount = (long) (rnd.nextFloat() * 10000);
        month.setStartingAllocation(category1, c1StartAmount);
        month.setStartingAllocation(category2, c2StartAmount);
        List<Transaction> c1FirstHalfTr = new ArrayList<Transaction>();
        List<Transaction> c1SecondHalfTr = new ArrayList<Transaction>();
        long c1FirstHalfAmount = 0L;
        long c1SecondHalfAmount = 0L;
        List<Transaction> c2EvenTr = new ArrayList<Transaction>();
        List<Transaction> c2OddTr = new ArrayList<Transaction>();
        long c2EvenAmount = 0L;
        long c2OddAmount = 0L;
        for (int i = 0; i < 1000; i++) {
            int day = (int) (rnd.nextFloat() * (lastDay - firstDay + 1) + firstDay);
            long amount = (long) ((rnd.nextFloat() - 0.5) * 10000);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            Transaction transaction = new Transaction(amount, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category1, month.getBudget()));
            month.addAllocation(transaction);
            if (day <= middle) {
                c1FirstHalfTr.add(transaction);
                c1FirstHalfAmount += amount;
            } else {
                c1SecondHalfTr.add(transaction);
                c1SecondHalfAmount += amount;
            }
        }
        for (int i = 0; i < 1000; i++) {
            int day = (int) (rnd.nextFloat() * (lastDay - firstDay + 1) + firstDay);
            long amount = (long) ((rnd.nextFloat() - 0.5) * 10000);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            Transaction transaction = new Transaction(amount, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category2, month.getBudget()));
            month.addAllocation(transaction);
            if (day % 2 == 0) {
                c2EvenTr.add(transaction);
                c2EvenAmount += amount;
            } else {
                c2OddTr.add(transaction);
                c2OddAmount += amount;
            }
        }
        assertEquals(c1StartAmount + c1FirstHalfAmount, month.getAllocatedBalance(category1, middle));
        assertEquals(c1StartAmount + c1FirstHalfAmount + c1SecondHalfAmount, month.getAllocatedBalance(category1, lastDay));
        assertEquals(c1StartAmount + c1FirstHalfAmount + c1SecondHalfAmount, month.getFinalAllocation(category1));
        assertEquals(c2StartAmount + c2EvenAmount + c2OddAmount, month.getAllocatedBalance(category2, lastDay));
        assertEquals(c2StartAmount + c2EvenAmount + c2OddAmount, month.getFinalAllocation(category2));
        for (Transaction t : c1FirstHalfTr) month.removeAllocation(t);
        for (int day = firstDay; day <= middle; day++) {
            assertEquals(c1StartAmount, month.getAllocatedBalance(category1, day));
            assertTrue(month.getAllocations(day, category1).isEmpty());
        }
        assertEquals(c1StartAmount + c1SecondHalfAmount, month.getFinalAllocation(category1));
        for (Transaction t : c2EvenTr) month.removeAllocation(t);
        for (int day = firstDay; day <= lastDay; day++) {
            if (day % 2 == 0) {
                assertEquals(month.getAllocatedBalance(category2, day - 1), month.getAllocatedBalance(category2, day));
                assertTrue(month.getAllocations(day, category2).isEmpty());
            }
        }
        for (Transaction t : c1SecondHalfTr) month.removeAllocation(t);
        for (Transaction t : c2OddTr) month.removeAllocation(t);
        for (int day = firstDay; day <= lastDay; day++) assertTrue(month.getAllocations(day).isEmpty());
        assertEquals(c1StartAmount, month.getFinalAllocation(category1));
        assertEquals(c2StartAmount, month.getFinalAllocation(category2));
    }

    /** Testing the automatic transactions generated by allocations and 
   * deductions in the budget
   */
    @Test
    public void test10() {
        ExpenseAllocation allocation = new ExpenseAllocation(category1, budget1, 100L, ExpenseAllocation.Type.DAILY, 1);
        budget1.addAllocation(allocation);
        Month month = new Month(budget1);
        month.generateTransactions(-1);
        for (int day = firstDay; day <= lastDay; day++) {
            List<Transaction> l = month.getAllocations(day, category1);
            assertEquals(1, l.size());
            assertTrue(l.get(0).isAutomatic());
            assertEquals(100L, l.get(0).getAmount());
        }
        assertEquals(100L * (lastDay - firstDay + 1), month.getFinalAllocation(category1));
    }

    /** Testing the automatic transactions generated by allocations and 
   * deductions in the budget
   */
    @Test
    public void test11() {
        AutomaticTransaction autoTr = new AutomaticTransaction(new MoneyPit(cash), new MoneyPit(category1, budget1), 100L, AutomaticTransaction.Type.DAILY, 1);
        budget1.addAutoTransaction(autoTr);
        Month month = new Month(budget1);
        month.generateTransactions(-1);
        for (int day = firstDay; day <= lastDay; day++) {
            List<Transaction> l = month.getTransactions(day, new MoneyPit(category1, budget1));
            assertEquals(1, l.size());
            assertTrue(l.get(0).isAutomatic());
            assertEquals(100L, l.get(0).getAmount());
        }
        assertEquals(-100L * (lastDay - firstDay + 1), month.getFinalAllocation(category1));
    }

    /** Testing the automatic transactions generated by allocations and 
   * deductions in the budget
   */
    @Test
    public void test12() {
        AutomaticTransaction autoExpense = new AutomaticTransaction(new MoneyPit(cash), new MoneyPit(category1, budget1), 100L, ExpenseAllocation.Type.DAILY, 1);
        budget1.addAutoTransaction(autoExpense);
        ExpenseAllocation allocation = new ExpenseAllocation(category1, budget1, 1000L, ExpenseAllocation.Type.MONTHLY, 1);
        budget1.addAllocation(allocation);
        allocation = new ExpenseAllocation(category2, budget1, 1000L, ExpenseAllocation.Type.MONTHLY, -1);
        budget1.addAllocation(allocation);
        autoExpense = new AutomaticTransaction(new MoneyPit(cash), new MoneyPit(category2, budget1), 100L, ExpenseAllocation.Type.MONTHLY, 15);
        budget1.addAutoTransaction(autoExpense);
        Month month = new Month(budget1);
        month.generateTransactions(-1);
        List<Transaction> l = month.getAllocations(firstDay, category1);
        assertEquals(1, l.size());
        for (int day = firstDay + 1; day <= lastDay; day++) {
            l = month.getAllocations(day, category1);
            assertEquals(0, l.size());
            l = month.getTransactions(day, new MoneyPit(category1, budget1));
            assertEquals(1, l.size());
            assertTrue(l.get(0).isAutomatic());
            assertEquals(100L, l.get(0).getAmount());
        }
        assertEquals(-100L * (lastDay - firstDay + 1) + 1000L, month.getFinalAllocation(category1));
        assertEquals(0L, month.getAllocatedBalance(category2, 14));
        assertEquals(-100L, month.getAllocatedBalance(category2, 15));
        assertEquals(-100L, month.getAllocatedBalance(category2, 16));
        assertEquals(900L, month.getFinalAllocation(category2));
    }

    /** Checking that changing budget works as expected */
    @Test(expected = IllegalArgumentException.class)
    public void test13() {
        Month month = new Month(budget2);
        month = month.changeBudget(budget1);
        month.getAllocatedBalance(category3, lastDay);
    }

    /** Checking that changing budget works as expected */
    @Test
    public void test14() {
        Month month = new Month(budget2);
        month.setStartingAllocation(category1, 100L);
        month.setStartingAllocation(category2, 200L);
        month.setStartingAllocation(category3, 300L);
        month.setStartingAllocation(category4, 400L);
        month = month.changeBudget(budget1);
        assertEquals(100L, month.getStartingAllocation(category1));
        assertEquals(200L, month.getStartingAllocation(category2));
        month = month.changeBudget(budget3);
        assertEquals(200L, month.getStartingAllocation(category2));
        assertEquals(0L, month.getStartingAllocation(category5));
        assertEquals(0L, month.getStartingAllocation(category6));
        assertEquals(0L, month.getStartingAllocation(category7));
        assertEquals(0L, month.getStartingAllocation(category8));
    }

    /** Checking that changing budget works as expected */
    @Test(expected = IllegalArgumentException.class)
    public void test15() {
        AutomaticTransaction autoExp = new AutomaticTransaction(new MoneyPit(cash), new MoneyPit(category1, budget1), 100L, ExpenseAllocation.Type.DAILY, 1);
        budget1.addAutoTransaction(autoExp);
        ExpenseAllocation allocation = new ExpenseAllocation(category2, budget1, 100L, ExpenseAllocation.Type.WEEKLY, 1);
        budget1.addAllocation(allocation);
        autoExp = new AutomaticTransaction(new MoneyPit(cash), new MoneyPit(category1, budget2), 200L, ExpenseAllocation.Type.DAILY, 1);
        budget2.addAutoTransaction(autoExp);
        allocation = new ExpenseAllocation(category2, budget2, 200L, ExpenseAllocation.Type.DAILY, 1);
        budget2.addAllocation(allocation);
        allocation = new ExpenseAllocation(category3, budget2, 200L, ExpenseAllocation.Type.WEEKLY, 3);
        budget2.addAllocation(allocation);
        Month month = new Month(budget1);
        month.generateTransactions(lastDay);
        month = month.changeBudget(budget2);
        for (int day = firstDay; day <= lastDay; day++) {
            List<Transaction> l = month.getAllocations(day, category1);
            assertTrue(l.isEmpty());
            l = month.getTransactions(day, category1);
            assertEquals(1, l.size());
            Transaction t = l.get(0);
            assertTrue(t.isAutomatic());
            assertEquals(200L, t.getAmount());
            l = month.getAllocations(day, category2);
            assertEquals(1, l.size());
            t = l.get(0);
            assertTrue(t.isAutomatic());
            assertEquals(200L, t.getAmount());
            l = month.getTransactions(day, category2);
            assertTrue(l.isEmpty());
            l = month.getAllocations(day, category3);
            if (l.size() > 0) {
                assertEquals(1, l.size());
                t = l.get(0);
                assertTrue(t.isAutomatic());
                assertEquals(200L, t.getAmount());
            }
        }
        calendar.set(Calendar.DAY_OF_MONTH, firstDay);
        Transaction transaction1 = new Transaction(33L, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category1, month.getBudget()));
        month.addTransaction(transaction1);
        Transaction allocation1 = new Transaction(33L, 0, calendar.getTime(), null, new MoneyPit(category1, month.getBudget()));
        month.addAllocation(allocation1);
        calendar.set(Calendar.DAY_OF_MONTH, lastDay);
        Transaction transaction2 = new Transaction(33L, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category1, month.getBudget()));
        month.addTransaction(transaction2);
        Transaction allocation2 = new Transaction(33L, 0, calendar.getTime(), null, new MoneyPit(category1, month.getBudget()));
        month.addAllocation(allocation2);
        Transaction transaction3 = new Transaction(33L, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category2, month.getBudget()));
        month.addTransaction(transaction2);
        Transaction allocation3 = new Transaction(33L, 0, calendar.getTime(), null, new MoneyPit(category2, month.getBudget()));
        month.addAllocation(allocation2);
        List<Transaction> l = month.getTransactions(firstDay, category1);
        assertTrue(l.contains(transaction1));
        l = month.getTransactions(lastDay, category1);
        assertTrue(l.contains(transaction2));
        l = month.getTransactions(lastDay, category2);
        assertTrue(l.contains(transaction3));
        l = month.getAllocations(firstDay, category1);
        assertTrue(l.contains(allocation1));
        l = month.getAllocations(lastDay, category1);
        assertTrue(l.contains(allocation2));
        l = month.getAllocations(lastDay, category2);
        assertTrue(l.contains(allocation3));
        month = month.changeBudget(budget1);
        l = month.getTransactions(firstDay, category1);
        assertTrue(l.contains(transaction1));
        l = month.getTransactions(lastDay, category1);
        assertTrue(l.contains(transaction2));
        l = month.getTransactions(lastDay, category2);
        assertTrue(l.contains(transaction3));
        l = month.getAllocations(firstDay, category1);
        assertTrue(l.contains(allocation1));
        l = month.getAllocations(lastDay, category1);
        assertTrue(l.contains(allocation2));
        l = month.getAllocations(lastDay, category2);
        assertTrue(l.contains(allocation3));
        month = month.changeBudget(budget3);
        l = month.getTransactions(lastDay, category2);
        assertTrue(l.contains(transaction3));
        l = month.getTransactions(lastDay, category1);
    }

    /** Checking that the same transaction can not be added twice */
    @Test(expected = IllegalArgumentException.class)
    public void test16() {
        Month month = new Month(budget1);
        calendar.set(Calendar.DAY_OF_MONTH, firstDay);
        Transaction transaction1 = new Transaction(33L, 0, calendar.getTime(), new MoneyPit(cash), new MoneyPit(category2, month.getBudget()));
        month.addTransaction(transaction1);
        month.addTransaction(transaction1);
    }

    /** Checking that the same allocation can not be added twice */
    @Test(expected = IllegalArgumentException.class)
    public void test17() {
        Month month = new Month(budget1);
        calendar.set(Calendar.DAY_OF_MONTH, firstDay);
        Transaction transaction1 = new Transaction(33L, 0, calendar.getTime(), null, new MoneyPit(category2, month.getBudget()));
        month.addAllocation(transaction1);
        month.addAllocation(transaction1);
    }
}
