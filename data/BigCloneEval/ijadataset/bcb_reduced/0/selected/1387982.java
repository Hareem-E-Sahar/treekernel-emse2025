package ca.compsci.opent.compiler;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.jar.*;
import java.util.zip.*;
import ca.compsci.opent.compiler.codegen.BytecodeGenerator;
import ca.compsci.opent.compiler.semantics.*;
import ca.compsci.opent.compiler.lexer.Lexer;
import ca.compsci.opent.compiler.lexer.LexerException;
import ca.compsci.opent.compiler.node.Start;
import ca.compsci.opent.compiler.optimizer.PostSemanticOptimizer;
import ca.compsci.opent.compiler.parser.Parser;
import ca.compsci.opent.compiler.parser.ParserException;

public class Main {

    public static final String COMPILED_EXT = ".otc";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("No file specified!");
            System.exit(1);
        }
        File file = new File(args[0]);
        FileReader file_reader = new FileReader(file);
        try {
            Lexer lexer = new Lexer(new PushbackReader(new BufferedReader(file_reader)));
            Parser parser = new Parser(lexer);
            Start ast = parser.parse();
            SemanticAnalyzer analyzer = new SemanticAnalyzer(file.getName());
            ast.apply(analyzer);
            SymbolTable table = analyzer.getTable();
            ast.apply(new PostSemanticOptimizer(table));
            BytecodeGenerator generator = new BytecodeGenerator(file.getParentFile(), table);
            ast.apply(generator);
            byte[] bytecode = generator.getBytecode();
            Manifest manifest = new Manifest();
            Attributes main = manifest.getMainAttributes();
            main.putValue("Manifest-Version", "1.0");
            main.putValue("Main-Class", "Main");
            String out_name;
            if (file.getName().indexOf(".") > -1) out_name = file.getName().replaceFirst("\\..+$", COMPILED_EXT); else out_name = file.getName() + COMPILED_EXT;
            JarOutputStream out = new JarOutputStream(new FileOutputStream(out_name), manifest);
            ZipEntry clazz = new ZipEntry(BytecodeGenerator.CLASS_NAME + ".class");
            clazz.setSize(bytecode.length);
            CRC32 crc = new CRC32();
            crc.update(bytecode);
            clazz.setCrc(crc.getValue());
            clazz.setTime(System.currentTimeMillis());
            out.putNextEntry(clazz);
            WritableByteChannel byte_channel = Channels.newChannel(out);
            try {
                byte_channel.write(ByteBuffer.wrap(bytecode));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            byte_channel.close();
        } catch (LexerException le) {
            Utils.reportSyntaxError(le, file.getName());
        } catch (ParserException pe) {
            Utils.reportSyntaxError(pe, file.getName());
        } catch (SemanticException se) {
        }
    }
}
