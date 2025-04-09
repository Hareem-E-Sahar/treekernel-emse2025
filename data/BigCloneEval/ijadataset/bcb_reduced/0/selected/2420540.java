package itcl.lang;

import tcl.lang.*;

class EnsemblePart {

    String name;

    int minChars;

    Command cmd;

    WrappedCommand wcmd;

    String usage;

    Ensemble ensemble;
}

class EnsembleParser implements AssocData {

    Interp master;

    Interp parser;

    Ensemble ensData;

    public void disposeAssocData(Interp interp) {
        Ensemble.DeleteEnsParser(this, this.master);
    }
}

class ItclEnsInvoc implements InternalRep {

    EnsemblePart ensPart;

    TclObject chainObj;

    public InternalRep duplicate() {
        return Ensemble.DupEnsInvocInternalRep(this);
    }

    public void dispose() {
        Ensemble.FreeEnsInvocInternalRep(this);
    }

    public String toString() {
        return Ensemble.UpdateStringOfEnsInvoc(this);
    }

    public static TclObject newInstance() {
        return new TclObject(new ItclEnsInvoc());
    }
}

class Ensemble {

    Interp interp;

    EnsemblePart[] parts;

    int numParts;

    int maxParts;

    WrappedCommand wcmd;

    EnsemblePart parent;

    static String[] SplitEnsemble(Interp interp, String ensName) throws TclException {
        TclObject list = TclString.newInstance(ensName);
        TclObject[] objArgv;
        String[] strArgv;
        objArgv = TclList.getElements(interp, list);
        strArgv = new String[objArgv.length];
        for (int i = 0; i < objArgv.length; i++) {
            strArgv[i] = objArgv[i].toString();
        }
        return strArgv;
    }

    static String MergeEnsemble(Interp interp, String[] nameArgv, int nameArgc) throws TclException {
        TclObject list = TclList.newInstance();
        for (int i = 0; i < nameArgc; i++) {
            TclList.append(interp, list, TclString.newInstance(nameArgv[i]));
        }
        return list.toString();
    }

    static void EnsembleInit(Interp interp) {
        interp.createCommand("::itcl::ensemble", new EnsembleCmd(null));
    }

    static void CreateEnsemble(Interp interp, String ensName) throws TclException {
        Ensemble parentEnsData = null;
        TclObject list;
        String[] nameArgv = null;
        try {
            nameArgv = SplitEnsemble(interp, ensName);
        } catch (TclException ex) {
            CreateEnsembleFailed(interp, ensName, ex);
        }
        if (nameArgv.length < 1) {
            TclException ex = new TclException(interp, "invalid ensemble name \"" + ensName + "\"");
            CreateEnsembleFailed(interp, ensName, ex);
        }
        parentEnsData = null;
        if (nameArgv.length > 1) {
            try {
                parentEnsData = FindEnsemble(interp, nameArgv, nameArgv.length - 1);
            } catch (TclException ex) {
                CreateEnsembleFailed(interp, ensName, ex);
            }
            if (parentEnsData == null) {
                String pname = MergeEnsemble(interp, nameArgv, nameArgv.length - 1);
                TclException ex = new TclException(interp, "invalid ensemble name \"" + pname + "\"");
                CreateEnsembleFailed(interp, ensName, ex);
            }
        }
        try {
            CreateEnsemble(interp, parentEnsData, nameArgv[nameArgv.length - 1]);
        } catch (TclException ex) {
            CreateEnsembleFailed(interp, ensName, ex);
        }
    }

    static void CreateEnsembleFailed(Interp interp, String ensName, TclException ex) throws TclException {
        StringBuffer buffer = new StringBuffer(64);
        buffer.append("\n    (while creating ensemble \"");
        buffer.append(ensName);
        buffer.append("\")");
        interp.addErrorInfo(buffer.toString());
        throw ex;
    }

    static void AddEnsemblePart(Interp interp, String ensName, String partName, String usageInfo, Command objCmd) throws TclException {
        String[] nameArgv = null;
        Ensemble ensData = null;
        EnsemblePart ensPart;
        try {
            nameArgv = SplitEnsemble(interp, ensName);
        } catch (TclException ex) {
            AddEnsemblePartFailed(interp, ensName, ex);
        }
        try {
            ensData = FindEnsemble(interp, nameArgv, nameArgv.length);
        } catch (TclException ex) {
            AddEnsemblePartFailed(interp, ensName, ex);
        }
        if (ensData == null) {
            String pname = MergeEnsemble(interp, nameArgv, nameArgv.length);
            TclException ex = new TclException(interp, "invalid ensemble name \"" + pname + "\"");
            AddEnsemblePartFailed(interp, ensName, ex);
        }
        try {
            ensPart = AddEnsemblePart(interp, ensData, partName, usageInfo, objCmd);
        } catch (TclException ex) {
            AddEnsemblePartFailed(interp, ensName, ex);
        }
    }

    static void AddEnsemblePartFailed(Interp interp, String ensName, TclException ex) throws TclException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\n    (while adding to ensemble \"");
        buffer.append(ensName);
        buffer.append("\")");
        interp.addErrorInfo(buffer.toString());
        throw ex;
    }

    static boolean GetEnsembleUsage(Interp interp, String ensName, StringBuffer buffer) {
        String[] nameArgv = null;
        Ensemble ensData;
        Itcl_InterpState state;
        String retval;
        state = Util.SaveInterpState(interp, 0);
        try {
            nameArgv = SplitEnsemble(interp, ensName);
        } catch (TclException ex) {
            Util.RestoreInterpState(interp, state);
            return false;
        }
        try {
            ensData = FindEnsemble(interp, nameArgv, nameArgv.length);
        } catch (TclException ex) {
            Util.RestoreInterpState(interp, state);
            return false;
        }
        if (ensData == null) {
            Util.RestoreInterpState(interp, state);
            return false;
        }
        GetEnsembleUsage(ensData, buffer);
        Util.DiscardInterpState(state);
        return true;
    }

    static boolean GetEnsembleUsageForObj(Interp interp, TclObject ensObj, StringBuffer buffer) {
        Ensemble ensData;
        TclObject chainObj = null;
        Command cmd;
        chainObj = ensObj;
        while (chainObj != null && (chainObj.getInternalRep() instanceof ItclEnsInvoc)) {
            ItclEnsInvoc t = (ItclEnsInvoc) chainObj.getInternalRep();
            chainObj = t.chainObj;
        }
        if (chainObj != null) {
            cmd = interp.getCommand(chainObj.toString());
            if (cmd != null && (cmd instanceof HandleEnsemble)) {
                ensData = ((HandleEnsemble) cmd).ensData;
                GetEnsembleUsage(ensData, buffer);
                return true;
            }
        }
        return false;
    }

    static void GetEnsembleUsage(Ensemble ensData, StringBuffer buffer) {
        String spaces = "  ";
        boolean isOpenEnded = false;
        EnsemblePart ensPart;
        for (int i = 0; i < ensData.numParts; i++) {
            ensPart = ensData.parts[i];
            if (ensPart.name.equals("@error")) {
                isOpenEnded = true;
            } else {
                buffer.append(spaces);
                GetEnsemblePartUsage(ensPart, buffer);
                spaces = "\n  ";
            }
        }
        if (isOpenEnded) {
            buffer.append("\n...and others described on the man page");
        }
    }

    static void GetEnsemblePartUsage(EnsemblePart ensPart, StringBuffer buffer) {
        EnsemblePart part;
        WrappedCommand wcmd;
        String name;
        Itcl_List trail;
        Itcl_ListElem elem;
        trail = new Itcl_List();
        Util.InitList(trail);
        for (part = ensPart; part != null; part = part.ensemble.parent) {
            Util.InsertList(trail, part);
        }
        wcmd = ensPart.ensemble.wcmd;
        name = ensPart.ensemble.interp.getCommandName(wcmd);
        Util.AppendElement(buffer, name);
        for (elem = Util.FirstListElem(trail); elem != null; elem = Util.NextListElem(elem)) {
            part = (EnsemblePart) Util.GetListValue(elem);
            Util.AppendElement(buffer, part.name);
        }
        Util.DeleteList(trail);
        if (ensPart.usage != null && ensPart.usage.length() > 0) {
            buffer.append(" ");
            buffer.append(ensPart.usage);
        } else if (ensPart.cmd != null && (ensPart.cmd instanceof HandleEnsemble)) {
            buffer.append(" option ?arg arg ...?");
        }
    }

    static void CreateEnsemble(Interp interp, Ensemble parentEnsData, String ensName) throws TclException {
        Ensemble ensData;
        EnsemblePart ensPart;
        WrappedCommand wcmd;
        ensData = new Ensemble();
        ensData.interp = interp;
        ensData.numParts = 0;
        ensData.maxParts = 10;
        ensData.parts = new EnsemblePart[ensData.maxParts];
        ensData.wcmd = null;
        ensData.parent = null;
        if (parentEnsData == null) {
            interp.createCommand(ensName, new HandleEnsemble(ensData));
            wcmd = Namespace.findCommand(interp, ensName, null, TCL.NAMESPACE_ONLY);
            ensData.wcmd = wcmd;
            return;
        }
        try {
            ensPart = CreateEnsemblePart(interp, parentEnsData, ensName);
        } catch (TclException ex) {
            DeleteEnsemble(ensData);
            throw ex;
        }
        ensData.wcmd = parentEnsData.wcmd;
        ensData.parent = ensPart;
        ensPart.cmd = new HandleEnsemble(ensData);
    }

    static EnsemblePart AddEnsemblePart(Interp interp, Ensemble ensData, String partName, String usageInfo, Command objProc) throws TclException {
        EnsemblePart ensPart;
        WrappedCommand wcmd;
        ensPart = CreateEnsemblePart(interp, ensData, partName);
        if (usageInfo != null) {
            ensPart.usage = usageInfo;
        }
        wcmd = new WrappedCommand();
        wcmd.ns = ensData.wcmd.ns;
        wcmd.cmd = objProc;
        ensPart.cmd = objProc;
        ensPart.wcmd = wcmd;
        return ensPart;
    }

    static void DeleteEnsemble(Ensemble ensData) {
        while (ensData.numParts > 0) {
            DeleteEnsemblePart(ensData.parts[0]);
        }
        ensData.parts = null;
    }

    static Ensemble FindEnsemble(Interp interp, String[] nameArgv, int nameArgc) throws TclException {
        WrappedCommand wcmd;
        Command cmd;
        Ensemble ensData;
        EnsemblePart ensPart;
        if (nameArgc < 1) {
            return null;
        }
        wcmd = Namespace.findCommand(interp, nameArgv[0], null, TCL.LEAVE_ERR_MSG);
        if (wcmd == null || !(wcmd.cmd instanceof HandleEnsemble)) {
            throw new TclException(interp, "command \"" + nameArgv[0] + "\" is not an ensemble");
        }
        ensData = ((HandleEnsemble) wcmd.cmd).ensData;
        for (int i = 1; i < nameArgc; i++) {
            ensPart = FindEnsemblePart(interp, ensData, nameArgv[i]);
            if (ensPart == null) {
                String pname = MergeEnsemble(interp, nameArgv, i);
                TclException ex = new TclException(interp, "invalid ensemble name \"" + pname + "\"");
            }
            cmd = ensPart.cmd;
            if (cmd == null || !(cmd instanceof HandleEnsemble)) {
                throw new TclException(interp, "part \"" + nameArgv[i] + "\" is not an ensemble");
            }
            ensData = ((HandleEnsemble) cmd).ensData;
        }
        return ensData;
    }

    static EnsemblePart CreateEnsemblePart(Interp interp, Ensemble ensData, String partName) throws TclException {
        int i, pos;
        EnsemblePart[] partList;
        EnsemblePart part;
        FindEnsemblePartIndexResult res = FindEnsemblePartIndex(ensData, partName);
        if (res.status) {
            throw new TclException(interp, "part \"" + partName + "\" already exists in ensemble");
        }
        pos = res.pos;
        if (ensData.numParts >= ensData.maxParts) {
            partList = new EnsemblePart[ensData.maxParts * 2];
            for (i = 0; i < ensData.maxParts; i++) {
                partList[i] = ensData.parts[i];
            }
            ensData.parts = null;
            ensData.parts = partList;
            ensData.maxParts = partList.length;
        }
        for (i = ensData.numParts; i > pos; i--) {
            ensData.parts[i] = ensData.parts[i - 1];
        }
        ensData.numParts++;
        part = new EnsemblePart();
        part.name = partName;
        part.cmd = null;
        part.usage = null;
        part.ensemble = ensData;
        ensData.parts[pos] = part;
        ComputeMinChars(ensData, pos);
        ComputeMinChars(ensData, pos - 1);
        ComputeMinChars(ensData, pos + 1);
        return part;
    }

    static void DeleteEnsemblePart(EnsemblePart ensPart) {
        int i, pos;
        Ensemble ensData;
        Command cmd = ensPart.cmd;
        if (cmd instanceof CommandWithDispose) {
            ((CommandWithDispose) cmd).disposeCmd();
        }
        ensPart.cmd = null;
        FindEnsemblePartIndexResult res = FindEnsemblePartIndex(ensPart.ensemble, ensPart.name);
        if (res.status) {
            pos = res.pos;
            ensData = ensPart.ensemble;
            for (i = pos; i < ensData.numParts - 1; i++) {
                ensData.parts[i] = ensData.parts[i + 1];
            }
            ensData.numParts--;
        }
        if (ensPart.usage != null) {
            ensPart.usage = null;
        }
        ensPart.name = null;
    }

    static EnsemblePart FindEnsemblePart(Interp interp, Ensemble ensData, String partName) throws TclException {
        int pos = 0;
        int first, last, nlen;
        int i, cmp;
        EnsemblePart rensPart = null;
        first = 0;
        last = ensData.numParts - 1;
        nlen = partName.length();
        while (last >= first) {
            pos = (first + last) / 2;
            if (partName.charAt(0) == ensData.parts[pos].name.charAt(0)) {
                cmp = partName.substring(0, nlen).compareTo(ensData.parts[pos].name);
                if (cmp == 0) {
                    break;
                }
            } else if (partName.charAt(0) < ensData.parts[pos].name.charAt(0)) {
                cmp = -1;
            } else {
                cmp = 1;
            }
            if (cmp > 0) {
                first = pos + 1;
            } else {
                last = pos - 1;
            }
        }
        if (last < first) {
            return rensPart;
        }
        if (nlen < ensData.parts[pos].minChars) {
            while (pos > 0) {
                pos--;
                if (partName.substring(0, nlen).compareTo(ensData.parts[pos].name) != 0) {
                    pos++;
                    break;
                }
            }
        }
        if (nlen < ensData.parts[pos].minChars) {
            StringBuffer buffer = new StringBuffer(64);
            buffer.append("ambiguous option \"" + partName + "\": should be one of...");
            for (i = pos; i < ensData.numParts; i++) {
                if (partName.substring(0, nlen).compareTo(ensData.parts[i].name) != 0) {
                    break;
                }
                buffer.append("\n  ");
                GetEnsemblePartUsage(ensData.parts[i], buffer);
            }
            throw new TclException(interp, buffer.toString());
        }
        rensPart = ensData.parts[pos];
        return rensPart;
    }

    static FindEnsemblePartIndexResult FindEnsemblePartIndex(Ensemble ensData, String partName) {
        int pos = 0;
        int first, last;
        int cmp;
        int posRes;
        first = 0;
        last = ensData.numParts - 1;
        while (last >= first) {
            pos = (first + last) / 2;
            if (partName.charAt(0) == ensData.parts[pos].name.charAt(0)) {
                cmp = partName.compareTo(ensData.parts[pos].name);
                if (cmp == 0) {
                    break;
                }
            } else if (partName.charAt(0) < ensData.parts[pos].name.charAt(0)) {
                cmp = -1;
            } else {
                cmp = 1;
            }
            if (cmp > 0) {
                first = pos + 1;
            } else {
                last = pos - 1;
            }
        }
        FindEnsemblePartIndexResult res = new FindEnsemblePartIndexResult();
        if (last >= first) {
            res.status = true;
            res.pos = pos;
            return res;
        }
        res.status = false;
        res.pos = first;
        return res;
    }

    static class FindEnsemblePartIndexResult {

        boolean status;

        int pos;
    }

    static void ComputeMinChars(Ensemble ensData, int pos) {
        int min, max;
        int p, q;
        String pstr, qstr;
        if (pos < 0 || pos >= ensData.numParts) {
            return;
        }
        ensData.parts[pos].minChars = 1;
        if (pos - 1 >= 0) {
            pstr = ensData.parts[pos].name;
            p = 0;
            qstr = ensData.parts[pos - 1].name;
            q = 0;
            final int plen = pstr.length();
            final int qlen = qstr.length();
            for (min = 1; p < plen && q < qlen && pstr.charAt(p) == qstr.charAt(q); min++) {
                p++;
                q++;
            }
            if (min > ensData.parts[pos].minChars) {
                ensData.parts[pos].minChars = min;
            }
        }
        if (pos + 1 < ensData.numParts) {
            pstr = ensData.parts[pos].name;
            p = 0;
            qstr = ensData.parts[pos + 1].name;
            q = 0;
            final int plen = pstr.length();
            final int qlen = qstr.length();
            for (min = 1; p < plen && q < qlen && pstr.charAt(p) == qstr.charAt(q); min++) {
                p++;
                q++;
            }
            if (min > ensData.parts[pos].minChars) {
                ensData.parts[pos].minChars = min;
            }
        }
        max = ensData.parts[pos].name.length();
        if (ensData.parts[pos].minChars > max) {
            ensData.parts[pos].minChars = max;
        }
    }

    static class HandleEnsemble implements CommandWithDispose {

        Ensemble ensData;

        HandleEnsemble(Ensemble ensData) {
            this.ensData = ensData;
        }

        public void disposeCmd() {
            DeleteEnsemble(ensData);
        }

        public void cmdProc(Interp interp, TclObject[] objv) throws TclException {
            Command cmd;
            EnsemblePart ensPart;
            String partName;
            final int partNameLen;
            TclObject cmdline, chainObj;
            TclObject[] cmdlinev;
            if (objv.length < 2) {
                StringBuffer buffer = new StringBuffer(64);
                buffer.append("wrong # args: should be one of...\n");
                GetEnsembleUsage(ensData, buffer);
                throw new TclException(interp, buffer.toString());
            }
            partName = objv[1].toString();
            partNameLen = partName.length();
            ensPart = FindEnsemblePart(interp, ensData, partName);
            if (ensPart == null) {
                ensPart = FindEnsemblePart(interp, ensData, "@error");
                if (ensPart != null) {
                    cmd = ensPart.cmd;
                    cmd.cmdProc(interp, objv);
                    return;
                }
            }
            if (ensPart == null) {
                EnsembleErrorCmd(ensData, interp, objv, 1);
            }
            chainObj = ItclEnsInvoc.newInstance();
            ItclEnsInvoc irep = (ItclEnsInvoc) chainObj.getInternalRep();
            irep.ensPart = ensPart;
            irep.chainObj = objv[0];
            objv[1].preserve();
            objv[0].preserve();
            cmdline = TclList.newInstance();
            TclList.append(interp, cmdline, chainObj);
            for (int i = 2; i < objv.length; i++) {
                TclList.append(interp, cmdline, objv[i]);
            }
            cmdline.preserve();
            try {
                cmdlinev = TclList.getElements(interp, cmdline);
                cmd = ensPart.cmd;
                cmd.cmdProc(interp, cmdlinev);
            } finally {
                cmdline.release();
            }
        }
    }

    static class EnsembleCmd implements Command {

        EnsembleParser ensParser;

        EnsembleCmd(EnsembleParser ensParser) {
            this.ensParser = ensParser;
        }

        public void cmdProc(Interp interp, TclObject[] objv) throws TclException {
            String ensName;
            EnsembleParser ensInfo;
            Ensemble ensData, savedEnsData;
            EnsemblePart ensPart;
            WrappedCommand wcmd;
            Command cmd;
            if (objv.length < 2) {
                StringBuffer buffer = new StringBuffer(64);
                buffer.append("wrong # args: should be \"");
                buffer.append(objv[0].toString());
                buffer.append(" name ?command arg arg...?\"");
                throw new TclException(interp, buffer.toString());
            }
            if (ensParser != null) {
                ensInfo = ensParser;
            } else {
                ensInfo = GetEnsembleParser(interp);
            }
            ensData = ensInfo.ensData;
            ensName = objv[1].toString();
            if (ensData != null) {
                try {
                    ensPart = FindEnsemblePart(interp, ensData, ensName);
                } catch (TclException ex) {
                    ensPart = null;
                }
                if (ensPart == null) {
                    CreateEnsemble(interp, ensData, ensName);
                    try {
                        ensPart = FindEnsemblePart(interp, ensData, ensName);
                    } catch (TclException ex) {
                        ensPart = null;
                    }
                    Util.Assert(ensPart != null, "Itcl_EnsembleCmd: can't create ensemble");
                }
                cmd = ensPart.cmd;
                if (cmd == null || !(cmd instanceof HandleEnsemble)) {
                    throw new TclException(interp, "part \"" + objv[1].toString() + "\" is not an ensemble");
                }
                ensData = ((HandleEnsemble) cmd).ensData;
            } else {
                try {
                    wcmd = Namespace.findCommand(interp, ensName, null, 0);
                } catch (TclException ex) {
                    wcmd = null;
                }
                if (wcmd == null) {
                    CreateEnsemble(interp, null, ensName);
                    wcmd = Namespace.findCommand(interp, ensName, null, 0);
                }
                if (wcmd == null) {
                    cmd = null;
                } else {
                    cmd = wcmd.cmd;
                }
                if (cmd == null || !(cmd instanceof HandleEnsemble)) {
                    throw new TclException(interp, "command \"" + objv[1].toString() + "\" is not an ensemble");
                }
                ensData = ((HandleEnsemble) cmd).ensData;
            }
            TclException evalEx = null;
            savedEnsData = ensInfo.ensData;
            ensInfo.ensData = ensData;
            if (objv.length == 3) {
                try {
                    ensInfo.parser.eval(objv[2].toString());
                } catch (TclException ex) {
                    evalEx = ex;
                }
            } else if (objv.length > 3) {
                TclObject tlist = TclList.newInstance();
                for (int i = 2; i < objv.length; i++) {
                    TclList.append(interp, tlist, objv[i]);
                }
                try {
                    ensInfo.parser.eval(tlist.toString());
                } catch (TclException ex) {
                    evalEx = ex;
                }
            }
            if (evalEx != null) {
                TclObject errInfoObj = ensInfo.parser.getVar("::errorInfo", TCL.GLOBAL_ONLY);
                if (errInfoObj != null) {
                    interp.addErrorInfo(errInfoObj.toString());
                }
                if (objv.length == 3) {
                    String msg = "\n    (\"ensemble\" body line " + ensInfo.parser.getErrorLine() + ")";
                    interp.addErrorInfo(msg);
                }
                ensInfo.ensData = savedEnsData;
                throw new TclException(interp, ensInfo.parser.getResult().toString());
            }
            ensInfo.ensData = savedEnsData;
            interp.setResult(ensInfo.parser.getResult().toString());
        }
    }

    static EnsembleParser GetEnsembleParser(Interp interp) {
        Namespace ns, childNs;
        EnsembleParser ensInfo;
        WrappedCommand wcmd;
        ensInfo = (EnsembleParser) interp.getAssocData("itcl_ensembleParser");
        if (ensInfo != null) {
            return ensInfo;
        }
        ensInfo = new EnsembleParser();
        ensInfo.master = interp;
        ensInfo.parser = new Interp();
        ensInfo.ensData = null;
        ns = Namespace.getGlobalNamespace(ensInfo.parser);
        while ((childNs = (Namespace) ItclAccess.FirstHashEntry(ns.childTable)) != null) {
            Namespace.deleteNamespace(childNs);
        }
        while ((wcmd = (WrappedCommand) ItclAccess.FirstHashEntry(ns.cmdTable)) != null) {
            ensInfo.parser.deleteCommandFromToken(wcmd);
        }
        ensInfo.parser.createCommand("part", new EnsPartCmd(ensInfo));
        ensInfo.parser.createCommand("option", new EnsPartCmd(ensInfo));
        ensInfo.parser.createCommand("ensemble", new EnsembleCmd(ensInfo));
        interp.setAssocData("itcl_ensembleParser", ensInfo);
        return ensInfo;
    }

    static void DeleteEnsParser(EnsembleParser ensInfo, Interp interp) {
        ensInfo.parser.dispose();
    }

    static class EnsPartCmd implements Command {

        EnsembleParser ensParser;

        EnsPartCmd(EnsembleParser ensParser) {
            this.ensParser = ensParser;
        }

        public void cmdProc(Interp interp, TclObject[] objv) throws TclException {
            EnsembleParser ensInfo = ensParser;
            Ensemble ensData = ensInfo.ensData;
            boolean varArgs, space;
            String partName, usage;
            Procedure proc;
            WrappedCommand wcmd;
            EnsemblePart ensPart;
            StringBuffer buffer;
            if (objv.length != 4) {
                throw new TclException(interp, "wrong # args: should be \"" + objv[0].toString() + " name args body\"");
            }
            partName = objv[1].toString();
            wcmd = ensData.wcmd;
            proc = ItclAccess.newProcedure(interp, wcmd.ns, partName, objv[2], objv[3], "unknown", 0);
            buffer = new StringBuffer();
            varArgs = false;
            space = false;
            TclObject[][] argList = ItclAccess.getArgList(proc);
            for (int i = 0; i < argList.length; i++) {
                TclObject vname = argList[i][0];
                TclObject def = argList[i][1];
                varArgs = false;
                if (vname.toString().equals("args")) {
                    varArgs = true;
                } else if (def != null) {
                    if (space) {
                        buffer.append(" ");
                    }
                    buffer.append("?");
                    buffer.append(vname);
                    buffer.append("?");
                    space = true;
                } else {
                    if (space) {
                        buffer.append(" ");
                    }
                    buffer.append(vname);
                    space = true;
                }
            }
            if (varArgs) {
                if (space) {
                    buffer.append(" ");
                }
                buffer.append("?arg arg ...?");
            }
            usage = buffer.toString();
            ensPart = AddEnsemblePart(interp, ensData, partName, usage, proc);
            ItclAccess.setWrappedCommand(proc, ensPart.wcmd);
        }
    }

    static void EnsembleErrorCmd(Ensemble ensData, Interp interp, TclObject[] objv, int skip) throws TclException {
        String cmdName;
        StringBuffer buffer = new StringBuffer(64);
        cmdName = objv[skip].toString();
        buffer.append("bad option \"");
        buffer.append(cmdName);
        buffer.append("\": should be one of...\n");
        GetEnsembleUsage(ensData, buffer);
        throw new TclException(interp, buffer.toString());
    }

    static void FreeEnsInvocInternalRep(ItclEnsInvoc obj) {
        TclObject prevArgObj = obj.chainObj;
        if (prevArgObj != null) {
            prevArgObj.release();
        }
    }

    static InternalRep DupEnsInvocInternalRep(ItclEnsInvoc obj) {
        ItclEnsInvoc dup = new ItclEnsInvoc();
        dup.ensPart = obj.ensPart;
        dup.chainObj = obj.chainObj;
        if (dup.chainObj != null) {
            dup.chainObj.preserve();
        }
        return dup;
    }

    static void SetEnsInvocFromAny(Interp interp, TclObject obj) throws TclException {
    }

    static String UpdateStringOfEnsInvoc(ItclEnsInvoc obj) {
        EnsemblePart ensPart = obj.ensPart;
        TclObject chainObj = obj.chainObj;
        StringBuffer buffer = new StringBuffer(64);
        int length;
        String name;
        if (chainObj != null) {
            name = chainObj.toString();
            buffer.append(name);
        }
        if (ensPart != null) {
            Util.AppendElement(buffer, ensPart.name);
        }
        return buffer.toString();
    }
}
