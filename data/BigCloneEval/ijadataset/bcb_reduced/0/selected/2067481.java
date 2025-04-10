package net.sf.nodeInsecure.util;

import net.sf.nodeInsecure.common.ExecutionContext;
import net.sf.nodeInsecure.common.Interpretable;
import net.sf.nodeInsecure.common.TerminalAccessor;
import net.sf.nodeInsecure.common.ExecutionContextImpl;
import net.sf.nodeInsecure.computer.Directory;
import net.sf.nodeInsecure.computer.Machine;
import net.sf.nodeInsecure.computer.MachineConfiguration;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 * @author: janmejay.singh
 * Date: Aug 28, 2007
 * Time: 7:18:14 PM
 */
@RunWith(JMock.class)
public class TouchTest {

    protected JUnit4Mockery context = new JUnit4Mockery();

    protected ExecutionContext mockContext;

    protected Interpretable mockInterpretable;

    protected TerminalAccessor mockAccessor;

    private Directory rootDir;

    @Before
    public void setup() {
        final Machine machine = new Machine(new MachineConfiguration("rz-400", (short) 2, 20, 3000));
        rootDir = new Directory("/", machine.getFileSystem()) {

            {
                addNewDir("foo");
                addNewDir("bar");
                addNewFile("baz");
            }
        };
        mockContext = new ExecutionContextImpl(machine, rootDir, new net.sf.nodeInsecure.common.Process() {

            public void close() {
            }
        });
        mockInterpretable = context.mock(Interpretable.class);
        mockAccessor = context.mock(TerminalAccessor.class);
        context.checking(new Expectations() {

            {
                ignoring(mockInterpretable).getContext();
                will(returnValue(mockContext));
                ignoring(mockInterpretable).getTerminalAccessor();
                will(returnValue(mockAccessor));
            }
        });
    }

    @Test
    public void should_create_a_new_file_under_the_current_directory() {
        final String new_file_name = "some_log_file";
        context.checking(new Expectations() {

            {
                ignoring(mockInterpretable).getArguments();
                will(returnValue(new String[] { new_file_name }));
            }
        });
        Touch touchCommand = new Touch();
        assertEquals(0, touchCommand.execute(mockInterpretable));
        assertNotNull(rootDir.getFileNamed(new_file_name));
    }

    @Test
    public void should_print_file_already_exists_message_when_demanded_to_use_filename_that_exists() {
        final String new_file_name = "baz";
        context.checking(new Expectations() {

            {
                ignoring(mockInterpretable).getArguments();
                will(returnValue(new String[] { new_file_name }));
                one(mockAccessor).writeLine("touch: " + new_file_name + ": File already exists");
            }
        });
        Touch touchCommand = new Touch();
        assertEquals(-1, touchCommand.execute(mockInterpretable));
    }

    @Test
    public void should_print_parent_dir_not_found_message_when_asked_to_create_file_under_a_dir_that_does_not_exist() {
        final String new_file_name = "yada/baz";
        context.checking(new Expectations() {

            {
                ignoring(mockInterpretable).getArguments();
                will(returnValue(new String[] { new_file_name }));
                one(mockAccessor).writeLine("touch: Directory requested in path `" + new_file_name + "` does not exist");
            }
        });
        Touch touchCommand = new Touch();
        assertEquals(-1, touchCommand.execute(mockInterpretable));
    }

    @Test
    public void should_crib_if_no_filename_is_given() {
        context.checking(new Expectations() {

            {
                one(mockInterpretable).getArguments();
                will(returnValue(new String[] {}));
                one(mockAccessor).writeLine("touch: No file-name given");
            }
        });
        Touch touchCommand = new Touch();
        assertEquals(-1, touchCommand.execute(mockInterpretable));
    }

    @Test
    public void should_crib_if_empty_filename_is_given() {
        final String new_file_name = "";
        context.checking(new Expectations() {

            {
                exactly(3).of(mockInterpretable).getArguments();
                will(returnValue(new String[] { new_file_name }));
                one(mockAccessor).writeLine("touch: No file-name given");
            }
        });
        Touch touchCommand = new Touch();
        assertEquals(-1, touchCommand.execute(mockInterpretable));
    }
}
