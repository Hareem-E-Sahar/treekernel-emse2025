package egovframework.com.utl.sys.htm.service;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import javax.annotation.Resource;

/**
 * @Class Name : HttpMntrngChecker.java
 * @Description : HTTP서비스모니터링을 위한 Check 클래스
 * @Modification Information
 *
 *    수정일          수정자         수정내용
 *    -------        -------     -------------------
 *    2010.09.06     박종선         최초생성
 *
 * @author  박종선
 * @since 2010.05.01
 * @version
 * @see
 *
 */
public class HttpMntrngChecker {

    @Resource(name = "HttpMonService")
    private EgovHttpMonService httpMonService;

    static final char FILE_SEPARATOR = File.separatorChar;

    static final int MAX_STR_LEN = 1024;

    /**
	 * 시스템에 존재하는 서버의 실행상태 정보를 조회하는 기능
	 * @param String sitUrl 사용포트
	 * @return String status 실행상태
	 * @exception Exception
	*/
    public static String getPrductStatus(String siteUrl) throws Exception {
        String httpSttusCd = null;
        BufferedReader buf = null;
        try {
            URL url = new URL(siteUrl);
            url.openStream();
            httpSttusCd = "01";
        } catch (Exception e) {
            httpSttusCd = "02";
        }
        return httpSttusCd;
    }
}
