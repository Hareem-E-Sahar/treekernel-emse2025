package br.com.visualmidia.tools;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Logger;
import br.com.visualmidia.business.City;
import br.com.visualmidia.business.GDDate;
import br.com.visualmidia.business.Operation;
import br.com.visualmidia.business.Parcel;
import br.com.visualmidia.business.Person;
import br.com.visualmidia.business.Presence;
import br.com.visualmidia.business.Registration;
import br.com.visualmidia.business.RegistrationAppointment;
import br.com.visualmidia.business.State;
import br.com.visualmidia.ui.report.data.Billet;
import br.com.visualmidia.ui.report.data.ParcelData;

public class MergeSortAlgorithm {

    private static final Logger logger = Logger.getLogger(MergeSortAlgorithm.class);

    private void sortBirthDateByPerson(List<Person> persons, int lowerValue, int hightValue) throws Exception {
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortBirthDateByPerson(persons, lowerValue, pivot);
        sortBirthDateByPerson(persons, pivot + 1, hightValue);
        List<Person> working = new ArrayList<Person>();
        for (int i = 0; i < length; i++) {
            working.add(i, persons.get(lowerValue + i));
        }
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    if (Integer.parseInt(working.get(m1).getBirthDate().split("/")[0]) > Integer.parseInt(working.get(m2).getBirthDate().split("/")[0])) {
                        persons.set(i + lowerValue, working.get(m2++));
                    } else {
                        persons.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    persons.set(i + lowerValue, working.get(m2++));
                }
            } else {
                persons.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortBirthDateByPerson(List<Person> persons) throws Exception {
        sortBirthDateByPerson(persons, 0, persons.size() - 1);
    }

    public void sortParcelByDate(List parcelsData) {
        try {
            if (parcelsData.size() > 1) {
                sortParcelByDate(parcelsData, 0, parcelsData.size() - 1);
            }
        } catch (Exception e) {
            logger.error("Sort Parcel By Date Error: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void sortParcelByDate(List parcelsData, int lowerValue, int hightValue) throws Exception {
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortParcelByDate(parcelsData, lowerValue, pivot);
        sortParcelByDate(parcelsData, pivot + 1, hightValue);
        List working = new ArrayList();
        for (int i = 0; i < length; i++) working.add(i, parcelsData.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    GDDate payDate = null;
                    GDDate payDate2 = null;
                    if (working.get(m1) instanceof ParcelData) {
                        ParcelData parcelData = (ParcelData) working.get(m1);
                        ParcelData parcelData2 = (ParcelData) working.get(m2);
                        payDate = new GDDate(parcelData.getParcelDate());
                        payDate2 = new GDDate(parcelData2.getParcelDate());
                    } else if (working.get(m1) instanceof Parcel) {
                        Parcel parcelData = (Parcel) working.get(m1);
                        Parcel parcelData2 = (Parcel) working.get(m2);
                        payDate = new GDDate(parcelData.getDate());
                        payDate2 = new GDDate(parcelData2.getDate());
                    }
                    if (payDate.afterDay(payDate2)) {
                        parcelsData.set(i + lowerValue, working.get(m2++));
                    } else {
                        parcelsData.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    parcelsData.set(i + lowerValue, working.get(m2++));
                }
            } else {
                parcelsData.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortOperationsByDate(List<Operation> operations) {
        try {
            sortOperationsByDate(operations, 0, operations.size() - 1);
        } catch (Exception e) {
            logger.error("Sort Operations By Date Error: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void sortOperationsByDate(List<Operation> operations, int lowerValue, int hightValue) throws Exception {
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortOperationsByDate(operations, lowerValue, pivot);
        sortOperationsByDate(operations, pivot + 1, hightValue);
        List<Operation> working = new ArrayList<Operation>();
        for (int i = 0; i < length; i++) working.add(i, operations.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    GDDate date = null;
                    GDDate date2 = null;
                    date = new GDDate(working.get(m1).getDateTime());
                    date2 = new GDDate(working.get(m2).getDateTime());
                    if (date.afterDay(date2)) {
                        operations.set(i + lowerValue, working.get(m2++));
                    } else {
                        operations.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    operations.set(i + lowerValue, working.get(m2++));
                }
            } else {
                operations.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortNumbers(List<String> numbers) {
        try {
            sortNumbers(numbers, 0, numbers.size() - 1);
        } catch (Exception e) {
            logger.error("Sort Operations By Date Error: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void sortNumbers(List<String> numbers, int lowerValue, int hightValue) throws Exception {
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortNumbers(numbers, lowerValue, pivot);
        sortNumbers(numbers, pivot + 1, hightValue);
        List<String> working = new ArrayList<String>();
        for (int i = 0; i < length; i++) working.add(i, numbers.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    int number1 = Integer.parseInt(working.get(m1));
                    int number2 = Integer.parseInt(working.get(m2));
                    if (number1 > number2) {
                        numbers.set(i + lowerValue, working.get(m2++));
                    } else {
                        numbers.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    numbers.set(i + lowerValue, working.get(m2++));
                }
            } else {
                numbers.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortOperationsById(List<Operation> operations) {
        try {
            sortOperationsById(operations, 0, operations.size() - 1);
        } catch (Exception e) {
            logger.error("Sort Operations By Id Error: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void sortOperationsById(List<Operation> operations, int lowerValue, int hightValue) throws Exception {
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortOperationsById(operations, lowerValue, pivot);
        sortOperationsById(operations, pivot + 1, hightValue);
        List<Operation> working = new ArrayList<Operation>();
        for (int i = 0; i < length; i++) working.add(i, operations.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    int id = Integer.parseInt(working.get(m1).getId());
                    int id2 = Integer.parseInt(working.get(m2).getId());
                    if (id > id2) {
                        operations.set(i + lowerValue, working.get(m2++));
                    } else {
                        operations.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    operations.set(i + lowerValue, working.get(m2++));
                }
            } else {
                operations.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortPresencesByDate(List<Presence> presences) {
        try {
            sortPresencesByDate(presences, 0, presences.size() - 1);
        } catch (Exception e) {
            logger.error("Sort Presences By Date Error: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void sortPresencesByDate(List<Presence> presences, int lowerValue, int hightValue) throws Exception {
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortPresencesByDate(presences, lowerValue, pivot);
        sortPresencesByDate(presences, pivot + 1, hightValue);
        List<Presence> working = new ArrayList<Presence>();
        for (int i = 0; i < length; i++) working.add(i, presences.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    GDDate date = null;
                    GDDate date2 = null;
                    date = new GDDate(working.get(m1).getDate());
                    date2 = new GDDate(working.get(m2).getDate());
                    if (date.afterDay(date2)) {
                        presences.set(i + lowerValue, working.get(m2++));
                    } else {
                        presences.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    presences.set(i + lowerValue, working.get(m2++));
                }
            } else {
                presences.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortPresencesByDateAndTime(List<Presence> presences) {
        try {
            sortPresencesByDateAndTime(presences, 0, presences.size() - 1);
        } catch (Exception e) {
            logger.error("Sort Presences By Date Error: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void sortPresencesByDateAndTime(List<Presence> presences, int lowerValue, int hightValue) throws Exception {
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortPresencesByDateAndTime(presences, lowerValue, pivot);
        sortPresencesByDateAndTime(presences, pivot + 1, hightValue);
        List<Presence> working = new ArrayList<Presence>();
        for (int i = 0; i < length; i++) working.add(i, presences.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    GDDate date = new GDDate(working.get(m1).getDate());
                    GDDate date2 = new GDDate(working.get(m2).getDate());
                    if (date.after(date2)) {
                        presences.set(i + lowerValue, working.get(m2++));
                    } else {
                        presences.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    presences.set(i + lowerValue, working.get(m2++));
                }
            } else {
                presences.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortPresencesByNameAndDateTime(List<Presence> presenceList) {
        try {
            sortPresencesByNameAndDateTime(presenceList, 0, presenceList.size() - 1);
        } catch (Exception e) {
            logger.error("MargeSortAlgothm sortRegistrationAppointmentByName Exception: ", e);
        }
    }

    private void sortPresencesByNameAndDateTime(List<Presence> presencesList, int lowerValue, int hightValue) throws Exception {
        Collator collator = Collator.getInstance(Locale.getDefault());
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortPresencesByNameAndDateTime(presencesList, lowerValue, pivot);
        sortPresencesByNameAndDateTime(presencesList, pivot + 1, hightValue);
        List<Presence> working = new ArrayList<Presence>();
        for (int i = 0; i < length; i++) working.add(i, presencesList.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    String name = working.get(m1).getRegistrationAppointment().getRegistration().getStudent().getName();
                    String name2 = working.get(m2).getRegistrationAppointment().getRegistration().getStudent().getName();
                    if (collator.compare(name, name2) > 0) {
                        presencesList.set(i + lowerValue, working.get(m2++));
                    } else {
                        if (name.equals(name2)) {
                            GDDate date = new GDDate(working.get(m1).getDate());
                            GDDate date2 = new GDDate(working.get(m2).getDate());
                            if (date.after(date2)) {
                                presencesList.set(i + lowerValue, working.get(m2++));
                            } else {
                                presencesList.set(i + lowerValue, working.get(m1++));
                            }
                        } else {
                            presencesList.set(i + lowerValue, working.get(m1++));
                        }
                    }
                } else {
                    presencesList.set(i + lowerValue, working.get(m2++));
                }
            } else {
                presencesList.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortRegistrationByDate(List<Registration> registrationsData) {
        try {
            sortRegistrationByDate(registrationsData, 0, registrationsData.size() - 1);
        } catch (Exception e) {
            logger.error("Sort Registration By Date Error: ", e);
        }
    }

    private void sortRegistrationByDate(List<Registration> registrationsData, int lowerValue, int hightValue) throws Exception {
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortRegistrationByDate(registrationsData, lowerValue, pivot);
        sortRegistrationByDate(registrationsData, pivot + 1, hightValue);
        List<Registration> working = new ArrayList<Registration>();
        for (int i = 0; i < length; i++) working.add(i, registrationsData.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    GDDate payDate = new GDDate(working.get(m1).getRegistrationDate());
                    GDDate payDate2 = new GDDate(working.get(m2).getRegistrationDate());
                    if (payDate.afterDay(payDate2)) {
                        registrationsData.set(i + lowerValue, working.get(m2++));
                    } else {
                        registrationsData.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    registrationsData.set(i + lowerValue, working.get(m2++));
                }
            } else {
                registrationsData.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortRegistrationByEndDate(List<Registration> registrationsData) {
        try {
            sortRegistrationByEndDate(registrationsData, 0, registrationsData.size() - 1);
        } catch (Exception e) {
            logger.error("MargeSortAlgothm sortRegistrationByEndDate Exception: ", e);
        }
    }

    private void sortRegistrationByEndDate(List<Registration> registrationsData, int lowerValue, int hightValue) throws Exception {
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortRegistrationByEndDate(registrationsData, lowerValue, pivot);
        sortRegistrationByEndDate(registrationsData, pivot + 1, hightValue);
        List<Registration> working = new ArrayList<Registration>();
        for (int i = 0; i < length; i++) working.add(i, registrationsData.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    GDDate payDate = working.get(m1).getEndDate();
                    GDDate payDate2 = working.get(m2).getEndDate();
                    if (payDate.afterDay(payDate2)) {
                        registrationsData.set(i + lowerValue, working.get(m2++));
                    } else {
                        registrationsData.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    registrationsData.set(i + lowerValue, working.get(m2++));
                }
            } else {
                registrationsData.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortRegistrationById(List<Registration> registrationsID) {
        try {
            sortRegistrationById(registrationsID, 0, registrationsID.size() - 1);
        } catch (Exception e) {
            logger.error("sorteRegistrationByName Error: ", e);
        }
    }

    private void sortRegistrationById(List<Registration> registrationsID, int lowerValue, int hightValue) throws Exception {
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortRegistrationById(registrationsID, lowerValue, pivot);
        sortRegistrationById(registrationsID, pivot + 1, hightValue);
        List<Registration> working = new ArrayList<Registration>();
        for (int i = 0; i < length; i++) working.add(i, registrationsID.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    Integer id = new Integer(working.get(m1).getIdRegistration());
                    Integer id2 = new Integer(working.get(m2).getIdRegistration());
                    if (id.compareTo(id2) > 0) {
                        registrationsID.set(i + lowerValue, working.get(m2++));
                    } else {
                        registrationsID.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    registrationsID.set(i + lowerValue, working.get(m2++));
                }
            } else {
                registrationsID.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortRegistrationByName(List<Registration> registrationsData) {
        try {
            sortRegistrationByName(registrationsData, 0, registrationsData.size() - 1);
        } catch (Exception e) {
            logger.error("sorteRegistrationByName Error: ", e);
        }
    }

    private void sortRegistrationByName(List<Registration> registrationsData, int lowerValue, int hightValue) throws Exception {
        Collator collator = Collator.getInstance(Locale.getDefault());
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortRegistrationByName(registrationsData, lowerValue, pivot);
        sortRegistrationByName(registrationsData, pivot + 1, hightValue);
        List<Registration> working = new ArrayList<Registration>();
        for (int i = 0; i < length; i++) working.add(i, registrationsData.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    String name = working.get(m1).getStudent().getName();
                    String name2 = working.get(m2).getStudent().getName();
                    if (collator.compare(name, name2) > 0) {
                        registrationsData.set(i + lowerValue, working.get(m2++));
                    } else {
                        registrationsData.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    registrationsData.set(i + lowerValue, working.get(m2++));
                }
            } else {
                registrationsData.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortRegistrationByDateOfLastParcel(List<Registration> registrationsData) {
        try {
            sortRegistrationByDateOfLastParcel(registrationsData, 0, registrationsData.size() - 1);
        } catch (Exception e) {
            logger.error("sorteRegistrationByDateOfLastParcel Error: ", e);
        }
    }

    private void sortRegistrationByDateOfLastParcel(List<Registration> registrationsData, int lowerValue, int hightValue) throws Exception {
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortRegistrationByDateOfLastParcel(registrationsData, lowerValue, pivot);
        sortRegistrationByDateOfLastParcel(registrationsData, pivot + 1, hightValue);
        List<Registration> working = new ArrayList<Registration>();
        for (int i = 0; i < length; i++) working.add(i, registrationsData.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    GDDate payDate = new GDDate(working.get(m1).getParcel(working.get(m1).getParcels().size() - 1).getDate());
                    GDDate payDate2 = new GDDate(working.get(m2).getParcel(working.get(m2).getParcels().size() - 1).getDate());
                    if (payDate.afterDay(payDate2)) {
                        registrationsData.set(i + lowerValue, working.get(m2++));
                    } else {
                        registrationsData.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    registrationsData.set(i + lowerValue, working.get(m2++));
                }
            } else {
                registrationsData.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortStateByName(List<State> statesList) {
        try {
            sortStateByName(statesList, 0, statesList.size() - 1);
        } catch (Exception e) {
            logger.error("sorteS Error: ", e);
        }
    }

    private void sortStateByName(List<State> statesList, int lowerValue, int hightValue) throws Exception {
        Collator collator = Collator.getInstance(Locale.getDefault());
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortStateByName(statesList, lowerValue, pivot);
        sortStateByName(statesList, pivot + 1, hightValue);
        List<State> working = new ArrayList<State>();
        for (int i = 0; i < length; i++) working.add(i, statesList.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    String stateName1 = working.get(m1).getAcronym();
                    String stateName2 = working.get(m2).getAcronym();
                    if (collator.compare(stateName1, stateName2) > 0) {
                        statesList.set(i + lowerValue, working.get(m2++));
                    } else {
                        statesList.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    statesList.set(i + lowerValue, working.get(m2++));
                }
            } else {
                statesList.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortCitiesByName(List<City> citiesList) {
        try {
            sortCitiesByName(citiesList, 0, citiesList.size() - 1);
        } catch (Exception e) {
            logger.error("sorteCitiesByName Error: ", e);
        }
    }

    private void sortCitiesByName(List<City> citiesList, int lowerValue, int hightValue) throws Exception {
        Collator collator = Collator.getInstance(Locale.getDefault());
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortCitiesByName(citiesList, lowerValue, pivot);
        sortCitiesByName(citiesList, pivot + 1, hightValue);
        List<City> working = new ArrayList<City>();
        for (int i = 0; i < length; i++) working.add(i, citiesList.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    String stateName1 = working.get(m1).getCityName();
                    String stateName2 = working.get(m2).getCityName();
                    if (collator.compare(stateName1, stateName2) > 0) {
                        citiesList.set(i + lowerValue, working.get(m2++));
                    } else {
                        citiesList.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    citiesList.set(i + lowerValue, working.get(m2++));
                }
            } else {
                citiesList.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortBankBilletByDateAndName(List<Billet> billets) {
        try {
            sortBankBilletByDateAndName(billets, 0, billets.size() - 1);
        } catch (Exception e) {
            logger.error("MargeSortAlgothm  sortBankBilletByDateAndName Exception: ", e);
        }
    }

    private void sortBankBilletByDateAndName(List<Billet> billetsList, int lowerValue, int hightValue) throws Exception {
        Collator collator = Collator.getInstance(Locale.getDefault());
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortBankBilletByDateAndName(billetsList, lowerValue, pivot);
        sortBankBilletByDateAndName(billetsList, pivot + 1, hightValue);
        List<Billet> working = new ArrayList<Billet>();
        for (int i = 0; i < length; i++) working.add(i, billetsList.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    long date1 = new GDDate(working.get(m1).getParcel().getDate()).getTimeInMillis();
                    long date2 = new GDDate(working.get(m2).getParcel().getDate()).getTimeInMillis();
                    String name1 = working.get(m1).getPerson().getName();
                    String name2 = working.get(m2).getPerson().getName();
                    if (collator.compare(name1, name2) > 0) {
                        billetsList.set(i + lowerValue, working.get(m2++));
                    } else if (collator.compare(name1, name2) < 0) {
                        billetsList.set(i + lowerValue, working.get(m1++));
                    } else {
                        if (date1 > date2) {
                            billetsList.set(i + lowerValue, working.get(m2++));
                        } else {
                            billetsList.set(i + lowerValue, working.get(m1++));
                        }
                    }
                } else {
                    billetsList.set(i + lowerValue, working.get(m2++));
                }
            } else {
                billetsList.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortRegistrationAppointmentByName(List<RegistrationAppointment> registrationList) {
        try {
            sortRegistrationAppointmentByName(registrationList, 0, registrationList.size() - 1);
        } catch (Exception e) {
            logger.error("MargeSortAlgothm sortRegistrationAppointmentByName Exception: ", e);
        }
    }

    private void sortRegistrationAppointmentByName(List<RegistrationAppointment> registrationList, int lowerValue, int hightValue) throws Exception {
        Collator collator = Collator.getInstance(Locale.getDefault());
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortRegistrationAppointmentByName(registrationList, lowerValue, pivot);
        sortRegistrationAppointmentByName(registrationList, pivot + 1, hightValue);
        List<RegistrationAppointment> working = new ArrayList<RegistrationAppointment>();
        for (int i = 0; i < length; i++) working.add(i, registrationList.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    String name = working.get(m1).getRegistration().getStudent().getName();
                    String name2 = working.get(m2).getRegistration().getStudent().getName();
                    if (collator.compare(name, name2) > 0) {
                        registrationList.set(i + lowerValue, working.get(m2++));
                    } else {
                        registrationList.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    registrationList.set(i + lowerValue, working.get(m2++));
                }
            } else {
                registrationList.set(i + lowerValue, working.get(m1++));
            }
        }
    }

    public void sortFlowBankAccountDataPerDate(List<List<String>> flowBankAccountDataList) {
        try {
            sortFlowBankAccountDataPerDate(flowBankAccountDataList, 0, flowBankAccountDataList.size() - 1);
        } catch (Exception e) {
            logger.error("MargeSortAlgothm sortFlowBankAccountDataPerDate Exception: ", e);
        }
    }

    private void sortFlowBankAccountDataPerDate(List<List<String>> flowBankAccountDataList, int lowerValue, int hightValue) throws Exception {
        Collator collator = Collator.getInstance(Locale.getDefault());
        if (lowerValue == hightValue) {
            return;
        }
        int length = hightValue - lowerValue + 1;
        int pivot = (lowerValue + hightValue) / 2;
        sortFlowBankAccountDataPerDate(flowBankAccountDataList, lowerValue, pivot);
        sortFlowBankAccountDataPerDate(flowBankAccountDataList, pivot + 1, hightValue);
        List<List<String>> working = new ArrayList<List<String>>();
        for (int i = 0; i < length; i++) working.add(i, flowBankAccountDataList.get(lowerValue + i));
        int m1 = 0;
        int m2 = pivot - lowerValue + 1;
        for (int i = 0; i < length; i++) {
            if (m2 <= hightValue - lowerValue) {
                if (m1 <= pivot - lowerValue) {
                    GDDate date1 = new GDDate(working.get(m1).get(1));
                    GDDate date2 = new GDDate(working.get(m2).get(1));
                    if (date1.afterDay(date2)) {
                        flowBankAccountDataList.set(i + lowerValue, working.get(m2++));
                    } else {
                        flowBankAccountDataList.set(i + lowerValue, working.get(m1++));
                    }
                } else {
                    flowBankAccountDataList.set(i + lowerValue, working.get(m2++));
                }
            } else {
                flowBankAccountDataList.set(i + lowerValue, working.get(m1++));
            }
        }
    }
}
