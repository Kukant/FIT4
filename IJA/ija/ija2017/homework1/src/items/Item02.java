package ija.ija2017.homework1.items;

import ija.ija2017.homework1.items.AbstractItem;

public class Item02 extends AbstractItem {

    public Item02(String Name,int Ports){
        this.Name = Name;
        this.Ports = Ports;
        this.State = 0;
    }

    public void execute(){
        this.State = this.State + 2;
    }

}