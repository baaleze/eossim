package fr.vahren.eossim.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fdroumaguet on 23/03/2017.
 */
public abstract class Item {

    public String name = "default item";

    public int level = 1;

    public List<Effect> effects = new ArrayList<>();

}
