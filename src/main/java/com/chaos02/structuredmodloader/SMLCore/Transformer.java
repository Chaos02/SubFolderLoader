package com.chaos02.structuredmodloader.SMLCore;

import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (basicClass == null || !transformedName.equals("net.minecraftforge.fml.relauncher.libraries.LibraryManager"))
			return basicClass;
		return LibraryManager.class.getName().getBytes();
	}

}
