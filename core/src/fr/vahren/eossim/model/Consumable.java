package fr.vahren.eossim.model;

import squidpony.squidmath.Coord;

import java.util.List;

/**
 * Created by fdroumaguet on 23/03/2017.
 */
public interface Consumable {

    void useOn(Unit source,Unit target);
    void useOnMultiple(Unit source, List<Unit> targets);
    void useThere(Unit source, Coord target);
}
