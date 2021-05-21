package com.github.hydrazine.minecraft;

import org.spacehq.mc.protocol.data.game.values.setting.Difficulty;
import org.spacehq.mc.protocol.data.game.values.world.WorldType;

public class World 
{
	
	private int dimension;
	private boolean hardcore;
	private Difficulty difficulty;
	private WorldType worldType;
	
	public World(int dimension, boolean hardcore, Difficulty difficulty, WorldType worldType)
	{
		this.setDimension(dimension);
		this.setHardcore(hardcore);
		this.setDifficulty(difficulty);
		this.setWorldType(worldType);
	}

	public Difficulty getDifficulty()
	{
		return difficulty;
	}

	public void setDifficulty(Difficulty difficulty) 
	{
		this.difficulty = difficulty;
	}

	public WorldType getWorldType() 
	{
		return worldType;
	}

	public void setWorldType(WorldType worldType)
	{
		this.worldType = worldType;
	}

	public boolean isHardcore() 
	{
		return hardcore;
	}

	public void setHardcore(boolean hardcore) {
		this.hardcore = hardcore;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

}
