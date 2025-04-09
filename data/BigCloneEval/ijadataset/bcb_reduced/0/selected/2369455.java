package com.kgt.struts.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.struts.util.MessageResourcesFactory;
import org.apache.struts.util.PropertyMessageResources;

/**
 * Extends <code>PropertyMessageResources</code> class of Apache Struts for
 * Korean messages.
 * <p>
 * parameter를 받는 메세지를 처리할 때 조사를 알맞게 선택해서 붙여준다.
 * 
 * <p>
 * 사용법:
 * <p>
 * 'ko' resource file에 다음과 같은 형식으로 한글 메시지를 등록한다.
 * 조사 두개를 '/'로 구분한 후 괄호로 묶어 준다.
 * <p>
 * <blockquote>
 * 
 * <pre>
 * 	required={0}(이/가) 필요합니다.
 * 	required.both={0}(과/와) {1}(은/는) 필수적입니다.
 * </pre>
 * 
 * </blockquote>
 * 
 * struts 설정 file(struts-config.xml)에 다음과 같은 설정을 추가 한다.
 * <p>
 * <blockquote>
 * 
 * <pre>
 * 	&lt;message-resources factory=&quot;com.kgt.struts.util.KoreanMessageResourcesFactory&quot; 
 * 		parameter=&quot;com.kgt.struts.ApplicationResources&quot; /&gt;
 * </pre>
 * 
 * </blockquote>
 * <p>
 * 주의:
 * <p>
 * 처음 조사가 바로 앞 음절에 종성이 없을 때 표시될 조사이어야만 한다.
 * 예를 들자면 다음과 같다.
 * <p>
 * <blockquote>
 * 
 * <pre>
 *  	(은/는), (이/가), (과/와)
 * </pre>
 * 
 * </blockquote>
 * 
 * @author M.W.Park (manywayPark at gmail.com)
 * @see KoreanMessageResourcesFactory
 */
public class KoreanPropertyMessageResources extends PropertyMessageResources {

    public static final char OPEN = '(';

    public static final char CLOSE = ')';

    public static final char OR = '/';

    public KoreanPropertyMessageResources(MessageResourcesFactory factory, String config) {
        super(factory, config);
    }

    public KoreanPropertyMessageResources(KoreanMessageResourcesFactory factory, String config, boolean returnNull) {
        super(factory, config, returnNull);
    }

    @Override
    public String getMessage(Locale locale, String key, Object[] args) {
        String msg = super.getMessage(locale, key, args);
        if (Locale.KOREA.equals(locale) || Locale.KOREAN.equals(locale)) {
            StringBuffer sb = new StringBuffer();
            Pattern p = Pattern.compile("\\" + OPEN + "." + OR + "." + "\\" + CLOSE);
            Matcher m = p.matcher(msg);
            int prv = 0;
            while (m.find()) {
                sb.append(msg.substring(prv, m.start()));
                sb.append(getPostWord(msg.charAt(m.start() - 1), m.group()));
                prv = m.end();
            }
            sb.append(msg.substring(prv));
            msg = sb.toString();
        }
        return msg;
    }

    private Object getPostWord(char lastCh, String g) {
        return hasLastConsonant(lastCh) ? g.charAt(1) : g.charAt(3);
    }

    private boolean hasLastConsonant(char c) {
        return (((c - 44032) % (21 * 28)) % 28) != 0;
    }
}
