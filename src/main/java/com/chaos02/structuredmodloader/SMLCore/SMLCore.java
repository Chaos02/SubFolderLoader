package com.chaos02.structuredmodloader.SMLCore;

import java.io.File;
import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.*;
import scala.actors.threadpool.Arrays;

/**
 * 
 * @see <a href="https://www.reddit.com/r/feedthebeast/comments/mhn530/comment/gt05unm/"> Coremod tutorial </a>
 *
 */

@Name("SMLCore")
@MCVersion("1.12.2")
@DependsOn("")
@SortingIndex(2000)
@TransformerExclusions("com.chaos02.structuredmodloader.SMLCore")
public class SMLCore implements IFMLLoadingPlugin {

	public static File mcLocation;

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {Transformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSetupClass() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * Inject coremod data into this coremod
     * This data includes:
     * "mcLocation" : the location of the minecraft directory,
     * "coremodList" : the list of coremods
     * "coremodLocation" : the file this coremod loaded from,
     */
	@Override
	public void injectData(Map<String, Object> data) {
		mcLocation = new File(data.get("mcLocation").toString());

	}

	@Override
	public String getAccessTransformerClass() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the location of the minecraft directory
	 */

}
