package edu.utdallas.pitext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class AbsoluteValueInsertion implements PITExtMutationOperatorStub, Opcodes {
	public static final int ABS = 0;
	public static final int ABS_NEG = 1;
	public static final int FOZ = 2;
	private final int variant;
	
	public AbsoluteValueInsertion(int variant) {
		this.variant = variant;
	}

	@Override
	public boolean canMutate(int opcode, int previousOpcode, Object... other) {
		if(0x1a /*iload_0*/ <= opcode && opcode <= 0x29 /*dload_3*/) {
			return true;
		}
		switch(opcode) {
		case ILOAD:
		case LLOAD:
		case FLOAD:
		case DLOAD:
			return true;
		}
		return false;
	}

	@Override
	public String identifier() {
		return "ABSOLUTE-VALUE-INSERTION-" + variant;
	}

	public static Collection<String> allVariants() {
		List<String> result = new ArrayList<>();
		for(int i = 0; i < 3; i++) {
			result.add("ABSOLUTE-VALUE-INSERTION-" + i);
		}
		return result;
	}

	@Override
	public String description() {
		final String desc;
		switch(variant) {
		case ABS:
			desc = "replaces v with |v|";
			break;
		case ABS_NEG:
			desc = "replaces v with -|v|";
			break;
		default:
			desc = "replaces v with failOnZero(v)";
		}
		return identifier() + ": " + desc;
	}
	
	private String getLOADType(int opcode) {
		if(opcode == ILOAD || Util.isBetween(opcode, 0x1a /*ILOAD_0*/, 0x1d /*ILOAD_3*/)) {
			return "I";
		}
		if(opcode == LLOAD || Util.isBetween(opcode, 0x1e /*LLOAD_0*/, 0x21 /*LLOAD_3*/)) {
			return "J";
		}
		if(opcode == FLOAD || Util.isBetween(opcode, 0x22 /*FLOAD_0*/, 0x25 /*FLOAD_3*/)) {
			return "F";
		}
		if(opcode == DLOAD || Util.isBetween(opcode, 0x26 /*DLOAD_0*/, 0x29 /*DLOAD_3*/)) {
			return "D";
		}
		return null;
	}

	@Override
	public MethodVisitor createMutator(MutationIdentifier mId, MethodVisitor mv) {
		final String enclosingClassName = mId.getClassName().asInternalName();
		return new MethodVisitor(ASM6, mv) {
			private int index = 1;
			
			private boolean shouldMutate() {
				return index == mId.getFirstIndex();
			}
			@Override
			public void visitVarInsn(int opcode, int var) {
				this.mv.visitVarInsn(opcode, var);
				final String type = getLOADType(opcode);
				if(type != null && shouldMutate()) {	
					switch(variant) {
					case ABS:
					case ABS_NEG:
						this.mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(" + type + ")" + type, false);
						if(variant == ABS_NEG) {
							if(type.equals("I")) {
								this.mv.visitInsn(INEG);
							} else if (type.equals("J")) {
								this.mv.visitInsn(LNEG);
							} else if (type.equals("F")) {
								this.mv.visitInsn(FNEG);
							} else if (type.equals("D")) {
								this.mv.visitInsn(DNEG);
							}
						}
						break;
					case FOZ:
						this.mv.visitMethodInsn(INVOKESTATIC, enclosingClassName, "__failOnZero__", "(" + type + ")" + type, false);
						break;
					}
				}
				index++;
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
