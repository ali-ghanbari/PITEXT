package edu.utdallas.pitext;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;

public class MutatingClassVisitor extends ClassVisitor implements Opcodes {
	private final MutationIdentifier mId;
	private final PITExtMutationEngine engine;

	public MutatingClassVisitor(MutationIdentifier mId, PITExtMutationEngine engine, ClassVisitor cv) {
		super(ASM6, cv);
		this.mId = mId;
		this.engine = engine;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodInfo theMethod = (new MethodInfo())
				.withAccess(access)
				.withMethodDescriptor(desc)
				.withMethodName(name);
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions); 
		if(!engine.isExcluded(theMethod)) {
			mv = engine.getMutationOperatorStubByName(mId.getMutator()).createMutator(mId, mv);
		}
		return mv;
	}

//	@Override
//	public void visitEnd() {
//		/*creating __failOnZero__ method for every class*/
//		MethodVisitor mv;
//		
//		Label l0;
//		
//		mv = this.cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "__failOnZero__", "(I)I", null, null);
//		mv.visitCode();
//		mv.visitVarInsn(ILOAD, 0);
//		l0 = new Label();
//		mv.visitJumpInsn(IFNE, l0);
//		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
//		mv.visitInsn(DUP);
//		mv.visitLdcInsn("zero value detected");
//		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
//		mv.visitInsn(ATHROW);
//		mv.visitLabel(l0);
//		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
//		mv.visitVarInsn(ILOAD, 0);
//		mv.visitInsn(IRETURN);
//		mv.visitMaxs(3, 1);
//		mv.visitEnd();
//
////		mv = this.cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "__failOnZero__", "(J)J", null, null);
////		mv.visitCode();
////		mv.visitVarInsn(LLOAD, 0);
////		mv.visitInsn(LCONST_0);
////		mv.visitInsn(LCMP);
////		l0 = new Label();
////		mv.visitJumpInsn(IFNE, l0);
////		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
////		mv.visitInsn(DUP);
////		mv.visitLdcInsn("failed on zero!");
////		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
////		mv.visitInsn(ATHROW);
////		mv.visitLabel(l0);
////		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
////		mv.visitVarInsn(LLOAD, 0);
////		mv.visitInsn(LRETURN);
////		mv.visitMaxs(4, 2);
////		mv.visitEnd();
////
////		mv = this.cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "__failOnZero__", "(F)F", null, null);
////		mv.visitCode();
////		mv.visitVarInsn(FLOAD, 0);
////		mv.visitInsn(FCONST_0);
////		mv.visitInsn(FCMPL);
////		l0 = new Label();
////		mv.visitJumpInsn(IFEQ, l0);
////		mv.visitVarInsn(FLOAD, 0);
////		mv.visitInsn(FRETURN);
////		mv.visitLabel(l0);
////		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
////		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
////		mv.visitInsn(DUP);
////		mv.visitLdcInsn("failed on zero!");
////		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
////		mv.visitInsn(ATHROW);
////		mv.visitMaxs(3, 1);
////		mv.visitEnd();
////
////		mv = this.cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "__failOnZero__", "(D)D", null, null);
////		mv.visitCode();
////		mv.visitVarInsn(DLOAD, 0);
////		mv.visitInsn(DCONST_0);
////		mv.visitInsn(DCMPL);
////		l0 = new Label();
////		mv.visitJumpInsn(IFEQ, l0);
////		mv.visitVarInsn(DLOAD, 0);
////		mv.visitInsn(DRETURN);
////		mv.visitLabel(l0);
////		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
////		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
////		mv.visitInsn(DUP);
////		mv.visitLdcInsn("failed on zero!");
////		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
////		mv.visitInsn(ATHROW);
////		mv.visitMaxs(4, 2);
////		mv.visitEnd();
////		/*creating __pi1__ and __pi2__ for types 64-bit numeric types for methods for every class*/
////		
////		mv = this.cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "__pi1__", "(JJ)J", null, null);
////		if(mv != null) {
////			mv.visitCode();
////			mv.visitVarInsn(LLOAD, 0);
////			mv.visitInsn(LRETURN);
////			mv.visitMaxs(2, 4);
////			mv.visitEnd();
////		}
////		
////		mv = this.cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "__pi2__", "(JJ)J", null, null);
////		if(mv != null) {
////			mv.visitCode();
////			mv.visitVarInsn(LLOAD, 2);
////			mv.visitInsn(LRETURN);
////			mv.visitMaxs(2, 4);
////			mv.visitEnd();
////		}
////		
////		mv = this.cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "__pi1__", "(DD)D", null, null);
////		if(mv != null) {
////			mv.visitCode();
////			mv.visitVarInsn(DLOAD, 0);
////			mv.visitInsn(DRETURN);
////			mv.visitMaxs(2, 4);
////			mv.visitEnd();
////		}
////		
////		mv = this.cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "__pi2__", "(DD)D", null, null);
////		if(mv != null) {
////			mv.visitCode();
////			mv.visitVarInsn(DLOAD, 2);
////			mv.visitInsn(DRETURN);
////			mv.visitMaxs(2, 4);
////			mv.visitEnd();
////		}
//		
//		super.visitEnd();
//	}

}
