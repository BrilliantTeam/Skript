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
 * Copyright 2011-2016 Peter Güttinger and contributors
 * 
 */

package ch.njol.skript.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.SyntaxElementInfo;

/**
 * Generates HTML based Skript documentation.
 */
public class HTMLGenerator {
	
	private File template;
	private File output;
	
	private String skeleton;
	
	public HTMLGenerator(File templateDir, File outputDir) {
		this.template = templateDir;
		this.output = outputDir;
		
		this.skeleton = readFile(new File(template + "/template.html")); // Skeleton which contains every other page
	}
	
	void generate() {
		for (File f : template.listFiles()) {
			if (f.getName() == "template.html" || !f.getName().endsWith(".html") || f.isDirectory())
				continue; // Ignore skeleton, README and directories
			
			String content = readFile(f);
			String page = skeleton.replace("${content}", content); // Content to inside skeleton
			
			page = page.replace("${skript.version}", Skript.getVersion().toString()); // Skript version
			
			List<String> replace = Lists.newArrayList();
			int include = page.indexOf("${include"); // Single file includes
			while (include != -1) {
				int endIncl = page.indexOf("}", include);
				String name = page.substring(include + 10, endIncl);
				replace.add(name);
				
				include = page.indexOf("${include", endIncl);
			}
			
			for (String name : replace) {
				String temp = readFile(new File(template + "/template/" + name));
				page = page.replace("${include " + name + "}", temp);
			}
			
			int generate = page.indexOf("${generate"); // Generate expressions etc. TODO
			if (generate != -1) {
				int nextBracket = page.indexOf("}", generate);
				String[] genParams = page.substring(generate + 11, nextBracket).split(" ");
				String generated = "";
				
				String descTemp = readFile(new File(template + "/template/" + genParams[1]));
				String genType = genParams[0];
				if (genType == "expressions") {
					Iterator<ExpressionInfo<?,?>> it = Skript.getExpressions();
					while (it.hasNext()) {
						ExpressionInfo<?,?> info = it.next();
						String desc = generateElement(descTemp, info);
					}
				}
			}
		}
	}
	
	String generateElement(String descTemp, SyntaxElementInfo<?> info) {
		String desc = descTemp.replace("${element.name}", info.c.getAnnotation(Name.class).value());
		desc = desc.replace("${element.since}", info.c.getAnnotation(Since.class).value());
		desc = desc.replace("${element.desc}", Joiner.on("\n").join(info.c.getAnnotation(Description.class).value()));
		desc = desc.replace("${element.examples}", Joiner.on("\n").join(info.c.getAnnotation(Examples.class).value()));
		
		List<String> toGen = Lists.newArrayList();
		int generate = desc.indexOf("${generate");
		while (generate != -1) {
			int nextBracket = desc.indexOf("}", generate);
			String data = desc.substring(generate + 11, nextBracket);
			toGen.add(data);
			
			generate = desc.indexOf("${generate", nextBracket);
		}
		
		// Assume element.pattern generate; TODO
		for (String data : toGen) {
			String[] split = data.split(" ");
			String pattern = readFile(new File(template + "/template/" + split[1]));
			String patterns = "";
			for (String line : info.patterns) {
				String parsed = pattern.replace("${element.pattern}", line);
				patterns += parsed;
			}
			
			desc.replace("${generate element.patterns " + split[1], patterns);
		}
		
		return desc;
	}
	
	@SuppressWarnings("null")
	String readFile(File f) {
		try {
			return Files.toString(f, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	void writeFile(File f, String data) {
		try {
			Files.write(data, f, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
