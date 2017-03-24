package fr.vahren.eossim.model;

import squidpony.squidgrid.gui.gdx.SColor;

/**
 * Created by fdroumaguet on 23/03/2017.
 */
public class Enemy extends Unit {

    public Enemy(){
        g = '$';
        color = SColor.RED_OCHRE;
    }

    @Override
    protected void onDeath(Unit source) {

    }
}
