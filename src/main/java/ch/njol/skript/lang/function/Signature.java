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

package ch.njol.skript.lang.function;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.util.NonNullPair;

/**
 * Function signature: name, parameter types and a return type.
 */
public class Signature<T> {
	
	final String name; // Stored for hashCode
	final List<Parameter<?>> parameters;
	@Nullable
	final ClassInfo<T> returnType;
	@Nullable
	final NonNullPair<String, Boolean> dbgInfo;
	
	public Signature(String name, List<Parameter<?>> parameters, @Nullable final ClassInfo<T> returnType, @Nullable final NonNullPair<String, Boolean> dbgInfo) {
		this.name = name;
		this.parameters = parameters;
		this.returnType = returnType;
		this.dbgInfo = dbgInfo;
		
		Functions.signatures.put(name, this);
	}
	
	public List<Parameter<?>> getParameters() {
		return parameters;
	}
	
	@Nullable
	public ClassInfo<T> getReturnType() {
		return returnType;
	}
	
	@Nullable
	public NonNullPair<String, Boolean> getDbgInfo() {
		return dbgInfo;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
