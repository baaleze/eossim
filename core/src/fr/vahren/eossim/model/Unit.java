package fr.vahren.eossim.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fdroumaguet on 23/03/2017.
 */
public interface Unit {

    int level = 1;
    String name = "default unit";

    int str = 0;
    int dex = 0;
    int aff = 0;
    int voo = 0;
    int cha = 0;

    List<Item> equipement = new ArrayList<>();
    List<Item> inventory = new ArrayList<>();

    int currentHp = 0;
    int currentMana = 0;

    int maxHp();
    int maxMana();

    int currentStr();
    int currentDex();
    int currentAff();
    int currentVoo();
    int currentCha();

    int currentArmor();
    int currentPhyRes();
    int currentElemRes();
    int currentSpellRes();


}
