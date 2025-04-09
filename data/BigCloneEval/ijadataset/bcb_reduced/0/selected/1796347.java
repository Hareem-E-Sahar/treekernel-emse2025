package affarituoioop;

import func.func;

public class Lista {

    private Pacco[] lista = new Pacco[20];

    private String[] premi = { "0.01 €", "0.02 €", "1 €", "2 €", "5 €", "20 €", "50 €", "100 €", "Una camicia", "Un KG di cemento", "1.000 €", "5.000 €", "10.000 €", "15.000 €", "20.000 €", "50.000 €", "75.000 €", "100.000 €", "250.000 €", "500.000 €" };

    private String[] regioni = { "Valle d'Aosta", "Piemonte", "Lombardia", "Trentino", "Veneto", "Friuli", "Liguria", "Toscana", "Emilia romagna", "Marche", "Lazio", "Abruzzo", "Molise", "Umbria", "Campania", "Puglia", "Calabria", "Basilicata", "Sicilia", "Sardegna" };

    private int[] numeri = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };

    private double[] premidottore = { 0.01, 0.02, 1, 2, 5, 20, 50, 100, 200, 500, 1000, 5000, 10000, 15000, 20000, 50000, 75000, 100000, 250000, 500000 };

    /**
     * Genera la lista di tutti i premi assegnati alle regioni.
     *
     * @return Un array di tipo Pacco contenente la lista di tutti i pacchi.
     */
    public Pacco[] assegnaPremi() {
        for (int i = 0; i < 20; i++) {
            int rand = func.rand(19);
            while (!func.isNumInArray(rand, this.numeri)) {
                rand = func.rand(19);
            }
            this.lista[i] = new Pacco();
            this.lista[i].setPacco(i + 1);
            this.lista[i].setRegione(this.regioni[i]);
            this.lista[i].setPremio(this.premi[rand]);
            for (int id = 0; id < this.numeri.length; id++) {
                if (this.numeri[id] == rand + 1) {
                    this.numeri[id] = 0;
                }
            }
        }
        return lista;
    }

    /**
     * Genera la lista dei premi rimasti.
     * @return Una stringa con tutti i premi rimasti.
     */
    public String getPremi() {
        String elpremi = "";
        for (int i = 0; i < this.lista.length; i++) {
            String color = "";
            if (i >= 0 && i <= 9) {
                color = "blue";
            } else if (i > 9 && i <= 14) {
                color = "yellow";
            } else {
                color = "red";
            }
            elpremi = elpremi + "<html><body color=" + color + ">" + this.premi[i] + "</font></html>\n";
        }
        return elpremi;
    }

    /**
     * Setta l'elemento pNum dell'array di pacchi con il pacco pPacco.
     * @param pNum L'indice dell'array da andare a settare.
     * @param pPacco Il pacco da inserire nella posizione pNum dell'array.
     */
    public void setListaElement(int pNum, Pacco pPacco) {
        if (pNum >= 0 && pNum <= 20) {
            lista[pNum] = pPacco;
        }
    }

    /**
     * Genera la lista di tutte le regioni ancora da scegliere con il numero del pacco accanto.
     * @return Una stringa contenente la lista.
     */
    public String getLista() {
        String list = "";
        for (int i = 0; i < lista.length; i++) {
            if (lista[i].getVisibile()) {
                list = list + lista[i].getPacco() + " " + lista[i].getRegione() + "\n";
            }
        }
        return list;
    }

    /**
     * Genera la lista contenente tutti i numeri dei pacchi ancora chiusi.
     * @return Un array di stringhe ognuna delle quali rappresenta il numero del pacco.
     */
    public String[] getPacchi() {
        int contatore = 0;
        for (int i = 0; i < lista.length; i++) {
            if (lista[i].getVisibile()) {
                contatore++;
            }
        }
        String[] pacchi = new String[contatore];
        int num = 0;
        for (int i = 0; i < lista.length; i++) {
            if (lista[i].getVisibile()) {
                pacchi[num] = lista[i].getPacco() + "";
                num++;
            }
        }
        return pacchi;
    }

    /**
     * Il pacco che si trova in posizione pNum-1.
     * @param pNum Il numero del pacco.
     * @return Il pacco in posizione pNum-1.
     */
    public Pacco getPacco(int pNum) {
        return lista[pNum - 1];
    }

    public boolean setPacco(int pNum, String pRegione, String pPremio, Pacco pPacco) {
        if (pPacco.setPacco(pNum) && pPacco.setRegione(pRegione) && pPacco.setPremio(pPremio)) {
            return true;
        }
        return false;
    }

    public void delPacco(Pacco pPacco, int pConferma) {
        for (int i = 0; i < lista.length; i++) {
            if (lista[i].equals(pPacco)) {
                lista[i].setVisibile(false);
            }
        }
        if (pConferma != 0) {
            for (int id = 0; id < this.premi.length; id++) {
                String premio = pPacco.getPremio();
                if (premio.equals(this.premi[id])) {
                    this.premi[id] = "";
                    this.premidottore[id] = 0;
                }
            }
        }
    }

    public String offerta(Pacco pPacco) {
        double somma = 0;
        int contatore = 0;
        for (int i = 0; i < this.premidottore.length; i++) {
            somma = somma + this.premidottore[i];
            if (this.premidottore[i] != 0) {
                contatore++;
            }
        }
        double[] prize = new double[contatore];
        int num = 0;
        for (int i = 0; i < this.premidottore.length; i++) {
            if (this.premidottore[i] != 0) {
                prize[num] = this.premidottore[i];
                num++;
            }
        }
        int media = (int) somma / contatore;
        int mediana = (int) prize[contatore / 2];
        int offerta = (media + mediana) / 2;
        if (pPacco.getPremio().equals("50.000 €") || pPacco.getPremio().equals("75.000 €") || pPacco.getPremio().equals("100.000 €") || pPacco.getPremio().equals("250.000 €") || pPacco.getPremio().equals("500.000 €")) {
            offerta = offerta + 2000;
        }
        String sOfferta = offerta + "";
        String offertafinale = "";
        for (int i = 0; i < sOfferta.length(); i++) {
            if (sOfferta.length() == 4) {
                if (i == 0 || i == 1) {
                    offertafinale = offertafinale + sOfferta.charAt(i);
                    if (i == 0) {
                        offertafinale = offertafinale + ".";
                    }
                } else {
                    offertafinale = offertafinale + "0";
                }
            }
            if (sOfferta.length() == 5) {
                if (i == 0 || i == 1) {
                    offertafinale = offertafinale + sOfferta.charAt(i);
                    if (i == 1) {
                        offertafinale = offertafinale + ".";
                    }
                } else {
                    offertafinale = offertafinale + "0";
                }
            }
            if (sOfferta.length() == 6) {
                if (i == 0 || i == 1) {
                    offertafinale = offertafinale + sOfferta.charAt(i);
                } else if (i == 2) {
                    offertafinale = offertafinale + ".";
                } else {
                    offertafinale = offertafinale + "0";
                }
            }
        }
        return offertafinale;
    }

    public int offertaInt(Pacco pPacco) {
        double somma = 0;
        int contatore = 0;
        for (int i = 0; i < this.premidottore.length; i++) {
            somma = somma + this.premidottore[i];
            if (this.premidottore[i] != 0) {
                contatore++;
            }
        }
        double[] prize = new double[contatore];
        int num = 0;
        for (int i = 0; i < this.premidottore.length; i++) {
            if (this.premidottore[i] != 0) {
                prize[num] = this.premidottore[i];
                num++;
            }
        }
        int media = (int) somma / contatore;
        int mediana = (int) prize[contatore / 2];
        int offerta = (media + mediana) / 2;
        if (pPacco.getPremio().equals("50.000 €") || pPacco.getPremio().equals("75.000 €") || pPacco.getPremio().equals("100.000 €") || pPacco.getPremio().equals("250.000 €") || pPacco.getPremio().equals("500.000 €")) {
            offerta = offerta + 2000;
        }
        return offerta;
    }
}
