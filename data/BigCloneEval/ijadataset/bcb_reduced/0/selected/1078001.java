package csa.jportal.ai.enhancedAI.enhancedSim;

import csa.jportal.ai.standardAI.hints.AIHelper;
import csa.jportal.card.Card;
import csa.jportal.card.CardList;
import java.util.Vector;
import java.util.zip.CRC32;

/**
 * Card must not be alterered!
 * @author malban
 */
public abstract class CardProxyRead {

    private Card card = null;

    private boolean tapped = false;

    private int toughness = 0;

    private int power = 0;

    private int preventedDamage = 0;

    private boolean isSick = false;

    private boolean isAttacker = false;

    private boolean isBlocker = false;

    private String abilities = "";

    private String dummyColor = "BURGW";

    private String dummyType = "Creature";

    private String dummySubType = "";

    public void setDummyColor(String dummyColor) {
        this.dummyColor = dummyColor;
    }

    public void setDummySubType(String dummySubType) {
        this.dummySubType = dummySubType;
    }

    public void setDummyType(String dummyType) {
        this.dummyType = dummyType;
    }

    private String currentKey = "";

    private boolean updateNeed = true;

    public final String getKey() {
        if (!updateNeed) return currentKey;
        String key = "";
        key += "ID:" + getCard().getUniqueID();
        key += " " + getNowColor();
        key += " " + getNowPower();
        key += " " + getNowToughness();
        key += " " + preventedDamage;
        key += " Abilities: " + getNowCardAbilities();
        key += " Tapped: " + isTapped();
        key += " Sick: " + isSick();
        key += " Attacker: " + isAttacker;
        key += " Blocker: " + isBlocker;
        CRC32 c = new CRC32();
        c.update(key.getBytes());
        currentKey = "" + c.getValue();
        return currentKey;
    }

    protected void setCard(Card c) {
        card = c;
        tapped = c.isTapped();
        toughness = c.getNowToughness() - getIntData(Card.CARD_DAMAGE);
        power = c.getNowPower();
        isAttacker = c.isAttacker();
        isBlocker = c.isBlocker();
        preventedDamage = c.getPreventDamage();
        isSick = c.getBoolData(Card.CARD_SICKNESS);
        abilities = card.getNowCardAbilitiesString();
        updateNeed = true;
    }

    protected void vClone(CardProxyRead c) {
        card = c.getCard();
        tapped = c.tapped;
        toughness = c.toughness;
        power = c.power;
        isAttacker = c.isAttacker;
        isBlocker = c.isBlocker;
        isSick = c.isSick;
        preventedDamage = c.preventedDamage;
        abilities = c.abilities;
        updateNeed = true;
    }

    public void reducePreventedDamage(int value) {
        addPreventDamage(-value);
    }

    public int getPreventDamage() {
        return preventedDamage;
    }

    public void addPreventDamage(int value) {
        preventedDamage += value;
        if (preventedDamage <= 0) preventedDamage = 0;
    }

    public void setSick(boolean b) {
        isSick = b;
        updateNeed = true;
    }

    public boolean isSick() {
        return isSick;
    }

    public String getId() {
        return card.getId();
    }

    public String getName() {
        return card.getName();
    }

    public String getText() {
        return card.getText();
    }

    public String getManaString() {
        return card.getManaString();
    }

    public void setTapped(boolean b) {
        tapped = b;
        updateNeed = true;
    }

    public boolean isAttacker() {
        return isAttacker;
    }

    public void setAttacker(boolean a) {
        isAttacker = a;
        updateNeed = true;
    }

    public boolean isBlocker() {
        return isBlocker;
    }

    public void setBlocker(boolean b) {
        isBlocker = b;
        updateNeed = true;
    }

    public boolean isTapped() {
        return tapped;
    }

    public Card getCard() {
        return card;
    }

    public int getManaCost() {
        return card.getManaCost();
    }

    public int getManaCost(String c) {
        return card.getManaCost(c);
    }

    public int getIntData(String s) {
        return card.getIntData(s);
    }

    public void addPower(int p) {
        power += p;
        updateNeed = true;
    }

    public void subPower(int p) {
        power -= p;
        updateNeed = true;
    }

    public void setNowPower(int t) {
        power = t;
        updateNeed = true;
    }

    public int getNowPower() {
        return power;
    }

    public String getNowColor() {
        return card.getNowColor();
    }

    public void addToughness(int t) {
        toughness += t;
        updateNeed = true;
    }

    public void subToughness(int t) {
        toughness -= t;
        updateNeed = true;
    }

    public void setNowToughness(int t) {
        toughness = t;
        updateNeed = true;
    }

    public int getNowToughness() {
        return toughness;
    }

    public String getNowCardAbilities() {
        return abilities;
    }

    void resetAbilities() {
        abilities = getCard().getRealScannedCardAbilitiesString();
    }

    void addAbility(String ab) {
        if (hasAbility(ab)) return;
        if (abilities.length() > 0) abilities += ", " + ab; else abilities += ab;
        updateNeed = true;
    }

    void subAbility(String ab) {
        if (abilities.length() == ab.length()) {
            abilities = "";
        } else {
            abilities = csa.util.UtilityString.replace(abilities, ab, "");
            abilities = csa.util.UtilityString.replace(abilities, ", , ", ", ");
        }
        updateNeed = true;
    }

    public boolean hasAbility(String a) {
        return abilities.toUpperCase().indexOf(a.toUpperCase()) != -1;
    }

    public boolean hasOneAbility(String a) {
        Vector<String> ret = CardList.getVectorOfString(a, ",");
        for (int i = 0; i < ret.size(); i++) {
            String aa = ret.elementAt(i);
            if (hasAbility(aa)) return true;
        }
        return false;
    }

    public String getType() {
        if (card.isDummy()) return dummyType;
        return card.getType();
    }

    public int getRealPower() {
        return card.getRealPower();
    }

    public int getRealToughness() {
        return card.getRealToughness();
    }

    public String getSubtype() {
        if (card.isDummy()) return dummySubType;
        return card.getSubtype();
    }

    public boolean isDummy() {
        return (card.isDummy());
    }

    public boolean hasColor(String c) {
        if (card.isDummy()) {
            return dummyColor.indexOf(c) != -1;
        }
        return card.hasColor(c);
    }

    public boolean hasOneColor(String a) {
        Vector<String> ret = CardList.getVectorOfString(a, ",");
        for (int i = 0; i < ret.size(); i++) {
            String color = ret.elementAt(i);
            if (hasColor(color)) return true;
        }
        return false;
    }

    public int getManaCostNonX() {
        return card.getManaCostNonX();
    }

    public boolean equalsCard(Card c) {
        return card.getUniqueID().equals(c.getUniqueID());
    }

    public boolean equalsCard(CardProxyRead c) {
        return card.getUniqueID().equals(c.card.getUniqueID());
    }

    public String getHintString(String s) {
        return AIHelper.getHintString(card, s);
    }

    public int getHintInt(String s) {
        return AIHelper.getHintInt(card, s);
    }

    public boolean hasHint(String h) {
        return AIHelper.hasHint(card, h);
    }

    public boolean isLand() {
        return card.isLand();
    }

    public boolean isCreature() {
        return card.isCreature();
    }

    public boolean isEnchantment() {
        return card.isEnchantment();
    }

    public boolean isSorcery() {
        return card.isSorcery();
    }

    public boolean isInstant() {
        return card.isInstant();
    }

    public boolean isArtifact() {
        return card.isArtifact();
    }

    public boolean hasTapAbility() {
        return card.isTapActivatable();
    }

    public boolean hasActivationAbility() {
        return card.isActivatable();
    }

    public final String toUString() {
        return card.getUniqueID();
    }
}
