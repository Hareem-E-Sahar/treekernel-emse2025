package game;

import input.MouseEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import util.*;
import game.AgentInfo.Ability;
import game.DataBattle.ListenerAgentDamage;
import game.DataBattle.TileOverlay;
import gui.*;
import gui.WidgetText.TextAlign;

public class DataBattleView extends Widget {

    /***************************************/
    private class ParticleEffect {

        private class Particle {

            float PosX;

            float PosY;

            float DirX;

            float DirY;

            float Trans = 1;

            float Fade;
        }

        public ParticleEffect(Vec pos, Color c, Random r) {
            mColor = c;
            mLifetime = r.nextInt(10) + 10;
            int nparticles = r.nextInt(4) + 6;
            mParticles = new Particle[nparticles];
            for (int i = 0; i < nparticles; ++i) {
                Particle p = new Particle();
                p.PosX = pos.getX() * TILE_SIZE + TILE_SIZE / 2;
                p.PosY = pos.getY() * TILE_SIZE + TILE_SIZE / 2;
                double O = r.nextDouble() * 3.1415926 * 2;
                float speed = r.nextFloat() * 2 + 3.0f;
                p.DirX = (float) Math.sin(O) * speed;
                p.DirY = (float) Math.cos(O) * speed;
                p.Fade = 0.95f - r.nextFloat() * 0.2f;
                mParticles[i] = p;
            }
        }

        public boolean isDead() {
            return mLifetime <= 0;
        }

        public void step() {
            mLifetime--;
            for (Particle p : mParticles) {
                p.PosX += p.DirX;
                p.PosY += p.DirY;
                p.Trans *= p.Fade;
            }
        }

        public void draw(Vec origin, RenderTarget t) {
            Graphics g = t.getContext();
            for (Particle p : mParticles) {
                int x = (int) p.PosX + origin.getX();
                int y = (int) p.PosY + origin.getY();
                g.setColor(new Color(mColor.getRed(), mColor.getGreen(), mColor.getBlue(), (int) (255 * p.Trans)));
                g.fillRect(x, y, 2, 2);
            }
        }

        private Particle[] mParticles;

        private int mLifetime;

        private Color mColor;
    }

    /***************************************/
    private class ParticleHandler {

        public void addParticleEffect(Vec pos, Color c) {
            mEffects.add(new ParticleEffect(pos, c, mRandom));
        }

        public void draw(Vec origin, RenderTarget t) {
            for (ParticleEffect p : mEffects) {
                if (p.isDead()) {
                    mDead.add(p);
                } else {
                    p.step();
                    p.draw(origin, t);
                }
            }
            if (!mDead.isEmpty()) {
                mEffects.removeAll(mDead);
                mDead.clear();
            }
        }

        private ArrayList<ParticleEffect> mDead = new ArrayList<ParticleEffect>();

        private ArrayList<ParticleEffect> mEffects = new ArrayList<ParticleEffect>();

        private Random mRandom = new Random();
    }

    /***************************************/
    private class FlashEffectHandler {

        public void setEffect(Vec pos) {
            mEffectPos = pos;
            mTicks = 0;
        }

        public void clearEffect() {
            mEffectPos = null;
        }

        public void draw(Vec origin, RenderTarget t) {
            if (mEffectPos == null) return;
            mTicks++;
            float n = ((mTicks % mFreq) / (-mFreq) + 1);
            int alpha = (int) (n * n * 150);
            int x = origin.getX() + mEffectPos.getX() * TILE_SIZE;
            int y = origin.getY() + mEffectPos.getY() * TILE_SIZE;
            Graphics g = t.getContext();
            g.setColor(new Color(255, 255, 255, alpha));
            g.fillRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);
        }

        private static final float mFreq = 30;

        private float mTicks = 0;

        private Vec mEffectPos;
    }

    private interface IAction {

        public void onActivate();

        public void onDeactivate();

        public void onClick(Vec pos, DataBattle.Tile target);
    }

    /***************************************/
    private class CutoffCornerWidget extends WidgetRect {

        public CutoffCornerWidget(int a, int b, int c, int d) {
            mA = a;
            mB = b;
            mC = c;
            mD = d;
        }

        public void onRender(RenderTarget t) {
            Rect r = getRect();
            Graphics g = t.getContext();
            int h = r.getHeight();
            int[] xs = { r.getX() + h * mA, r.getX() + r.getWidth() - h * mB, r.getX() + r.getWidth() - h * mD, r.getX() + h * mC };
            int[] ys = { r.getY(), r.getY(), r.getY() + h, r.getY() + h };
            g.setColor(getColor());
            g.fillPolygon(xs, ys, 4);
        }

        private int mA, mB, mC, mD;
    }

    /***************************************/
    public static class SignalSelectAbility {

        public void fire(AgentInfo.Ability ab) {
            for (ListenerSelectAbility l : mListeners) l.onSelectAbility(ab);
        }

        public void connect(ListenerSelectAbility l) {
            mListeners.add(l);
        }

        ArrayList<ListenerSelectAbility> mListeners = new ArrayList<ListenerSelectAbility>();
    }

    public static interface ListenerSelectAbility {

        public void onSelectAbility(AgentInfo.Ability a);
    }

    private class AgentStatsWidget extends Widget {

        private class AbilityDisplay {

            WidgetText Name;

            WidgetText Desc;

            Widget Container;

            AgentInfo.Ability Ability;
        }

        private class AgentThumbDisplay extends WidgetImage {

            public void onRender(RenderTarget t) {
                if (getImage() != null) {
                    Rect r = getRect();
                    Graphics g = t.getContext();
                    g.setColor(mAgentColor);
                    g.fillOval(r.getX(), r.getY(), r.getWidth(), r.getHeight());
                    g.drawImage(getImage(), r.getX() + r.getWidth() / 2 - 7, r.getY() + r.getHeight() / 2 - 7, null);
                }
            }

            public void setAgentColor(Color c) {
                mAgentColor = c;
            }

            private Color mAgentColor;
        }

        public AgentStatsWidget() {
            setActive(false);
            setSize(new UDim(0, 0, 1, 1));
            mAbilityContainer = new WidgetRect();
            mAbilityContainer.setSize(new UDim(250, 150));
            mAbilityContainer.setPos(new UDim(0, -150, 0, 1));
            mAbilityContainer.setColor(CONTENT_COLOR);
            mAbilityContainer.setParent(this);
            CutoffCornerWidget header = new CutoffCornerWidget(0, 1, 0, 0);
            header.setSize(new UDim(0, 20, 1, 0));
            header.setPos(new UDim(0, -20, 0, 0));
            header.setColor(HEADER_COLOR);
            header.setParent(mAbilityContainer);
            WidgetLabel headerlb = new WidgetLabel();
            headerlb.setSize(new UDim(0, 0, 1, 1));
            headerlb.setBackground(false);
            headerlb.setActive(false);
            headerlb.setTextColor(HEADER_TEXT_COLOR);
            headerlb.setText("ABILITIES");
            headerlb.setParent(header);
            WidgetRect agentContainer = new WidgetRect();
            agentContainer.setSize(new UDim(150, 150));
            agentContainer.setPos(new UDim(0, -150 - 170, 0, 1));
            agentContainer.setColor(CONTENT_COLOR);
            agentContainer.setParent(this);
            header = new CutoffCornerWidget(0, 1, 0, 0);
            header.setSize(new UDim(0, 20, 1, 0));
            header.setPos(new UDim(0, -20, 0, 0));
            header.setColor(HEADER_COLOR);
            header.setParent(agentContainer);
            headerlb = new WidgetLabel();
            headerlb.setSize(new UDim(0, 0, 1, 1));
            headerlb.setBackground(false);
            headerlb.setTextColor(HEADER_TEXT_COLOR);
            headerlb.setText("AGENT");
            headerlb.setParent(header);
            mAgentThumb = new AgentThumbDisplay();
            mAgentThumb.setSize(new UDim(18, 18));
            mAgentThumb.setPos(new UDim(-10, 6, 0.5f, 0));
            mAgentThumb.setParent(agentContainer);
            mAgentName = new WidgetText();
            mAgentName.setSize(new UDim(0, 14, 1, 0));
            mAgentName.setPos(new UDim(0, 30, 0, 0));
            mAgentName.setTextAlign(TextAlign.Left);
            mAgentName.setTextColor(CONTENT_TEXT_COLOR);
            mAgentName.setBackground(false);
            mAgentName.setParent(agentContainer);
            mAgentDesc = new WidgetText();
            mAgentDesc.setSize(new UDim(-5, -44, 1, 1));
            mAgentDesc.setPos(new UDim(5, 44));
            mAgentDesc.setTextAlign(TextAlign.Left);
            mAgentDesc.setTextColor(CONTENT_TEXT_COLOR);
            mAgentDesc.setBackground(false);
            mAgentDesc.setParent(agentContainer);
        }

        public void setAgentInfo(AgentInfo a) {
            if (a == null) {
                for (int i = 0; i < mAbilityDisplays.size(); ++i) mAbilityDisplays.get(i).Container.setVisible(false);
                mAgentThumb.setImage(null);
                mAgentName.setText("");
                mAgentDesc.setText("");
            } else {
                AgentInfo.Ability[] abs = a.getAbilities();
                while (abs.length > mAbilityDisplays.size()) addNewAbilityDisplay();
                float frac = 1 / ((float) mAbilityDisplays.size());
                for (int i = 0; i < abs.length; ++i) {
                    AbilityDisplay disp = mAbilityDisplays.get(i);
                    AgentInfo.Ability ab = abs[i];
                    disp.Container.setSize(new UDim(0, 0, frac, 1));
                    disp.Container.setPos(new UDim(0, 0, frac * i, 0));
                    disp.Container.setVisible(true);
                    disp.Name.setText(ab.getName());
                    disp.Desc.setText(ab.getDesc());
                    disp.Ability = ab;
                }
                for (int i = abs.length; i < mAbilityDisplays.size(); ++i) mAbilityDisplays.get(i).Container.setVisible(false);
                mAgentThumb.setImage(a.getThumb());
                mAgentThumb.setAgentColor(a.getColor());
                mAgentName.setText(a.getName());
                mAgentDesc.setText(a.getDesc());
            }
        }

        private void addNewAbilityDisplay() {
            final AbilityDisplay disp = new AbilityDisplay();
            disp.Container = new Widget();
            disp.Container.setSize(new UDim(0, 0, 0.5f, 1.0f));
            disp.Container.setParent(mAbilityContainer);
            disp.Name = new WidgetText();
            disp.Name.setSize(new UDim(0, 14, 1, 0));
            disp.Name.setBackground(false);
            disp.Name.setTextAlign(TextAlign.Left);
            disp.Name.setTextColor(CONTENT_TEXT_COLOR);
            disp.Name.setParent(disp.Container);
            disp.Desc = new WidgetText();
            disp.Desc.setSize(new UDim(-4, -28, 1, 1));
            disp.Desc.setPos(new UDim(4, 14, 0, 0));
            disp.Desc.setBackground(false);
            disp.Desc.setTextColor(CONTENT_TEXT_COLOR);
            disp.Desc.setTextAlign(TextAlign.Left);
            disp.Desc.setParent(disp.Container);
            WidgetRect w = new CutoffCornerWidget(1, 0, 0, 0);
            w.setSize(new UDim(80, 14, 0, 0));
            w.setPos(new UDim(-80, -14, 1, 1));
            w.setColor(Color.red);
            w.setParent(disp.Container);
            WidgetLabel l = new WidgetLabel();
            l.setSize(new UDim(0, 0, 1, 1));
            l.setBackground(false);
            l.setActive(false);
            l.setTextColor(Color.black);
            l.setText("SELECT");
            l.setParent(w);
            w.onMouseDown.connect(new MouseEvent.Listener() {

                public void onMouseEvent(MouseEvent e) {
                    if (mGameStarted) onSelectAbility.fire(disp.Ability);
                }
            });
            mAbilityDisplays.add(disp);
        }

        private WidgetRect mAbilityContainer;

        private WidgetText mAgentName;

        private WidgetText mAgentDesc;

        private AgentThumbDisplay mAgentThumb;

        public final SignalSelectAbility onSelectAbility = new SignalSelectAbility();

        private ArrayList<AbilityDisplay> mAbilityDisplays = new ArrayList<AbilityDisplay>();
    }

    /***************************************/
    public class ActionWinLoss implements IAction {

        public ActionWinLoss() {
            mWidget = new WidgetRect();
            mWidget.setColor(CONTENT_COLOR);
            mWidget.setSize(new UDim(400, 200));
            mWidget.setPos(new UDim(-200, -100, 0.5f, 0.5f));
            mWidget.setParent(DataBattleView.this);
            mWidget.setVisible(false);
            mWidget.onMouseDown.connect(new MouseEvent.Listener() {

                public void onMouseEvent(MouseEvent e) {
                    DataBattleView.this.setParent(null);
                    DataBattleView.this.setVisible(false);
                    mGame.enterNodeMap();
                }
            });
            CutoffCornerWidget top = new CutoffCornerWidget(1, 1, 0, 0);
            top.setSize(new UDim(0, 20, 1, 0));
            top.setPos(new UDim(0, -20));
            top.setColor(HEADER_COLOR);
            top.setParent(mWidget);
            CutoffCornerWidget bottom = new CutoffCornerWidget(0, 0, 1, 1);
            bottom.setSize(new UDim(0, 20, 1, 0));
            bottom.setPos(new UDim(0, 0, 0, 1));
            bottom.setColor(HEADER_COLOR);
            bottom.setParent(mWidget);
            mMessageWidget = new WidgetText();
            mMessageWidget.setSize(new UDim(0, 0, 1, 1));
            mMessageWidget.setBackground(false);
            mMessageWidget.setTextColor(CONTENT_TEXT_COLOR);
            mMessageWidget.setParent(mWidget);
            mMessageWidget.setActive(false);
        }

        public void onActivate() {
            mMessageWidget.setText(mMessage);
            mWidget.setVisible(true);
        }

        public void onDeactivate() {
        }

        public void onClick(Vec pos, DataBattle.Tile target) {
        }

        public void setState(String msg) {
            mMessage = msg;
        }

        private WidgetRect mWidget;

        private WidgetText mMessageWidget;

        private String mMessage;
    }

    /***************************************/
    private class ActionUseAbility implements IAction {

        public ActionUseAbility() {
        }

        public void onActivate() {
            mTarget.clear(true, true, true);
            if (mSourceAgent.getTurnState() != Agent.TurnState.Done) mAbility.select(mSourceAgent);
            showGui();
        }

        public void onDeactivate() {
            hideGui();
        }

        public void onClick(Vec pos, DataBattle.Tile target) {
            if (target.Select) {
                mAbility.apply(mSourceAgent, target.Agent, pos);
                mSourceAgent.setTurnState(Agent.TurnState.Done);
                setAction(mActionSelectAgent);
            } else {
                mActionSelectAgent.setState(false);
                setAction(mActionSelectAgent);
                mCurrentAction.onClick(pos, target);
            }
        }

        public void setState(Agent source, AgentInfo.Ability ability) {
            mSourceAgent = source;
            mAbility = ability;
        }

        private void showGui() {
            mAgentStats.setVisible(true);
            mAgentStats.setAgentInfo(mSourceAgent.getInfo());
        }

        private void hideGui() {
            mAgentStats.setVisible(false);
            mAgentStats.setAgentInfo(null);
        }

        private Agent mSourceAgent;

        private AgentInfo.Ability mAbility;
    }

    /***************************************/
    private class ActionMoveOrSelectAbility implements IAction {

        public ActionMoveOrSelectAbility() {
            mAgentStats.onSelectAbility.connect(new ListenerSelectAbility() {

                public void onSelectAbility(Ability a) {
                    selectAbility(a);
                }
            });
        }

        public void onActivate() {
            if (mTargetAgent.getTurnState() != Agent.TurnState.Done) {
                mTarget.clear(true, true, true);
                mTarget.moveFlood(mTargetAgent.getPos(), mTargetAgent.getMoveLeft());
            }
            mTarget.getTile(mTargetAgent.getPos()).Overlay = TileOverlay.Sel;
            mFlashEffectHandler.setEffect(mTargetAgent.getPos());
            mAgentStats.setAgentInfo(mTargetAgent.getInfo());
            showGui();
        }

        public void onDeactivate() {
            mFlashEffectHandler.clearEffect();
            hideGui();
        }

        public void onClick(Vec pos, DataBattle.Tile target) {
            if (target.Select) {
                mTargetAgent.move(pos);
                mTarget.clear(true, true, true);
                mTarget.moveFlood(mTargetAgent.getPos(), mTargetAgent.getMoveLeft());
                mTarget.getTile(mTargetAgent.getPos()).Overlay = TileOverlay.Sel;
                mFlashEffectHandler.setEffect(mTargetAgent.getPos());
                if (mTargetAgent.getMoveLeft() == 0) {
                    Ability[] ab = mTargetAgent.getInfo().getAbilities();
                    if (ab.length > 0) {
                        mActionUseAbility.setState(mTargetAgent, ab[0]);
                        setAction(mActionUseAbility);
                    }
                }
            } else {
                mActionSelectAgent.setState(false);
                setAction(mActionSelectAgent);
                mCurrentAction.onClick(pos, target);
            }
        }

        public void setState(Agent a) {
            mTargetAgent = a;
        }

        private void selectAbility(AgentInfo.Ability a) {
            mActionUseAbility.setState(mTargetAgent, a);
            setAction(mActionUseAbility);
        }

        private void showGui() {
            mAgentStats.setVisible(true);
        }

        private void hideGui() {
            mAgentStats.setVisible(false);
        }

        private Agent mTargetAgent;
    }

    /***************************************/
    private class ActionSelectAgent implements IAction {

        public ActionSelectAgent() {
        }

        public void onActivate() {
            mTarget.clear(true, true, true);
            if (mAutoSelect) {
                for (Agent a : mTarget.getAgents()) {
                    if (a.getTeam() == mTeam && a.getTurnState() != Agent.TurnState.Done) {
                        mActionMoveOrSelectAbility.setState(a);
                        setAction(mActionMoveOrSelectAbility);
                        return;
                    }
                }
                mAutoSelect = false;
            }
        }

        public void onDeactivate() {
            hideGui();
        }

        @SuppressWarnings("unused")
        public void onClick(Vec pos, DataBattle.Tile target) {
            if (target.Agent != null) {
                hideGui();
                mActionMoveOrSelectAbility.setState(target.Agent);
                setAction(mActionMoveOrSelectAbility);
            } else if (target.Credit > 0) {
                showGui();
            } else if (false) {
                showGui();
            }
        }

        public void setState(boolean autoselect) {
            mAutoSelect = autoselect;
        }

        private void hideGui() {
        }

        private void showGui() {
        }

        private boolean mAutoSelect = true;
    }

    /***************************************/
    private class ActionPlaceAgents implements IAction {

        private class PlaceAgentsEntry {

            AgentInfo Agent;

            int Quantity;

            WidgetLabel Row;
        }

        public ActionPlaceAgents() {
            mAgentSelectContainer = new Widget();
            mAgentSelectContainer.setSize(new UDim(100, 200));
            mAgentSelectContainer.setParent(DataBattleView.this);
            mStartButton = new CutoffCornerWidget(0, 0, 1, 0);
            mStartButton.setSize(new UDim(150, 30));
            mStartButton.setPos(new UDim(-150, 0, 1, 0));
            mStartButton.setColor(HEADER_COLOR);
            mStartButton.setParent(DataBattleView.this);
            WidgetLabel starttext = new WidgetLabel();
            starttext.setSize(new UDim(0, 0, 1, 1));
            starttext.setBackground(false);
            starttext.setActive(false);
            starttext.setTextColor(HEADER_TEXT_COLOR);
            starttext.setText("START GAME");
            starttext.setParent(mStartButton);
            mStartButton.onMouseDown.connect(new MouseEvent.Listener() {

                public void onMouseEvent(MouseEvent e) {
                    if (mTeam.getNumAgents() == 0) return;
                    for (int x = 0; x < mTarget.getSize().getX(); ++x) {
                        for (int y = 0; y < mTarget.getSize().getY(); ++y) {
                            DataBattle.Tile tl = mTarget.getTile(x, y);
                            if (tl.Upload) tl.Upload = false;
                        }
                    }
                    mGameStarted = true;
                    mNextTurnButton.setVisible(true);
                    mActionSelectAgent.setState(true);
                    setAction(mActionSelectAgent);
                }
            });
            WidgetRect agentListContainer = new WidgetRect();
            agentListContainer.setSize(new UDim(0.0f, -20.0f, 1, 1));
            agentListContainer.setColor(CONTENT_COLOR);
            agentListContainer.setParent(mAgentSelectContainer);
            CutoffCornerWidget bottom = new CutoffCornerWidget(0, 0, 0, 1);
            bottom.setSize(new UDim(0, 20, 1, 0));
            bottom.setPos(new UDim(0, -20, 0, 1));
            bottom.setColor(HEADER_COLOR);
            bottom.setParent(mAgentSelectContainer);
            int yoffs = 0;
            for (final Inventory.InventoryEntry agententry : mGame.getInventory().getAgents()) {
                final PlaceAgentsEntry thisentry = new PlaceAgentsEntry();
                final WidgetLabel row = new WidgetLabel();
                row.setSize(new UDim(0, 16, 1, 0));
                row.setPos(new UDim(0, yoffs));
                yoffs += 16;
                row.setBackground(false);
                row.setTextColor(CONTENT_TEXT_COLOR);
                row.setText(agententry.Quantity + "x " + agententry.Agent.getName());
                row.setParent(agentListContainer);
                row.onMouseDown.connect(new MouseEvent.Listener() {

                    public void onMouseEvent(MouseEvent e) {
                        if (mTargetTile == null) return;
                        if (thisentry.Quantity > 0) {
                            if (mTargetTile.Agent != null) {
                                PlaceAgentsEntry ent = mEntryLookup.get(mTargetTile.Agent.getInfo());
                                ent.Quantity++;
                                mTarget.killAgent(mTargetTile.Agent);
                                ent.Row.setText(ent.Quantity + "x " + ent.Agent.getName());
                            }
                            new Agent(mTarget, mTeam, mTargetTilePos, thisentry.Agent);
                            thisentry.Quantity--;
                            thisentry.Row.setText(thisentry.Quantity + "x " + thisentry.Agent.getName());
                            mAgentStats.setAgentInfo(thisentry.Agent);
                        }
                    }
                });
                thisentry.Row = row;
                thisentry.Agent = agententry.Agent;
                thisentry.Quantity = agententry.Quantity;
                mEntryLookup.put(thisentry.Agent, thisentry);
            }
        }

        public void onActivate() {
            showGui();
        }

        public void onDeactivate() {
            hideGui();
            mFlashEffectHandler.clearEffect();
        }

        public void onClick(Vec pos, DataBattle.Tile target) {
            if (target.Upload) {
                mTargetTile = target;
                mTargetTilePos = pos;
                mFlashEffectHandler.setEffect(pos);
            } else {
                mFlashEffectHandler.clearEffect();
                mTargetTile = null;
            }
            if (target.Agent != null) {
                mAgentStats.setAgentInfo(target.Agent.getInfo());
                mFlashEffectHandler.setEffect(pos);
            } else mAgentStats.setAgentInfo(null);
        }

        private void showGui() {
            mAgentSelectContainer.setVisible(true);
            mStartButton.setVisible(true);
        }

        private void hideGui() {
            mAgentSelectContainer.setVisible(false);
            mStartButton.setVisible(false);
        }

        private DataBattle.Tile mTargetTile;

        private Vec mTargetTilePos;

        private Widget mAgentSelectContainer;

        private WidgetRect mStartButton;

        private HashMap<AgentInfo, PlaceAgentsEntry> mEntryLookup = new HashMap<AgentInfo, PlaceAgentsEntry>();
    }

    /***************************************/
    private class BoardView extends Widget {

        public BoardView() {
            onMouseDown.connect(new MouseEvent.Listener() {

                public void onMouseEvent(MouseEvent e) {
                    Vec pos = e.getInput().getMousePos().sub(new Vec(1, 1)).sub(getRect().getPos()).div(TILE_SIZE);
                    DataBattle.Tile tl = mTarget.getTile(pos);
                    if (mTarget.getTurn() == mTeam) mCurrentAction.onClick(pos, tl);
                }
            });
        }

        public void onRender(RenderTarget t) {
            Graphics g = t.getContext();
            int ox = getRect().getX();
            int oy = getRect().getY();
            for (int x = 0; x < mTarget.getSize().getX(); ++x) {
                for (int y = 0; y < mTarget.getSize().getY(); ++y) {
                    DataBattle.Tile tl = mTarget.getTile(x, y);
                    int tlx = ox + x * TILE_SIZE;
                    int tly = oy + y * TILE_SIZE;
                    if (tl.Filled) {
                        g.setColor(TILE_COLOR);
                        g.fillRect(tlx + 1, tly + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                        if (tl.Upload) g.drawImage(UPLOAD_OVERLAY_IMAGE, tlx, tly, null);
                        if (tl.Credit > 0) {
                            int centerx = tlx + TILE_SIZE / 2;
                            int centery = tly + TILE_SIZE / 2;
                            g.setColor(CREDIT_COLOR);
                            g.drawRect(centerx - 1, centery - 1, 3, 3);
                            g.drawRect(centerx - 3, centery - 3, 7, 7);
                        }
                        if (tl.Agent != null) {
                            g.setColor(tl.Agent.getInfo().getColor());
                            g.fillRect(tlx + 1, tly + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                            for (Vec v : Vec.getDirs()) {
                                DataBattle.Tile adjacenttl = mTarget.getTile(x + v.getX(), y + v.getY());
                                if (adjacenttl != null && adjacenttl.Agent == tl.Agent) {
                                    int atlx = ox + (x + v.getX()) * TILE_SIZE;
                                    int atly = oy + (y + v.getY()) * TILE_SIZE;
                                    int centerx = (atlx + tlx) / 2;
                                    int centery = (atly + tly) / 2;
                                    g.fillRect(centerx + TILE_SIZE / 2 - 2, centery + TILE_SIZE / 2 - 2, 4, 4);
                                }
                            }
                            if (tl.Agent.getPos().eq(x, y)) {
                                g.drawImage(tl.Agent.getInfo().getThumb(), tlx + 3, tly + 3, null);
                                switch(tl.Agent.getTurnState()) {
                                    case Done:
                                        g.drawImage(CHECK_IMAGE, tlx + 13, tly + 13, null);
                                }
                                int[] xs = { tlx + 1, tlx + 1, tlx + 1 + 5 };
                                int[] ys = { tly + TILE_SIZE - 2 - 5, tly + TILE_SIZE - 1, tly + TILE_SIZE - 1 };
                                g.setColor(tl.Agent.getTeam().getColor());
                                g.fillPolygon(xs, ys, 3);
                                g.setColor(TILE_COLOR);
                                g.drawLine(tlx + 1, tly + TILE_SIZE - 1 - 6, tlx + 1 + 5, tly + TILE_SIZE - 2);
                            }
                        }
                        Image overlayimg = null;
                        switch(tl.Overlay) {
                            case Neg:
                                overlayimg = RED_OVERLAY_IMAGE;
                                break;
                            case Pos:
                                overlayimg = BLUE_OVERLAY_IMAGE;
                                break;
                            case Mod:
                                overlayimg = GREEN_OVERLAY_IMAGE;
                                break;
                            case Sel:
                                overlayimg = WHITE_OVERLAY_IMAGE;
                                break;
                            case MoveTo:
                                overlayimg = MOVETO_OVERLAY_IMAGE;
                                break;
                            case Move:
                                overlayimg = MOVE_OVERLAY_IMAGE;
                                break;
                            case None:
                                break;
                            default:
                                break;
                        }
                        if (overlayimg != null) {
                            g.drawImage(overlayimg, tlx, tly, null);
                        }
                    } else {
                    }
                }
            }
            mParticleHandler.draw(getRect().getPos(), t);
            mFlashEffectHandler.draw(getRect().getPos(), t);
        }
    }

    /***************************************/
    public DataBattleView(DataBattle b, Team myteam, GameSession game, AIController aic) {
        mTarget = b;
        mTeam = myteam;
        mGame = game;
        mAIController = aic;
        mTeam.onBeginTurn.connect(new Team.ListenerBeginTurn() {

            public void onTurn() {
                mNextTurnButton.setVisible(true);
                if (mCurrentAction == mActionSelectAgent) {
                    mActionSelectAgent.setState(true);
                    setAction(mActionSelectAgent);
                }
            }
        });
        mTarget.onAgentDamage.connect(new ListenerAgentDamage() {

            public void onAgentDamage(Vec sq) {
                mParticleHandler.addParticleEffect(sq, mTarget.getTile(sq).Agent.getInfo().getColor());
            }
        });
        Widget w = new BoardView();
        w.setSize(new UDim(mTarget.getSize().fvec().mul(TILE_SIZE)));
        w.setPos(new UDim(mTarget.getSize().fvec().mul(TILE_SIZE).neg().div(2), new FVec(0.5f, 0.5f)));
        w.setParent(this);
        CutoffCornerWidget nextTurn = new CutoffCornerWidget(1, 0, 0, 0);
        nextTurn.setSize(new UDim(180, 50));
        nextTurn.setPos(new UDim(-180, -50, 1, 1));
        nextTurn.setColor(HEADER_COLOR);
        nextTurn.setParent(this);
        nextTurn.onMouseDown.connect(new MouseEvent.Listener() {

            public void onMouseEvent(MouseEvent e) {
                if (mGameStarted) {
                    checkForWin();
                    mNextTurnButton.setVisible(false);
                    mCurrentAction = mActionSelectAgent;
                    mCurrentAction.onActivate();
                    mTarget.nextTurn();
                }
            }
        });
        WidgetLabel nextTurnText = new WidgetLabel();
        nextTurnText.setSize(new UDim(0, 0, 1, 1));
        nextTurnText.setBackground(false);
        nextTurnText.setActive(false);
        nextTurnText.setTextColor(HEADER_TEXT_COLOR);
        nextTurnText.setText("DONE TURN");
        nextTurnText.setParent(nextTurn);
        mNextTurnButton = nextTurn;
        mNextTurnButton.setVisible(false);
        mAgentStats = new AgentStatsWidget();
        mAgentStats.setParent(this);
        mActionUseAbility = new ActionUseAbility();
        mActionMoveOrSelectAbility = new ActionMoveOrSelectAbility();
        mActionSelectAgent = new ActionSelectAgent();
        mActionPlaceAgents = new ActionPlaceAgents();
        mActionWinLoss = new ActionWinLoss();
        setAction(mActionPlaceAgents);
    }

    public void onRender(RenderTarget t) {
        Graphics g = t.getContext();
        Rect r = getRect();
        g.setColor(Color.black);
        g.fillRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        mAIController.step();
    }

    private void setAction(IAction a) {
        if (mCurrentAction != null) mCurrentAction.onDeactivate();
        mCurrentAction = a;
        if (mCurrentAction != null) mCurrentAction.onActivate();
    }

    private void checkForWin() {
        if (mTeam.getNumAgents() == 0) {
            mActionWinLoss.setState("GAME OVER - YOU LOSE\n\nCLICK TO EXIT");
            setAction(mActionWinLoss);
        }
        boolean foundUnit = false;
        for (Team t : mTarget.getTeams()) {
            if (t != mTeam && t.getNumAgents() > 0) {
                foundUnit = true;
                break;
            }
        }
        if (!foundUnit) {
            mActionWinLoss.setState("YOU WIN!\n\nCLICK TO EXIT");
            mTarget.getTarget().setDefeated();
            setAction(mActionWinLoss);
        }
    }

    private Team mTeam;

    private DataBattle mTarget;

    private GameSession mGame;

    private AIController mAIController;

    private boolean mGameStarted = false;

    private AgentStatsWidget mAgentStats;

    private Widget mNextTurnButton;

    private ActionUseAbility mActionUseAbility;

    private ActionMoveOrSelectAbility mActionMoveOrSelectAbility;

    private ActionSelectAgent mActionSelectAgent;

    private ActionPlaceAgents mActionPlaceAgents;

    private ActionWinLoss mActionWinLoss;

    private IAction mCurrentAction;

    private ParticleHandler mParticleHandler = new ParticleHandler();

    private FlashEffectHandler mFlashEffectHandler = new FlashEffectHandler();

    private static final Color TILE_COLOR = new Color(128, 128, 128);

    private static final Color CREDIT_COLOR = new Color(0, 255, 0);

    private static final Color HEADER_COLOR = new Color(0x22, 0x66, 0xab);

    private static final Color HEADER_TEXT_COLOR = new Color(0xec, 0xb0, 0x1f);

    private static final Color CONTENT_COLOR = new Color(0x12, 0x47, 0x6d, 128);

    private static final Color CONTENT_TEXT_COLOR = new Color(0xe8, 0xd9, 0xb0);

    private static final int TILE_SIZE = 20;

    private static final Image CHECK_IMAGE = ResourceLoader.LoadImage("AgentTurnDone.png");

    private static final Image RED_OVERLAY_IMAGE = ResourceLoader.LoadImage("TileOverlayNeg.png");

    private static final Image BLUE_OVERLAY_IMAGE = ResourceLoader.LoadImage("TileOverlayPos.png");

    private static final Image GREEN_OVERLAY_IMAGE = ResourceLoader.LoadImage("TileOverlayMod.png");

    private static final Image WHITE_OVERLAY_IMAGE = ResourceLoader.LoadImage("TileOverlaySel.png");

    private static final Image MOVE_OVERLAY_IMAGE = ResourceLoader.LoadImage("TileOverlayMove.png");

    private static final Image MOVETO_OVERLAY_IMAGE = ResourceLoader.LoadImage("TileOverlayMoveTo.png");

    private static final Image UPLOAD_OVERLAY_IMAGE = ResourceLoader.LoadImage("TileOverlayUpload.png");
}
