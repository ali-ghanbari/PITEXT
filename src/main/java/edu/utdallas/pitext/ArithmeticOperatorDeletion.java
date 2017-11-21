package edu.utdallas.pitext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class ArithmeticOperatorDeletion implements PITExtMutationOperatorStub, Opcodes {
	public static final int FIRST = 1;
	public static final int SECOND = 2;
	private final int variant;

	public ArithmeticOperatorDeletion(int variant) {
		this.variant = variant;
	}

	@Override
	public boolean canMutate(int opcode, int previousOpcode, Object... other) {
		switch(opcode) {
		case IADD:
		case ISUB:
		case IMUL:
		case IDIV:
		case IREM:
		case LADD:
		case LSUB:
		case LMUL:
		case LDIV:
		case LREM:
		case FADD:
		case FSUB:
		case FMUL:
		case FDIV:
		case FREM:
		case DADD:
		case DSUB:
		case DMUL:
		case DDIV:
		case DREM:
			return true;
		}
		return false;
	}

	@Override
	public String identifier() {
		return "ARITHMETIC-OPERATOR-DELETION-" + variant;
	}

	public static Collection<String> allVariants() {
		List<String> result = new ArrayList<>();
		result.add("ARITHMETIC-OPERATOR-DELETION-1");
		result.add("ARITHMETIC-OPERATOR-DELETION-2");
		return result;
	}

	@Override
	public String description() {
		final String desc;
		switch(variant) {
		case FIRST:
			desc = "keeps first operand";
			break;
		default:
			desc = "keeps second operand";
		}
		return identifier() + ": " + desc;
	}
	
	@Override
	public MethodVisitor createMutator(MutationIdentifier mId, MethodVisitor mv) {
		final String enclosingClassName = mId.getClassName().asInternalName();
		return new MethodVisitor(ASM6, mv) {
			private int index = 0;
			
			private boolean shouldMutate() {
				return index == mId.getFirstIndex();
			}
			
			@Override
			public void visitVarInsn(int opcode, int var) {
				index++;
				this.mv.visitVarInsn(opcode, var);
			}
			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String desc) {
				index++;
				super.visitFieldInsn(opcode, owner, name, desc);
			}
			@Override
			public void visitIincInsn(int var, int increment) {
				index++;
				super.visitIincInsn(var, increment);
			}
			@Override
			public void visitInsn(int opcode) {
				index++;
				if(shouldMutate()) {
					switch(opcode) {
					case IADD:
					case ISUB:
					case IMUL:
					case IDIV:
						/*float's and int's are both 32-bit, so we can treat them equally*/
					case FADD:
					case FSUB:
					case FMUL:
					case FDIV:
						if(variant == FIRST) { /*keep first*/
							this.mv.visitInsn(POP);
						} else if (variant == SECOND) { /*keep second*/
							this.mv.visitInsn(SWAP);
							this.mv.visitInsn(POP);
						}
						return;
					case LADD:
					case LSUB:
					case LMUL:
					case LDIV:
						this.mv.visitMethodInsn(INVOKESTATIC, enclosingClassName, "__pi" + variant + "__", "(JJ)J", false);
						return;						
					case DADD:
					case DSUB:
					case DMUL:
					case DDIV:
						this.mv.visitMethodInsn(INVOKESTATIC, enclosingClassName, "__pi" + variant + "__", "(DD)D", false);
						return;
					}
				}
				super.visitInsn(opcode);
			}
			@Override
			public void visitIntInsn(int opcode, int operand) {
				index++;
				super.visitIntInsn(opcode, operand);
			}
			@Override
			public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
				index++;
				super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
			}
			@Override
			public void visitJumpInsn(int opcode, Label label) {
				index++;
				super.visitJumpInsn(opcode, label);
			}
			@Override
			public void visitLdcInsn(Object cst) {
				index++;
				super.visitLdcInsn(cst);
			}
			@Override
			public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
				index++;
				super.visitLookupSwitchInsn(dflt, keys, labels);
			}
			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				index++;
				super.visitMethodInsn(opcode, owner, name, desc, itf);
			}
			@Override
			public void visitMultiANewArrayInsn(String desc, int dims) {
				index++;
				super.visitMultiANewArrayInsn(desc, dims);
			}
			@Override
			public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
				index++;
				super.visitTableSwitchInsn(min, max, dflt, labels);
			}
			@Override
			public void visitTypeInsn(int opcode, String type) {
				index++;
				super.visitTypeInsn(opcode, type);
			}
		};
	}

}
