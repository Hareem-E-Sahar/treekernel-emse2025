public class Test {    private boolean needsWhitespaceBetweenJavascript(InlineStringReader reader, InlineStringWriter writer, int a, int b) throws IOException {
        return (a == ')' && b == '{') || (a == ',') || (!isOperator(a) && b == '=') || (a == '=' && (b != '>' && b != '=')) || (b == '?') || (a == '?') || (b == '{') || (a != '(' && a != '!' && b == '!') || (a != '+' && b == '+' && reader.readAhead() != '+') || (a == '+' && b == '+' && reader.readAhead() == '+') || (a != '-' && b == '-' && reader.readAhead() != '-') || (a == '-' && b == '-' && reader.readAhead() == '-') || (a != '|' && b == '|') || (a != '&' && b == '&') || (b == '<' || b == '>') || (isOperator(a) && !isOperator(b) && b != ')' && a != '!' && b != ';' && !writer.getLastWritten(3).equals(", -") && !writer.getLastWritten(3).equals(", +")) || (a == ')' && isOperator(b)) || (b == '*') || (a == ')' && b == '-') || (a == ']' && b == ':') || (a == ')' && b == ':') || (a == ':' && b != ':' && !writer.getLastWritten(2).equals("::")) || (isPreviousWordReserved(writer) && (b == '(' || b == '$')) || (a == ')' && Character.isLetter(b)) || ((isOperator(a) || Character.isLetter(a)) && b == '"') || ((isOperator(a) || Character.isLetter(a)) && b == '\'') || (Character.isLetter(a) && isOperator(b) && !isTwoCharacterOperator(b, reader.readAhead()));
    }
}