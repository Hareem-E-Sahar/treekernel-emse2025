package com.l2jserver.gameserver.templates.effects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.l2jserver.gameserver.model.ChanceCondition;
import com.l2jserver.gameserver.model.L2Effect;
import com.l2jserver.gameserver.skills.AbnormalEffect;
import com.l2jserver.gameserver.skills.Env;
import com.l2jserver.gameserver.skills.conditions.Condition;
import com.l2jserver.gameserver.skills.funcs.FuncTemplate;
import com.l2jserver.gameserver.skills.funcs.Lambda;
import com.l2jserver.gameserver.templates.skills.L2SkillType;

/**
 * @author mkizub
 * 
 */
public class EffectTemplate {

    static Logger _log = Logger.getLogger(EffectTemplate.class.getName());

    private final Class<?> _func;

    private final Constructor<?> _constructor;

    public final Condition attachCond;

    public final Condition applayCond;

    public final Lambda lambda;

    public final int counter;

    public final int period;

    public final AbnormalEffect abnormalEffect;

    public final AbnormalEffect specialEffect;

    public final AbnormalEffect eventEffect;

    public FuncTemplate[] funcTemplates;

    public final String stackType;

    public final float stackOrder;

    public final boolean icon;

    public final String funcName;

    public final double effectPower;

    public final L2SkillType effectType;

    public final int triggeredId;

    public final int triggeredLevel;

    public final ChanceCondition chanceCondition;

    public EffectTemplate(Condition pAttachCond, Condition pApplayCond, String func, Lambda pLambda, int pCounter, int pPeriod, AbnormalEffect pAbnormalEffect, AbnormalEffect pSpecialEffect, AbnormalEffect pEventEffect, String pStackType, float pStackOrder, boolean showicon, double ePower, L2SkillType eType, int trigId, int trigLvl, ChanceCondition chanceCond) {
        attachCond = pAttachCond;
        applayCond = pApplayCond;
        lambda = pLambda;
        counter = pCounter;
        period = pPeriod;
        abnormalEffect = pAbnormalEffect;
        specialEffect = pSpecialEffect;
        eventEffect = pEventEffect;
        stackType = pStackType;
        stackOrder = pStackOrder;
        icon = showicon;
        funcName = func;
        effectPower = ePower;
        effectType = eType;
        triggeredId = trigId;
        triggeredLevel = trigLvl;
        chanceCondition = chanceCond;
        try {
            _func = Class.forName("com.l2jserver.gameserver.skills.effects.Effect" + func);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            _constructor = _func.getConstructor(Env.class, EffectTemplate.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public L2Effect getEffect(Env env) {
        if (attachCond != null && !attachCond.test(env)) return null;
        try {
            L2Effect effect = (L2Effect) _constructor.newInstance(env, this);
            return effect;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            _log.log(Level.WARNING, "Error creating new instance of Class " + _func + " Exception was: " + e.getTargetException().getMessage(), e.getTargetException());
            return null;
        }
    }

    /**
	 * Creates an L2Effect instance from an existing one and an Env object.
	 * 
	 * @param env
	 * @param stolen
	 * @return
	 */
    public L2Effect getStolenEffect(Env env, L2Effect stolen) {
        Class<?> func;
        Constructor<?> stolenCons;
        try {
            func = Class.forName("com.l2jserver.gameserver.skills.effects.Effect" + stolen.getEffectTemplate().funcName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            stolenCons = func.getConstructor(Env.class, L2Effect.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            L2Effect effect = (L2Effect) stolenCons.newInstance(env, stolen);
            return effect;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            _log.log(Level.WARNING, "Error creating new instance of Class " + func + " Exception was: " + e.getTargetException().getMessage(), e.getTargetException());
            return null;
        }
    }

    public void attach(FuncTemplate f) {
        if (funcTemplates == null) {
            funcTemplates = new FuncTemplate[] { f };
        } else {
            int len = funcTemplates.length;
            FuncTemplate[] tmp = new FuncTemplate[len + 1];
            System.arraycopy(funcTemplates, 0, tmp, 0, len);
            tmp[len] = f;
            funcTemplates = tmp;
        }
    }
}
