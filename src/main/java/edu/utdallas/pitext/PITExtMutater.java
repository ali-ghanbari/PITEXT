package edu.utdallas.pitext;

import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.Option;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class PITExtMutater implements Mutater {
	private final PITExtMutationEngine engine;
	private final ClassByteArraySource byteSource;
	
	public PITExtMutater(ClassByteArraySource byteSource, PITExtMutationEngine engine) {
		this.engine = engine;
		this.byteSource = byteSource;
	}

	@Override
	public Mutant getMutation(MutationIdentifier mId) {
		final Option<byte[]> bytes = byteSource.getBytes(mId.getClassName().asJavaName());
		final ClassReader classReader = new ClassReader(bytes.value());
		final ClassWriter classWriter = new ClassWriter(0);
		final MutatingClassVisitor mcVisitor = 
				new MutatingClassVisitor(mId, engine, classWriter);
		classReader.accept(mcVisitor, 0);
		return new Mutant(new MutationDetails(mId, "", "", 0, 0), classWriter.toByteArray());
	}

	@Override
	public List<MutationDetails> findMutations(ClassName classToMutate) {
		return byteSource.getBytes(classToMutate.asInternalName())
				.flatMap(classBytes -> findMutationPoints(classToMutate, classBytes));
	}

	private List<MutationDetails> findMutationPoints(ClassName className, byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        MutationPointCollector collector = new MutationPointCollector(className, engine);
        reader.accept(collector, 0);
        return collector.mutationPoints;
    }
}
