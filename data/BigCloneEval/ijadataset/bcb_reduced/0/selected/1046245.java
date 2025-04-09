package org.guestshome.businessobjects.statistics;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import javax.persistence.*;
import java.math.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.hssf.record.formula.functions.*;
import java.io.*;
import org.extutils.web.*;
import org.guestshome.businessobjects.BusinessObjectsFacade;
import org.guestshome.commons.*;
import org.guestshome.entities.Facility;
import org.guestshome.entities.GuestPathology;
import org.guestshome.entities.GuestReception;
import org.guestshome.entities.Person;
import org.guestshome.entities.ReceptionType;
import org.guestshome.entities.WaitingList;
import org.sqlutils.ListCommand;
import org.sqlutils.ListResponse;
import org.sqlutils.jpa.JPAMethods;
import org.sqlutils.jpa.JPASelectStatement;
import org.sqlutils.logger.Logger;

/**
 * <p>Title: GuestsHome application</p>
 * <p>Description: Business object used to generate statistics about
 * waiting lists; statistics are expressed in XLS format.</p>
 * <p>Copyright: Copyright (C) 2009 Informatici senza frontiere</p>
 *
 * This application is free software; you can redistribute it and/or
 * modify it under the terms of the (LGPL) Lesser General Public
 * License as published by the Free Software Foundation;
 *
 *                GNU LESSER GENERAL PUBLIC LICENSE
 *                 Version 2.1, February 1999
 *
 * This application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * @author Mauro Carniel
 * @version 1.0
 *
 */
public class WaitingListStatsBO {

    /**
	 * @return statistics about guests leaving, expressed in XLS format
	 * @throws an exception in case of errors
	 */
    public byte[] getWaitingListStats(String username, EntityManager em, int year, int waitingListId, UserInfo userInfo, PropertiesResourcesFactory factory) throws Throwable {
        try {
            String path = this.getClass().getResource("/").getPath().replaceAll("%20", " ");
            String xlsFile = path + "waitingpeoplestats_" + factory.getResources().getLanguageId() + ".xls";
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(xlsFile));
            CustomWorkbook wb = new CustomWorkbook(fs);
            HSSFRow row = null;
            HSSFCell cell = null;
            HSSFSheet s = wb.getSheetAt(0);
            HSSFSheet newsheet = null;
            cell = s.getRow(3).getCell(4);
            cell.setCellValue(cell.getStringCellValue() + " " + userInfo.getFacilitiesCity());
            cell = s.getRow(23).getCell(4);
            cell.setCellValue(cell.getStringCellValue() + " " + userInfo.getFacilitiesCity());
            cell = s.getRow(43).getCell(4);
            cell.setCellValue(cell.getStringCellValue() + " " + userInfo.getFacilitiesCity());
            cell = s.getRow(3).getCell(7);
            cell.setCellValue(cell.getStringCellValue() + " " + userInfo.getFacilitiesCity());
            cell = s.getRow(23).getCell(7);
            cell.setCellValue(cell.getStringCellValue() + " " + userInfo.getFacilitiesCity());
            cell = s.getRow(43).getCell(7);
            cell.setCellValue(cell.getStringCellValue() + " " + userInfo.getFacilitiesCity());
            cell = s.getRow(63).getCell(9);
            cell.setCellValue(cell.getStringCellValue() + " " + userInfo.getFacilitiesCity());
            cell = s.getRow(83).getCell(9);
            cell.setCellValue(cell.getStringCellValue() + " " + userInfo.getFacilitiesCity());
            cell = s.getRow(103).getCell(9);
            cell.setCellValue(cell.getStringCellValue() + " " + userInfo.getFacilitiesCity());
            Object[][] data = getWaitingListStats(username, em, year, waitingListId, userInfo.getFacilitiesCountry(), userInfo.getFacilitiesCity(), null);
            int r = 4;
            int[] totals = new int[8];
            for (int i = 0; i < 12; i++) {
                row = s.getRow(r);
                for (int j = 0; j < 8; j++) {
                    cell = row.getCell(j + 1);
                    if (data[i][j] != null) {
                        cell.setCellValue((Integer) data[i][j]);
                        totals[j] += (Integer) data[i][j];
                    }
                }
                r++;
            }
            row = s.getRow(r);
            for (int j = 0; j < 8; j++) {
                cell = row.getCell(j + 1);
                if (j != 2) cell.setCellValue(totals[j]);
            }
            data = getWaitingListStats(username, em, year, waitingListId, userInfo.getFacilitiesCountry(), userInfo.getFacilitiesCity(), "M");
            r = 24;
            totals = new int[8];
            for (int i = 0; i < 12; i++) {
                row = s.getRow(r);
                for (int j = 0; j < 8; j++) {
                    cell = row.getCell(j + 1);
                    if (data[i][j] != null) {
                        cell.setCellValue((Integer) data[i][j]);
                        totals[j] += (Integer) data[i][j];
                    }
                }
                r++;
            }
            row = s.getRow(r);
            for (int j = 0; j < 8; j++) {
                cell = row.getCell(j + 1);
                if (j != 2) cell.setCellValue(totals[j]);
            }
            data = getWaitingListStats(username, em, year, waitingListId, userInfo.getFacilitiesCountry(), userInfo.getFacilitiesCity(), "F");
            r = 44;
            totals = new int[8];
            for (int i = 0; i < 12; i++) {
                row = s.getRow(r);
                for (int j = 0; j < 8; j++) {
                    cell = row.getCell(j + 1);
                    if (data[i][j] != null) {
                        cell.setCellValue((Integer) data[i][j]);
                        totals[j] += (Integer) data[i][j];
                    }
                }
                r++;
            }
            row = s.getRow(r);
            for (int j = 0; j < 8; j++) {
                cell = row.getCell(j + 1);
                if (j != 2) cell.setCellValue(totals[j]);
            }
            data = getWaitingListStatsPerSexAgeCountry(username, em, year, waitingListId, userInfo.getFacilitiesCountry(), userInfo.getFacilitiesCity(), null);
            r = 64;
            totals = new int[11];
            for (int i = 0; i < 12; i++) {
                row = s.getRow(r);
                for (int j = 0; j < 11; j++) {
                    cell = row.getCell(j + 1);
                    if (data[i][j] != null) {
                        cell.setCellValue((Integer) data[i][j]);
                        totals[j] += (Integer) data[i][j];
                    }
                }
                r++;
            }
            row = s.getRow(r);
            if (row != null) for (int j = 0; j < 11; j++) {
                cell = row.getCell(j + 1);
                cell.setCellValue(totals[j]);
            }
            data = getWaitingListStatsPerSexAgeCountry(username, em, year, waitingListId, userInfo.getFacilitiesCountry(), userInfo.getFacilitiesCity(), "M");
            r = 84;
            totals = new int[11];
            for (int i = 0; i < 12; i++) {
                row = s.getRow(r);
                for (int j = 0; j < 11; j++) {
                    cell = row.getCell(j + 1);
                    if (data[i][j] != null) {
                        cell.setCellValue((Integer) data[i][j]);
                        totals[j] += (Integer) data[i][j];
                    }
                }
                r++;
            }
            row = s.getRow(r);
            if (row != null) for (int j = 0; j < 11; j++) {
                cell = row.getCell(j + 1);
                cell.setCellValue(totals[j]);
            }
            data = getWaitingListStatsPerSexAgeCountry(username, em, year, waitingListId, userInfo.getFacilitiesCountry(), userInfo.getFacilitiesCity(), "F");
            r = 104;
            totals = new int[11];
            for (int i = 0; i < 12; i++) {
                row = s.getRow(r);
                for (int j = 0; j < 11; j++) {
                    cell = row.getCell(j + 1);
                    if (data[i][j] != null) {
                        cell.setCellValue((Integer) data[i][j]);
                        totals[j] += (Integer) data[i][j];
                    }
                }
                r++;
            }
            row = s.getRow(r);
            if (row != null) for (int j = 0; j < 11; j++) {
                cell = row.getCell(j + 1);
                cell.setCellValue(totals[j]);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            out.close();
            return out.toByteArray();
        } catch (Throwable ex) {
            Logger.error(null, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
	 * @return number of person in waiting list for the specified year, for each month of the year
	 * If "sex" is not null, then statistics are filtered per specified sex.
	 * @throws an exception in case of errors
	 */
    private Object[][] getWaitingListStats(String username, EntityManager em, int year, int waitingListId, String facilityCountryCode, String facilityCity, String sex) throws Throwable {
        try {
            Calendar cal = Calendar.getInstance();
            Date startDate = null;
            Date endDate = null;
            int day;
            Object[][] res = new Object[12][8];
            Query q = null;
            List<Object> params = new ArrayList<Object>();
            List list = null;
            for (int i = 0; i < 12; i++) {
                cal.set(cal.YEAR, year);
                cal.set(cal.MONTH, i);
                cal.set(cal.DAY_OF_MONTH, 1);
                cal.set(cal.HOUR_OF_DAY, 0);
                cal.set(cal.MINUTE, 0);
                cal.set(cal.SECOND, 0);
                cal.set(cal.MILLISECOND, 0);
                startDate = new java.sql.Date(cal.getTimeInMillis());
                switch(i) {
                    case 1:
                        day = 28;
                        break;
                    case 10:
                        day = 30;
                        break;
                    case 3:
                        day = 30;
                        break;
                    case 5:
                        day = 30;
                        break;
                    case 8:
                        day = 30;
                        break;
                    default:
                        day = 31;
                        break;
                }
                cal.set(cal.MONTH, i);
                cal.set(cal.DAY_OF_MONTH, day);
                cal.set(cal.HOUR_OF_DAY, 23);
                cal.set(cal.MINUTE, 59);
                cal.set(cal.SECOND, 59);
                cal.set(cal.MILLISECOND, 999);
                endDate = new java.sql.Date(cal.getTimeInMillis());
                String sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.pk.waitingListsId= ?1 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                list = JPAMethods.getResultList(username, q, params);
                int n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?4 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                list = JPAMethods.getResultList(username, q, params);
                res[i][0] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted = ?1 and " + "gr.waitingListId=?4 and " + "gr.entryDate>= ?2 and gr.entryDate<= ?3 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                list = JPAMethods.getResultList(username, q, params);
                res[i][1] = new Integer(list.get(0).toString());
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted = ?1 and " + "gr.waitingListId=?4 and " + "gr.entryDate>= ?2 and gr.entryDate<= ?3 and " + "gr.people.birthCountryCode is not null and " + "gr.people.birthCountryCode = ?5 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                params.add(facilityCountryCode);
                list = JPAMethods.getResultList(username, q, params);
                res[i][2] = new Integer(list.get(0).toString());
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted = ?1 and " + "gr.waitingListId=?4 and " + "gr.entryDate>= ?2 and gr.entryDate<= ?3 and " + "gr.people.birthCountryCode is not null and " + "gr.people.birthCountryCode = ?5 and " + "gr.people.lastResidenceCity is not null and " + "gr.people.lastResidenceCity = ?6 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                params.add(facilityCountryCode);
                params.add(facilityCity);
                list = JPAMethods.getResultList(username, q, params);
                res[i][3] = new Integer(list.get(0).toString());
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted = ?1 and " + "gr.waitingListId=?4 and " + "gr.entryDate>= ?2 and gr.entryDate<= ?3 and " + "gr.people.birthCountryCode is not null and " + "gr.people.birthCountryCode <> ?5 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                params.add(facilityCountryCode);
                list = JPAMethods.getResultList(username, q, params);
                res[i][4] = new Integer(list.get(0).toString());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.pk.waitingListsId= ?1 and " + "wp.people.birthCountryCode is not null and " + "wp.people.birthCountryCode = ?4 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                params.add(facilityCountryCode);
                list = JPAMethods.getResultList(username, q, params);
                n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?4 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.birthCountryCode is not null and " + "gr.people.birthCountryCode = ?5 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                params.add(facilityCountryCode);
                list = JPAMethods.getResultList(username, q, params);
                res[i][5] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.pk.waitingListsId= ?1 and " + "wp.people.birthCountryCode is not null and " + "wp.people.birthCountryCode = ?4 and " + "wp.people.lastResidenceCity is not null and " + "wp.people.lastResidenceCity = ?5 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                params.add(facilityCountryCode);
                params.add(facilityCity);
                list = JPAMethods.getResultList(username, q, params);
                n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?4 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.birthCountryCode is not null and " + "gr.people.birthCountryCode = ?5 and " + "gr.people.lastResidenceCity is not null and " + "gr.people.lastResidenceCity = ?6 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                params.add(facilityCountryCode);
                params.add(facilityCity);
                list = JPAMethods.getResultList(username, q, params);
                res[i][6] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.pk.waitingListsId= ?1 and " + "wp.people.isPoliticalRefugee='Y' " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                list = JPAMethods.getResultList(username, q, params);
                n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?4 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.isPoliticalRefugee='Y' " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                list = JPAMethods.getResultList(username, q, params);
                res[i][7] = new Integer(n + ((Number) list.get(0)).intValue());
            }
            return res;
        } catch (Throwable ex) {
            Logger.error(null, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
	 * @param year year used to calculate the required date
	 * @param firstDate <code>true</code> first day of year, otherwise last day of year 
	 * @return date first day of year or last day of year, related to today-year
	 */
    private Timestamp getDay(int year, boolean firstDay) {
        Calendar cal = Calendar.getInstance();
        year = cal.get(cal.YEAR) - year;
        cal.set(cal.YEAR, year);
        cal.set(cal.MONTH, firstDay ? 0 : 11);
        cal.set(cal.DAY_OF_MONTH, firstDay ? 1 : 31);
        cal.set(cal.HOUR_OF_DAY, firstDay ? 0 : 23);
        cal.set(cal.MINUTE, firstDay ? 0 : 59);
        cal.set(cal.SECOND, firstDay ? 0 : 59);
        cal.set(cal.MILLISECOND, firstDay ? 0 : 999);
        return new Timestamp(cal.getTimeInMillis());
    }

    /**
	 * @return number of waiting people recorded in the specified year, for each month of the year and sex/age/country
	 * If "sex" is not null, then statistics are filtered per specified sex.
	 * @throws an exception in case of errors
	 */
    private Object[][] getWaitingListStatsPerSexAgeCountry(String username, EntityManager em, int year, int waitingListId, String facilityCountryCode, String facilityCity, String sex) throws Throwable {
        try {
            Calendar cal = Calendar.getInstance();
            Date startDate = null;
            Date endDate = null;
            int day;
            Object[][] res = new Object[12][11];
            Query q = null;
            List<Object> params = new ArrayList<Object>();
            List list = null;
            for (int i = 0; i < 12; i++) {
                cal.set(cal.YEAR, year);
                cal.set(cal.MONTH, i);
                cal.set(cal.DAY_OF_MONTH, 1);
                cal.set(cal.HOUR_OF_DAY, 0);
                cal.set(cal.MINUTE, 0);
                cal.set(cal.SECOND, 0);
                cal.set(cal.MILLISECOND, 0);
                startDate = new java.sql.Date(cal.getTimeInMillis());
                switch(i) {
                    case 1:
                        day = 28;
                        break;
                    case 10:
                        day = 30;
                        break;
                    case 3:
                        day = 30;
                        break;
                    case 5:
                        day = 30;
                        break;
                    case 8:
                        day = 30;
                        break;
                    default:
                        day = 31;
                        break;
                }
                cal.set(cal.MONTH, i);
                cal.set(cal.DAY_OF_MONTH, day);
                cal.set(cal.HOUR_OF_DAY, 23);
                cal.set(cal.MINUTE, 59);
                cal.set(cal.SECOND, 59);
                cal.set(cal.MILLISECOND, 999);
                endDate = new java.sql.Date(cal.getTimeInMillis());
                String sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.pk.waitingListsId= ?1 and " + "wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.people.birthDate>= ?4 and wp.people.birthDate<= ?5 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(29, true));
                params.add(getDay(18, false));
                list = JPAMethods.getResultList(username, q, params);
                int n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?6 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.birthDate>= ?4 and gr.people.birthDate<= ?5" + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(29, true));
                params.add(getDay(18, false));
                params.add(waitingListId);
                list = JPAMethods.getResultList(username, q, params);
                res[i][0] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.pk.waitingListsId= ?1 and " + "wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.people.birthDate>= ?4 and wp.people.birthDate<= ?5 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(39, true));
                params.add(getDay(30, false));
                list = JPAMethods.getResultList(username, q, params);
                n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?6 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.birthDate>= ?4 and gr.people.birthDate<= ?5" + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(39, true));
                params.add(getDay(30, false));
                params.add(waitingListId);
                list = JPAMethods.getResultList(username, q, params);
                res[i][1] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.pk.waitingListsId= ?1 and " + "wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.people.birthDate>= ?4 and wp.people.birthDate<= ?5 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(49, true));
                params.add(getDay(40, false));
                list = JPAMethods.getResultList(username, q, params);
                n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?6 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.birthDate>= ?4 and gr.people.birthDate<= ?5" + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(49, true));
                params.add(getDay(40, false));
                params.add(waitingListId);
                list = JPAMethods.getResultList(username, q, params);
                res[i][2] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.pk.waitingListsId= ?1 and " + "wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.people.birthDate>= ?4 and wp.people.birthDate<= ?5 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(64, true));
                params.add(getDay(50, false));
                list = JPAMethods.getResultList(username, q, params);
                n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?6 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.birthDate>= ?4 and gr.people.birthDate<= ?5" + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(64, true));
                params.add(getDay(50, false));
                params.add(waitingListId);
                list = JPAMethods.getResultList(username, q, params);
                res[i][3] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.pk.waitingListsId= ?1 and " + "wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.people.birthDate>= ?4 and wp.people.birthDate<= ?5 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(74, true));
                params.add(getDay(65, false));
                list = JPAMethods.getResultList(username, q, params);
                n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?6 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.birthDate>= ?4 and gr.people.birthDate<= ?5" + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(74, true));
                params.add(getDay(65, false));
                params.add(waitingListId);
                list = JPAMethods.getResultList(username, q, params);
                res[i][4] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.pk.waitingListsId= ?1 and " + "wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.people.birthDate<= ?4 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(75, false));
                list = JPAMethods.getResultList(username, q, params);
                n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?5 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.birthDate<= ?4 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(getDay(75, false));
                params.add(waitingListId);
                list = JPAMethods.getResultList(username, q, params);
                res[i][5] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.pk.waitingListsId= ?1 and " + "wp.people.birthCountryCode is not null and " + "wp.people.birthCountryCode = ?4 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                params.add(facilityCountryCode);
                list = JPAMethods.getResultList(username, q, params);
                n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?4 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.birthCountryCode is not null and " + "gr.people.birthCountryCode = ?5 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                params.add(facilityCountryCode);
                list = JPAMethods.getResultList(username, q, params);
                res[i][6] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.pk.waitingListsId= ?1 and " + "wp.people.birthCountryCode is not null and " + "wp.people.birthCountryCode <> ?4 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                params.add(facilityCountryCode);
                list = JPAMethods.getResultList(username, q, params);
                n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?4 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.birthCountryCode is not null and " + "gr.people.birthCountryCode <> ?5 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                params.add(facilityCountryCode);
                list = JPAMethods.getResultList(username, q, params);
                res[i][7] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.pk.waitingListsId= ?1 and " + "wp.people.birthCountryCode is not null and " + "wp.people.birthCountryCode = ?4 and " + "wp.people.lastResidenceCity is not null and " + "wp.people.lastResidenceCity = ?5 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                params.add(facilityCountryCode);
                params.add(facilityCity);
                list = JPAMethods.getResultList(username, q, params);
                n = ((Number) list.get(0)).intValue();
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?4 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.birthCountryCode is not null and " + "gr.people.birthCountryCode = ?5 and " + "gr.people.lastResidenceCity is not null and " + "gr.people.lastResidenceCity = ?6 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                params.add(facilityCountryCode);
                params.add(facilityCity);
                list = JPAMethods.getResultList(username, q, params);
                res[i][8] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.pk.waitingListsId= ?1 and " + "wp.people.isPoliticalRefugee='Y' " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                list = JPAMethods.getResultList(username, q, params);
                n = new Integer(list.get(0).toString());
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?4 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 and " + "gr.people.isPoliticalRefugee='Y' " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                list = JPAMethods.getResultList(username, q, params);
                res[i][9] = new Integer(n + ((Number) list.get(0)).intValue());
                sql = "select count(wp) from WaitingPerson wp inner join fetch wp.people " + "where wp.waitingDate>= ?2 and wp.waitingDate<= ?3 and " + "wp.pk.waitingListsId= ?1 " + (sex != null ? " and wp.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(waitingListId);
                params.add(startDate);
                params.add(endDate);
                list = JPAMethods.getResultList(username, q, params);
                n = new Integer(list.get(0).toString());
                sql = "select count(gr) from GuestReception gr inner join fetch gr.people " + "where gr.deleted= ?1 and " + "gr.waitingListId=?4 and " + "gr.waitingDate>= ?2 and gr.waitingDate<= ?3 " + (sex != null ? " and gr.people.sex='" + sex + "'" : "");
                q = em.createQuery(sql);
                params.clear();
                params.add(Consts.FLAG_N);
                params.add(startDate);
                params.add(endDate);
                params.add(waitingListId);
                list = JPAMethods.getResultList(username, q, params);
                res[i][10] = new Integer(n + ((Number) list.get(0)).intValue());
            }
            return res;
        } catch (Throwable ex) {
            Logger.error(null, ex.getMessage(), ex);
            throw ex;
        }
    }
}
