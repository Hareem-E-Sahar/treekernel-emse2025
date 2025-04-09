package org.iaccess.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.iaccess.accesscontrol.IAccessManager;
import org.iaccess.accesscontrol.IAccessRequest;

/**
 * Encapsulates the logic level computations. It functions as a wrapper to the DLV solver used
 * as an underlying engine for deduction and abduction reasonings. The configuration file is config_dlv.xml. All computations on the dlv system are
 * taken from the configuration file. Instead of recompilling the file, one can change some of the input parameters to DLV in the
 * configuration file. For example, from 'cautious' to 'brave' reasoning, as explained in the doDeduction() function.
 * <p> The class implements the interactive access control algorithm, function computeAccessDecision(),
 * and the stepwise disclosure of missing credentials, function doStepwiseDisclosure().
 * <p> Improvements on the interactive access control, and, on the complementing it, stepwise disclosure
 * have been done with respect to the published negotiation scheme in the JNSM paper.
 * <p> The improvements mainly concern the way disclosure policy is treated in order to avoid
 * endless loops. The last may occur if a client declines to present a missing credential that is not part of a solution for a resource, but is part
 * of a fine-grained disclosure control (stepwise computing).
 * For such cases, in the SetUp function we prepare the disclosure policy in a way of not chaining any credentials in heads of rules
 * that are part of the declined credentials. Now, during a stepwise negotiation/computing whatever a client declines to present
 * the access decision will take it into account, i.e. any decision for a missing set of credentials is computed as all credentials are
 * properly chained by the disclosure policy, and in accordance with declined credentials.
 * <p>Some more details are given for each of the functions in the class.
 * <p> ResultOfOperation class is used to interface the results of logic level computations and the negotiation level.
 * 
 * @see org.iaccess.logic.ResultOfOperation
 */
public class BasicAccessControlAlgorithm {

    private static final Logger LOGGER = Logger.getLogger(BasicAccessControlAlgorithm.class.getName());

    private boolean isRequestForCertificate = false;

    private String fileStr;

    private List<String> Cert_Presented, Cert_Declined, Cred_Declined, Cred_Presented;

    private IAccessRequest request;

    private File deductionPositiveFactsFile, deductionQueryFile, abductionHypothesesFile, abductionObservationsFile, stepwiseDisclosurePolicy, stepwiseDisclosurePolicyWithComplementRules, stepwiseDisclosurePolicyForOneStep, queryOneStep, oneStepHypotheses, oneStepObservations, requestAsFactFile;

    private DLVRunner dlvRunner = null;

    private LogicLayerConfiguration config = null;

    public BasicAccessControlAlgorithm(IAccessManager iAccessManager) {
        config = iAccessManager.getLogicLayerConfiguration();
        dlvRunner = iAccessManager.getDLVRunner();
    }

    /**
     * Deletes temporal files generated during internal computations.
     */
    public void deleteTempFiles() {
        try {
            if (deductionPositiveFactsFile != null && deductionPositiveFactsFile.exists()) deductionPositiveFactsFile.delete();
            if (deductionQueryFile != null && deductionQueryFile.exists()) deductionQueryFile.delete();
            if (abductionHypothesesFile != null && abductionHypothesesFile.exists()) abductionHypothesesFile.delete();
            if (abductionObservationsFile != null && abductionObservationsFile.exists()) abductionObservationsFile.delete();
            if (stepwiseDisclosurePolicy != null && stepwiseDisclosurePolicy.exists()) stepwiseDisclosurePolicy.delete();
            if (stepwiseDisclosurePolicyWithComplementRules != null && stepwiseDisclosurePolicyWithComplementRules.exists()) stepwiseDisclosurePolicyWithComplementRules.delete();
            if (stepwiseDisclosurePolicyForOneStep != null && stepwiseDisclosurePolicyForOneStep.exists()) stepwiseDisclosurePolicyForOneStep.delete();
            if (queryOneStep != null && queryOneStep.exists()) queryOneStep.delete();
            if (oneStepHypotheses != null && oneStepHypotheses.exists()) oneStepHypotheses.delete();
            if (oneStepObservations != null && oneStepObservations.exists()) oneStepObservations.delete();
            if (requestAsFactFile != null && requestAsFactFile.exists()) requestAsFactFile.delete();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "EXCEPTION: While deleting temp files from the working directory:  {0}", e.toString());
        }
    }

    /**
     * A supporting function for string matching and replacement.
     */
    private String replacePatStr2Str(String patternStr, String inputStr, String replaceStr) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.replaceAll(replaceStr);
    }

    /**
     * A supporting function for intelligent Policy parsing process.
     * All proper logic programming rules are split into array of strings, but
     * without the ending dot ".".
     *
     */
    private String[] parsePolicyAllRulesAndFacts(String policy) throws Exception {
        List<String> allRulesAndFacts = new ArrayList<String>();
        int lastSuccessfulIndex = 0;
        boolean allowNextIndexOfQuotes = true;
        boolean ignoreDot = false;
        boolean missingClosingQuotes = false;
        Pattern pattDot = Pattern.compile("\\.");
        Pattern pattQuotes = Pattern.compile("\"");
        Matcher matcherDot = pattDot.matcher(policy);
        Matcher matcherQuotes = pattQuotes.matcher(policy);
        while (matcherDot.find()) {
            if (ignoreDot && (matcherDot.start() < matcherQuotes.start())) continue;
            ignoreDot = false;
            while (true) {
                if (allowNextIndexOfQuotes) if (!matcherQuotes.find()) break;
                if (matcherQuotes.start() < matcherDot.start()) {
                    missingClosingQuotes = false;
                    while (true) {
                        if (!matcherQuotes.find()) {
                            missingClosingQuotes = true;
                            break;
                        }
                        if (!policy.startsWith("\\", matcherQuotes.start() - 1)) break;
                    }
                    if (missingClosingQuotes) throw new Exception("Unable to parse. Missing a closing quotes (\") in disclosure policy files.");
                    allowNextIndexOfQuotes = true;
                    if (matcherQuotes.start() > matcherDot.start()) {
                        ignoreDot = true;
                        break;
                    }
                } else {
                    allowNextIndexOfQuotes = false;
                    break;
                }
            }
            if (!ignoreDot) {
                allRulesAndFacts.add(policy.substring(lastSuccessfulIndex, matcherDot.start()));
                lastSuccessfulIndex = matcherDot.end();
            }
        }
        if (allRulesAndFacts.isEmpty()) throw new Exception("No rules parsed from disclosure policy files.");
        return allRulesAndFacts.toArray(new String[allRulesAndFacts.size()]);
    }

    /**
     * Use this function before to run computeAccessDecision() and doStewiseDisclosure() functions.
     * This functions prepares the disclosure policy and the set of presented and declined credentials for
     * appropriate computations. We use credentials and certificates templates to wrap around the values of credentials
     * from negotiation layer to the policy layer, transparently to the negotiation.
     */
    public ResultOfOperation setUp(IAccessRequest request, String serviceRequest, List<String> CP, List<String> CN, long negStartTime) {
        ResultOfOperation resSetUp = new ResultOfOperation();
        try {
            Cert_Presented = new ArrayList<String>();
            Cred_Presented = new ArrayList<String>();
            Cert_Declined = new ArrayList<String>();
            Cred_Declined = new ArrayList<String>();
            String InternalDelimiter = "\",\"";
            int firstIndex = -1;
            if (CP != null) {
                for (int i = 0; i < CP.size(); i++) {
                    firstIndex = CP.get(i).indexOf(InternalDelimiter);
                    if (firstIndex == -1) throw new Exception("Could not identify input credential at logic level SetUp function!");
                    if (firstIndex == CP.get(i).lastIndexOf(InternalDelimiter)) {
                        Cert_Presented.add(CP.get(i));
                    } else {
                        Cred_Presented.add(CP.get(i));
                    }
                }
            }
            if (CN != null) {
                firstIndex = -1;
                for (int i = 0; i < CN.size(); i++) {
                    firstIndex = CN.get(i).indexOf(InternalDelimiter);
                    if (firstIndex == -1) throw new Exception("Could not identify the credential: " + CN.get(i).toString());
                    if (firstIndex == CN.get(i).lastIndexOf(InternalDelimiter)) Cert_Declined.add(CN.get(i)); else Cred_Declined.add(CN.get(i));
                }
            }
            if ((request.sessionID != null) && (request.serviceRequest != null)) {
                this.request = request;
            } else {
                throw new Exception("Service request is not specified!");
            }
            fileStr = this.request.sessionID;
            deductionPositiveFactsFile = new File(config.WorkingDirectory + config.OSFileDelimiter + "_deductionPositiveFacts_" + fileStr);
            deductionQueryFile = new File(config.WorkingDirectory + config.OSFileDelimiter + "_deductionQuery_" + fileStr);
            FileOutputStream deductionPositiveFactsStream = new FileOutputStream(deductionPositiveFactsFile);
            FileOutputStream deductionQueryStream = new FileOutputStream(deductionQueryFile);
            String tempStr = null;
            String predicateTimeNow = (new Date()).toString().toLowerCase();
            predicateTimeNow = predicateTimeNow.replaceAll("\\s+", ",");
            predicateTimeNow = predicateTimeNow.replace(':', ',');
            predicateTimeNow = config.PredicateName4SystemTimeNow + "(" + predicateTimeNow + ").";
            deductionPositiveFactsStream.write(predicateTimeNow.getBytes());
            String predicateTNTimeByNow = null;
            long currentSystemTime = System.currentTimeMillis();
            if (negStartTime >= currentSystemTime) predicateTNTimeByNow = "0"; else predicateTNTimeByNow = String.valueOf(currentSystemTime - negStartTime);
            predicateTNTimeByNow = config.PredicateName4TNTimeByNow + "(" + predicateTNTimeByNow + ").";
            deductionPositiveFactsStream.write(predicateTNTimeByNow.getBytes());
            for (int i = 0; i < Cert_Presented.size(); i++) {
                tempStr = config.PatternContent.matcher(config.DLVTemplate4Certificate).replaceAll(Cert_Presented.get(i).toString());
                Cert_Presented.set(i, tempStr);
                deductionPositiveFactsStream.write((tempStr + ".").getBytes());
            }
            for (int i = 0; i < Cred_Presented.size(); i++) {
                tempStr = config.PatternContent.matcher(config.DLVTemplate4Credential).replaceAll(Cred_Presented.get(i).toString());
                deductionPositiveFactsStream.write((tempStr + ".").getBytes());
            }
            for (int i = 0; i < Cert_Declined.size(); i++) {
                tempStr = config.PatternContent.matcher(config.DLVTemplate4Certificate).replaceAll(Cert_Declined.get(i).toString());
                Cert_Declined.set(i, tempStr);
            }
            firstIndex = this.request.serviceRequest.indexOf(InternalDelimiter);
            if (firstIndex == this.request.serviceRequest.lastIndexOf(InternalDelimiter)) isRequestForCertificate = true; else isRequestForCertificate = false;
            String requestAsFact = "";
            requestAsFactFile = new File(config.WorkingDirectory + config.OSFileDelimiter + "_requestAsFact_" + fileStr);
            if (serviceRequest != null) {
                requestAsFact = config.PatternContent.matcher(config.DLVTemplate4ServiceRequest).replaceAll(serviceRequest) + ".";
                FileOutputStream requestAsFactFileStream = new FileOutputStream(requestAsFactFile);
                requestAsFactFileStream.write(requestAsFact.getBytes());
                requestAsFactFileStream.close();
            }
            if (this.request.isService) {
                deductionQueryStream.write((config.PatternContent.matcher(config.DLVTemplate4Service).replaceAll(this.request.serviceRequest) + "?").getBytes());
            } else {
                if (isRequestForCertificate) {
                    deductionQueryStream.write((config.PatternContent.matcher(config.DLVTemplate4Certificate).replaceAll(this.request.serviceRequest) + "?").getBytes());
                } else {
                    deductionQueryStream.write((config.PatternContent.matcher(config.DLVTemplate4Credential).replaceAll(this.request.serviceRequest) + "?").getBytes());
                }
            }
            deductionPositiveFactsStream.close();
            deductionQueryStream.close();
            String readLine = "", allPolicyInteractionRulesAndFacts = "";
            Pattern pattRule = Pattern.compile(":-");
            for (int i = 0; i < config.DisclosurePolicyFiles.length; i++) {
                BufferedReader currentlyOpenFileReader = new BufferedReader(new FileReader(config.DisclosurePolicyFiles[i]));
                while ((readLine = currentlyOpenFileReader.readLine()) != null) {
                    if (readLine.length() == 0 || readLine.startsWith("%")) continue;
                    allPolicyInteractionRulesAndFacts += readLine;
                }
                currentlyOpenFileReader.close();
            }
            allPolicyInteractionRulesAndFacts = replacePatStr2Str("\\s*+\\,\\s*+", allPolicyInteractionRulesAndFacts, ",");
            allPolicyInteractionRulesAndFacts = replacePatStr2Str("\\s*+\\(\\s*+", allPolicyInteractionRulesAndFacts, "(");
            allPolicyInteractionRulesAndFacts = replacePatStr2Str("\\s*+\\)\\s*+", allPolicyInteractionRulesAndFacts, ")");
            allPolicyInteractionRulesAndFacts = replacePatStr2Str("\\s*+:-\\s*+", allPolicyInteractionRulesAndFacts, ":-");
            allPolicyInteractionRulesAndFacts = replacePatStr2Str("\\s*+\\)\\.\\s*+", allPolicyInteractionRulesAndFacts, ").");
            String pattCredStr = config.PatternRightBrace.matcher(config.DLVTemplate4Credential).replaceAll(config.PatternStr2RightBraceStr);
            pattCredStr = config.PatternLeftBrace.matcher(pattCredStr).replaceAll(config.PatternStr2LeftBraceStr);
            pattCredStr = config.PatternContent.matcher(pattCredStr).replaceAll(".+?");
            String pattCertStr = config.PatternRightBrace.matcher(config.DLVTemplate4Certificate).replaceAll(config.PatternStr2RightBraceStr);
            pattCertStr = config.PatternLeftBrace.matcher(pattCertStr).replaceAll(config.PatternStr2LeftBraceStr);
            pattCertStr = config.PatternContent.matcher(pattCertStr).replaceAll(".+?");
            Pattern pattCred = Pattern.compile(pattCredStr), pattCert = Pattern.compile(pattCertStr);
            String[] allRulesAndFacts = parsePolicyAllRulesAndFacts(allPolicyInteractionRulesAndFacts);
            for (int i = 0; i < allRulesAndFacts.length; i++) {
                String tmpStr = allRulesAndFacts[i];
                tmpStr = replacePatStr2Str("\\s+", tmpStr, "");
                if (tmpStr.length() == 0) continue;
                Matcher matcherRule = pattRule.matcher(allRulesAndFacts[i]);
                Matcher matcherCred;
                if (matcherRule.find()) {
                    String[] HeadAndBodyOfARule = pattRule.split(allRulesAndFacts[i]);
                    matcherCred = pattCred.matcher(HeadAndBodyOfARule[0]);
                    if (matcherCred.find()) {
                        HeadAndBodyOfARule[0] = matcherCred.replaceAll(config.DLVOneStepPrefix4Credentials + "$0");
                    }
                    matcherCred = pattCert.matcher(HeadAndBodyOfARule[0]);
                    if (matcherCred.find()) {
                        HeadAndBodyOfARule[0] = matcherCred.replaceAll(config.DLVOneStepPrefix4Credentials + "$0");
                    }
                    allRulesAndFacts[i] = HeadAndBodyOfARule[0] + ":-" + HeadAndBodyOfARule[1] + ".";
                } else {
                    matcherCred = pattCred.matcher(allRulesAndFacts[i]);
                    if (matcherCred.find()) {
                        allRulesAndFacts[i] = matcherCred.replaceAll(config.DLVOneStepPrefix4Credentials + "$0");
                    }
                    matcherCred = pattCert.matcher(allRulesAndFacts[i]);
                    if (matcherCred.find()) {
                        allRulesAndFacts[i] = matcherCred.replaceAll(config.DLVOneStepPrefix4Credentials + "$0");
                    }
                    allRulesAndFacts[i] += ".";
                }
            }
            allPolicyInteractionRulesAndFacts = "";
            for (int i = 0; i < allRulesAndFacts.length; i++) {
                allPolicyInteractionRulesAndFacts += allRulesAndFacts[i];
            }
            stepwiseDisclosurePolicy = new File(config.WorkingDirectory + config.OSFileDelimiter + "_stepwiseDisclosurePolicy_" + fileStr);
            FileOutputStream stepwiseDisclosurePolicyFileStream = new FileOutputStream(stepwiseDisclosurePolicy);
            stepwiseDisclosurePolicyFileStream.write((requestAsFact + allPolicyInteractionRulesAndFacts).getBytes());
            stepwiseDisclosurePolicyFileStream.close();
            stepwiseDisclosurePolicyWithComplementRules = new File(config.WorkingDirectory + config.OSFileDelimiter + "_stepwiseDisclosurePolicyWithComplementRules_" + fileStr);
            FileOutputStream stepwiseDisclosurePolicyWithComplementRulesFileStream = new FileOutputStream(stepwiseDisclosurePolicyWithComplementRules);
            for (int j = 0; j < Cred_Declined.size(); j++) {
                String tmp = config.PatternContent.matcher(config.DLVTemplate4Credential).replaceAll(Cred_Declined.get(j).toString());
                tmp = config.Prefix4DeclinedCred + config.DLVOneStepPrefix4Credentials + tmp + ".";
                stepwiseDisclosurePolicyWithComplementRulesFileStream.write(tmp.getBytes());
            }
            for (int j = 0; j < Cert_Declined.size(); j++) {
                String tmp = config.Prefix4DeclinedCred + config.DLVOneStepPrefix4Credentials + Cert_Declined.get(j) + ".";
                stepwiseDisclosurePolicyWithComplementRulesFileStream.write(tmp.getBytes());
            }
            stepwiseDisclosurePolicyWithComplementRulesFileStream.write((requestAsFact + allPolicyInteractionRulesAndFacts).getBytes());
            stepwiseDisclosurePolicyWithComplementRulesFileStream.write((config.Rule4NotChainCred).getBytes());
            stepwiseDisclosurePolicyWithComplementRulesFileStream.write((config.Rule4NotChainCert).getBytes());
            stepwiseDisclosurePolicyWithComplementRulesFileStream.close();
            resSetUp.setIsSuccessful(true);
            resSetUp.setOperationResult("Successfully executed SetUp function!");
        } catch (Exception e) {
            resSetUp.setIsSuccessful(false);
            resSetUp.setOperationResult(e.toString());
        }
        return resSetUp;
    }

    /**
         * Computes if an access policy together with a set of presented credentials grants a request.
         * It first checks if the access policy is consistent according to the client's presented credentials
         * and then, it checks if the request is in a stable model of the access policy.
         * We use 'cautious' reasoning for computing if the request is in all stable models of the access policy.
         * It is important to note that 'brave' reasoning (consult DLV reasoner for details) my result in inconsistent
         * interactions of missing credentials within a wrong/different stable model of the access policy.
         * <p>We notice that abduction reasoning of DLV (to be fully confirmed) uses 'brave' reasoning for
         * computing missing sets of credentials, and that's why the access policy is also to be a stratified logic program.
         * In this way you guarantee that abduction and deduction in the interactive access control will have consistent
         * behavior. <p>One should experiment if having an access policy with more than one stable models the abduction
         * behavior is consistent with respect to a given service request, which appears in one of the stable
         * models of the access policy. Hope, it is clear.
         */
    private ResultOfOperation doDeduction() {
        ResultOfOperation resdoDeduction = new ResultOfOperation();
        try {
            String[] ExecStrConsistency = null;
            if (this.request.isService) {
                ExecStrConsistency = new String[3 + config.DeductionServicePolicyFiles.length];
                ExecStrConsistency[0] = config.DLVDirectory + config.OSFileDelimiter + config.DLVExecutableName;
                ExecStrConsistency[1] = "-silent";
                ExecStrConsistency[2] = deductionPositiveFactsFile.getCanonicalPath();
                for (int i = 0; i < config.DeductionServicePolicyFiles.length; i++) ExecStrConsistency[3 + i] = config.DeductionServicePolicyFiles[i];
            } else {
                ExecStrConsistency = new String[4 + config.DeductionServicePolicyFiles.length];
                ExecStrConsistency[0] = config.DLVDirectory + config.OSFileDelimiter + config.DLVExecutableName;
                ExecStrConsistency[1] = "-silent";
                ExecStrConsistency[2] = deductionPositiveFactsFile.getCanonicalPath();
                ExecStrConsistency[3] = requestAsFactFile.getCanonicalPath();
                for (int i = 0; i < config.DeductionCredentialPolicyFiles.length; i++) ExecStrConsistency[4 + i] = config.DeductionCredentialPolicyFiles[i];
            }
            String[] dlvOutput = dlvRunner.runDLV(ExecStrConsistency);
            if ((dlvOutput == null) || (dlvOutput[0].trim().isEmpty())) {
                resdoDeduction.setIsSuccessful(true);
                resdoDeduction.setOperationResult(new Integer(0));
                return resdoDeduction;
            }
            ArrayList<String> execStr = new ArrayList<String>();
            String[] cmdOpt = config.PatternWhiteSpace.split(config.DLVDeductionCommandPromptOptions);
            if (this.request.isService) {
                execStr.add(config.DLVDirectory + config.OSFileDelimiter + config.DLVExecutableName);
                for (int i = 0; i < cmdOpt.length; i++) execStr.add(cmdOpt[i]);
                for (int i = 0; i < config.DeductionServicePolicyFiles.length; i++) execStr.add(config.DeductionServicePolicyFiles[i]);
                execStr.add(deductionPositiveFactsFile.getCanonicalPath());
                execStr.add(deductionQueryFile.getCanonicalPath());
            } else {
                execStr.add(config.DLVDirectory + config.OSFileDelimiter + config.DLVExecutableName);
                for (int i = 0; i < cmdOpt.length; i++) execStr.add(cmdOpt[i]);
                for (int i = 0; i < config.DeductionServicePolicyFiles.length; i++) execStr.add(config.DeductionServicePolicyFiles[i]);
                execStr.add(deductionPositiveFactsFile.getCanonicalPath());
                execStr.add(deductionQueryFile.getCanonicalPath());
                execStr.add(requestAsFactFile.getCanonicalPath());
                for (int i = 0; i < config.DeductionCredentialPolicyFiles.length; i++) execStr.add(config.DeductionCredentialPolicyFiles[i]);
            }
            dlvOutput = dlvRunner.runDLV(execStr.toArray(new String[execStr.size()]));
            Pattern patternCredentialsDelimiter = Pattern.compile("\\s*" + config.CredentialsDelimiter + "\\s*");
            String[] keywords = patternCredentialsDelimiter.split(config.DLVKeywords4SUccessfulDeduction);
            int countSucessKeywords = 0;
            boolean grantAccess = false;
            if (dlvOutput != null) {
                for (int j = 0; j < dlvOutput.length; j++) {
                    for (int i = 0; i < keywords.length; i++) {
                        Pattern pat = Pattern.compile(keywords[i]);
                        Matcher matcher = pat.matcher(dlvOutput[j]);
                        if (matcher.find()) {
                            countSucessKeywords++;
                            if (countSucessKeywords == keywords.length) grantAccess = true;
                        }
                    }
                    if (grantAccess) break;
                }
            }
            resdoDeduction.setIsSuccessful(true);
            if (grantAccess) resdoDeduction.setOperationResult(new Integer(1)); else resdoDeduction.setOperationResult(new Integer(0));
            return resdoDeduction;
        } catch (Exception e) {
            resdoDeduction.setIsSuccessful(false);
            resdoDeduction.setOperationResult(e.toString());
            return resdoDeduction;
        }
    }

    /**
         * Computes the set of disclosable credentials in accordance with the
         * interactive access control algorithm, implemented in function computeAccessDecision().
         * The function assumes that the disclosure policy is a stratified logic programm, i.e., it has unique
         * stable model. So, the function takes ONLY the FIRST stable model of the disclosure policy (union
         * the presented credentials).
         * Initially we wanted to compute a set of missing credentials among more than one sets of
         * disclosable credentials, but it was against the theory, that is, the disclosure policy is
         * to be a stratified logic program and, as such, it has only one stable model -- one set
         * of disclosable credentials.
         */
    private ResultOfOperation computeAbducibles() {
        ResultOfOperation resComputeAbducibles = new ResultOfOperation();
        LinkedList abducibleModels = new LinkedList();
        String readLine = null, declinedCredPattern = null, presentedCredPattern = null;
        try {
            ArrayList<String> runDLVInput = new ArrayList<String>();
            runDLVInput.add(config.DLVDirectory + config.OSFileDelimiter + config.DLVExecutableName);
            String[] tt = config.PatternWhiteSpace.split(config.DLVComputeAbduciblesCommandPromptOptions);
            for (int i = 0; i < tt.length; i++) runDLVInput.add(tt[i]);
            runDLVInput.add(deductionPositiveFactsFile.getCanonicalPath());
            runDLVInput.add(stepwiseDisclosurePolicyWithComplementRules.getCanonicalPath());
            String[] dlvOutput = dlvRunner.runDLV(runDLVInput.toArray(new String[runDLVInput.size()]));
            if (dlvOutput != null) readLine = dlvOutput[0];
            if (readLine == null || readLine.equals("") || readLine.equals("{}")) {
                resComputeAbducibles.setIsSuccessful(true);
                resComputeAbducibles.setOperationResult(null);
                return resComputeAbducibles;
            }
            readLine = replacePatStr2Str("\\{", readLine, "");
            readLine = replacePatStr2Str("\\}", readLine, ".");
            readLine = replacePatStr2Str("\\)\\,", readLine, ").");
            for (int i = 0; i < Cred_Presented.size(); i++) {
                presentedCredPattern = config.PatternContent.matcher(config.DLVExtendedTemplate4Credential).replaceAll(Cred_Presented.get(i).toString() + "\\,[0-9]+?");
                presentedCredPattern = config.PatternRightBrace.matcher(presentedCredPattern).replaceAll(config.PatternStr2RightBraceStr);
                presentedCredPattern = config.PatternLeftBrace.matcher(presentedCredPattern).replaceAll(config.PatternStr2LeftBraceStr);
                readLine = replacePatStr2Str(presentedCredPattern + "\\.", readLine, "");
            }
            for (int i = 0; i < Cert_Presented.size(); i++) {
                presentedCredPattern = config.PatternRightBrace.matcher(Cert_Presented.get(i).toString()).replaceAll(config.PatternStr2RightBraceStr);
                presentedCredPattern = config.PatternLeftBrace.matcher(presentedCredPattern).replaceAll(config.PatternStr2LeftBraceStr);
                readLine = replacePatStr2Str(presentedCredPattern + "\\.", readLine, "");
            }
            for (int i = 0; i < Cred_Declined.size(); i++) {
                declinedCredPattern = config.PatternContent.matcher(config.DLVExtendedTemplate4Credential).replaceAll(Cred_Declined.get(i).toString() + "\\,[0-9]+?");
                declinedCredPattern = config.PatternRightBrace.matcher(declinedCredPattern).replaceAll(config.PatternStr2RightBraceStr);
                declinedCredPattern = config.PatternLeftBrace.matcher(declinedCredPattern).replaceAll(config.PatternStr2LeftBraceStr);
                readLine = replacePatStr2Str(declinedCredPattern + "\\.", readLine, "");
            }
            for (int i = 0; i < Cert_Declined.size(); i++) {
                declinedCredPattern = config.PatternRightBrace.matcher(Cert_Declined.get(i).toString()).replaceAll(config.PatternStr2RightBraceStr);
                declinedCredPattern = config.PatternLeftBrace.matcher(declinedCredPattern).replaceAll(config.PatternStr2LeftBraceStr);
                readLine = replacePatStr2Str(declinedCredPattern + "\\.", readLine, "");
            }
            abducibleModels.add(readLine);
            resComputeAbducibles.setIsSuccessful(true);
            resComputeAbducibles.setOperationResult(abducibleModels);
            return resComputeAbducibles;
        } catch (Exception e) {
            resComputeAbducibles.setIsSuccessful(false);
            resComputeAbducibles.setOperationResult(e.toString());
            return resComputeAbducibles;
        }
    }

    /**
     * Extracts a missing set of credentials to a form of the negotiation layer, that is, the function strips down
     * credential and certificate notations and returns only their values. For example, "cred(john_couk,social_worker,california_ca)" is
     * transformed to "john_couk,social_worker,california_ca".
     */
    private ResultOfOperation extractMinimalCredentials(String[] inputCredentials) {
        ResultOfOperation resextCredContModel = new ResultOfOperation();
        List<String> CertCont = new ArrayList<String>(), CredCont = new ArrayList<String>();
        try {
            String newCredPattStr = config.PatternLeftBrace.matcher(config.DLVExtendedTemplate4Credential).replaceAll(config.PatternStr2LeftBraceStr);
            newCredPattStr = config.PatternRightBrace.matcher(newCredPattStr).replaceAll(config.PatternStr2RightBraceStr);
            newCredPattStr = config.PatternContent.matcher(newCredPattStr).replaceAll("(.+?)\\,[0-9]+?");
            String newCertPattStr = config.PatternLeftBrace.matcher(config.DLVTemplate4Certificate).replaceAll(config.PatternStr2LeftBraceStr);
            newCertPattStr = config.PatternRightBrace.matcher(newCertPattStr).replaceAll(config.PatternStr2RightBraceStr);
            newCertPattStr = config.PatternContent.matcher(newCertPattStr).replaceAll("(.+?)");
            Pattern PattCred = Pattern.compile(newCredPattStr);
            Matcher credMatcher;
            Pattern PattCert = Pattern.compile(newCertPattStr);
            Matcher certMatcher;
            for (int i = 0; i < inputCredentials.length; i++) {
                credMatcher = PattCred.matcher(inputCredentials[i]);
                if (credMatcher.find()) CredCont.add(credMatcher.group(1));
                certMatcher = PattCert.matcher(inputCredentials[i]);
                if (certMatcher.find()) CertCont.add(certMatcher.group(1));
            }
            CertCont.addAll(CredCont);
            resextCredContModel.setIsSuccessful(true);
            resextCredContModel.setOperationResult(CertCont);
            return resextCredContModel;
        } catch (Exception e) {
            resextCredContModel.setIsSuccessful(false);
            resextCredContModel.setOperationResult(e.toString());
            return resextCredContModel;
        }
    }

    /**
         * Computes a minimal set of missing credentials. It is used by the
         * core access control algorithm implemented as function computeAccessDecision();
         * It has an input list of sets of disclosable credentials, BUT only first element is actually
         * used. Initially we wanted to compute a set of missing credentials among more than one sets of
         * disclosable credentials, but it was against the theory, that is, the disclosure policy is
         * to be a stratified logic program and, as such, it has only one stable model -- one set
         * of disclosable credentials.
         */
    private ResultOfOperation doAbduction(List<String> abducibleSets) {
        ResultOfOperation resdoAbduction = new ResultOfOperation();
        try {
            abductionObservationsFile = new File(config.WorkingDirectory + config.OSFileDelimiter + "_abductionObservations_" + fileStr + config.observationsFileExt);
            FileOutputStream abductionObservationsStream = new FileOutputStream(abductionObservationsFile);
            if (this.request.isService) abductionObservationsStream.write((config.PatternContent.matcher(config.DLVTemplate4Service).replaceAll(this.request.serviceRequest) + ".").getBytes()); else {
                if (isRequestForCertificate) abductionObservationsStream.write((config.PatternContent.matcher(config.DLVTemplate4Certificate).replaceAll(this.request.serviceRequest) + ".").getBytes()); else abductionObservationsStream.write((config.PatternContent.matcher(config.DLVTemplate4Credential).replaceAll(this.request.serviceRequest) + ".").getBytes());
            }
            abductionObservationsStream.close();
            String pattRoleWeightStr = config.PatternLeftBrace.matcher(config.DLVExtendedTemplate4Credential).replaceAll(config.PatternStr2LeftBraceStr);
            pattRoleWeightStr = config.PatternRightBrace.matcher(pattRoleWeightStr).replaceAll(config.PatternStr2RightBraceStr);
            pattRoleWeightStr = config.PatternContent.matcher(pattRoleWeightStr).replaceAll(".+?\\,([0-9]+?)");
            Pattern pattExtractRoleWeightFromExtendedCredential = Pattern.compile(pattRoleWeightStr);
            int beginIndex = "Diagnosis: ".length();
            String AbductionHypFileStr = config.WorkingDirectory + config.OSFileDelimiter + "_abductionHypotheses_" + fileStr + config.hypothesesFileExt;
            abductionHypothesesFile = new File(AbductionHypFileStr);
            ArrayList<String> DLVStr = new ArrayList<String>();
            DLVStr.add(config.DLVDirectory + config.OSFileDelimiter + config.DLVExecutableName);
            String[] cmdOpt = config.PatternWhiteSpace.split(config.DLVAbductionCommandPromptOptions);
            for (int i = 0; i < cmdOpt.length; i++) DLVStr.add(cmdOpt[i]);
            DLVStr.add(deductionPositiveFactsFile.getCanonicalPath());
            DLVStr.add(abductionObservationsFile.getCanonicalPath());
            DLVStr.add(abductionHypothesesFile.getCanonicalPath());
            if (this.request.isService) {
                for (int i = 0; i < config.AbductionPolicyFiles.length; i++) DLVStr.add(config.AbductionPolicyFiles[i]);
            } else {
                DLVStr.add(requestAsFactFile.getCanonicalPath());
                for (int i = 0; i < config.AbductionCredentialPolicyFiles.length; i++) DLVStr.add(config.AbductionCredentialPolicyFiles[i]);
            }
            FileOutputStream abductionHypothesesStream = new FileOutputStream(abductionHypothesesFile);
            abductionHypothesesStream.write(abducibleSets.get(0).getBytes());
            abductionHypothesesStream.close();
            String[] dlvOutput = dlvRunner.runDLV(DLVStr.toArray(new String[DLVStr.size()]));
            String ReadLine = null;
            if (dlvOutput != null) ReadLine = dlvOutput[0];
            if (ReadLine == null || ReadLine.equals("") || ReadLine.equals("Diagnosis:")) {
                resdoAbduction.setIsSuccessful(true);
                resdoAbduction.setOperationResult(null);
                return resdoAbduction;
            }
            ReadLine = ReadLine.substring(beginIndex);
            int minSetCardinality = 0, currentSetCardinality = 0, minSumOfWeights = 0, currentSumOfWeights = 0;
            String minSetStr = ReadLine;
            Matcher matchSetCardinality = config.Patt4CountingSetCardinality.matcher(ReadLine);
            while (matchSetCardinality.find()) minSetCardinality++;
            minSetCardinality++;
            Matcher matchRoleWeight = pattExtractRoleWeightFromExtendedCredential.matcher(ReadLine);
            while (matchRoleWeight.find()) minSumOfWeights += Integer.parseInt(matchRoleWeight.group(1));
            for (int indx = 1; indx < dlvOutput.length; indx++) {
                ReadLine = dlvOutput[indx];
                ReadLine = ReadLine.substring(beginIndex);
                currentSetCardinality = 0;
                currentSumOfWeights = 0;
                matchSetCardinality = config.Patt4CountingSetCardinality.matcher(ReadLine);
                while (matchSetCardinality.find()) currentSetCardinality++;
                currentSetCardinality++;
                matchRoleWeight = pattExtractRoleWeightFromExtendedCredential.matcher(ReadLine);
                while (matchRoleWeight.find()) currentSumOfWeights += Integer.parseInt(matchRoleWeight.group(1));
                if (config.CriterionRoleMinimalitySetCardinality) {
                    if (currentSumOfWeights < minSumOfWeights) {
                        minSumOfWeights = currentSumOfWeights;
                        minSetCardinality = currentSetCardinality;
                        minSetStr = ReadLine;
                    }
                    if (currentSumOfWeights == minSumOfWeights) {
                        if (currentSetCardinality < minSetCardinality) {
                            minSumOfWeights = currentSumOfWeights;
                            minSetCardinality = currentSetCardinality;
                            minSetStr = ReadLine;
                        }
                    }
                } else {
                    if (currentSetCardinality < minSetCardinality) {
                        minSumOfWeights = currentSumOfWeights;
                        minSetCardinality = currentSetCardinality;
                        minSetStr = ReadLine;
                    }
                    if (currentSetCardinality == minSetCardinality) {
                        if (currentSumOfWeights < minSumOfWeights) {
                            minSumOfWeights = currentSumOfWeights;
                            minSetCardinality = currentSetCardinality;
                            minSetStr = ReadLine;
                        }
                    }
                }
            }
            Pattern Patt4SplittingSet = Pattern.compile("\\)\\s*+");
            String[] st = Patt4SplittingSet.split(minSetStr);
            for (int i = 0; i < st.length; i++) {
                st[i] += ")";
            }
            return extractMinimalCredentials(st);
        } catch (Exception e) {
            resdoAbduction.setIsSuccessful(false);
            resdoAbduction.setOperationResult(e.toString());
            return resdoAbduction;
        }
    }

    /**
         * Performs the stepwise computing of missing credentials.
         * Important, before to run this function you have to use the SetUp() function of this class.
         * <p>The function takes as input a set of missing credentials and returns a next step
         * of credentials (among the missing ones) to be asked to an opponent.
         * Similarly in computing to the immediate consequence operator in logic programming.
         * The disclosure policy (a copy of it) is internally manipulated to disclose
         * a next set of credentials. We use internally negation as failure on a set of credentials
         * to make the computing.
         * <p>Important, we guarantee that if the disclosure policy is a stratified logic program,
         * the internal transformations preserve the disclosure policy stratified. That is, we use
         * negation as failure on facts internally generated and added to the policy, and a single stable model
         * of the policy.
         */
    public ResultOfOperation doStepwiseDisclosure(List<String> MissingCreds) {
        ResultOfOperation resStepwiseDisclosure = new ResultOfOperation();
        try {
            String ReadLine = null;
            String InternalDelimiter = ",";
            String TempStr = null;
            int FirstIndex = -1;
            if (MissingCreds == null) {
                deleteTempFiles();
                resStepwiseDisclosure.setIsSuccessful(true);
                resStepwiseDisclosure.setOperationResult(null);
                return resStepwiseDisclosure;
            }
            List<String> CM = new ArrayList<String>();
            CM.addAll(MissingCreds);
            for (int i = 0; i < CM.size(); i++) {
                FirstIndex = CM.get(i).indexOf(InternalDelimiter);
                if (FirstIndex == -1) throw new Exception("Could not identify a valid credential format in CM of the stepwise function: <" + CM.get(i).toString() + ">.");
                if (FirstIndex == CM.get(i).lastIndexOf(InternalDelimiter)) {
                    TempStr = config.PatternContent.matcher(config.DLVTemplate4Certificate).replaceAll(CM.get(i).toString());
                    CM.set(i, TempStr);
                } else {
                    TempStr = config.PatternContent.matcher(config.DLVTemplate4Credential).replaceAll(CM.get(i).toString());
                    CM.set(i, TempStr);
                }
            }
            ArrayList<String> DLVstr = new ArrayList<String>();
            DLVstr.add(config.DLVDirectory + config.OSFileDelimiter + config.DLVExecutableName);
            String[] cmdOpt = config.PatternWhiteSpace.split(config.DLVOneStepDeductionCommandPromptOptions);
            for (int i = 0; i < cmdOpt.length; i++) DLVstr.add(cmdOpt[i]);
            DLVstr.add(deductionPositiveFactsFile.getCanonicalPath());
            DLVstr.add(stepwiseDisclosurePolicy.getCanonicalPath());
            String[] dlvOutput = dlvRunner.runDLV(DLVstr.toArray(new String[DLVstr.size()]));
            String presentedCredPattern, declinedCredPattern;
            if (dlvOutput != null) ReadLine = dlvOutput[0];
            if (ReadLine == null || ReadLine.equals("") || ReadLine.equals("{}")) {
                deleteTempFiles();
                resStepwiseDisclosure.setIsSuccessful(true);
                resStepwiseDisclosure.setOperationResult(null);
                return resStepwiseDisclosure;
            }
            ReadLine = replacePatStr2Str("\\{", ReadLine, "");
            ReadLine = replacePatStr2Str("\\}", ReadLine, ".");
            ReadLine = replacePatStr2Str("\\)\\,", ReadLine, ").");
            ReadLine = replacePatStr2Str(config.DLVOneStepPrefix4Credentials, ReadLine, "");
            for (int i = 0; i < Cred_Presented.size(); i++) {
                presentedCredPattern = config.PatternContent.matcher(config.DLVExtendedTemplate4Credential).replaceAll(Cred_Presented.get(i).toString() + "\\,[0-9]+?");
                presentedCredPattern = config.PatternRightBrace.matcher(presentedCredPattern).replaceAll(config.PatternStr2RightBraceStr);
                presentedCredPattern = config.PatternLeftBrace.matcher(presentedCredPattern).replaceAll(config.PatternStr2LeftBraceStr);
                ReadLine = replacePatStr2Str(presentedCredPattern + "\\.", ReadLine, "");
            }
            for (int i = 0; i < Cert_Presented.size(); i++) {
                presentedCredPattern = config.PatternRightBrace.matcher(Cert_Presented.get(i).toString()).replaceAll(config.PatternStr2RightBraceStr);
                presentedCredPattern = config.PatternLeftBrace.matcher(presentedCredPattern).replaceAll(config.PatternStr2LeftBraceStr);
                ReadLine = replacePatStr2Str(presentedCredPattern + "\\.", ReadLine, "");
            }
            for (int i = 0; i < Cred_Declined.size(); i++) {
                declinedCredPattern = config.PatternContent.matcher(config.DLVExtendedTemplate4Credential).replaceAll(Cred_Declined.get(i).toString() + "\\,[0-9]+?");
                declinedCredPattern = config.PatternRightBrace.matcher(declinedCredPattern).replaceAll(config.PatternStr2RightBraceStr);
                declinedCredPattern = config.PatternLeftBrace.matcher(declinedCredPattern).replaceAll(config.PatternStr2LeftBraceStr);
                ReadLine = replacePatStr2Str(declinedCredPattern + "\\.", ReadLine, "");
            }
            for (int i = 0; i < Cert_Declined.size(); i++) {
                declinedCredPattern = config.PatternRightBrace.matcher(Cert_Declined.get(i).toString()).replaceAll(config.PatternStr2RightBraceStr);
                declinedCredPattern = config.PatternLeftBrace.matcher(declinedCredPattern).replaceAll(config.PatternStr2LeftBraceStr);
                ReadLine = replacePatStr2Str(declinedCredPattern + "\\.", ReadLine, "");
            }
            String ReadLineTemp = replacePatStr2Str("\\s*+", ReadLine, "");
            if (ReadLineTemp.length() == 0) {
                deleteTempFiles();
                resStepwiseDisclosure.setIsSuccessful(true);
                resStepwiseDisclosure.setOperationResult(null);
                return resStepwiseDisclosure;
            }
            ReadLine = replacePatStr2Str("\\)\\.\\s*+", ReadLine, ").");
            Pattern pattSplitOutput = Pattern.compile("\\)\\.");
            String[] OneStepCredentials = pattSplitOutput.split(ReadLine);
            for (int i = 0; i < OneStepCredentials.length; i++) OneStepCredentials[i] += ")";
            stepwiseDisclosurePolicyForOneStep = new File(config.WorkingDirectory + config.OSFileDelimiter + "_stepwiseDisclosurePolicyForOneStep_" + fileStr);
            FileOutputStream stepwiseDisclosurePolicyForOneStepFileStream = new FileOutputStream(stepwiseDisclosurePolicyForOneStep);
            for (int j = 0; j < Cred_Declined.size(); j++) {
                String tmp = config.PatternContent.matcher(config.DLVTemplate4Credential).replaceAll(Cred_Declined.get(j).toString());
                tmp = config.Prefix4DeclinedCred + config.DLVOneStepPrefix4Credentials + tmp + ".";
                stepwiseDisclosurePolicyForOneStepFileStream.write(tmp.getBytes());
            }
            for (int j = 0; j < Cert_Declined.size(); j++) {
                String tmp = config.Prefix4DeclinedCred + config.DLVOneStepPrefix4Credentials + Cert_Declined.get(j) + ".";
                stepwiseDisclosurePolicyForOneStepFileStream.write(tmp.getBytes());
            }
            String newCredPattStr = config.PatternLeftBrace.matcher(config.DLVExtendedTemplate4Credential).replaceAll(config.PatternStr2LeftBraceStr);
            newCredPattStr = config.PatternRightBrace.matcher(newCredPattStr).replaceAll(config.PatternStr2RightBraceStr);
            newCredPattStr = config.PatternContent.matcher(newCredPattStr).replaceAll("(.+?)\\,[0-9]+?");
            Pattern pattExtCred = Pattern.compile(newCredPattStr);
            for (int i = 0; i < OneStepCredentials.length; i++) {
                Matcher matchedExtCred = pattExtCred.matcher(OneStepCredentials[i]);
                String toStore = null;
                if (matchedExtCred.find()) toStore = config.PatternContent.matcher(config.DLVTemplate4Credential).replaceAll(matchedExtCred.group(1)); else toStore = OneStepCredentials[i];
                String tmp = config.Prefix4DeclinedCred + config.DLVOneStepPrefix4Credentials + toStore + ".";
                stepwiseDisclosurePolicyForOneStepFileStream.write(tmp.getBytes());
            }
            stepwiseDisclosurePolicyForOneStepFileStream.write(config.Rule4NotChainCred.getBytes());
            stepwiseDisclosurePolicyForOneStepFileStream.write(config.Rule4NotChainCert.getBytes());
            stepwiseDisclosurePolicyForOneStepFileStream.close();
            String query;
            if (CM == null || CM.isEmpty()) query = "q."; else {
                query = "q :- ";
                for (int i = 0; i < CM.size(); i++) {
                    if (i + 1 == CM.size()) query = query + CM.get(i) + "."; else query = query + CM.get(i) + ",";
                }
            }
            queryOneStep = new File(config.WorkingDirectory + config.OSFileDelimiter + "_queryOneStep_" + fileStr);
            FileOutputStream queryOneStepFileStream = new FileOutputStream(queryOneStep);
            queryOneStepFileStream.write((query).getBytes());
            queryOneStepFileStream.close();
            oneStepObservations = new File(config.WorkingDirectory + config.OSFileDelimiter + "_oneStepObservations_" + fileStr + config.observationsFileExt);
            FileOutputStream oneStepObservationsFileStream = new FileOutputStream(oneStepObservations);
            oneStepObservationsFileStream.write(("q.").getBytes());
            oneStepObservationsFileStream.close();
            oneStepHypotheses = new File(config.WorkingDirectory + config.OSFileDelimiter + "_oneStepHypotheses_" + fileStr + config.hypothesesFileExt);
            FileOutputStream oneStepHypothesesFileStream = new FileOutputStream(oneStepHypotheses);
            oneStepHypothesesFileStream.write((ReadLine).getBytes());
            oneStepHypothesesFileStream.close();
            ArrayList<String> DLVStr = new ArrayList<String>();
            DLVStr.add(config.DLVDirectory + config.OSFileDelimiter + config.DLVExecutableName);
            cmdOpt = config.PatternWhiteSpace.split(config.DLVAbductionOneStepCommandPromptOptions);
            for (int i = 0; i < cmdOpt.length; i++) DLVStr.add(cmdOpt[i]);
            DLVStr.add(deductionPositiveFactsFile.getCanonicalPath());
            DLVStr.add(queryOneStep.getCanonicalPath());
            DLVStr.add(stepwiseDisclosurePolicy.getCanonicalPath());
            DLVStr.add(stepwiseDisclosurePolicyForOneStep.getCanonicalPath());
            for (int i = 0; i < config.OneStepAbductionPolicyFiles.length; i++) DLVStr.add(config.OneStepAbductionPolicyFiles[i]);
            DLVStr.add(oneStepHypotheses.getCanonicalPath());
            DLVStr.add(oneStepObservations.getCanonicalPath());
            dlvOutput = dlvRunner.runDLV(DLVStr.toArray(new String[DLVStr.size()]));
            String pattRoleWeightStr = config.PatternLeftBrace.matcher(config.DLVExtendedTemplate4Credential).replaceAll(config.PatternStr2LeftBraceStr);
            pattRoleWeightStr = config.PatternRightBrace.matcher(pattRoleWeightStr).replaceAll(config.PatternStr2RightBraceStr);
            pattRoleWeightStr = config.PatternContent.matcher(pattRoleWeightStr).replaceAll(".+?\\,([0-9]+?)");
            Pattern pattExtractRoleWeightFromExtendedCredential = Pattern.compile(pattRoleWeightStr);
            int beginIndex = "Diagnosis: ".length();
            boolean flag4existingSet = false;
            String minSetStr = new String();
            int minSetCardinalityInt = 0, currentSetCardinality = 0, minSumOfWeights = 0, currentSumOfWeights = 0;
            if (dlvOutput != null) {
                for (int indx = 0; indx < dlvOutput.length; indx++) {
                    ReadLine = dlvOutput[indx];
                    if (ReadLine.equals("Diagnosis:")) {
                        deleteTempFiles();
                        resStepwiseDisclosure.setIsSuccessful(true);
                        resStepwiseDisclosure.setOperationResult(null);
                        return resStepwiseDisclosure;
                    }
                    currentSetCardinality = 0;
                    currentSumOfWeights = 0;
                    ReadLine = ReadLine.substring(beginIndex);
                    Matcher matchSetCardinality = config.Patt4CountingSetCardinality.matcher(ReadLine);
                    while (matchSetCardinality.find()) currentSetCardinality++;
                    currentSetCardinality++;
                    Matcher matchRoleWeight = pattExtractRoleWeightFromExtendedCredential.matcher(ReadLine);
                    while (matchRoleWeight.find()) currentSumOfWeights += Integer.parseInt(matchRoleWeight.group(1));
                    if (!flag4existingSet) {
                        minSetCardinalityInt = currentSetCardinality;
                        minSumOfWeights = currentSumOfWeights;
                        minSetStr = ReadLine;
                    } else {
                        if (config.CriterionRoleMinimalitySetCardinality) {
                            if (currentSumOfWeights < minSumOfWeights) {
                                minSetCardinalityInt = currentSetCardinality;
                                minSumOfWeights = currentSumOfWeights;
                                minSetStr = ReadLine;
                            }
                            if (currentSumOfWeights == minSumOfWeights) {
                                if (currentSetCardinality < minSetCardinalityInt) {
                                    minSetCardinalityInt = currentSetCardinality;
                                    minSumOfWeights = currentSumOfWeights;
                                    minSetStr = ReadLine;
                                }
                            }
                        } else {
                            if (currentSetCardinality < minSetCardinalityInt) {
                                minSetCardinalityInt = currentSetCardinality;
                                minSumOfWeights = currentSumOfWeights;
                                minSetStr = ReadLine;
                            }
                            if (currentSetCardinality == minSetCardinalityInt) {
                                if (currentSumOfWeights < minSumOfWeights) {
                                    minSetCardinalityInt = currentSetCardinality;
                                    minSumOfWeights = currentSumOfWeights;
                                    minSetStr = ReadLine;
                                }
                            }
                        }
                    }
                    flag4existingSet = true;
                }
            }
            if (!flag4existingSet) {
                deleteTempFiles();
                resStepwiseDisclosure.setIsSuccessful(true);
                resStepwiseDisclosure.setOperationResult(null);
                return resStepwiseDisclosure;
            } else {
                deleteTempFiles();
                Pattern Patt4SplittingSet = Pattern.compile("\\)\\s*+");
                String[] st = Patt4SplittingSet.split(minSetStr);
                for (int i = 0; i < st.length; i++) st[i] += ")";
                return extractMinimalCredentials(st);
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "IACCESS: EXCEPTION: BasicAccessControlAlgorithm.doStepwiseDisclosure(): {0}", e.toString());
            deleteTempFiles();
            resStepwiseDisclosure.setIsSuccessful(false);
            resStepwiseDisclosure.setOperationResult(e.toString());
            return resStepwiseDisclosure;
        }
    }

    /** 
         * Implements the interactive access control algorithm.
         * It computes grant/deny/MissingCredentials acccording to the
         * interactive algorithm.
         * Important: before to invoke this function you have to run the SetUp() function of this class.
         * <p><b>1. Deduction:</b> Computes if an access policy together with a set of presented credentials grants a request.
         * It first checks if the access policy is consistent according to the client's presented credentials
         * and then, it checks if the request is in a stable model of the access policy.
         * We use 'cautious' reasoning for computing if the request is in all stable models of the access policy.
         * It is important to note that 'brave' reasoning (consult DLV reasoner for details) my result in inconsistent
         * interactions of missing credentials within a wrong/different stable model of the access policy.
         * <p>We notice that abduction reasoning of DLV (to be fully confirmed) uses 'brave' reasoning for
         * computing missing sets of credentials, and that's why the access policy is also to be a stratified logic program.
         * In this way you guarantee that abduction and deduction in the interactive access control will have consistent
         * behavior. <p>One should experiment if having an access policy with more than one stable models the abduction
         * behavior is consistent with respect to a given service request, which appears in one of the stable
         * models of the access policy. Hope, it is clear.
         * <p><b>2. Disclosable credentials: </b> Computes the set of disclosable credentials in accordance with the
         * interactive access control algorithm, implemented in function computeAccessDecision().
         * The function assumes that the disclosure policy is a stratified logic programm, i.e., it has unique
         * stable model. So, the function takes ONLY the FIRST stable model of the disclosure policy union
         * the presented credentials.
         * <p><b>3. Abduction: </b> Computes a minimal set of missing credentials. It is used by the
         * core access control algorithm implemened as function computeAccessDecision();
         * It has an input list of sets of disclosable credentials, BUT only first element is actually
         * used. Initially we wanted to compute a set of missing credentials among more than one sets of
         * disclosable credentials, but it was against the theory, that is, the disclosure policy is
         * to be a stratified logic program and, as such, it has only one stable model -- one set
         * of disclosable credentials.
         */
    public ResultOfOperation computeAccessDecision() {
        String msg = "computeAccessDecision() -> ";
        ResultOfOperation resComputeAccessDecision = new ResultOfOperation();
        ResultOfOperation resDeduction = doDeduction();
        if (resDeduction.isSuccessful()) {
            if (((Integer) resDeduction.getOperationResult()).compareTo(new Integer(1)) == 0) {
                deleteTempFiles();
                LOGGER.log(Level.INFO, "{0}Success", msg);
                return resDeduction;
            }
            ResultOfOperation rescomAbducibles = computeAbducibles();
            if (rescomAbducibles.isSuccessful()) {
                if (rescomAbducibles.getOperationResult() == null) {
                    deleteTempFiles();
                    resComputeAccessDecision.setIsSuccessful(true);
                    resComputeAccessDecision.setOperationResult(new Integer(0));
                    LOGGER.log(Level.INFO, "{0}Deny, there is no disclosable", msg);
                    return resComputeAccessDecision;
                }
                ResultOfOperation resAbduction = doAbduction((LinkedList) rescomAbducibles.getOperationResult());
                if (resAbduction.isSuccessful()) {
                    if (resAbduction.getOperationResult() == null) {
                        deleteTempFiles();
                        resComputeAccessDecision.setIsSuccessful(true);
                        resComputeAccessDecision.setOperationResult(new Integer(0));
                        LOGGER.log(Level.INFO, "{0}Deny, no missing credentials", msg);
                        return resComputeAccessDecision;
                    } else {
                        LOGGER.log(Level.INFO, "{0}Deny, but there are missing credentials", msg);
                        return resAbduction;
                    }
                } else {
                    LOGGER.log(Level.INFO, "IACCESS: EXCEPTION: BasicAccessControlAlgorithm.doAbduction(): {0}", resAbduction.getOperationResult().toString());
                    deleteTempFiles();
                    return resAbduction;
                }
            } else {
                LOGGER.log(Level.INFO, "IACCESS: EXCEPTION: BasicAccessControlAlgorithm.computeAbducibles(): {0}", rescomAbducibles.getOperationResult().toString());
                deleteTempFiles();
                return rescomAbducibles;
            }
        } else {
            LOGGER.log(Level.INFO, "IACCESS: EXCEPTION: BasicAccessControlAlgorithm.doDeduction(): {0}", resDeduction.getOperationResult().toString());
            deleteTempFiles();
            return resDeduction;
        }
    }
}
