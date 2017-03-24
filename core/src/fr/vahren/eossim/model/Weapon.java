package fr.vahren.eossim.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fdroumaguet on 23/03/2017.
 */
public class Weapon extends Item {

    public Map<Stat,Integer> statRequirements = new HashMap<>();

    public boolean elem = false;

    public int baseDamage = 0;
}
