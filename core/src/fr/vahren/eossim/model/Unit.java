package fr.vahren.eossim.model;

import squidpony.squidmath.Coord;

import java.util.ArrayList;
import java.util.List;

import static fr.vahren.eossim.ESGame.gridHeight;
import static fr.vahren.eossim.ESGame.gridWidth;

/**
 * Created by fdroumaguet on 23/03/2017.
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


    /**
     * Move the player if he isn't bumping into a wall or trying to go off the map somehow.
     * In a fully-fledged game, this would not be organized like this, but this is a one-file demo.
     * @param xmod
     * @param ymod
     * @param bareDungeon
     */
    public void move(int xmod, int ymod, char[][] bareDungeon) {
        int newX = position.x + xmod, newY = position.y + ymod;
        if (newX >= 0 && newY >= 0 && newX < gridWidth && newY < gridHeight
                && bareDungeon[newX][newY] != '#')
        {
            translate(xmod, ymod);
        }
    }




    public int maxHp(){
        return str*2+5;
    }
    public int maxEnergy(){
        return voo*2+5;
    }

    public int currentStr(){
        return computeCurrentStat(Stat.STR);
    }

    public int currentDex(){
        return computeCurrentStat(Stat.DEX);
    }
    public int currentAff(){
        return computeCurrentStat(Stat.AFF);
    }
    public int currentVoo(){
        return computeCurrentStat(Stat.VOO);
    }
    public int currentCha(){
        return computeCurrentStat(Stat.CHA);
    }

    private int computeCurrentStat(Stat stat) {
        int base = str;
        for(Item e:equipement){
            for(Effect eff:e.effects){
                if(eff instanceof StatEffect && ((StatEffect)eff).stat == stat){
                    base+=eff.value;
                }
            }
        }
        return base;
    }

    public int currentArmor(){
        return computeCurrentStat(Stat.ARM);
    }
    public int currentPhyRes(){
        return computeCurrentStat(Stat.RES);
    }
    public int currentElemRes(){
        return computeCurrentStat(Stat.ERS);
    }
    public int currentSpellRes(){
        return computeCurrentStat(Stat.SRS);
    }
    public int currentHp(){
        return computeCurrentStat(Stat.HP);
    }
    public int currentEnergy(){
        return computeCurrentStat(Stat.NRG);
    }

    public void translate(int xmod, int ymod) {
        position = position.translate(xmod,ymod);
    }
}
