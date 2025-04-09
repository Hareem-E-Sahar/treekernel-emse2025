package galoot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TemplateTest extends TestCase {

    private static final Log log = LogFactory.getLog(TemplateTest.class);

    protected ContextStack context;

    int[] intArray = { 7, 13, 15, 26, 42 };

    double[] doubleArray = { 0.0, 3.1415, Math.PI, Math.E };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = new ContextStack();
        List<String> randomStrings = new ArrayList<String>();
        Random random = new Random();
        for (int i = 0, num = random.nextInt(100); i < num; ++i) {
            int minChar = 32, maxChar = 126;
            int stringSize = random.nextInt(30);
            StringBuilder buf = new StringBuilder();
            for (int j = 0; j < stringSize; ++j) {
                int charId = minChar + random.nextInt(maxChar - minChar);
                buf.append((char) charId);
            }
            randomStrings.add(buf.toString());
        }
        context.put("name", "Tom");
        context.put("randomStrings", randomStrings);
        context.put("intArray", intArray);
        context.put("intList", TemplateUtils.objectToCollection(intArray));
        context.put("doubleArray", doubleArray);
        context.put("doubleList", TemplateUtils.objectToCollection(doubleArray));
    }

    public void testVarExpression() {
        try {
            Template t = new Template("{{ name }}");
            String output = t.render(context);
            assertEquals(output, "Tom");
            t = new Template("{{ name.length }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), "Tom".length());
            t = new Template("{{ intList.size }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), intArray.length);
            t = new Template("{{ doesnt_exist }}");
            output = t.render(context);
            assertTrue(output.length() == 0);
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testIfStatement() {
        try {
            Template t = new Template("{% if name %}true{% endif %}");
            String output = t.render(context);
            assertEquals(output, "true");
            t = new Template("{% if not name %}false{% endif %}");
            output = t.render(context);
            assertTrue(output.length() == 0);
            t = new Template("{% if bad_var %}false{% endif %}");
            output = t.render(context);
            assertTrue(output.length() == 0);
            t = new Template("{% if name %}true{% else %}false{% endif %}");
            output = t.render(context);
            assertEquals(output, "true");
            t = new Template("{% if not name %}true{% else %}false{% endif %}");
            output = t.render(context);
            assertEquals(output, "false");
            t = new Template("{% if bad_var %}false{% else %}else{% endif %}");
            output = t.render(context);
            assertEquals(output, "else");
            t = new Template("{% if name %}{% if intArray %}true{% endif %}{% endif %}");
            output = t.render(context);
            assertEquals(output, "true");
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testForStatement() {
        try {
            Template t = new Template("{% for var in intArray %}{{ var }}, {% endfor %}");
            String output = t.render(context);
            String expected = "";
            for (int i = 0; i < intArray.length; i++) expected += String.valueOf(intArray[i]) + ", ";
            assertEquals(output, expected);
            t = new Template("{% for var in intArray %}{{ forloop.counter0 }}:{{ var }}, {% endfor %}");
            output = t.render(context);
            expected = "";
            for (int i = 0; i < intArray.length; i++) expected += String.valueOf(i) + ":" + String.valueOf(intArray[i]) + ", ";
            assertEquals(output, expected);
            t = new Template("{% for var in intArray %}{% for var in intArray %}c{% endfor %}{% endfor %}");
            output = t.render(context);
            expected = "";
            for (int i = 0; i < intArray.length; i++) for (int j = 0; j < intArray.length; j++) expected += "c";
            assertEquals(output, expected);
            t = new Template("{% for var in intArray %}{% for var in intArray %}" + "{% if forloop.parent %}c{% endif %}" + "{% endfor %}{% endfor %}");
            output = t.render(context);
            expected = "";
            for (int i = 0; i < intArray.length; i++) for (int j = 0; j < intArray.length; j++) expected += "c";
            assertEquals(output, expected);
            t = new Template("{% for i in intArray %} == row {{ forloop.counter0 }} ==\n" + "{% for j in intArray %}" + "[{{ forloop.parent.counter0 }}]" + "[{{ forloop.counter0 }}]: {{ i }}, {% endfor %}" + "\n{% endfor %}");
            output = t.render(context);
            log.info("output matrix:\n" + output);
            expected = "";
            for (int i = 0; i < intArray.length; i++) {
                expected += " == row " + String.valueOf(i) + " ==\n";
                for (int j = 0; j < intArray.length; j++) {
                    expected += "[" + String.valueOf(i) + "][" + String.valueOf(j) + "]: " + String.valueOf(intArray[i]) + ", ";
                }
                expected += "\n";
            }
            log.info("expected matrix:\n" + expected);
            assertEquals(output, expected);
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testInclude() {
        File f = null;
        try {
            Template t = new Template("{% include \"garp\" %}");
            String output = t.render(context);
            assertEquals(output, "");
            f = File.createTempFile("tmp", "fake");
            FileUtils.writeStringToFile(f, "{% with name|upper as bar %}{{ bar }}{% endwith %}", "UTF-8");
            log.debug("creating tmp file: " + f.getAbsolutePath());
            PluginRegistry.getInstance().removeTemplateIncludePath(f.getParent());
            t = new Template("{% include \"" + f.getName() + "\" %}");
            output = t.render(context);
            assertTrue(output.length() == 0);
            PluginRegistry.getInstance().addTemplateIncludePath(f.getParent());
            t = new Template("{% include \"" + f.getName() + "\" %}");
            output = t.render(context);
            assertEquals("TOM", output);
            context.put("mypath", f.getName());
            t = new Template("{% include mypath %}");
            output = t.render(context);
            assertEquals("TOM", output);
            PluginRegistry.getInstance().removeTemplateIncludePath(f.getParent());
            FileUtils.forceDelete(f);
        } catch (IOException e) {
            if (f != null) {
                try {
                    FileUtils.forceDelete(f);
                } catch (IOException e1) {
                }
            }
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testWithStatement() {
        try {
            Template t = new Template("{% with name|upper as bar %}{{ bar }}{% endwith %}");
            String output = t.render(context);
            assertEquals(output, "TOM");
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testDefaultFilters() {
        try {
            Template t = new Template("{{ name|length }}");
            String output = t.render(context);
            assertEquals(Integer.parseInt(output), "Tom".length());
            t = new Template("{{ name|upper }}");
            output = t.render(context);
            assertEquals(output, "Tom".toUpperCase());
            t = new Template("{{ name|lower }}");
            output = t.render(context);
            assertEquals(output, "Tom".toLowerCase());
            t = new Template("{{ intArray|length }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), intArray.length);
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testLoad() {
        Filter filter = new Filter() {

            public Object filter(Object object, String args) {
                return object == null ? "true" : "false";
            }

            public String getName() {
                return "is_null";
            }
        };
        PluginRegistry.getInstance().registerFilter(filter);
        try {
            Template t = new Template("{{ bad_var|is_null }}");
            String output = t.render(context);
            assertTrue(output.length() == 0);
            t = new Template("{% load \"is_null\" %}{{ bad_var|is_null }}");
            output = t.render(context);
            assertEquals(output, "true");
            context.getFilterMap().removeFilter(filter.getName());
            t = new Template("{% load \"is_null\" as isNull %}{{ bad_var|isNull }}");
            output = t.render(context);
            assertEquals(output, "true");
            context.getFilterMap().removeFilter(filter.getName());
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testExtends() {
        try {
            File parentFile = File.createTempFile("parent", "txt");
            File childFile1 = File.createTempFile("child1", "txt");
            File childFile2 = File.createTempFile("child2", "txt");
            File childFile3 = File.createTempFile("child3", "txt");
            File childFile4 = File.createTempFile("child4", "txt");
            File childFile5 = File.createTempFile("child5", "txt");
            FileUtils.writeStringToFile(parentFile, "parent_start {% block block1 %}" + "parent_block1 {% block block2 %}" + "parent_block2" + "{% endblock %}" + "{% endblock %}" + " parent_end");
            FileUtils.writeStringToFile(childFile1, "{% extends \"" + parentFile.getName() + "\" %}" + "child_start{% block block1 %}" + "child_block1{% endblock %}child_end");
            FileUtils.writeStringToFile(childFile2, "{% extends \"" + parentFile.getName() + "\" %}" + "child_start{% block block2 %}" + "child_block2{% endblock %}child_end");
            FileUtils.writeStringToFile(childFile3, "{% extends \"" + childFile2.getName() + "\" %}" + "child_start{% block block1 %}" + "child_block1{% endblock %}child_end");
            FileUtils.writeStringToFile(childFile4, "{% extends \"" + childFile3.getName() + "\" %}" + "child_start{% block block1 %}" + "sub_child_block1 {{ block.super }}" + "{% endblock %}child_end");
            FileUtils.writeStringToFile(childFile5, "{% extends \"" + childFile4.getName() + "\" %}" + "child_start{% block block1 %}" + "sub_child_block1 {{ block.super }}" + "{% endblock %}child_end");
            String parentExpected = "parent_start parent_block1 parent_block2 parent_end";
            String child1Expected = "parent_start child_block1 parent_end";
            String child2Expected = "parent_start parent_block1 child_block2 parent_end";
            String child3Expected = "parent_start child_block1 parent_end";
            String child4Expected = "parent_start sub_child_block1 child_block1 parent_end";
            String child5Expected = "parent_start sub_child_block1 sub_child_block1 child_block1 parent_end";
            PluginRegistry.getInstance().addTemplateIncludePath(parentFile.getParent());
            Template t = new Template(parentFile);
            String output = t.render(context);
            assertEquals(output, parentExpected);
            t = new Template(childFile1);
            output = t.render(context);
            assertEquals(output, child1Expected);
            t = new Template(childFile2);
            output = t.render(context);
            assertEquals(output, child2Expected);
            t = new Template(childFile3);
            output = t.render(context);
            assertEquals(output, child3Expected);
            t = new Template(childFile4);
            output = t.render(context);
            assertEquals(output, child4Expected);
            t = new Template(childFile5);
            output = t.render(context);
            assertEquals(output, child5Expected);
            FileUtils.forceDelete(parentFile);
            FileUtils.forceDelete(childFile1);
            FileUtils.forceDelete(childFile2);
            FileUtils.forceDelete(childFile3);
            FileUtils.forceDelete(childFile4);
            FileUtils.forceDelete(childFile5);
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testTemplateTags() {
        try {
            Template t = new Template("{% templatetag opencomment %}");
            String output = t.render(context);
            assertEquals(output, "{#");
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testNow() {
        try {
            Date date = new Date();
            Template t = new Template("{% now %}");
            String output = t.render(context);
            System.out.println(output);
            t = new Template("{% now \"Today: %tm/%Td/%tY\" %}");
            output = t.render(context);
            System.out.println(output);
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testBlock() {
        try {
            Template t = new Template("{% block %}here{% endblock %}");
            String output = t.render(context);
            assertEquals(output, "here");
            t = new Template("{% block named %}here{% endblock %}");
            output = t.render(context);
            assertEquals(output, "here");
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testSet() {
        try {
            Template t = new Template("{% set name|upper as upperName %}{{ upperName }}");
            String output = t.render(context);
            assertEquals(output, "TOM");
            t = new Template("{% set intArray|length as len %}{{ len }}");
            output = t.render(context);
            assertEquals(output, String.valueOf(intArray.length));
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }
}
