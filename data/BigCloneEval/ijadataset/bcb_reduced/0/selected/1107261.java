package failure.wm.business.patterns;

import failure.wm.business.*;
import failure.common.*;
import java.util.Vector;

/**
 * Custom pattern to build worlds
 * @author Higurashi
 * @version WM 1.0
 * @since WM 1.0
 */
public class WpCustomSurface extends WorldPattern {

    private int[][] description;

    private SurfaceType surface;

    private int level;

    private boolean passing;

    private int layer;

    /**
     * Constructor
     * @param position the base position of the pattern
     * @param surface the surface of the pattern (size)
     * @param level the level of the pattern (altitude)
     * @param description the mapping of the pattern (contains 1 and 0)
     * @throws failure.wm.business.WorldException
     */
    public WpCustomSurface(Position position, SurfaceType surface, int level, int[][] description, boolean passing, int layer) throws WorldException {
        super(description[0].length, description.length + level, position);
        this.layer = layer;
        this.passing = passing;
        this.level = level;
        this.surface = surface;
        this.description = new int[x][y];
        for (int i = 0; i < description[0].length; i++) {
            for (int j = 0; j < description.length; j++) {
                this.description[i][j] = description[j][i];
            }
        }
    }

    public void buildLayer0() throws WorldException {
    }

    public void buildLayer1() throws WorldException {
    }

    /**
     * @see WorldPattern
     */
    @Override
    public void build() throws WorldException {
        boolean C = false;
        boolean H = false;
        boolean B = false;
        boolean G = false;
        boolean D = false;
        boolean NC = false;
        boolean NH = false;
        boolean NB = false;
        boolean NG = false;
        boolean ND = false;
        boolean HD = false;
        boolean HG = false;
        boolean BD = false;
        boolean BG = false;
        boolean NHD = false;
        boolean NHG = false;
        boolean NBD = false;
        boolean NBG = false;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                C = this.description[i][j] == 1;
                H = j > 0 && this.description[i][j - 1] == 1;
                B = j + 1 < this.description[i].length - 1 && this.description[i][j + 1] == 1;
                G = i > 0 && this.description[i - 1][j] == 1;
                D = i + 1 < this.description.length - 1 && this.description[i + 1][j] == 1;
                NC = this.description[i][j] == 0;
                NH = j <= 0 || this.description[i][j - 1] == 0;
                NB = j + 1 >= this.description[i].length - 1 || this.description[i][j + 1] == 0;
                NG = i <= 0 || this.description[i - 1][j] == 0;
                ND = i + 1 >= this.description.length - 1 || this.description[i + 1][j] == 0;
                HD = j > 0 && i > 0 && this.description[i - 1][j - 1] == 1;
                HG = j > 0 && i + 1 < this.description.length - 1 && this.description[i + 1][j - 1] == 1;
                BD = j + 1 < this.description[i].length - 1 && i > 0 && this.description[i - 1][j + 1] == 1;
                BG = j + 1 < this.description[i].length - 1 && i + 1 < this.description.length - 1 && this.description[i + 1][j + 1] == 1;
                NHD = j > 0 && i > 0 && this.description[i - 1][j - 1] == 0;
                NHG = j > 0 && i + 1 < this.description.length - 1 && this.description[i + 1][j - 1] == 0;
                NBD = j + 1 < this.description[i].length - 1 && i > 0 && this.description[i - 1][j + 1] == 0;
                NBG = j + 1 < this.description[i].length - 1 && i + 1 < this.description.length - 1 && this.description[i + 1][j + 1] == 0;
                if (this.description[i][j] == 2) {
                    this.setBlock(i, j, this.surface.getBasicSet().C_I, false);
                }
                if (this.description[i][j] == 3) {
                    this.setBlock(i, j, BlockType.invisible, false);
                }
                if (this.description[i][j] == 1 || this.description[i][j] == 0) {
                    if (this.layer == 0) {
                        if (NC && NH && NB && NG && D && NHG) {
                            this.setBlock(i, j + 1, this.surface.getLevelSet().ONO, true);
                        }
                        if (NC && NH && NB && NG && D && HG && BG) {
                            for (int k = 1; k <= this.level; k++) {
                                this.setBlock(i, j + k, this.surface.getLevelSet().OO, false);
                            }
                        }
                        if (NC && NH && NB && NG && D && NBG) this.setBlock(i, j + this.level, this.surface.getLevelSet().OSO, true);
                        if (NC && NH && NB && G && ND && NHD) this.setBlock(i, j + 1, this.surface.getLevelSet().ENE, true);
                        if (NC && NH && NB && G && ND && HD && BD) {
                            for (int k = 1; k <= this.level; k++) {
                                this.setBlock(i, j + k, this.surface.getLevelSet().EE, true);
                            }
                        }
                        if (NC && NH && NB && G && ND && NBD) this.setBlock(i, j + this.level, this.surface.getLevelSet().ESE, true);
                        if (C && NH && NB && NG && ND) {
                            this.setBlock(i, j, this.surface.getExtendedSet().C_M, false);
                            for (int k = 1; k <= this.level; k++) {
                                this.setBlock(i, j + k, this.getS(k), false);
                            }
                        }
                        if (C && H && NB && NG && ND) {
                            this.setBlock(i, j, this.surface.getExtendedSet().S_M, false);
                            for (int k = 1; k <= this.level; k++) {
                                this.setBlock(i, j + k, this.getS(k), false);
                            }
                        }
                        if (C && NH && B && NG && ND) this.setBlock(i, j, this.surface.getExtendedSet().N_M, false);
                        if (C && NH && NB && G && ND) {
                            this.setBlock(i, j, this.surface.getExtendedSet().E_M, false);
                            for (int k = 1; k <= this.level; k++) {
                                this.setBlock(i, j + k, this.getS(k), false);
                            }
                        }
                        if (C && NH && NB && NG && D) {
                            this.setBlock(i, j, this.surface.getExtendedSet().O_M, false);
                            for (int k = 1; k <= this.level; k++) {
                                this.setBlock(i, j + k, this.getS(k), false);
                            }
                        }
                        if (C && H && B && NG && ND) this.setBlock(i, j, this.surface.getExtendedSet().V_M, false);
                        if (C && NH && NB && G && D) {
                            this.setBlock(i, j, this.surface.getExtendedSet().H_M, false);
                            for (int k = 1; k <= this.level; k++) {
                                this.setBlock(i, j + k, this.getS(k), false);
                            }
                        }
                        if (C && H && B && G && D) {
                            if (NHD && NHG && NBD && NBG) {
                                this.setBlock(i, j, this.surface.getExtendedSet().HV_M, false);
                            } else {
                                if (NHD) this.setBlock(i, j, this.getNOI(this.level), true); else if (NHG) this.setBlock(i, j, this.getNEI(this.level), true); else if (NBD) this.setBlock(i, j, this.getSOI(this.level), true); else if (NBG) this.setBlock(i, j, this.getSEI(this.level), true); else {
                                    if (this.surface != SurfaceType.water_1_A) {
                                        this.setBlock(i, j, this.surface.getBasicSet().C_I, this.passing);
                                    }
                                }
                            }
                        }
                        if (C && NH && B && G && D) {
                            if (BD && BG) {
                                if (level == 0) {
                                    this.setBlock(i, j, this.getN(this.level), this.passing);
                                } else {
                                    this.setBlock(i, j, this.getN(this.level), this.passing);
                                    this.setCollisionBlock(i, j, Orientation.UP);
                                    this.setCollisionBlock(i, j - 1, Orientation.DOWN);
                                }
                            } else {
                                if (NBD) this.setBlock(i, j, this.surface.getExtendedSet().SO_H_M, this.passing);
                                if (NBG) this.setBlock(i, j, this.surface.getExtendedSet().SE_H_M, this.passing);
                            }
                        }
                        if (C && NH && B && NG && D && level == 0) {
                            if (BG) this.setBlock(i, j, this.getNO(this.level), this.passing);
                            if (NBG) this.setBlock(i, j, this.getNOM(this.level), this.passing);
                        }
                        if (C && NH && B && G && ND) {
                            if (BD) {
                                if (level == 0) {
                                    this.setBlock(i, j, this.getNE(this.level), this.passing);
                                } else {
                                    this.setBlock(i, j, this.getNE(this.level), this.passing);
                                    this.setCollisionBlock(i + 1, j, Orientation.LEFT);
                                    this.setCollisionBlock(i, j, Orientation.UP);
                                }
                            }
                            if (NBD) this.setBlock(i, j, this.getNEM(this.level), this.passing);
                        }
                        if (C && H && B && G && ND) {
                            if (HD && BD) {
                                if (level == 0) {
                                    this.setBlock(i, j, this.getE(this.level), this.passing);
                                } else {
                                    this.setBlock(i, j, this.getE(this.level), this.passing);
                                    this.setCollisionBlock(i + 1, j, Orientation.LEFT);
                                }
                            } else {
                                if (NHD) this.setBlock(i, j, this.surface.getExtendedSet().NO_V_M, this.passing);
                                if (NBD) this.setBlock(i, j, this.surface.getExtendedSet().SO_V_M, this.passing);
                            }
                        }
                        if (C && H && B && NG && D) {
                            if (BG && HG) {
                                if (level == 0) {
                                    this.setBlock(i, j, this.getO(this.level), this.passing);
                                } else {
                                    this.setBlock(i, j, this.getO(this.level), this.passing);
                                    this.setCollisionBlock(i - 1, j, Orientation.RIGHT);
                                    this.setCollisionBlock(i, j, Orientation.LEFT);
                                }
                            } else {
                                if (NBG) this.setBlock(i, j, this.surface.getExtendedSet().SE_V_M, this.passing);
                                if (NHG) this.setBlock(i, j, this.surface.getExtendedSet().NE_V_M, this.passing);
                            }
                        }
                        if (C && H && NB && NG && D) {
                            if (HG) {
                                if (level == 0) {
                                    this.setBlock(i, j, this.getSO(0), this.passing);
                                } else {
                                    this.setBlock(i, j, this.getSO(0), this.passing);
                                    this.setCollisionBlock(i - 1, j, Orientation.RIGHT);
                                }
                            } else {
                                if (NHG) this.setBlock(i, j, this.getSOM(this.level), this.passing);
                            }
                            for (int k = 1; k <= this.level; k++) {
                                this.setBlock(i, j + k, this.getSO(k), false);
                            }
                        }
                        if (C && H && NB && G && ND) {
                            if (HD) {
                                if (level == 0) {
                                    this.setBlock(i, j, this.getSE(0), this.passing);
                                } else {
                                    this.setBlock(i, j, this.getSE(0), this.passing);
                                    this.setCollisionBlock(i, j, Orientation.RIGHT);
                                }
                            } else {
                                if (NHD) this.setBlock(i, j, this.getSEM(this.level), this.passing);
                            }
                            for (int k = 1; k <= this.level; k++) {
                                this.setBlock(i, j + k, this.getSE(k), false);
                            }
                        }
                        if (C && H && NB && G && D) {
                            if (HD && HG) {
                                if (level == 0) {
                                    this.setBlock(i, j, this.getS(0), this.passing);
                                } else {
                                    this.setBlock(i, j, this.getS(0), this.passing);
                                    this.setCollisionBlock(i, j, Orientation.DOWN);
                                }
                            } else {
                                if (NHD) this.setBlock(i, j, this.surface.getExtendedSet().NO_H_M, this.passing);
                                if (NHG) this.setBlock(i, j, this.surface.getExtendedSet().NE_H_M, this.passing);
                            }
                            for (int k = 1; k <= this.level; k++) {
                                this.setBlock(i, j + k, this.getS(k), false);
                            }
                        }
                        if (C && NH && B && NG && D && level != 0) {
                            if (BG) {
                                if (level == 0) {
                                    this.setBlock(i, j, this.getNO(this.level), this.passing);
                                } else {
                                    this.setBlock(i, j, this.getNO(this.level), this.passing);
                                    this.setCollisionBlock(i - 1, j, Orientation.RIGHT);
                                    this.setCollisionBlock(i, j, Orientation.UP);
                                }
                            }
                            if (NBG) this.setBlock(i, j, this.getNOM(this.level), this.passing);
                        }
                        if (NC) {
                            if (this.level == 0) {
                                this.setBlock(i, j, this.surface.getBasicSet().C_E, true);
                            }
                        }
                    } else {
                    }
                }
            }
        }
    }

    /**
     * Get the block N (nord)
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getN(int level) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getBasicSet().N;
        } else {
            block = this.surface.getLevelSet().N_U;
        }
        return block;
    }

    private BlockType getNOM(int i) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getExtendedSet().NO_M;
        } else {
            block = this.surface.getExtendedSet().NO_M;
        }
        return block;
    }

    private BlockType getNEM(int i) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getExtendedSet().NE_M;
        } else {
            block = this.surface.getExtendedSet().NE_M;
        }
        return block;
    }

    private BlockType getSOM(int i) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getExtendedSet().SO_M;
        } else {
            block = this.surface.getExtendedSet().SO_M;
        }
        return block;
    }

    private BlockType getSEM(int i) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getExtendedSet().SE_M;
        } else {
            block = this.surface.getExtendedSet().SE_M;
        }
        return block;
    }

    /**
     * Get the block S (sud)
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getS(int level) {
        BlockType block = null;
        if (this.level == 0) {
            block = this.surface.getBasicSet().S;
        } else if (level == 0) {
            block = this.surface.getLevelSet().S_U;
        } else if (level < this.level) {
            block = this.surface.getLevelSet().SS;
        } else {
            block = this.surface.getLevelSet().S_D;
        }
        return block;
    }

    /**
     * Get the block E (est)
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getE(int level) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getBasicSet().E;
        } else {
            block = this.surface.getLevelSet().E_U;
        }
        return block;
    }

    /**
     * Get the block O (ouest)
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getO(int level) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getBasicSet().O;
        } else {
            block = this.surface.getLevelSet().O_U;
        }
        return block;
    }

    /**
     * Get the block NO (nord ouest)
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getNO(int level) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getBasicSet().NO;
        } else {
            block = this.surface.getLevelSet().NO_U;
        }
        return block;
    }

    /**
     * Get the block NO (nord ouest) INTERNAL
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getNOI(int level) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getBasicSet().NO_I;
        } else {
            block = this.surface.getLevelSet().NO_I_U;
        }
        return block;
    }

    /**
     * Get the block NE (nord est) INTERNAL
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getNEI(int level) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getBasicSet().NE_I;
        } else {
            block = this.surface.getLevelSet().NE_I_U;
        }
        return block;
    }

    /**
     * Get the block SE (sud est) INTERNAL
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getSEI(int level) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getBasicSet().SE_I;
        } else {
            block = this.surface.getLevelSet().SE_I_U;
        }
        return block;
    }

    /**
     * Get the block SO (sud ouest) INTERNAL
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getSOI(int level) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getBasicSet().SO_I;
        } else {
            block = this.surface.getLevelSet().SO_I_U;
        }
        return block;
    }

    /**
     * Get the block SO (sud ouest)
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getSO(int level) {
        BlockType block = null;
        if (this.level == 0) {
            block = this.surface.getBasicSet().SO;
        } else if (level == 0) {
            block = this.surface.getLevelSet().SO_U;
        } else if (level < this.level) {
            block = this.surface.getLevelSet().SSO;
        } else {
            block = this.surface.getLevelSet().SO_D;
        }
        return block;
    }

    /**
     * Get the block NE (nord est)
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getNE(int level) {
        BlockType block = null;
        if (level == 0) {
            block = this.surface.getBasicSet().NE;
        } else {
            block = this.surface.getLevelSet().NE_U;
        }
        return block;
    }

    /**
     * Get the block SE (sud est)
     * @param level the level of the patern
     * @return the block type to use
     */
    private BlockType getSE(int level) {
        BlockType block = null;
        if (this.level == 0) {
            block = this.surface.getBasicSet().SE;
        } else if (level == 0) {
            block = this.surface.getLevelSet().SE_U;
        } else if (level < this.level) {
            block = this.surface.getLevelSet().SSE;
        } else {
            block = this.surface.getLevelSet().SE_D;
        }
        return block;
    }
}
