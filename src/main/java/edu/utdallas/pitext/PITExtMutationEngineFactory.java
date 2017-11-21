package edu.utdallas.pitext;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodInfo;

public class PITExtMutationEngineFactory implements MutationEngineFactory {

	@Override
	public String description() {
		return "PITExt - An extension to PIT";
	}

	@Override
	public MutationEngine createEngine(Predicate<String> excludedMethods, Collection<String> mutatorNames) {
		final Predicate<MethodInfo> isExcluded = mi -> excludedMethods.apply(mi.getName());
		if(mutatorNames == null || mutatorNames.isEmpty()) {
			mutatorNames = PITExtMutationOperatorFactory.allVariants();
		}
		final Map<String, PITExtMutationOperatorStub> mutatorStubs = mutatorNames.stream()
				.map(PITExtMutationOperatorFactory::forName)
				.collect(Collectors.toMap(PITExtMutationOperatorStub::identifier, Function.identity()));
		
		return new PITExtMutationEngine(mutatorStubs, isExcluded);
	}

	@Override
	public String name() {
		return "pitext";
	}

}
