package mkk.princess.infrastructure.find;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Find email address from text.
 * <p/>
 * Use Regex implement.
 *
 * @author Shengzhao Li
 */
public class RegexEmailFinder extends AbstractEmailFinder {

    private static final Logger log = Logger.getLogger(RegexEmailFinder.class);

    /**
     * Email regex.   default
     * sales@4x4-utv-accessories.com
     */
    public static final String DEFAULT_EMAIL_REGEX = "[a-zA-Z0-9_]+[\\\\Wa-zA-Z0-9_]*@[a-zA-Z0-9_-]+.[a-zA-Z]{2,5}?(.[a-z]{2,5})?";

    /**
     * Mailto email regex. default
     * mailto:sales@4x4-utv-accessories.com
     */
    public static final String DEFAULT_EMAIL_TO_REGEX = "mailto:[a-zA-Z0-9_]+[\\\\Wa-zA-Z0-9_]*@[a-zA-Z0-9_-]+.[a-zA-Z]{2,5}?(.[a-z]{2,5})?";

    /**
     * Pattern flags
     */
    private static final int PATTERN_FLAGS = Pattern.DOTALL | Pattern.CASE_INSENSITIVE;

    /**
     * The text which need find email
     */
    protected String text;

    /**
     * If it is true, the email like: mailto:abd@ad.com  ,
     * otherwise, normal email regex.
     */
    protected boolean mailTo;

    /**
     * The matcher regex
     */
    protected String regex;

    public RegexEmailFinder() {
    }

    public RegexEmailFinder(String text) {
        this.text = text;
        log.info("Create by [" + this.text + "]");
    }

    public RegexEmailFinder(String text, boolean mailTo) {
        this(text);
        this.mailTo = mailTo;
        log.info("Create by [" + this.text + "," + this.mailTo + "]");
    }

    /**
     * Execute find email action.
     *
     * @return Email list
     */
    public List<String> find() {
        if (this.text == null || !this.text.contains("@")) {
            return Collections.emptyList();
        }
        initialization();
        List<String> emailList = new ArrayList<String>();
        if (this.mailTo) {
            findByMailTo(emailList);
        } else {
            findByMail(emailList);
        }
        log.info("Total find [" + emailList.size() + "] email addresses from [" + this.text + "]");
        return emailList;
    }

    private void initialization() {
        this.text = this.text.trim();
        if (this.regex == null) {
            log.info("Use default regex.");
            if (this.mailTo) {
                this.regex = DEFAULT_EMAIL_TO_REGEX;
            } else {
                this.regex = DEFAULT_EMAIL_REGEX;
            }
        }
    }

    private void findByMail(List<String> emailList) {
        log.info("Find by mail start ...");
        Pattern pattern = Pattern.compile(this.regex, PATTERN_FLAGS);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String mail = text.substring(start, end + 1);
            if (log.isDebugEnabled()) {
                log.debug("Find [" + mail + "]");
            }
            emailList.add(mail);
        }
    }

    private void findByMailTo(List<String> emailList) {
        log.info("Find by mailTo start ...");
        Pattern pattern = Pattern.compile(this.regex, PATTERN_FLAGS);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String mail = text.substring(start + 7, end + 1);
            if (log.isDebugEnabled()) {
                log.debug("Find [" + mail + "]");
            }
            emailList.add(mail);
        }
    }

    public void setMailTo(boolean mailTo) {
        this.mailTo = mailTo;
    }

    public boolean isMailTo() {
        return mailTo;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
