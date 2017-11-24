package edu.utdallas.pitext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class UnaryOperatorInsertion implements PITExtMutationOperatorStub, Opcodes {
	private final int variant;

	public static final int INC_INS = 0b0000_0000_0000_0000_0000_0000_0000_0001;
	public static final int INC_REM = 0b0000_0000_0000_0000_0000_0000_0000_0010;
	public static final int DEC_INS = 0b0000_0000_0000_0000_0000_0000_0000_0100;
	public static final int DEC_REM = 0b0000_0000_0000_0000_0000_0000_0000_1000;
	public static final int ANG_INS = 0b0000_0000_0000_0000_0000_0000_0001_0000; //Arithmetic Negation Insertion
	public static final int ANG_REM = 0b0000_0000_0000_0000_0000_0000_0010_0000; //Arithmetic Negation Removal
//	private static final int BNT_INS_MASK = 0b0000_0000_0000_0000_0000_0000_0100_0000; //Boolean Not Insertion (NOT SUPPORTED)
//	private static final int BNT_REM_MASK = 0b0000_0000_0000_0000_0000_0000_1000_0000; //Boolean Not Removal (NOT SUPPORTED)
	public static final int BWN_INS = 0b0000_0000_0000_0000_0000_0001_0000_0000; //Bitwise Not Insertion
	public static final int BWN_REM = 0b0000_0000_0000_0000_0000_0010_0000_0000; //Bitwise Not Removal

	public UnaryOperatorInsertion(int variant) {
		this.variant = variant;
	}
	
	private boolean insertIncrement() {
		return (variant == INC_INS);
	}
	
	private boolean insertDecrement() {
		return (variant == DEC_INS);
	}
	
	private boolean insertArithNeg() {
		return (variant == ANG_INS);
	}
	
	private boolean insertBitwiseNeg() {
		return (variant == BWN_INS);
	}
	
	private boolean removeIncrement() {
		return (variant == INC_REM);
	}
	
	private boolean removeDecrement() {
		return (variant == DEC_REM);
	}
	
	private boolean removeArithNeg() {
		return (variant == ANG_REM);
	}
	
	private boolean removeBitwiseNeg() {
		return (variant == BWN_REM);
	}

	@Override
	public boolean canMutate(int opcode, Object... other) {
		if(opcode == IINC) {
			final int increment = (int) other[0];
			if((removeIncrement() && increment == 1) || (removeDecrement() && increment == -1)) {
				return true;
			}
			return false;
		}
		if(opcode == IXOR && removeBitwiseNeg()) {
			return true;
		}
		if(opcode == INEG && removeArithNeg()) {
			return true;
		}
		return opcode == ILOAD && (insertIncrement() || insertBitwiseNeg() || insertDecrement() || insertArithNeg());
	}

	@Override
	public String identifier() {
		return "UNARY-OPERATOR-INSERTION-" + variant;
	}

	public static Collection<String> allVariants() {
		List<String> result = new ArrayList<>();
		for(int i = 1; i <= 1024; i <<= 1) {
			if(i != 64 && i != 128) {
				result.add("UNARY-OPERATOR-INSERTION-" + i);
			}
		}
		return result;
	}

	@Override
	public String description() {
		final String desc;
		switch(variant) {
		case INC_INS:
			desc = "inserts increment operator";
			break;
		case DEC_INS:
			desc = "inserts decrement operator";
			break;
		case ANG_INS:
			desc = "inserts arithmetic negation operator";
			break;
		case BWN_INS:
			desc = "inserts bitwise negation operator";
			break;
		case INC_REM:
			desc = "removes increment operator";
			break;
		case DEC_REM:
			desc = "removes decrement operator";
			break;
		case ANG_REM:
			desc = "removes arithmetic negation operator";
			break;
		default:
			desc = "removes bitwise negation operator";
		}
		return identifier() + ": " + desc;
	}

	@Override
	public MethodVisitor createMutator(MutationIdentifier mId, MethodVisitor mv) {
		final boolean IsConstructor = mId.getLocation().getMethodName().name().equals("<init>");
		return new MethodVisitor(ASM6, mv) {
			//private int previousOpcode = -1;
			private int index = 0;
			
			private boolean shouldMutate() {
				return index == mId.getFirstIndex() &&  !IsConstructor;
			}
			
			@Override
			public void visitVarInsn(int opcode, int var) {
				index++;
				if(shouldMutate()) {
					if(opcode == ILOAD) {
						if(insertIncrement()) {
							super.visitIincInsn(var, 1);
							super.visitVarInsn(opcode, var);
						} else if(insertDecrement()) {
							super.visitIincInsn(var, -1);
							super.visitVarInsn(opcode, var);
						} else if(insertArithNeg()) {
							super.visitVarInsn(opcode, var);
							super.visitInsn(INEG);
						} else if(insertBitwiseNeg()) {
							super.visitVarInsn(opcode, var);
							this.mv.visitMethodInsn(INVOKESTATIC, "edu/utdallas/pitextutils/PITExtUtils", "__bitwise_negation__", "(I)I", false);
						} else {
							super.visitVarInsn(opcode, var);
						}
					} else {
						super.visitVarInsn(opcode, var);
					}
				} else {
					super.visitVarInsn(opcode, var);
				}
//				previousOpcode = opcode;
			}
			
			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String desc) {
				index++;
//				previousOpcode = opcode;
				super.visitFieldInsn(opcode, owner, name, desc);
			}
			@Override
			public void visitIincInsn(int var, int increment) {
				index++;
//				previousOpcode = IINC;
				if(shouldMutate()) {
					if((removeIncrement() && increment == 1) || (removeDecrement() && increment == -1)) {
						return;
					}
				}
				super.visitIincInsn(var, increment);
			}
			@Override
			public void visitInsn(int opcode) {
				index++;
				if(shouldMutate()) {
					if(opcode == IXOR && removeBitwiseNeg()) {
						super.visitMethodInsn(INVOKESTATIC, "edu/utdallas/pitextutils/PITExtUtils", "__neutralize_bitwise_neg__", "(II)I", false);
						return;
					}
				}
//				previousOpcode = opcode;
				if(shouldMutate() && opcode == INEG && removeArithNeg()) {
					return;
				}
				super.visitInsn(opcode);
			}
			@Override
			public void visitIntInsn(int opcode, int operand) {
				index++;
//				previousOpcode = opcode;
				super.visitIntInsn(opcode, operand);
			}
			@Override
			public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
				index++;
//				previousOpcode = INVOKEDYNAMIC;
				super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
			}
			@Override
			public void visitJumpInsn(int opcode, Label label) {
				index++;
//				previousOpcode = opcode;
				this.mv.visitJumpInsn(opcode, label);
			}
			@Override
			public void visitLdcInsn(Object cst) {
				index++;
//				previousOpcode = LDC;
				super.visitLdcInsn(cst);
			}
			@Override
			public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
				index++;
//				previousOpcode = LOOKUPSWITCH;
				super.visitLookupSwitchInsn(dflt, keys, labels);
			}
			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				index++;
//				previousOpcode = opcode;
				super.visitMethodInsn(opcode, owner, name, desc, itf);
			}
			@Override
			public void visitMultiANewArrayInsn(String desc, int dims) {
				index++;
//				previousOpcode = MULTIANEWARRAY;
				super.visitMultiANewArrayInsn(desc, dims);
			}
			@Override
			public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
				index++;
//				previousOpcode = TABLESWITCH;
				super.visitTableSwitchInsn(min, max, dflt, labels);
			}
			@Override
			public void visitTypeInsn(int opcode, String type) {
				index++;
//				previousOpcode = opcode;
				super.visitTypeInsn(opcode, type);
			}
		};
	}

}
