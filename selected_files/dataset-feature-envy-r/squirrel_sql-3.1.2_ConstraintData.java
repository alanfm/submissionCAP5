package net.sourceforge.squirrel_sql.plugins.graph;

import java.util.Arrays;
import java.util.Vector;

public class ConstraintData {
    private String _pkTableName;
    private String _fkTableName;
    private String _constraintName;
    private boolean _nonDbConstraint;
    private ColumnInfo[] _columnInfos = new ColumnInfo[0];
    private boolean _showThisConstraintName;

    public ConstraintData(String pkTableName, String fkTableName, String constraintName) {
        this(pkTableName, fkTableName, constraintName, false);
    }

    public ConstraintData(ConstraintDataXmlBean constraintDataXmlBean) {
        XmlConverter.populateFromXml(this, constraintDataXmlBean);
    }

    public ConstraintData(String pkTableName, String fkTableName, String constraintName, boolean nonDbConstraint) {
        _pkTableName = pkTableName;
        _fkTableName = fkTableName;
        _constraintName = constraintName;
        _nonDbConstraint = nonDbConstraint;
    }

    public void addColumnInfo(ColumnInfo colInfo) {
        ColumnInfoManager.addColumn(_columnInfos, colInfo);
    }

    public String[] getDDL() {
        return DDLGenerator.generateDDL(this);
    }

    public void replaceCopiedColsByReferences(ColumnInfo[] colInfoRefs, boolean retainImportData) {
        ColumnInfoManager.replaceColumns(_columnInfos, colInfoRefs, retainImportData);
    }

    // Getters e Setters
    public String getPkTableName() { return _pkTableName; }
    public String getFkTableName() { return _fkTableName; }
    public String getConstraintName() { return _constraintName; }
    public ColumnInfo[] getColumnInfos() { return _columnInfos; }
}

public class DDLGenerator {
    public static String[] generateDDL(ConstraintData constraint) {
        Vector<String> ddl = new Vector<>();
        ddl.add("ALTER TABLE " + constraint.getFkTableName());
        ddl.add("ADD CONSTRAINT " + constraint.getConstraintName());

        if (constraint.getColumnInfos().length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("FOREIGN KEY (").append(constraint.getColumnInfos()[0].getName());
            for (int i = 1; i < constraint.getColumnInfos().length; i++) {
                sb.append(", ").append(constraint.getColumnInfos()[i].getName());
            }
            sb.append(")");
            ddl.add(sb.toString());

            sb.setLength(0);
            sb.append("REFERENCES ").append(constraint.getPkTableName()).append(" (");
            sb.append(constraint.getColumnInfos()[0].getImportedColumnName());
            for (int i = 1; i < constraint.getColumnInfos().length; i++) {
                sb.append(", ").append(constraint.getColumnInfos()[i].getImportedColumnName());
            }
            sb.append(")");
            ddl.add(sb.toString());
        }

        return ddl.toArray(new String[0]);
    }
}

public class ColumnInfoManager {
    public static void addColumn(ColumnInfo[] columnInfos, ColumnInfo colInfo) {
        Vector<ColumnInfo> buf = new Vector<>(Arrays.asList(columnInfos));
        buf.add(colInfo);
        columnInfos = buf.toArray(new ColumnInfo[0]);
    }

    public static void replaceColumns(ColumnInfo[] columnInfos, ColumnInfo[] colInfoRefs, boolean retainImportData) {
        for (ColumnInfo ref : colInfoRefs) {
            for (int i = 0; i < columnInfos.length; i++) {
                if (ref.getName().equals(columnInfos[i].getName())) {
                    if (retainImportData) {
                        ref.setImportData(
                            columnInfos[i].getImportedTableName(),
                            columnInfos[i].getImportedColumnName(),
                            columnInfos[i].getConstraintName(),
                            columnInfos[i].isNonDbConstraint()
                        );
                    }
                    columnInfos[i] = ref;
                    break;
                }
            }
        }
    }
}

public class XmlConverter {
    public static void populateFromXml(ConstraintData constraint, ConstraintDataXmlBean xmlBean) {
        constraint._pkTableName = xmlBean.getPkTableName();
        constraint._fkTableName = xmlBean.getFkTableName();
        constraint._constraintName = xmlBean.getConstraintName();
        constraint._nonDbConstraint = xmlBean.isNonDbConstraint();
        constraint._showThisConstraintName = xmlBean.isShowThisConstraintName();

        constraint._columnInfos = new ColumnInfo[xmlBean.getColumnInfoXmlBeans().length];
        for (int i = 0; i < constraint._columnInfos.length; i++) {
            constraint._columnInfos[i] = new ColumnInfo(xmlBean.getColumnInfoXmlBeans()[i]);
        }
    }

    public static ConstraintDataXmlBean toXmlBean(ConstraintData constraint) {
        ConstraintDataXmlBean xmlBean = new ConstraintDataXmlBean();
        xmlBean.setPkTableName(constraint.getPkTableName());
        xmlBean.setFkTableName(constraint.getFkTableName());
        xmlBean.setConstraintName(constraint.getConstraintName());
        xmlBean.setNonDbConstraint(constraint.isNonDbConstraint());
        xmlBean.setShowThisConstraintName(constraint.isShowThisConstraintName());

        ColumnInfoXmlBean[] colInfoXmlBeans = new ColumnInfoXmlBean[constraint.getColumnInfos().length];
        for (int i = 0; i < colInfoXmlBeans.length; i++) {
            colInfoXmlBeans[i] = constraint.getColumnInfos()[i].getXmlBean();
        }
        xmlBean.setColumnInfoXmlBeans(colInfoXmlBeans);

        return xmlBean;
    }
}