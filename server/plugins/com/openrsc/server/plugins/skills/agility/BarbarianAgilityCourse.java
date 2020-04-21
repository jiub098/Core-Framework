package com.openrsc.server.plugins.skills.agility;

import com.openrsc.server.constants.Skills;
import com.openrsc.server.model.entity.GameObject;
import com.openrsc.server.model.entity.player.Player;
import com.openrsc.server.plugins.Functions;
import com.openrsc.server.plugins.triggers.OpLocTrigger;
import com.openrsc.server.plugins.triggers.OpBoundTrigger;
import com.openrsc.server.util.rsc.Formulae;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.openrsc.server.plugins.Functions.*;

public class BarbarianAgilityCourse implements OpBoundTrigger,
	OpLocTrigger {

	private static final int LOW_WALL = 163;
	private static final int LOW_WALL2 = 164;
	private static final int LEDGE = 678;
	private static final int NET = 677;
	private static final int LOG = 676;
	private static final int PIPE = 671;
	private static final int BACK_PIPE = 672;
	private static final int SWING = 675;
	private static final int HANDHOLDS = 679;

	//private static final int[] obstacleOrder = {SWING, LOG, NET, LEDGE, LOW_WALL, LOW_WALL2};
	private static Set<Integer> obstacles = new HashSet<Integer>(Arrays.asList(SWING, LOG, NET, LEDGE, LOW_WALL));
	private static Integer lastObstacle = LOW_WALL2;

	@Override
	public boolean blockOpLoc(GameObject obj, String command, Player player) {
		return inArray(obj.getID(), PIPE, BACK_PIPE, SWING, LOG, LEDGE, NET, HANDHOLDS);
	}

	@Override
	public void onOpLoc(GameObject obj, String command, Player p) {
		if (obj.getID() == BACK_PIPE || obj.getID() == PIPE) {
			if (getCurrentLevel(p, Skills.AGILITY) < 35) {
				p.message("You need an agility level of 35 to attempt to squeeze through the pipe");
				return;
			}
			if (obj.getWorld().getServer().getConfig().WANT_FATIGUE) {
				if (obj.getWorld().getServer().getConfig().STOP_SKILLING_FATIGUED >= 1
					&& p.getFatigue() >= p.MAX_FATIGUE) {
					p.message("You are too tired to squeeze through the pipe");
					return;
				}
			}
			p.setBusy(true);
			p.message("You squeeze through the pipe");
			Functions.delay(p.getWorld().getServer().getConfig().GAME_TICK * 3);
			if (p.getY() <= 551) {
				teleport(p, 487, 554);
			} else {
				teleport(p, 487, 551);
			}
			p.incExp(Skills.AGILITY, 20, true);
			p.setBusy(false);
			return;
		}
		if (p.getWorld().getServer().getConfig().WANT_FATIGUE) {
			if (p.getWorld().getServer().getConfig().STOP_SKILLING_FATIGUED >= 1
				&& p.getFatigue() >= p.MAX_FATIGUE && !inArray(obj.getID(), LEDGE)) {
				p.message("you are too tired to train");
				return;
			}
		}
		p.setBusy(true);
		final boolean passObstacle = succeed(p);
		switch (obj.getID()) {
			case SWING:
				p.message("You grab the rope and try and swing across");
				delay(p.getWorld().getServer().getConfig().GAME_TICK * 3);
				if (passObstacle) {
					p.message("You skillfully swing across the hole");
					delay(p.getWorld().getServer().getConfig().GAME_TICK * 3);
					teleport(p, 486, 559);
					p.incExp(Skills.AGILITY, 80, true);
					AgilityUtils.completedObstacle(p, obj.getID(), obstacles, lastObstacle, 300);
					p.setBusy(false);
				} else {
					p.message("Your hands slip and you fall to the level below");
					delay(p.getWorld().getServer().getConfig().GAME_TICK * 3);
					teleport(p, 486, 3389);
					p.message("You land painfully on the spikes");
					delay(p.getWorld().getServer().getConfig().GAME_TICK * 3);
					int swingDamage = (int) Math.round((p.getSkills().getLevel(Skills.HITS)) * 0.15D);
					p.damage(swingDamage);
					say(p, "ouch");
					p.setBusy(false);
				}
				break;
			case LOG:
				p.message("you stand on the slippery log");
				delay(p.getWorld().getServer().getConfig().GAME_TICK * 3);
				if (passObstacle) {
					teleport(p, 489, 563);
					delay(p.getWorld().getServer().getConfig().GAME_TICK);
					teleport(p, 490, 563);
					delay(p.getWorld().getServer().getConfig().GAME_TICK);
					p.message("and walk across");
					teleport(p, 492, 563);
					p.incExp(Skills.AGILITY, 50, true);
					AgilityUtils.completedObstacle(p, obj.getID(), obstacles, lastObstacle, 300);
					p.face(495, 563);
					p.setBusy(false);
				} else {
					int slipDamage = (int) Math.round((p.getSkills().getLevel(Skills.HITS)) * 0.1D);
					p.message("Your lose your footing and land in the water");
					teleport(p, 490, 561);
					p.message("Something in the water bites you");
					p.damage(slipDamage);
					p.setBusy(false);
				}
				break;
			case NET:
				p.message("You climb up the netting");
				teleport(p, 496, 1507);
				p.incExp(Skills.AGILITY, 50, true);
				AgilityUtils.completedObstacle(p, obj.getID(), obstacles, lastObstacle, 300);
				break;
			case LEDGE:
				if (obj.getX() != 498) {
					return;
				}
				p.message("You put your foot on the ledge and try to edge across");
				delay(p.getWorld().getServer().getConfig().GAME_TICK * 2);
				if (passObstacle) {
					teleport(p, 501, 1506);
					p.message("You skillfully balance across the hole");
					p.incExp(Skills.AGILITY, 80, true);
					AgilityUtils.completedObstacle(p, obj.getID(), obstacles, lastObstacle, 300);
				} else {
					int ledgeDamage = (int) Math.round((p.getSkills().getLevel(Skills.HITS)) * 0.15D);
					p.message("you lose your footing and fall to the level below");
					teleport(p, 499, 563);
					p.message("You land painfully on the spikes");
					p.damage(ledgeDamage);
					say(p, null, "ouch");
				}
				p.setBusy(false);
				break;
			case HANDHOLDS:
				p.message("You climb up the wall");
				teleport(p, 497, 555);
				p.incExp(Skills.AGILITY
					, 20, true);
				break;
		}

		p.setBusy(false);
	}

	@Override
	public boolean blockOpBound(GameObject obj, Integer click, Player player) {
		return inArray(obj.getID(), LOW_WALL, LOW_WALL2);
	}

	@Override
	public void onOpBound(GameObject obj, Integer click, Player p) {
		if (p.getWorld().getServer().getConfig().WANT_FATIGUE) {
			if (p.getWorld().getServer().getConfig().STOP_SKILLING_FATIGUED >= 1
				&& p.getFatigue() >= p.MAX_FATIGUE) {
				p.message("you are too tired to train");
				return;
			}
		}
		p.setBusy(true);
		p.message("You jump over the wall");
		delay(p.getWorld().getServer().getConfig().GAME_TICK * 2);
		teleport(p, p.getX() == obj.getX() ? p.getX() - 1 : p.getX() + 1, p.getY());
		p.incExp(Skills.AGILITY, 20, true);
		AgilityUtils.completedObstacle(p, obj.getID(), obstacles, lastObstacle, 300);
		p.setBusy(false);
	}

	private boolean succeed(Player player) {
		return Formulae.calcProductionSuccessful(35, getCurrentLevel(player, Skills.AGILITY), true, 65);
	}

}
