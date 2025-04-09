package mediaframe.mpeg4.audio.AAC;

import java.io.IOException;

public final class Dolby_Adapt {

    static final int NORM_TYPE = 0;

    static final int START_TYPE = 1;

    static final int SHORT_TYPE = 2;

    static final int STOP_TYPE = 3;

    static final int N_SHORT_IN_START = 4;

    static final int START_OFFSET = 0;

    static final int SHORT_IN_START_OFFSET = 5;

    static final int N_SHORT_IN_STOP = 3;

    static final int STOP_OFFSET = 3;

    static final int SHORT_IN_STOP_OFFSET = 0;

    static final int N_SHORT_IN_4STOP = 4;

    static final int BLOCK_LEN_LONG = 1024;

    static final int BLOCK_LEN_SHORT = 128;

    static final int NWINLONG = BLOCK_LEN_LONG;

    static final int ALFALONG = 4;

    static final int NWINSHORT = BLOCK_LEN_SHORT;

    static final int ALFASHORT = 7;

    /** flat params */
    static final int NWINFLAT = NWINLONG;

    /** Advanced flat params */
    static final int NWINADV = NWINLONG - NWINSHORT;

    static final int NFLAT = (NWINFLAT - NWINSHORT) / 2;

    static final int NADV0 = (NWINADV - NWINSHORT) / 2;

    static final int WS_FHG = 0;

    static final int WS_DOLBY = 1;

    static final int N_WINDOW_SHAPES = 2;

    static final int WT_LONG = 0;

    static final int WT_SHORT = 1;

    static final int WT_FLAT = 2;

    static final int WT_ADV = 3;

    static final int N_WINDOW_TYPES = 4;

    /** ADVanced transform types */
    static final int LONG_BLOCK = 0;

    static final int START_BLOCK = 1;

    static final int SHORT_BLOCK = 2;

    static final int STOP_BLOCK = 3;

    static final int START_ADV_BLOCK = 4;

    static final int STOP_ADV_BLOCK = 5;

    static final int START_FLAT_BLOCK = 6;

    static final int STOP_FLAT_BLOCK = 7;

    static final int N_BLOCK_TYPES = 8;

    /** Advanced window sequence (frame) types */
    static final int ONLY_LONG = 0;

    static final int LONG_START = 1;

    static final int LONG_STOP = 2;

    static final int SHORT_START = 3;

    static final int SHORT_STOP = 4;

    static final int EIGHT_SHORT = 5;

    static final int SHORT_EXT_STOP = 6;

    static final int NINE_SHORT = 7;

    static final int OLD_START = 8;

    static final int OLD_STOP = 9;

    static final int N_WINDOW_SEQUENCES = 10;

    private boolean dolbyShortOffset = true;

    private float[] transBuff = new float[2 * BLOCK_LEN_LONG];

    private float[] timeOut = new float[BLOCK_LEN_LONG];

    private float[] fhg_long = new float[NWINLONG];

    private float[] fhg_short = new float[NWINSHORT];

    private float[] fhg_edler = new float[NWINLONG];

    private float[] dol_edler = new float[NWINLONG];

    private float[] fhg_adv = new float[NWINADV];

    private float[] dol_adv = new float[NWINADV];

    private float[][][] windowPtr = { { fhg_long, Tables.dol_long }, { fhg_short, fhg_short }, { fhg_edler, fhg_edler }, { fhg_adv, fhg_adv } };

    private int[] windowLeng = { NWINLONG, NWINSHORT, NWINLONG, NWINADV };

    /*****************************************************************************
	*
	*	freq2time_adapt
	*	transform freq. domain data to time domain.  
	*	Overlap and add transform output to recreate time sequence.
	*	Blocks composed of multiple segments (i.e. all but long) have 
	*	  input spectrums interleaved.
	*	input: see below
	*	output: see below
	*	local static:
	*	  timeBuff		time domain data fifo
	*	globals: none
	*
	*****************************************************************************/
    void freq2time_adapt(byte blockType, Wnd_Shape wnd_shape, float[] freqIn, float[] timeBuff, float[] ftimeOut) throws IOException {
        int transBuffPtr = 0, timeBuffPtr = 0, destPtr = 0, srcPtr = 0;
        int i, j;
        switch(blockType) {
            case NORM_TYPE:
                blockType = ONLY_LONG;
                break;
            case START_TYPE:
                blockType = OLD_START;
                break;
            case SHORT_TYPE:
                blockType = EIGHT_SHORT;
                break;
            case STOP_TYPE:
                blockType = OLD_STOP;
                break;
            default:
                throw new IOException("dolby_adapt.c: Illegal block type " + blockType + " - aborting");
        }
        if (blockType == ONLY_LONG) {
            unfold(freqIn, srcPtr, transBuff, 1, BLOCK_LEN_LONG);
            ITransformBlock(transBuff, LONG_BLOCK, wnd_shape, timeBuff);
            transBuffPtr = 0;
            timeBuffPtr = 0;
            destPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeOut[destPtr++] = transBuff[transBuffPtr++] + timeBuff[timeBuffPtr++];
            }
            timeBuffPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeBuff[timeBuffPtr++] = transBuff[transBuffPtr++];
            }
        } else if (blockType == SHORT_START) {
            unfold(freqIn, srcPtr, transBuff, 1, (BLOCK_LEN_SHORT + BLOCK_LEN_LONG) / 2);
            ITransformBlock(transBuff, START_BLOCK, wnd_shape, timeBuff);
            transBuffPtr = 0;
            timeBuffPtr = 0;
            destPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeOut[destPtr++] = transBuff[transBuffPtr++] + timeBuff[timeBuffPtr++];
            }
            timeBuffPtr = 0;
            for (i = 0; i < BLOCK_LEN_SHORT; i++) {
                timeBuff[timeBuffPtr++] = transBuff[transBuffPtr++];
            }
            srcPtr = ((BLOCK_LEN_LONG + BLOCK_LEN_SHORT) / 2);
            timeBuffPtr = 0;
            for (i = 0; i < N_SHORT_IN_START; i++) {
                unfold(freqIn, srcPtr, transBuff, 1, BLOCK_LEN_SHORT);
                srcPtr += BLOCK_LEN_SHORT;
                ITransformBlock(transBuff, SHORT_BLOCK, wnd_shape, timeBuff);
                transBuffPtr = 0;
                for (j = 0; j < BLOCK_LEN_SHORT; j++) {
                    timeBuff[timeBuffPtr++] += transBuff[transBuffPtr++];
                }
                for (j = 0; j < BLOCK_LEN_SHORT; j++) {
                    timeBuff[timeBuffPtr++] = transBuff[transBuffPtr++];
                }
                timeBuffPtr -= BLOCK_LEN_SHORT;
            }
            dolbyShortOffset = true;
        } else if (blockType == EIGHT_SHORT) {
            if (dolbyShortOffset) destPtr = 0 + 4 * BLOCK_LEN_SHORT; else destPtr = 0 + (BLOCK_LEN_LONG - BLOCK_LEN_SHORT) / 2;
            for (i = 0; i < 8; i++) {
                unfold(freqIn, srcPtr, transBuff, 1, BLOCK_LEN_SHORT);
                srcPtr += BLOCK_LEN_SHORT;
                ITransformBlock(transBuff, SHORT_BLOCK, wnd_shape, timeBuff);
                transBuffPtr = 0;
                for (j = 0; j < BLOCK_LEN_SHORT; j++) {
                    timeBuff[destPtr++] += transBuff[transBuffPtr++];
                }
                for (j = 0; j < BLOCK_LEN_SHORT; j++) {
                    timeBuff[destPtr++] = transBuff[transBuffPtr++];
                }
                destPtr -= BLOCK_LEN_SHORT;
            }
            destPtr = 0;
            timeBuffPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeOut[destPtr++] = timeBuff[timeBuffPtr++];
            }
            destPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeBuff[destPtr++] = timeBuff[timeBuffPtr++];
            }
        } else if (blockType == SHORT_STOP) {
            destPtr = 4 * BLOCK_LEN_SHORT;
            srcPtr = 0;
            for (i = 0; i < N_SHORT_IN_STOP; i++) {
                unfold(freqIn, srcPtr, transBuff, 1, BLOCK_LEN_SHORT);
                srcPtr += BLOCK_LEN_SHORT;
                ITransformBlock(transBuff, SHORT_BLOCK, wnd_shape, timeBuff);
                transBuffPtr = 0;
                for (j = 0; j < BLOCK_LEN_SHORT; j++) {
                    timeBuff[destPtr++] += transBuff[transBuffPtr++];
                }
                for (j = 0; j < BLOCK_LEN_SHORT; j++) {
                    timeBuff[destPtr++] = transBuff[transBuffPtr++];
                }
                destPtr -= BLOCK_LEN_SHORT;
            }
            unfold(freqIn, srcPtr, transBuff, 1, (BLOCK_LEN_SHORT + BLOCK_LEN_LONG) / 2);
            ITransformBlock(transBuff, STOP_BLOCK, wnd_shape, timeBuff);
            transBuffPtr = 0;
            for (i = 0; i < BLOCK_LEN_SHORT; i++) {
                timeBuff[destPtr++] += transBuff[transBuffPtr++];
            }
            destPtr = 0;
            timeBuffPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeOut[destPtr++] = timeBuff[timeBuffPtr];
                timeBuff[timeBuffPtr++] = transBuff[transBuffPtr++];
            }
        } else if (blockType == LONG_START) {
            unfold(freqIn, srcPtr, transBuff, 1, 960);
            ITransformBlock(transBuff, START_ADV_BLOCK, wnd_shape, timeBuff);
            transBuffPtr = 0;
            timeBuffPtr = 0;
            destPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeOut[destPtr++] = transBuff[transBuffPtr++] + timeBuff[timeBuffPtr++];
            }
            timeBuffPtr = 0;
            for (i = 0; i < NWINADV; i++) {
                timeBuff[timeBuffPtr++] = transBuff[transBuffPtr++];
            }
            for (; i < (2 * (BLOCK_LEN_LONG)); i++) {
                timeBuff[timeBuffPtr++] = 0;
            }
        } else if (blockType == LONG_STOP) {
            unfold(freqIn, srcPtr, transBuff, 1, 960);
            ITransformBlock(transBuff, STOP_ADV_BLOCK, wnd_shape, timeBuff);
            transBuffPtr = 0;
            timeBuffPtr = 0;
            destPtr = 0;
            for (i = 0; i < (BLOCK_LEN_LONG - 896); i++) {
                timeOut[destPtr++] = timeBuff[timeBuffPtr++];
            }
            for (; i < BLOCK_LEN_LONG; i++) {
                timeOut[destPtr++] = transBuff[transBuffPtr++] + timeBuff[timeBuffPtr++];
            }
            timeBuffPtr = 0;
            for (; i < (2 * (BLOCK_LEN_LONG)); i++) {
                timeBuff[timeBuffPtr++] = transBuff[transBuffPtr++];
            }
        } else if (blockType == SHORT_EXT_STOP) {
            destPtr = 3 * BLOCK_LEN_SHORT;
            for (i = 0; i < 4; i++) {
                unfold(freqIn, srcPtr, transBuff, 1, BLOCK_LEN_SHORT);
                srcPtr += BLOCK_LEN_SHORT;
                ITransformBlock(transBuff, SHORT_BLOCK, wnd_shape, timeBuff);
                transBuffPtr = 0;
                for (j = 0; j < BLOCK_LEN_SHORT; j++) {
                    timeBuff[destPtr++] += transBuff[transBuffPtr++];
                }
                for (j = 0; j < BLOCK_LEN_SHORT; j++) {
                    timeBuff[destPtr++] = transBuff[transBuffPtr++];
                }
                destPtr -= BLOCK_LEN_SHORT;
            }
            unfold(freqIn, srcPtr, transBuff, 1, (BLOCK_LEN_SHORT + BLOCK_LEN_LONG) / 2);
            ITransformBlock(transBuff, STOP_BLOCK, wnd_shape, timeBuff);
            transBuffPtr = 0;
            for (i = 0; i < BLOCK_LEN_SHORT; i++) {
                timeBuff[destPtr++] += transBuff[transBuffPtr++];
            }
            destPtr = 0;
            timeBuffPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeOut[destPtr++] = timeBuff[timeBuffPtr];
                timeBuff[timeBuffPtr++] = transBuff[transBuffPtr++];
            }
        } else if (blockType == NINE_SHORT) {
            destPtr = 3 * BLOCK_LEN_SHORT;
            for (i = 0; i < 9; i++) {
                unfold(freqIn, srcPtr, transBuff, 1, BLOCK_LEN_SHORT);
                srcPtr += BLOCK_LEN_SHORT;
                ITransformBlock(transBuff, SHORT_BLOCK, wnd_shape, timeBuff);
                transBuffPtr = 0;
                for (j = 0; j < BLOCK_LEN_SHORT; j++) {
                    timeBuff[destPtr++] += transBuff[transBuffPtr++];
                }
                for (j = 0; j < BLOCK_LEN_SHORT; j++) {
                    timeBuff[destPtr++] = transBuff[transBuffPtr++];
                }
                destPtr -= BLOCK_LEN_SHORT;
            }
            destPtr = 0;
            timeBuffPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeOut[destPtr++] = timeBuff[timeBuffPtr++];
            }
            destPtr = 0;
            for (; i < (2 * (BLOCK_LEN_LONG)); i++) {
                timeBuff[destPtr++] = timeBuff[timeBuffPtr++];
            }
            dolbyShortOffset = true;
        } else if (blockType == OLD_START) {
            unfold(freqIn, srcPtr, transBuff, 1, BLOCK_LEN_LONG);
            ITransformBlock(transBuff, START_FLAT_BLOCK, wnd_shape, timeBuff);
            transBuffPtr = 0;
            timeBuffPtr = 0;
            destPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeOut[destPtr++] = transBuff[transBuffPtr++] + timeBuff[timeBuffPtr++];
            }
            timeBuffPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeBuff[timeBuffPtr++] = transBuff[transBuffPtr++];
            }
            dolbyShortOffset = false;
        } else if (blockType == OLD_STOP) {
            unfold(freqIn, srcPtr, transBuff, 1, BLOCK_LEN_LONG);
            ITransformBlock(transBuff, STOP_FLAT_BLOCK, wnd_shape, timeBuff);
            transBuffPtr = 0;
            timeBuffPtr = 0;
            destPtr = 0;
            for (i = 0; i < (BLOCK_LEN_LONG - NFLAT); i++) {
                timeOut[destPtr++] = transBuff[transBuffPtr++] + timeBuff[timeBuffPtr++];
            }
            for (; i < BLOCK_LEN_LONG; i++) {
                timeOut[destPtr++] = transBuff[transBuffPtr++];
            }
            timeBuffPtr = 0;
            for (i = 0; i < BLOCK_LEN_LONG; i++) {
                timeBuff[timeBuffPtr++] = transBuff[transBuffPtr++];
            }
        } else {
            throw new IOException("Illegal Block_type " + blockType + " in time2freq_adapt(), aborting ...");
        }
        for (i = 0; i < BLOCK_LEN_LONG; i++) {
            ftimeOut[i] = timeOut[i];
        }
    }

    /*****************************************************************************
	*
	*	InitBlock
	*	calculate windows for use by Window()
	*	input: none
	*	output: none
	*	local static: none
	*	globals: shortWindow[], longWindow[]
	*
	*****************************************************************************/
    void InitBlock() {
        int i, j;
        double phaseInc;
        phaseInc = (Math.PI / (2.0f * (float) NWINLONG));
        for (i = 0; i < NWINLONG; i++) {
            fhg_long[i] = (float) Math.sin(phaseInc * ((float) i + 0.5f));
        }
        phaseInc = Math.PI / (2.0f * (float) NWINSHORT);
        for (i = 0; i < NWINSHORT; i++) {
            fhg_short[i] = (float) Math.sin(phaseInc * ((float) i + 0.5f));
        }
        for (i = 0, j = 0; i < NFLAT; i++, j++) {
            fhg_edler[j] = 0;
            dol_edler[j] = 0;
        }
        for (i = 0; i < NWINSHORT; i++, j++) {
            fhg_edler[j] = fhg_short[i];
            dol_edler[j] = Tables.dol_short[i];
        }
        for (; j < NWINFLAT; j++) {
            fhg_edler[j] = 1;
            dol_edler[j] = 1;
        }
        for (i = 0, j = 0; i < NADV0; i++, j++) {
            fhg_adv[j] = 0;
            dol_adv[j] = 0;
        }
        for (i = 0; i < NWINSHORT; i++, j++) {
            fhg_adv[j] = fhg_short[i];
            dol_adv[j] = Tables.dol_short[i];
        }
        for (; j < NWINADV; j++) {
            fhg_adv[j] = 1;
            dol_adv[j] = 1;
        }
    }

    /*****************************************************************************
	*
	*	Window
	*	window input sequence based on window type
	*	input: see below
	*	output: see below
	*	local static:
	*	  firstTime				flag = need to initialize data structures
	*	globals: shortWindow[], longWindow[]
	*
	*****************************************************************************/
    private boolean firstTime = true;

    void ITransformBlock(float[] dataPtr, int bT, Wnd_Shape wnd_shape, float[] state) {
        int leng0, leng1;
        int i, leng;
        float[] windPtr;
        int beginWT, endWT;
        int dataPtr_index = 0;
        int winPtr_index = 0;
        if (firstTime) {
            InitBlock();
            firstTime = false;
        }
        if ((bT == LONG_BLOCK) || (bT == START_BLOCK) || (bT == START_FLAT_BLOCK) || (bT == START_ADV_BLOCK)) {
            beginWT = WT_LONG;
        } else if (bT == STOP_FLAT_BLOCK) {
            beginWT = WT_FLAT;
        } else if (bT == STOP_ADV_BLOCK) {
            beginWT = WT_ADV;
        } else {
            beginWT = WT_SHORT;
        }
        if ((bT == LONG_BLOCK) || (bT == STOP_BLOCK) || (bT == STOP_FLAT_BLOCK) || (bT == STOP_ADV_BLOCK)) {
            endWT = WT_LONG;
        } else if (bT == START_FLAT_BLOCK) {
            endWT = WT_FLAT;
        } else if (bT == START_ADV_BLOCK) {
            endWT = WT_ADV;
        } else {
            endWT = WT_SHORT;
        }
        leng0 = windowLeng[beginWT];
        leng1 = windowLeng[endWT];
        MDCT.ITransform(dataPtr, leng0 + leng1, leng1);
        windPtr = windowPtr[beginWT][wnd_shape.prev_bk];
        for (i = 0; i < windowLeng[beginWT]; i++) {
            dataPtr[dataPtr_index++] *= windPtr[winPtr_index++];
        }
        leng = windowLeng[endWT];
        windPtr = windowPtr[endWT][wnd_shape.this_bk];
        winPtr_index = leng - 1;
        for (i = 0; i < leng; i++) {
            dataPtr[dataPtr_index++] *= windPtr[winPtr_index--];
        }
        wnd_shape.prev_bk = wnd_shape.this_bk;
    }

    /*****************************************************************************
	*
	*	unfold
	*	create full spectrum by reflecting-inverting first half over to second
	*	input: see below 
	*	output: see below
	*	local static: none
	*	globals: none
	*
	*****************************************************************************/
    void unfold(float[] data_in, int data_in_ptr, float[] data_out, int inStep, int inLeng) {
        int i;
        for (i = 0; i < inLeng; i++) {
            data_out[i] = data_in[data_in_ptr];
            data_out[2 * inLeng - i - 1] = -(data_in[data_in_ptr]);
            data_in_ptr += inStep;
        }
    }
}
