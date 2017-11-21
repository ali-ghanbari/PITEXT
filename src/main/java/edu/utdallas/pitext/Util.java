package edu.utdallas.pitext;

public final class Util {
	private static int counter = 0;
	
	private Util() {
		
	}
	
	public static synchronized void println(String s) {
		try(java.io.PrintWriter pw = new java.io.PrintWriter("d:\\out--" + (counter++) + ".log")) {
			pw.println(s);
			pw.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public static boolean isBetween(int x, int lower, int upper) {
		return lower <= x && x <= upper;
	}
	
	/*Oscar, thanks!*/
	public static boolean hasFlag(int value, int flag) {
		return (value & flag) == flag;
	}
}
