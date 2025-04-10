package com.whitebearsolutions.imagine.wbsagnitio.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import com.whitebearsolutions.directory.Entry;
import com.whitebearsolutions.imagine.wbsagnitio.NetworkManager;
import com.whitebearsolutions.imagine.wbsagnitio.ServiceManager;
import com.whitebearsolutions.imagine.wbsagnitio.directory.BranchManager;
import com.whitebearsolutions.imagine.wbsagnitio.directory.ContextManager;
import com.whitebearsolutions.imagine.wbsagnitio.directory.HierarchyManager;
import com.whitebearsolutions.imagine.wbsagnitio.directory.SchemaManager;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class HAConfiguration extends WBSAgnitioConfiguration {

    public static String getStatus() throws Exception {
        File _f = new File(FILE_CONFIGURATION);
        if (!_f.exists()) {
            return "standalone";
        }
        Configuration _c = new Configuration(_f);
        if (_c.getProperty("directory.status") == null || (!"master".equals(_c.getProperty("directory.status")) && !"slave".equals(_c.getProperty("directory.status")))) {
            return "standalone";
        } else if ("master".equals(_c.getProperty("directory.status"))) {
            return "master";
        } else if ("slave".equals(_c.getProperty("directory.status"))) {
            return "slave";
        }
        return "unknown";
    }

    public static void update() throws Exception {
        File _f = new File(FILE_CONFIGURATION);
        if (!_f.exists()) {
            throw new Exception("configuration not found");
        }
        Configuration _c = new Configuration(_f);
        StringBuilder _sb = new StringBuilder();
        _sb.append("Port 22\n");
        _sb.append("Protocol 2\n");
        _sb.append("HostKey /etc/ssh/ssh_host_rsa_key\n");
        _sb.append("UsePrivilegeSeparation yes\n");
        _sb.append("KeyRegenerationInterval 3600\n");
        _sb.append("ServerKeyBits 768\n");
        _sb.append("SyslogFacility AUTH\n");
        _sb.append("LogLevel INFO\n");
        _sb.append("LoginGraceTime 120\n");
        _sb.append("PermitRootLogin yes\n");
        _sb.append("StrictModes yes\n");
        _sb.append("RSAAuthentication yes\n");
        _sb.append("PubkeyAuthentication yes\n");
        _sb.append("IgnoreRhosts yes\n");
        _sb.append("RhostsRSAAuthentication no\n");
        _sb.append("HostbasedAuthentication no\n");
        _sb.append("PermitEmptyPasswords no\n");
        _sb.append("ChallengeResponseAuthentication no\n");
        if (_c.checkProperty("system.console", "true")) {
            _sb.append("PasswordAuthentication yes\n");
        } else {
            _sb.append("PasswordAuthentication no\n");
        }
        _sb.append("X11Forwarding no\n");
        _sb.append("X11DisplayOffset 10\n");
        _sb.append("PrintMotd no\n");
        _sb.append("PrintLastLog no\n");
        _sb.append("TCPKeepAlive yes\n");
        _sb.append("AcceptEnv LANG LC_*\n");
        _sb.append("Subsystem sftp /usr/lib/openssh/sftp-server\n");
        _sb.append("UsePAM yes");
        FileOutputStream _fos = new FileOutputStream(FILE_CONFIGURATION_SSHD);
        _fos.write(_sb.toString().getBytes());
        _fos.close();
        ServiceManager.restart(ServiceManager.SSH);
        String _status = getStatus();
        if ("standalone".equals(_status)) {
            setStandalone();
        } else if ("master".equals(_status)) {
            if (_c.getProperty("directory.virtual") == null) {
                throw new Exception("parameter [directory.virtual] not found");
            }
            if (_c.getProperty("directory.remote") == null) {
                throw new Exception("parameter [directory.remote] not found");
            }
            setMaster(_c.getProperty("directory.virtual"), _c.getProperty("directory.remote"));
        } else if ("slave".equals(_status)) {
            if (_c.getProperty("directory.virtual") == null) {
                throw new Exception("no se puede encontrar el par&acute;metro [directory.virtual]");
            }
            if (_c.getProperty("directory.remote") == null) {
                throw new Exception("parameter [directory.remote] not found");
            }
            setSlave(_c.getProperty("directory.virtual"), _c.getProperty("directory.remote"));
        }
    }

    public static void setStandalone() throws Exception {
        File _f = new File(FILE_CONFIGURATION);
        if (!_f.exists()) {
            throw new Exception("configuration not found");
        }
        Configuration _c = new Configuration(_f);
        SystemConfiguration _sc = new SystemConfiguration(_c);
        NetworkManager _nm = new NetworkManager(_c);
        ContextManager _cm = new ContextManager(_c);
        BranchManager _bm = new BranchManager(_c);
        HierarchyManager _hm = new HierarchyManager(_c);
        SchemaManager _sm = new SchemaManager();
        if (new File("/etc/ha.d/ha.cf").exists()) {
            ServiceManager.remove(ServiceManager.CLUSTER);
            _nm.setStandaloneNetwork();
            _f = new File("/etc/ha.d/nodeinfo");
            if (_f.exists()) {
                _f.delete();
            }
            _f = new File("/etc/ha.d/ha.cf");
            if (_f.exists()) {
                _f.delete();
            }
            _f = new File("/etc/ha.d/haresources");
            if (_f.exists()) {
                _f.delete();
            }
            _f = new File("/etc/ha.d/ldirectord.cf");
            if (_f.exists()) {
                _f.delete();
            }
        }
        if (!ServiceManager.isRunning(ServiceManager.LDAP)) {
            ServiceManager.start(ServiceManager.LDAP);
            Thread.sleep(500);
        }
        StringBuilder _sb = new StringBuilder();
        _sb.append("include         /etc/ldap/schema/core.schema\n");
        _sb.append("include         /etc/ldap/schema/cosine.schema\n");
        _sb.append("include         /etc/ldap/schema/nis.schema\n");
        _sb.append("include         /etc/ldap/schema/inetorgperson.schema\n");
        _sb.append("include         /etc/ldap/schema/samba.schema\n");
        _sb.append("include         /etc/ldap/schema/dnszone.schema\n");
        _sb.append("include         /etc/ldap/schema/hdb.schema\n");
        _sb.append("include         /etc/ldap/schema/authldap.schema\n");
        _sb.append("include         /etc/ldap/schema/dhcp.schema\n");
        _sb.append("include         /etc/ldap/schema/dyngroup.schema\n");
        _sb.append("include         /etc/ldap/schema/wbsagnitio.schema\n");
        _sb.append("include         /etc/ldap/schema/optional.schema\n");
        _sb.append("pidfile         /var/run/slapd/slapd.pid\n");
        _sb.append("argsfile        /var/run/slapd/slapd.args\n");
        _sb.append("loglevel        0\n");
        _sb.append("modulepath      /usr/lib/ldap\n");
        _sb.append("moduleload      back_hdb\n");
        _sb.append("moduleload      accesslog\n");
        _sb.append("moduleload      syncprov\n");
        _sb.append("moduleload      unique\n");
        _sb.append("moduleload      memberof\n");
        _sb.append("moduleload      constraint\n");
        _sb.append("moduleload      dynlist\n");
        int value = 5000;
        if (_c.getProperty("directory.query.limit") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.query.limit"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("sizelimit " + value + "\n");
        value = 10;
        if (_c.getProperty("directory.threads.cpu") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.threads.cpu"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("tool-threads " + value + "\n");
        _sb.append("TLSCACertificateFile /etc/pki/certs/wbsagnitio-ca.pem\n");
        _sb.append("TLSCertificateFile /etc/pki/certs/wbsagnitio." + _nm.getDomain() + ".pem\n");
        _sb.append("TLSCertificateKeyFile /etc/pki/private/wbsagnitio." + _nm.getDomain() + ".key\n");
        _sb.append("TLSVerifyClient allow\n\n");
        _sb.append("database        hdb\n");
        _sb.append("suffix          \"" + _c.getProperty("ldap.basedn") + "\"\n");
        _sb.append("directory       \"/var/lib/ldap\"\n");
        _sb.append("rootdn          \"cn=syncrepl\"\n");
        value = 512;
        if (_c.getProperty("directory.checkpoint.size") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.checkpoint.size"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("checkpoint " + value);
        value = 10;
        if (_c.getProperty("directory.checkpoint.interval") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.checkpoint.interval"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append(" " + value + "\n");
        value = 5000;
        if (_c.getProperty("directory.cachesize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.cachesize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("cachesize " + value + "\n");
        value = 15000;
        if (_c.getProperty("directory.idlcachesize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.idlcachesize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("idlcachesize " + value + "\n");
        value = 52428800;
        if (_c.getProperty("directory.db.cachesize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.db.cachesize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("dbconfig set_cachesize 0 " + value);
        value = 1;
        if (_c.getProperty("directory.db.cachenumber") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.db.cachenumber"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append(" " + value + "\n");
        value = 2097512;
        if (_c.getProperty("directory.db.logbuffersize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.db.logbuffersize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("dbconfig set_lg_bsize " + value + "\n");
        _sb.append("dbconfig set_flags DB_LOG_AUTOREMOVE\n");
        _sb.append("sasl-realm " + _nm.getDomain().toUpperCase() + "\n");
        _sb.append("sasl-host wbsagnitio." + _nm.getDomain() + "\n");
        _sb.append("sasl-regexp cn=([^,]*),cn=[^,]*,cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?cn=$1\n");
        _sb.append("sasl-regexp cn=([^,]*),cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?cn=$1\n");
        _sb.append("sasl-regexp uid=([^,]*),cn=[^,]*,cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?uid=$1\n");
        _sb.append("sasl-regexp uid=([^,]*),cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?uid=$1\n");
        _sb.append("sasl-authz-policy both\n");
        _sb.append("index           objectClass eq\n");
        _sb.append("index           entryCSN,entryUUID eq\n");
        _sb.append("index           uid,cn,gecos pres,sub,eq\n");
        _sb.append("index           uidNumber,gidNumber eq\n");
        _sb.append("index           uniqueMember,memberUid,sambaSID,SambaSIDList pres,eq\n");
        _sb.append("index           zoneName,relativeDomainName,sambaDomainName eq\n");
        _sb.append("index           krb5PrincipalName,krb5PrincipalRealm eq\n");
        _sb.append("lastmod         on\n\n");
        _sb.append("overlay         dynlist\n");
        _sb.append("dynlist-attrset virtualGroup memberDnURL uniqueMember\n");
        _sb.append("dynlist-attrset virtualGroup memberUidURL memberUid:uid\n");
        _sb.append("dynlist-attrset virtualGroup memberSidURL sambaSIDList:sambaSID\n\n");
        _sb.append("overlay         unique\n");
        _sb.append("unique_base		\"" + _c.getProperty("ldap.basedn") + "\"\n");
        _sb.append("unique_attributes	uid uidNumber employeeNumber");
        for (String _attribute : _sm.getUniqueAttributeTypesNames()) {
            _sb.append(" ");
            _sb.append(_attribute);
        }
        _sb.append("\n\n");
        if (!_hm.hasProviders()) {
            _sb.append("overlay         memberof\n");
            _sb.append("memberof-refint TRUE\n");
            _sb.append("memberof-group-oc groupOfUniqueNames\n");
            _sb.append("memberof-member-ad uniqueMember\n");
            _sb.append("memberof-memberof-ad memberOf\n\n");
        }
        if ((_c.getProperty("directory.restriction.uid") != null && !_c.getProperty("directory.restriction.uid").isEmpty()) || (_c.getProperty("directory.restriction.mail") != null && !_c.getProperty("directory.restriction.mail").isEmpty())) {
            _sb.append("overlay         constraint\n");
            if (_c.getProperty("directory.restriction.uid") != null && !_c.getProperty("directory.restriction.uid").isEmpty()) {
                _sb.append("constraint_attribute uid regex " + _c.getProperty("directory.restriction.uid") + "\n");
            }
            if (_c.getProperty("directory.restriction.mail") != null && !_c.getProperty("directory.restriction.mail").isEmpty()) {
                _sb.append("constraint_attribute mail regex " + _c.getProperty("directory.restriction.mail") + "\n");
            }
            _sb.append("\n\n");
        }
        _sb.append("overlay         accesslog\n");
        _sb.append("logdb           cn=accesslog\n");
        _sb.append("logops          ");
        if (_c.checkProperty("directory.accesslog.bind", "true")) {
            _sb.append("session ");
        }
        if (_c.checkProperty("directory.accesslog.read", "true")) {
            _sb.append("reads ");
        }
        _sb.append("writes\n");
        _sb.append("logsuccess      TRUE\n");
        _sb.append("logpurge        ");
        int day = 7;
        if (_c.getProperty("directory.accesslog.days") != null) {
            try {
                day = Integer.parseInt(_c.getProperty("directory.accesslog.days"));
            } catch (NumberFormatException _ex) {
            }
        }
        if (day < 10) {
            _sb.append("0");
        }
        _sb.append(day);
        _sb.append("+00:00 01+00:00\n\n");
        if (_hm.hasConsumers()) {
            _sb.append("overlay         syncprov\n");
            _sb.append("syncprov-checkpoint 1000 60\n\n");
        }
        _sb.append("limits dn.exact=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" time.soft=unlimited time.hard=unlimited size.soft=unlimited size.hard=unlimited\n\n");
        if (_hm.hasProviders()) {
            for (String rid : _hm.getProviderRids()) {
                Map<String, String> values = _hm.getProviderValues(rid);
                _sb.append("syncrepl        rid=" + rid + "\n");
                _sb.append("                provider=ldap://" + values.get("host") + "\n");
                _sb.append("                type=refreshAndPersist\n");
                _sb.append("                interval=00:00:15:00\n");
                _sb.append("                searchbase=\"" + values.get("basedn") + "\"\n");
                _sb.append("                attrs=*,+\n");
                _sb.append("                retry=\"5 5 300 +\"\n");
                if (values.get("userdn") != null && values.get("password") != null) {
                    _sb.append("                binddn=\"" + values.get("userdn") + "\"\n");
                    _sb.append("                credentials=" + values.get("password") + "\n");
                } else {
                    _sb.append("                binddn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + values.get("basedn") + "\"\n");
                    _sb.append("                credentials=wbs321\n");
                }
                _sb.append("                bindmethod=simple\n");
                _sb.append("                scope=sub\n");
                _sb.append("                starttls=no\n");
                _sb.append("                logbase=\"cn=accesslog\"\n");
                _sb.append("                logfilter=\"(&(objectClass=auditWriteObject)(reqResult=0))\"\n");
                _sb.append("                syncdata=accesslog\n\n");
            }
        }
        _sb.append("access to dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\"\n");
        _sb.append("        by peername.regex=\"IP=127\\.0\\.0\\.1:+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        for (String _rid : _hm.getConsumerRids()) {
            Map<String, String> values = _hm.getConsumerValues(_rid);
            _sb.append("        by peername.regex=\"IP=" + values.get("host").replace(".", "\\.") + ":+\"");
            if (values.get("userdn") != null && values.get("password") != null) {
                _sb.append(" dn=\"" + values.get("userdn") + "\" read\n");
            } else {
                _sb.append(" read\n");
            }
        }
        _sb.append("        by * auth\n\n");
        for (String _context : _cm.getContextNames()) {
            Entry _e = _cm.getContext(_context);
            _sb.append("access to dn.subtree=\"" + _e.getID() + "\"\n");
            _sb.append("        by peername.regex=\"IP=127\\.0\\.0\\.1:+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
            for (String _rid : _hm.getRestrictedContextConsumerRids(_context)) {
                Map<String, String> values = _hm.getConsumerValues(_rid);
                _sb.append("        by peername.regex=\"IP=" + values.get("host").replace(".", "\\.") + ":+\" none\n");
            }
            _sb.append("        by ssf=128 dn=\"uid=" + _sc.getAdministrativeUser() + ",ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
            if (_c.checkProperty("directory.security.users.edit.attributes.basic", "true")) {
                _sb.append("        by ssf=128 self write\n");
            }
            _sb.append("        by dn.regex=\"^uid=(.*),ou=([^,]+)," + _e.getID() + "\" read\n");
            _sb.append("        by * break\n\n");
        }
        for (Entry _e : _bm.getAllBranches()) {
            if (_e.hasAttribute("writeMemberGroup") || _e.hasAttribute("readMemberGroup")) {
                _sb.append("access to dn.children=\"" + _e.getID() + "\"\n");
                if (_e.hasAttribute("writeMemberGroup")) {
                    for (Object o : _e.getAttribute("writeMemberGroup")) {
                        _sb.append("        by ssf=128 set=\"user & [");
                        _sb.append(o);
                        _sb.append("]/uniqueMember\" write\n");
                    }
                }
                if (_e.hasAttribute("readMemberGroup")) {
                    for (Object o : _e.getAttribute("readMemberGroup")) {
                        _sb.append("        by set=\"user & [");
                        _sb.append(o);
                        _sb.append("]/uniqueMember\" read\n");
                    }
                }
                _sb.append("        by * break\n\n");
            }
        }
        _sb.append("access to attrs=userPassword,shadowLastChange,sambaNTPassword,sambaLMPassword,krb5Key,userPKCS12\n");
        _sb.append("        by peername.regex=\"IP=127\\.0\\.0\\.1:+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        _sb.append("        by ssf=128 dn=\"uid=" + _sc.getAdministrativeUser() + ",ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        for (String _rid : _hm.getConsumerRids()) {
            Map<String, String> values = _hm.getConsumerValues(_rid);
            _sb.append("        by peername.regex=\"IP=" + values.get("host").replace(".", "\\.") + ":+\"");
            if (values.get("userdn") != null && values.get("password") != null) {
                _sb.append(" dn=\"" + values.get("userdn") + "\" read\n");
            } else {
                _sb.append(" read\n");
            }
        }
        if (_c.checkProperty("directory.security.users.edit.attributes.password", "true")) {
            _sb.append("        by ssf=128 self write\n");
        }
        _sb.append("        by set=\"this/accountEnableStatus & [disabled]\" none\n");
        _sb.append("        by * break\n\n");
        _sb.append("access to *\n");
        _sb.append("        by sockurl.regex=\"^ldapi:///$\" write\n");
        _sb.append("        by peername.regex=\"IP=127\\.0\\.0\\.1:+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        _sb.append("        by ssf=128 dn=\"uid=" + _sc.getAdministrativeUser() + ",ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        for (String _rid : _hm.getConsumerRids()) {
            Map<String, String> values = _hm.getConsumerValues(_rid);
            _sb.append("        by peername.regex=\"IP=" + values.get("host").replace(".", "\\.") + ":+\"");
            if (values.get("userdn") != null && values.get("password") != null) {
                _sb.append(" dn=\"" + values.get("userdn") + "\" read\n");
            } else {
                _sb.append(" read\n");
            }
        }
        if (_c.checkProperty("directory.security.users.edit.attributes.basic", "true")) {
            _sb.append("        by ssf=128 self write\n");
        }
        _sb.append("        by set=\"user/accountEnableStatus & [disabled]\" none\n");
        _sb.append("        by dn.regex=\"^uid=(.*),ou=([^,]+)," + _c.getProperty("ldap.basedn") + "\" read\n");
        _sb.append("        by anonymous read\n");
        _sb.append("        by * auth\n\n");
        _sb.append("database        hdb\n");
        _sb.append("suffix          \"cn=accesslog\"\n");
        _sb.append("directory       /var/lib/ldap/accesslog\n");
        _sb.append("checkpoint 512 60\n");
        _sb.append("cachesize 1000\n");
        _sb.append("idlcachesize 3000\n");
        _sb.append("dbconfig set_cachesize 0 10485760 0\n");
        _sb.append("dbconfig set_lg_bsize 2097512\n");
        _sb.append("dbconfig set_flags DB_LOG_AUTOREMOVE\n");
        _sb.append("index           objectClass eq\n");
        _sb.append("index           entryCSN,entryUUID eq\n");
        _sb.append("index           reqEnd,reqResult,reqStart eq\n");
        if (_hm.hasConsumers()) {
            _sb.append("overlay         syncprov\n");
            _sb.append("syncprov-nopresent TRUE\n");
            _sb.append("syncprov-reloadhint TRUE\n\n");
        }
        FileOutputStream _fos = new FileOutputStream("/etc/ldap/slapd.conf");
        _fos.write(_sb.toString().getBytes());
        _fos.close();
        ServiceManager.restart(ServiceManager.LDAP);
        ServiceManager.restart(ServiceManager.WATCHDOG);
    }

    public static void setSlave(String virtual_address, String master_address) throws Exception {
        File _f = new File(FILE_CONFIGURATION);
        if (!_f.exists()) {
            throw new Exception("no se encuentra el fichero de configuraci&oacute;n");
        }
        Configuration _c = new Configuration(_f);
        SystemConfiguration _sc = new SystemConfiguration(_c);
        NetworkManager _nm = new NetworkManager(_c);
        ContextManager _cm = new ContextManager(_c);
        if (!new File("/etc/ha.d/ha.cf").exists()) {
            _nm.setSlaveNetwork(master_address);
            StringBuilder _sb = new StringBuilder();
            _sb.append("logfile /var/log/heartbeat.log\n");
            _sb.append("logfacility local0\n");
            _sb.append("keepalive 2\n");
            _sb.append("deadtime 30\n");
            _sb.append("auto_failback on\n");
            _sb.append("initdead 60\n");
            _sb.append("hopfudge 1\n");
            _sb.append("udpport 694\n");
            _sb.append("ucast " + _nm.getInterface(0) + " master\n");
            _sb.append("node master\n");
            _sb.append("node slave\n");
            FileOutputStream _fos = new FileOutputStream("/etc/ha.d/ha.cf");
            _fos.write(_sb.toString().getBytes());
            _fos.close();
            if (!new File("/etc/ha.d/authkeys").exists()) {
                _sb = new StringBuilder();
                _sb.append("auth 1\n");
                _sb.append("1 sha1 1wbs2agnitio3\n");
                _fos = new FileOutputStream("/etc/ha.d/authkeys");
                _fos.write(_sb.toString().getBytes());
                _fos.close();
                Command.systemCommand("/bin/chmod 600 /etc/ha.d/authkeys");
            }
            _sb = new StringBuilder();
            _sb.append("master " + virtual_address + " LVS-DR ldirectord");
            if (new File("/etc/rc3.d/S50samba").exists()) {
                _sb.append(" samba");
            }
            _sb.append("\n");
            _fos = new FileOutputStream("/etc/ha.d/haresources");
            _fos.write(_sb.toString().getBytes());
            _fos.close();
            _f = new File("/var/lib/heartbeat/hb_uuid");
            if (_f.exists()) {
                _f.delete();
            }
            _sb = new StringBuilder();
            _sb.append("checktimeout = 10\n");
            _sb.append("checkinterval = 10\n");
            _sb.append("autoreload=yes\n");
            _sb.append("quiescent=yes\n");
            _sb.append("\nvirtual = " + virtual_address + ":389\n");
            _sb.append("\tservice = ldap\n");
            _sb.append("\tprotocol = tcp\n");
            _sb.append("\tscheduler = wlc\n");
            _sb.append("\treal = " + master_address + ":389 gate 100\n");
            _sb.append("\treal = " + _nm.getStringAddress(_nm.getInterface(0)) + ":389 gate 100\n");
            _sb.append("\trequest=\"" + _c.getProperty("ldap.basedn") + "\"\n");
            _sb.append("\treceive=\"" + _c.getProperty("ldap.basedn") + "\"\n");
            _sb.append("\nvirtual = " + virtual_address + ":53\n");
            _sb.append("\tservice = dns\n");
            _sb.append("\tprotocol = udp\n");
            _sb.append("\tscheduler = wlc\n");
            _sb.append("\treal = " + master_address + ":53 gate 100\n");
            _sb.append("\treal = " + _nm.getStringAddress(_nm.getInterface(0)) + ":53 gate 100\n");
            _sb.append("\tchecktype = connect\n");
            _sb.append("\nvirtual = " + virtual_address + ":53\n");
            _sb.append("\tservice = dns\n");
            _sb.append("\tprotocol = tcp\n");
            _sb.append("\tscheduler = wlc\n");
            _sb.append("\treal = " + master_address + ":53 gate 100\n");
            _sb.append("\treal = " + _nm.getStringAddress(_nm.getInterface(0)) + ":53 gate 100\n");
            _sb.append("\tchecktype = connect\n");
            _fos = new FileOutputStream("/etc/ha.d/ldirectord.cf");
            _fos.write(_sb.toString().getBytes());
            _fos.close();
            ServiceManager.initialize(ServiceManager.CLUSTER, _c);
        }
        if (!ServiceManager.isRunning(ServiceManager.LDAP)) {
            ServiceManager.start(ServiceManager.LDAP);
            Thread.sleep(500);
        }
        StringBuilder _sb = new StringBuilder();
        _sb.append("include         /etc/ldap/schema/core.schema\n");
        _sb.append("include         /etc/ldap/schema/cosine.schema\n");
        _sb.append("include         /etc/ldap/schema/nis.schema\n");
        _sb.append("include         /etc/ldap/schema/inetorgperson.schema\n");
        _sb.append("include         /etc/ldap/schema/samba.schema\n");
        _sb.append("include         /etc/ldap/schema/dnszone.schema\n");
        _sb.append("include         /etc/ldap/schema/hdb.schema\n");
        _sb.append("include         /etc/ldap/schema/authldap.schema\n");
        _sb.append("include         /etc/ldap/schema/dhcp.schema\n");
        _sb.append("include         /etc/ldap/schema/dyngroup.schema\n");
        _sb.append("include         /etc/ldap/schema/wbsagnitio.schema\n");
        _sb.append("include         /etc/ldap/schema/optional.schema\n");
        _sb.append("pidfile         /var/run/slapd/slapd.pid\n");
        _sb.append("argsfile        /var/run/slapd/slapd.args\n");
        _sb.append("loglevel        0\n");
        _sb.append("modulepath      /usr/lib/ldap\n");
        _sb.append("moduleload      back_hdb\n");
        _sb.append("moduleload      dynlist\n");
        int value = 5000;
        if (_c.getProperty("directory.query.limit") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.query.limit"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("sizelimit " + value + "\n");
        value = 10;
        if (_c.getProperty("directory.threads.cpu") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.threads.cpu"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("tool-threads " + value + "\n");
        _sb.append("TLSCACertificateFile /etc/pki/certs/wbsagnitio-ca.pem\n");
        _sb.append("TLSCertificateFile /etc/pki/certs/wbsagnitio." + _nm.getDomain() + ".pem\n");
        _sb.append("TLSCertificateKeyFile /etc/pki/private/wbsagnitio." + _nm.getDomain() + ".key\n");
        _sb.append("TLSVerifyClient allow\n\n");
        _sb.append("database        hdb\n");
        _sb.append("suffix          \"" + _c.getProperty("ldap.basedn") + "\"\n");
        _sb.append("directory       \"/var/lib/ldap\"\n");
        _sb.append("rootdn          \"cn=syncrepl\"\n");
        value = 512;
        if (_c.getProperty("directory.checkpoint.size") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.checkpoint.size"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("checkpoint " + value);
        value = 10;
        if (_c.getProperty("directory.checkpoint.interval") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.checkpoint.interval"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append(" " + value + "\n");
        value = 5000;
        if (_c.getProperty("directory.cachesize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.cachesize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("cachesize " + value + "\n");
        value = 15000;
        if (_c.getProperty("directory.idlcachesize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.idlcachesize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("idlcachesize " + value + "\n");
        value = 52428800;
        if (_c.getProperty("directory.db.cachesize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.db.cachesize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("dbconfig set_cachesize 0 " + value);
        value = 1;
        if (_c.getProperty("directory.db.cachenumber") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.db.cachenumber"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append(" " + value + "\n");
        value = 2097512;
        if (_c.getProperty("directory.db.logbuffersize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.db.logbuffersize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("dbconfig set_lg_bsize " + value + "\n");
        _sb.append("dbconfig set_flags DB_LOG_AUTOREMOVE\n");
        _sb.append("sasl-realm " + _nm.getDomain().toUpperCase() + "\n");
        _sb.append("sasl-host wbsagnitio." + _nm.getDomain() + "\n");
        _sb.append("sasl-regexp cn=([^,]*),cn=[^,]*,cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?cn=$1\n");
        _sb.append("sasl-regexp cn=([^,]*),cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?cn=$1\n");
        _sb.append("sasl-regexp uid=([^,]*),cn=[^,]*,cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?uid=$1\n");
        _sb.append("sasl-regexp uid=([^,]*),cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?uid=$1\n");
        _sb.append("sasl-authz-policy both\n");
        _sb.append("index           objectClass eq\n");
        _sb.append("index           uid,cn,gecos pres,sub,eq\n");
        _sb.append("index           uidNumber,gidNumber eq\n");
        _sb.append("index           uniqueMember,memberUid,sambaSID,SambaSIDList pres,eq\n");
        _sb.append("index           zoneName,relativeDomainName,sambaDomainName eq\n");
        _sb.append("index           krb5PrincipalName,krb5PrincipalRealm eq\n");
        _sb.append("lastmod         on\n\n");
        _sb.append("overlay         dynlist\n");
        _sb.append("dynlist-attrset virtualGroup memberDnURL uniqueMember\n");
        _sb.append("dynlist-attrset virtualGroup memberUidURL memberUid:uid\n");
        _sb.append("dynlist-attrset virtualGroup memberSidURL sambaSIDList:sambaSID\n\n");
        _sb.append("syncrepl        rid=001\n");
        _sb.append("                provider=ldap://" + master_address + "\n");
        _sb.append("                type=refreshAndPersist\n");
        _sb.append("                interval=00:00:15:00\n");
        _sb.append("                searchbase=\"" + _c.getProperty("ldap.basedn") + "\"\n");
        _sb.append("                attrs=*,+\n");
        _sb.append("                retry=\"5 5 300 +\"\n");
        _sb.append("                binddn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\"\n");
        _sb.append("                credentials=wbs321\n");
        _sb.append("                bindmethod=simple\n");
        _sb.append("                scope=sub\n");
        _sb.append("                starttls=no\n");
        _sb.append("                logbase=\"cn=accesslog\"\n");
        _sb.append("                logfilter=\"(&(objectClass=auditWriteObject)(reqResult=0))\"\n");
        _sb.append("                syncdata=accesslog\n\n");
        _sb.append("access to dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\"\n");
        _sb.append("        by peername.regex=\"IP=" + master_address.replace(".", "\\.") + "+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        _sb.append("        by * auth\n\n");
        _sb.append("access to attrs=userPassword,shadowLastChange,sambaNTPassword,sambaLMPassword,krb5Key,userPKCS12\n");
        _sb.append("        by peername.regex=\"IP=" + master_address.replace(".", "\\.") + "+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        _sb.append("        by set=\"this/accountEnableStatus & [disabled]\" none\n");
        _sb.append("        by * break\n\n");
        for (String _context : _cm.getContextNames()) {
            Entry _e = _cm.getContext(_context);
            _sb.append("access to dn.subtree=\"" + _e.getID() + "\"\n");
            _sb.append("        by peername.regex=\"IP=" + master_address.replace(".", "\\.") + "+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
            _sb.append("        by dn.regex=\"^uid=(.*),ou=([^,]+)," + _e.getID() + "\" read\n");
            _sb.append("        by * break\n\n");
        }
        _sb.append("access to *\n");
        _sb.append("        by peername.regex=\"IP=" + master_address.replace(".", "\\.") + "+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        _sb.append("        by set=\"user/accountEnableStatus & [disabled]\" none\n");
        _sb.append("        by dn.regex=\"^uid=(.*),ou=([^,]+)," + _c.getProperty("ldap.basedn") + "\" read\n");
        _sb.append("        by anonymous read\n");
        _sb.append("        by * auth\n\n");
        FileOutputStream _fos = new FileOutputStream("/etc/ldap/slapd.conf");
        _fos.write(_sb.toString().getBytes());
        _fos.close();
        ServiceManager.restart(ServiceManager.LDAP);
        ServiceManager.restart(ServiceManager.WATCHDOG);
    }

    public static void setMaster(String virtual_address, String slave_address) throws Exception {
        File _f = new File(FILE_CONFIGURATION);
        if (!_f.exists()) {
            throw new Exception("no se encuentra el fichero de configuraci&oacute;n");
        }
        Configuration _c = new Configuration(_f);
        SystemConfiguration _sc = new SystemConfiguration(_c);
        NetworkManager _nm = new NetworkManager(_c);
        HierarchyManager _hm = new HierarchyManager(_c);
        SchemaManager _sm = new SchemaManager();
        ContextManager _cm = new ContextManager(_c);
        BranchManager _bm = new BranchManager(_c);
        if (!new File("/etc/ha.d/ha.cf").exists()) {
            _nm.setMasterNetwork(virtual_address, slave_address);
            StringBuilder _sb = new StringBuilder();
            _sb.append("logfile /var/log/heartbeat.log\n");
            _sb.append("logfacility local0\n");
            _sb.append("keepalive 2\n");
            _sb.append("deadtime 30\n");
            _sb.append("auto_failback on\n");
            _sb.append("initdead 60\n");
            _sb.append("hopfudge 1\n");
            _sb.append("udpport 694\n");
            _sb.append("ucast " + _nm.getInterface(0) + " slave\n");
            _sb.append("node master\n");
            _sb.append("node slave\n");
            FileOutputStream _fos = new FileOutputStream("/etc/ha.d/ha.cf");
            _fos.write(_sb.toString().getBytes());
            _fos.close();
            if (!new File("/etc/ha.d/authkeys").exists()) {
                _sb = new StringBuilder();
                _sb.append("auth 1\n");
                _sb.append("1 sha1 1wbs2agnitio3\n");
                _fos = new FileOutputStream("/etc/ha.d/authkeys");
                _fos.write(_sb.toString().getBytes());
                _fos.close();
                Command.systemCommand("/bin/chmod 600 /etc/ha.d/authkeys");
            }
            _sb = new StringBuilder();
            _sb.append("master " + virtual_address + " LVS-DR ldirectord");
            if (new File("/etc/rc3.d/S50samba").exists()) {
                _sb.append(" samba");
            }
            _sb.append("\n");
            _fos = new FileOutputStream("/etc/ha.d/haresources");
            _fos.write(_sb.toString().getBytes());
            _fos.close();
            _f = new File("/var/lib/heartbeat/hb_uuid");
            if (_f.exists()) {
                _f.delete();
            }
            _sb = new StringBuilder();
            _sb.append("checktimeout = 10\n");
            _sb.append("checkinterval = 10\n");
            _sb.append("autoreload=yes\n");
            _sb.append("quiescent=yes\n");
            _sb.append("\nvirtual = " + virtual_address + ":389\n");
            _sb.append("\tservice = ldap\n");
            _sb.append("\tprotocol = tcp\n");
            _sb.append("\tscheduler = wlc\n");
            _sb.append("\treal = " + _nm.getStringAddress(_nm.getInterface(0)) + ":389 gate 100\n");
            _sb.append("\treal = " + slave_address + ":389 gate 100\n");
            _sb.append("\trequest=\"" + _c.getProperty("ldap.basedn") + "\"\n");
            _sb.append("\treceive=\"" + _c.getProperty("ldap.basedn") + "\"\n");
            _sb.append("\nvirtual = " + virtual_address + ":53\n");
            _sb.append("\tservice = dns\n");
            _sb.append("\tprotocol = udp\n");
            _sb.append("\tscheduler = wlc\n");
            _sb.append("\treal = " + _nm.getStringAddress(_nm.getInterface(0)) + ":53 gate 100\n");
            _sb.append("\treal = " + slave_address + ":53 gate 100\n");
            _sb.append("\tchecktype = connect\n");
            _sb.append("\nvirtual = " + virtual_address + ":53\n");
            _sb.append("\tservice = dns\n");
            _sb.append("\tprotocol = tcp\n");
            _sb.append("\tscheduler = wlc\n");
            _sb.append("\treal = " + _nm.getStringAddress(_nm.getInterface(0)) + ":53 gate 100\n");
            _sb.append("\treal = " + slave_address + ":53 gate 100\n");
            _sb.append("\tchecktype = connect\n");
            _fos = new FileOutputStream("/etc/ha.d/ldirectord.cf");
            _fos.write(_sb.toString().getBytes());
            _fos.close();
            ServiceManager.initialize(ServiceManager.CLUSTER, _c);
        }
        if (!ServiceManager.isRunning(ServiceManager.LDAP)) {
            ServiceManager.start(ServiceManager.LDAP);
            Thread.sleep(500);
        }
        StringBuilder _sb = new StringBuilder();
        _sb.append("include         /etc/ldap/schema/core.schema\n");
        _sb.append("include         /etc/ldap/schema/cosine.schema\n");
        _sb.append("include         /etc/ldap/schema/nis.schema\n");
        _sb.append("include         /etc/ldap/schema/inetorgperson.schema\n");
        _sb.append("include         /etc/ldap/schema/samba.schema\n");
        _sb.append("include         /etc/ldap/schema/dnszone.schema\n");
        _sb.append("include         /etc/ldap/schema/hdb.schema\n");
        _sb.append("include         /etc/ldap/schema/authldap.schema\n");
        _sb.append("include         /etc/ldap/schema/dhcp.schema\n");
        _sb.append("include         /etc/ldap/schema/dyngroup.schema\n");
        _sb.append("include         /etc/ldap/schema/wbsagnitio.schema\n");
        _sb.append("include         /etc/ldap/schema/optional.schema\n");
        _sb.append("pidfile         /var/run/slapd/slapd.pid\n");
        _sb.append("argsfile        /var/run/slapd/slapd.args\n");
        _sb.append("loglevel        0\n");
        _sb.append("modulepath      /usr/lib/ldap\n");
        _sb.append("moduleload      back_hdb\n");
        _sb.append("moduleload      accesslog\n");
        _sb.append("moduleload      syncprov\n");
        _sb.append("moduleload      unique\n");
        _sb.append("moduleload      memberof\n");
        _sb.append("moduleload      constraint\n");
        _sb.append("moduleload      dynlist\n");
        int value = 5000;
        if (_c.getProperty("directory.query.limit") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.query.limit"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("sizelimit " + value + "\n");
        value = 10;
        if (_c.getProperty("directory.threads.cpu") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.threads.cpu"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("tool-threads " + value + "\n");
        _sb.append("TLSCACertificateFile /etc/pki/certs/wbsagnitio-ca.pem\n");
        _sb.append("TLSCertificateFile /etc/pki/certs/wbsagnitio." + _nm.getDomain() + ".pem\n");
        _sb.append("TLSCertificateKeyFile /etc/pki/private/wbsagnitio." + _nm.getDomain() + ".key\n");
        _sb.append("TLSVerifyClient allow\n\n");
        _sb.append("database        hdb\n");
        _sb.append("suffix          \"" + _c.getProperty("ldap.basedn") + "\"\n");
        _sb.append("directory       \"/var/lib/ldap\"\n");
        _sb.append("rootdn          \"cn=syncrepl\"\n");
        value = 512;
        if (_c.getProperty("directory.checkpoint.size") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.checkpoint.size"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("checkpoint " + value);
        value = 10;
        if (_c.getProperty("directory.checkpoint.interval") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.checkpoint.interval"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append(" " + value + "\n");
        value = 5000;
        if (_c.getProperty("directory.cachesize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.cachesize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("cachesize " + value + "\n");
        value = 15000;
        if (_c.getProperty("directory.idlcachesize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.idlcachesize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("idlcachesize " + value + "\n");
        value = 52428800;
        if (_c.getProperty("directory.db.cachesize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.db.cachesize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("dbconfig set_cachesize 0 " + value);
        value = 1;
        if (_c.getProperty("directory.db.cachenumber") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.db.cachenumber"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append(" " + value + "\n");
        value = 2097512;
        if (_c.getProperty("directory.db.logbuffersize") != null) {
            try {
                value = Integer.parseInt(_c.getProperty("directory.db.logbuffersize"));
            } catch (NumberFormatException _ex) {
            }
        }
        _sb.append("dbconfig set_lg_bsize " + value + "\n");
        _sb.append("dbconfig set_flags DB_LOG_AUTOREMOVE\n");
        _sb.append("sasl-realm " + _nm.getDomain().toUpperCase() + "\n");
        _sb.append("sasl-host wbsagnitio." + _nm.getDomain() + "\n");
        _sb.append("sasl-regexp cn=([^,]*),cn=[^,]*,cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?cn=$1\n");
        _sb.append("sasl-regexp cn=([^,]*),cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?cn=$1\n");
        _sb.append("sasl-regexp uid=([^,]*),cn=[^,]*,cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?uid=$1\n");
        _sb.append("sasl-regexp uid=([^,]*),cn=[^,]*,cn=auth ldap:///" + _c.getProperty("ldap.basedn") + "??sub?uid=$1\n");
        _sb.append("sasl-authz-policy both\n");
        _sb.append("index           objectClass eq\n");
        _sb.append("index           entryCSN,entryUUID eq\n");
        _sb.append("index           uid,cn,gecos pres,sub,eq\n");
        _sb.append("index           uidNumber,gidNumber eq\n");
        _sb.append("index           uniqueMember,memberUid,sambaSID,SambaSIDList pres,eq\n");
        _sb.append("index           zoneName,relativeDomainName,sambaDomainName eq\n");
        _sb.append("index           krb5PrincipalName,krb5PrincipalRealm eq\n");
        _sb.append("lastmod         on\n\n");
        _sb.append("overlay         dynlist\n");
        _sb.append("dynlist-attrset virtualGroup memberDnURL uniqueMember\n");
        _sb.append("dynlist-attrset virtualGroup memberUidURL memberUid:uid\n");
        _sb.append("dynlist-attrset virtualGroup memberSidURL sambaSIDList:sambaSID\n\n");
        _sb.append("overlay         unique\n");
        _sb.append("unique_base		\"" + _c.getProperty("ldap.basedn") + "\"\n");
        _sb.append("unique_attributes	uid uidNumber employeeNumber");
        for (String _attribute : _sm.getUniqueAttributeTypesNames()) {
            _sb.append(" ");
            _sb.append(_attribute);
        }
        _sb.append("\n\n");
        if (!_hm.hasProviders()) {
            _sb.append("overlay         memberof\n");
            _sb.append("memberof-refint TRUE\n");
            _sb.append("memberof-group-oc groupOfUniqueNames\n");
            _sb.append("memberof-member-ad uniqueMember\n");
            _sb.append("memberof-memberof-ad memberOf\n\n");
        }
        if ((_c.getProperty("directory.restriction.uid") != null && !_c.getProperty("directory.restriction.uid").isEmpty()) || (_c.getProperty("directory.restriction.mail") != null && !_c.getProperty("directory.restriction.mail").isEmpty())) {
            _sb.append("overlay         constraint\n");
            if (_c.getProperty("directory.restriction.uid") != null && !_c.getProperty("directory.restriction.uid").isEmpty()) {
                _sb.append("constraint_attribute uid regex " + _c.getProperty("directory.restriction.uid") + "\n");
            }
            if (_c.getProperty("directory.restriction.mail") != null && !_c.getProperty("directory.restriction.mail").isEmpty()) {
                _sb.append("constraint_attribute mail regex " + _c.getProperty("directory.restriction.mail") + "\n");
            }
            _sb.append("\n");
        }
        _sb.append("overlay         accesslog\n");
        _sb.append("logdb           cn=accesslog\n");
        _sb.append("logops          ");
        if (_c.checkProperty("directory.accesslog.bind", "true")) {
            _sb.append("session ");
        }
        if (_c.checkProperty("directory.accesslog.read", "true")) {
            _sb.append("reads ");
        }
        _sb.append("writes\n");
        _sb.append("logsuccess      TRUE\n");
        _sb.append("logpurge        ");
        int day = 7;
        if (_c.getProperty("directory.accesslog.days") != null) {
            try {
                day = Integer.parseInt(_c.getProperty("directory.accesslog.days"));
            } catch (NumberFormatException _ex) {
            }
        }
        if (day < 10) {
            _sb.append("0");
        }
        _sb.append(day);
        _sb.append("+00:00 01+00:00\n\n");
        _sb.append("overlay         syncprov\n");
        _sb.append("syncprov-checkpoint 1000 60\n\n");
        _sb.append("limits dn.exact=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" time.soft=unlimited time.hard=unlimited size.soft=unlimited size.hard=unlimited\n\n");
        if (_hm.hasProviders()) {
            for (String _rid : _hm.getProviderRids()) {
                Map<String, String> values = _hm.getProviderValues(_rid);
                _sb.append("syncrepl        rid=" + _rid + "\n");
                _sb.append("                provider=ldap://" + values.get("host") + "\n");
                _sb.append("                type=refreshAndPersist\n");
                _sb.append("                interval=00:00:15:00\n");
                _sb.append("                searchbase=\"" + values.get("basedn") + "\"\n");
                _sb.append("                attrs=*,+\n");
                _sb.append("                retry=\"5 5 300 +\"\n");
                if (values.get("userdn") != null && values.get("password") != null) {
                    _sb.append("                binddn=\"" + values.get("userdn") + "\"\n");
                    _sb.append("                credentials=" + values.get("password") + "\n");
                } else {
                    _sb.append("                binddn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + values.get("basedn") + "\"\n");
                    _sb.append("                credentials=wbs321\n");
                }
                _sb.append("                bindmethod=simple\n");
                _sb.append("                scope=sub\n");
                _sb.append("                starttls=no\n");
                _sb.append("                logbase=\"cn=accesslog\"\n");
                _sb.append("                logfilter=\"(&(objectClass=auditWriteObject)(reqResult=0))\"\n");
                _sb.append("                syncdata=accesslog\n\n");
            }
        }
        _sb.append("access to dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\"\n");
        _sb.append("        by peername.regex=\"IP=127\\.0\\.0\\.1:+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        _sb.append("        by peername.regex=\"IP=" + slave_address.replace(".", "\\.") + ":+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" read\n");
        for (String _rid : _hm.getConsumerRids()) {
            Map<String, String> values = _hm.getConsumerValues(_rid);
            _sb.append("        by peername.regex=\"IP=" + values.get("host").replace(".", "\\.") + ":+\"");
            if (values.get("userdn") != null && values.get("password") != null) {
                _sb.append(" dn=\"" + values.get("userdn") + "\" read\n");
            } else {
                _sb.append(" read\n");
            }
        }
        _sb.append("        by * auth\n\n");
        for (String _context : _cm.getContextNames()) {
            Entry _e = _cm.getContext(_context);
            _sb.append("access to dn.subtree=\"" + _e.getID() + "\"\n");
            _sb.append("        by peername.regex=\"IP=127\\.0\\.0\\.1:+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
            for (String _rid : _hm.getRestrictedContextConsumerRids(_context)) {
                Map<String, String> values = _hm.getConsumerValues(_rid);
                _sb.append("        by peername.regex=\"IP=" + values.get("host").replace(".", "\\.") + ":+\" none\n");
            }
            _sb.append("        by ssf=128 dn=\"uid=" + _sc.getAdministrativeUser() + ",ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
            if (_c.checkProperty("directory.security.users.edit.attributes.basic", "true")) {
                _sb.append("        by ssf=128 self write\n");
            }
            _sb.append("        by dn.regex=\"^uid=(.*),ou=([^,]+)," + _e.getID() + "\" read\n");
            _sb.append("        by * break\n\n");
        }
        for (Entry _e : _bm.getAllBranches()) {
            if (_e.hasAttribute("writeMemberGroup") || _e.hasAttribute("readMemberGroup")) {
                _sb.append("access to dn.children=\"" + _e.getID() + "\"\n");
                if (_e.hasAttribute("writeMemberGroup")) {
                    for (Object o : _e.getAttribute("writeMemberGroup")) {
                        _sb.append("        by ssf=128 set=\"user & [");
                        _sb.append(o);
                        _sb.append("]/uniqueMember\" write\n");
                    }
                }
                if (_e.hasAttribute("readMemberGroup")) {
                    for (Object o : _e.getAttribute("readMemberGroup")) {
                        _sb.append("        by set=\"user & [");
                        _sb.append(o);
                        _sb.append("]/uniqueMember\" read\n");
                    }
                }
                _sb.append("        by * break\n\n");
            }
        }
        _sb.append("access to attrs=userPassword,shadowLastChange,sambaNTPassword,sambaLMPassword,krb5Key,userPKCS12\n");
        _sb.append("        by peername.regex=\"IP=127\\.0\\.0\\.1:+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        _sb.append("        by ssf=128 dn=\"uid=" + _sc.getAdministrativeUser() + ",ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        for (String _rid : _hm.getConsumerRids()) {
            Map<String, String> values = _hm.getConsumerValues(_rid);
            _sb.append("        by peername.regex=\"IP=" + values.get("host").replace(".", "\\.") + ":+\"");
            if (values.get("userdn") != null && values.get("password") != null) {
                _sb.append(" dn=\"" + values.get("userdn") + "\" read\n");
            } else {
                _sb.append(" read\n");
            }
        }
        _sb.append("        by peername.regex=\"IP=" + slave_address.replace(".", "\\.") + ":+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" read\n");
        if (_c.checkProperty("directory.security.users.edit.attributes.password", "true")) {
            _sb.append("        by ssf=128 self write\n");
        }
        _sb.append("        by set=\"this/accountEnableStatus & [disabled]\" none\n");
        _sb.append("        by * break\n\n");
        _sb.append("access to *\n");
        _sb.append("        by sockurl.regex=\"^ldapi:///$\" write\n");
        _sb.append("        by peername.regex=\"IP=127\\.0\\.0\\.1:+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        _sb.append("        by ssf=128 dn=\"uid=" + _sc.getAdministrativeUser() + ",ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" write\n");
        _sb.append("        by peername.regex=\"IP=" + slave_address.replace(".", "\\.") + ":+\" dn=\"cn=Manager,ou=" + _sc.getBranchName(SystemConfiguration.BRANCH_USERS) + "," + _c.getProperty("ldap.basedn") + "\" read\n");
        for (String _rid : _hm.getConsumerRids()) {
            Map<String, String> values = _hm.getConsumerValues(_rid);
            _sb.append("        by peername.regex=\"IP=" + values.get("host").replace(".", "\\.") + ":+\"");
            if (values.get("userdn") != null && values.get("password") != null) {
                _sb.append(" dn=\"" + values.get("userdn") + "\" read\n");
            } else {
                _sb.append(" read\n");
            }
        }
        if (_c.checkProperty("directory.security.users.edit.attributes.basic", "true")) {
            _sb.append("        by ssf=128 self write\n");
        }
        _sb.append("        by set=\"user/accountEnableStatus & [disabled]\" none\n");
        _sb.append("        by dn.regex=\"^uid=(.*),ou=([^,]+)," + _c.getProperty("ldap.basedn") + "\" read\n");
        _sb.append("        by anonymous read\n");
        _sb.append("        by * auth\n\n");
        _sb.append("database        hdb\n");
        _sb.append("suffix          \"cn=accesslog\"\n");
        _sb.append("directory       /var/lib/ldap/accesslog\n");
        _sb.append("checkpoint 512 60\n");
        _sb.append("cachesize 1000\n");
        _sb.append("idlcachesize 3000\n");
        _sb.append("dbconfig set_cachesize 0 10485760 0\n");
        _sb.append("dbconfig set_lg_bsize 2097512\n");
        _sb.append("dbconfig set_flags DB_LOG_AUTOREMOVE\n");
        _sb.append("index           objectClass eq\n");
        _sb.append("index           entryCSN,entryUUID eq\n");
        _sb.append("index           reqEnd,reqResult,reqStart eq\n\n");
        _sb.append("overlay         syncprov\n");
        _sb.append("syncprov-nopresent TRUE\n");
        _sb.append("syncprov-reloadhint TRUE\n\n");
        FileOutputStream _fos = new FileOutputStream("/etc/ldap/slapd.conf");
        _fos.write(_sb.toString().getBytes());
        _fos.close();
        ServiceManager.restart(ServiceManager.LDAP);
        ServiceManager.restart(ServiceManager.WATCHDOG);
    }
}
