package io.github.mruks.projectchucm;

import org.bukkit.scheduler.BukkitRunnable;

public class UHCTimer extends BukkitRunnable {
	private int counter = 0;
	private int adder = 0;
	
	public UHCTimer(int adder) {
		super();
		this.adder = adder;
		counter = adder;
	}

	@Override
	public void run() {
		ProjectChucm.sendAllMessage("[SERVER] MARK " + counter + " MINS IN");
		counter += adder;
	}
}
