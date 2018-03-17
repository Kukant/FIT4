package ija.ija2017.homework1.items;

import ija.ija2017.repository.Item;

abstract public class AbstractItem implements Item {

    protected String Name;
    protected int State;
    protected int Ports;

    public String getName(){
        return this.Name;
    }

    public int numberOfPorts(){
        return this.Ports;
    }

    public int getState(){
        return this.State;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (int i = 0; i < this.Name.length(); i++) {
            hash = hash*31 + this.Name.charAt(i);
        }
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {


        final AbstractItem myObj = (AbstractItem) obj;
        if (myObj.Name == this.Name) {
            return true;
        } else {
            return false;
        }
    }

}