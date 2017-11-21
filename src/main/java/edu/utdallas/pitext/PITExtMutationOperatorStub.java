package edu.utdallas.pitext;

import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.MutationIdentifier;

public interface PITExtMutationOperatorStub {
	public boolean canMutate(int opcode, int previousOpcode, Object... other);
	
	public String identifier();
	
	public String description();
	
	public MethodVisitor createMutator(MutationIdentifier mId, MethodVisitor mv);
}
