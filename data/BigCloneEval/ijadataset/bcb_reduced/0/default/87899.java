import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.GregorianCalendar;

public class Kiralama {

    private int ID;

    private Musteri musteri;

    private Film film;

    private GregorianCalendar baslangicTarihi;

    double fiyat = 0;

    Loading loader;

    public Kiralama(Musteri musteri, Film film, int ID) {
        this.musteri = musteri;
        this.film = film;
        this.ID = ID;
        this.baslangicTarihi = new GregorianCalendar();
        film.setKiralanabilirlik(false);
        musteri.setEldekiFilm(musteri.getEldekiFilm() + 1);
    }

    public Kiralama(Musteri musteri, Film film, int ID, GregorianCalendar baslangicTarihi) {
        this.musteri = musteri;
        this.film = film;
        this.ID = ID;
        this.baslangicTarihi = baslangicTarihi;
        film.setKiralanabilirlik(false);
        musteri.setEldekiFilm(musteri.getEldekiFilm() + 1);
    }

    public Kiralama(Musteri musteri, Film film, int ID, GregorianCalendar baslangicTarihi, Loading loader) {
        this.musteri = musteri;
        this.film = film;
        this.ID = ID;
        this.baslangicTarihi = baslangicTarihi;
        film.setKiralanabilirlik(false);
        musteri.setEldekiFilm(musteri.getEldekiFilm() + 1);
        this.loader = loader;
    }

    public int getID() {
        return ID;
    }

    public Musteri getMusteri() {
        return musteri;
    }

    public Film getFilm() {
        return film;
    }

    public double getUcret() {
        Date currentDate = new Date();
        long kiraGunu = currentDate.getTime() - this.baslangicTarihi.getTimeInMillis();
        kiraGunu = kiraGunu / (1000 * 60 * 60 * 24);
        if (kiraGunu >= 2) return 2 + (kiraGunu - 2) / 2; else return 2;
    }

    public double getUcret(String paraBirimi, ExchangeRateProvider provider) {
        Date currentDate = new Date();
        long kiraGunu = currentDate.getTime() - this.baslangicTarihi.getTimeInMillis();
        kiraGunu = kiraGunu / (1000 * 60 * 60 * 24);
        if (kiraGunu >= 2) {
            fiyat = 2 + (kiraGunu - 2) / 2;
        } else {
            fiyat = 2;
        }
        return (double) (fiyat * provider.getRateFromTo("TR", "EUR"));
    }

    public void dosyayaKaydet() {
        String dosyaKaydi = this.ID + "," + this.getFilm().getAd() + "," + this.musteri.getID() + "," + this.baslangicTarihi;
        loader.load(dosyaKaydi);
    }
}
