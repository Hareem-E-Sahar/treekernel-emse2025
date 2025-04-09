package palindrume;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import palindrume.bd.BDBase;
import palindrume.bd.DateFormateur;
import palindrume.bd.Tracer;
import palindrume.model.PalindrumeFinder;
import palindrume.view.PalindromView;
import palindrume.wiki.WikiDumpFileParser;

/**
 * Point d'entrée de l'application
 * 
 */
public class PalindromLauncher {

    public static void main(String[] args) {
        launch_gui();
    }

    private static void test_regex() {
        String input = "{{pron-rég|France <!-- précisez svp la ville ou la région -->|bɔ̃.ʒuʁ|audio=Bonjour.ogg}}";
        Pattern p = Pattern.compile("(\\{\\{pron-rég|)([^|]*|)");
        Matcher m = p.matcher(input);
        while (m.find()) {
            System.out.println("Le texte \"" + m.group() + "\" débute à " + m.start() + " et termine à " + m.end());
        }
    }

    private static void create_db() {
        BDBase db = new BDBase();
        if (!db.connect("com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/PALINDRUME", "root", "fil")) {
            Tracer.t("connection impossible");
            return;
        }
        Tracer.t("start : " + DateFormateur.getTimeHHMMSS());
        Tracer.activate(false);
        WikiDumpFileParser.create_db(db, "frwiktionary-20090120-pages-articles.xml");
        Tracer.activate(true);
        Tracer.t("end : " + DateFormateur.getTimeHHMMSS());
    }

    /**
	 * Création de l'implémentation et de la vue
	 * 
	 */
    private static void launch_gui() {
        BDBase db = new BDBase();
        if (!db.connect("com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/PALINDRUME", "root", "fil")) {
            Tracer.t("connection impossible");
            return;
        }
        PalindrumeFinder ph = new PalindrumeFinder(db);
        PalindromView v = new PalindromView(ph);
        v.show();
    }
}
