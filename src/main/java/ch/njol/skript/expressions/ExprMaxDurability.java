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
package ch.njol.skript.expressions;

import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;

@Name("Max Durability")
@Description("The maximum durability of an item.")
@Examples("maximum durability of diamond sword")
@Since("INSERT VERSION")
public class ExprMaxDurability extends SimplePropertyExpression<Object, Short> {

	static {
		register(ExprMaxDurability.class, Short.class, "max[imum] durabilit(y|ies)", "itemstacks/slots");
	}
	
	@Override
	@Nullable
	public Short convert(Object o) {
		if (o instanceof Slot) {
			final ItemStack i = ((Slot) o).getItem();
			return i == null ? null : i.getType().getMaxDurability();
		} else {
			return ((ItemStack) o).getType().getMaxDurability();
		}
	}
	
	@Override
	public Class<? extends Short> getReturnType() {
		return Short.class;
	}

	@Override
	protected String getPropertyName() {
		return "max durability";
	}
	
}
