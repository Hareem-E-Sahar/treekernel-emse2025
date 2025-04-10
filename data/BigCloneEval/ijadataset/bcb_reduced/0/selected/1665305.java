package org.fudaa.dodico.reflux.io;

import gnu.trove.TDoubleArrayList;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Properties;
import org.fudaa.ctulu.CtuluGNULineNumberReader;
import org.fudaa.ctulu.CtuluLibFile;
import org.fudaa.ctulu.CtuluLibMessage;
import org.fudaa.dodico.commun.DodicoLib;
import org.fudaa.dodico.fortran.FileOpReadCharSimpleAbstract;
import org.fudaa.dodico.fortran.FortranReader;
import org.fudaa.dodico.h2d.resource.H2dResource;

/**
 * @author Fred Deniger
 * @version $Id: RefluxSolutionNewReader.java,v 1.10 2006-09-19 14:45:48 deniger Exp $
 */
public class RefluxSolutionNewReader extends FileOpReadCharSimpleAbstract {

    File f_;

    CtuluGNULineNumberReader specReader_;

    boolean suivi_;

    RefluxSolutionFileFormat version_;

    /**
   * @param _ft la version utilisee
   */
    public RefluxSolutionNewReader(final RefluxSolutionFileFormat _ft) {
        version_ = _ft;
    }

    protected Object internalRead() {
        if ((f_ == null) || (in_ == null)) {
            analyze_.addFatalError(H2dResource.getS("Le flux est null"));
            return null;
        }
        final RefluxSolutionSequentielResult r = new RefluxSolutionSequentielResult();
        final File parent = f_.getParentFile();
        int nbNodeRead = 0;
        final File dat = new File(parent, CtuluLibFile.getSansExtension(f_.getName()) + ".dat");
        int nbNds = -1;
        if (dat.exists()) {
            final Properties prs = new Properties();
            try {
                final FileInputStream readDat = new FileInputStream(dat);
                prs.load(readDat);
                readDat.close();
            } catch (final FileNotFoundException e) {
                analyze_.manageException(e);
            } catch (final IOException e) {
                analyze_.manageException(e);
            }
            nbNds = Integer.parseInt(prs.getProperty("nbNoeuds", "-1"));
            r.typeProjet_ = Integer.parseInt(prs.getProperty("tpProbleme", "0"));
        }
        r.ft_ = version_;
        final TDoubleArrayList timeStep = new TDoubleArrayList();
        long lineLength = -1;
        long enteteEtapeLength;
        long delimLength;
        try {
            in_.setBlankZero(true);
            long debPos = getCurrentPosInReader();
            final String s = in_.readLine().trim();
            delimLength = getCurrentPosInReader() - debPos;
            r.delimLength_ = delimLength;
            final int delim = version_.getFirstLine();
            if (Integer.parseInt(s) != delim) {
                analyze_.addFatalError(DodicoLib.getS("Format du champ incorrect"), 1);
                return null;
            }
            final int[] fmtEtape = version_.getEtapeFormat();
            debPos = getCurrentPosInReader();
            in_.readFields(fmtEtape);
            enteteEtapeLength = getCurrentPosInReader() - debPos;
            r.enteteLength_ = enteteEtapeLength;
            timeStep.add(in_.doubleField(5));
            in_.readFields(version_.getNbVariableFormat());
            r.nbVar_ = in_.intField(1);
            if (r.nbVar_ <= 0) {
                analyze_.addFatalError(DodicoLib.getS("Format du champ incorrect"), in_.getLineNumber());
                return null;
            }
            debPos = getCurrentPosInReader();
            r.firstPost_ = debPos;
            String line = in_.readLine();
            if (line.charAt(version_.getFirstDataColLength() - 1) == '1') {
                r.sizeFirstCol_ = version_.getFirstDataColLength();
            } else {
                r.sizeFirstCol_ = version_.getFirstDataColDoubleLength();
            }
            line = line.trim();
            final String delimString = Integer.toString(delim);
            lineLength = getCurrentPosInReader() - debPos;
            long tempLength;
            int debTimeStep = 0;
            while (!delimString.equals(line)) {
                nbNodeRead++;
                if (nbNodeRead > 0 && lineLength > 0) {
                    tempLength = getCurrentPosInReader() - debPos;
                    if (tempLength != lineLength) {
                        if (!isSuivi()) {
                            analyze_.addFatalError(H2dResource.getS("longueur de ligne non constante ") + ": " + in_.getLineNumber(), in_.getLineNumber());
                        }
                        return null;
                    }
                }
                debPos = getCurrentPosInReader();
                line = in_.readLine().trim();
            }
            if ((nbNds > 0) && (nbNodeRead != nbNds)) {
                analyze_.addWarn(H2dResource.getS("Le nombre de noeuds annonc� par le fichier dat est incorrect"), -1);
            }
            r.nbPoint_ = nbNodeRead;
            if (progress_ != null) {
                progress_.setProgression(30);
            }
            final long skip = nbNodeRead * lineLength;
            in_.close();
            in_ = null;
            final FileInputStream stream = new FileInputStream(f_);
            final FileChannel ch = stream.getChannel();
            ch.position(getCurrentPosInReader());
            final ByteBuffer buf = ByteBuffer.allocate((int) enteteEtapeLength);
            for (int i = 0; i < fmtEtape.length - 1; i++) {
                debTimeStep += fmtEtape[i];
            }
            while (ch.position() < ch.size()) {
                buf.rewind();
                ch.read(buf);
                buf.rewind();
                final String str = new String(buf.array()).substring(debTimeStep);
                timeStep.add(Double.parseDouble(str));
                ch.position(ch.position() + skip + delimLength);
            }
        } catch (final EOFException e) {
            if (r.nbPoint_ != nbNodeRead) {
                r.nbPoint_ = nbNodeRead;
            }
            if (CtuluLibMessage.DEBUG) {
                CtuluLibMessage.debug(f_.getAbsolutePath() + " end of file");
            }
        } catch (final IllegalArgumentException e) {
            analyze_.manageException(e);
            return null;
        } catch (final IOException e) {
            analyze_.manageException(e);
        }
        r.timeStep_ = timeStep.toNativeArray();
        r.lineLength_ = lineLength;
        return r;
    }

    public boolean isSuivi() {
        return suivi_;
    }

    protected void processFile(final File _f) {
        f_ = _f;
    }

    /**
   * On utilise un reader sp�cial qui permet de conna�tre la position.
   */
    protected void setFile(final Reader _r) {
        specReader_ = new CtuluGNULineNumberReader(_r);
        in_ = new FortranReader(specReader_);
    }

    public void setSuivi(final boolean _suivi) {
        suivi_ = _suivi;
    }

    public long getCurrentPosInReader() {
        return specReader_.getPosFirstCharInBuffer() + specReader_.getCharInBuffer();
    }
}
