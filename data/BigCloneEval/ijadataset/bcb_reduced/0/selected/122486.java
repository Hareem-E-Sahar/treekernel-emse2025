package galaxiia.ui;

import java.awt.*;
import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import org.jul.i18n.I18n;
import org.jul.ui.DisplayModeWrapper;
import galaxiia.configuration.*;
import galaxiia.exception.ExceptionInitialisation;
import galaxiia.jeu.terrain.Terrain;
import galaxiia.noyau.*;
import galaxiia.noyau.enregistreur.InformationPartie;
import galaxiia.ui.dialogues.DialogueTexte;

public class Galaxiia {

    public static class GestionExceptions implements UncaughtExceptionHandler {

        public void uncaughtException(Thread t, Throwable e) {
            UI.erreurFatale("Erreur inattendue dans le thread " + t.getName() + " !", e);
        }
    }

    public static final String NOM = "Galaxiia 1.0";

    public static final String EXTENSION_SAUVEGARDES = "gxi";

    public static final String EXTENSION_CONFIGURATION = "xml";

    public static final String REPERTOIRE_SAUVEGARDES = System.getProperty("user.home");

    public static final int TAILLE_BORDURE = 20;

    public static final int TAILLE_BORDURE_HAUTEUR = 10;

    public static final int TAILLE_ESPACE_GRILLE = 3;

    public static final Dimension DIMENSIONS_DIALOGUE_TEXTE = new Dimension(600, 300);

    public static final Dimension DIMENSIONS_DIALOGUE_ERREUR = new Dimension(500, 350);

    private static final String FORMAT_TOURS = "{0,choice,0#en début de partie|1#dès le premier tour|1<au bout de {0,number,integer} tours}";

    public static final String FORMAT_VECTEUR = "'{'x = {0,number,#.##} ; y = {1,number,#.##}'}'";

    private static ConfigurationModules configurationModules;

    private static ConfigurationGenerale configurationGenerale;

    public static final ClassLoader loaderNoyau = Noyau.class.getClassLoader();

    public static boolean securiteChargement = true;

    public static boolean securiteExecution = true;

    public static boolean effectueSauvegardes = true;

    public static boolean texturesHauteDefinition = false;

    public static boolean pleinEcran = false;

    public static DisplayMode modeAffichage = null;

    public static boolean previsualisationCarte = true;

    private static I18n i18n;

    private static String fichierConfigurationGenerale = System.getProperty("user.home") + File.separator + ".galaxiia" + File.separator + "galaxiia.xml";

    public static final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

    public static String formateVictoire(InformationPartie informationPartie) {
        switch(informationPartie.getStatutPartie()) {
            case EN_COURS:
                return i18n.format("interrupted-by-user", informationPartie.getNombreToursPartie());
            case ERREUR:
                return "Partie interrompue à cause d'une erreur noyau " + MessageFormat.format(FORMAT_TOURS, informationPartie.getNombreToursPartie()) + ".";
            case VICTOIRE:
                if (informationPartie.getEquipeGagnante() == Terrain.PARTIE_NULLE) {
                    return i18n.format("draw-game", informationPartie.getNombreToursPartie());
                } else {
                    return i18n.format("victory", informationPartie.getNombreToursPartie(), informationPartie.getEquipeGagnante());
                }
            default:
                throw new IllegalStateException();
        }
    }

    public static StackTraceElement trouveDepartException(StackTraceElement[] pileExecution, String classe) {
        for (StackTraceElement element : pileExecution) {
            if (element.getClassName().equals(classe)) {
                return element;
            }
        }
        return pileExecution[0];
    }

    private static String genereAide(Class<?> aideClasse) throws IOException {
        InputStream stream = Galaxiia.class.getResourceAsStream('/' + aideClasse.getCanonicalName().replace('.', '/') + ".html");
        if (stream != null) {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } finally {
                reader.close();
            }
            return builder.toString();
        } else {
            return null;
        }
    }

    public static void afficheAideInterface(Frame frame, Class<?> aideClasse) {
        try {
            String aide = genereAide(aideClasse);
            if (aide != null) {
                new DialogueTexte(frame, "Aide", aide, false);
            } else {
                UI.erreur(frame, "Aucune aide n'est disponible.");
            }
        } catch (IOException e) {
            UI.erreur(frame, "Impossible de charger l'aide.", e);
        }
    }

    public static void afficheAideInterface(Dialog dialog, Class<?> aideClasse) {
        try {
            String aide = genereAide(aideClasse);
            if (aide != null) {
                new DialogueTexte(dialog, "Aide", aide, false);
            } else {
                UI.erreur(dialog, "Aucune aide n'est disponible.");
            }
        } catch (IOException e) {
            UI.erreur(dialog, "Impossible de charger l'aide.", e);
        }
    }

    public static String getExtension(File fichier) {
        String nom = fichier.getName();
        int positionSeparateur = nom.lastIndexOf('.');
        if (positionSeparateur >= 0) {
            return nom.substring(positionSeparateur + 1, nom.length());
        } else {
            return "";
        }
    }

    private static String[] recolleArgumentsEspaces(String[] args) {
        List<String> arguments = new ArrayList<String>(args.length);
        StringBuilder argumentLong = null;
        for (String arg : args) {
            if (arg.indexOf('"') == 0) {
                argumentLong = new StringBuilder(arg.substring(1));
            } else if (argumentLong != null) {
                if (arg.endsWith("\"")) {
                    argumentLong.append(arg.substring(0, arg.length() - 1));
                    arguments.add(argumentLong.toString());
                    argumentLong = null;
                } else {
                    argumentLong.append(arg);
                }
            } else {
                arguments.add(arg);
            }
        }
        if (argumentLong != null) {
            arguments.add(argumentLong.toString());
        }
        return arguments.toArray(new String[0]);
    }

    private static int analyseArgument(String[] args, int i) {
        if (args[i].equals("--no-save")) {
            effectueSauvegardes = false;
            return i + 1;
        } else if (args[i].equals("--interface") || args[i].equals("-i")) {
            i++;
            if (i < args.length) {
                configurationGenerale.setInterfaceJoueur(args[i]);
                return i + 1;
            } else {
                System.err.println("Argument attendu pour l'option " + args[i - 1] + ".");
                return -1;
            }
        } else if (args[i].equals("--config-file") || args[i].equals("-c")) {
            i++;
            if (i < args.length) {
                fichierConfigurationGenerale = args[i];
                return i + 1;
            } else {
                System.err.println("Argument attendu pour l'option " + args[i - 1] + ".");
                return -1;
            }
        } else if (args[i].equals("--language") || args[i].equals("-l")) {
            i++;
            if (i < args.length) {
                String[] locale = args[i].split("_");
                if (locale.length == 2) {
                    Locale.setDefault(new Locale(locale[0], locale[1]));
                } else {
                    Locale.setDefault(new Locale(locale[0]));
                }
                return i + 1;
            } else {
                System.err.println("Argument attendu pour l'option " + args[i - 1] + ".");
                return -1;
            }
        } else if (args[i].equals("--game-file") || args[i].equals("-g")) {
            i++;
            if (i < args.length) {
                configurationGenerale.setFichierPartie(new File(args[i]));
                return i + 1;
            } else {
                System.err.println("Argument attendu pour l'option " + args[i - 1] + ".");
                return -1;
            }
        } else if (args[i].equals("--display-mode") || args[i].equals("-d")) {
            i++;
            if (i < args.length) {
                try {
                    modeAffichage = DisplayModeWrapper.parseFromInternalName(args[i]).getDisplayMode();
                } catch (ParseException e) {
                    System.err.println("Argument invalide pour l'option " + args[i - 1] + " : " + e);
                } catch (IllegalArgumentException e) {
                    System.err.println("Mode invalide : " + args[i]);
                }
                return i + 1;
            } else {
                System.err.println("Argument attendu pour l'option " + args[i - 1] + ".");
                return -1;
            }
        } else if (args[i].equals("--fullscreen") || args[i].equals("-f")) {
            pleinEcran = true;
            return i + 1;
        } else if (args[i].equals("--disable-map-previsualization")) {
            previsualisationCarte = false;
            return i + 1;
        } else if (args[i].equals("--no-loading-check")) {
            System.err.println("ATTENTION : Sécurité durant le chargement désactivée !");
            securiteChargement = false;
            return i + 1;
        } else if (args[i].equals("--high-definition")) {
            texturesHauteDefinition = true;
            return i + 1;
        } else if (args[i].equals("--standard-definition")) {
            texturesHauteDefinition = false;
            return i + 1;
        } else if (args[i].equals("--no-execution-check")) {
            System.err.println("ATTENTION : Sécurité durant l'exécution désactivée !");
            securiteExecution = false;
            return i + 1;
        }
        return i + 1;
    }

    public static void main(String[] args) {
        try {
            configurationModules = new ConfigurationModules();
        } catch (ExceptionInitialisation e) {
            UI.erreurFatale("Erreur lors du chargement des modules !", e);
        }
        configurationGenerale = new ConfigurationGenerale(configurationModules);
        try {
            configurationGenerale.chargeConfiguration(configurationModules, fichierConfigurationGenerale);
        } catch (FileNotFoundException e) {
            System.err.println("[INIT] Attention : Fichier de configuration générale non trouvé à l'emplacement \"" + fichierConfigurationGenerale + "\" !");
        } catch (InvalidPropertiesFormatException e) {
            UI.erreur("Le fichier de configuration général est mal formé !", e);
        } catch (IOException e) {
            UI.erreur("Erreur lors du chargement du fichier de configuration général.", e);
        }
        args = recolleArgumentsEspaces(args);
        int index = 0;
        while (index != -1 && index < args.length) {
            index = analyseArgument(args, index);
        }
        i18n = UI.getI18nInstance(Galaxiia.class);
        Thread.setDefaultUncaughtExceptionHandler(new GestionExceptions());
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable t) {
            UI.erreur("Impossible d'utiliser le thème système pour l'interface.", t);
        }
        UI.init();
        try {
            configurationGenerale.sauvegardeConfiguration();
        } catch (IOException e) {
            UI.erreur("Erreur lors de la sauvegarde du fichier de configuration générale.", e);
        }
        try {
            configurationGenerale.getInterfaceJoueur().getConstructor(new Class[] { args.getClass(), ConfigurationGenerale.class }).newInstance(new Object[] { args, configurationGenerale });
        } catch (Throwable t) {
            UI.erreurFatale("Impossible de démarrer l'interface !", t);
        }
    }
}
