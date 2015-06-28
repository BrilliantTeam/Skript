/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.bukkit.Bukkit;

import ch.njol.skript.config.Config;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.util.Pair;
import ch.njol.util.coll.iterator.EnumerationIterable;

/**
 * @author Peter Güttinger
 *
 */
public class FeatureConfig {
	
	private static boolean loaded = false;
	private static boolean debug = false;
	
	private static List<String> disabledClassNames = new ArrayList<String>();
	private static List<String> disabledPatterns = new ArrayList<String>();
	
	public static boolean contains(String className, String... patterns){
		if(disabledClassNames.contains(className)){
			if(debug)
				Bukkit.getLogger().info("Disabling feature " + className + " through the Features.sk config.");
			return true;
		}
		
		for(String pattern : patterns){
			if(disabledPatterns.contains(pattern)){
				if(debug)
					Bukkit.getLogger().info("Disabling the feature " + className + " which had the exact pattern: " + pattern + ".");
				return true;
			}
		}
		return false;
	}
	

	
	static void load(File f){
		if(loaded)
			return;
		loaded = true;
		File featureFile = new File(Skript.getInstance().getDataFolder(), "features.sk");
		Config mc = null;
		if(featureFile.exists()){
			try {
				mc = new Config(featureFile, false, false, ":");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			ZipFile zip = null;
			try{
				zip = new ZipFile(f);
				File saveTo = null;
				ZipEntry entry = zip.getEntry("features.sk");
				if(entry != null){
					final File af = new File(Skript.getInstance().getDataFolder(), entry.getName());
					if (!af.exists())
						saveTo = af;
				}if (saveTo != null) {
					final InputStream in = zip.getInputStream(entry);
					try {
						assert in != null;
						FileUtils.save(in, saveTo);
					} finally {
						in.close();
					}
				}
			}catch (final ZipException e1) {} catch (final IOException e2) {}
			finally {
				if (zip != null) {
					try {
						zip.close();
					} catch (final IOException e3) {}
				}
				featureFile = new File(Skript.getInstance().getDataFolder(), "features.sk");
				try {
					mc = new Config(featureFile, false, false, ":");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		if(mc != null){
			HashMap<String, String> map = mc.toMap(",");
			for(Entry<String, String> entry : map.entrySet()){
				if(entry.getKey().equalsIgnoreCase("DEBUG")){
					if(entry.getValue().equalsIgnoreCase("true"))
						debug = true;
				}else if(entry.getKey().startsWith("Feature") && !entry.getValue().equalsIgnoreCase("null")){
					disabledPatterns.add(entry.getValue());
				}else{
					disabledClassNames.add(entry.getKey());
					if(!entry.getValue().equalsIgnoreCase("null")){
						disabledPatterns.add(entry.getValue());
					}
				}
			}
		}
	}
	
	static void discard(){
		if(loaded){
			disabledPatterns.clear();
			disabledClassNames.clear();
		}
	}
}
