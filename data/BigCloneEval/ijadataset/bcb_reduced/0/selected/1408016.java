package org.phramer.v1.decoder.loader;

import info.olteanu.interfaces.*;
import info.olteanu.utils.*;
import info.olteanu.utils.io.*;
import info.olteanu.utils.lang.*;
import info.olteanu.utils.remoteservices.cache.*;
import info.olteanu.utils.remoteservices.client.*;
import java.io.*;
import java.lang.reflect.*;
import org.phramer.*;
import org.phramer.v1.decoder.*;
import org.phramer.v1.decoder.cost.*;
import org.phramer.v1.decoder.instrumentation.*;
import org.phramer.v1.decoder.lm.*;
import org.phramer.v1.decoder.lm.context.*;
import org.phramer.v1.decoder.lm.ngram.*;
import org.phramer.v1.decoder.lm.preprocessor.*;
import org.phramer.v1.decoder.lm.remote.*;
import org.phramer.v1.decoder.loader.custom.*;
import org.phramer.v1.decoder.table.*;
import org.phramer.v1.decoder.token.*;

public class LoaderSimpleImpl implements AllLoader {

    public ContextLoader getCloneForConcurrencyCL() throws PhramerException {
        return this;
    }

    public FutureCostLoader getCloneForConcurrencyFCL() throws PhramerException {
        return this;
    }

    public LMLoader getCloneForConcurrencyLML() throws PhramerException {
        return this;
    }

    public TTLoader getCloneForConcurrencyTTL() throws PhramerException {
        return this;
    }

    public ContextComparator getContextComparator(int contextLength) {
        return new DefaultContextComparator();
    }

    public LMContext getNullContext(int lmContextLength) {
        if (lmContextLength == 2) return new ThreeGramLMContext();
        return new NGramLMContext(lmContextLength);
    }

    public LMPreprocessor loadPreprocessor(String lmFileName, int index) throws IOException, PhramerException {
        return new LMPreprocessorWord(StringFilter.VOID);
    }

    public LanguageModelIf loadLanguageModel(String lmURL, String encodingTextFile, int index) throws IOException, PhramerException {
        if (lmURL.startsWith("remote:")) {
            boolean monoLine = false;
            boolean doConversion = false;
            int n = -1;
            if (lmURL.indexOf('#') != -1) {
                String params = lmURL.substring(lmURL.indexOf('#'));
                if (params.contains("/log10")) doConversion = true;
                if (params.contains("/mono")) monoLine = true;
                if (params.contains("/n")) {
                    String nn = params.substring(params.indexOf("/n") + 2);
                    if (nn.indexOf('/') != -1) nn = nn.substring(0, nn.indexOf('/'));
                    n = Integer.parseInt(nn);
                }
            }
            return new RemoteLanguageModel(new AutoFlushCachedLineRemoteService(new RemoteConnector(lmURL, true, true, monoLine), 30000, 1000), doConversion, n);
        }
        if (lmURL.startsWith("sqlite:")) {
            lmURL = lmURL.substring("sqlite:".length());
            return new CachedLM(getSQLiteLanguageModel(lmURL), 50000, 5000);
        }
        boolean featMemory = true;
        boolean featVocabulary = false;
        boolean featBinary = false;
        boolean featNoCache = false;
        boolean featFastCall = false;
        Boolean featFastHash = false;
        int featCacheSize = 100000;
        {
            boolean newFeature;
            do {
                newFeature = false;
                if (lmURL.startsWith("fast:")) {
                    newFeature = true;
                    featFastCall = true;
                    featNoCache = true;
                } else if (lmURL.startsWith("fasthash:")) {
                    newFeature = true;
                    featFastHash = true;
                } else if (lmURL.startsWith("memory:")) {
                    newFeature = true;
                    featMemory = true;
                } else if (lmURL.startsWith("vocabulary:")) {
                    newFeature = true;
                    featVocabulary = true;
                } else if (lmURL.startsWith("binary:")) {
                    newFeature = true;
                    featBinary = true;
                } else if (lmURL.startsWith("nocache:")) {
                    newFeature = true;
                    featNoCache = true;
                } else if (lmURL.startsWith("cachesize:")) {
                    newFeature = true;
                    lmURL = StringTools.substringAfter(lmURL, ":");
                    featCacheSize = Integer.parseInt(StringTools.substringBefore(lmURL, ":"));
                }
                if (newFeature) lmURL = StringTools.substringAfter(lmURL, ":");
            } while (newFeature);
        }
        BackOffLM lm = featVocabulary ? (featFastHash ? new VocabularyFastHashBackOffLM(featFastCall) : new VocabularyBackOffLM(featFastCall)) : new SimpleBackOffLM();
        if (featBinary) lm.loadBinary(IOTools.getInputStream(lmURL), 0); else lm.loadArpa(new InputStreamReader(IOTools.getInputStream(lmURL), encodingTextFile));
        if (featNoCache) return lm; else if (lm.isVocabularyBasedLM()) return new VocabularyCachedLM(lm, featCacheSize, featCacheSize / 20); else return new CachedLM(lm, featCacheSize, featCacheSize / 20);
    }

    public TranslationTable loadTranslationTable(TokenBuilder tokenBuilder, String ttURL, String encodingTextFile, int ttLimit, int maxPhraseLength, double ttThreshold, double ttTresholdWeights[], boolean storeDetails) throws IOException, PhramerException {
        if (ttURL.startsWith("remote:")) return new RemoteTranslationTable(new AutoFlushCachedLineRemoteService(new RemoteConnector(ttURL, true, true, false), 2000, 500), Constants.TYPE_PHARAOH, new EFProcessorSimple(tokenBuilder), ttLimit, ttThreshold, ttTresholdWeights);
        if (ttURL.startsWith("remoteb:")) return new CachedTranslationTable(new BinaryRemoteTranslationTable(StringTools.substringAfter(ttURL, "/"), StringTools.substringBefore(ttURL, "/"), encodingTextFile, tokenBuilder), 1000, 50);
        if (ttURL.startsWith("sqlite:")) {
            String dbName = ttURL.substring("sqlite:".length());
            boolean directionFE = true;
            if (dbName.startsWith("direct:")) {
                directionFE = true;
                dbName = dbName.substring("direct:".length());
            } else if (dbName.startsWith("reverse:")) {
                directionFE = false;
                dbName = dbName.substring("reverse:".length());
            }
            return new CachedTranslationTable(getSQLiteTranslationTable(dbName, directionFE, tokenBuilder, ttLimit, ttThreshold, ttTresholdWeights), 500, 30);
        }
        MutableInt type = new MutableInt(0);
        MutableBool nio = new MutableBool();
        ttURL = parseMemoryTT(ttURL, type, nio);
        if (nio.value) {
            return new CachedTranslationTable(new NioBufferMemoryTranslationTable(ttURL, encodingTextFile, tokenBuilder, new EFProcessorSimple(tokenBuilder)), 1000, 50);
        }
        return new MemoryTranslationTable(IOTools.getInputStream(ttURL), encodingTextFile, type.value, new EFProcessorSimple(tokenBuilder), ttLimit, maxPhraseLength, ttThreshold, ttTresholdWeights, storeDetails);
    }

    public static String parseMemoryTT(String ttURL, MutableInt type, MutableBool nio) {
        boolean featMemory = true;
        boolean featWordAlign = false;
        boolean featBinary = false;
        boolean featSorted = false;
        boolean featNIO = false;
        {
            boolean newFeature;
            do {
                newFeature = false;
                if (ttURL.startsWith("nio:")) {
                    newFeature = true;
                    featNIO = true;
                } else if (ttURL.startsWith("memory:")) {
                    newFeature = true;
                    featMemory = true;
                } else if (ttURL.startsWith("wordalign:")) {
                    newFeature = true;
                    featWordAlign = true;
                } else if (ttURL.startsWith("binary:")) {
                    newFeature = true;
                    featBinary = true;
                } else if (ttURL.startsWith("sorted:")) {
                    newFeature = true;
                    featSorted = true;
                }
                if (newFeature) ttURL = StringTools.substringAfter(ttURL, ":");
            } while (newFeature);
        }
        int typeX = Constants.TYPE_PHARAOH;
        if (featBinary) typeX |= Constants.FEATURE_BINARY;
        if (featSorted) typeX |= Constants.FEATURE_SORTED;
        if (featWordAlign) typeX |= Constants.FEATURE_WORD_ALIGNMENT;
        type.value = typeX;
        nio.value = featNIO;
        return ttURL;
    }

    public static boolean isMemoryTT(String ttURL) {
        return !(ttURL.startsWith("sqlite:") || ttURL.startsWith("remote:"));
    }

    private static NgramLanguageModel getSQLiteLanguageModel(String lmURL) throws IOException, PhramerException {
        try {
            Class c = Class.forName("org.phramer.v1.decoder.lm.ngram.SQLiteBackOffLM");
            Constructor constr = c.getConstructors()[0];
            Object[] params = { lmURL };
            try {
                return (NgramLanguageModel) constr.newInstance(params);
            } catch (InstantiationException e) {
                throw new Error(e);
            } catch (IllegalAccessException e) {
                throw new Error(e);
            } catch (IllegalArgumentException e) {
                throw new Error(e);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof IOException) throw (IOException) e.getTargetException();
                throw new PhramerException(e);
            }
        } catch (ClassNotFoundException e) {
            throw new Error("phramer sqlite edition is not in classpath");
        }
    }

    private TranslationTable getSQLiteTranslationTable(String dbName, boolean directionFE, TokenBuilder tokenBuilder, int ttLimit, double ttThreshold, double ttTresholdWeights[]) throws PhramerException, IOException {
        try {
            Class c = Class.forName("org.phramer.v1.decoder.table.SQLiteTranslationTable");
            Constructor constr = c.getConstructors()[0];
            Object[] params = { dbName, directionFE, Constants.TYPE_PHARAOH, new EFProcessorSimple(tokenBuilder), ttLimit, ttThreshold, ttTresholdWeights };
            try {
                return (TranslationTable) constr.newInstance(params);
            } catch (InstantiationException e) {
                throw new Error(e);
            } catch (IllegalAccessException e) {
                throw new Error(e);
            } catch (IllegalArgumentException e) {
                throw new Error(e);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof IOException) throw (IOException) e.getTargetException();
                if (e.getTargetException() instanceof PhramerException) throw (PhramerException) e.getTargetException();
                throw new PhramerException(e);
            }
        } catch (ClassNotFoundException e) {
            throw new Error("phramer sqlite edition is not in classpath");
        }
    }

    public FutureCostEstimator getCostCalculator(PhramerConfig config, Instrument instrument) throws PhramerException {
        if (config.futureCostCalculatorClass == null) {
            if (config.compatibilityLevel >= 1000200) return new PharaohDFutureCostCalculator(config, instrument);
            return new PharaohFutureCostCalculator(config, instrument);
        }
        Object[] args = { config, instrument };
        try {
            return (FutureCostEstimator) Class.forName(config.futureCostCalculatorClass).getConstructors()[0].newInstance(args);
        } catch (IllegalAccessException e) {
            throw new PhramerException(e);
        } catch (IllegalArgumentException e) {
            throw new PhramerException(e);
        } catch (ClassNotFoundException e) {
            throw new PhramerException(e);
        } catch (InvocationTargetException e) {
            throw new PhramerException(e);
        } catch (InstantiationException e) {
            throw new PhramerException(e);
        } catch (SecurityException e) {
            throw new PhramerException(e);
        }
    }
}
