package com.sutorit.java.RoboQuiz.AppBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Scanner;
import com.sutorit.java.RoboQuiz.AppBuilder.util.SqlHelper;
import com.sutorit.java.RoboQuiz.AppBuilder.view.AppBuilderWindow;

/**
 * @author Tobias C. Sutor <tobias@sutor-it.com>
 */
public class Builder {

    private static final StringBuilder logOutput = new StringBuilder();

    private static final String REPLACE_IMPORT = "import com.sutorit.android.roboquiz";

    private static final String REPLACE_PACKAGE = "com.sutorit.android.roboquiz";

    private static final String REPLACE_R_IMPORT = Builder.REPLACE_IMPORT + ".R";

    private final String appName;

    private final String appNameSystem;

    private final String author;

    private final boolean backAllowed;

    private final String iconPath;

    private final File outputFolder;

    private final File outputSrcFolder;

    private final String packageName;

    private final String packageNameFull;

    private final int percentage;

    private final String replace_import;

    private final String replace_r_import;

    private final File sdkFolder;

    private final boolean showAnsAmount;

    private final File templateFolder;

    private final File templateSrcPath;

    private final int time;

    private final String web;

    /**
     * @param appName the appname to convert
     * @return the appname without whitespaces and all lowercase
     */
    public static String appNameToAppNameSystem(final String appName) {
        return appName.trim().toLowerCase().replace(" ", "");
    }

    /**
     * @param web the webadress to be converted (i.e.: mywebsite.com)
     * @return the package name (i.e.: com.mywebsite.android)
     */
    public static String createPackageUrl(final String web) {
        final String[] tmpWeb = web.split("\\.");
        final StringBuilder packageName = new StringBuilder(web.length());
        for (int i = tmpWeb.length - 1; i >= 0; i--) {
            if (i != tmpWeb.length - 1) {
                packageName.append(".");
            }
            packageName.append(tmpWeb[i]);
        }
        packageName.append(".android");
        return packageName.toString();
    }

    /**
     * @return the logOutput
     */
    public static final String getLogOutput() {
        return Builder.logOutput.toString();
    }

    /**
     * @param command the command to execute
     * @return the output of the command - if any
     */
    public static final String runCommand(final String command) {
        Builder.logOutput.delete(0, Builder.logOutput.length());
        try {
            final Process child = Runtime.getRuntime().exec(command);
            final InputStream in = child.getInputStream();
            int c;
            while ((c = in.read()) != -1) {
                Builder.logOutput.append((char) c);
            }
            in.close();
        } catch (final IOException e1) {
        }
        return Builder.logOutput.toString();
    }

    /**
     * @param appName the name of the app
     * @param backAllowed whether the user is allowed
     * @param outputFolder path of the folder where the final app should be
     *        created
     * @param packageName name of the app-package (without the app-name)
     * @param percentage the required score to pass the quiz
     * @param sdkFolder path to the android sdk
     * @param showAnsAmount whether the quiz should show the amount of expected
     *        answers
     * @param templateFolder path to the folder where the android-app template
     *        resides
     * @param time the time per question in seconds
     */
    public Builder(final String appName, final boolean backAllowed, final String outputFolder, final String packageName, final int percentage, final String sdkFolder, final boolean showAnsAmount, final String templateFolder, final int time) {
        this(sdkFolder, templateFolder, outputFolder, time, backAllowed, showAnsAmount, percentage, appName, packageName, "", "");
    }

    /**
     * @param appName the name of the app
     * @param backAllowed whether the user is allowed
     * @param outputFolder path of the folder where the final app should be
     *        created
     * @param packageName name of the app-package (without the app-name)
     * @param percentage the required score to pass the quiz
     * @param sdkFolder path to the android sdk
     * @param showAnsAmount whether the quiz should show the amount of expected
     *        answers
     * @param templateFolder path to the folder where the android-app template
     *        resides
     * @param time the time per question in seconds
     * @param iconPath path to the icon for the app
     * @param author name of the app-author
     */
    public Builder(final String sdkFolder, final String templateFolder, final String outputFolder, final int time, final boolean backAllowed, final boolean showAnsAmount, final int percentage, final String appName, final String packageName, final String iconPath, final String author) {
        this.sdkFolder = new File(sdkFolder);
        this.templateFolder = new File(templateFolder);
        this.outputFolder = new File(outputFolder);
        this.time = time;
        this.backAllowed = backAllowed;
        this.showAnsAmount = showAnsAmount;
        this.percentage = percentage;
        this.iconPath = iconPath;
        this.appName = appName;
        this.appNameSystem = Builder.appNameToAppNameSystem(appName);
        this.author = author;
        this.web = packageName;
        this.packageName = Builder.createPackageUrl(packageName);
        this.packageNameFull = this.packageName.concat("." + this.appNameSystem);
        final StringBuilder tmpPath = new StringBuilder(this.outputFolder.getAbsolutePath());
        tmpPath.append(File.separator + "src");
        for (final String s : this.packageName.split("\\.")) {
            tmpPath.append(File.separator);
            tmpPath.append(s);
        }
        this.outputSrcFolder = new File(tmpPath.toString());
        this.replace_import = "import " + this.packageName + "." + this.appNameSystem;
        this.replace_r_import = this.replace_import + ".R";
        this.templateSrcPath = new File(this.templateFolder, File.separator + "src" + File.separator + "com" + File.separator + "sutorit" + File.separator + "android" + File.separator + "roboquiz");
    }

    /**
     * Copies the database
     * 
     * @return whether the file could be copied
     */
    public boolean stepEight() {
        Builder.logOutput.delete(0, Builder.logOutput.length());
        Builder.logOutput.append("\nCopying the database...");
        try {
            this.copyFile(SqlHelper.getDatabaseFile(), new File(this.outputFolder, "assets".concat(File.separator).concat("input.db")));
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Sets the appname in the language files
     * 
     * @return whether the name could be changed
     */
    public boolean stepFive() {
        Builder.logOutput.delete(0, Builder.logOutput.length());
        Builder.logOutput.append("\nDefining the Appname...");
        for (final File f : new File(this.outputFolder, "res").listFiles()) {
            if (f.isDirectory() && f.getName().startsWith("values")) {
                Builder.logOutput.append("\nLanguage: " + (f.getName().length() == 6 ? "en" : f.getName().substring(f.getName().length() - 2)));
                for (final File child : f.listFiles()) {
                    if (!child.isHidden()) {
                        if (!this.copySourceFiles(child, f, 2)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Adjusts the settings in the static-file (time, percentage etc.)
     * 
     * @return whether the file could be modified
     */
    public boolean stepFour() {
        Builder.logOutput.delete(0, Builder.logOutput.length());
        Builder.logOutput.append("\nAdjusting settings...");
        if (!this.copySourceFiles(new File(this.outputSrcFolder, "Statics.java"), this.outputSrcFolder, 0)) {
            return false;
        }
        return true;
    }

    /**
     * Builds the app with ant
     * 
     * @return whether the process was successfull
     */
    public boolean stepNine() {
        Builder.logOutput.delete(0, Builder.logOutput.length());
        try {
            final String command = "ant -f " + this.outputFolder + File.separator + "build.xml debug";
            Builder.logOutput.append("Building project: \n" + command);
            final Process child = Runtime.getRuntime().exec(command);
            Builder.logOutput.append("\n");
            final InputStream in = child.getInputStream();
            int c;
            final InputStreamReader isr = new InputStreamReader(in);
            while ((c = isr.read()) != -1) {
                if (isr.ready()) {
                    Builder.logOutput.append((char) c);
                    AppBuilderWindow.updateBuildOutputLog(Builder.logOutput.toString());
                    Builder.logOutput.delete(0, Builder.logOutput.length());
                }
            }
            Builder.logOutput.append("\n");
            in.close();
            isr.close();
            return true;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates an Android-Project
     * 
     * @return whether the creation of the project was successful
     */
    public boolean stepOne() {
        Builder.logOutput.delete(0, Builder.logOutput.length());
        return this.createAndroidProject();
    }

    public boolean stepSeven() {
        Builder.logOutput.delete(0, Builder.logOutput.length());
        if (this.iconPath.length() > 0) {
            final File icon = new File(this.iconPath);
            Builder.logOutput.append("\nCopying Icon...");
            for (final File f : new File(this.outputFolder, "res").listFiles()) {
                if (f.isDirectory() && f.getName().startsWith("drawable")) {
                    for (final File child : f.listFiles()) {
                        if (child.getName().equals("icon.png")) {
                            try {
                                child.delete();
                                this.copyFile(icon, child.getAbsoluteFile());
                                Builder.logOutput.append("\nCopied icon from: " + this.iconPath + " to: " + child.getAbsolutePath());
                            } catch (final IOException e) {
                                e.printStackTrace();
                                return false;
                            }
                        }
                    }
                }
            }
        } else {
            Builder.logOutput.append("\nNo Icon set - nothing to do.");
        }
        return true;
    }

    /**
     * Adjust the AndroidManifest.xml
     * 
     * @return whether the file could be changed
     */
    public boolean stepSix() {
        Builder.logOutput.delete(0, Builder.logOutput.length());
        Builder.logOutput.append("\nAdjusting AndroidManifest.xml...");
        if (!this.copySourceFiles(new File(this.outputFolder, "AndroidManifest.xml"), this.outputFolder, 4)) {
            return false;
        }
        return true;
    }

    /**
     * Copies the view files and replaces the import- and package-definitions
     * 
     * @return whether the creation of the view-files was successful
     */
    public boolean stepThree() {
        Builder.logOutput.delete(0, Builder.logOutput.length());
        Builder.logOutput.append("\nPreparing source files...");
        Builder.logOutput.append("\nTemplate source: " + this.templateSrcPath);
        Builder.logOutput.append("\nOutput source: " + this.outputSrcFolder);
        for (final File parent : this.templateSrcPath.listFiles()) {
            if (parent.isDirectory()) {
                final File outP = new File(this.outputSrcFolder, parent.getName());
                outP.mkdir();
                Builder.logOutput.append("\nDirectory created: " + outP.getAbsolutePath());
                for (final File child : parent.listFiles()) {
                    if (child.getName().endsWith("java")) {
                        if (!this.copySourceFiles(child, outP, 1)) {
                            return false;
                        }
                    }
                }
            }
            if (parent.getName().endsWith("java")) {
                if (!this.copySourceFiles(parent, this.outputSrcFolder, 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Copies the assets, libs, res and AndroidManifest files
     * 
     * @return whether the process was successful
     */
    public boolean stepTwo() {
        Builder.logOutput.delete(0, Builder.logOutput.length());
        Builder.logOutput.append("\nRemoving wrong MainMenu");
        new File(this.outputSrcFolder, File.separator + "MainMenu.java").delete();
        Builder.logOutput.append("\nRemoving wrong main.xml");
        new File(this.outputFolder, File.separator + "res" + File.separator + "layout" + File.separator + "main.xml").delete();
        try {
            this.copyDirectory(new File(this.templateFolder, "assets"), new File(this.outputFolder, "assets"));
            Builder.logOutput.append("\nCopying assets");
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            this.copyDirectory(new File(this.templateFolder, "libs"), new File(this.outputFolder, "libs"));
            Builder.logOutput.append("\nCopying libs");
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            this.copyDirectory(new File(this.templateFolder, "res"), new File(this.outputFolder, "res"));
            Builder.logOutput.append("\nCopying res");
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            this.copyFile(new File(this.templateFolder, "AndroidManifest.xml"), new File(this.outputFolder, "AndroidManifest.xml"));
            Builder.logOutput.append("\nCopying AndroidManifest.xml");
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * @param srcDir
     * @param dstDir
     * @throws IOException
     */
    private void copyDirectory(final File srcDir, final File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
            final String[] children = srcDir.list();
            for (final String element : children) {
                this.copyDirectory(new File(srcDir, element), new File(dstDir, element));
            }
        } else {
            this.copyFile(srcDir, dstDir);
        }
    }

    private void copyFile(final File src, final File dst) throws IOException {
        final InputStream in = new FileInputStream(src);
        final OutputStream out = new FileOutputStream(dst);
        final byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Copies and manipulates the source files
     * 
     * @param f file
     * @param parent parent folder
     * @return whether the file could be created
     */
    private boolean copySourceFiles(final File f, final File parent, final int filetype) {
        Scanner input;
        FileWriter fw;
        final StringBuilder output = new StringBuilder();
        try {
            input = new Scanner(f);
            input.useDelimiter("\n");
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        while (input.hasNext()) {
            output.append(input.nextLine() + "\n");
        }
        input.close();
        try {
            final File out = new File(parent, f.getName());
            out.createNewFile();
            fw = new FileWriter(out.getAbsoluteFile());
            fw.write(this.modifySourceFile(output.toString(), filetype));
            fw.close();
            Builder.logOutput.append("\nFile created: " + out.getAbsoluteFile());
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
        output.delete(0, output.length());
        return true;
    }

    private boolean createAndroidProject() {
        try {
            final String command = this.sdkFolder + File.separator + "tools" + File.separator + "android create project " + "--target 7 " + "--name " + this.appNameSystem + " --path " + this.outputFolder + " --activity MainMenu " + " --package " + this.packageName;
            Builder.logOutput.append("Creating project: \n");
            Builder.logOutput.append(command);
            final Process child = Runtime.getRuntime().exec(command);
            Builder.logOutput.append("\n");
            final InputStream in = child.getInputStream();
            int c;
            while ((c = in.read()) != -1) {
                Builder.logOutput.append((char) c);
            }
            Builder.logOutput.append("\n");
            in.close();
            return true;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String modifySourceFile(final String content, final int filetype) {
        String res = content;
        switch(filetype) {
            case 0:
                res = res.replace("allowedTime = 60", "allowedTime = " + this.time);
                Builder.logOutput.append("\nSetting allowed time to: " + this.time);
                res = res.replace("backAllowed = true", "backAllowed = " + this.backAllowed);
                Builder.logOutput.append("\nSetting back allowe to: " + this.backAllowed);
                res = res.replace("percentForSuccess = 85", "percentForSuccess = " + this.percentage);
                Builder.logOutput.append("\nSetting required score to: " + this.percentage);
                res = res.replace("showAmountCorrectAnswers = true", "showAmountCorrectAnswers = " + this.showAnsAmount);
                Builder.logOutput.append("\nSetting amount of answers to: " + this.showAnsAmount);
                break;
            case 1:
                res = res.replace(Builder.REPLACE_R_IMPORT, this.replace_r_import);
                res = res.replace(Builder.REPLACE_PACKAGE, this.packageNameFull);
                break;
            case 2:
                res = res.replace("My Quiz App", this.appName);
                Builder.logOutput.append("\nSetting Appname to: " + this.appName);
                res = res.replace("AUTHOR", this.author);
                Builder.logOutput.append("\nSetting Author to: " + this.author);
                res = res.replace("WEB", this.web);
                Builder.logOutput.append("\nSetting Web to: " + this.web);
                break;
            case 3:
                break;
            case 4:
                res = res.replace(Builder.REPLACE_PACKAGE, this.packageNameFull);
                break;
        }
        return res;
    }
}
