package com.amd.javalabs.tools.disasm.builders;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import com.amd.javalabs.tools.disasm.InstructionBuildContext;
import com.amd.javalabs.tools.disasm.Mnemonic;
import com.amd.javalabs.tools.disasm.ModRegRM;
import com.amd.javalabs.tools.disasm.instructions.Instruction;
import com.amd.javalabs.tools.disasm.instructions.OpcodeEncoding;
import com.amd.javalabs.tools.disasm.instructions.OpcodeTableEntry;

public class InstructionBuilder implements OpcodeTableEntry {

    protected Mnemonic mnemonic;

    protected OpcodeEncoding opcodeEncoding;

    protected int opcode;

    protected Class<?> reflectiveClass = null;

    public int getOpcode() {
        return (opcode);
    }

    public InstructionBuilder(Class<?> _reflectiveClass, int _opcode, Mnemonic _mnemonic, OpcodeEncoding _mode) {
        reflectiveClass = _reflectiveClass;
        opcode = _opcode;
        mnemonic = _mnemonic;
        opcodeEncoding = _mode;
    }

    protected InstructionBuilder(int _opcode, Mnemonic _mnemonic, OpcodeEncoding _mode) {
        this(null, _opcode, _mnemonic, _mode);
    }

    protected InstructionBuilder(int _opcode, Mnemonic _mnemonic) {
        this(_opcode, _mnemonic, OpcodeEncoding.NONE);
    }

    public InstructionBuilder(Class<?> _reflectiveClass, int _opcode, Mnemonic _mnemonic) {
        this(_reflectiveClass, _opcode, _mnemonic, OpcodeEncoding.NONE);
    }

    public final Instruction build(InstructionBuildContext _ibc) {
        if (opcodeEncoding.isModRegRmByteUsed()) {
            _ibc.setModRegRM(new ModRegRM(_ibc.getByte(), _ibc.getPrefixes()));
            _ibc.stepByte();
        }
        _ibc.setOperands(opcodeEncoding.buildOperands(_ibc));
        Instruction instruction = null;
        if (reflectiveClass == null) {
            instruction = buildInstruction(_ibc);
        } else {
            Constructor<?> constructor;
            try {
                constructor = reflectiveClass.getConstructor(new Class[] { Mnemonic.class, InstructionBuildContext.class });
                instruction = (Instruction) constructor.newInstance(new Object[] { mnemonic, _ibc });
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return (instruction);
    }

    protected Instruction buildInstruction(InstructionBuildContext _ibc) {
        return new Instruction(mnemonic, _ibc);
    }

    public OpcodeEncoding getOpcodeEncoding() {
        return opcodeEncoding;
    }

    public Mnemonic getMnemonic() {
        return (mnemonic);
    }
}
