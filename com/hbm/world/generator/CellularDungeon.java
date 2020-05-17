package com.hbm.world.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class CellularDungeon {

	// a buffer "map" of the rooms being generated before being spawned in
	CellularDungeonRoom[][] cells;
	EnumFacing[][] doors;
	// the order in which the buffer should be processed
	private List<int[]> order = new ArrayList<int[]>();

	// the size of the cell array x
	int dimX;
	// the size of the cell array z
	int dimZ;
	// the base width (and length) of a room
	public int width;
	// the height of a room
	public int height;
	// list of random floor blocks with equal weight
	public List<Block> floor = new ArrayList<Block>();
	// list of random ceiling blocks with equal weight
	public List<Block> ceiling = new ArrayList<Block>();
	// list of random wall blocks with equal weight
	public List<Block> wall = new ArrayList<Block>();
	// the rooms that the dungeon can use
	public List<CellularDungeonRoom> rooms = new ArrayList<CellularDungeonRoom>();
	int tries;

	public CellularDungeon(int width, int height, int dimX, int dimZ, int tries) {

		this.dimX = dimX;
		this.dimZ = dimZ;
		this.width = width;
		this.height = height;
		this.tries = tries;
	}

	public CellularDungeon(int width, int height, int dimX, int dimZ, int tries, Block floor, Block ceiling, Block wall) {

		this.dimX = dimX;
		this.dimZ = dimZ;
		this.width = width;
		this.height = height;
		this.tries = tries;
		this.floor.add(floor);
		this.ceiling.add(ceiling);
		this.wall.add(wall);
	}

	public void generate(World world, int x, int y, int z, Random rand) {
		if(world.isRemote)
			return;
		
		x -= dimX * width / 2;
		z -= dimZ * width / 2;

		compose(rand);
		for(int[] coord : order) {

			if(coord == null || coord.length != 2)
				continue;

			int dx = coord[0];
			int dz = coord[1];

			if(cells[dx][dz] != null) {
				cells[dx][dz].generate(world, x + dx * (width - 1), y, z + dz * (width - 1), doors[dx][dz]);
			}
		}
	}

	int rec = 0;

	public void compose(Random rand) {

		cells = new CellularDungeonRoom[dimX][dimZ];
		doors = new EnumFacing[dimX][dimZ];
		order.clear();

		int startX = dimX / 2;
		int startZ = dimZ / 2;

		cells[startX][startZ] = DungeonToolbox.getRandom(rooms, rand);
		doors[startX][startZ] = null;
		order.add(new int[] { startX, startZ });

		rec = 0;
		addRoom(startX, startZ, rand, null, DungeonToolbox.getRandom(rooms, rand));
	}

	// if x and z are occupied, it will just use the next nearby random space
	private boolean addRoom(int x, int z, Random rand, EnumFacing door, CellularDungeonRoom room) {

		rec++;
		if(rec > tries)
			return false;

		if(x < 0 || z < 0 || x >= dimX || z >= dimZ)
			return false;

		if(cells[x][z] != null) {

			EnumFacing dir = getRandomDir(rand);
			addRoom(x + dir.getFrontOffsetX(), z + dir.getFrontOffsetZ(), rand, dir.getOpposite(), DungeonToolbox.getRandom(rooms, rand));
			return false;
		}

		// CellularDungeonRoom next = DungeonToolbox.getRandom(rooms, rand);

		if(room.daisyChain == null || addRoom(x + room.daisyDirection.getFrontOffsetX(), z + room.daisyDirection.getFrontOffsetZ(), rand, null, room.daisyChain)) {
			cells[x][z] = room;
			doors[x][z] = door;
			order.add(new int[] { x, z });
		}

		// if(room.daisyChain == null)
		for(int i = 0; i < 3; i++) {
			EnumFacing dir = getRandomDir(rand);
			addRoom(x + dir.getFrontOffsetX(), z + dir.getFrontOffsetZ(), rand, dir.getOpposite(), DungeonToolbox.getRandom(rooms, rand));
		}

		return true;
	}

	/*public boolean addDaisychain(int x, int z, Random rand, ForgeDirection door, CellularDungeonRoom room) {
	
		if(x < 0 || z < 0 || x >= dimX || z >= dimZ)
			return false;
		
		if(cells[x][z] != null)
			return false;
	}*/

	public static EnumFacing getRandomDir(Random rand) {

		return EnumFacing.getFront(rand.nextInt(4) + 2);
	}
}