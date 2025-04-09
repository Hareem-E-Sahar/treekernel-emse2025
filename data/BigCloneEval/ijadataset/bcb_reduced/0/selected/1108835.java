package src;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import src.argumentHandler.ArgumentReader;
import src.resources.RegExpressions;
import src.utilities.IORead;
import src.utilities.Sort;
import src.utilities.StringTools;
import src.utilities.Stopwatch;
import src.utilities.StopwatchNano;

/**
 *
 * Only a few tests ...
 *
 * @author Simon Eugster
 */
@SuppressWarnings("unused")
public class RegexpBug {

    static PrintStream o = System.out;

    static StopwatchNano sn = new StopwatchNano();

    public static void main(String[] args) throws IOException {
        String[] arg = { "--helpfiles", "--silent" };
        String[] argDe = { "--helpfiles-de", "--silent" };
        String[] arg2 = { "--helpfiles", "-s", "hd" };
        String[] argTest = { "--helpfiles", "-s", "glassborder" };
        String[] argsFiles = { "bla test.args ble", "test.args", "--test.args", "test.args2" };
        String[] argsSizes = { "200px", "x300px", "200x300px", "px" };
        String rIf = "{{#if:a|b|c}}";
        for (String s : "a\tb".split("\\t")) {
            System.out.println("Element: " + s);
        }
    }

    private static final String noNewlines(String s) {
        return s.replace("\n", "\\n").replace("\r", "\\r").replace(" ", "␣");
    }

    /**
	 * String: about 4 times faster
	 * @param n
	 */
    private static void benchRegexVsString(int n) {
        StopwatchNano sn = new StopwatchNano();
        String test = testText.toString();
        sn.start();
        Pattern p = Pattern.compile("H(?:unger)?\\. Stufe für Stufe");
        for (int i = 0; i < n; i++) {
            Matcher m = p.matcher(test);
            if (m.find()) {
                ;
            }
        }
        sn.stop();
        System.err.println("Regex: " + sn.getStoppedTimeString());
        sn.start();
        for (int i = 0; i < n; i++) {
            if (test.startsWith("Hunger. Stufe für Stufe")) {
                ;
            }
            if (test.startsWith("H. Stufe für Stufe")) {
                ;
            }
        }
        sn.stop();
        System.err.println("String: " + sn.getStoppedTimeString());
        sn.start();
        p = Pattern.compile("H(?:unger)?\\. Stufe für Stufe");
        for (int i = 0; i < n; i++) {
            Matcher m = p.matcher(test);
            if (m.find()) {
                String s = m.group();
            }
        }
        sn.stop();
        System.err.println("Regex: " + sn.getStoppedTimeString());
        sn.start();
        for (int i = 0; i < n; i++) {
            if (test.startsWith("Hunger. Stufe für Stufe")) {
                String s = test.substring(0, "Hunger. Stufe für Stufe".length());
            }
            if (test.startsWith("H. Stufe für Stufe")) {
                String s = test.substring(0, "H. Stufe für Stufe".length());
            }
        }
        sn.stop();
        System.err.println("String: " + sn.getStoppedTimeString());
        sn.start();
        for (int i = 0; i < n; i++) {
            if (test.startsWith("Hunger. Stufe für Stufe")) {
                String s = test.substring(0, 24);
            }
            if (test.startsWith("H. Stufe für Stufe")) {
                String s = test.substring(0, 19);
            }
        }
        sn.stop();
        System.err.println("String: " + sn.getStoppedTimeString());
    }

    /**
	 * Deleting: about 8 times faster
	 * @param n
	 */
    private static void benchSbDeleteVsNew(int n) {
        StopwatchNano sn = new StopwatchNano();
        for (int i = 0; i < n; i++) {
            StringBuffer sb = new StringBuffer(testText);
            sn.continueTime();
            sb.delete(100, 200);
            sn.stop();
        }
        System.out.println("Deleting: " + sn.getStoppedTimeString());
        sn.reset();
        for (int i = 0; i < n; i++) {
            StringBuffer sb = new StringBuffer(testText);
            sn.continueTime();
            StringBuffer a = new StringBuffer();
            a.append(sb.substring(0, 100));
            a.append(sb.substring(200, sb.length()));
            sb = a;
            sn.stop();
        }
        sn.stop();
        System.out.println("Creating new SB: " + sn.getStoppedTimeString());
    }

    private static void b(StringBuffer sb) {
        sb.delete(0, 1);
        bla(sb);
    }

    private static void bla(StringBuffer... sb) {
        for (StringBuffer s : sb) s.delete(1, 2);
    }

    public static class specialObject {

        public String s;

        public int i;

        public specialObject(int i, String s) {
            this.s = s;
            this.i = i;
        }
    }

    /**
	 * a=b is about 250 times faster than a=new StringBuffer(b)
	 * @param n How many times to run the test; will be doubled
	 */
    private static void benchNewVsSize(int n) throws IOException {
        StopwatchNano sn = new StopwatchNano();
        StopwatchNano sn2 = new StopwatchNano();
        StringBuffer in = IORead.readSBuffer(new java.io.File("doc.txt"));
        StringBuffer out = new StringBuffer();
        sn.start();
        for (int i = 0; i < n; i++) {
            out = in;
            sn2.continueTime();
            in = new StringBuffer();
            sn2.stop();
            in = out;
            sn2.continueTime();
            out = new StringBuffer();
            sn2.stop();
        }
        sn.stop();
        sn2.stop();
        System.out.println("New StringBuffer: " + sn.getStoppedTimeString() + " / " + sn2.getStoppedTimeString());
        sn.reset();
        sn.start();
        sn2.reset();
        for (int i = 0; i < n; i++) {
            out = new StringBuffer(in);
            sn2.continueTime();
            in.setLength(0);
            sn2.stop();
            in = new StringBuffer(out);
            sn2.continueTime();
            out.setLength(0);
            sn2.stop();
        }
        sn.stop();
        System.out.println("Size to 0: " + sn.getStoppedTimeString() + " / " + sn2.getStoppedTimeString());
    }

    private static void testRegexMultipleMatch(Pattern pattern, String test) {
        o.print("\n> Starting test for Regex (multiple matches)\n" + "    in " + test + "\n" + "       ");
        for (int i = 0; i < test.length() + 1; i++) {
            o.print(i % 10);
        }
        System.out.println();
        Matcher m = pattern.matcher(test);
        int i = 0;
        while (m.find()) {
            o.println("Match " + i + ": " + m.group() + " (" + m.start() + " to " + m.end() + ")");
            i++;
        }
    }

    /**
	 * Tests a pattern on test strings. Takes the first match only.
	 * @param pattern
	 * @param tests
	 */
    private static void testRegexSingleMatch(String pattern, String[] tests) {
        o.printf("\n> Starting test for Regex (single match): >>%s<<", pattern);
        boolean found;
        StopwatchNano tn = new StopwatchNano();
        for (String s : tests) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(s);
            tn.start();
            found = m.find();
            tn.stop();
            o.println('\n' + (found ? "String >>".toUpperCase() + noNewlines(m.group()) + "<<" : "nothing".toUpperCase()) + " matched in >>".toUpperCase() + noNewlines(s) + "<<" + (found ? (m.start() == m.end() ? ", position ".toUpperCase() + m.start() : ", characters ".toUpperCase() + m.start() + " to " + m.end()) : ""));
            if (found) {
                for (int i = 0; i <= m.groupCount(); i++) {
                    o.println("Group ".toUpperCase() + i + ": >>" + m.group(i) + "<<");
                }
            }
            o.println("Search ".toUpperCase() + "took " + tn.getStoppedTimeString());
            tn.reset();
        }
        o.println("\n> Regex test finished");
    }

    private static void benchMatcherReplace(int n) {
        Pattern p = Pattern.compile("a");
        Matcher m;
        StopwatchNano sn = new StopwatchNano();
        sn.start();
        for (int i = 0; i < n; i++) {
            m = p.matcher(testText);
            StringBuffer out = new StringBuffer(m.replaceAll("b"));
        }
        sn.stop();
        System.out.println("Replacing all a&#x2019;s with b&#x2019;s took " + sn.getStoppedTimeString());
        sn.reset();
        m = p.matcher(testText);
        sn.start();
        for (int i = 0; i < n; i++) {
            m = p.matcher(testText);
            StringBuffer out = new StringBuffer(m.replaceAll("bbbbbbbbbb"));
        }
        sn.stop();
        System.out.println("Replacing all a&#x2019;s with 10 b&#x2019;s took " + sn.getStoppedTimeString());
        sn.reset();
        m = p.matcher(testText);
        sn.start();
        for (int i = 0; i < n; i++) {
            m = p.matcher(testText);
            StringBuffer out = new StringBuffer();
            int last = 0;
            while (m.find()) {
                out.append(testText.substring(last, m.start()));
                out.append("bbbbbbbbbb");
                last = m.end();
            }
            out.append(testText.substring(last, testText.length()));
            m.replaceAll("bbbbbbbbbb");
        }
        sn.stop();
        System.out.println("Replacing all a&#x2019;s with 10 b&#x2019;s took " + sn.getStoppedTimeString());
        sn.reset();
    }

    private static void benchStringBuffer(int exp) {
        double n = Math.pow(10, exp);
        StringBuffer s = new StringBuffer();
        String s2 = new String();
        StringBuffer content = new StringBuffer(testText);
        StopwatchNano tn = new StopwatchNano();
        int steps = (n < 100 ? 10 : (int) (n / 100));
        int rand;
        System.out.println("This bench reads substrings from a StringBuffer with a length of " + content.length() + " characters. \n");
        System.out.println("> subSequence to StringBuffer");
        tn.start();
        for (int i = 0; i < n; i++) {
            if (i % steps == 0) {
                System.out.print('.');
            }
            rand = (int) (Math.random() * content.length());
            s = new StringBuffer(content.subSequence(rand, (int) (rand + (Math.random() * (content.length() - rand - 1)))));
        }
        tn.stop();
        System.out.println("\nReading a StringBuffer of coincidal length via subSequence " + n + " sw: " + tn.getStoppedTimeString());
        tn.reset();
        System.out.println();
        System.out.println("> substring to StringBuffer");
        tn.start();
        for (int i = 0; i < n; i++) {
            if (i % steps == 0) {
                System.out.print('.');
            }
            rand = (int) (Math.random() * content.length());
            s = new StringBuffer(content.substring(rand, (int) (rand + (Math.random() * (content.length() - rand - 1)))));
        }
        tn.stop();
        System.out.println("\nReading a StringBuffer of coincidal length via substring " + n + " sw: " + tn.getStoppedTimeString());
        tn.reset();
        System.out.println('\n');
        System.out.println("> subSequence to String");
        tn.start();
        for (int i = 0; i < n; i++) {
            if (i % steps == 0) {
                System.out.print('.');
            }
            rand = (int) (Math.random() * content.length());
            s2 = content.subSequence(rand, (int) (rand + (Math.random() * (content.length() - rand - 1)))).toString();
        }
        tn.stop();
        System.out.println("\nReading a String of coincidal length via subSequence " + n + " sw: " + tn.getStoppedTimeString());
        tn.reset();
        System.out.println();
        System.out.println("> substring to String");
        tn.start();
        for (int i = 0; i < n; i++) {
            if (i % steps == 0) {
                System.out.print('.');
            }
            rand = (int) (Math.random() * content.length());
            s2 = content.substring(rand, (int) (rand + (Math.random() * (content.length() - rand - 1))));
        }
        tn.stop();
        System.out.println("\nReading a String of coincidal length via substring " + n + " sw: " + tn.getStoppedTimeString());
    }

    static final StringBuffer testText = new StringBuffer("Hunger. Stufe für Stufe schob sie sich die Treppe hinauf. " + "Pizza Funghi Salami, Sternchen Salami gleich Blockwurst. " + "Die Pilze hatten sechs Monate in einem Sarg aus Blech, abgeschattet vom Sonnenlicht, eingeschläfert in einer Sosse aus Essig, billigem " + "Öl und verschiedenen Geschmacksverstärkern, geruht. Es war nur ein Augenblick, in dem sie die Welt erblickt hatten, dann verschwanden " + "sie wieder in einem 450° heissen Ofen. Die Pizza ruhte auf ihrer rechten Hand, und in ihrer Linken hielt sie eine jener nichtssagenden " + "Plastiktüten. Wie fast jeden Abend hatte sie noch das weisse Häubchen aus dem Krankenhaus auf dem Kopf. Das Fettgewebe ihrer Schenkel " + "verspürte einen Heisshunger auf das müde Öl, das bei jedem Schritt sanft auf den Salamischeiben schaukelte. Die kleinen Zellen ihrer " + "heissen Oberschenkel waren gierig, als sie im Treppenhaus ein Geräusch hörte. Punkt 21 Uhr 53 hatte Herr Erlenkötter die Wohnungstür " + "hinter sich geschlossen. \nIn der Linken hielt er die Leine von Gershwin, der die Stadt und noch mehr die Ausflüge um diese Tageszeit " + "liebte. Herr Erlenkötter verschloss wie jeden Abend zuerst das obere Sicherheitsschloss und dann das Türschloss. Danach schnippte er den " + "Schlüssel mit einer schnellen Bewegung in das dafür vorgesehene Lederetui. Die Hand, die die Leine des Hundes hielt, half der anderen, und" + " nachdem er das Etui in seine rechte Jackentasche gesteckt hatte, begann für beide der Abend. Er begann für Gershwin, der schon an der " + "Leine zog, weil er den scharfen Geruch von Desinfektionsmitteln und die süssen Ausdünstungen der Blondine von unten gerochen hatte, und er " + "begann für Erwin. Sie hörte das Schliessen der Tür, als sie gerade den Briefkasten öffnete. Zwei Rechnungen und ein Brief fielen auf den " + "Boden. Einzig ein zweifach gefalteter Prospekt machte sich im Briefkasten breit. Es hatte alles verdrängt und wartete darauf, in liebevolle, " + "interessierte Finger genommen und von neugierigen Pupillen gelesen zu werden. Mit einem entschlossenen Griff zerdrückte sie ihn und riss " + "ihn aus dem Metallkasten. Sie knüllte ihn zusammen und warf ihn in einen Blecheimer zu Hunderten von Zetteln. Wartenden, die irgendwann " + "einmal von einem Handschuh nach oben gerissen wurden, um dann im dunklen Häckselwerk eines LKW zu landen. Dann begann jene feuchte Reise, " + "an deren Ende wieder ein neuer Prospekt stand. Nicht häufig spürte Gershwin den Geruch der Blondine im Treppenhaus in dieser Präsenz. " + "Manchmal standen noch vereinzelte Geruchsmarken zwischen dem Geländer. Aber es war nicht der Duft der Gegenwart. Es war eine " + "Vorvergangenheit, das Gefühl, zu spät dazusein. Für einen Moment eine Vergangenheit zu empfinden, die in 10 Minuten gänzlich der " + "Geschichte des Alltags anheimfiel. Einer Geschichte, die von niemand geschrieben und die in jeder Sekunde milliardenfach an anderen " + "Orten gelebt wird. Zwischen all der Süsse und Schärfe, die er von diesem Geruch kannte, roch er einen Anflug von Blut. Hellem, rotem " + "Blut. Sein Atem beschleunigte sich. Während seine Nüstern diesen klaren Geruch von Hühnchen bis in die letzte Kapillare seiner Lunge " + "einsaugte. Er musste dieses Hühnchen für einen Augenblick zwischen seinen Kiefern halten und seine Zähne in das tiefgefrorene Fleisch " + "schlagen, auch wenn er wusste, dass Erwin dieses Verhalten niemals tolerieren würde und sowohl der Abendspaziergang als auch die " + "Hundeplätzchen in den nächsten Tagen entfallen würden. Langsam schob sich die Krankenschwester, in der einen Hand die Pizza, in der " + "anderen die Einkaufstüte, nach oben. \nSie waren noch eine Stufe voneinander entfernt. Gershwin nahm sein Hundeherz zusammen und sprang. " + "Hunger. Stufe für Stufe schob sie sich die Treppe hinauf. Pizza Funghi Salami, Sternchen Salami gleich Blockwurst. Die Pilze hatten " + "sechs Monate in einem Sarg aus Blech, abgeschattet vom Sonnenlicht, eingeschläfert in einer Sosse aus Essig, billigem Öl und verschiedenen " + "Geschmacksverstärkern, geruht. Es war nur ein Augenblick, in dem sie die Welt erblickt hatten, dann verschwanden sie wieder in einem 450° " + "heissen Ofen. Die Pizza ruhte auf ihrer rechten Hand, und in ihrer Linken hielt sie eine jener nichtssagenden Plastiktüten. Wie fast jeden " + "Abend hatte sie noch das weisse Häubchen aus dem Krankenhaus auf dem Kopf. Das Fettgewebe ihrer Schenkel verspürte einen Heisshunger auf " + "das müde Öl, das bei jedem Schritt sanft auf den Salamischeiben schaukelte. Die kleinen Zellen ihrer heissen Oberschenkel waren gierig, " + "als sie im Treppenhaus ein Geräusch hörte. \nPunkt 21 Uhr 53 hatte Herr Erlenkötter die Wohnungstür hinter sich geschlossen. In der Linken " + "hielt er die Leine von Gershwin, der die Stadt und noch mehr die Ausflüge um diese Tageszeit liebte. Herr Erlenkötter verschloss wie " + "jeden Abend zuerst das obere Sicherheitsschloss und dann das Türschloss. Danach schnippte er den Schlüssel mit einer schnellen Bewegung " + "in das dafür vorgesehene Lederetui. Die Hand, die die Leine des Hundes hielt, half der anderen, und nachdem er das Etui in seine rechte " + "Jackentasche gesteckt hatte, begann für beide der Abend. Er begann für Gershwin, der schon an der Leine zog, weil er den scharfen Geruch " + "von Desinfektionsmitteln und die süssen Ausdünstungen der Blondine von unten gerochen hatte, und er begann für Erwin. Sie hörte das " + "Schliessen der Tür, als sie gerade den Briefkasten öffnete. Zwei Rechnungen und ein Brief fielen auf den Boden. Einzig ein zweifach " + "gefalteter Prospekt machte sich im Briefkasten breit. Es hatte alles verdrängt und wartete darauf, in liebevolle, interessierte Finger " + "genommen und von neugierigen Pupillen gelesen zu werden. Mit einem entschlossenen Griff zerdrückte sie ihn und riss ihn aus dem Metallkasten. " + "Sie knüllte ihn zusammen und warf ihn in einen Blecheimer zu Hunderten von Zetteln. Wartenden, die irgendwann einmal von einem Handschuh " + "nach oben gerissen wurden, um dann im dunklen Häckselwerk eines LKW zu landen. Dann begann jene feuchte Reise, an deren Ende wieder ein " + "neuer Prospekt stand. Nicht häufig spürte Gershwin den Geruch der Blondine im Treppenhaus in dieser Präsenz. Manchmal standen noch " + "vereinzelte Geruchsmarken zwischen dem Geländer. Aber es war nicht der Duft der Gegenwart. Es war eine Vorvergangenheit, das Gefühl, zu" + " spät dazusein. Für einen Moment eine Vergangenheit zu empfinden, die in 10 Minuten gänzlich der Geschichte des Alltags anheimfiel. " + "Einer Geschichte, die von niemand geschrieben und die in jeder Sekunde milliardenfach an anderen Orten gelebt wird. Zwischen all der " + "Süsse und Schärfe, die er von diesem Geruch kannte, roch er einen Anflug von Blut. Hellem, rotem Blut. Sein Atem beschleunigte sich. " + "Während seine Nüstern diesen klaren Geruch von Hühnchen bis in die letzte Kapillare seiner Lunge einsaugte. Er musste dieses Hühnchen " + "für einen Augenblick zwischen seinen Kiefern halten und seine Zähne in das tiefgefrorene Fleisch schlagen, auch wenn er wusste, dass " + "Erwin dieses Verhalten niemals tolerieren würde und sowohl der Abendspaziergang als auch die Hundeplätzchen in den nächsten Tagen " + "entfallen würden. Langsam schob sich die Krankenschwester, in der einen Hand die Pizza, in der anderen die Einkaufstüte, nach oben. " + "Sie waren noch eine Stufe voneinander entfernt. Gershwin nahm sein Hundeherz zusammen und sprang. Hunger. Stufe für Stufe schob sie " + "sich die Treppe hinauf. Pizza Funghi Salami, Sternchen Salami gleich Blockwurst. \nDie Pilze hatten sechs Monate in einem Sarg aus Blech, " + "abgeschattet vom Sonnenlicht, eingeschläfert in einer Sosse aus Essig, billigem Öl und verschiedenen Geschmacksverstärkern, geruht. Es " + "war nur ein Augenblick, in dem sie die Welt erblickt hatten, dann verschwanden sie wieder in einem 450° heissen Ofen. Die Pizza ruhte auf " + "ihrer rechten Hand, und in ihrer Linken hielt sie eine jener nichtssagenden Plastiktüten. Wie fast jeden Abend hatte sie noch das weisse " + "Häubchen aus dem Krankenhaus auf dem Kopf. Das Fettgewebe ihrer Schenkel verspürte einen Heisshunger auf das müde Öl, das bei jedem Schritt " + "sanft auf den Salamischeiben schaukelte. Die kleinen Zellen ihrer heissen Oberschenkel waren gierig, als sie im Treppenhaus ein Geräusch " + "hörte. Punkt 21 Uhr 53 hatte Herr Erlenkötter die Wohnungstür hinter sich geschlossen. In der Linken hielt er die Leine von Gershwin, der " + "die Stadt und noch mehr die Ausflüge um diese Tageszeit liebte. Herr Erlenkötter verschloss wie jeden Abend zuerst das obere " + "Sicherheitsschloss und dann das Türschloss. Danach schnippte er den Schlüssel mit einer schnellen Bewegung in das dafür vorgesehene " + "Lederetui. Die Hand, die die Leine des Hundes hielt, half der anderen, und nachdem er das Etui in seine rechte Jackentasche gesteckt hatte, " + "begann für beide der Abend. Er begann für Gershwin, der schon an der Leine zog, weil er den scharfen Geruch von Desinfektionsmitteln und " + "die süssen Ausdünstungen der Blondine von unten gerochen hatte, und er begann für Erwin. Sie hörte das Schliessen der Tür, als sie gerade " + "den Briefkasten öffnete. Zwei Rechnungen und ein Brief fielen auf den Boden. Einzig ein zweifach gefalteter Prospekt machte sich im " + "Briefkasten breit. Es hatte alles verdrängt und wartete darauf, in liebevolle, interessierte Finger genommen und von neugierigen Pupillen " + "gelesen zu werden. Mit einem entschlossenen Griff zerdrückte sie ihn und riss ihn aus dem Metallkasten. Sie knüllte ihn zusammen und warf " + "ihn in einen Blecheimer zu Hunderten von Zetteln. Wartenden, die irgendwann einmal von einem Handschuh nach oben gerissen wurden, um dann " + "im dunklen Häckselwerk eines LKW zu landen. Dann begann jene feuchte Reise, an deren Ende wieder ein neuer Prospekt stand. Nicht häufig " + "spürte Gershwin den Geruch der Blondine im Treppenhaus in dieser Präsenz. Manchmal standen noch vereinzelte Geruchsmarken zwischen dem " + "Geländer. Aber es war nicht der Duft der Gegenwart. Es war eine Vorvergangenheit, das Gefühl, zu spät dazusein. Für einen Moment eine " + "Vergangenheit zu empfinden, die in 10 Minuten gänzlich der Geschichte des Alltags anheimfiel. Einer Geschichte, die von niemand geschrieben " + "und die in jeder Sekunde milliar");
}
