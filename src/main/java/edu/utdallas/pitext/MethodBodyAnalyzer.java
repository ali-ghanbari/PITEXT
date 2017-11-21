package edu.utdallas.pitext;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.reloc.asm.commons.Method;

final class MethodBodyAnalyzer extends MethodVisitor implements Opcodes {
	private final MutationPointCollector collector;
	private final Set<Label> handlers;
	private final Method method;
	private int previousOpcode;
	private int lineNumber;
	private int index;

	public MethodBodyAnalyzer(Method method, MutationPointCollector collector) {
		super(ASM6);
		this.method = method;
		this.collector = collector;
		this.lineNumber = 0;
		this.index = 1;
		this.handlers = new HashSet<Label>();
		this.previousOpcode = -1;
	}
	
	@Override
	public void visitInsn(int opcode) {
		collector.registerMutationPoint(method, lineNumber, index++, opcode, previousOpcode);
		if (endsBlock(opcode)) {
			collector.blockCounter.registerFinallyBlockEnd();
			collector.blockCounter.registerNewBlock();
		}
		super.visitInsn(opcode);
		previousOpcode = opcode;
	}

	@Override
	public void visitJumpInsn(int opcode, Label target) {
		collector.registerMutationPoint(method, lineNumber, index++, opcode, previousOpcode);
		collector.blockCounter.registerNewBlock();
		super.visitJumpInsn(opcode, target);
		previousOpcode = opcode;
	}

	/*Henry, thanks!*/
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		if (type == null) {
			this.handlers.add(handler);
		}
		super.visitTryCatchBlock(start, end, handler, type);
	}
	
	/*Henry, thanks!*/
	@Override
	public void visitLabel(Label label) {
		if (handlers.contains(label)) {
			collector.blockCounter.registerFinallyBlockStart();
		}
		super.visitLabel(label);
	}
	
	/*Henry, thanks!*/
	private boolean endsBlock(int opcode) {
		switch (opcode) {
		case RETURN:
		case ARETURN:
		case DRETURN:
		case FRETURN:
		case IRETURN:
		case LRETURN:
		case ATHROW:
			return true;
		default:
			return false;
		}
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		collector.registerMutationPoint(method, lineNumber, index++, opcode, previousOpcode);
		super.visitFieldInsn(opcode, owner, name, desc);
		previousOpcode = opcode;
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		collector.registerMutationPoint(method, lineNumber, index++, IINC, previousOpcode, increment);
		super.visitIincInsn(var, increment);
		previousOpcode = IINC;
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		collector.registerMutationPoint(method, lineNumber, index++, opcode, previousOpcode);
		super.visitIntInsn(opcode, operand);
		previousOpcode = opcode;
	}

	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
		collector.registerMutationPoint(method, lineNumber, index++, INVOKEDYNAMIC, previousOpcode);
		super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		previousOpcode = INVOKEDYNAMIC;
	}

	@Override
	public void visitLdcInsn(Object cst) {
		collector.registerMutationPoint(method, lineNumber, index++, LDC, previousOpcode, cst);
		super.visitLdcInsn(cst);
		previousOpcode = LDC;
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		collector.registerMutationPoint(method, lineNumber, index++, LOOKUPSWITCH, previousOpcode);
		super.visitLookupSwitchInsn(dflt, keys, labels);
		previousOpcode = LOOKUPSWITCH;
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		collector.registerMutationPoint(method, lineNumber, index++, opcode, previousOpcode);
		super.visitMethodInsn(opcode, owner, name, desc, itf);
		previousOpcode = opcode;
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		collector.registerMutationPoint(method, lineNumber, index++, MULTIANEWARRAY, previousOpcode);
		super.visitMultiANewArrayInsn(desc, dims);
		previousOpcode = MULTIANEWARRAY;
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
		collector.registerMutationPoint(method, lineNumber, index++, TABLESWITCH, previousOpcode);
		super.visitTableSwitchInsn(min, max, dflt, labels);
		previousOpcode = TABLESWITCH;
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		collector.registerMutationPoint(method, lineNumber, index++, opcode, previousOpcode);
		super.visitTypeInsn(opcode, type);
		previousOpcode = opcode;
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		collector.registerMutationPoint(method, lineNumber, index++, opcode, previousOpcode);
		super.visitVarInsn(opcode, var);
		previousOpcode = opcode;
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		lineNumber = line;
		super.visitLineNumber(line, start);
	}
}
