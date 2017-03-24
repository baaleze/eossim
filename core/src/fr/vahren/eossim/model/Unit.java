package fr.vahren.eossim.model;

import com.badlogic.gdx.graphics.Color;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidLayers;
import squidpony.squidmath.Coord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fr.vahren.eossim.ESGame.gridHeight;
import static fr.vahren.eossim.ESGame.gridWidth;
import static fr.vahren.eossim.ESGame.rng;

/**
 * Created by fdroumaguet on 23/03/2017.
 * A unit.
 */
public abstract class Unit {

    public int level = 1;
    public String name = "default unit";
    public char g = '&';

    public Coord position = null;

    public int str = 0;
    public int dex = 0;
    public int aff = 0;
    public int voo = 0;
    public int cha = 0;

    public List<Item> equipement = new ArrayList<>();
    public List<Item> inventory = new ArrayList<>();

    public int hp = 0;
    public int energy = 0;
    protected Color color = SColor.DARK_BLUE_LAPIS_LAZULI;


    /**
     * Move the player if he isn't bumping into a wall or trying to go off the map somehow.
     * In a fully-fledged game, this would not be organized like this, but this is a one-file demo.
     * @param xmod delta x
     * @param ymod delta y
     * @param bareDungeon the dungeon structure
     */
    public void move(int xmod, int ymod, char[][] bareDungeon) {
        int newX = position.x + xmod, newY = position.y + ymod;
        if (newX >= 0 && newY >= 0 && newX < gridWidth && newY < gridHeight
                && bareDungeon[newX][newY] != '#')
        {
            translate(xmod, ymod);
        }
    }

    /**
     * Checks if the unit hits the target
     * @param target The target of the attack
     * @return true if it hits
     */
    public boolean doesHit(Unit target){
        int chance = Math.max(20,100-5*(currentStat(Stat.DEX)-target.currentStat(Stat.DEX)));
        return rng.between(1,100) < chance;
    }

    /**
     * Compute damage done by this unit with all its weapons on the target.
     * @param target Target unit of the attack.
     * @return Damage output.
     */
    public int getDamage(Unit target){
        int dmg = 0;
        for(Item eq:equipement) {
            if(eq instanceof Weapon) {
                Weapon w = (Weapon)eq;

                //[StatBonus] = SumForEachStat([Stat] - [RequiredStat])/NbStats
                double statBonus = 0.0;
                for(Map.Entry<Stat,Integer> stat:w.statRequirements.entrySet()){
                    statBonus += currentStat(stat.getKey()) - stat.getValue();
                }
                statBonus = statBonus/w.statRequirements.size();

                // [Dmg] = [BaseDamage]+[StatBonus] - [Res]
                dmg += w.baseDamage + statBonus - (w.elem ? target.currentStat(Stat.ERS) : target.currentStat(Stat.RES));
            }
        }
        return Math.max(0,dmg);
    }


    /**
     * Compute stats from base stat and equipement.
     * Gets the base stat then loop over equipement to add bonuses.
     * @param stat the stat to compute
     * @return final stat value
     */
    public int currentStat(Stat stat) {
        int base;
        switch (stat){
            case STR:
                base = str;
                break;
            case DEX:
                base = dex;
                break;
            case CHA:
                base = cha;
                break;
            case VOO:
                base = voo;
                break;
            case AFF:
                base = aff;
                break;
            case NRG:
                base = energy;
                break;
            case HP:
                base = hp;
                break;
            case MAXHP:
                base = str*2+5;
                break;
            case MAXNRG:
                base = voo*2+5;
                break;
            case ARM:
                base = 0;
                break;
            case ERS:
                base = aff/2;
                break;
            case SRS:
                base = voo/2;
                break;
            case ESQ:
                base = 0;
                break;
            case RES:
                base = str/2;
                break;
            default:
                base = 0;
        }
        for(Item e:equipement){
            for(Effect eff:e.effects){
                if(eff instanceof StatEffect && ((StatEffect)eff).stat == stat){
                    base+=eff.value;
                }
            }
        }
        return base;
    }

    public void translate(int xmod, int ymod) {
        position = position.translate(xmod,ymod);
    }

    public void render(SquidLayers display) {
        display.put(position.x, position.y, g, color);
    }

    /**
     * Applies damage
     * @param source Attacker
     * @param dmg damage value
     */
    public void damage(Unit source,int dmg){
        hp -= dmg;
        if(hp<=0){
            onDeath(source);
        }
    }

    protected abstract void onDeath(Unit source);
}
