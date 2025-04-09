package nz.ac.waikato.modeljunit.gui;

import java.lang.reflect.Constructor;
import java.util.Random;
import javax.swing.Box;
import javax.swing.JCheckBox;
import nz.ac.waikato.modeljunit.RandomTester;
import nz.ac.waikato.modeljunit.Tester;

public class OptionPanelRandomWalk extends OptionPanelAdapter implements IAlgorithmParameter {

    private static final long serialVersionUID = -7675450997014889733L;

    private StringBuffer m_bufRandomTest;

    private JCheckBox m_checkRandomSeed;

    public OptionPanelRandomWalk(String name, String explain, String imgPath) {
        super(name, explain, imgPath);
        m_checkRandomSeed = new JCheckBox("Use random seed");
        add(m_checkRandomSeed);
        add(Box.createHorizontalStrut(6));
        add(Box.createHorizontalGlue());
    }

    @Override
    public String generateCode() {
        m_bufRandomTest = new StringBuffer();
        m_bufRandomTest.append(Indentation.indent(Parameter.getClassName() + " model = new " + Parameter.getClassName() + "();"));
        m_bufRandomTest.append(Indentation.indent("Tester tester = new RandomTester(model);"));
        if (m_checkRandomSeed.isSelected()) m_bufRandomTest.append(Indentation.indent("tester.setRandom(new Random());"));
        return m_bufRandomTest.toString();
    }

    @Override
    public void initialize(int idx) {
        try {
            Class<?> testerClass = Class.forName("nz.ac.waikato.modeljunit.RandomTester");
            Constructor<?> con = testerClass.getConstructor(new Class[] { Class.forName("nz.ac.waikato.modeljunit.FsmModel") });
            m_tester[idx] = (RandomTester) con.newInstance(new Object[] { TestExeModel.getModelObject() });
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    @Override
    public String generateImportLab() {
        m_bufRandomTest = new StringBuffer();
        if (m_checkRandomSeed.isSelected()) {
            m_bufRandomTest.append(Indentation.indent("import java.util.Random;"));
        }
        m_bufRandomTest.append(Indentation.indent("import nz.ac.waikato.modeljunit.RandomTester;"));
        return m_bufRandomTest.toString();
    }

    @Override
    public void runAlgorithm(int idx) {
        if (m_checkRandomSeed.isSelected()) {
            Random rand = new Random();
            m_tester[idx].setRandom(rand);
        } else {
            m_tester[idx].setRandom(new Random(Tester.FIXEDSEED));
        }
    }
}
