package util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.grlea.log.SimpleLogger;

public class Check {

    private static final SimpleLogger LOG = new SimpleLogger(Check.class);

    /**
	 * Prüft ob der String eine E-Mail Adresse ist
	 * @param email
	 * @return
	 */
    public boolean isEmail(final String email) {
        boolean check = false;
        try {
            final InternetAddress iAdr = new InternetAddress();
            iAdr.setAddress(email);
            try {
                iAdr.validate();
                final Pattern pat = Pattern.compile("[A-Za-z0-9._-]+@[A-Za-z0-9][A-Za-z0-9.-]{0,61}[A-Za-z0-9]\\.[A-Za-z.]{2,6}");
                final Matcher match = pat.matcher(email);
                if (match.find()) {
                    check = true;
                }
            } catch (final AddressException e1) {
                LOG.info("isEmail: " + email + " " + e1.toString());
            }
        } catch (final Exception e) {
            LOG.error("isEmail: " + email + " " + e.toString());
        }
        return check;
    }

    /**
	 * Stellt sicher, dass der String nicht null ist und einen Wert der Mindestlänge l hat
	 * @param input
	 * @param len
	 * @return
	 */
    public boolean isMinLength(final String input, final int len) {
        boolean check = false;
        if (input != null && input.length() >= len) {
            check = true;
        }
        return check;
    }

    /**
	 * Stellt sicher, dass der String nicht null ist und einen Wert der genauen Länge l hat
	 * @param input
	 * @param len
	 * @return
	 */
    public boolean isExactLength(final String input, final int len) {
        boolean check = false;
        if (input != null && input.length() == len) {
            check = true;
        }
        return check;
    }

    /**
	 * Ueeberprueft, ob die Länge des Strings sich zwischen einem Minimum und einem Maximum befindet<br>
	 * min <= s <= max<p>
	 *
	 * @param input
	 * @param min
	 * @param max
	 * @return
	 */
    public boolean isLengthInMinMax(final String input, final int min, final int max) {
        boolean check = false;
        if (input != null && min <= input.length() && input.length() <= max) {
            check = true;
        }
        return check;
    }

    /**
	 * This method checks if a String is a valid URL
	 */
    public boolean isUrl(final String link) {
        boolean check = true;
        try {
            final URL url = new URL(link);
            LOG.ludicrous("Gültige URL: " + url);
        } catch (final MalformedURLException e) {
            LOG.info("isUrl: " + link + "\040" + e.toString());
            check = false;
        }
        return check;
    }

    /**
	 * This method checks if a String is a valid URL and from a specific filetype
	 */
    public boolean isUrlAndFiletype(final String link, final String[] filetypes) {
        boolean check = false;
        try {
            final String extension = link.substring(link.lastIndexOf('.') + 1);
            for (final String filetype : filetypes) {
                if (extension.equalsIgnoreCase(filetype)) {
                    check = true;
                }
            }
            if (link.length() > 254) {
                check = false;
            }
            if (check) {
                check = isUrl(link);
            }
        } catch (final Exception e) {
            check = false;
        }
        return check;
    }

    /**
	 * This method checks if a String ends with a valid filetype extension
	 *
	 * @param String path
	 * @return boolean check
	 */
    public boolean isFiletypeExtension(String fileName, String extension) {
        boolean check = false;
        if (fileName != null && extension != null && fileName.length() > extension.length()) {
            fileName = fileName.toLowerCase();
            extension = extension.toLowerCase();
            try {
                if (fileName.contains(extension) && fileName.lastIndexOf(extension) == fileName.length() - extension.length()) {
                    check = true;
                }
            } catch (final Exception e) {
                check = false;
            }
        }
        return check;
    }

    /**
	 * Extrahiert aus einem String alle Wörter und Zahlen (Regexp: \w   A word character: [a-zA-Z_0-9])
	 * @param input
	 * @return ArrayList words
	 */
    public List<String> getAlphanumericWordCharacters(final String input) {
        final List<String> words = new ArrayList<String>();
        if (input != null) {
            final Pattern pat = Pattern.compile("([\\p{L}||\\P{Alpha}&&[^\\p{Punct}]&&[^\\p{Space}]])*");
            final Matcher match = pat.matcher(input);
            while (match.find()) {
                words.add(input.substring(match.start(), match.end()));
            }
        }
        return words;
    }

    /**
	 * Counts the occurence of a character in a string
	 */
    public int countCharacterInString(final String input, final String countString) {
        return input.split("\\Q" + countString + "\\E", -1).length - 1;
    }

    /**
	 * This method checks if a String contains only numbers
	 */
    public boolean containsOnlyNumbers(final String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        final int max = str.length();
        for (int i = 0; i < max; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
	 * This method checks if a String contains only word caharcters
	 */
    public boolean containsOnlyLetters(final String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        final int max = str.length();
        for (int i = 0; i < max; i++) {
            if (!Character.isLetter(str.charAt(i)) && str.charAt(i) != ' ' && str.charAt(i) != '.' && str.charAt(i) != ',' && str.charAt(i) != '?' && str.charAt(i) != '!' && str.charAt(i) != '(' && str.charAt(i) != ')' && str.charAt(i) != '-' && str.charAt(i) != ':' && str.charAt(i) != '"' && str.charAt(i) != '&' && str.charAt(i) != '/') {
                return false;
            }
        }
        return true;
    }

    /**
	 * Prüft, ob ein Google Captcha vorliegt
	 * @param content
	 * @return boolean check
	 */
    public boolean containsGoogleCaptcha(final String content) {
        boolean check = false;
        if (content != null && (content.contains("=\"captcha\"") || content.contains("=\"Captcha\""))) {
            check = true;
        }
        return check;
    }

    /**
	 * Checks if an ISSN is valid
	 * @param String issn
	 * @return boolean check
	 */
    public boolean isValidIssn(final String issn) {
        boolean check = false;
        String kontrollziffer = "";
        try {
            if (issn.length() == 9 && issn.substring(4, 5).equals("-")) {
                final int pos8 = Integer.valueOf(issn.substring(0, 1));
                final int pos7 = Integer.valueOf(issn.substring(1, 2));
                final int pos6 = Integer.valueOf(issn.substring(2, 3));
                final int pos5 = Integer.valueOf(issn.substring(3, 4));
                final int pos4 = Integer.valueOf(issn.substring(5, 6));
                final int pos3 = Integer.valueOf(issn.substring(6, 7));
                final int pos2 = Integer.valueOf(issn.substring(7, 8));
                final String pos1 = issn.substring(8);
                final int sum = pos8 * 8 + pos7 * 7 + pos6 * 6 + pos5 * 5 + pos4 * 4 + pos3 * 3 + pos2 * 2;
                final int checksum = 11 - (sum - (sum / 11) * 11);
                if (checksum == 10 || checksum == 11) {
                    if (checksum == 10) {
                        kontrollziffer = "X";
                    }
                    if (checksum == 11) {
                        kontrollziffer = "0";
                    }
                } else {
                    kontrollziffer = String.valueOf(checksum);
                }
                if (pos1.equalsIgnoreCase(kontrollziffer)) {
                    check = true;
                } else {
                    LOG.warn("ungültige Prüfziffer: " + issn);
                }
            } else {
                if (issn.length() > 0) {
                    LOG.warn("ungültige Prüfziffer: " + issn);
                }
            }
        } catch (final Exception e) {
            LOG.error("isValidIssn: " + issn + "\040" + e.toString());
        }
        return check;
    }

    /**
	 * Checks if a String is a valid year.
	 * Works from 14th century till 22th century. This sould be usable for some time...
	 * @param String year
	 * @return boolean check
	 */
    public boolean isYear(final String year) {
        boolean check = false;
        if (isExactLength(year, 4) && org.apache.commons.lang.StringUtils.isNumeric(year)) {
            final Pattern pat = Pattern.compile("13[0-9]{2}|14[0-9]{2}|15[0-9]{2}|16[0-9]{2}|17[0-9]{2}|18[0-9]{2}|19[0-9]{2}|20[0-9]{2}|21[0-9]{2}");
            final Matcher match = pat.matcher(year);
            try {
                if (match.find()) {
                    check = true;
                }
            } catch (final Exception e) {
                LOG.error("isYear(String year): " + year + "\040" + e.toString());
            }
        }
        return check;
    }

    /**
	 * Checks in MARC field 008 if the "date entered of file" is valid.
	 * @param String date
	 * @return boolean check
	 */
    public boolean is008DateEnteredOfFile(final String date) {
        boolean check = false;
        if (isExactLength(date, 6) && org.apache.commons.lang.StringUtils.isNumeric(date)) {
            final int month = Integer.valueOf(date.substring(2, 4));
            final int day = Integer.valueOf(date.substring(4, 6));
            if (month <= 12 && day <= 31) {
                check = true;
            }
        }
        return check;
    }

    /**
	 * Checks in MARC field 008 if the "date entered of file" is valid.
	 * @param String type
	 * @return boolean check
	 */
    public boolean is008TypeOfPublication(final String type) {
        boolean check = false;
        if (isExactLength(type, 1) && (org.apache.commons.lang.StringUtils.isAlpha(type) || "|".equals(type))) {
            final char typ = type.charAt(0);
            if ('t' == typ || 'u' == typ || '|' == typ || 'b' == typ || 'c' == typ || 'd' == typ || 'e' == typ || 'i' == typ || 'k' == typ || 'm' == typ || 'n' == typ || 'p' == typ || 'q' == typ || 'r' == typ || 's' == typ) {
                check = true;
            }
        }
        return check;
    }

    /**
	 * Checks in MARC field 008 if the "Date1" or "Date2" is valid.
	 * @param String date
	 * @return boolean check
	 */
    public boolean is008DateOneOrTwo(final String date) {
        boolean check = false;
        if (isExactLength(date, 4)) {
            if (org.apache.commons.lang.StringUtils.isNumeric(date)) {
                if (isYear(date)) {
                    check = true;
                }
            } else if ("    ".equals(date) || "u   ".equals(date) || "||||".equals(date)) {
                check = true;
            }
        }
        return check;
    }

    /**
	 * Checks in MARC field 008 if the "Place of Publication" is valid.
	 * @param String place
	 * @return boolean check
	 */
    public boolean is008PlaceOfPublication(final String place) {
        boolean check = false;
        if (isExactLength(place, 3)) {
            if (org.apache.commons.lang.StringUtils.isAlpha(place) || (org.apache.commons.lang.StringUtils.isAlpha(place.substring(0, 2)) && place.charAt(2) == ' ')) {
                final String compare = place.trim();
                final Set<MarcCountryCodes> places = EnumSet.allOf(MarcCountryCodes.class);
                try {
                    if (places.contains(MarcCountryCodes.valueOf(compare))) {
                        return true;
                    }
                } catch (final Exception e) {
                    System.out.println("Illegal Place of Publication: " + compare);
                }
            } else if ("xx ".equals(place) || "vp ".equals(place)) {
                check = true;
            }
        }
        return check;
    }

    /**
	 * Checks in MARC field 008 if the "Language" is valid.
	 * @param String language
	 * @return boolean check
	 */
    public boolean is008Language(final String lang) {
        boolean check = false;
        if (isExactLength(lang, 3)) {
            if (org.apache.commons.lang.StringUtils.isAlpha(lang)) {
                if ("new".equals(lang)) {
                    return true;
                }
                final Set<MarcLanguageCodes> langCodes = EnumSet.allOf(MarcLanguageCodes.class);
                try {
                    if (langCodes.contains(MarcLanguageCodes.valueOf(lang))) {
                        return true;
                    }
                } catch (final Exception e) {
                    System.out.println("Illegal Language: " + lang);
                }
            } else if ("   ".equals(lang) || "zxx".equals(lang) || "mul".equals(lang) || "sgn".equals(lang) || "und".equals(lang)) {
                check = true;
            }
        }
        return check;
    }

    /**
	 * Checks in MARC field 008 if the "Modified record" is valid.
	 * @param String modified
	 * @return boolean check
	 */
    public boolean is008Modified(final String modified) {
        boolean check = false;
        if (isExactLength(modified, 1)) {
            final char mod = modified.charAt(0);
            if ('d' == mod || ' ' == mod || '|' == mod || 'o' == mod || 'r' == mod || 's' == mod || 'x' == mod) {
                check = true;
            }
        }
        return check;
    }

    /**
	 * Checks in MARC field 008 if the "Cataloging source" is valid.
	 * @param String source
	 * @return boolean check
	 */
    public boolean is008Source(final String source) {
        boolean check = false;
        if (isExactLength(source, 1)) {
            final char mod = source.charAt(0);
            if ('d' == mod || ' ' == mod || '|' == mod || 'c' == mod || 'u' == mod) {
                check = true;
            }
        }
        return check;
    }

    private enum MarcCountryCodes {

        aa, abc, aca, ae, af, ag, ai, aj, aku, alu, am, an, ao, aq, aru, as, at, au, aw, ay, azu, ba, bb, bcc, bd, be, bf, bg, bh, bi, bl, bm, bn, bo, bp, br, bs, bt, bu, bv, bw, bx, cau, cb, cc, cd, ce, cf, cg, ch, ci, cj, ck, cl, cm, cou, cq, cr, ctu, cu, cv, cw, cx, cy, dcu, deu, dk, dm, dq, dr, ea, ec, eg, em, enk, er, es, et, fa, fg, fi, fj, fk, flu, fm, fp, fr, fs, ft, gau, gb, gd, gh, gi, gl, gm, go, gp, gr, gs, gt, gu, gv, gw, gy, gz, hiu, hm, ho, ht, hu, iau, ic, idu, ie, ii, ilu, inu, io, iq, ir, is, it, iv, iy, ja, ji, jm, jo, ke, kg, kn, ko, ksu, ku, kv, kyu, kz, lau, lb, le, lh, li, lo, ls, lu, lv, ly, mau, mbc, mc, mdu, meu, mf, mg, miu, mj, mk, ml, mm, mnu, mo, mou, mp, mq, mr, msu, mtu, mu, mv, mw, mx, my, mz, na, nbu, ncu, ndu, ne, nfc, ng, nhu, nik, nju, nkc, nl, nmu, nn, no, np, nq, nr, nsc, ntc, nu, nuc, nvu, nw, nx, nyu, nz, ohu, oku, onc, oru, ot, pau, pc, pe, pf, pg, ph, pic, pk, pl, pn, po, pp, pr, pw, py, qa, qea, quc, rb, re, rh, riu, rm, ru, rw, sa, scu, sdu, se, sf, sg, sh, si, sj, sl, sm, snc, so, sp, sq, sr, ss, stk, su, sw, sx, sy, sz, ta, tc, tg, th, ti, tk, tl, tma, tnu, to, tr, ts, tu, tv, txu, tz, ua, uc, ug, uik, un, up, utu, uv, uy, uz, vau, vb, vc, ve, vi, vm, vp, vra, vtu, wau, wea, wf, wiu, wj, wk, wlk, ws, wvu, wyu, xa, xb, xc, xd, xe, xf, xga, xh, xj, xk, xl, xm, xn, xna, xo, xoa, xp, xr, xra, xs, xv, xx, xxc, xxk, xxu, ye, ykc, za
    }

    ;

    private enum MarcLanguageCodes {

        aar, abk, ace, ach, ada, ady, afa, afh, afr, ain, aka, akk, alb, ale, alg, alt, amh, ang, anp, apa, ara, arc, arg, arm, arn, arp, art, arw, asm, ast, ath, aus, ava, ave, awa, aym, aze, bad, bai, bak, bal, bam, ban, baq, bas, bat, bej, bel, bem, ben, ber, bho, bih, bik, bin, bis, bla, bnt, bos, bra, bre, btk, bua, bug, bul, bur, byn, cad, cai, car, cat, cau, ceb, cel, cha, chb, che, chg, chi, chk, chm, chn, cho, chp, chr, chu, chv, chy, cmc, cop, cor, cos, cpe, cpf, cpp, cre, crh, crp, csb, cus, cze, dak, dan, dar, day, del, den, dgr, din, div, doi, dra, dsb, dua, dum, dut, dyu, dzo, efi, egy, eka, elx, eng, enm, epo, est, ewe, ewo, fan, fao, fat, fij, fil, fin, fiu, fon, fre, frm, fro, frr, frs, fry, ful, fur, gaa, gay, gba, gem, geo, ger, gez, gil, gla, gle, glg, glv, gmh, goh, gon, gor, got, grb, grc, gre, grn, gsw, guj, gwi, hai, hat, hau, haw, heb, her, hil, him, hin, hit, hmn, hmo, hrv, hsb, hun, hup, iba, ibo, ice, ido, iii, ijo, iku, ile, ilo, ina, inc, ind, ine, inh, ipk, ira, iro, ita, jav, jbo, jpn, jpr, jrb, kaa, kab, kac, kal, kam, kan, kar, kas, kau, kaw, kaz, kbd, kha, khi, khm, kho, kik, kin, kir, kmb, kok, kom, kon, kor, kos, kpe, krc, krl, kro, kru, kua, kum, kur, kut, lad, lah, lam, lao, lat, lav, lez, lim, lin, lit, lol, loz, ltz, lua, lub, lug, lui, lun, luo, lus, mac, mad, mag, mah, mai, mak, mal, man, mao, map, mar, mas, may, mdf, mdr, men, mga, mic, min, mis, mkh, mlg, mlt, mnc, mni, mno, moh, mon, mos, mul, mun, mus, mwl, mwr, myn, myv, nah, nai, nap, nau, nav, nbl, nde, ndo, nds, nep, nia, nic, niu, nno, nob, nog, non, nor, nqo, nso, nub, nwc, nya, nym, nyn, nyo, nzi, oci, oji, ori, orm, osa, oss, ota, oto, paa, pag, pal, pam, pan, pap, pau, peo, per, phi, phn, pli, pol, pon, por, pra, pro, pus, que, raj, rap, rar, roa, roh, rom, rum, run, rup, rus, sad, sag, sah, sai, sal, sam, san, sas, sat, scn, sco, sel, sem, sga, sgn, shn, sid, sin, sio, sit, sla, slo, slv, sma, sme, smi, smj, smn, smo, sms, sna, snd, snk, sog, som, son, sot, spa, srd, srn, srp, srr, ssa, ssw, suk, sun, sus, sux, swa, swe, syc, syr, tah, tai, tam, tat, tel, tem, ter, tet, tgk, tgl, tha, tib, tig, tir, tiv, tkl, tlh, tli, tmh, tog, ton, tpi, tsi, tsn, tso, tuk, tum, tup, tur, tut, tvl, twi, tyv, udm, uga, uig, ukr, umb, und, urd, uzb, vai, ven, vie, vol, vot, wak, wal, war, was, wel, wen, wln, wol, xal, xho, yao, yap, yid, yor, ypk, zap, zbl, zen, zha, znd, zul, zun, zxx, zza
    }

    ;
}
