package ms.jasim.framework;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import ms.jacrim.pddl.PddlProblem;
import ms.jacrim.pddl.PddlSolution;
import ms.jacrim.pddl.PddlProblem.PddlPredicate;
import ms.jacrim.pddl.PddlSolution.Action;
import ms.spm.IAppContext;
import ms.utils.DataBag;
import ms.utils.NamedList;
import ms.utils.Utils;
import ms.utils.XmlFile;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PddlModel {

    public class Goal {

        private String name;

        private int criticality, maxSat;

        public final NamedList<Decomposition> Decomposition = new NamedList<Decomposition>();

        private String text;

        public void loadFromXML(Element node, XmlFile xmlDoc) {
            name = node.getAttribute("id");
            text = node.getAttribute("text");
            criticality = Utils.parseIntDef(node.getAttribute("criticality"), 1);
            maxSat = Utils.parseIntDef(node.getAttribute("max_satisfaction"), 0);
            NodeList decomposes = node.getElementsByTagName("decomposition");
            for (int i = 0; i < decomposes.getLength(); i++) {
                Decomposition de = new Decomposition();
                de.loadFromXml((Element) decomposes.item(i), xmlDoc);
                Decomposition.add(de);
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCriticality() {
            return criticality;
        }

        public void setCriticality(int criticality) {
            this.criticality = criticality;
        }

        public int getMaxSat() {
            return maxSat;
        }

        public void setMaxSat(int maxSat) {
            this.maxSat = maxSat;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Decomposition createDecomposition() {
            Decomposition de = new Decomposition();
            Decomposition.add(de);
            return de;
        }
    }

    public class Actor {

        private String name;

        private int maxJobs;

        private int maxWorkLoad;

        private boolean enabled = true;

        public final NamedList<Goal> Requests = new NamedList<Goal>();

        public final NamedList<Capacity> Capacity = new NamedList<Capacity>();

        private String text;

        public void loadFromXml(Element node, XmlFile xmlDoc) throws XPathExpressionException {
            name = node.getAttribute("id");
            text = node.getAttribute("text");
            maxJobs = Utils.parseIntDef(node.getAttribute("max_jobs"), 999);
            maxWorkLoad = Utils.parseIntDef(node.getAttribute("max_work_load"), 999);
            String subgoals = xmlDoc.evalXPath("request/@goals", node);
            for (String g : subgoals.split(",")) {
                Goal goal = Goals.get(g.trim());
                if (goal != null) Requests.add(goal);
            }
            NodeList capacity = node.getElementsByTagName("can_provide");
            for (int i = 0; i < capacity.getLength(); i++) {
                Capacity ca = new Capacity();
                ca.loadFromXml((Element) capacity.item(i), xmlDoc);
                ca.setActor(this);
                Capacity.add(ca);
            }
        }

        public Capacity createCapacity() {
            Capacity ca = new Capacity();
            ca.setActor(this);
            Capacity.add(ca);
            return ca;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getMaxJobs() {
            return maxJobs;
        }

        public void setMaxJobs(int maxJobs) {
            this.maxJobs = maxJobs;
        }

        public int getMaxWorkLoad() {
            return maxWorkLoad;
        }

        public void setMaxWorkLoad(int maxWorkLoad) {
            this.maxWorkLoad = maxWorkLoad;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Capacity getCapacity(Goal goal) {
            for (Capacity cap : Capacity) if (cap.getGoal() == goal) return cap;
            return null;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getText() {
            return text;
        }

        public void setText(String value) {
            text = value;
        }

        public boolean containCapacity(Goal arg0) {
            for (Capacity cap : Capacity) if (cap.getGoal() == arg0) return true;
            return false;
        }
    }

    public class Decomposition {

        public static final int AND = 0;

        public static final int OR = 1;

        private String name;

        private int type;

        public final NamedList<Goal> SubGoals = new NamedList<Goal>();

        public void loadFromXml(Element node, XmlFile xmlDoc) {
            name = node.getAttribute("id");
            type = "AND".equalsIgnoreCase(node.getAttribute("type")) ? AND : OR;
            String subgoals = node.getAttribute("subgoals");
            for (String g : subgoals.split(",")) {
                Goal goal = Goals.get(g.trim());
                if (goal != null) SubGoals.add(goal);
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return (type == AND ? "AND" : "OR") + ": " + SubGoals.toString();
        }
    }

    public class Capacity {

        private Goal goal;

        private Actor actor;

        private int satisfaction, effort, consumeTime;

        public void loadFromXml(Element node, XmlFile xmlDoc) {
            goal = Goals.get(node.getAttribute("goal-id"));
            satisfaction = Utils.parseIntDef(node.getAttribute("satisfaction"), 1);
            effort = Utils.parseIntDef(node.getAttribute("effort"), 1);
            consumeTime = Utils.parseIntDef(node.getAttribute("time"), 1);
        }

        public String getName() {
            return goal != null ? goal.getName() : null;
        }

        public Goal getGoal() {
            return goal;
        }

        public void setGoal(Goal goal) {
            this.goal = goal;
        }

        public Actor getActor() {
            return actor;
        }

        public void setActor(Actor actor) {
            this.actor = actor;
        }

        public int getSatisfaction() {
            return satisfaction;
        }

        public void setSatisfaction(int satisfaction) {
            this.satisfaction = satisfaction;
        }

        public int getEffort() {
            return effort;
        }

        public void setEffort(int effort) {
            this.effort = effort;
        }

        public int getConsumeTime() {
            return consumeTime;
        }

        public void setConsumeTime(int consumeTime) {
            this.consumeTime = consumeTime;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public class Evaluator {

        private ms.jasim.framework.IEvaluator instance;

        private ms.jasim.framework.IEvaluator.Info evalInfo;

        private DataBag settings;

        @SuppressWarnings("unchecked")
        public void loadFromXml(Element node, XmlFile xmlDoc) throws Exception {
            String className = node.getAttribute("class");
            Class<? extends ms.jasim.framework.IEvaluator> evalClass = (Class<? extends ms.jasim.framework.IEvaluator>) Class.forName(className);
            Constructor<? extends ms.jasim.framework.IEvaluator> ctor = evalClass.getConstructor();
            instance = ctor.newInstance();
            extractEvaluatorInfo(evalClass);
        }

        private void extractEvaluatorInfo(Class<? extends ms.jasim.framework.IEvaluator> evalClass) {
            evalInfo = evalClass.getAnnotation(ms.jasim.framework.IEvaluator.Info.class);
        }

        public ms.jasim.framework.IEvaluator getInstance() {
            return instance;
        }

        public DataBag getSettings() {
            return settings;
        }

        public String getName() {
            return evalInfo.Name();
        }

        public String getDecription() {
            return evalInfo.Description();
        }

        public String[] getComponents() {
            return evalInfo.Components();
        }

        @Override
        public String toString() {
            return evalInfo.Name();
        }
    }

    private String metric, filename;

    private int satCoeff, workloadCoeff;

    public final NamedList<Goal> Goals = new NamedList<Goal>();

    public final NamedList<Actor> Actors = new NamedList<Actor>();

    public final ArrayList<Evaluator> Evaluators = new ArrayList<Evaluator>();

    private final List<String> postCommands = new ArrayList<String>();

    public void loadFromXml(XmlFile xmlDoc) throws Exception {
        metric = xmlDoc.evalXPath("/jacrim-model/pddl/@metric");
        satCoeff = Utils.parseIntDef(xmlDoc.evalXPath("/jacrim-model/pddl/fact/@satisfaction_coeff"), 1);
        workloadCoeff = Utils.parseIntDef(xmlDoc.evalXPath("/jacrim-model/pddl/fact/@work_load_coeff"), 1);
        NodeList evals = xmlDoc.selectNodes("//evaluators/add");
        for (int i = 0; i < evals.getLength(); i++) {
            Element node = (Element) evals.item(i);
            Evaluator e = new Evaluator();
            e.loadFromXml(node, xmlDoc);
            Evaluators.add(e);
        }
        NodeList goals = xmlDoc.selectNodes("//pddl/fact/goals/goal");
        for (int i = 0; i < goals.getLength(); i++) {
            Element node = (Element) goals.item(i);
            Goal g = new Goal();
            g.setName(node.getAttribute("id"));
            Goals.add(g);
        }
        for (int i = 0; i < goals.getLength(); i++) {
            Element node = (Element) goals.item(i);
            Goal g = Goals.get(i);
            g.loadFromXML(node, xmlDoc);
        }
        NodeList actors = xmlDoc.selectNodes("//pddl/fact/actors/actor");
        for (int i = 0; i < actors.getLength(); i++) {
            Element node = (Element) actors.item(i);
            Actor a = new Actor();
            a.loadFromXml(node, xmlDoc);
            Actors.add(a);
        }
    }

    public void loadFromFile(String xmlFilename) throws Exception {
        XmlFile doc = new XmlFile(xmlFilename);
        loadFromXml(doc);
        setFilename(xmlFilename);
    }

    @Override
    public PddlModel clone() {
        PddlModel result = new PddlModel();
        result.filename = filename;
        result.metric = metric;
        result.satCoeff = satCoeff;
        result.workloadCoeff = workloadCoeff;
        result.Goals.addAll(Goals);
        result.Actors.addAll(Actors);
        result.Evaluators.addAll(Evaluators);
        result.postCommands.addAll(postCommands);
        return result;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public int getSatCoeff() {
        return satCoeff;
    }

    public void setSatCoeff(int satCoeff) {
        this.satCoeff = satCoeff;
    }

    public int getWorkloadCoeff() {
        return workloadCoeff;
    }

    public void setWorkloadCoeff(int workloadCoeff) {
        this.workloadCoeff = workloadCoeff;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    /**
	 * +!goal - add goal,
	 * <p>
	 * -!goal - remove goal,
	 * <p>
	 * +@fact - add fact,
	 * <p>
	 * -@fact - remove fact
	 */
    public synchronized void addPostCommand(String cmd) {
        postCommands.add(cmd);
    }

    public synchronized void removePostCommand(String cmd) {
        postCommands.remove(cmd);
    }

    private PddlProblem processPostCommand(PddlProblem pddl) {
        for (String cmd : postCommands) {
            String arg = cmd.substring(2);
            if (cmd.startsWith("+!")) pddl.addGoalPredicate(arg); else if (cmd.startsWith("-!")) pddl.removeGoalPredicate(arg); else if (cmd.startsWith("+@")) pddl.addFact(arg); else if (cmd.startsWith("-@")) pddl.removeFact(arg);
        }
        return pddl;
    }

    public void reflectFinishedAction(Action act) {
        switch(act.getFunctor()) {
            case SATISFIES:
                addPostCommand(String.format("+@pr_satisfies %s %s", act.Argument(0), act.Argument(1)));
                addPostCommand(String.format("+@satisfied %s", act.Argument(1)));
                break;
        }
    }

    public PddlProblem generatePddl() throws Exception {
        PddlProblem result = new PddlProblem();
        result.setMetric(this.getMetric());
        result.addFact(String.format("(= (satisfaction_coeff) %d)", this.getSatCoeff()));
        result.addFact(String.format("(= (work_load_coeff) %d)", this.getWorkloadCoeff()));
        result.addObject("foo", "t_gtype");
        for (Goal goal : Goals) {
            result.addObject(goal.getName(), "t_goal");
            result.addFact(String.format("(= (max_satisfaction_degree %s) %s)", goal.getName(), goal.getMaxSat()));
            for (Decomposition de : goal.Decomposition) {
                PddlPredicate p = result.addFact((de.getType() == Decomposition.AND ? "and_subgoal" : "or_subgoal") + de.SubGoals.size(), goal.getName());
                for (Goal sg : de.SubGoals) p.getArguments().add(sg.getName());
            }
        }
        for (Actor actor : Actors) if (actor.isEnabled()) {
            result.addObject(actor.getName(), "t_actor");
            result.addFact(String.format("(= (max_job_at_hand %s) %d)", actor.getName(), actor.getMaxJobs()));
            result.addFact(String.format("(= (max_work_load %s) %d)", actor.getName(), actor.getMaxWorkLoad()));
            for (Capacity cap : actor.Capacity) {
                result.addFact("can_provide", actor.getName(), cap.getGoal().getName());
                result.addFact(String.format("(= (satisfaction_ability %s %s) %d)", actor.getName(), cap.getGoal().getName(), cap.getSatisfaction()));
                result.addFact(String.format("(= (work_effort %s %s) %d)", actor.getName(), cap.getGoal().getName(), cap.getEffort()));
                result.addFact(String.format("(= (time_effort %s %s) %d)", actor.getName(), cap.getGoal().getName(), cap.getConsumeTime()));
            }
            for (Goal goal : actor.Requests) {
                result.addGoalPredicate("satisfied " + goal.getName().toUpperCase());
                result.addFact("requests", actor.getName(), goal.getName());
            }
        }
        return processPostCommand(result);
    }

    public NamedList<EvaluatorResult> evaluateSolution(IAppContext context, Iterable<PddlSolution.Action> actions) {
        NamedList<EvaluatorResult> result = new NamedList<EvaluatorResult>();
        for (Evaluator eval : Evaluators) result.add(eval.getInstance().evaluate(context, this, actions));
        return result;
    }

    public void addActor() {
        Actors.add(new Actor());
    }

    public void addGoal() {
        Goals.add(new Goal());
    }
}
