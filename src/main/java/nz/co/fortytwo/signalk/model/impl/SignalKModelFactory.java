package nz.co.fortytwo.signalk.model.impl;

import nz.co.fortytwo.signalk.model.SignalKModel;

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
	/**
	 * Returns a different clean instance - only needed for testing!
	 * @return
	 */
	public static synchronized SignalKModel getCleanInstance(){
		return new SignalKModelImpl();
	}
}
