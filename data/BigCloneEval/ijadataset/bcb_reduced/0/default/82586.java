import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;
import java.io.*;

public final class Cakil extends MIDlet implements CommandListener {

    private Display display;

    private Form form;

    private TextField text;

    private TextField arti1;

    private TextField arti2;

    private TextField pesan;

    private final Command exit = new Command("Exit", Command.EXIT, 2);

    private final Command submit = new Command("Submit", Command.OK, 2);

    private Image splash;

    private Alert alert;

    private byte[] array1 = new byte[10000];

    private byte[] temp = new byte[1000];

    private int begin;

    private int length;

    private String firstlastei[] = new String[64];

    private String firstlastie[] = new String[64];

    public Cakil() {
        display = Display.getDisplay(this);
        form = new Form("Cakil");
        text = new TextField("kata", "", 50, TextField.ANY);
        arti1 = new TextField("English-Indonesian", "", 500, TextField.UNEDITABLE);
        arti2 = new TextField("Indonesian-English", "", 500, TextField.UNEDITABLE);
        pesan = new TextField("Maaf", "kata tidak ditemukan", 500, TextField.UNEDITABLE);
        try {
            splash = Image.createImage("/icon/tcotsplash.png");
        } catch (IOException io) {
            System.out.print("ada yang gak beres " + io);
        }
        alert = new Alert("Cakil", "abdul.arfan@gmail.com", splash, AlertType.INFO);
        form.append(text);
        form.addCommand(exit);
        form.addCommand(submit);
        form.setCommandListener(this);
        InputStream is;
        is = this.getClass().getResourceAsStream("firstlastei.txt");
        int n = 0;
        try {
            n = is.read(array1);
        } catch (IOException io) {
            System.out.print("ada yang gak beres " + io);
        }
        int index = 0;
        int awal = 0;
        for (int x = 1; x <= n - 1; x++) {
            if (array1[x] == 9) {
                firstlastei[index++] = new String(array1, awal, x - awal);
                awal = x + 1;
                x = awal + 1;
            }
        }
        try {
            is.close();
        } catch (IOException io) {
            System.out.print("ada yang gak beres " + io);
        }
        is = getClass().getResourceAsStream("firstlastie.txt");
        n = 0;
        try {
            n = is.read(array1);
        } catch (IOException io) {
            System.out.print("ada yang gak beres " + io);
        }
        index = 0;
        awal = 0;
        for (int x = 1; x <= n - 1; x++) {
            if (array1[x] == 9) {
                firstlastie[index++] = new String(array1, awal, x - awal);
                awal = x + 1;
                x = awal + 1;
            }
        }
        try {
            is.close();
        } catch (IOException io) {
            System.out.print("ada yang gak beres " + io);
        }
    }

    public void startApp() {
        display.setCurrent(alert, form);
    }

    public void pauseApp() {
        display.setCurrent(null);
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable d) {
        if (c == exit) {
            destroyApp(false);
            notifyDestroyed();
            return;
        } else if (c == submit) {
            int n = form.size();
            boolean masuk = false;
            for (int x = n - 1; x >= 1; x--) {
                form.delete(x);
            }
            String jawaban;
            jawaban = findWord(text.getString().toLowerCase(), true);
            if (jawaban != null) {
                masuk = true;
                arti1.setString(jawaban);
                form.append(arti1);
            }
            jawaban = findWord(text.getString().toLowerCase(), false);
            if (jawaban != null) {
                masuk = true;
                arti2.setString(jawaban);
                form.append(arti2);
            }
            if (!masuk) {
                form.append(pesan);
            }
        }
    }

    private String findWord(String kata, boolean ei) {
        int index;
        if (ei) {
            index = cariIndexFirstLast(firstlastei, kata);
            if (index >= 0) {
                return findWord(kata, "kamusei" + index + ".txt");
            }
        } else {
            index = cariIndexFirstLast(firstlastie, kata);
            if (index >= 0) {
                return findWord(kata, "kamusie" + index + ".txt");
            }
        }
        return null;
    }

    private String findWord(String kata, String file) {
        InputStream is = getClass().getResourceAsStream(file);
        try {
            int index = 0;
            int first;
            int last;
            int lokasi;
            while (true) {
                int n = is.read(array1);
                first = 0;
                while (first < n && array1[first] != 10) {
                    first++;
                }
                first++;
                if (first - 3 < n) for (int x = 0; x <= first - 3; x++) {
                    temp[index++] = array1[x];
                }
                if (compare(temp, 0, index - 1, kata) == 0) {
                    return new String(temp, kata.length() + 1, index - kata.length() - 1);
                }
                index = 0;
                last = n - 1;
                while (last >= 0 && array1[last] != 13) {
                    last--;
                }
                last--;
                if (last >= 0) for (int x = last + 3; x <= n - 1; x++) {
                    temp[index++] = array1[x];
                }
                int hasil = searchLastWord(last, kata);
                if (hasil == 0) {
                    return new String(array1, begin + kata.length() + 1, length - kata.length());
                } else if (hasil > 0) {
                    lokasi = binarySearchWord(first, last, kata);
                    if (lokasi >= 0) {
                        return new String(array1, lokasi + kata.length() + 1, length - kata.length() - 1);
                    }
                    if (lokasi < 0) {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ada yang salah " + e);
            return null;
        }
    }

    private int binarySearchWord(int low, int high, String word) {
        boolean naik = false;
        boolean turun = false;
        int mid;
        int start, end;
        while (low <= high) {
            mid = (low + high) / 2;
            start = cariAwal(mid);
            end = cariAkhir(mid);
            if (start < low || end > high) return -1;
            int hasil = compare(array1, start, end, word);
            if (hasil == 0) {
                length = end - start + 1;
                return start;
            } else if (hasil < 0) {
                low = end + 3;
                naik = true;
            } else {
                high = start - 3;
                turun = true;
            }
        }
        if (naik && turun) {
            return -2;
        } else return -1;
    }

    private int searchLastWord(int l, String word) {
        length = 0;
        while (l >= 0 && array1[l] != 10) {
            l--;
            length++;
        }
        l++;
        begin = l;
        for (int x = begin; x < begin + length; x++) {
        }
        return compare(array1, l, l + length - 1, word);
    }

    private int cariAwal(int awal) {
        while (array1[awal] != 10) {
            awal--;
            if (awal < 0) return 0;
        }
        return awal + 1;
    }

    private int cariAkhir(int akhir) {
        while (array1[akhir] != 13) {
            akhir++;
            if (akhir >= array1.length) return array1.length - 1;
        }
        return akhir - 1;
    }

    private int compare(byte[] a, int awal, int akhir, String kata) {
        int end = awal + kata.length() - 1;
        for (int x = awal; x <= end; x++) {
            if (a[x] != kata.charAt(x - awal)) return a[x] - kata.charAt(x - awal);
        }
        if (a[end + 1] == 9) return 0;
        return (akhir - awal + 1 - kata.length());
    }

    private int cariFirstLast(String a[], String kata) {
        int low = 0;
        int high = 64 - 1;
        int mid = 0;
        while (low <= high) {
            mid = (high + low) / 2;
            if (a[mid].compareTo(kata) < 0) {
                low = mid + 1;
            } else if (a[mid].compareTo(kata) > 0) {
                high = mid - 1;
            } else return mid;
        }
        return -(low + 1);
    }

    private int cariIndexFirstLast(String a[], String kata) {
        int index = cariFirstLast(a, kata);
        if (index < 0) {
            index = -index;
            if (index % 2 == 0) return (index - 2) / 2; else return -1;
        }
        return index / 2;
    }
}
