package fr.vahren.eossim.model;

/**
 * Created by fdroumaguet on 23/03/2017.
 */
public class Player extends Unit {

    public Player(){
        g = '@';
    }


    @Override
    protected void onDeath(Unit source) {

    }
}
