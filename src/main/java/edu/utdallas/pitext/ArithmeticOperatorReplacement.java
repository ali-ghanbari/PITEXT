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

public class ArithmeticOperatorReplacement implements PITExtMutationOperatorStub, Opcodes {
	private final int variant;
	
	private static final int PLUS = 0b0000_0000_0000_0000_0000_0000_0000_0000;
	private static final int MINUS = 0b0000_0000_0000_0000_0000_0000_0000_0001;
	private static final int ASTERISK = 0b0000_0000_0000_0000_0000_0000_0000_0010;
	private static final int DIVISION = 0b0000_0000_0000_0000_0000_0000_0000_0011;
	private static final int MODULO = 0b0000_0000_0000_0000_0000_0000_0000_0100;
	
	private static final int PLUS_POS = 0;
	private static final int MINUS_POS = 4;
	private static final int ASTERISK_POS = 8;
	private static final int DIVISION_POS = 12;
	private static final int MODULO_POS = 16;
	
	private static final int PLUS_POS_MASK = 0b0000_0000_0000_0000_0000_0000_0000_0111;
	private static final int MINUS_POS_MASK = 0b0000_0000_0000_0000_0000_0000_0111_0000;
	private static final int ASTERISK_POS_MASK = 0b0000_0000_0000_0000_0000_0111_0000_0000;
	private static final int DIVISION_POS_MASK = 0b0000_0000_0000_0000_0111_0000_0000_0000;
	private static final int MODULO_POS_MASK = 0b0000_0000_0000_0111_0000_0000_0000_0000;
	
	private final Map<Integer, Integer> AOM;
	
	private static final Map<Integer, Integer> int_AOMap;
	private static final Map<Integer, Integer> long_AOMap;
	private static final Map<Integer, Integer> float_AOMap;
	private static final Map<Integer, Integer> double_AOMap;
	
	static {
		int_AOMap = new HashMap<>();
		int_AOMap.put(PLUS, IADD);
		int_AOMap.put(MINUS, ISUB);
		int_AOMap.put(ASTERISK, IMUL);
		int_AOMap.put(DIVISION, IDIV);
		int_AOMap.put(MODULO, IREM);
		
		long_AOMap = new HashMap<>();
		long_AOMap.put(PLUS, LADD);
		long_AOMap.put(MINUS, LSUB);
		long_AOMap.put(ASTERISK, LMUL);
		long_AOMap.put(DIVISION, LDIV);
		long_AOMap.put(MODULO, LREM);
		
		float_AOMap = new HashMap<>();
		float_AOMap.put(PLUS, FADD);
		float_AOMap.put(MINUS, FSUB);
		float_AOMap.put(ASTERISK, FMUL);
		float_AOMap.put(DIVISION, FDIV);
		float_AOMap.put(MODULO, FREM);
		
		double_AOMap = new HashMap<>();
		double_AOMap.put(PLUS, DADD);
		double_AOMap.put(MINUS, DSUB);
		double_AOMap.put(ASTERISK, DMUL);
		double_AOMap.put(DIVISION, DDIV);
		double_AOMap.put(MODULO, DREM);
	}

	public ArithmeticOperatorReplacement(int variant) {
		this.variant = variant;
		this.AOM = new HashMap<>();
		populateAOM();
	}
	
	private void populateAOM() {
		AOM.put(IADD, int_AOMap.get((variant >>> PLUS_POS) & PLUS_POS_MASK));
		AOM.put(ISUB, int_AOMap.get((variant >>> MINUS_POS) & MINUS_POS_MASK));
		AOM.put(IMUL, int_AOMap.get((variant >>> ASTERISK_POS) & ASTERISK_POS_MASK));
		AOM.put(IDIV, int_AOMap.get((variant >>> DIVISION_POS) & DIVISION_POS_MASK));
		AOM.put(IREM, int_AOMap.get((variant >>> MODULO_POS) & MODULO_POS_MASK));
		
		AOM.put(LADD, long_AOMap.get((variant >>> PLUS_POS) & PLUS_POS_MASK));
		AOM.put(LSUB, long_AOMap.get((variant >>> MINUS_POS) & MINUS_POS_MASK));
		AOM.put(LMUL, long_AOMap.get((variant >>> ASTERISK_POS) & ASTERISK_POS_MASK));
		AOM.put(LDIV, long_AOMap.get((variant >>> DIVISION_POS) & DIVISION_POS_MASK));
		AOM.put(LREM, long_AOMap.get((variant >>> MODULO_POS) & MODULO_POS_MASK));
		
		AOM.put(FADD, float_AOMap.get((variant >>> PLUS_POS) & PLUS_POS_MASK));
		AOM.put(FSUB, float_AOMap.get((variant >>> MINUS_POS) & MINUS_POS_MASK));
		AOM.put(FMUL, float_AOMap.get((variant >>> ASTERISK_POS) & ASTERISK_POS_MASK));
		AOM.put(FDIV, float_AOMap.get((variant >>> DIVISION_POS) & DIVISION_POS_MASK));
		AOM.put(FREM, float_AOMap.get((variant >>> MODULO_POS) & MODULO_POS_MASK));
		
		AOM.put(DADD, double_AOMap.get((variant >>> PLUS_POS) & PLUS_POS_MASK));
		AOM.put(DSUB, double_AOMap.get((variant >>> MINUS_POS) & MINUS_POS_MASK));
		AOM.put(DMUL, double_AOMap.get((variant >>> ASTERISK_POS) & ASTERISK_POS_MASK));
		AOM.put(DDIV, double_AOMap.get((variant >>> DIVISION_POS) & DIVISION_POS_MASK));
		AOM.put(DREM, double_AOMap.get((variant >>> MODULO_POS) & MODULO_POS_MASK));
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
		return "ARITHMETIC-OPERATOR-REPLACEMENT-" + variant;
	}

	public static Collection<String> allVariants() {
		List<String> result = new ArrayList<>();
		for(int plus_suplant = 0; plus_suplant < 5; plus_suplant++) {
			if(plus_suplant != PLUS) {
				int v = plus_suplant 
						+ (MINUS << MINUS_POS) 
						+ (ASTERISK << ASTERISK_POS) 
						+ (DIVISION << DIVISION_POS) 
						+ (MODULO << MODULO_POS);
				result.add("ARITHMETIC-OPERATOR-REPLACEMENT-" + v);
			}
		}
		for(int minus_suplant = 0; minus_suplant < 5; minus_suplant++) {
			if(minus_suplant != MINUS) {
				int v = PLUS 
					+ (minus_suplant << MINUS_POS) 
					+ (ASTERISK << ASTERISK_POS) 
					+ (DIVISION << DIVISION_POS) 
					+ (MODULO << MODULO_POS);
				result.add("ARITHMETIC-OPERATOR-REPLACEMENT-" + v);
			}
		}
		for(int asterisk_suplant = 0; asterisk_suplant < 5; asterisk_suplant++) {
			if(asterisk_suplant != ASTERISK) {
				int v = PLUS 
					+ (MINUS << MINUS_POS) 
					+ (asterisk_suplant << ASTERISK_POS) 
					+ (DIVISION << DIVISION_POS) 
					+ (MODULO << MODULO_POS);
				result.add("ARITHMETIC-OPERATOR-REPLACEMENT-" + v);
			}
		}
		for(int division_suplant = 0; division_suplant < 5; division_suplant++) {
			if(division_suplant != DIVISION) {
				int v = PLUS 
					+ (MINUS << MINUS_POS) 
					+ (ASTERISK << ASTERISK_POS) 
					+ (division_suplant << DIVISION_POS) 
					+ (MODULO << MODULO_POS);
				result.add("ARITHMETIC-OPERATOR-REPLACEMENT-" + v);
			}
		}
		for(int modulo_suplant = 0; modulo_suplant < 5; modulo_suplant++) {
			if(modulo_suplant != MODULO) {
				int v = PLUS 
						+ (MINUS << MINUS_POS) 
						+ (ASTERISK << ASTERISK_POS) 
						+ (DIVISION << DIVISION_POS) 
						+ (modulo_suplant << MODULO_POS);
				result.add("ARITHMETIC-OPERATOR-REPLACEMENT-" + v);
			}
		}
		return result;
	}

	@Override
	public String description() {
		return identifier(); //it would be difficult to give more precise explanation
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
						Util.println(opcode + "\t" + AOM.get(opcode));
						this.mv.visitInsn(AOM.get(opcode));
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
