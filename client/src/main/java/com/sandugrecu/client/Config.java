package com.sandugrecu.client;


public class Config {
	private static String serverIP = "localhost";
	
	public static String getServerIP() {
		return serverIP;
	}
	
	public static void setServerIP(String newIP) {
        serverIP = newIP;
    }
}
