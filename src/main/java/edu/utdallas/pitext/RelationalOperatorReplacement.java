package edu.utdallas.pitext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class RelationalOperatorReplacement implements PITExtMutationOperatorStub, Opcodes {
	private final int variant;
	
	private static final int EQ = 0b0000_0000_0000_0000_0000_0000_0000_0000;
	private static final int LT = 0b0000_0000_0000_0000_0000_0000_0000_0001;
	private static final int LE = 0b0000_0000_0000_0000_0000_0000_0000_0010;
	private static final int GT = 0b0000_0000_0000_0000_0000_0000_0000_0011;
	private static final int GE = 0b0000_0000_0000_0000_0000_0000_0000_0100;
	private static final int NE = 0b0000_0000_0000_0000_0000_0000_0000_0101;
	private static final int REF_EQ = 0b0000_0000_0000_0000_0000_0000_0000_0000;
	private static final int REF_NE = 0b0000_0000_0000_0000_0000_0000_0000_0001;
	
	private static final int PRM_EQ_POS = 0;
	private static final int PRM_LT_POS = 4;
	private static final int PRM_LE_POS = 8;
	private static final int PRM_GT_POS = 12;
	private static final int PRM_GE_POS = 16;
	private static final int PRM_NE_POS = 20;
	private static final int REF_EQ_POS = 24;
	private static final int REF_NE_POS = 28;
	
	private static final int PRM_EQ_POS_MASK = 0b0000_0000_0000_0000_0000_0000_0000_0111;
	private static final int PRM_LT_POS_MASK = 0b0000_0000_0000_0000_0000_0000_0111_0000;
	private static final int PRM_LE_POS_MASK = 0b0000_0000_0000_0000_0000_0111_0000_0000;
	private static final int PRM_GT_POS_MASK = 0b0000_0000_0000_0000_0111_0000_0000_0000;
	private static final int PRM_GE_POS_MASK = 0b0000_0000_0000_0111_0000_0000_0000_0000;
	private static final int PRM_NE_POS_MASK = 0b0000_0000_0111_0000_0000_0000_0000_0000;
	private static final int REF_EQ_POS_MASK = 0b0000_0111_0000_0000_0000_0000_0000_0000;
	private static final int REF_NE_POS_MASK = 0b0111_0000_0000_0000_0000_0000_0000_0000;
	
	private final Map<Integer, Integer> ROM = new HashMap<>();
	
	private static final Map<Integer, Integer> int_ROMap;
	private static final Map<Integer, Integer> fp_ROMap;
	private static final Map<Integer, Integer> rf_ROMap;
	
	static {
		int_ROMap = new HashMap<>();
		int_ROMap.put(EQ, IF_ICMPEQ);
		int_ROMap.put(LT, IF_ICMPLT);
		int_ROMap.put(LE, IF_ICMPLE);
		int_ROMap.put(GT, IF_ICMPGT);
		int_ROMap.put(GE, IF_ICMPGE);
		int_ROMap.put(NE, IF_ICMPNE);
		fp_ROMap = new HashMap<>();
		fp_ROMap.put(EQ, IFEQ);
		fp_ROMap.put(LT, IFLT);
		fp_ROMap.put(LE, IFLE);
		fp_ROMap.put(GT, IFGT);
		fp_ROMap.put(GE, IFGE);
		fp_ROMap.put(NE, IFNE);
		rf_ROMap = new HashMap<>();
		rf_ROMap.put(REF_EQ, IF_ACMPEQ);
		rf_ROMap.put(REF_NE, IF_ACMPNE);
	}

	public RelationalOperatorReplacement(int variant) {
		this.variant = variant;
		populateROM();
	}
	
	private void populateROM() {
		ROM.put(IF_ICMPEQ, int_ROMap.get((variant >>> PRM_EQ_POS) & PRM_EQ_POS_MASK));
		ROM.put(IF_ICMPLT, int_ROMap.get((variant >>> PRM_LT_POS) & PRM_LT_POS_MASK));
		ROM.put(IF_ICMPLE, int_ROMap.get((variant >>> PRM_LE_POS) & PRM_LE_POS_MASK));
		ROM.put(IF_ICMPGT, int_ROMap.get((variant >>> PRM_GT_POS) & PRM_GT_POS_MASK));
		ROM.put(IF_ICMPGE, int_ROMap.get((variant >>> PRM_GE_POS) & PRM_GE_POS_MASK));
		ROM.put(IF_ICMPNE, int_ROMap.get((variant >>> PRM_NE_POS) & PRM_NE_POS_MASK));
		
		ROM.put(IFEQ, fp_ROMap.get((variant >>> PRM_EQ_POS) & PRM_EQ_POS_MASK));
		ROM.put(IFLT, fp_ROMap.get((variant >>> PRM_LT_POS) & PRM_LT_POS_MASK));
		ROM.put(IFLE, fp_ROMap.get((variant >>> PRM_LE_POS) & PRM_LE_POS_MASK));
		ROM.put(IFGT, fp_ROMap.get((variant >>> PRM_GT_POS) & PRM_GT_POS_MASK));
		ROM.put(IFGE, fp_ROMap.get((variant >>> PRM_GE_POS) & PRM_GE_POS_MASK));
		ROM.put(IFNE, fp_ROMap.get((variant >>> PRM_NE_POS) & PRM_NE_POS_MASK));
		
		ROM.put(IF_ACMPEQ, rf_ROMap.get((variant >>> REF_EQ_POS) & REF_EQ_POS_MASK));
		ROM.put(IF_ACMPNE, rf_ROMap.get((variant >>> REF_NE_POS) & REF_NE_POS_MASK));
		
	}

	@Override
	public boolean canMutate(int opcode, int previousOpcode, Object... other) {
		switch(opcode) {
		case IFEQ:
		case IFNE:
		case IFLT:
		case IFGE:
		case IFGT:
		case IFLE:
		case IF_ICMPEQ:
		case IF_ICMPNE:
		case IF_ICMPLT:
		case IF_ICMPGE:
		case IF_ICMPGT:
		case IF_ICMPLE:
		case IF_ACMPEQ:
		case IF_ACMPNE:
			return true;
		}
		return false;
	}

	@Override
	public String identifier() {
		return "RELATIONAL-OPERATOR-REPLACEMENT-" + variant;
	}

	public static Collection<String> allVariants() {
		List<String> result = new ArrayList<>();
		
		for(int eq_suplant = 0; eq_suplant < 6; eq_suplant++) {
			if(eq_suplant != EQ) {
				int v = eq_suplant 
						+ (LT << PRM_LT_POS)
						+ (LE << PRM_LE_POS)
						+ (GT << PRM_GT_POS)
						+ (GE << PRM_GE_POS)
						+ (NE << PRM_NE_POS)
						+ (REF_EQ << REF_EQ_POS)
						+ (REF_NE << REF_NE_POS);
				result.add("RELATIONAL-OPERATOR-REPLACEMENT-" + v);
			}
		}
		
		for(int lt_suplant = 0; lt_suplant < 6; lt_suplant++) {
			if(lt_suplant != LT) {
				int v = (EQ << PRM_EQ_POS) 
						+ (lt_suplant << PRM_LT_POS)
						+ (LE << PRM_LE_POS)
						+ (GT << PRM_GT_POS)
						+ (GE << PRM_GE_POS)
						+ (NE << PRM_NE_POS)
						+ (REF_EQ << REF_EQ_POS)
						+ (REF_NE << REF_NE_POS);
				result.add("RELATIONAL-OPERATOR-REPLACEMENT-" + v);
			}
		}
		
		for(int le_suplant = 0; le_suplant < 6; le_suplant++) {
			if(le_suplant != LE) {
				int v = (EQ << PRM_EQ_POS) 
						+ (LT << PRM_LT_POS)
						+ (le_suplant << PRM_LE_POS)
						+ (GT << PRM_GT_POS)
						+ (GE << PRM_GE_POS)
						+ (NE << PRM_NE_POS)
						+ (REF_EQ << REF_EQ_POS)
						+ (REF_NE << REF_NE_POS);
				result.add("RELATIONAL-OPERATOR-REPLACEMENT-" + v);
			}
		}
		
		for(int gt_suplant = 0; gt_suplant < 6; gt_suplant++) {
			if(gt_suplant != GT) {
				int v = (EQ << PRM_EQ_POS) 
						+ (LT << PRM_LT_POS)
						+ (LE << PRM_LE_POS)
						+ (gt_suplant << PRM_GT_POS)
						+ (GE << PRM_GE_POS)
						+ (NE << PRM_NE_POS)
						+ (REF_EQ << REF_EQ_POS)
						+ (REF_NE << REF_NE_POS);
				result.add("RELATIONAL-OPERATOR-REPLACEMENT-" + v);
			}
		}
		
		for(int ge_suplant = 0; ge_suplant < 6; ge_suplant++) {
			if(ge_suplant != GE) {
				int v = (EQ << PRM_EQ_POS) 
						+ (LT << PRM_LT_POS)
						+ (LE << PRM_LE_POS)
						+ (GT << PRM_GT_POS)
						+ (ge_suplant << PRM_GE_POS)
						+ (NE << PRM_NE_POS)
						+ (REF_EQ << REF_EQ_POS)
						+ (REF_NE << REF_NE_POS);
				result.add("RELATIONAL-OPERATOR-REPLACEMENT-" + v);
			}
		}
		
		for(int ne_suplant = 0; ne_suplant < 6; ne_suplant++) {
			if(ne_suplant != NE) {
				int v = (EQ << PRM_EQ_POS) 
						+ (LT << PRM_LT_POS)
						+ (LE << PRM_LE_POS)
						+ (GT << PRM_GT_POS)
						+ (GE << PRM_GE_POS)
						+ (ne_suplant << PRM_NE_POS)
						+ (REF_EQ << REF_EQ_POS)
						+ (REF_NE << REF_NE_POS);
				result.add("RELATIONAL-OPERATOR-REPLACEMENT-" + v);
			}
		}
		
		for(int ref_eq_suplant = 0; ref_eq_suplant < 2; ref_eq_suplant++) {
			if(ref_eq_suplant != REF_EQ) {
				int v = (EQ << PRM_EQ_POS) 
						+ (LT << PRM_LT_POS)
						+ (LE << PRM_LE_POS)
						+ (GT << PRM_GT_POS)
						+ (GE << PRM_GE_POS)
						+ (NE << PRM_NE_POS)
						+ (ref_eq_suplant << REF_EQ_POS)
						+ (REF_NE << REF_NE_POS);
				result.add("RELATIONAL-OPERATOR-REPLACEMENT-" + v);
			}
		}
		
		for(int ref_ne_suplant = 0; ref_ne_suplant < 2; ref_ne_suplant++) {
			if(ref_ne_suplant != REF_NE) {
				int v = (EQ << PRM_EQ_POS) 
						+ (LT << PRM_LT_POS)
						+ (LE << PRM_LE_POS)
						+ (GT << PRM_GT_POS)
						+ (GE << PRM_GE_POS)
						+ (NE << PRM_NE_POS)
						+ (REF_EQ << REF_EQ_POS)
						+ (ref_ne_suplant << REF_NE_POS);
				result.add("RELATIONAL-OPERATOR-REPLACEMENT-" + v);
			}
		}
		return result;
	}

	@Override
	public String description() {
		return identifier();
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
				if(shouldMutate()) {
					switch(opcode) {
					case IFEQ:
					case IFNE:
					case IFLT:
					case IFGE:
					case IFGT:
					case IFLE:
					case IF_ICMPEQ:
					case IF_ICMPNE:
					case IF_ICMPLT:
					case IF_ICMPGE:
					case IF_ICMPGT:
					case IF_ICMPLE:
					case IF_ACMPEQ:
					case IF_ACMPNE:
						this.mv.visitJumpInsn(ROM.get(opcode), label);
						return;
					}
				}
				this.mv.visitJumpInsn(opcode, label);
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