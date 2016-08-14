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
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.registrations.Classes;

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
	
	public void generate() {
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
			
			int generate = page.indexOf("${generate"); // Generate expressions etc.
			while (generate != -1) {
				int nextBracket = page.indexOf("}", generate);
				String[] genParams = page.substring(generate + 11, nextBracket).split(" ");
				String generated = "";
				
				String descTemp = readFile(new File(template + "/template/" + genParams[1]));
				String genType = genParams[0];
				if (genType == "expressions") {
					Iterator<ExpressionInfo<?,?>> it = Skript.getExpressions();
					while (it.hasNext()) {
						ExpressionInfo<?,?> info = it.next();
						assert info != null;
						String desc = generateAnnotated(descTemp, info);
						generated += desc;
					}
				} else if (genType == "effects") {
					for (SyntaxElementInfo<? extends Effect> info : Skript.getEffects()) {
						assert info != null;
						generated += generateAnnotated(descTemp, info);
					}
				} else if (genType == "conditions") {
					for (SyntaxElementInfo<? extends Condition> info : Skript.getConditions()) {
						assert info != null;
						generated += generateAnnotated(descTemp, info);
					}
				} else if (genType == "events") {
					for (SkriptEventInfo<?> info : Skript.getEvents()) {
						assert info != null;
						generated += generateEvent(descTemp, info);
					}
				} else if (genType == "classes") {
					for (ClassInfo<?> info : Classes.getClassInfos()) {
						assert info != null;
						generated += generateClass(descTemp, info);
					}
				}
				
				page = page.replace(page.substring(generate, nextBracket), generated);
				
				generate = page.indexOf("${generate", nextBracket);
			}
			
			writeFile(new File(output + "/" + f.getName()), page);
		}
	}
	
	/**
	 * Generates documentation entry for a type which is documented using
	 * annotations. This means expressions, effects and conditions.
	 * @param descTemp Template for description.
	 * @param info Syntax element info.
	 * @return Generated HTML entry.
	 */
	String generateAnnotated(String descTemp, SyntaxElementInfo<?> info) {
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
			
			desc.replace("${generate element.patterns " + split[1] + "}", patterns);
		}
		
		return desc;
	}
	
	String generateEvent(String descTemp, SkriptEventInfo<?> info) {
		String desc = descTemp.replace("${element.name}", info.getName());
		desc = desc.replace("${element.since}", info.getSince());
		desc = desc.replace("${element.desc}", Joiner.on("\n").join(info.getDescription()));
		desc = desc.replace("${element.examples}", Joiner.on("\n").join(info.getExamples()));
		
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
			
			desc.replace("${generate element.patterns " + split[1] + "}", patterns);
		}
		
		return desc;
	}
	
	String generateClass(String descTemp, ClassInfo<?> info) {
		String desc = descTemp.replace("${element.name}", info.getDocName());
		desc = desc.replace("${element.since}", info.getSince());
		desc = desc.replace("${element.desc}", Joiner.on("\n").join(info.getDescription()));
		desc = desc.replace("${element.examples}", Joiner.on("\n").join(info.getExamples()));
		
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
			String[] lines = info.getUsage();
			if (lines == null)
				continue;
			for (String line : lines) {
				String parsed = pattern.replace("${element.pattern}", line);
				patterns += parsed;
			}
			
			desc.replace("${generate element.patterns " + split[1] + "}", patterns);
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
