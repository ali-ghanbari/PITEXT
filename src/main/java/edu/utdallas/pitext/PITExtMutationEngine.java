package edu.utdallas.pitext;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodInfo;

public class PITExtMutationEngine implements MutationEngine {
	private final Map<String, PITExtMutationOperatorStub> mutatorStubs;
	private final Predicate<MethodInfo> isExcluded;
	
	public PITExtMutationEngine(Map<String, PITExtMutationOperatorStub> mutatorStubs, Predicate<MethodInfo> isExcluded) {
		this.mutatorStubs = mutatorStubs;
		this.isExcluded = isExcluded;
	}

	@Override
	public Mutater createMutator(ClassByteArraySource source) {
		return new PITExtMutater(source, this);
	}

	@Override
	public Collection<String> getMutatorNames() {
		return mutatorStubs.keySet();
	}
	
	public Collection<PITExtMutationOperatorStub> allMutators() {
		return mutatorStubs.values();
	}
	
	public Collection<PITExtMutationOperatorStub> mutatorsFor(int opcode, int previousOpcode, Object... other) {
		return allMutators().stream()
				.filter(m -> m.canMutate(opcode, previousOpcode, other))
				.collect(Collectors.toList());
	}
	
	public boolean isExcluded(MethodInfo mi) {
		return false;//isExcluded.apply(mi);
	}
	
	public PITExtMutationOperatorStub getMutationOperatorStubByName(String identifier) {
		return mutatorStubs.get(identifier);
	}
}
