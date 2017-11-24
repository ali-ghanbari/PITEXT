package edu.utdallas.pitext;

import java.util.Arrays;
import java.util.Collection;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class RequiredConstantReplacement implements PITExtMutationOperatorStub, Opcodes {
	private final int variant;
	
	public static final int RESET_1 = 0b0000_0000_0000_0000_0000_0000_0000_0001;
	public static final int RESET_0 = 0b0000_0000_0000_0000_0000_0000_0000_0010;
	public static final int DEC_1 = 0b0000_0000_0000_0000_0000_0000_0000_0100;
	public static final int INC_1 = 0b0000_0000_0000_0000_0000_0000_0000_1000;

	public RequiredConstantReplacement(int variant) {
		this.variant = variant;
	}
	
	private boolean numeric(Object o) {
		return o instanceof Integer 
				|| o instanceof Long 
				|| o instanceof Float 
				|| o instanceof Double;
	}

	@Override
	public boolean canMutate(int opcode, Object... other) {
		return (ICONST_M1 <= opcode && opcode <= ICONST_5)
				|| LCONST_0 == opcode || LCONST_1 == opcode
				|| FCONST_0 == opcode || FCONST_1 == opcode || FCONST_2 == opcode
				|| DCONST_0 == opcode || DCONST_1 == opcode
				|| BIPUSH == opcode || SIPUSH == opcode
				|| (LDC == opcode && numeric(other[0]));
	}

	@Override
	public String identifier() {
		return "REQUIRED-CONSTANT-REPLACEMENT-" + variant;
	}

	public static Collection<String> allVariants() {
		return Arrays.asList("REQUIRED-CONSTANT-REPLACEMENT-1",
				"REQUIRED-CONSTANT-REPLACEMENT-2",
				"REQUIRED-CONSTANT-REPLACEMENT-4",
				"REQUIRED-CONSTANT-REPLACEMENT-8");
	}

	@Override
	public String description() {
		final String desc;
		switch(variant) {
		case RESET_0:
			desc = "resets mumeric constant to 0";
			break;
		case RESET_1:
			desc = "resets mumeric constant to 1";
			break;
		case DEC_1:
			desc = "decrement mumeric constant by 1";
			break;
		default:
			desc = "increment mumeric constant to 1";
		}
		return identifier() + ": " + desc;
	}
	
	private void applyMutationReset(String type, MethodVisitor mv) {
		switch(variant) {
		case RESET_0:
			mv.visitInsn(xCONST_0(type));
			break;
		case RESET_1:
			mv.visitInsn(xCONST_1(type));
		}
	}
	
	private void applyMutation_INC_DEC(String type, MethodVisitor mv) {
		switch(variant) {
		case INC_1:
			mv.visitMethodInsn(INVOKESTATIC, "edu/utdallas/pitextutils/PITExtUtils", "__increment__",
					"(" + type + ")" + type, false);
			break;
		case DEC_1:
			mv.visitMethodInsn(INVOKESTATIC, "edu/utdallas/pitextutils/PITExtUtils", "__decrement__",
					"(" + type + ")" + type, false);
		}
	}
		
	private int xCONST_0(String type) {
		if(type.equals("I")) {
			return ICONST_0;
		}
		if(type.equals("J")) {
			return LCONST_0;
		}
		if(type.equals("F")) {
			return FCONST_0;
		}
		if(type.equals("D")) {
			return DCONST_0;
		}
		return -1;
	}
	
	private int xCONST_1(String type) {
		return xCONST_0(type) + 1; //xCONST_1 is the next instruction
	}

	@Override
	public MethodVisitor createMutator(MutationIdentifier mId, MethodVisitor mv) {
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
					if(ICONST_M1 <= opcode && opcode <= ICONST_5) {
						if(variant == RESET_0 || variant == RESET_1) {
							applyMutationReset("I", this.mv);
						} else {
							super.visitInsn(opcode);
							applyMutation_INC_DEC("I", this.mv);
						}
					} else if (LCONST_0 == opcode || LCONST_1 == opcode) {
						if(variant == RESET_0 || variant == RESET_1) {
							applyMutationReset("J", this.mv);
						} else {
							super.visitInsn(opcode);
							applyMutation_INC_DEC("J", this.mv);
						}
					} else if (FCONST_0 == opcode || FCONST_1 == opcode || FCONST_2 == opcode) {
						if(variant == RESET_0 || variant == RESET_1) {
							applyMutationReset("F", this.mv);
						} else {
							super.visitInsn(opcode);
							applyMutation_INC_DEC("F", this.mv);
						}
					} else if (DCONST_0 == opcode || DCONST_1 == opcode) {
						if(variant == RESET_0 || variant == RESET_1) {
							applyMutationReset("D", this.mv);
						} else {
							super.visitInsn(opcode);
							applyMutation_INC_DEC("D", this.mv);
						}
					} else {
						this.mv.visitInsn(opcode);
					}
				} else {
					this.mv.visitInsn(opcode);
				}				
			}
			@Override
			public void visitIntInsn(int opcode, int operand) {
				index++;
				if(shouldMutate()) {
					if(opcode == BIPUSH || opcode == SIPUSH) {
						if(variant == RESET_0 || variant == RESET_1) {
							applyMutationReset("I", this.mv);
						} else {
							super.visitIntInsn(opcode, operand);
							applyMutation_INC_DEC("I", this.mv);
						}
					} else {
						super.visitIntInsn(opcode, operand);
					}
				} else {
					super.visitIntInsn(opcode, operand);
				}
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
				this.mv.visitJumpInsn(opcode, label);
			}
			@Override
			public void visitLdcInsn(Object cst) {
				index++;
				if(shouldMutate()) {
					if (cst instanceof Integer) {
						if(variant == RESET_0 || variant == RESET_1) {
							applyMutationReset("I", this.mv);
						} else {
							super.visitLdcInsn(cst);
							applyMutation_INC_DEC("I", this.mv);
						}
					} else if (cst instanceof Long) {
						if(variant == RESET_0 || variant == RESET_1) {
							applyMutationReset("J", this.mv);
						} else {
							super.visitLdcInsn(cst);
							applyMutation_INC_DEC("J", this.mv);
						}
					} else if (cst instanceof Float) {
						if(variant == RESET_0 || variant == RESET_1) {
							applyMutationReset("F", this.mv);
						} else {
							super.visitLdcInsn(cst);
							applyMutation_INC_DEC("F", this.mv);
						}
					} else if (cst instanceof Double) {
						if(variant == RESET_0 || variant == RESET_1) {
							applyMutationReset("D", this.mv);
						} else {
							super.visitLdcInsn(cst);
							applyMutation_INC_DEC("D", this.mv);
						}
					} else { //do not touch other constants
						super.visitLdcInsn(cst);
					}
				} else {
					super.visitLdcInsn(cst);
				}
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
