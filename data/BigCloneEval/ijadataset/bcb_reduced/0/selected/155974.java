package org.internna.ossmoney.services.impl;

import java.util.Set;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipOutputStream;
import org.internna.ossmoney.model.Payee;
import org.internna.ossmoney.model.Account;
import org.internna.ossmoney.util.StringUtils;
import org.internna.ossmoney.model.AccountTransaction;
import org.internna.ossmoney.model.FinancialInstitution;
import org.internna.ossmoney.model.security.UserDetails;
import org.internna.ossmoney.model.support.NameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.util.StringUtils.hasText;

@Component
@Transactional
public final class AdminService implements org.internna.ossmoney.services.AdminService {

    @Autowired
    private MessageSource messageSource;

    protected void setMessageSource(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void createInstitution(String name, String web, String icon) {
        UserDetails user = UserDetails.findCurrentUser();
        createInstitution(user, name, web, icon);
    }

    @Override
    public void createInstitution(UserDetails owner, String name, String web, String icon) {
        if (hasText(name) && (owner != null)) {
            FinancialInstitution institution = new FinancialInstitution();
            institution.setWeb(web);
            institution.setIcon(icon);
            institution.setName(name);
            institution.setOwner(owner);
            institution.persist();
            owner.getInstitutions().add(institution);
            owner.merge();
            createPayee(owner, name);
        }
    }

    @Override
    public void createPayee(String name) {
        UserDetails user = UserDetails.findCurrentUser();
        createPayee(user, name);
    }

    @Override
    public void createPayee(UserDetails owner, String name) {
        if (hasText(name) && (owner != null)) {
            Payee payee = new Payee();
            payee.setName(name);
            payee.setOwner(owner);
            payee.persist();
            owner.getPayees().add(payee);
            owner.merge();
        }
    }

    @Override
    public byte[] backup(final UserDetails user) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (user != null) {
            Set<Account> accounts = user.getAccounts();
            if (!CollectionUtils.isEmpty(accounts)) {
                ZipOutputStream zos = new ZipOutputStream(bos);
                for (Account account : accounts) {
                    NameValuePair<String, String> qifs = qif(account);
                    ZipEntry cash = new ZipEntry(account.getName() + ".qif");
                    zos.putNextEntry(cash);
                    copy(StringUtils.asStream(qifs.getKey()), zos);
                    zos.closeEntry();
                    if (qifs.getValue() != null) {
                        ZipEntry investment = new ZipEntry(account.getName() + "-investment.qif");
                        zos.putNextEntry(investment);
                        copy(StringUtils.asStream(qifs.getValue()), zos);
                        zos.closeEntry();
                    }
                }
                zos.close();
            }
        }
        return bos.toByteArray();
    }

    protected NameValuePair<String, String> qif(final Account account) {
        StringBuilder cash = new StringBuilder(account.asQIF());
        StringBuilder investment = new StringBuilder("!Type:Invst\n");
        Set<AccountTransaction> transactions = account.getTransactions();
        if (!CollectionUtils.isEmpty(transactions)) {
            for (AccountTransaction transaction : transactions) {
                (transaction.isInvestmentTransaction() ? investment : cash).append(transaction.asQIF(messageSource));
            }
        }
        return new NameValuePair<String, String>(cash.toString(), investment.length() > 15 ? investment.toString() : null);
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        try {
            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } finally {
            in.close();
        }
    }
}
