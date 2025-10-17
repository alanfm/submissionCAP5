// Classe dedicada à avaliação de blocos de instruções
class InstructionBlockEvaluator {
    private final ValueFactory valueFactory;
    private final InvocationUnit invocationUnit;

    public InstructionBlockEvaluator(ValueFactory valueFactory, InvocationUnit invocationUnit) {
        this.valueFactory = valueFactory;
        this.invocationUnit = invocationUnit;
    }

    public void evaluateSingleInstructionBlock(Clazz clazz, Method method, CodeAttribute codeAttribute,
                                               TracedVariables variables, TracedStack stack, int startOffset) {
        byte[] code = codeAttribute.code;
        Processor processor = new Processor(variables, stack, valueFactory, invocationUnit);

        int offset = startOffset;
        while (offset < codeAttribute.u4codeLength) {
            Instruction instruction = InstructionFactory.create(code, offset);
            processor.execute(instruction);
            offset += instruction.length(offset);
        }
    }
}

// Classe dedicada à manipulação de exceções
class ExceptionHandlerEvaluator {
    public void evaluateExceptionHandlers(Clazz clazz, Method method, CodeAttribute codeAttribute,
                                          int startOffset, int endOffset, PartialEvaluator evaluator) {
        ExceptionHandlerFilter exceptionEvaluator = new ExceptionHandlerFilter(startOffset, endOffset, evaluator);
        codeAttribute.exceptionsAccept(clazz, method, startOffset, endOffset, exceptionEvaluator);
    }
}

// Classe dedicada à generalização de contextos
class ContextGeneralizer {
    public void generalize(PartialEvaluator evaluator, PartialEvaluator other, int codeStart, int codeEnd) {
        for (int offset = codeStart; offset < codeEnd; offset++) {
            if (other.isTraced(offset)) {
                evaluator.variablesBefore[offset].generalize(other.variablesBefore[offset], false);
                evaluator.stacksBefore[offset].generalize(other.stacksBefore[offset]);
                evaluator.variablesAfter[offset].generalize(other.variablesAfter[offset], false);
                evaluator.stacksAfter[offset].generalize(other.stacksAfter[offset]);
            }
        }
    }
}

// Versão refatorada da classe principal
public class PartialEvaluator extends SimplifiedVisitor implements AttributeVisitor, ExceptionInfoVisitor {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_RESULTS = false;

    private final ValueFactory valueFactory;
    private final InvocationUnit invocationUnit;
    private final boolean evaluateAllCode;
    private final BasicBranchUnit branchUnit;
    private final BranchTargetFinder branchTargetFinder;
    private final java.util.Stack callingInstructionBlockStack;
    private final java.util.Stack instructionBlockStack = new java.util.Stack();

    // Arrays para armazenar informações de avaliação
    private InstructionOffsetValue[] branchOriginValues;
    private InstructionOffsetValue[] branchTargetValues;
    private TracedVariables[] variablesBefore;
    private TracedStack[] stacksBefore;
    private TracedVariables[] variablesAfter;
    private TracedStack[] stacksAfter;
    private boolean[] generalizedContexts;
    private int[] evaluationCounts;

    public PartialEvaluator(ValueFactory valueFactory, InvocationUnit invocationUnit, boolean evaluateAllCode,
                            BasicBranchUnit branchUnit, BranchTargetFinder branchTargetFinder,
                            java.util.Stack callingInstructionBlockStack) {
        this.valueFactory = valueFactory;
        this.invocationUnit = invocationUnit;
        this.evaluateAllCode = evaluateAllCode;
        this.branchUnit = branchUnit;
        this.branchTargetFinder = branchTargetFinder;
        this.callingInstructionBlockStack = callingInstructionBlockStack == null ? this.instructionBlockStack : callingInstructionBlockStack;
    }

    @Override
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute) {
        initializeArrays(codeAttribute);
        evaluateInstructionBlockAndExceptionHandlers(clazz, method, codeAttribute, 0, codeAttribute.u4codeLength);
    }

    private void evaluateInstructionBlockAndExceptionHandlers(Clazz clazz, Method method, CodeAttribute codeAttribute,
                                                              int startOffset, int endOffset) {
        InstructionBlockEvaluator blockEvaluator = new InstructionBlockEvaluator(valueFactory, invocationUnit);
        ExceptionHandlerEvaluator exceptionEvaluator = new ExceptionHandlerEvaluator();

        blockEvaluator.evaluateSingleInstructionBlock(clazz, method, codeAttribute, variablesBefore[startOffset],
                stacksBefore[startOffset], startOffset);
        exceptionEvaluator.evaluateExceptionHandlers(clazz, method, codeAttribute, startOffset, endOffset, this);
    }

    private void generalize(PartialEvaluator other, int codeStart, int codeEnd) {
        ContextGeneralizer generalizer = new ContextGeneralizer();
        generalizer.generalize(this, other, codeStart, codeEnd);
    }

    private void initializeArrays(CodeAttribute codeAttribute) {
        int codeLength = codeAttribute.u4codeLength;
        branchOriginValues = new InstructionOffsetValue[codeLength];
        branchTargetValues = new InstructionOffsetValue[codeLength];
        variablesBefore = new TracedVariables[codeLength];
        stacksBefore = new TracedStack[codeLength];
        variablesAfter = new TracedVariables[codeLength];
        stacksAfter = new TracedStack[codeLength];
        generalizedContexts = new boolean[codeLength];
        evaluationCounts = new int[codeLength];
    }
}