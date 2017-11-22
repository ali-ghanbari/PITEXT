package edu.utdallas.pitext;

public final class Util {
	private Util() {
		
	}
	
	public static boolean isBetween(int x, int lower, int upper) {
		return lower <= x && x <= upper;
	}
	
	/*Oscar, thanks!*/
	public static boolean hasFlag(int value, int flag) {
		return (value & flag) == flag;
	}
}
