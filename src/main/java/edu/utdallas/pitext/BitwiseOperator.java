package edu.utdallas.pitext;

import java.util.Collection;
import java.util.Collections;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class BitwiseOperator implements PITExtMutationOperatorStub, Opcodes {

	@Override
	public boolean canMutate(int opcode, int previousOpcode, Object... other) {
		switch(opcode) {
		case IAND:
		case LAND:
		case IOR:
		case LOR:
			return true;
		}
		return false;
	}

	@Override
	public String identifier() {
		return "BITWISE-OPERATOR";
	}

	public static Collection<String> allVariants() {
		return Collections.singleton("BITWISE-OPERATOR");
	}

	@Override
	public String description() {
		return "replaces AND's with OR's and vice versa";
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
					switch(opcode) {
					case IAND:
						this.mv.visitInsn(IOR);
						return;
					case LAND:
						this.mv.visitInsn(LOR);
						return;
					case IOR:
						this.mv.visitInsn(IAND);
						return;
					case LOR:
						this.mv.visitInsn(LAND);
						return;
					}
				}
				this.mv.visitInsn(opcode);
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
