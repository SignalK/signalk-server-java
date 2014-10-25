package nz.co.fortytwo.freeboard.signalk.impl;

import nz.co.fortytwo.freeboard.signalk.SignalKModel;

/**
 * Factory to get signalKModel singleton
 * @author robert
 *
 */
public class SignalKModelFactory {
	private static SignalKModel signalKModel;
	/**
	 * Get the signalKModel singleton
	 * @return
	 */
	public static synchronized SignalKModel getInstance(){
		if(signalKModel==null){
			signalKModel=new SignalKModelImpl();
		}
		return signalKModel;
	}
}
