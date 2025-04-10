public class Test {    private void generateVerilogFile(ForgeFileHandler fileHandler, BlockDescriptor ibd, BlockDescriptor obd, int numBlocks) {
        final String taskName = ibd.getFunctionName();
        final int inBlockSize = ibd.getBlockOrganization().length;
        final int outBlockSize = obd.getBlockOrganization().length;
        final int outBlockSizeValid = outBlockSize + 1;
        final int numBlocksPad = numBlocks + 1;
        final int outputPadSize = 1 << obd.getByteWidth();
        final int inFifoBits = ibd.getByteWidth() * 8 - 1;
        final int outFifoBits = obd.getByteWidth() * 8 - 1;
        final int inBlocksWords = inBlockSize * numBlocks;
        final int inBlocksWordsPad = inBlockSize * numBlocksPad;
        final int outBlocksWords = outBlockSize * numBlocks;
        final int outBlocksWordsValid = outBlockSizeValid * numBlocks;
        final int outBlocksWordsPad = outBlockSize * numBlocksPad;
        final int outBlocksWordsPadValid = outBlockSizeValid * numBlocksPad;
        final boolean isInputFifo = (inBlockSize > 0);
        final boolean isOutputFifo = (outBlockSize > 0);
        String designVerilogIdentifier = ID.toVerilogIdentifier(ID.showLogical(design));
        boolean atbGenerateBMReport = EngineThread.getGenericJob().getUnscopedBooleanOptionValue(OptionRegistry.ATB_BENCHMARK_REPORT);
        boolean dumpCycles = EngineThread.getGenericJob().getUnscopedBooleanOptionValue(OptionRegistry.WRITE_CYCLE_C);
        try {
            int space = 0;
            PrintWriter pw = new PrintWriter(new FileWriter(fileHandler.getFile(TestBenchEngine.ATB)));
            pw.println("`timescale 1ns/1ps");
            pw.println("`define legacy_model // Some simulators cannot handle the syntax of the new memory models.  This define uses a simpler syntax for the memory models in the unisims library");
            final String simFile = fileHandler.getFile(VerilogTranslateEngine.SIMINCL).getAbsolutePath();
            pw.println("`include \"" + simFile + "\"");
            pw.println("`timescale 1ns/1ps");
            pw.println("");
            pw.println("module fixture();");
            space += 3;
            pw.println("");
            pw.println(pad(space) + "//io");
            pw.println(pad(space) + "integer        resultFile;");
            pw.println(pad(space) + "integer        clkCntFile;");
            if (dumpCycles) {
                pw.println(pad(space) + "integer        cycleFile;");
            }
            if (atbGenerateBMReport) {
                pw.println(pad(space) + "/* benchmarking .. */");
                pw.println(pad(space) + "integer    benchmarkReportFile;");
                pw.println(pad(space) + "integer    benchNumReads;");
                pw.println(pad(space) + "integer    benchConsumedBytes;");
                pw.println(pad(space) + "integer        benchNumWrites;");
                pw.println(pad(space) + "integer    benchProducedBytes;");
                pw.println(pad(space) + "integer        benchFirstReadCycle;");
                pw.println(pad(space) + "integer        benchLastWriteCycle;");
                pw.println(pad(space) + "integer    benchWorkInProgress;");
                pw.println(pad(space) + "real       benchThroughput;");
                pw.println(pad(space) + "integer    benchTotalCycles;");
                pw.println(pad(space) + "real       benchOverallInputUtil;");
                pw.println(pad(space) + "real       benchOverallOutputUtil;");
                pw.println(pad(space) + "real       benchOverallCoreUtil;");
                pw.println(pad(space) + "real       benchZoneInputUtil;");
                pw.println(pad(space) + "real       benchZoneOutputUtil;");
                pw.println(pad(space) + "real       benchZoneCoreUtil;");
                pw.println(pad(space) + "integer        benchCoreCyclesCounter;");
                pw.println(pad(space) + "integer        benchCoreCycles;");
                pw.println(pad(space) + "integer        benchIdleCyclesCounter;");
                pw.println(pad(space) + "integer        benchIdleCycles;");
                pw.println(pad(space) + "integer        benchIdleFlag;");
                pw.println(pad(space) + "real       benchIdlePercentage;");
                pw.println(pad(space) + "integer        benchInReadZone;");
                pw.println(pad(space) + "integer        benchInWriteZone;");
                pw.println(pad(space) + "integer        benchReadZoneCycles;");
                pw.println(pad(space) + "integer        benchReadZoneCyclesCounter;");
                pw.println(pad(space) + "integer        benchReadZoneCyclesCounterSave;");
                pw.println(pad(space) + "integer        benchWriteZoneCycles;");
                pw.println(pad(space) + "integer        benchWriteZoneCyclesCounter;");
                pw.println(pad(space) + "integer        benchWriteZoneCyclesCounterSave;");
                pw.println(pad(space) + "integer        benchCoreZoneCycles;");
                pw.println(pad(space) + "integer        benchCoreZoneCyclesCounter;");
                pw.println(pad(space) + "/* End benchmarking .. */");
            }
            pw.println(pad(space) + "//clock");
            pw.println(pad(space) + "reg            clk;");
            pw.println(pad(space) + "// set/reset");
            pw.println(pad(space) + "reg            LGSR;");
            final String results = fileHandler.getFile(TestBenchEngine.RESULTS).getAbsolutePath();
            final String resultsClocks = fileHandler.getFile(TestBenchEngine.DefaultTestBenchEngine.CLKS).getAbsolutePath();
            final String cycleResults = fileHandler.getFile(TestBenchEngine.BlockIOTestBenchEngine.CYCLES).getAbsolutePath();
            if (!isInputFifo && !isOutputFifo) {
                pw.println(pad(space) + "//initial begin  $dumpfile(\"waves.vcd\");  $dumpvars;end   ");
                pw.println(pad(space) + "assign glbl.GSR=LGSR;");
                pw.println(pad(space));
                pw.println(pad(space) + "initial");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "resultFile <= $fopen (\"" + results + "\");");
                pw.println(pad(space) + "clkCntFile <= $fopen (\"" + resultsClocks + "\");");
                if (dumpCycles) {
                    pw.println(pad(space) + "cycleFile <= $fopen (\"" + cycleResults + "\");");
                }
                if (atbGenerateBMReport) {
                    writeBenchmarkInit(pw, fileHandler, space);
                }
                pw.println(pad(space) + "clk <= 0;");
                pw.println(pad(space));
                pw.println(pad(space) + "#1 LGSR <= 0;");
                pw.println(pad(space));
                pw.println(pad(space) + "#1000 $fwrite (resultFile, \"PASSED\");");
                pw.println(pad(space));
                pw.println(pad(space) + "#1000 $finish(1);");
                pw.println(pad(space));
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println("");
                pw.print(pad(space) + "" + designVerilogIdentifier + " dut();");
                pw.println("");
                pw.println(pad(space) + "always #25 clk <= ~clk;");
                pw.println("");
                space -= 3;
            } else {
                pw.println(pad(space) + "// input data[block * cycle + 1 cycle for good/pad bytes]");
                pw.println(pad(space) + "// inBlockSize: " + inBlockSize + " outBlockSize: " + numBlocksPad);
                pw.println(pad(space) + "reg  [" + inFifoBits + ":0]    FSL_input[0:" + (inBlocksWordsPad) + "]; // sized to hold " + numBlocks + " input blocks of data and one block of pad");
                pw.println(pad(space) + "reg  [" + outFifoBits + ":0]    FSL_output[0:" + (outBlocksWordsPadValid) + "]; // sized to hold " + numBlocks + " output blocks of expected data, each including one word of valid, and one block of pad");
                pw.println(pad(space));
                pw.println(pad(space) + "reg  [" + outFifoBits + ":0]       out[0:" + (outBlockSize) + "];    // sized to hold one output block");
                pw.println(pad(space) + "reg  [31:0]    din_index;    // hold the index into FSL_input");
                pw.println(pad(space) + "reg  [31:0]    dout_index;   // hold the index into FSL_output");
                pw.println(pad(space) + "reg  [31:0]    dout_in_block_index; // holds the index within the current output block");
                pw.println(pad(space) + "reg            task_" + taskName + "_fail;");
                pw.println(pad(space) + "reg            task_" + taskName + "_finished;");
                pw.println(pad(space) + "reg  [" + outFifoBits + ":0]       dout_valid_index;   ");
                pw.println(pad(space) + "wire  [" + outFifoBits + ":0]      dout_valid;   // stays the same for an entire output block  ");
                pw.println(pad(space) + "reg  [31:0]    hangTimer;");
                pw.println(pad(space) + "reg  [31:0]    clockCount;");
                pw.println(pad(space) + "wire           read;");
                pw.println(pad(space) + "wire           write;");
                pw.println(pad(space) + "reg            inDataExists;");
                pw.println(pad(space) + "reg            outDataFull;");
                pw.println(pad(space) + "wire           fsl1_m_control;");
                pw.println(pad(space) + "wire [" + inFifoBits + ":0]    din;");
                pw.println(pad(space) + "wire [" + outFifoBits + ":0]   dout;");
                pw.println(pad(space) + "wire [" + outFifoBits + ":0]   expected; // expected value for current FSL1_M fifo data");
                pw.println(pad(space) + "wire [" + outFifoBits + ":0]   current_pad;  // a mask with ff for each data byte, and 0 for each pad byte");
                pw.println(pad(space) + "reg             startSimulation;");
                if (dumpCycles) {
                    pw.println(pad(space) + "reg  [31:0]     relativeCycleCount;");
                }
                pw.println(pad(space) + "");
                pw.println(pad(space) + "");
                pw.println(pad(space));
                pw.println(pad(space) + "//initial begin  $dumpfile(\"waves.vcd\");  $dumpvars;end   ");
                pw.println(pad(space) + "assign glbl.GSR=LGSR;");
                pw.println(pad(space));
                pw.println(pad(space) + "initial");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                final String inVecs = fileHandler.getFile(TestBenchEngine.BlockIOTestBenchEngine.INVECS).getAbsolutePath();
                final String outVecs = fileHandler.getFile(TestBenchEngine.BlockIOTestBenchEngine.OUTVECS).getAbsolutePath();
                pw.println(pad(space) + "$readmemh(\"" + inVecs + "\", FSL_input);");
                pw.println(pad(space) + "$readmemh(\"" + outVecs + "\", FSL_output);");
                pw.println(pad(space) + "LGSR <= 1;");
                pw.println(pad(space) + "hangTimer <= 0;");
                pw.println(pad(space) + "");
                pw.println(pad(space) + "resultFile <= $fopen (\"" + results + "\");");
                pw.println(pad(space) + "clkCntFile <= $fopen (\"" + resultsClocks + "\");");
                if (dumpCycles) {
                    pw.println(pad(space) + "cycleFile <= $fopen (\"" + cycleResults + "\");");
                }
                if (atbGenerateBMReport) {
                    writeBenchmarkInit(pw, fileHandler, space);
                }
                pw.println(pad(space) + "clk <= 0;");
                pw.println(pad(space) + "inDataExists <= 0;");
                pw.println(pad(space) + "dout_index <= 1;      // first element is valid data flag ");
                pw.println(pad(space) + "dout_in_block_index <= 0;");
                pw.println(pad(space) + "task_" + taskName + "_fail <= 0;");
                pw.println(pad(space) + "task_" + taskName + "_finished <= 0;");
                pw.println("");
                pw.println(pad(space) + "");
                pw.println(pad(space) + "outDataFull<=0;");
                pw.println(pad(space) + "clockCount <= 0;");
                pw.println(pad(space) + "din_index <= 0;");
                pw.println(pad(space) + "startSimulation <= 0;");
                pw.println(pad(space) + "");
                pw.println(pad(space) + "dout_valid_index <= 0;");
                if (dumpCycles) {
                    pw.println(pad(space) + "relativeCycleCount <= 0;");
                }
                pw.println("");
                pw.println(pad(space) + "#1 LGSR <= 0;");
                pw.println(pad(space) + "");
                pw.println(pad(space) + "#500 startSimulation <= 1;");
                pw.println(pad(space) + "#25 startSimulation <= 0; // stay high for half a clock");
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println("");
                pw.print(pad(space) + "" + designVerilogIdentifier + " dut(");
                if (isInputFifo) {
                    pw.println(".FSL0_S_READ(read),   .FSL0_S_DATA(din),  .FSL0_S_EXISTS(inDataExists), ");
                    pw.println(pad(space) + pad(designVerilogIdentifier.length() + 5) + ".FSL0_S_CONTROL(1'b0), .FSL0_S_CLK(clk),");
                    pw.print(pad(space + designVerilogIdentifier.length() + 5));
                }
                pw.println(".FSL1_M_WRITE(write), .FSL1_M_DATA(dout), .FSL1_M_FULL(outDataFull),");
                pw.println(pad(space + designVerilogIdentifier.length() + 5) + ".FSL1_M_CONTROL(fsl1_m_control), .FSL1_M_CLK(clk),");
                pw.println(pad(space + designVerilogIdentifier.length() + 5) + ".RESET(1'b0), // GSR will hit internal reset at start");
                pw.println(pad(space + designVerilogIdentifier.length() + 5) + ".CLK(clk));");
                pw.println("");
                pw.println(pad(space) + "always #25 clk <= ~clk;");
                pw.println("");
                pw.println(pad(space) + "assign din=FSL_input[din_index];");
                pw.println(pad(space) + "assign expected=FSL_output[dout_index];");
                pw.println(pad(space) + "assign current_pad=FSL_output[dout_in_block_index+(" + (outBlockSizeValid * numBlocks) + ")];");
                pw.println(pad(space) + "assign dout_valid=FSL_output[dout_valid_index];");
                pw.println("");
                pw.println("   //send to input fifo and read from output fifo");
                pw.println(pad(space) + "always @(posedge clk)");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                if (dumpCycles) {
                    pw.println(pad(space) + "if ((relativeCycleCount > 0) || read || write) begin");
                    space += 2;
                    pw.println(pad(space) + "$fwrite(cycleFile, \"%x %x %x %x %x\\n\", relativeCycleCount, din, read, dout, write);");
                    pw.println(pad(space) + "relativeCycleCount <= relativeCycleCount + 1;");
                    space -= 2;
                    pw.println(pad(space) + "end");
                }
                if (atbGenerateBMReport) {
                    writeBenchmarkAtClockEdge(pw, space, outBlocksWordsValid, numBlocks);
                }
                pw.println(pad(space) + "if (startSimulation === 1)");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "inDataExists <= 1;");
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println(pad(space) + "else if (read && din_index === " + (inBlocksWords - 1) + ") // reached the end of the input data");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "inDataExists <= 0;");
                pw.println(pad(space) + "din_index <= din_index + 1;");
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println(pad(space) + "else if (read && din_index < " + (inBlocksWords) + ") // only look at the data, the last block is pad which can be ignored for the input");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "inDataExists <= 1;");
                pw.println(pad(space) + "din_index <= din_index + 1;");
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println(pad(space) + "");
                pw.println(pad(space) + "// check that the output is the same as the expected, masking over the pad bytes in the current word");
                pw.println(pad(space) + "if (write && dout_index < " + (outBlocksWordsValid) + ") // output size * num blocks excluding the pad block.  output size includes the data valid word");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "//out[dout_in_block_index] <= dout;");
                pw.println(pad(space) + "if (dout_valid && (dout & current_pad) !== (expected & current_pad))");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "$fwrite(resultFile, \"FAIL: Incorrect result.  output block index %d (offset in block %d) expected %x found %x\\n\",");
                pw.println(pad(space + 8) + "(dout_index-1), dout_in_block_index, FSL_output[dout_index] & current_pad, dout & current_pad);");
                pw.println(pad(space) + "task_" + taskName + "_fail <= 1;");
                pw.println(pad(space) + "$finish;");
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println(pad(space));
                pw.println(pad(space) + "dout_index <= dout_index+1;");
                pw.println(pad(space) + "");
                pw.println(pad(space) + "if (dout_in_block_index === (" + outBlockSize + "-1)) // last word in block, increment valid_index and dout_index over dout_valid");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "dout_in_block_index <= 0;");
                pw.println(pad(space) + "dout_valid_index <= dout_valid_index+" + outBlockSizeValid + ";");
                pw.println(pad(space) + "dout_index <= dout_index+2;");
                pw.println(pad(space) + "$fwrite(clkCntFile,\"%d\\n\",clockCount);");
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println(pad(space) + "else");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "dout_in_block_index <= dout_in_block_index + 1;");
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 5;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println(pad(space) + "");
                pw.println(pad(space) + "if (write && dout_index >= " + (outBlocksWordsValid) + ")");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "$fwrite(resultFile, \"FAIL: extraneous output after last expected block finished\");");
                pw.println(pad(space) + "task_" + taskName + "_fail <= 1;");
                pw.println(pad(space) + "$finish;");
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println(pad(space) + "");
                pw.println(pad(space) + "if (dout_index === " + (outBlocksWordsValid) + "+1)");
                space += 2;
                {
                    pw.println(pad(space) + "begin");
                    space += 3;
                    pw.println(pad(space) + "$fwrite (resultFile, \"PASSED\");");
                    pw.println(pad(space) + "$finish(1);");
                }
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println(pad(space) + "");
                pw.println(pad(space) + "clockCount <= clockCount + 1;");
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println(pad(space) + "");
                pw.println(pad(space) + "// run a hang timer to make sure no one block takes too long");
                pw.println(pad(space) + "always @(posedge clk)");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "if (write)");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "hangTimer <= 0;");
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 2;
                pw.println(pad(space) + "else");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "hangTimer <= hangTimer + 1;");
                String hangExpire = "1500";
                try {
                    final Option op = EngineThread.getGenericJob().getOption(OptionRegistry.HANG_TIMER);
                    int HANGTIMER = Integer.parseInt(op.getValue(CodeLabel.UNSCOPED).toString(), 10);
                    hangExpire = Integer.toString(HANGTIMER);
                } catch (Exception e) {
                    hangExpire = "1500";
                }
                pw.println(pad(space) + "if (hangTimer > " + hangExpire + ")");
                space += 2;
                pw.println(pad(space) + "begin");
                space += 3;
                pw.println(pad(space) + "$fwrite (resultFile, \"FAIL: Hang Timer expired at input block offset %d\\n\", din_index);");
                pw.println(pad(space) + "$finish;");
                space -= 3;
                pw.println(pad(space) + "end");
                space -= 5;
                pw.println(pad(space) + "end // else: !if(write)");
                space -= 2;
                space -= 3;
                pw.println(pad(space) + "end // always @ (posedge clk)");
                space -= 5;
                pw.println("");
            }
            pw.println(pad(space) + "");
            pw.println(pad(space) + "endmodule // fixture");
            pw.close();
        } catch (IOException ioe) {
            final String name = fileHandler.getFile(TestBenchEngine.ATB).getAbsolutePath();
            EngineThread.getEngine().fatalError("Couldn't produce " + name + ": " + ioe);
        }
    }
}