// Classe dedicada à construção do DFA
class DFABuilder {
    private final CMNode syntaxTree;
    private final int leafCount;

    public DFABuilder(CMNode syntaxTree, int leafCount) {
        this.syntaxTree = syntaxTree;
        this.leafCount = leafCount;
    }

    public void buildDFA(XSDFACM dfa) {
        rewriteSyntaxTree(dfa);
        initializeStates(dfa);
        calculateFollowLists(dfa);
        optimizeTransitions(dfa);
    }

    private void rewriteSyntaxTree(XSDFACM dfa) {
        // Lógica para reescrever a árvore sintática
    }

    private void initializeStates(XSDFACM dfa) {
        // Inicialização dos estados do DFA
    }

    private void calculateFollowLists(XSDFACM dfa) {
        dfa.calcFollowList(dfa.getHeadNode());
    }

    private void optimizeTransitions(XSDFACM dfa) {
        // Otimização das transições do DFA
    }
}

// Classe dedicada ao gerenciamento de estados
class StateManager {
    public static boolean isFinalState(int state, boolean[] finalStateFlags) {
        return state >= 0 && finalStateFlags[state];
    }

    public static Vector whatCanGoHere(int[] state, XSDFACM dfa) {
        int curState = state[0];
        if (curState < 0) {
            curState = state[1];
        }
        Occurence o = (dfa.getCountingStates() != null) ? dfa.getCountingStates()[curState] : null;
        int count = state[2];
        Vector ret = new Vector();

        for (int i = 0; i < dfa.getElemMapSize(); i++) {
            if (dfa.getElemMapType(i) == XSParticleDecl.PARTICLE_WILDCARD) {
                XSWildcardDecl wildcard = (XSWildcardDecl) dfa.getElemMap(i);
                if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST ||
                        wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) {
                    ret.add(wildcard);
                }
            }
        }
        return ret;
    }
}

// Versão refatorada da classe principal
public class XSDFACM implements XSCMValidator {
    private CMNode headNode;
    private int leafCount;
    private boolean isCompactedForUPA;
    private XSCMLeaf[] leafList;
    private int[] elemMap;
    private int[] elemMapType;
    private int[] elemMapId;
    private int elemMapSize;
    private CMStateSet[] followList;
    private boolean[] finalStateFlags;
    private int[][] transTable;
    private Occurence[] countingStates;

    public XSDFACM(CMNode syntaxTree, int leafCount) {
        this.headNode = syntaxTree;
        this.leafCount = leafCount;
        this.isCompactedForUPA = syntaxTree.isCompactedForUPA();
        DFABuilder builder = new DFABuilder(syntaxTree, leafCount);
        builder.buildDFA(this);
    }

    public void calcFollowList(CMNode nodeCur) {
        if (nodeCur.type() == XSModelGroupImpl.MODELGROUP_CHOICE) {
            calcFollowList(((XSCMBinOp) nodeCur).getLeft());
            calcFollowList(((XSCMBinOp) nodeCur).getRight());
        } else if (nodeCur.type() == XSModelGroupImpl.MODELGROUP_SEQUENCE) {
            calcFollowList(((XSCMBinOp) nodeCur).getLeft());
            calcFollowList(((XSCMBinOp) nodeCur).getRight());
        }
    }

    public void postTreeBuildInit(CMNode nodeCur) {
        nodeCur.setMaxStates(leafCount);
        if (nodeCur instanceof XSCMLeaf) {
            XSCMLeaf leaf = (XSCMLeaf) nodeCur;
            int pos = leaf.getPosition();
            leafList[pos] = leaf;
        }
    }

    @Override
    public boolean isFinalState(int state) {
        return StateManager.isFinalState(state, finalStateFlags);
    }

    @Override
    public Vector whatCanGoHere(int[] state) {
        return StateManager.whatCanGoHere(state, this);
    }

    public CMNode getHeadNode() {
        return headNode;
    }

    public int getLeafCount() {
        return leafCount;
    }

    public XSCMLeaf[] getLeafList() {
        return leafList;
    }

    public int[] getElemMap() {
        return elemMap;
    }

    public int[] getElemMapType() {
        return elemMapType;
    }

    public int[] getElemMapId() {
        return elemMapId;
    }

    public int getElemMapSize() {
        return elemMapSize;
    }

    public Occurence[] getCountingStates() {
        return countingStates;
    }

    public void dumpTree(CMNode nodeCur, int level) {
        for (int index = 0; index < level; index++) {
            System.out.print(" ");
        }
        int type = nodeCur.type();
        switch (type) {
            case XSModelGroupImpl.MODELGROUP_CHOICE:
            case XSModelGroupImpl.MODELGROUP_SEQUENCE:
                System.out.println("Node Type: " + type);
                break;
            default:
                System.out.println("Unknown Node Type");
        }
    }
}