package edu.utdallas.pitext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.blocks.ConcreteBlockCounter;
import org.pitest.reloc.asm.commons.Method;

final class MutationPointCollector extends ClassVisitor implements Opcodes {
	private final ClassName className;
	private final PITExtMutationEngine engine;
	final ConcreteBlockCounter blockCounter;
	final List<MutationDetails> mutationPoints;
	private String source;

	public MutationPointCollector(ClassName className, PITExtMutationEngine engine) {
		super(ASM6);
		this.className = className;
		this.engine = engine;
		this.source = null;
		this.mutationPoints = new ArrayList<>();
		this.blockCounter = new ConcreteBlockCounter();
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if(Util.hasFlag(access, Opcodes.ACC_ABSTRACT) || Util.hasFlag(access, Opcodes.ACC_SYNTHETIC))
			return super.visitMethod(access, name, desc, signature, exceptions);
		Method method = new Method(name, desc);
		return new MethodBodyAnalyzer(method, this);
	}

	@Override
	public void visitSource(String source, String debug) {
		this.source = source;
		super.visitSource(source, debug);
	}
	
	void registerMutationPoint(Method method, int lineNumber, int index, int opcode, int previousOpcode, Object... other) {
		for(PITExtMutationOperatorStub mutatorStub : engine.mutatorsFor(opcode, previousOpcode, other)) {
			registerMutationPoint(method, mutatorStub, lineNumber, index);
		}
	}
	
	private void registerMutationPoint(Method method, PITExtMutationOperatorStub mutatorStub, int lineNumber, int index) {
		Location location = new Location(className, MethodName.fromString(method.getName()), method.getDescriptor());
		MutationIdentifier id = new MutationIdentifier(location, Collections.singleton(index), mutatorStub.identifier());
		mutationPoints.add(new MutationDetails(id, source, mutatorStub.description(), lineNumber, blockCounter.getCurrentBlock()));
	}
}
