public class Test {    public static void main(String[] arg) {
        if (arg.length != 1 && arg.length != 2) {
            System.err.println("Usage - java org.mortbay.jetty.security.Password [<user>] <password>");
            System.err.println("If the password is ?, the user will be prompted for the password");
            System.exit(1);
        }
        String p = arg[arg.length == 1 ? 0 : 1];
        Password pw = "?".equals(p) ? new Password(p) : new Password(p);
        System.err.println(pw.toString());
        System.err.println(obfuscate(pw.toString()));
        System.err.println(Credential.MD5.digest(p));
        if (arg.length == 2) System.err.println(Credential.Crypt.crypt(arg[0], pw.toString()));
    }
}