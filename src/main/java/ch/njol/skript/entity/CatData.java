package ch.njol.skript.entity;

import org.bukkit.entity.Cat;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;

public class CatData extends EntityData<Cat> {
	
	static {
		if (Skript.classExists("org.bukkit.entity.Cat"))
			EntityData.register(CatData.class, "cat", Cat.class, "cat");
	}
	
	@Nullable
	private Cat.Type race = null;
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null)
			race = ((Literal<Cat.Type>) exprs[0]).getSingle();
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Cat> c, @Nullable Cat cat) {
		race = cat.getCatType();
		return false;
	}
	
	@Override
	public void set(Cat entity) {
		Cat.Type type = race != null ? race : CollectionUtils.getRandom(Cat.Type.values());
		assert type != null;
		entity.setCatType(type);
	}
	
	@Override
	protected boolean match(Cat entity) {
		return race == null || entity.getCatType() == race;
	}
	
	@Override
	public Class<? extends Cat> getType() {
		return Cat.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new CatData();
	}
	
	@Override
	protected int hashCode_i() {
		return race != null ? race.hashCode() : 0;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> data) {
		return data instanceof CatData ? race == ((CatData) data).race : false;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		return data instanceof CatData ? race == null || race == ((CatData) data).race : false;
	}
}
