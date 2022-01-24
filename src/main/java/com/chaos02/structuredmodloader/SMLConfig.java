package com.chaos02.structuredmodloader;

import java.util.List;

@SuppressWarnings("unused")
public class SMLConfig {
	private List<String> ignoreDir;
	private int recurseDepth;
	
	/*
	@Override
	public String toString() {
		return "[ ignoreDir: " + String.valueOf(ignoreDir) + "recurseDepth: " + String.valueOf(recurseDepth);
	}
	*/
	
	public void setDirs(List<String> StringList) {
		ignoreDir = StringList;
	}
	
	public void setDepth(int n) {
		recurseDepth = n;
	}
}
