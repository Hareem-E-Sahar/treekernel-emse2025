public class Test {        MIBTableRow(String tableName, String tablePrefix, String rowName, String rowOid, String mode) throws NotEnoughInformationException {
            this.tableName = tableName;
            this.tablePrefix = tablePrefix;
            this.name = rowName;
            this.objectId = rowOid;
            this.typeName = this.name.substring(0, 1).toUpperCase() + this.name.substring(1);
            this.syntax = rowName;
            this.maxAccess = "not-accessible";
            this.status = "current";
            this.description = "";
            this.rowObjects = new ArrayList<MIBObject>(1);
            MappedAttribute index = new MappedAttribute();
            index.setSnmpType("DisplayString");
            index.setName("index");
            index.setOid(".1");
            index.setMode("ro");
            index.setOidDefName(this.name);
            index.setMaxAccess("not-accessible");
            index.setStatus("current");
            rowObjects.add(new MIBObject(index, true));
            this.indexName = "index";
            MappedAttribute element = new MappedAttribute();
            element.setSnmpType("DisplayString");
            element.setName("element");
            element.setOid(".2");
            element.setMode(mode);
            element.setOidDefName(this.name);
            if (element.isReadWrite()) element.setMaxAccess("read-write"); else element.setMaxAccess("read-only");
            element.setStatus("current");
            rowObjects.add(new MIBObject(element, true));
        }
}