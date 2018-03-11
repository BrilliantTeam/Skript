/**
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
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
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
 */

package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Raw Name")
@Description("Raw Minecraft material name for given item. Note that this is not guaranteed to give same results on all servers.")
@Examples("raw name of tool of player")
@Since("unknown (2.2) (Estimate Mirre's edit), 2.2-dev35 (Converted to SimplePropertyExpression)")
public class ExprRawName extends SimplePropertyExpression<ItemType, String[]> {
	
	static {
		register(ExprRawName.class, String[].class, "(raw|minecraft|vanilla) name[s]", "itemtypes");
	}
	
	@Override
	public Class<String[]> getReturnType() {
		return String[].class;
	}
	
	@Override
	protected String getPropertyName() {
		return "(raw|minecraft|vanilla) name[s]";
	}
	
	@SuppressWarnings("null")
	@Override
	public String[] convert(final ItemType itemtype) {
		List<String> names = itemtype.getRawNames();
		return names.toArray(new String[names.size()]);
	}

}
