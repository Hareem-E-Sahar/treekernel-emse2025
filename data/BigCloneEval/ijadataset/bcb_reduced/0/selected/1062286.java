package testes;

import java.util.regex.*;
import java.io.*;

public class ExpressaoRegular1 {

    public static void main(String args[]) {
        Console console = System.console();
        String regex = console.readLine("%nInforme a expressão: ");
        Pattern pattern = Pattern.compile(regex);
        String source = console.readLine("Informe a entrada: ");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            System.out.printf("Encontrado: \"%s\" de %d à %d.%n", matcher.group(), matcher.start(), matcher.end());
        }
    }
}
