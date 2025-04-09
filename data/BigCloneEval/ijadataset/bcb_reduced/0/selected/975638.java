package server;

import objects.*;
import objects.util.GamerAddress;
import objects.util.LockServer;
import util.*;
import util.bl.BlackList;
import util.bl.BlackListIO;
import util.bl.BlackListItem;
import util.schedule.Holiday;
import util.schedule.Schedule;
import util.schedule.ScheduleItem;
import java.io.*;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class OGSserver {

    private static final Logger logger = Logger.getLogger("ogs.server");

    private static Map<String, IServerCommand> userCommands;

    private static Map<String, IServerCommand> adminCommands;

    private static final Properties props = new SuperProperties();

    public static boolean isSendMail = false;

    private static boolean noEnd = false;

    public static void main(String[] args) {
        boolean isCheckMail = false;
        boolean useSchedule = false;
        int i = 0;
        while (i < args.length) {
            String opt = args[i++];
            if ("-help".equals(opt)) {
                System.out.println("Usage: opengs [-options] [file...]");
                System.out.println("where options include:");
                System.out.println("    -help         show this message");
                System.out.println("    -verbose      log details");
                System.out.println("    -mail         check and send mail");
                System.out.println("    -nomail       not check and send mail");
                System.out.println("    -checkmail    check mail");
                System.out.println("    -nocheckmail  not check mail");
                System.out.println("    -sendmail     send mail");
                System.out.println("    -nosendmail   not send mail");
                System.out.println("    -schedule     use schedule for turn generation");
                System.out.println("    -noschedule   not use schedule for turn generation");
                System.out.println("    -end          stop processing comands after multiline command");
                System.out.println("    -noend        continue processing comands after multiline command");
                System.out.println("    --            end of options list");
                System.exit(0);
            } else if ("-verbose".equals(opt)) {
                Galaxy.getLogger().setLevel(Level.ALL);
                Galaxy.getLogger().config("Verbose mode on");
            } else if ("-mail".equals(opt)) {
                isCheckMail = true;
                isSendMail = true;
            } else if ("-nomail".equals(opt)) {
                isCheckMail = false;
                isSendMail = false;
            } else if ("-checkmail".equals(opt)) {
                isCheckMail = true;
            } else if ("-nocheckmail".equals(opt)) {
                isCheckMail = false;
            } else if ("-sendmail".equals(opt)) {
                isSendMail = true;
            } else if ("-nosendmail".equals(opt)) {
                isSendMail = false;
            } else if ("-schedule".equals(opt)) {
                useSchedule = true;
            } else if ("-noschedule".equals(opt)) {
                useSchedule = false;
            } else if ("-end".equals(opt)) {
                noEnd = false;
            } else if ("-noend".equals(opt)) {
                noEnd = true;
            } else if ("--".equals(opt)) {
                break;
            } else if ("-".equals(opt)) {
                --i;
                break;
            } else if (opt.startsWith("-")) {
                Galaxy.getLogger().severe("Unknown switch " + opt);
                System.exit(1);
            } else {
                --i;
                break;
            }
        }
        if (!isCheckMail) Galaxy.getLogger().config("Don't check mail");
        if (!isSendMail) Galaxy.getLogger().config("Don't send mail");
        System.setProperty("line.separator", "\r\n");
        try {
            props.load(new InputStreamReader(OGSserver.class.getResourceAsStream("/server.properties"), "UTF-8"));
            props.load(new InputStreamReader(new FileInputStream(Utils.joinPath("ini", "server.ini")), "UTF-8"));
            props.load(new InputStreamReader(new FileInputStream(Utils.joinPath("ini", "mail.ini")), "UTF-8"));
            props.load(new InputStreamReader(OGSserver.class.getResourceAsStream("/version.txt"), "UTF-8"));
        } catch (IOException err) {
            Galaxy.getLogger().log(Level.SEVERE, "Can't load server properties", err);
            System.exit(1);
        }
        int port = Integer.parseInt(OGSserver.getProperty("Server.LockedPort"));
        LockServer lockServerThread = LockServer.open(port);
        if (lockServerThread == null) {
            Galaxy.getLogger().severe("Another copy of server runing - exit");
            System.exit(1);
        }
        Profiler.getProfiler().start("total");
        try {
            System.setProperty("file.encoding", OGSserver.getProperty("Server.Charset", "UTF-8"));
            String timeZone = OGSserver.getProperty("Server.TimeZone");
            if (timeZone != null) TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
            Galaxy.getLogger().config("TimeZone: " + TimeZone.getDefault().getDisplayName());
            for (String key : new String[] { "Server.SaveInfo.InformWebServer", "Server.SaveInfo.InformWebServer.URL", "Server.SaveInfo" }) System.setProperty(key, OGSserver.getProperty(key));
            loadBlackList();
            if (useSchedule) {
                processSchedule();
            }
            if (isSendMail) {
                sendWeeklyMessage();
            }
            userCommands = loadCommands(OGSserver.getProperties(), false);
            adminCommands = loadCommands(OGSserver.getProperties(), true);
            if (isCheckMail) processMail(); else {
                while (i < args.length) {
                    String fileName = args[i++];
                    InputStream is;
                    if ("-".equals(fileName)) is = System.in; else try {
                        is = new FileInputStream(fileName);
                    } catch (FileNotFoundException err) {
                        Galaxy.getLogger().log(Level.SEVERE, "Commands executing", err);
                        continue;
                    }
                    exec(null, new InputStreamReader(is));
                }
            }
        } finally {
            Profiler.getProfiler().stop("total");
            lockServerThread.close();
            Profiler.getProfiler().flush();
            System.gc();
        }
    }

    private static void processSchedule() {
        Date date = new Date();
        for (Holiday holiday : Holiday.getHolidays()) if (holiday.contains(date)) return;
        int pause = 0;
        try {
            pause = Integer.parseInt(OGSserver.getProperty("Server.Schedule.Pause", "30"));
        } catch (NumberFormatException err) {
            Galaxy.getLogger().severe("Bad value for schedule pause");
        }
        List<String> needArchive = new ArrayList<String>();
        for (String gameName : getGameNames()) {
            Schedule sch = Schedule.getSchedule(Utils.joinPath(Galaxy.GAMES_DIR, gameName));
            if (sch.isTimeToArchive(date)) needArchive.add(gameName); else if (sch.isTimeToGenerate(date) && !sch.isDateInsideStopPeriod(date)) {
                if (sch.isTooLateToGenerate(date, pause)) {
                    Galaxy.getLogger().severe("Game " + gameName + " generation skipped");
                    sch.updateNextScheduleDate(sch.getTurn());
                    Schedule.commitSchedule(Utils.joinPath(Galaxy.GAMES_DIR, gameName), sch);
                } else {
                    Galaxy galaxy = Galaxy.load(gameName);
                    if (galaxy == null) continue;
                    if (galaxy.getState() == Galaxy.State.GAME || galaxy.getState() == Galaxy.State.RECRUITING) {
                        sch.updateNextScheduleDate(galaxy.getTurn() + 2);
                        Schedule.commitSchedule(Utils.joinPath(Galaxy.GAMES_DIR, gameName), sch);
                        Galaxy.getLogger().info("Generate " + galaxy.getName() + " turn " + (galaxy.getTurn() + 1) + " by schedule");
                        generateGame(galaxy);
                    }
                }
            }
        }
        for (String gameName : needArchive) {
            Galaxy galaxy = Galaxy.load(gameName);
            if (galaxy == null) continue;
            if (galaxy.getState() != Galaxy.State.FINAL && galaxy.getState() != Galaxy.State.ARCHIVE) {
                Galaxy.getLogger().severe("Game " + galaxy.getName() + " : time to archive, but game state != FINAL, break archiving");
                continue;
            }
            if (!archiveGame(galaxy)) break;
        }
    }

    public static boolean archiveGame(Galaxy galaxy) {
        Galaxy.getLogger().info("Archiving game " + galaxy.getName());
        if (!Utils.deleteDir(Utils.joinPath(galaxy.getGameDir(), Galaxy.PLAYERS_DIR))) Galaxy.getLogger().severe("Can't delete directory " + Utils.joinPath(galaxy.getGameDir(), Galaxy.PLAYERS_DIR));
        galaxy.setState(Galaxy.State.ARCHIVE);
        galaxy.save();
        File arc = new File(Galaxy.ARCHIVE_DIR);
        if (!arc.isDirectory()) if (!arc.mkdir()) {
            Galaxy.getLogger().severe("Archiving - can't create archive directory");
            return false;
        }
        File gamedir = Utils.joinPath(Galaxy.GAMES_DIR, galaxy.getName());
        File archivedir = Utils.joinPath(Galaxy.ARCHIVE_DIR, galaxy.getName());
        if (!gamedir.renameTo(archivedir)) {
            Galaxy.getLogger().severe("Can't move game " + galaxy.getName() + "to archive");
            return false;
        }
        Galaxy.getLogger().info("Game " + galaxy.getName() + " moved to archive");
        galaxy.log("Game moved to archive");
        return true;
    }

    private static void processMail() {
        javax.mail.Session session = javax.mail.Session.getInstance(OGSserver.getProperties(), new MailAuthenticator());
        try {
            javax.mail.URLName url;
            if (props.getProperty("mail.store.url") != null) url = new javax.mail.URLName(props.getProperty("mail.store.url")); else url = new javax.mail.URLName(OGSserver.getProperty("mail.store.protocol"), OGSserver.getProperty("mail.store.host"), Integer.parseInt(OGSserver.getProperty("mail.store.port", "-1")), OGSserver.getProperty("mail.store.file"), OGSserver.getProperty("mail.store.user"), OGSserver.getProperty("mail.store.password"));
            javax.mail.Store store = session.getStore(url);
            store.connect(url.getUsername(), url.getPassword());
            javax.mail.Folder folder = store.getFolder(OGSserver.getProperty("mail.store.file", "inbox"));
            if (!folder.exists()) {
                Galaxy.getLogger().severe("Mail folder does not exist");
                store.close();
                System.exit(1);
            }
            folder.open(javax.mail.Folder.READ_WRITE);
            try {
                while (folder.getMessageCount() > 0) {
                    javax.mail.internet.MimeMessage msg = (javax.mail.internet.MimeMessage) folder.getMessage(1);
                    try {
                        if (testMessage(msg)) {
                            String body = getMessageText(msg);
                            if (body == null) processBadMessage(msg); else try {
                                exec(msg, new StringReader(body));
                            } catch (Exception err) {
                                Galaxy.getLogger().log(Level.SEVERE, "Internal error", err);
                                processBadMessage(msg);
                            }
                        }
                    } catch (javax.mail.MessagingException err) {
                        MailFactory.getLogger().log(Level.FINE, "Illegal message", err);
                    } finally {
                        msg.setFlag(javax.mail.Flags.Flag.DELETED, true);
                        folder.close(true);
                        folder.open(javax.mail.Folder.READ_WRITE);
                    }
                }
            } finally {
                folder.close(true);
                store.close();
            }
        } catch (javax.mail.MessagingException err) {
            MailFactory.getLogger().log(Level.SEVERE, "Mail processing error", err);
        }
    }

    private static String getMessageText(javax.mail.internet.MimeMessage msg) throws javax.mail.MessagingException {
        try {
            String result = getPartText(msg, msg);
            if (result == null) {
                ignoreMessage(msg, "Illegal content type");
                replyMailError(msg, "illegal_content_type");
            }
            return result;
        } catch (UnsupportedEncodingException err) {
            MailFactory.getLogger().log(Level.WARNING, "Unsupported encoding", err);
            replyMailError(msg, "unsupported_encoding");
            return null;
        } catch (IOException err) {
            MailFactory.getLogger().log(Level.SEVERE, "Message reading error", err);
            replyMailError(msg, "reading_error");
            return null;
        }
    }

    private static String getPartText(javax.mail.internet.MimeMessage msg, javax.mail.internet.MimePart part) throws javax.mail.MessagingException, IOException {
        javax.mail.internet.ContentType contentType = new javax.mail.internet.ContentType(part.getContentType());
        if (contentType.match("text/plain")) return readAll(part, getCharset(msg, part, contentType));
        if (contentType.match("text/html")) return HtmlParser.htmlToText(readAll(part, getCharset(msg, part, contentType)));
        if (contentType.match("multipart/*")) {
            javax.mail.Multipart mp = (javax.mail.Multipart) part.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; ++i) {
                String result = getPartText(msg, (javax.mail.internet.MimePart) mp.getBodyPart(i));
                if (result != null && !result.trim().isEmpty()) return result;
            }
        }
        return null;
    }

    private static String getCharset(javax.mail.internet.MimeMessage msg, javax.mail.internet.MimePart part, javax.mail.internet.ContentType contentType) throws javax.mail.MessagingException {
        String charset = contentType.getParameter("charset");
        if (charset == null) {
            if ("7bit".equals(part.getEncoding())) return "ascii";
            MailFactory.getLogger().fine("Not specified charset");
            replyMailError(msg, "unspecified_encoding");
            charset = OGSserver.getProperty("mail.mime.charset");
            if (charset == null) charset = javax.mail.internet.MimeUtility.getDefaultJavaCharset();
        }
        return charset;
    }

    private static String readAll(javax.mail.Part part, String charset) throws IOException, javax.mail.MessagingException {
        InputStreamReader in = new InputStreamReader(part.getInputStream(), charset);
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[1 << 16];
        int count;
        while ((count = in.read(buffer, 0, buffer.length)) >= 0) builder.append(buffer, 0, count);
        return builder.toString();
    }

    private static void processBadMessage(javax.mail.internet.MimeMessage msg) {
        if (Utils.parseBoolean(getProperty("mail.gm.forwardspam", "no"))) try {
            javax.mail.Message forward = MailFactory.newMessage(null, "[OGS] Bad message");
            MailFactory.setGameMasterAddress(msg);
            javax.mail.Multipart multipart = new javax.mail.internet.MimeMultipart();
            javax.mail.BodyPart messageBodyPart = new javax.mail.internet.MimeBodyPart();
            ByteArrayOutputStream report = new ByteArrayOutputStream(Galaxy.FILEBLOCKSIZE);
            msg.writeTo(report);
            javax.mail.util.ByteArrayDataSource dataSource = new javax.mail.util.ByteArrayDataSource(report.toByteArray(), "message/rfc822");
            messageBodyPart.setDataHandler(new javax.activation.DataHandler(dataSource));
            messageBodyPart.setDisposition(javax.mail.Part.ATTACHMENT);
            multipart.addBodyPart(messageBodyPart);
            forward.setContent(multipart);
            javax.mail.Transport.send(forward);
        } catch (javax.mail.MessagingException err) {
            MailFactory.getLogger().log(Level.SEVERE, "Can't forward bad message to GameMaster", err);
        } catch (IOException err) {
            MailFactory.getLogger().log(Level.SEVERE, "Can't forward bad message to GameMaster", err);
        }
    }

    private static void replyMailError(javax.mail.internet.MimeMessage msg, String error) {
        try {
            javax.mail.internet.MimeMessage answer = MailFactory.replyMessage(msg, MailFactory.getSubject("illegal", OGSserver.getMessageParameters()));
            if (answer.getRecipients(javax.mail.Message.RecipientType.TO) == null) return;
            MailFactory.setText(answer, MailFactory.getMessage(error, OGSserver.getMessageParameters()));
            javax.mail.Transport.send(answer);
        } catch (javax.mail.MessagingException err) {
            MailFactory.getLogger().log(Level.SEVERE, "Can't send message.", err);
        }
    }

    private static boolean testMessage(javax.mail.internet.MimeMessage msg) throws javax.mail.MessagingException {
        if (msg.isExpunged()) return false;
        if (msg.isSet(javax.mail.Flags.Flag.DELETED)) return false;
        String messageID = msg.getMessageID();
        if (messageID == null) {
            ignoreMessage(msg, "Missing message ID");
            return false;
        }
        javax.mail.internet.InternetAddress[] from;
        try {
            from = (javax.mail.internet.InternetAddress[]) msg.getFrom();
        } catch (javax.mail.MessagingException err) {
            ignoreMessage(msg, "Illegal sender address: " + err);
            return false;
        }
        if (from == null) {
            ignoreMessage(msg, "Missing sender address");
            return false;
        }
        int maxsize = Integer.parseInt(getProperty("mail.maxsize", "0"));
        int size = msg.getSize();
        if (maxsize > 0 && (size > maxsize || size == -1)) {
            ignoreMessage(msg, "Message is too large (" + size + ')');
            return false;
        }
        String[] precendences = msg.getHeader("Precedence");
        if (precendences != null) for (String precendence : precendences) {
            precendence = precendence.toLowerCase();
            if ("bulk".equals(precendence) || "junk".equals(precendence) || "list".equals(precendence)) {
                ignoreMessage(msg, "Precedence: " + precendence);
                return false;
            }
        }
        Enumeration<?> enumer = msg.getAllHeaders();
        while (enumer.hasMoreElements()) {
            javax.mail.Header header = (javax.mail.Header) enumer.nextElement();
            String name = header.getName().toLowerCase();
            if (name.startsWith("list-") || name.startsWith("x-list") || name.startsWith("x-mirror") || name.startsWith("x-auto") || "x-mailing-list".equals(name) || "mailing-list".equals(name)) {
                ignoreMessage(msg, "Message from mailing list");
                return false;
            }
        }
        for (javax.mail.internet.InternetAddress aFrom : from) {
            BlackListItem item = getBlackList().findEmailsListMember(aFrom.getAddress());
            if (item != null) {
                ignoreMessage(msg, item.getReason());
                return false;
            }
        }
        return true;
    }

    private static void ignoreMessage(javax.mail.internet.MimeMessage msg, String reason) {
        StringBuilder buffer = new StringBuilder("Ignore message");
        try {
            String messageID = msg.getMessageID();
            buffer.append(' ').append(messageID);
        } catch (javax.mail.MessagingException ignored) {
        }
        try {
            javax.mail.Address[] from = msg.getFrom();
            if (from != null && from.length != 0) {
                buffer.append(" from ");
                for (int i = 0; i < from.length; i++) {
                    javax.mail.Address address = from[i];
                    if (i > 0) buffer.append(", ");
                    buffer.append(address.toString());
                }
            }
        } catch (javax.mail.MessagingException ignored) {
        }
        buffer.append(". ").append(reason);
        logger.finer(buffer.toString());
    }

    private static final BlackList bl = new BlackList();

    public static BlackList getBlackList() {
        return bl;
    }

    private static void loadBlackList() {
        try {
            BlackListIO.load(OGSserver.class.getResourceAsStream("/bl.xml"), bl);
        } catch (Exception err) {
            Galaxy.getLogger().log(Level.SEVERE, "Blacklist error", err);
        }
        String blFile = OGSserver.getProperty("Server.BL.path");
        if (blFile != null) try {
            if (!new File(blFile).exists()) Galaxy.getLogger().severe("Blacklist file " + blFile + " not found!"); else {
                BlackListIO.load(new FileInputStream(blFile), bl);
                if (bl.isDirty()) BlackListIO.save(blFile, bl);
            }
        } catch (Exception err) {
            Galaxy.getLogger().log(Level.SEVERE, "Blacklist error", err);
        }
    }

    private static void exec(javax.mail.internet.MimeMessage msg, Reader reader) {
        Session session = new Session();
        session.multicommand = noEnd;
        session.message = msg;
        session.reader = new BufferedReader(reader);
        if (msg != null) try {
            session.ip = MailFactory.getSendersIP(msg.getHeader("Received"));
        } catch (javax.mail.MessagingException ignored) {
        }
        boolean firstLine = true;
        while (session.reader != null) {
            String line;
            try {
                line = session.reader.readLine();
            } catch (IOException err) {
                MailFactory.getLogger().log(Level.SEVERE, "Reading order", err);
                replyMailError(session.message, "reading_error");
                return;
            }
            if (line == null) return;
            String[] cmdLine = Utils.parseCmd(line);
            if (cmdLine.length == 0) continue;
            ListIterator<String> cmd = Arrays.asList(cmdLine).listIterator();
            String commandName = cmd.next().toUpperCase();
            cmd.set(commandName);
            IServerCommand command = getCommand(commandName, session.isGM);
            if (command == null) {
                if (firstLine && msg != null) {
                    ignoreMessage(msg, "Illegal command: " + (line.length() < 30 ? line : line.substring(0, 30) + "..."));
                    return;
                }
                Map<String, Object> arguments = new HashMap<String, Object>();
                arguments.put("CommandLine", Utils.join(cmdLine));
                arguments.put("Command", commandName);
                Answer answer = session.newAnswer();
                answer.setSubject(MailFactory.getSubject("command_error", arguments, OGSserver.getMessageParameters()));
                answer.println("> " + Utils.join(cmdLine));
                answer.print("ERROR: Unknown command");
                answer.send(session);
                continue;
            }
            String[] text = null;
            if (commandName.charAt(0) == '#') {
                text = skipMessageBodyToEnd(session.reader);
                if (!session.multicommand) session.reader = null;
            }
            Galaxy.getLogger().fine("Executing command: " + Utils.join(cmdLine));
            Profiler.getProfiler().start("command." + commandName, (Object[]) cmdLine);
            try {
                Answer answer = session.newAnswer();
                if (command.exec(session, answer, cmd, text)) {
                    if (answer.subject == null) {
                        Map<String, Object> arguments = new HashMap<String, Object>();
                        arguments.put("DateTime", new Date());
                        cmdLine = new String[cmd.nextIndex()];
                        while (cmd.hasPrevious()) {
                            int i = cmd.previousIndex();
                            cmdLine[i] = cmd.previous();
                        }
                        arguments.put("CommandLine", Utils.join(cmdLine));
                        arguments.put("Command", commandName);
                        answer.setSubject(MailFactory.getSubject("command_translation", arguments, OGSserver.getMessageParameters()));
                    }
                    answer.send(session);
                }
            } catch (GalaxyException err) {
                Map<String, Object> arguments = new HashMap<String, Object>();
                arguments.put("CommandLine", Utils.join(cmdLine));
                arguments.put("Command", commandName);
                Answer answer = session.newAnswer();
                answer.setSubject(MailFactory.getSubject("command_error", arguments, OGSserver.getMessageParameters()));
                answer.println("> " + Utils.join(cmdLine));
                answer.print("ERROR: ");
                answer.println(err.getMessage());
                Galaxy.getLogger().log(Level.FINER, "Command error: " + err.getMessage(), err.err);
                answer.send(session);
            } finally {
                Profiler.getProfiler().stop("command." + commandName);
            }
            firstLine = false;
        }
    }

    public static IServerCommand getCommand(String commandName, boolean isGM) {
        IServerCommand command = null;
        if (isGM) command = adminCommands.get(commandName);
        if (command == null) command = userCommands.get(commandName);
        return command;
    }

    public static Map<String, IServerCommand> loadCommands(Properties props, boolean isGM) {
        Map<String, IServerCommand> commands = new HashMap<String, IServerCommand>();
        String[] availableCommands = Utils.split(props.getProperty(isGM ? "Server.AdminCommands" : "Server.UserCommands"));
        for (String key : availableCommands) {
            String className = props.getProperty("Server.Command." + key);
            try {
                IServerCommand command = (IServerCommand) Class.forName(className).newInstance();
                commands.put(key.toUpperCase(), command);
            } catch (Exception err) {
                Galaxy.getLogger().log(Level.SEVERE, "Can't load server command " + key + ": " + className, err);
            }
        }
        return commands;
    }

    public static void generateGame(Galaxy galaxy) {
        try {
            if (galaxy.getState() == Galaxy.State.RECRUITING) {
                galaxy.generateNewGame();
                galaxy.saveWithCopy();
                Galaxy.getLogger().info("Game " + galaxy.getName() + " started");
                galaxy.log("Game started");
                saveFullReport(galaxy);
                sendAllReports(galaxy);
                runActions(galaxy, "START");
            } else {
                galaxy.savePreGen();
                galaxy.generate();
                galaxy.saveWithCopy();
                Galaxy.getLogger().info("Game " + galaxy.getName() + " turn " + galaxy.getTurn() + " generated");
                galaxy.log("Turn {0} generated", galaxy.getTurn());
                saveFullReport(galaxy);
                execDeferredOrders(galaxy);
                sendAllReports(galaxy);
                runActions(galaxy, "TURN");
                if (galaxy.getState() == Galaxy.State.FINAL) finishGame(galaxy);
            }
        } catch (GalaxyException err) {
            Galaxy.getLogger().log(Level.SEVERE, "Failed game " + galaxy.getName() + " turn " + (galaxy.getTurn() + 1) + " generating", err);
        }
    }

    public static void finishTurn(Galaxy galaxy) {
        if (galaxy.isAllFinished()) {
            String name = galaxy.getName();
            int turn = galaxy.getTurn() + 1;
            Galaxy.getLogger().info("Generate " + name + " turn " + turn + " by finish");
            Schedule sch = Schedule.getSchedule(galaxy.getGameDir());
            sch.updateNextScheduleDate(turn + 1);
            sch.updateNextScheduleDate(turn + 1, sch.getDateGenerate());
            Schedule.commitSchedule(galaxy.getGameDir(), sch);
            generateGame(galaxy);
        }
    }

    public static void finishGame(Galaxy galaxy) {
        Schedule sch = Schedule.getSchedule(galaxy.getGameDir());
        Calendar calendar = Calendar.getInstance(Locale.ROOT);
        calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(galaxy.props.getProperty("Archive.Days", "7")));
        sch.setDateArchive(calendar.getTime());
        Schedule.commitSchedule(galaxy.getGameDir(), sch);
        Galaxy.getLogger().info("Game " + galaxy.getName() + " finished");
        galaxy.log("Game finished");
        runActions(galaxy, "FINISH");
    }

    private static void sendPreInvitation(Galaxy galaxy, Race race, Schedule sch) {
        try {
            Map<String, Object> arguments = new HashMap<String, Object>();
            arguments.put("Galaxy.Name", galaxy.getName());
            arguments.put("Race.Name", race.getName());
            arguments.put("StartDate", sch.getDateGenerate());
            javax.mail.internet.MimeMessage msg = MailFactory.newMessage(race.getAddress(GamerAddress.Mode.ANSWER), MailFactory.getSubject("preinvitation", arguments, OGSserver.getMessageParameters()));
            arguments.put("Races.Count", galaxy.getRaces().size());
            arguments.put("Race.Password", race.getPassword());
            arguments.put("Race.PasswordPrefix", race.getPasswordPrefix());
            arguments.put("mail.server.address", OGSserver.getProperty("mail.server.address"));
            arguments.put("mail.gm.address", OGSserver.getProperty("mail.gm.address"));
            arguments.put("Galaxy.Info", galaxy.getDescriptionString());
            arguments.put("Schedule", scheduleToString(sch));
            MailFactory.setText(msg, MailFactory.getMessage("preinvitation", arguments, OGSserver.getMessageParameters()));
            javax.mail.Transport.send(msg);
        } catch (javax.mail.MessagingException err) {
            MailFactory.getLogger().log(Level.WARNING, "Can't send game " + galaxy.getName() + " preinvitation message for " + race.getName(), err);
        }
    }

    public static void sendPreInvitations(Galaxy galaxy, Schedule sch) {
        for (Race race : galaxy.getRaces()) sendPreInvitation(galaxy, race, sch);
    }

    public static Properties getProperties() {
        return props;
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }

    public static String getProperty(String key, String value) {
        return props.getProperty(key, value);
    }

    public static String getServerName() {
        return OGSserver.getProperty("Server.Name");
    }

    public static String getServerVersion() {
        return OGSserver.getProperty("Server.Version");
    }

    public static String getPersonal() {
        return OGSserver.getProperty("mail.server.personal");
    }

    public static String getLanguage() {
        return OGSserver.getProperty("Server.Language");
    }

    public static boolean isSaveReports() {
        return Utils.parseBoolean(getProperty("Save.Reports", "yes"));
    }

    private static Map<String, String> messageParameters;

    public static synchronized Map<String, String> getMessageParameters() {
        if (messageParameters == null) {
            Map<String, String> parameters = new HashMap<String, String>();
            for (String key : new String[] { "Server.Name", "Server.Abbr", "mail.server.signature" }) parameters.put(key, OGSserver.getProperty(key));
            messageParameters = Collections.unmodifiableMap(parameters);
        }
        return messageParameters;
    }

    public static String[] getGameNames() {
        return new File(Galaxy.GAMES_DIR).list();
    }

    public static String[] getArchivedGameNames() {
        return new File(Galaxy.ARCHIVE_DIR).list();
    }

    private static void sendWeeklyMessage() {
        if (!Utils.parseBoolean(OGSserver.getProperty("mail.weekly.use", "no"))) return;
        if (!new SimpleDateFormat("EEEE HH:mm", Locale.ROOT).format(new Date()).equalsIgnoreCase(OGSserver.getProperty("mail.weekly.time"))) return;
        List<Map<String, Object>> gamesList = new ArrayList<Map<String, Object>>();
        for (String gameName : getGameNames()) {
            Galaxy galaxy = Galaxy.loadGame(gameName);
            if (galaxy == null || galaxy.getState() != Galaxy.State.RECRUITING) continue;
            Map<String, Object> gameData = new HashMap<String, Object>();
            gameData.put("Galaxy.Name", galaxy.getName());
            gameData.put("Galaxy.Info", galaxy.getDescriptionString());
            gameData.put("Races.Count", galaxy.getRaces().size());
            List<Race> sortedRaces = new ArrayList<Race>(galaxy.getRaces());
            Collections.sort(sortedRaces);
            List<Map<String, Object>> racesList = new ArrayList<Map<String, Object>>();
            for (Race race : sortedRaces) {
                Map<String, Object> raceData = new HashMap<String, Object>();
                raceData.put("Race.Name", race.getName());
                racesList.add(raceData);
            }
            gameData.put("Races", racesList);
            gamesList.add(gameData);
        }
        if (gamesList.isEmpty()) return;
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("Games.Count", gamesList.size());
        String subject = MailFactory.getSubject("weekly", arguments, OGSserver.getMessageParameters());
        arguments.put("Games", gamesList);
        arguments.put("mail.server.url", OGSserver.getProperty("mail.server.url"));
        arguments.put("mail.doc.url", OGSserver.getProperty("mail.doc.url"));
        arguments.put("mail.server.address", OGSserver.getProperty("mail.server.address"));
        String text = MailFactory.getMessage("weekly", arguments, OGSserver.getMessageParameters());
        MailFactory.sendMessage(OGSserver.getProperty("mail.weekly.address"), subject, text);
    }

    public static void writeZIPReport(Galaxy galaxy, Race race, String flags, String reportName, Charset charset, OutputStream os) {
        try {
            ZipOutputStream zos = new ZipOutputStream(os);
            zos.putNextEntry(new ZipEntry(reportName + ".rep"));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zos, charset), Galaxy.FILEBLOCKSIZE);
            writeReport(galaxy, new PrintWriter(writer), race, flags);
            zos.close();
            os.flush();
        } catch (IOException err) {
            Galaxy.getLogger().log(Level.SEVERE, "Can't create game " + galaxy.getName() + " turn " + galaxy.getTurn() + (race != null ? " report for " + race.getName() : " full report"), err);
        } finally {
            System.gc();
        }
    }

    private static void execDeferredOrders(Galaxy galaxy) {
        if (galaxy.getState() != Galaxy.State.GAME) return;
        List<Race> races = new ArrayList<Race>();
        for (Race race : galaxy.getRaces()) {
            if (!race.isBanned()) {
                File orderFile = galaxy.getOrderFile(race, galaxy.getTurn());
                if (orderFile.exists()) races.add(race);
            }
        }
        if (races.isEmpty()) return;
        Collections.shuffle(races);
        for (Race race : races) {
            Galaxy.getLogger().fine("Executing deferred order for race " + race.getName());
            try {
                File orderFile = galaxy.getOrderFile(race, galaxy.getTurn());
                String[] order = skipMessageBodyToEnd(new BufferedReader(new InputStreamReader(new FileInputStream(orderFile), "UTF-8")));
                if (isSendMail) {
                    StringWriter buffer = new StringWriter();
                    PrintWriter out = new PrintWriter(buffer);
                    Set<javax.mail.internet.InternetAddress> addresses = new HashSet<javax.mail.internet.InternetAddress>(Arrays.asList(race.getAddress(GamerAddress.Mode.ANSWER)));
                    int errors = galaxy.execOrder(race, order, out);
                    out.println();
                    addresses.addAll(Arrays.asList(race.getAddress(GamerAddress.Mode.ANSWER)));
                    if (addresses.isEmpty()) continue;
                    try {
                        Map<String, Object> arguments = new HashMap<String, Object>();
                        arguments.put("Galaxy.Name", galaxy.getName());
                        arguments.put("Galaxy.Turn", galaxy.getTurn());
                        arguments.put("Race.Name", race.getName());
                        String subject;
                        if (errors == 0) subject = MailFactory.getSubject("deferred_order_translation", arguments, OGSserver.getMessageParameters()); else {
                            arguments.put("Errors", errors);
                            subject = MailFactory.getSubject("deferred_order_translation_failed", arguments, OGSserver.getMessageParameters());
                        }
                        javax.mail.internet.MimeMessage answer = MailFactory.newMessage(addresses.toArray(new javax.mail.internet.InternetAddress[addresses.size()]), subject);
                        MailFactory.setText(answer, buffer.toString());
                        javax.mail.Transport.send(answer);
                    } catch (javax.mail.MessagingException err) {
                        MailFactory.getLogger().log(Level.SEVERE, "Can't send deferred order translation listing. Game: " + galaxy.getName() + " Race: " + race.getName(), err);
                    }
                } else {
                    galaxy.execOrder(race, order, new PrintWriter(System.out));
                    System.out.println();
                }
                race.log("Turn {0} orders executed", galaxy.getTurn());
                finishTurn(galaxy);
            } catch (IOException err) {
                Galaxy.getLogger().log(Level.SEVERE, "Can't load deferred order. Game: " + galaxy.getName() + " Turn: " + galaxy.getTurn() + " Race: " + race.getName(), err);
            }
        }
        galaxy.save();
    }

    public static String formatHelp(boolean isFullHelp, String topicName, Class<?> aClass) {
        File file = Utils.joinPath("share", "help", "opengs", aClass.getName().replace('.', File.separatorChar) + ".txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            if (isFullHelp) {
                StringBuilder result = new StringBuilder();
                result.append(topicName);
                result.append(": ");
                result.append(reader.readLine());
                StringBuilder template = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    template.append('\n');
                    template.append(line);
                }
                Map<String, Object> arguments = new HashMap<String, Object>();
                arguments.put("Command", topicName);
                result.append(MapFormat.format(template.toString(), arguments, OGSserver.getMessageParameters()));
                return result.toString();
            } else return reader.readLine();
        } catch (IOException err) {
            Galaxy.getLogger().log(Level.WARNING, "Can't read help for command " + aClass.getName() + ": exception ", err);
            return "No help for " + topicName;
        }
    }

    private static final Map<String, MapFormat> formats = new HashMap<String, MapFormat>();

    public static MapFormat getFormat(String key) {
        MapFormat format = formats.get(key);
        if (format == null) {
            format = new MapFormat(OGSserver.getProperty(key));
            formats.put(key, format);
        }
        return format;
    }

    public static MapFormat getFormat2(String key) {
        MapFormat format = formats.get(key);
        if (format == null) {
            try {
                File file = Utils.joinPath("share", "messages", OGSserver.getProperty(key));
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                StringBuilder buf = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) buf.append(line).append('\n');
                reader.close();
                format = new MapFormat(buf.toString());
            } catch (IOException err) {
                Galaxy.getLogger().log(Level.SEVERE, "Error while loading message " + key, err);
                format = new MapFormat("");
            }
            formats.put(key, format);
        }
        return format;
    }

    public static void runAction(String key, Galaxy galaxy) {
        IAction action = null;
        try {
            action = (IAction) Class.forName(getProperty("Action." + key)).newInstance();
        } catch (Exception err) {
            Galaxy.getLogger().log(Level.SEVERE, "Can't initialize action " + key, err);
        }
        if (action != null) {
            Galaxy.getLogger().fine("Run " + key + " on " + galaxy.getName());
            action.exec(key, galaxy);
        }
    }

    public static void runActions(Galaxy galaxy, String type) {
        String[] actionNames = Utils.split(galaxy.props.getProperty("Actions." + type, ""));
        for (String key : actionNames) runAction(key, galaxy);
    }

    public static String[] skipMessageBodyToEnd(BufferedReader in) {
        List<String> result = new ArrayList<String>();
        try {
            String line;
            while ((line = in.readLine()) != null && !(line.length() >= 4 && "#end".equalsIgnoreCase(line.substring(0, 4)))) result.add(line);
        } catch (IOException err) {
            MailFactory.getLogger().log(Level.SEVERE, "Can't read message", err);
        }
        return result.toArray(new String[result.size()]);
    }

    public static void sendAllReports(Galaxy galaxy) {
        boolean saveReports = isSaveReports();
        if (!isSendMail && !saveReports) return;
        Galaxy.getLogger().fine("Sending reports");
        for (Race race : galaxy.getRaces()) {
            String reportName = race.getReportName(false);
            ByteArrayOutputStream os = new ByteArrayOutputStream(Galaxy.FILEBLOCKSIZE);
            writeZIPReport(galaxy, race, race.getRepParams(), reportName, race.getEncoding(), os);
            if (saveReports) {
                try {
                    File file = Utils.joinPath(race.getPlayerDir(), "reports", reportName + ".rep.zip");
                    file.getParentFile().mkdirs();
                    os.writeTo(new FileOutputStream(file));
                } catch (IOException err) {
                    Galaxy.getLogger().log(Level.SEVERE, "Can't save game " + galaxy.getName() + " turn " + galaxy.getTurn() + " report for " + race.getName(), err);
                }
            }
            if (isSendMail) {
                Schedule sch = Schedule.getSchedule(galaxy.getGameDir());
                try {
                    javax.mail.internet.InternetAddress[] address = race.getAddress(GamerAddress.Mode.REPORT);
                    if (address.length == 0) continue;
                    Map<String, Object> arguments = new HashMap<String, Object>();
                    arguments.put("Galaxy.Name", galaxy.getName());
                    arguments.put("Galaxy.Turn", galaxy.getTurn());
                    arguments.put("Race.Name", race.getName());
                    javax.mail.internet.MimeMessage msg = MailFactory.newMessage(address, MailFactory.getSubject("report", arguments, OGSserver.getMessageParameters()));
                    arguments.put("NextGenerationDate", sch.getDateGenerate());
                    arguments.put("mail.gm.address", getProperty("mail.gm.address"));
                    StringWriter broadcasts = new StringWriter();
                    writeBroadcastMessage(galaxy, broadcasts, race);
                    arguments.put("Broadcasts", broadcasts.toString());
                    javax.mail.internet.MimeBodyPart text = new javax.mail.internet.MimeBodyPart();
                    text.setText(MailFactory.getMessage("report", arguments, OGSserver.getMessageParameters()));
                    javax.mail.internet.MimeBodyPart attachment = new javax.mail.internet.MimeBodyPart();
                    attachment.setDataHandler(new javax.activation.DataHandler(new javax.mail.util.ByteArrayDataSource(os.toByteArray(), "application/x-zip")));
                    attachment.setDisposition(javax.mail.Part.ATTACHMENT);
                    try {
                        attachment.setFileName(javax.mail.internet.MimeUtility.encodeText(reportName + ".rep.zip"));
                    } catch (UnsupportedEncodingException err) {
                        attachment.setFileName(reportName + ".rep.zip");
                    }
                    javax.mail.Multipart mp = new javax.mail.internet.MimeMultipart();
                    mp.addBodyPart(text);
                    mp.addBodyPart(attachment);
                    msg.setContent(mp);
                    javax.mail.Transport.send(msg);
                } catch (javax.mail.MessagingException err) {
                    Galaxy.getLogger().log(Level.SEVERE, "Can't send game " + galaxy.getName() + " turn " + galaxy.getTurn() + " report for " + race.getName(), err);
                }
            }
            System.gc();
        }
    }

    public static void writeBroadcastMessage(Galaxy galaxy, Writer writer, Race recipient) {
        if (galaxy.getState() == Galaxy.State.FINAL || galaxy.getState() == Galaxy.State.ARCHIVE) {
            PrintWriter pw = new PrintWriter(writer);
            pw.println();
            pw.println("Broadcast Message");
            pw.println();
            pw.println("\t<<<           PLEASE ATTENTION!         >>>");
            pw.println("\t<<<      AFTER " + new DecimalFormat("###").format(galaxy.getTurn()) + " INCREDIBLE YEARS     >>>");
            pw.println();
            pw.println("\t<<<           THE GAME IS OVER!         >>>");
            pw.println();
            if (galaxy.getWinners().size() == 1) pw.println("\tWinner of \"" + galaxy.getName() + "\" game is " + galaxy.getWinners().iterator().next().getName() + '!');
            if (galaxy.getWinners().size() > 1) {
                pw.println("\tWinners of \"" + galaxy.getName() + "\" game are:");
                pw.println();
                for (Race winner : galaxy.getWinners()) pw.println("\t\t" + winner.getName());
            }
            if (recipient != null && galaxy.getWinners().contains(recipient)) {
                pw.println();
                pw.println("\t<<< Congratulations! You WON this game! >>>");
                pw.println("\t<<<     Your name will live forever     >>>");
                pw.println(String.format("\t<<<      in annals of %-18s>>>", getServerName() + '!'));
            }
            pw.println();
            pw.println();
            pw.println("\t<<<       WELCOME TO FUTURE GAME!       >>>");
            pw.println();
            pw.println();
            pw.flush();
            return;
        }
        if (galaxy.getMessages().isEmpty() && (recipient == null || recipient.getMessages().isEmpty())) return;
        PrintWriter pw = new PrintWriter(writer);
        pw.println();
        pw.println("\t\tBroadcast Message");
        if (!galaxy.getMessages().isEmpty()) {
            pw.println();
            for (String line : galaxy.getMessages()) {
                if (line.isEmpty()) pw.println(); else pw.println('\t' + line);
            }
        }
        if (recipient != null && !recipient.getMessages().isEmpty()) {
            pw.println();
            for (String line : recipient.getMessages()) {
                if (line.isEmpty()) pw.println(); else pw.println('\t' + line);
            }
        }
        pw.flush();
    }

    public static void saveFullReport(Galaxy galaxy) {
        if (isSaveReports()) {
            Galaxy.getLogger().fine("Saving full report");
            String reportName = galaxy.getReportName();
            try {
                writeZIPReport(galaxy, null, galaxy.fullReportParameters, reportName, Charset.forName(getProperty("FullReport.Encoding")), new FileOutputStream(Utils.joinPath(galaxy.getGameDir(), "reports", reportName + ".rep.zip")));
            } catch (IOException err) {
                Galaxy.getLogger().log(Level.SEVERE, "Can't save full report. Game: " + galaxy.getName(), err);
            }
            System.gc();
        }
    }

    public static String scheduleToString(Schedule sch) {
        String[] names = new DateFormatSymbols().getWeekdays();
        DecimalFormat format = new DecimalFormat("00");
        StringBuilder result = new StringBuilder();
        int[] limits = sch.getScheduleLimits();
        for (int i = 0; i < limits.length; ++i) {
            int from = limits[i];
            Collection<ScheduleItem> scheduleItems = sch.getScheduleItemsFrom(from);
            result.append("From ").append(from);
            if (i + 1 < limits.length) result.append(" to ").append(limits[i + 1] - 1).append(" turns:\n"); else result.append(" turn to the end of game:\n");
            for (ScheduleItem item : scheduleItems) {
                String weekDay = names[item.getDay()];
                weekDay = weekDay.substring(0, 1).toUpperCase() + weekDay.substring(1);
                result.append(weekDay).append(' ').append(format.format((long) item.getHour())).append(':').append(format.format((long) item.getMinute())).append('\n');
            }
            result.append('\n');
        }
        return result.toString();
    }

    private static void writeReport(Galaxy galaxy, PrintWriter out, Race race, String flags) throws IOException {
        Galaxy.getLogger().log(Level.FINER, "Generating {2} report for game {0} turn {1}", new Object[] { galaxy.getName(), galaxy.getTurn(), race == null ? "GameMaster" : race });
        String gamename = galaxy.getName();
        int turn = galaxy.getTurn();
        try {
            Profiler.getProfiler().start("report", gamename, turn, race != null ? race.getName() : "-");
            if (race != null) out.println(race.getName() + " Report for Galaxy PLUS " + gamename + " Turn " + turn + ' ' + new Date().toString()); else out.println("Galaxy PLUS " + gamename + " FULL Report Turn " + turn + ' ' + galaxy.saveDate.toString());
            out.println("Galaxy PLUS version 1.7 - OGS v." + getServerVersion() + " family " + getServerName() + " server");
            out.println();
            out.println("Size: " + (int) galaxy.getGeometry().getSize() + "  Planets: " + galaxy.getPlanets().length + "  Players: " + galaxy.getAllRaces().size());
            for (int i = 0; i < flags.length(); ++i) {
                String key = flags.substring(i, i + 1);
                try {
                    Profiler.getProfiler().start("report." + key, gamename, turn, race != null ? race.getName() : "-");
                    IParameter parameter = galaxy.reportParameters.get(key);
                    if (parameter != null) {
                        Galaxy.getLogger().finest("Report section " + key + ": " + parameter.getClass().getName());
                        parameter.doIt(out, galaxy, race);
                    } else Galaxy.getLogger().severe("Game " + gamename + ": Unknown report parameter - " + key);
                } finally {
                    Profiler.getProfiler().stop("report." + key);
                }
            }
        } finally {
            out.flush();
            Profiler.getProfiler().stop("report");
        }
    }

    private static boolean canAutoGenerate(Galaxy galaxy) {
        if (galaxy.hasTeams()) {
            if (galaxy.teamSize == 0) return false;
            int fullTeams = 0;
            for (Team team : galaxy.getTeams()) {
                int racesCount = 0;
                for (Race race : team.getRaces()) if (race.isConfirmed()) ++racesCount;
                if (racesCount >= galaxy.teamSize) ++fullTeams;
            }
            return fullTeams * galaxy.teamSize >= galaxy.racesCount;
        } else {
            if (galaxy.racesCount == 0) return false;
            int racesCount = 0;
            for (Race race : galaxy.getRaces()) if (race.isConfirmed()) ++racesCount;
            return racesCount >= galaxy.racesCount;
        }
    }

    public static void acceptJoin(Galaxy galaxy, Race race) {
        race.log("Accepted for game");
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("Galaxy.Name", galaxy.getName());
        arguments.put("Races.Count", galaxy.getRaces().size());
        arguments.put("Race.Name", race.getName());
        String subject = MailFactory.getSubject("join_notification", arguments, OGSserver.getMessageParameters());
        arguments.put("Race.Password", race.getPassword());
        arguments.put("Race.PasswordPrefix", race.getPasswordPrefix());
        MailFactory.sendMessage(race.getAddress(GamerAddress.Mode.ANSWER), subject, MailFactory.getMessage("join_notification", arguments, OGSserver.getMessageParameters()));
        if ("auto".equalsIgnoreCase(galaxy.props.getProperty("Galaxy.StopJoin"))) {
            if (canAutoGenerate(galaxy)) {
                Schedule sch = Schedule.getSchedule(galaxy.getGameDir());
                Calendar calendar = Calendar.getInstance(Locale.ROOT);
                calendar.add(Calendar.DAY_OF_WEEK, galaxy.firstTurnPause);
                sch.updateNextScheduleDate(1, calendar.getTime());
                if (sch.getDateGenerate() == null) MailFactory.informGameMind("Game " + galaxy.getName() + " recruiting is completed but schedule for turn 0 is not defined");
                Schedule.commitSchedule(galaxy.getGameDir(), sch);
                Galaxy.getLogger().info("Generate " + galaxy.getName() + " turn " + (galaxy.getTurn() + 1) + " by autostart");
                generateGame(galaxy);
            }
        } else if ("flexible".equalsIgnoreCase(galaxy.props.getProperty("Galaxy.StopJoin"))) {
            Schedule sch = Schedule.getSchedule(galaxy.getGameDir());
            if (sch.getDateGenerate() != null) sendPreInvitation(galaxy, race, sch); else if (canAutoGenerate(galaxy)) {
                Calendar calendar = Calendar.getInstance(Locale.ROOT);
                calendar.add(Calendar.DAY_OF_WEEK, galaxy.firstTurnPause);
                sch.updateNextScheduleDate(0, calendar.getTime());
                if (sch.getDateGenerate() == null) MailFactory.informGameMind("Game " + galaxy.getName() + " recruiting is completed but schedule for turn 0 is not defined"); else {
                    Schedule.commitSchedule(galaxy.getGameDir(), sch);
                    sendPreInvitations(galaxy, sch);
                    MailFactory.informGameMind("Game " + galaxy.getName() + " will started at " + sch.getDateGenerate());
                }
            }
        } else {
            Schedule sch = Schedule.getSchedule(galaxy.getGameDir());
            if (sch.getDateGenerate() != null) sendPreInvitation(galaxy, race, sch);
        }
    }

    public static void playerLog(Session session, Race race, String pattern, Object... arguments) {
        try {
            InetAddress ip = null;
            javax.mail.internet.InternetAddress[] from = null;
            if (session != null) {
                ip = session.ip;
                from = session.getFrom();
            }
            StringBuilder builder = new StringBuilder();
            builder.append(race.getGalaxy().getName()).append('\t');
            builder.append(race.getName()).append('\t');
            builder.append(ip != null ? ip.getHostAddress() : "-").append('\t');
            builder.append(from != null && from.length != 0 ? from[0].getAddress() : "-").append('\t');
            builder.append(MessageFormat.format(pattern, arguments));
            Utils.appendLogLine(new File("secret.log"), builder.toString());
        } catch (IOException err) {
            Galaxy.getLogger().log(Level.SEVERE, "Can't write server log", err);
        }
    }
}
