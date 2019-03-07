package org.opentripplanner.scripting.api;
import org.opentripplanner.scripting.api.OtpsEntryPoint;

public class Test {

	public static void main(String args[]) throws Exception {
		String[] a = {"--graphs", "C:/Projekte/OTP Router", "--router", "flensburg_car"};
		OtpsEntryPoint otp = OtpsEntryPoint.fromArgs(a);
		OtpsRouter router = otp.getRouter();
	}
}