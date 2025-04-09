package com.sshtools.j2ssh.transport.publickey;

import com.sshtools.j2ssh.SshThread;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class SshKeyGenerator {

    private static String filename = null;

    private static String type = "dsa";

    private static int bits = 1024;

    private static boolean useGUI;

    private static boolean guiAvailable;

    private static boolean toOpenSSH = false;

    private static boolean toSECSH = false;

    private static boolean changePass = false;

    static {
        try {
            Class.forName("com.sshtools.j2ssh.keygen.Main");
            guiAvailable = true;
        } catch (ClassNotFoundException cnfe) {
        }
    }

    /**
     * Creates a new SshKeyGenerator object.
     */
    public SshKeyGenerator() {
    }

    /**
     *
     *
     * @param type
     * @param bits
     * @param filename
     * @param username
     * @param passphrase
     *
     * @throws IOException
     */
    public void generateKeyPair(String type, int bits, String filename, String username, String passphrase) throws IOException {
        System.out.println("****Sshtools.com SSH Key Pair Generator****");
        String keyType = type;
        if (keyType.equalsIgnoreCase("DSA")) {
            keyType = "ssh-dss";
        }
        if (keyType.equalsIgnoreCase("RSA")) {
            keyType = "ssh-rsa";
        }
        final SshKeyPair pair = SshKeyPairFactory.newInstance(keyType);
        System.out.println("Generating " + String.valueOf(bits) + " bit " + keyType + " key pair");
        Thread thread = new SshThread(new Runnable() {

            public void run() {
                pair.generate(SshKeyGenerator.this.bits);
            }
        }, "Key generator", true);
        thread.start();
        while (thread.isAlive()) {
            System.out.print(".");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        System.out.println();
        System.out.println("Creating Public Key file " + filename + ".pub");
        SshPublicKeyFile pub = SshPublicKeyFile.create(pair.getPublicKey(), new SECSHPublicKeyFormat(username, String.valueOf(bits) + "-bit " + type));
        FileOutputStream out = new FileOutputStream(filename + ".pub");
        out.write(pub.getBytes());
        out.close();
        System.out.println("Generating Private Key file " + filename);
        if (passphrase == null) {
            passphrase = promptForPassphrase(true);
        }
        SshPrivateKeyFile prv = SshPrivateKeyFile.create(pair.getPrivateKey(), passphrase, new SshtoolsPrivateKeyFormat(username, String.valueOf(bits) + "-bit " + type));
        out = new FileOutputStream(filename);
        out.write(prv.getBytes());
        out.close();
    }

    /**
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            processCommandLine(args);
            if (useGUI) {
                Class c = Class.forName("com.sshtools.j2ssh.keygen.Main");
                Method m = c.getMethod("main", new Class[] { args.getClass() });
                m.invoke(null, new Object[] { new String[] {} });
            } else {
                File f = new File(filename);
                if (filename == null) {
                    System.err.print("You must supply a valid file to convert!");
                    System.exit(1);
                }
                if (toOpenSSH || toSECSH) {
                    if (!f.exists()) {
                        System.err.print("The file " + f.getAbsolutePath() + " does not exist!");
                        System.exit(1);
                    }
                    try {
                        if (toOpenSSH) {
                            System.out.print(convertPublicKeyFile(f, new OpenSSHPublicKeyFormat()));
                        } else {
                            System.out.print(convertPublicKeyFile(f, new SECSHPublicKeyFormat()));
                        }
                    } catch (InvalidSshKeyException e) {
                        System.err.println("The key format is invalid!");
                    } catch (IOException ioe) {
                        System.err.println("An error occurs whilst reading the file " + f.getAbsolutePath());
                    }
                    System.exit(0);
                }
                if (changePass) {
                    if (!f.exists()) {
                        System.err.print("The file " + f.getAbsolutePath() + " does not exist!");
                        System.exit(1);
                    }
                    changePassphrase(f);
                } else {
                    SshKeyGenerator generator = new SshKeyGenerator();
                    String username = System.getProperty("user.name");
                    generator.generateKeyPair(type, bits, filename, username, null);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     *
     *
     * @param args
     */
    public static void processCommandLine(String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-b")) {
                    bits = Integer.parseInt(args[++i]);
                } else if (args[i].equalsIgnoreCase("-t")) {
                    type = args[++i];
                } else if (args[i].equalsIgnoreCase("-p")) {
                    changePass = true;
                } else if (args[i].equalsIgnoreCase("-g") && guiAvailable) {
                    useGUI = true;
                } else if (args[i].equalsIgnoreCase("-i")) {
                    toOpenSSH = true;
                } else if (args[i].equalsIgnoreCase("-e")) {
                    toSECSH = true;
                } else if (!args[i].startsWith("-")) {
                    if (filename != null) {
                        printUsage();
                        System.exit(1);
                    }
                    filename = args[i];
                }
            }
        }
        if (!useGUI && (filename == null)) {
            printUsage();
            System.exit(0);
        }
    }

    private static void changePassphrase(File f) {
        System.out.println("Opening Private Key file " + f.getAbsolutePath());
        try {
            System.out.println("Opening Private Key file " + f.getAbsolutePath());
            String oldPassphrase = promptForPassphrase(false);
            String newPassphrase = promptForPassphrase(true);
            changePassphrase(f, oldPassphrase, newPassphrase);
        } catch (InvalidSshKeyException e) {
            System.err.println("The key format is invalid!");
        } catch (IOException ioe) {
            System.err.println("An error occurs whilst reading the file " + f.getAbsolutePath());
        }
    }

    /**
     *
     *
     * @param f
     * @param oldPassphrase
     * @param newPassphrase
     *
     * @throws IOException
     * @throws InvalidSshKeyException
     */
    public static void changePassphrase(File f, String oldPassphrase, String newPassphrase) throws IOException, InvalidSshKeyException {
        SshPrivateKeyFile file = SshPrivateKeyFile.parse(f);
        System.out.println("Saving Private Key file with new passphrase");
        file.changePassphrase(oldPassphrase, newPassphrase);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            out.write(file.getBytes());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     *
     *
     * @param f
     * @param convert
     *
     * @return
     *
     * @throws InvalidSshKeyException
     * @throws IOException
     */
    public static String convertPublicKeyFile(File f, SshPublicKeyFormat convert) throws InvalidSshKeyException, IOException {
        SshPublicKeyFile file = SshPublicKeyFile.parse(f);
        file.setFormat(convert);
        return file.toString();
    }

    private static void printUsage() {
        System.out.println("Usage: SshKeyGenerator [options] filename");
        System.out.println("Options:");
        System.out.println("-b bits        Number of bits in the key to create.");
        System.out.println("-e             Convert OpenSSH to IETF SECSH key file.");
        System.out.println("-i             Convert IETF SECSH to OpenSSH key file.");
        System.out.println("-t type        The type of key to create.");
        System.out.println("-p             Change the passphrase of the private key file.");
        if (guiAvailable) {
            System.out.println("-g \t\tUse GUI to create key");
        }
    }

    private static String promptForPassphrase(boolean confirm) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String pass1 = "";
        String pass2 = "";
        while (true) {
            System.out.print("Enter passphrase: ");
            pass1 = reader.readLine();
            if (!confirm) {
                break;
            }
            System.out.print("Confirm passphrase: ");
            pass2 = reader.readLine();
            if (pass1.equals(pass2)) {
                if (pass1.trim().length() == 0) {
                    System.out.print("You supplied an empty passphrase, are you sure? [Yes|No]: ");
                    pass2 = reader.readLine();
                    if (pass2.equalsIgnoreCase("YES")) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                System.out.println("The passphrases supplied were not indentical! Try again");
            }
        }
        return pass1;
    }
}
