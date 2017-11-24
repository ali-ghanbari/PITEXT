package edu.utdallas.pitext;

import java.util.Collection;

import org.pitest.util.PitError;

public final class PITExtMutationOperatorFactory {
	
	private PITExtMutationOperatorFactory() {
		
	}
	
	public static PITExtMutationOperatorStub forName(String variantIdentifier) {
		int variant = -1;
		String[] splits = variantIdentifier.split("\\-");
		if(Character.isDigit(splits[splits.length - 1].charAt(0))) {
			variant = Integer.parseInt(splits[splits.length - 1]);
		}
		if(variantIdentifier.matches("ABSOLUTE\\-VALUE\\-INSERTION\\-\\p{Digit}+")) {
			return new AbsoluteValueInsertion(variant);
		} else if(variantIdentifier.matches("ARITHMETIC\\-OPERATOR\\-DELETION\\-\\p{Digit}+")) {
			return new ArithmeticOperatorDeletion(variant);
		} else if(variantIdentifier.matches("ARITHMETIC\\-OPERATOR\\-REPLACEMENT\\-\\p{Digit}+")) {
			return new ArithmeticOperatorReplacement(variant);
		} else if(variantIdentifier.equals("BITWISE-OPERATOR")) {
			return new BitwiseOperator();
		} else if(variantIdentifier.matches("RELATIONAL\\-OPERATOR\\-REPLACEMENT\\-\\p{Digit}+")) {
			return new RelationalOperatorReplacement(variant);
		} else if(variantIdentifier.matches("REQUIRED\\-CONSTANT\\-REPLACEMENT\\-\\p{Digit}+")) {
			return new RequiredConstantReplacement(variant);
		} else if(variantIdentifier.matches("UNARY\\-OPERATOR\\-INSERTION\\-\\p{Digit}+")) {
			return new UnaryOperatorInsertion(variant);
		}
		throw new PitError("unidentified pitext operator " + variantIdentifier);
	}
	
	public static Collection<String> allVariants() {
		Collection<String> result;
		result = AbsoluteValueInsertion.allVariants();
		result.addAll(ArithmeticOperatorDeletion.allVariants());
		result.addAll(ArithmeticOperatorReplacement.allVariants());
		result.addAll(BitwiseOperator.allVariants());
		result.addAll(RelationalOperatorReplacement.allVariants());
		result.addAll(RequiredConstantReplacement.allVariants());
		result.addAll(UnaryOperatorInsertion.allVariants());
		return result;
	}
}
